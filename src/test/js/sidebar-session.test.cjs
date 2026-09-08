const {test}=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm');
const source=fs.readFileSync('src/main/resources/static/app.js','utf8');
function fn(name){return source.split('\n').find(line=>line.startsWith('function '+name+'('))}
test('Polish visit counts handle singular, plural and teens',()=>{const c={};vm.createContext(c);vm.runInContext(fn('visitNoun'),c);for(const [n,label] of [[0,'wizyt'],[1,'wizyta'],[2,'wizyty'],[4,'wizyty'],[5,'wizyt'],[12,'wizyt'],[14,'wizyt'],[21,'wizyt'],[22,'wizyty'],[112,'wizyt']])assert.equal(c.visitNoun(n),label)});
test('new reservation clears stale selections and only searches with a selected specialist',()=>{
 const elements=new Map();const $=id=>{if(!elements.has(id))elements.set(id,{value:'',showModal(){}});return elements.get(id)};let searches=0;
 const c={Date,$,state:{auth:'yes',clients:[{}],specialists:[{}],rooms:[{}],schedules:[]},cancelFirstSlotSearch(){},fillServiceOptions(){},syncTimeInputsFromHidden(){},suggestRoomIfNeeded(){},refreshRoomAvailabilityOptions(){},refreshAppointmentDayPreview(){},inputDate:d=>d.toISOString(),setFirstAvailableSpecialistSlot(){searches++}};
 vm.createContext(c);vm.runInContext(fn('newVisit'),c);$('specialistFilter').value='all';$('roomFilter').value='all';$('specialistId').value='stale';$('roomId').value='stale';c.newVisit();assert.equal($('specialistId').value,'');assert.equal($('roomId').value,'');assert.equal(searches,0);
 $('specialistFilter').value='selected';$('roomFilter').value='room';c.newVisit();assert.equal($('specialistId').value,'selected');assert.equal($('roomId').value,'room');assert.equal(searches,1);
});
test('API rejects data arriving after logout and never uses browser credentials',async()=>{
 let resolve;const c={state:{auth:'session'},fetch:async(path,options)=>{assert.equal(options.credentials,'omit');return {json:()=>new Promise(r=>resolve=r)}}};vm.createContext(c);vm.runInContext(source.split('\n').find(line=>line.startsWith('const api='))+'\nthis.callApi=api',c);const response=await c.callApi('/api/clients');const pending=response.json();c.state.auth=null;resolve([{name:'Private'}]);await assert.rejects(pending,/Sesja została zakończona/);await assert.rejects(c.callApi('/api/clients'),/Zaloguj/);
});
