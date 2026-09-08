const {test}=require('node:test'),assert=require('node:assert/strict'),fs=require('node:fs'),vm=require('node:vm');
const source=fs.readFileSync('src/main/resources/static/app.js','utf8');
test('highlight keeps all visits visible and resets when the global count reaches zero',()=>{
 const elements=new Map();const $=id=>{if(!elements.has(id))elements.set(id,{value:'all',setAttribute(key,value){this[key]=value}});return elements.get(id)};
 const classes=new Set();const c={state:{auth:true,pendingCount:7,pendingHighlight:true,specialists:[]},$,calendar:{classList:{toggle(name,on){on?classes.add(name):classes.delete(name)}}}};vm.createContext(c);
 for(const name of ['visible','syncPendingHighlight']){const start=source.indexOf('function '+name+'(');vm.runInContext(source.slice(start,source.indexOf('\n',start)),c)}
 c.syncPendingHighlight();assert.equal($('pendingCount').textContent,7);assert.equal($('pendingFilter').hidden,false);assert.equal($('pendingFilter')['aria-pressed'],'true');assert.equal(classes.has('highlight-pending'),true);
 assert.equal([{status:'CONFIRMED'},{status:'SCHEDULED'}].filter(c.visible).length,2);
 c.state.pendingHighlight=false;c.syncPendingHighlight();assert.equal(classes.size,0);assert.equal($('pendingFilter').hidden,false);
 c.state.pendingHighlight=true;c.state.pendingCount=0;c.syncPendingHighlight();assert.equal($('pendingFilter').hidden,true);assert.equal(c.state.pendingHighlight,false);assert.equal(classes.size,0);assert.equal($('pendingFilter')['aria-pressed'],'false');
});
