(function () {

  var i18n = new VueI18n({
    locale: 'en', // set locale
    fallbackLocale: 'en',
    messages: {}
  });

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
      promises.push(fetchLangFile(url, lang));
    });
    return Promise.all(promises).then(() => i18n);
  }

  function fetchLangFile(url, lang) {
    if (url && url.indexOf('?') >= 0) {
      url = `${url}&v=${eXo.env.client.assetsVersion}`
    } else {
      url = `${url}?v=${eXo.env.client.assetsVersion}`
    }
    return fetch(url, { credentials: 'include' })
      .then(function (resp) {
        return resp.json();
      })
      .then(function (msgs) {
        i18n.mergeLocaleMessage(lang, msgs);
        i18n.locale = lang;
        return i18n;
      });
  }

  return { 'loadLanguageAsync': loadLanguageAsync };
})();