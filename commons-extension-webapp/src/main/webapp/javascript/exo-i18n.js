/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
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