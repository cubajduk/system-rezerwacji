const {test}=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm');
const source=fs.readFileSync('src/main/resources/static/app.js','utf8');
function setup(){
 const elements=new Map(),a={id:'a',clientId:'c',specialistId:'s',roomId:'r',status:'SCHEDULED',startsAt:'2026-09-08T09:00',endsAt:'2026-09-08T09:55'};
 const c={Date,state:{editingId:'a',appointments:[a,{...a,id:'b'}],previewAppointments:[a],pendingCount:2},$:id=>{if(!elements.has(id))elements.set(id,{disabled:false,close(){this.closed=true},showModal(){this.closed=false}});return elements.get(id)},client:()=> 'Jan Kowalski',specialist:()=> 'Anna Nowak',room:()=> 'G1',confirm:()=>false,render(){},load:async()=>{},alert(){},api:async()=>{throw Error('Unexpected request')}};
 vm.createContext(c);vm.runInContext(source.slice(source.indexOf('function appointmentDeletionMessage'),source.indexOf("$('deleteAppointment').addEventListener")),c);return c;
}
test('cancel confirmation sends no deletion and message identifies the saved visit',async()=>{
 const c=setup();const message=c.appointmentDeletionMessage(c.state.appointments[0]);for(const text of ['Jan Kowalski','Anna Nowak','08.09.2026','09:00','09:55','G1'])assert.ok(message.includes(text));
 await c.deleteEditedAppointment();assert.equal(c.state.appointments.length,2);
});
test('confirmed deletion removes only selected visit and refreshes pending count',async()=>{
 const c=setup();c.confirm=()=>true;c.api=async(path,options)=>{assert.equal(path,'/api/appointments/a');assert.equal(options.method,'DELETE');return {ok:true}};
 await c.deleteEditedAppointment();assert.equal(c.state.appointments.length,1);assert.equal(c.state.appointments[0].id,'b');assert.equal(c.state.pendingCount,1);assert.equal(c.$('appointmentDialog').closed,true);
});
test('failed deletion leaves visit and displays error; opening edit clears previous error',async()=>{
 const c=setup();c.confirm=()=>true;c.api=async()=>({ok:false,json:async()=>({message:'Brak uprawnień'})});await c.deleteEditedAppointment();assert.equal(c.state.appointments.length,2);assert.equal(c.$('appointmentError').textContent,'Brak uprawnień');
 Object.assign(c,{cancelFirstSlotSearch(){},fillServiceOptions(){},toggleRecurrence(){},refreshRoomAvailabilityOptions(){}});
 const start=source.indexOf('function edit(id)');vm.runInContext(source.slice(start,source.indexOf('\n',start)),c);c.edit('a');assert.equal(c.$('appointmentError').textContent,'');assert.equal(c.$('deleteAppointment').hidden,false);
});

test('series deletion requests future scope and preserves earlier visits',async()=>{
 const c=setup();const a=c.state.appointments[0];a.recurrenceGroupId='series';c.state.appointments.push({...a,id:'later',startsAt:'2026-09-15T09:00'},{...a,id:'earlier',startsAt:'2026-09-01T09:00'});c.chooseDeletionScope=async()=> 'series';c.api=async(path)=>{assert.equal(path,'/api/appointments/a?deleteSeries=true');return {ok:true}};await c.deleteEditedAppointment();assert.deepEqual(Array.from(c.state.appointments,x=>x.id),['b','earlier']);
});
test('cancelling series dialog sends no request',async()=>{const c=setup();c.state.appointments[0].recurrenceGroupId='series';c.chooseDeletionScope=async()=> 'cancel';await c.deleteEditedAppointment();assert.equal(c.state.appointments.length,2)});
test('price arrows round in their direction to multiples of ten',()=>{const c={};vm.createContext(c);vm.runInContext(source.split('\n').find(line=>line.startsWith('function steppedPrice')),c);for(const [value,direction,expected] of [[15,1,20],[20,1,30],[248,-1,240],[240,-1,230],[0,-1,0],[15.99,1,20]])assert.equal(c.steppedPrice(value,direction),expected)});
