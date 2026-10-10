// Offline: minden fájl a gyorsítótárból jön azonnal, a háttérben frissül, ha van net.
const C='metro-szim-v4';
const CORE=['./','./index.html','./manifest.webmanifest','./icon-192.png','./icon-512.png','./apple-touch-icon.png'];
self.addEventListener('install',e=>{e.waitUntil(caches.open(C).then(c=>c.addAll(CORE)));self.skipWaiting()});
self.addEventListener('activate',e=>{e.waitUntil(caches.keys().then(k=>Promise.all(k.filter(n=>n!==C).map(n=>caches.delete(n)))).then(()=>self.clients.claim()))});
self.addEventListener('fetch',e=>{const r=e.request;if(r.method!=='GET')return;
  const nav=r.mode==='navigate';
  e.respondWith(caches.open(C).then(async c=>{
    const hit=await c.match(nav?'./index.html':r,{ignoreSearch:true});
    const net=fetch(r).then(res=>{if(res&&(res.ok||res.type==='opaque'))c.put(nav?'./index.html':r,res.clone());return res}).catch(()=>null);
    if(hit){e.waitUntil(net);return hit}
    const res=await net;return res||new Response('Offline – nyisd meg egyszer net mellett az appot.',{status:503,headers:{'Content-Type':'text/plain; charset=utf-8'}});
  }))});
