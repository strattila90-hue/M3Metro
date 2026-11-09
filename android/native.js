/* Android-alkalmazás: a WebView-ból hiányzó böngészőfunkciók pótlása az AppNative hídon át.
   A workflow az APK építésekor az index.html elejére illeszti; böngészőben nem fut (nincs AppNative). */
(function () {
  var N = window.AppNative;
  if (!N) return;
  window.IS_APP = true;

  /* --- felolvasás: speechSynthesis a telefon saját beszédmotorjával --- */
  var LANGS = ['hu-HU', 'en-US', 'en-GB', 'sk-SK', 'de-DE'];
  var voices = null, queue = {}, seq = 0, speaking = 0, listeners = {};
  function getVoices() {
    if (voices && voices.length) return voices;
    voices = [];
    try {
      if (N.hasTts()) LANGS.forEach(function (l, i) {
        if (N.hasLang(l)) voices.push({ name: 'Android ' + l, lang: l, voiceURI: 'android-' + l, localService: true, default: i === 0 });
      });
    } catch (e) { }
    return voices;
  }
  function fire(u, type, extra) {
    var ev = { type: type, utterance: u, charIndex: 0, elapsedTime: 0, name: '' };
    if (extra) for (var k in extra) ev[k] = extra[k];
    try { if (typeof u['on' + type] === 'function') u['on' + type](ev); } catch (e) { setTimeout(function () { throw e; }); }
    (u._l && u._l[type] || []).forEach(function (f) { try { f(ev); } catch (e) { } });
  }
  window.__ttsEv = function (kind, id) {
    var u = queue[id];
    if (!u) return;
    if (kind === 'start') { fire(u, 'start'); return; }
    delete queue[id];
    speaking = Math.max(0, speaking - 1);
    if (kind === 'error') fire(u, 'error', { error: 'synthesis-failed' }); else fire(u, 'end');
  };
  function Utter(text) {
    this.text = text == null ? '' : String(text);
    this.lang = ''; this.voice = null; this.rate = 1; this.pitch = 1; this.volume = 1;
    this.onstart = this.onend = this.onerror = this.onboundary = this.onpause = this.onresume = this.onmark = null;
    this._l = {};
  }
  Utter.prototype.addEventListener = function (t, f) { (this._l[t] = this._l[t] || []).push(f); };
  Utter.prototype.removeEventListener = function (t, f) { var a = this._l[t] || []; var i = a.indexOf(f); if (i >= 0) a.splice(i, 1); };

  var synth = {
    onvoiceschanged: null,
    get speaking() { return speaking > 0; },
    get pending() { return speaking > 1; },
    paused: false,
    getVoices: getVoices,
    speak: function (u) {
      var id = 'u' + (++seq);
      var lang = (u.voice && u.voice.lang) || u.lang || document.documentElement.lang || 'hu-HU';
      queue[id] = u; speaking++;
      var ok = false;
      try { ok = N.speak(id, String(u.text), lang, +u.rate || 1, +u.pitch || 1, u.volume == null ? 1 : +u.volume); } catch (e) { }
      if (!ok) { delete queue[id]; speaking = Math.max(0, speaking - 1); setTimeout(function () { fire(u, 'error', { error: 'synthesis-unavailable' }); }, 0); }
    },
    cancel: function () {
      var old = queue; queue = {}; speaking = 0;
      try { N.stopSpeak(); } catch (e) { }
      Object.keys(old).forEach(function (k) { setTimeout(function () { fire(old[k], 'error', { error: 'interrupted' }); }, 0); });
    },
    pause: function () { }, resume: function () { },
    addEventListener: function (t, f) { (listeners[t] = listeners[t] || []).push(f); },
    removeEventListener: function (t, f) { var a = listeners[t] || []; var i = a.indexOf(f); if (i >= 0) a.splice(i, 1); }
  };
  try {
    Object.defineProperty(window, 'speechSynthesis', { value: synth, configurable: true, writable: true });
    Object.defineProperty(window, 'SpeechSynthesisUtterance', { value: Utter, configurable: true, writable: true });
  } catch (e) { window.speechSynthesis = synth; window.SpeechSynthesisUtterance = Utter; }
  // a beszédmotor pár száz ms alatt indul el: utána jelezzük, hogy megvannak a hangok
  var tries = 0;
  (function wait() {
    var ready = false; try { ready = N.hasTts(); } catch (e) { }
    if (ready) {
      voices = null; getVoices();
      var ev = { type: 'voiceschanged' };
      try { if (typeof synth.onvoiceschanged === 'function') synth.onvoiceschanged(ev); } catch (e) { }
      (listeners.voiceschanged || []).forEach(function (f) { try { f(ev); } catch (e) { } });
    } else if (tries++ < 40) setTimeout(wait, 250);
  })();

  /* --- megosztás a rendszer Megosztás ablakával --- */
  try {
    Object.defineProperty(navigator, 'share', {
      configurable: true,
      value: function (d) { d = d || {}; N.share(d.title || '', d.text || '', d.url || ''); return Promise.resolve(); }
    });
    Object.defineProperty(navigator, 'canShare', { configurable: true, value: function () { return true; } });
  } catch (e) { }

  /* --- külső linkek a böngészőben nyílnak --- */
  var open0 = window.open;
  window.open = function (url) {
    try {
      var u = new URL(url, location.href);
      if (u.host !== location.host) { N.openUrl(u.href); return null; }
      location.href = u.href; return window;
    } catch (e) { return open0 ? open0.apply(window, arguments) : null; }
  };

  /* --- Kilépés gomb: bezárja az alkalmazást --- */
  window.close = function () { try { N.exit(); } catch (e) { } };

  /* --- az alkalmazás eleve teljes képernyős --- */
  try {
    var fsOk = function () { return Promise.resolve(); };
    Element.prototype.requestFullscreen = fsOk;
    Element.prototype.webkitRequestFullscreen = fsOk;
  } catch (e) { }
})();
