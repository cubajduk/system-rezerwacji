const {test}=require('node:test');
const assert=require('node:assert/strict');
const fs=require('node:fs');
const vm=require('node:vm');
const source=fs.readFileSync('src/main/resources/static/app.js','utf8');
function setup(){
  const elements=new Map();
  const $=id=>{if(!elements.has(id))elements.set(id,{value:'',addEventListener(){}});return elements.get(id)};
  const state={serviceSort:'name',serviceSortDirection:'asc',serviceListSearch:'',services:[
    {id:'a',name:'Terapia 30 min',defaultPrice:90,suggestedDurationMinutes:30,description:'Mowa'},
    {id:'b',name:'Terapia 60 min',defaultPrice:160,suggestedDurationMinutes:60},
    {id:'c',name:'Konsultacja',defaultPrice:250,suggestedDurationMinutes:null},
    {id:'d',name:'Usunięta',defaultPrice:1,deleted:true}
  ]};
  const context={state,$,inputDate:d=>`${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}T${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`,syncTimeInputsFromHidden(){},updateVisitDuration(){},refreshRoomAvailabilityOptions(){},refreshAppointmentDayPreview(){}};
  vm.createContext(context);vm.runInContext(source.slice(source.indexOf('function fillServiceOptions('),source.indexOf('function timeParts(')),context);
  return {...context};
}
test('service selection updates price and end for new and edited visits; missing duration uses 55 min',()=>{
  for(const editingId of [null,'existing']){
    const c=setup();c.state.editingId=editingId;c.$('startsAt').value='2026-09-07T10:15';
    for(const [id,price,end] of [['a','90.00','10:45'],['b','160.00','11:15'],['c','250.00','11:10']]){
      c.$('serviceId').value=id;c.applySelectedService();
      assert.equal(c.$('price').value,price);assert.equal(c.$('endsAt').value,`2026-09-07T${end}`);
      assert.equal(c.$('startsAt').value,'2026-09-07T10:15');
    }
  }
});
test('duration supports crossing midnight and manual durations for available slots',()=>{
  const c=setup();c.$('startsAt').value='2026-09-07T23:30';c.$('serviceId').value='b';c.applySelectedService();
  assert.equal(c.$('endsAt').value,'2026-09-08T00:30');assert.equal(c.currentVisitDuration(),60);
  c.$('endsAt').value='2026-09-08T00:45';assert.equal(c.currentVisitDuration(),75);
});
test('numeric sorting, descending sorting, search by description and removed entries',()=>{
  const c=setup();c.state.serviceSort='defaultPrice';
  assert.equal(c.sortedServices().map(s=>s.id).join(','),'a,b,c');
  c.state.serviceSortDirection='desc';assert.equal(c.sortedServices().map(s=>s.id).join(','),'c,b,a');
  c.state.serviceSort='suggestedDurationMinutes';c.state.serviceSortDirection='asc';assert.equal(c.sortedServices().map(s=>s.id).join(','),'a,c,b');
  c.state.serviceListSearch='mowa';assert.equal(c.sortedServices().map(s=>s.id).join(','),'a');
});
