package hu.jatekok.app;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Önálló Android-alkalmazás egy webes játékhoz / alkalmazáshoz.
 * Minden fájl az APK-ban van (assets/www), ezért első indításkor is azonnal, internet nélkül fut.
 * A tartalom a https://appassets.androidplatform.net/assets/www/ címről töltődik (WebViewAssetLoader),
 * így a localStorage, a fetch és a hang ugyanúgy működik, mint a weben.
 * A WebView-ból hiányzó böngészőfunkciókat (felolvasás, megosztás) az AppNative híd pótolja (native.js).
 */
public class MainActivity extends Activity {
    private static final String HOST = "appassets.androidplatform.net";
    private static final String START = "https://" + HOST + "/assets/www/index.html";
    private static final int FILE_REQ = 7, GEO_REQ = 8;

    private WebView web;
    private TextToSpeech tts;
    private volatile boolean ttsReady = false;
    private ValueCallback<Uri[]> fileCb;
    private GeolocationPermissions.Callback geoCb;
    private String geoOrigin;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUi();

        final WebViewAssetLoader.AssetsPathHandler assets = new WebViewAssetLoader.AssetsPathHandler(this);
        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .setDomain(HOST)
                .addPathHandler("/assets/", path -> {
                    // az alkalmazásban nincs service worker: minden fájl úgyis a telefonon van,
                    // így egy APK-frissítés után sem maradhat régi fájl a tárban
                    if (path.equals("www/sw.js"))
                        return new WebResourceResponse("text/plain", "utf-8", 404, "Not Found", null, new ByteArrayInputStream(new byte[0]));
                    return assets.handle(path);
                })
                .build();

