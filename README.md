# M3 Metró Szimulátor

2D metróvezető-játék (PWA) a 81-717-es szerelvénnyel. Telepíthető, internet nélkül is fut.

## Felszíni vasút
A metró mellett négy felszíni útvonal is választható (Új menet → Felszíni vasút):

| Útvonal | Szerelvény | Megállók |
|---|---|---|
| Bzmot – Balaton-felvidék | Bzmot motorkocsi + Btx vezérlőkocsi | Balatonfüred → Révfülöp |
| Csörgő – Badacsony | M41 „Csörgő” + 3 Bhv személykocsi | Révfülöp → Tapolca |
| Szili – Dél-Balaton | V43 „Szili” + 4 Bhv személykocsi (felsővezeték alatt) | Siófok → Fonyód |
| Tehervonat – Százhalombatta | M44 „Bobó” + nyitott, tartály- és fedett teherkocsik | Budapest-Ferencváros → Százhalombatta |

- A járműveket a játék rajzolja meg induláskor (a metrókocsi festett, kopott stílusában), nincs hozzájuk külön képfájl.
- Részletek: forgóvázak tengelyágy-csavarokkal, rugókkal, lengéscsillapítókkal és fékpofákkal; ütközők, csavarkapocs, fékcső; homlok- és zárjelző lámpák; kürtök; tetőszellőzők; zsalus gépterem-ajtók; a személykocsikon lebukó ablakok és útvonaltábla (pl. „Siófok – Fonyód”); teherkocsikon kézifék-kerék, veszélyességi bárca. Festés: Bzmot piros–sárga („piroska”), M41 gyári piros, V43 a 2000-es sorozat kék–sárga–szürke–fehér („Papagáj”) festésével, 15,7 m hosszú, 1180 mm-es kerekekkel.
- A táj követi a napszakot: a valós idő és dátum alapján (napkelte, napnyugta) nappal, hajnalban, alkonyatkor vagy éjjel jelenik meg, éjjel világító ablakokkal, lámpákkal és fényszóróval. A főmenü képe is a napszakhoz igazodik.
- A felszíni vonatok nehezebbek: lassabban gyorsulnak és fékeznek, a sebességet az EVM felügyeli; a sárga lassújel-táblán a korlátozás tízes egységben áll.
- Tehervonattal a megállási jelnél a **Megállás**, majd a menetrend szerinti időben az **Indulás** gombbal lehet továbbmenni.

## Közzététel
1. Töltsd fel a mappa tartalmát egy GitHub repóba (a fájlok a gyökérben legyenek).
2. Vercel: New Project → a repó kiválasztása → Framework: Other → Deploy. (Vagy GitHub Pages.)
3. Nyisd meg a kapott https címet a telefonon.

## Telepítés
- **Android (Chrome):** menü ⋮ → *Alkalmazás telepítése* / *Hozzáadás a kezdőképernyőhöz*.
- **iPhone (Safari):** Megosztás → *Főképernyőhöz adás*.

Telepítés után teljes képernyőn, fekvő módban indul, és offline is működik.

## Android-alkalmazás (APK)
**Letöltés:** https://github.com/strattila90-hue/M3Metro/releases/download/apk/m3-metro.apk

Önálló alkalmazás: minden fájl az APK-ban van, első indításkor is internet nélkül fut. Letöltés után koppints a fájlra, engedélyezd az „ismeretlen forrásból” telepítést, és telepítsd. Az új verzió a régire rátelepíthető, az adatok megmaradnak (állandó aláíró kulcs: `android/app.keystore`, ne cseréld le).

- Építés: `.github/workflows/apk.yml` minden main ágra kerülő feltöltéskor (GitHub Actions), a kiadás címkéje mindig `apk`.
- Beállítások: `android/app.properties` (név, azonosító, tájolás, mely fájlok kerülnek bele – új fájlt/mappát ide is fel kell venni).
- `android/prep.py` másolja a fájlokat, a Google-betűtípusokat helyben csomagolja, és beilleszti a `native.js`-t, ami a WebView-ból hiányzó felolvasást (`speechSynthesis`), megosztást és kilépést pótolja.
- Az alkalmazásban nincs service worker: a fájlok eleve a telefonon vannak, frissítés = új APK.
- **Automatikus frissítés:** induláskor (és visszatéréskor legfeljebb 6 óránként) az alkalmazás megnézi a kiadás `apk.json` fájlját; ha újabb az APK, a háttérben letölti, és felajánlja a telepítést (egy koppintás, az adatok megmaradnak). Az első alkalommal az Android engedélyt kér a telepítéshez.
