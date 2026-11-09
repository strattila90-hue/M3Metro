# M3 Metró Szimulátor

2D metróvezető-játék (PWA) a 81-717-es szerelvénnyel. Telepíthető, internet nélkül is fut.

## Felszíni vasút
A metró mellett kilenc felszíni útvonal választható (Új menet → Személyvonat / Tehervonat), ebből öt hosszú:

| Útvonal | Megállók | Alap szerelvény |
|---|---|---|
| Balaton-felvidék | Balatonfüred → Révfülöp | Bzmot + vezérlőkocsi |
| Badacsony | Révfülöp → Tapolca | M41 + 3 Bhv |
| Dél-Balaton | Siófok → Fonyód | V43 + 4 Bhv |
| Százhalombatta (teher) | Budapest-Ferencváros → Százhalombatta | M44 + 7 teherkocsi |
| Győr (hosszú) | Budapest-Kelenföld → Győr, 13 megálló | V43 + 5 Bhv |
| Velencei-tó (hosszú) | Budapest-Déli → Székesfehérvár, 11 megálló | V43 + 4 Bhv |
| Balaton-felvidék teljes (hosszú) | Balatonfüred → Tapolca, 16 megálló | M41 + 4 Bhv |
| Déli part teljes (hosszú) | Siófok → Balatonszentgyörgy, 14 megálló | V43 + 5 Bhv |
| Dunaújváros (teher, hosszú) | Ferencváros → Dunaújváros | V43 + 10 teherkocsi |

A távolságok közelítők.

**Járművek oldal** (főmenü → Járművek): lapozható (nyilak, pöttyök vagy húzás), a képre koppintva kürtöl. Van Kürt, Motorhang (felpörgés füsttel) és Fényszóró gomb, valamint rövid leírás és műszaki adatok. Beállítható a kocsik száma (személykocsi 2–7, teherkocsi 3–14, Bzmot 1–3 egység). A listában azok az útvonalak jelennek meg, amelyeken az adott jármű járhat; a V43 csak villamosított vonalon, a metró csak a metróban. A nehezebb szerelvény lassabban gyorsul, a menetrend ehhez igazodik.

- A járműveket a játék rajzolja meg induláskor (a metrókocsi festett, kopott stílusában), nincs hozzájuk külön képfájl.
- Részletek: forgóvázak tengelyágy-csavarokkal, rugókkal, lengéscsillapítókkal és fékpofákkal; ütközők, csavarkapocs, fékcső; homlok- és zárjelző lámpák; kürtök; tetőszellőzők; zsalus gépterem-ajtók; a személykocsikon lebukó ablakok és útvonaltábla (pl. „Siófok – Fonyód”); teherkocsikon kézifék-kerék, veszélyességi bárca. Festés: Bzmot piros–sárga („piroska”), M41 gyári piros, V43 a 2000-es sorozat kék–sárga–szürke–fehér („Papagáj”) festésével, 15,7 m hosszú, 1180 mm-es kerekekkel.
- A táj követi a napszakot: a valós idő és dátum alapján (napkelte, napnyugta) nappal, hajnalban, alkonyatkor vagy éjjel jelenik meg, éjjel világító ablakokkal, lámpákkal és fényszóróval. A főmenü képe is a napszakhoz igazodik.
- A felszíni vonatok nehezebbek: lassabban gyorsulnak és fékeznek, a sebességet az EVM felügyeli; a sárga lassújel-táblán a korlátozás tízes egységben áll.
- Hangok a felszínen: dízelmotor gyújtási lüktetéssel és kipufogó-dübörgéssel (az M41-en jellegzetes csörgéssel, a Bzmoton négyfokozatú mechanikus sebességváltással), a V43-on transzformátorzúgás és fokozatkapcsoló-kattogás, légsűrítő álló helyzetben, légfék, pneumatikus harmonikaajtó, kalauzsíp, járművenként más kürt, háromhangú állomási dallam, szél, nappal madarak, meleg hónapokban éjjel tücskök, a teherkocsiknál erősebb csattogás.
- Vasúti átjárók: villogó fény, csengő, fokozatosan ereszkedő sorompó; az autók jönnek-mennek, a lezárt sorompó előtt sorba állnak. Ha az átjáró előtt kürtölsz (H), +10 pont.
- Átjáróbaleset (a Vészhelyzetek beállítás szerint): egy autó késve behajt és a sínen ragad. A figyelmeztetés a fékútnál korábban érkezik. Kürtszóra az autós megállhat (+20), vagy újraindíthatja a motort (+30). Ha megállsz előtte: +100. Ütközéskor −250 pont, villanás, szikrák, füst; hívd a 112-t, a mentők és a tűzoltók 5 s múlva érkeznek, utána a menet folytatható.
- Pálya és táj: vasúti töltés rézsűvel, földfoltokkal és árokkal; mögötte helyenként bevágás; zúzottkő-ágyazat, betonaljak, sínleerősítés; fű és gaz; hektométerkövek. Évszakhoz igazodó színek (tavasz, nyár, ősz, havas tél); szántóföldek, párhuzamos országút autókkal, falvak templommal és füstölő kéményekkel, jegenyesorok, legelő tehenek, nagyfeszültségű távvezeték, felhők, nádas a tóparton.
- Jelzők: előjelző (Ae), ötfényű bejárati jelző (A) létrával, kijárati (K) és térközjelzők; a lassújel előtt sárga háromszög figyelmeztet.
- Reakciók: kürtszóra a peronon várakozók hátrébb lépnek, a madarak felrebbennek (a vonat közeledtére is); személyvonaton indokolatlan vészfékezéskor az utasok panaszkodnak.
- Tehervonattal a megállási jelnél a **Megállás**, majd a menetrend szerinti időben az **Indulás** gombbal lehet továbbmenni.

## II. világháború a vasúton (Idővonal 1939–1945)
Főmenü → **Idővonal 1939–1945**: az idővonalon szabadon lehet ugrani az események között. Mindegyikhez van rövid történelmi összefoglaló forrásokkal, és leírás arról, mi szemléltető a játékban.

| Dátum | Helyszín | Mozdony és kocsik | Esemény a játékban |
|---|---|---|---|
| 1943. február 7. | Poljani → Slisszelburg („Győzelem útja”, Leningrád) | Эу 708-64 (0-10-0), szovjet kéttengelyes kocsik | a blokád áttörése után épült ideiglenes vonal frontközeli szakaszán tüzérségi becsapódások, hajnali sötét, elsötétített lámpák |
| 1944. április 3. | Ferencváros rendező → Kelenföld → Budaörs, a Déli összekötő vasúti hídon át | MÁV 424 „Bivaly” (2′D), MÁV G és O kocsik | légiriadó, amerikai bombázók hullámai, légvédelmi tűz, becsapódások a rendezőben |
| 1943. augusztus 3. | Szmalyavicsi → Zsodzina → Barisau (Belarusz, „Sínháború”) | DR 52 „Kriegslok” (1′E), G 10 és Om kocsik | partizánrobbantás előtted: meg kell állni a sínhiány előtt, különben kisiklás; a javítás Időugrással átugorható |

- Korabeli alakjelzők (szemaforok), a gőzmozdonyok hajtórúdjai a kerekekkel együtt mozognak, kerékfordulatonként négy gőzpuffanás füsttel, gőzsíp, éjjel a tűzszekrény fénye.
- **Időugrás** (Szünet menü vagy J billentyű): ugrás a következő eseményhez, illetve a sínjavítás végére; a menetidő hozzáadódik.
- Sérülés a kijelzőn; 100%-nál a menet véget ér.
- A deportálóvonatok nem részei a játéknak.

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
