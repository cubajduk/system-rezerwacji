const {test}=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm');
const source=fs.readFileSync('src/main/resources/static/app.js','utf8');
function setup(){
 const c={Date,state:{calendarMode:'week',calendarDate:'2026-09-13',weekStart:new Date('2026-09-07T00:00:00')},iso:d=>`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`,startOfWeek:d=>{d=new Date(d);d.setDate(d.getDate()-(d.getDay()+6)%7);d.setHours(0,0,0,0);return d},render(){},load:async()=>{}};
 vm.createContext(c);const start=source.indexOf('function shortCalendarView');vm.runInContext(source.slice(start,source.indexOf('function weekPeriodLabel',start)),c);
 const shift=source.indexOf('async function shiftCalendar');vm.runInContext(source.slice(shift,source.indexOf('\n}',shift)+2),c);return c;
}
test('four modes select correct dates including Sunday to Monday',()=>{
 const c=setup();for(const [mode,count,start,end] of [['week',7,'2026-09-07','2026-09-13'],['workweek',5,'2026-09-07','2026-09-11'],['day',1,'2026-09-13','2026-09-13'],['twoDays',2,'2026-09-13','2026-09-14']]){c.state.calendarMode=mode;const range=c.calendarRange();assert.equal(range.count,count);assert.equal(c.iso(range.start),start);assert.equal(c.iso(range.end),end)}
});
test('short view navigation crosses month boundary and Today restores current date',async()=>{
 const c=setup();c.state.calendarMode='twoDays';c.state.calendarDate='2026-09-30';await c.shiftCalendar(1);assert.equal(c.state.calendarDate,'2026-10-02');await c.shiftCalendar(-1);assert.equal(c.state.calendarDate,'2026-09-30');
 c.state.calendarMode='day';await c.shiftCalendar(1);assert.equal(c.state.calendarDate,'2026-10-01');await c.shiftCalendar(0);assert.equal(c.state.calendarDate,c.iso(new Date()));
});
test('weekends toggle gives Friday/Monday and navigation skips excluded days in both directions',async()=>{
 const c=setup();c.state.calendarMode='twoDays';c.state.calendarDate='2026-09-11';c.state.showWeekends=false;
 assert.deepEqual(Array.from(c.calendarRange().dates,c.iso),['2026-09-11','2026-09-14']);
 await c.shiftCalendar(1);assert.equal(c.state.calendarDate,'2026-09-15');await c.shiftCalendar(-1);assert.equal(c.state.calendarDate,'2026-09-11');
 c.state.showWeekends=true;assert.deepEqual(Array.from(c.calendarRange().dates,c.iso),['2026-09-11','2026-09-12']);
 c.state.calendarMode='threeDays';c.state.showWeekends=false;assert.deepEqual(Array.from(c.calendarRange().dates,c.iso),['2026-09-11','2026-09-14','2026-09-15']);
});
