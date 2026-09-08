const {test}=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm');
const source=fs.readFileSync('src/main/resources/static/app.js','utf8');
function setup(){
  const c={Date,state:{rooms:[{id:'r1'},{id:'r2'}],schedules:[{specialistId:'s1',startDate:'2026-09-08',startTime:'09:00',endTime:'12:00',recurrence:'ONCE',daysOfWeek:[]}]},$:()=>({value:'r1'}),iso:d=>`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`};
  vm.createContext(c);vm.runInContext(source.slice(source.indexOf('function firstSpecialistSlot'),source.indexOf('async function setFirstAvailableSpecialistSlot')),c);
  vm.runInContext(source.slice(source.indexOf('function scheduleOccursOn'),source.indexOf('\n',source.indexOf('function scheduleOccursOn'))),c);
  c.find=(appointments=[],now='2026-09-08T08:00:00')=>c.firstSpecialistSlot('s1',55,new Date(now),new Date('2026-09-10T23:59:00'),appointments);
  return c;
}
test('uses work start and rounds current time forward to a whole minute',()=>{
  const c=setup();assert.equal(c.find().start.getHours(),9);
  const slot=c.find([],'2026-09-08T09:17:20');assert.equal(slot.start.getMinutes(),18);assert.equal((slot.end-slot.start)/60000,55);
});
test('skips specialist conflicts and selects another free room',()=>{
  const c=setup(),appointments=[{specialistId:'s1',roomId:'r1',startsAt:'2026-09-08T09:00',endsAt:'2026-09-08T10:00'},{specialistId:'other',roomId:'r1',startsAt:'2026-09-08T10:00',endsAt:'2026-09-08T12:00'}];
  const slot=c.find(appointments);assert.equal(slot.start.getHours(),10);assert.equal(slot.roomId,'r2');
});
test('moves to next work day if remaining work period cannot fit duration',()=>{
  const c=setup();c.state.schedules.push({...c.state.schedules[0],startDate:'2026-09-10',startTime:'08:15'});
  const slot=c.find([],'2026-09-08T11:30:00');assert.equal(slot.start.getDate(),10);assert.equal(slot.start.getHours(),8);assert.equal(slot.start.getMinutes(),15);
});
test('all rooms occupied blocks a slot, cancelled visits do not, and missing schedules return null',()=>{
  const c=setup(),appointments=['r1','r2'].map(roomId=>({specialistId:'other',roomId,startsAt:'2026-09-08T09:00',endsAt:'2026-09-08T12:00'}));
  assert.equal(c.find(appointments),null);appointments[0].status='CANCELLED_BY_CLIENT';assert.equal(c.find(appointments).roomId,'r1');
  c.state.schedules=[];assert.equal(c.find(),null);
});
