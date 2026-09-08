const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const source=fs.readFileSync('src/main/resources/static/app.js','utf8');
function setup(){
  const timers=new Map(),select={value:'all'};let next=0,hit=null;
  const c={Date,state:{rooms:[{id:'a'},{id:'b'},{id:'c'}],appointments:[]},$:()=>select,
    document:{elementFromPoint:()=>({closest:()=>hit})},
    setTimeout:(fn,ms)=>{timers.set(++next,{fn,ms});return next},clearTimeout:id=>timers.delete(id),
    syncRoomLabel(){},drawCalendar(){},summary(){},render(){},alert(){},
    startOfWeek:d=>{d=new Date(d);d.setDate(d.getDate()-(d.getDay()+6)%7);d.setHours(0,0,0,0);return d},
    iso:d=>d.toISOString().slice(0,10),api:async()=>({ok:true,json:async()=>[]})};
  vm.createContext(c);vm.runInContext(source.slice(source.indexOf("function calendarOpening()"),source.indexOf("function calendarMinutes()"))+source.split("\n").find(line=>line.startsWith("function calendarMinutes()")),c);vm.runInContext(source.slice(source.indexOf('function clearDragNavigation'),source.indexOf("$('weekCalendar').addEventListener('pointerdown'")),c);
  vm.runInContext(source.slice(source.indexOf('function shortCalendarView'),source.indexOf('function weekPeriodLabel')),c);c.state.weekStart=c.startOfWeek(new Date());
  return {c,select,timers,hover:id=>{hit=id?{id,classList:{add(){},remove(){}}}:null;return hit},tick:async()=>{const [id,t]=timers.entries().next().value;timers.delete(id);assert.equal(t.ms,2000);await t.fn()}};
}
test('holding an arrow repeats every two seconds, skips all rooms and stops on exit',async()=>{
  const {c,select,timers,hover,tick}=setup();
  const drag={mode:'move',dragged:true};c.state.drag=drag;hover('nextRoom');c.updateDragNavigation(drag);
  assert.equal(select.value,'all');await tick();assert.equal(select.value,'a');await tick();assert.equal(select.value,'b');await tick();assert.equal(select.value,'c');await tick();assert.equal(select.value,'a');
  hover('previousRoom');c.updateDragNavigation(drag);await tick();assert.equal(select.value,'c');
  hover(null);c.updateDragNavigation(drag);assert.equal(timers.size,0);
  select.value='all';await c.navigateWhileDragging('previousRoom');assert.equal(select.value,'c');
});
test('week navigation stops at current week and keeps the drag alive',async()=>{
  const {c}=setup();const drag={};c.state.drag=drag;const today=+c.state.weekStart;
  await c.navigateWhileDragging('previousWeek');assert.equal(+c.state.weekStart,today);
  await c.navigateWhileDragging('nextWeek');assert.ok(+c.state.weekStart>today);assert.equal(c.state.drag,drag);
  await c.navigateWhileDragging('previousWeek');assert.equal(+c.state.weekStart,today);
});
test('Today switches after two seconds, keeps the card and stops once current week is shown',async()=>{
  const {c,timers,hover,tick}=setup();const today=+c.state.weekStart;
  c.state.weekStart.setDate(c.state.weekStart.getDate()+21);
  const drag={mode:'move',dragged:true};c.state.drag=drag;
  hover('goToday');c.updateDragNavigation(drag);
  assert.notEqual(+c.state.weekStart,today);await tick();
  assert.equal(+c.state.weekStart,today);assert.equal(c.state.drag,drag);assert.equal(timers.size,0);
  c.updateDragNavigation(drag);assert.equal(timers.size,0);
  c.api=()=>{throw Error('Current week must not reload')};await c.navigateWhileDragging('goToday');
});
test('cancelled drag ignores an outstanding week response; resize does not navigate',async()=>{
  const {c,timers,hover}=setup();const today=+c.state.weekStart;
  hover('nextRoom');c.updateDragNavigation({mode:'end',dragged:true});assert.equal(timers.size,0);
  c.state.drag={};let resolve;c.api=()=>new Promise(r=>resolve=r);
  const pending=c.navigateWhileDragging('nextWeek');c.state.drag=null;resolve({ok:true,json:async()=>[]});await pending;
  assert.equal(+c.state.weekStart,today);
});
test('dropping in another room uses its conflicts, date, pointer position and original duration',async()=>{
  const {c,select}=setup();select.value='b';let saved;
  c.calendar={hasPointerCapture:()=>false};c.document.elementFromPoint=()=>({closest:()=>({dataset:{day:'2026-10-06'},getBoundingClientRect:()=>({top:100})})});
  c.snapMinute=x=>Math.max(0,Math.min(720,Math.round(x/5)*5));c.appointmentTime=(day,minute)=>({day,minute});
  c.nearestFreeMoveStart=(a,day,start,duration)=>{assert.equal(a.roomId,'b');assert.equal(start,180);assert.equal(duration,55);return start};
  c.persistAppointmentTime=async(...args)=>saved=args;
  c.state.drag={a:{id:'visit',roomId:'a'},pointerId:1,mode:'move',dragged:true,grabY:20,originStart:60,originEnd:115,element:{classList:{remove(){}}}};
  await c.finishAppointmentDrag({pointerId:1,clientX:50,clientY:300});
  assert.equal(saved[0].roomId,'b');assert.equal(saved[1].day,'2026-10-06');assert.equal(saved[2].minute,235);assert.equal(c.state.drag,null);
});
