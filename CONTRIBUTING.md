# Contributing

## English

Thanks for helping! The project is a single-file vanilla JS game (`index.html`), no build step.

- **Run locally:** `python3 -m http.server 8000`, then open http://localhost:8000
- **Check before a PR:** extract the script blocks and syntax-check them, then load the page and click through the menu and a short run with no console errors:
  `python3 -c "import re;s=open('index.html',encoding='utf-8').read();open('/tmp/a.js','w').write('\n'.join(re.findall(r'<script>(.*?)</script>',s,re.S)))" && node --check /tmp/a.js`
- **Style:** keep the single-file structure and the existing code style; the UI text is Hungarian. `index.html` has very long lines, so search with `grep -n -o` instead of opening the whole file.
- **Version:** bump `APP_VER` in `index.html` and the cache name `C` in `sw.js`, otherwise installed copies will not update. If you add a file, list it in `android/app.properties` (`files=`) and in `sw.js` (`CORE`).
- **Pull requests:** fork, create a branch, make a small focused change, open a PR describing what and why (a screenshot helps).

## Magyar

Köszönöm a segítséget! A projekt egyetlen fájlból álló, tiszta JS játék (`index.html`), nincs build lépés.

- **Helyi futtatás:** `python3 -m http.server 8000`, majd http://localhost:8000
- **Ellenőrzés PR előtt:** a `<script>` blokkok kinyerése és `node --check` (lásd fent), majd töltsd be az oldalt, kattints végig a menün és egy rövid menetet, konzolhiba nélkül.
- **Stílus:** maradjon az egyfájlos felépítés és a meglévő kódstílus; a felület szövegei magyarok. Az `index.html`-ben nagyon hosszú sorok vannak, ezért `grep -n -o`-val keress.
- **Verzió:** emeld az `APP_VER`-t az `index.html`-ben és a gyorsítótár nevét (`C`) az `sw.js`-ben, különben a telepített példányok nem frissülnek. Új fájlt vedd fel az `android/app.properties` `files=` sorába és az `sw.js` `CORE` listájába.
- **Pull request:** fork, új ág, kicsi és célzott változtatás, a PR leírja a mit és miértet (képernyőkép jól jön).
