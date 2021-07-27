(function () {

  var i18n = new VueI18n({
    locale: 'en', // set locale
    fallbackLocale: 'en',
    messages: {}
  });

  window.eXoI18N = window.eXoI18N || {};
  window.eXoI18N.executingFetches = window.eXoI18N.executingFetches || {};
  window.eXoI18N.i18NFetchedURLs = window.eXoI18N.i18NFetchedURLs || [];
  window.eXoI18N.i18NPreloadedMessages = window.eXoI18N.i18NPreloadedMessages || {};

  /**
   * Load translated strings from the given URLs and for the given language
   * @param {string} lang - Language to load strings for
   * @param {(string|string[])} urls - Single URL or array of URLs to load i18n files from
   * @returns {Promise} Promise giving the i18n object with loaded translated strings
   */
  function loadLanguageAsync(lang, urls) {
    if (!lang) {
      lang = i18n.locale;
    }

    if(typeof urls === 'string') {
      urls = [ urls ];
    }

    let promises = [];
    urls.forEach(url => {
      const promise = fetchLangFile(url, lang);
      if (promise) {
        promises.push(promise);
      }
    });
    return Promise.all(promises).then(() => i18n);
  }

  function fetchLangFile(url, lang) {
    if (url && url.indexOf('?') >= 0) {
      url = `${url}&v=${eXo.env.client.assetsVersion}`
    } else {
      url = `${url}?v=${eXo.env.client.assetsVersion}`
    }

    const msgs = window.eXoI18N.i18NPreloadedMessages[url];
    if (msgs) {
      delete window.eXoI18N.i18NPreloadedMessages[url];
      i18n.mergeLocaleMessage(lang, msgs);
      return Promise.resolve(i18n);
    } else if (window.eXoI18N.i18NFetchedURLs.indexOf(url) >= 0) {
      return window.eXoI18N.executingFetches[url];
    } else {
      const i18NFetch = fetch(url)
        .then(resp => resp && resp.ok && resp.json())
        .then(msgs => {
          if (msgs) {
            i18n.mergeLocaleMessage(lang, msgs);
            i18n.locale = lang;
          }
          return i18n;
        })
        .finally(() => delete window.eXoI18N.executingFetches[url]);
      window.eXoI18N.executingFetches[url] = i18NFetch;
      window.eXoI18N.i18NFetchedURLs.push(url);
      return i18NFetch;
    }
  }

  return { 'loadLanguageAsync': loadLanguageAsync };
})();