        web = new WebView(this);
        web.setBackgroundColor(BuildConfig.BG_COLOR);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setGeolocationEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setTextZoom(100);
        s.setSupportZoom(false);
        s.setUserAgentString(s.getUserAgentString() + " JatekApp/" + BuildConfig.VERSION_NAME);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest req) {
                return loader.shouldInterceptRequest(req.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri u = req.getUrl();
                if (HOST.equals(u.getHost())) {
                    if (isPdf(u)) { openAssetFile(u); return true; }
                    return false;
                }
                openExternal(u.toString());
                return true;
            }
        });

        web.setDownloadListener((url, ua, cd, mime, len) -> {
            Uri u = Uri.parse(url);
            if (HOST.equals(u.getHost())) openAssetFile(u);
            else if (url.startsWith("http")) openExternal(url);
            else Toast.makeText(this, "Ez a letöltés az alkalmazásban nem érhető el.", Toast.LENGTH_LONG).show();
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> cb, FileChooserParams params) {
                if (fileCb != null) fileCb.onReceiveValue(null);
                fileCb = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                String type = "*/*";
                String[] acc = params.getAcceptTypes();
                if (acc != null && acc.length > 0 && acc[0] != null && acc[0].contains("/")) type = acc[0];
                i.setType(type);
                try {
                    startActivityForResult(Intent.createChooser(i, "Fájl kiválasztása"), FILE_REQ);
                } catch (ActivityNotFoundException e) {
                    fileCb = null;
                    return false;
                }
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback cb) {
                if (hasLocationPermission()) { cb.invoke(origin, true, false); return; }
                geoOrigin = origin;
                geoCb = cb;
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, GEO_REQ);
            }
        });

        web.addJavascriptInterface(new Bridge(), "AppNative");

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true;
                setTtsLang(BuildConfig.TTS_LANG);
            }
        });
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) { js("window.__ttsEv&&__ttsEv('start','" + id + "')"); }
            @Override public void onDone(String id) { js("window.__ttsEv&&__ttsEv('end','" + id + "')"); }
            @Override public void onError(String id) { js("window.__ttsEv&&__ttsEv('error','" + id + "')"); }
            @Override public void onStop(String id, boolean interrupted) { js("window.__ttsEv&&__ttsEv('end','" + id + "')"); }
        });

        web.loadUrl(START);
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] res) {
        super.onRequestPermissionsResult(req, perms, res);
        if (req == GEO_REQ && geoCb != null) {
            geoCb.invoke(geoOrigin, hasLocationPermission(), false);
            geoCb = null;
        }
    }

    private int setTtsLang(String tag) {
        if (tts == null) return TextToSpeech.LANG_NOT_SUPPORTED;
        Locale l = Locale.forLanguageTag(tag == null || tag.isEmpty() ? BuildConfig.TTS_LANG : tag.replace('_', '-'));
        return tts.setLanguage(l);
    }

    private void js(String code) {
        runOnUiThread(() -> { if (web != null) web.evaluateJavascript(code, null); });
    }

    private static boolean isPdf(Uri u) {
        String p = u.getPath();
        return p != null && p.toLowerCase(Locale.ROOT).endsWith(".pdf");
    }

    /** az APK-ba csomagolt fájl (pl. PDF) megnyitása a telefon saját alkalmazásával */
    private void openAssetFile(Uri u) {
        String p = u.getPath();
        if (p == null || !p.startsWith("/assets/")) return;
        final String asset = Uri.decode(p.substring("/assets/".length()));
        final String name = asset.substring(asset.lastIndexOf('/') + 1);
        new Thread(() -> {
            try {
                File dir = new File(getCacheDir(), "share");
                dir.mkdirs();
                File out = new File(dir, name);
                try (InputStream in = getAssets().open(asset); OutputStream os = new FileOutputStream(out)) {
                    byte[] b = new byte[65536]; int n;
                    while ((n = in.read(b)) > 0) os.write(b, 0, n);
                }
                Uri fu = FileProvider.getUriForFile(this, getPackageName() + ".files", out);
                String mime = name.toLowerCase(Locale.ROOT).endsWith(".pdf") ? "application/pdf" : "*/*";
                runOnUiThread(() -> {
                    Intent v = new Intent(Intent.ACTION_VIEW);
                    v.setDataAndType(fu, mime);
                    v.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try { startActivity(v); return; } catch (ActivityNotFoundException ignored) { }
                    Intent sh = new Intent(Intent.ACTION_SEND);
                    sh.setType(mime);
                    sh.putExtra(Intent.EXTRA_STREAM, fu);
                    sh.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    try { startActivity(Intent.createChooser(sh, name)); }
                    catch (ActivityNotFoundException e) { Toast.makeText(this, "Nincs alkalmazás a fájl megnyitásához.", Toast.LENGTH_LONG).show(); }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Nem sikerült megnyitni: " + name, Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void openExternal(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Nincs böngésző a link megnyitásához.", Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("deprecation")
    private void hideSystemUi() {
        if (!BuildConfig.IMMERSIVE) return;
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == FILE_REQ && fileCb != null) {
            fileCb.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(res, data));
            fileCb = null;
        }
    }

    /** vissza gomb: előbb az alkalmazáson belül lép vissza, a kezdőoldalon a háttérbe teszi az alkalmazást */
    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else moveTaskToBack(true);
    }

    @Override
    protected void onPause() {
        if (tts != null) tts.stop();
        if (web != null) web.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) web.onResume();
        hideSystemUi();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) tts.shutdown();
        if (web != null) web.destroy();
        super.onDestroy();
    }

    /** a JavaScriptből hívható függvények (a native.js használja) */
    private class Bridge {
        @JavascriptInterface
        public String version() { return BuildConfig.VERSION_NAME; }

        @JavascriptInterface
        public boolean hasTts() { return ttsReady; }

        /** van-e hang ehhez a nyelvhez (pl. "hu-HU") */
        @JavascriptInterface
        public boolean hasLang(String lang) {
            if (!ttsReady || tts == null) return false;
            int r = tts.isLanguageAvailable(Locale.forLanguageTag(lang.replace('_', '-')));
            return r >= TextToSpeech.LANG_AVAILABLE;
        }

        /** felolvasás sorba állítva; az események: __ttsEv('start'|'end'|'error', id) */
        @JavascriptInterface
        public boolean speak(String id, String text, String lang, float rate, float pitch, float volume) {
            if (!ttsReady || tts == null) return false;
            int r = setTtsLang(lang);
            if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) setTtsLang(BuildConfig.TTS_LANG);
            tts.setPitch(Math.max(0.3f, Math.min(2f, pitch)));
            tts.setSpeechRate(Math.max(0.3f, Math.min(2.5f, rate)));
            Bundle b = new Bundle();
            b.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, Math.max(0f, Math.min(1f, volume)));
            return tts.speak(text, TextToSpeech.QUEUE_ADD, b, id) == TextToSpeech.SUCCESS;
        }

        @JavascriptInterface
        public void stopSpeak() { if (tts != null) tts.stop(); }

        @JavascriptInterface
        public void share(String title, String text, String url) {
            runOnUiThread(() -> {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                String body = (text == null ? "" : text) + (url == null || url.isEmpty() ? "" : (text == null || text.isEmpty() ? "" : "\n") + url);
                i.putExtra(Intent.EXTRA_TEXT, body);
                if (title != null && !title.isEmpty()) i.putExtra(Intent.EXTRA_SUBJECT, title);
                try { startActivity(Intent.createChooser(i, "Megosztás")); } catch (ActivityNotFoundException ignored) { }
            });
        }

        @JavascriptInterface
        public void openUrl(String url) { runOnUiThread(() -> openExternal(url)); }

        @JavascriptInterface
        public void exit() { runOnUiThread(MainActivity.this::finishAndRemoveTask); }
    }
}
