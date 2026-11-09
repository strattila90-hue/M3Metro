#!/usr/bin/env python3
"""APK előkészítése: a webes fájlokat az android/app/src/main/assets/www mappába másolja.

- a beállítások: android/app.properties (files = mit kell bemásolni, icon = alkalmazásikon)
- az index.html elejére beilleszti a native.js-t (felolvasás, megosztás pótlása a WebView-ban)
- a Google Fonts betűtípusokat letölti és helyi fájlként csomagolja (így internet nélkül is ugyanaz a betű)
- kiírja a verziót (APP_VERSION) a GitHub Actions környezetébe

Futtatás a repó gyökeréből: python3 android/prep.py
"""
import os, re, shutil, sys, urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
AND = os.path.join(ROOT, 'android')
WWW = os.path.join(AND, 'app', 'src', 'main', 'assets', 'www')
RES = os.path.join(AND, 'app', 'src', 'main', 'res')


def props():
    p = {}
    with open(os.path.join(AND, 'app.properties'), encoding='utf-8') as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith('#') and '=' in line:
                k, v = line.split('=', 1)
                p[k.strip()] = v.strip()
    return p


def localize_fonts(html):
    """<link href="https://fonts.googleapis.com/css2?..."> -> helyi fonts/gf.css + woff2 fájlok"""
    links = re.findall(r'<link[^>]+href="(https://fonts\.googleapis\.com/css2?\?[^"]+)"[^>]*>', html)
    if not links:
        return html
    os.makedirs(os.path.join(WWW, 'gfonts'), exist_ok=True)
    ua = 'Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36'
    css_all, n = [], 0
    for href in links:
        url = href.replace('&amp;', '&')
        css = urllib.request.urlopen(urllib.request.Request(url, headers={'User-Agent': ua}), timeout=60).read().decode('utf-8')

        def grab(m):
            nonlocal n
            n += 1
            ext = os.path.splitext(m.group(1).split('?')[0])[1] or '.woff2'
            name = 'f%d%s' % (n, ext)
            data = urllib.request.urlopen(urllib.request.Request(m.group(1), headers={'User-Agent': ua}), timeout=60).read()
            with open(os.path.join(WWW, 'gfonts', name), 'wb') as f:
                f.write(data)
            return 'url(%s)' % name
        css_all.append(re.sub(r'url\((https://fonts\.gstatic\.com/[^)]+)\)', grab, css))
    with open(os.path.join(WWW, 'gfonts', 'fonts.css'), 'w', encoding='utf-8') as f:
        f.write('\n'.join(css_all))
    print('betűtípusok helyben: %d fájl' % n)
    html = re.sub(r'<link[^>]+rel="preconnect"[^>]+fonts\.(googleapis|gstatic)\.com[^>]*>\s*', '', html)
    first = [True]

    def repl(m):
        if first[0]:
            first[0] = False
            return '<link rel="stylesheet" href="gfonts/fonts.css">'
        return ''
    return re.sub(r'<link[^>]+href="https://fonts\.googleapis\.com/css2?\?[^"]+"[^>]*>', repl, html)


def main():
    p = props()
    shutil.rmtree(WWW, ignore_errors=True)
    os.makedirs(WWW)
    for item in p['files'].split():
        src = os.path.join(ROOT, item)
        dst = os.path.join(WWW, item)
        if os.path.isdir(src):
            shutil.copytree(src, dst)
        elif os.path.isfile(src):
            os.makedirs(os.path.dirname(dst), exist_ok=True)
            shutil.copy2(src, dst)
        else:
            sys.exit('HIBA: hiányzó fájl: ' + item)
    shutil.copy2(os.path.join(AND, 'native.js'), os.path.join(WWW, 'native.js'))

    idx = os.path.join(WWW, 'index.html')
    html = open(idx, encoding='utf-8').read()
    html = localize_fonts(html)
    tag = '<script src="native.js"></script>'
    html, k = re.subn(r'(<meta charset=[^>]*>)', lambda m: m.group(1) + '\n' + tag, html, count=1, flags=re.I)
    if not k:
        html, k = re.subn(r'(<head[^>]*>)', lambda m: m.group(1) + '\n' + tag, html, count=1, flags=re.I)
    if not k:
        sys.exit('HIBA: nincs <head> az index.html-ben')
    open(idx, 'w', encoding='utf-8').write(html)

    for d in ('mdpi', 'hdpi', 'xhdpi', 'xxhdpi', 'xxxhdpi'):
        os.makedirs(os.path.join(RES, 'mipmap-' + d), exist_ok=True)
        shutil.copy2(os.path.join(ROOT, p['icon']), os.path.join(RES, 'mipmap-' + d, 'ic_launcher.png'))

    ver = '1.0'
    vf, vre = p.get('versionFile'), p.get('versionRegex')
    if vf and vre:
        m = re.search(vre, open(os.path.join(ROOT, vf), encoding='utf-8').read())
        if m:
            ver = m.group(1)
    print('verzió:', ver)
    env = os.environ.get('GITHUB_ENV')
    if env:
        with open(env, 'a') as f:
            f.write('APP_VERSION=%s\nAPK_NAME=%s\nAPP_NAME=%s\n' % (ver, p['apkName'], p['appName']))


if __name__ == '__main__':
    main()
