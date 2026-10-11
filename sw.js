// M3 Metró Szimulátor – offline működés.
// Telepítéskor mindent letölt (játék, ikonok, betűtípusok), utána net nélkül is indul.
const C='metro-szim-v10';
const CORE=['./','./index.html','./manifest.webmanifest','./icon-192.png','./icon-512.png','./apple-touch-icon.png'];
const FONT_CSS='https://fonts.googleapis.com/css2?family=Saira+Condensed:wght@500;600;700&family=IBM+Plex+Mono:wght@500;600&display=swap';
async function precacheFonts(c){
  try{const r=await fetch(FONT_CSS,{mode:'cors'});if(!r.ok)return;const css=await r.clone().text();await c.put(FONT_CSS,r);
    const urls=[...css.matchAll(/url\((https:\/\/fonts\.gstatic\.com\/[^)]+)\)/g)].map(m=>m[1]);
    await Promise.all(urls.map(u=>fetch(u,{mode:'cors'}).then(f=>f.ok&&c.put(u,f)).catch(()=>{})))}catch(e){}
}
self.addEventListener('install',e=>{e.waitUntil(caches.open(C).then(async c=>{await c.addAll(CORE);await precacheFonts(c)}));self.skipWaiting()});
self.addEventListener('activate',e=>{e.waitUntil(caches.keys().then(k=>Promise.all(k.filter(n=>n.startsWith('metro-szim')&&n!==C).map(n=>caches.delete(n)))).then(()=>self.clients.claim()))});
self.addEventListener('message',e=>{if(e.data==='precache')e.waitUntil(caches.open(C).then(async c=>{await c.addAll(CORE).catch(()=>{});await precacheFonts(c)}))});
self.addEventListener('fetch',e=>{const r=e.request;if(r.method!=='GET')return;
  const nav=r.mode==='navigate',key=nav?'./index.html':r;
  e.respondWith(caches.open(C).then(async c=>{
    const hit=await c.match(key,{ignoreSearch:nav});
    const net=fetch(r).then(res=>{if(res&&(res.ok||res.type==='opaque'))c.put(key,res.clone());return res}).catch(()=>null);
    if(hit){e.waitUntil(net);return hit}
    const res=await net;if(res)return res;
    if(nav){const idx=await c.match('./index.html');if(idx)return idx}
    return new Response('Offline – nyisd meg egyszer net mellett az appot, utána net nélkül is működik.',{status:503,headers:{'Content-Type':'text/plain; charset=utf-8'}});
  }))});
