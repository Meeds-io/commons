(function($) {
  const atWhoCallback = $.fn.atwho["default"].callbacks;
  const providers = {};

  var $input, $editable, lastNoResultQuery;

  function loadFromProvider(term, response) {
    var p = [];
    var _this = this;

    var sourceProviders = this.settings && this.settings.sourceProviders;
    sourceProviders = sourceProviders || (this.options && this.options.sourceProviders);
    $.each(providers, function(name, provider) {
      if ($.inArray(name, sourceProviders) != -1) {
        if (!p[name]) {
          p[p.length] = provider;
        }
      }
    });

    var items = [];
    var count = 0;
    var finish = function(results) {
      if (results && results.length) {
        $.each(results, function(idx, elm) {
          items[items.length] = elm;
        });
      }

      if (++count == p.length) {
        response.call(this, items);
      }
    };

    //
    for (let provider of p) {
      if ($.isFunction(provider)) {
        provider.call(_this, term, function(results) {
          finish(results);
        });
      }
    }

  }

  var App = (function() {
    function App(input) {
      this.$input = input;
    }
    return App;
  })();

  var API = {
    getTags: function() {
      var at = this.settings.at;
      var tags = [];
      $('<div>' + this.$input.html() + '</div>').find('[a.metadata-tag]').each(function() {
        const value = $(this).text().trim().replace(at, '');
        tags.push(value);
      });
      return tags;
    },
  };

  var defaultAtConfig = {
    at: "#",
    suffix: '\u00A0',
    searchKey: 'name',
    acceptSpaceBar: true,
    minLen: 0,
    // TODO For test only
    source: [
      { name: 'test1' },
      { name: 'test2' }
    ],
    sourceProviders: ['exo:tag'],
    providers: {
      'exo:tag': function(query, callback) {
        if (lastNoResultQuery && query.length > lastNoResultQuery.length) {
          if (query.substr(0, lastNoResultQuery.length) === lastNoResultQuery) {
            callback.call(this, []);
            return;
          }
        }
        if (tagSearchCached[query]) {
          callback.call(this, tagSearchCached[query]);
        } else {
          const url = window.location.protocol + '//' + window.location.host + eXo.social.portal.context + '/' + eXo.social.portal.rest + '/v1/social/tags?q=' + query;
          $.getJSON(url, function(responseData) {
            var result = [];
            for (let d of responseData) {
              result.push({
                name: d.name,
                value: d.name,
              });
            }

            tagSearchCached[query] = result;
            if (tagSearchCached[query].length == 0) {
              lastNoResultQuery = query;
            } else {
              lastNoResultQuery = false;
            }
            callback.call(this, tagSearchCached[query]);
          });
        }
      }
    },
    callbacks: {
      matcher: function(flag, subtext, should_startWithSpace, acceptSpaceBar) {
        subtext = subtext.trim();
        var _a, _y, match, regexp, space;
        flag = flag.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
        if (should_startWithSpace) {
          flag = '(?:^|\\s)' + flag;
        }
        _a = decodeURI("%C3%80");
        _y = decodeURI("%C3%BF");
        space = acceptSpaceBar ? "\ " : "";
        regexp = new RegExp(flag + "([A-Za-z" + _a + "-" + _y + "0-9_" + space + "\'\.\+\-]*)$|" + flag + "([^\\x00-\\xff]*)$", 'gi');
        match = regexp.exec(subtext);
        if (match) {
          var a = match[2] || match[1];
          return a;
        } else {
          return null;
        }
      },
      highlighter: function(li, query) {
        li = $('<div></div>').append(li).html();
        return atWhoCallback.highlighter.call(this, li, query);
      }
    },
    functionOverrides: {
      // This method just copy from AtWho library to add some more customization
      insert: function(content, $li) {
        let element = this.query.el;
        element
          .removeClass('atwho-query')
          .html(content)
          .attr('contenteditable', "false");
        const parentElement = element.parent();
        if (parentElement.hasClass('metadata-tag')) {
          parentElement.html(element.html());
          element = parentElement;
        }
        if (range = this._getRange()) {
          if (element.length) {
            range.setEndAfter(element[0]);
          }
          range.collapse(false);
          if (element !== parentElement) {
            let suffix;
            suffix = (suffix = this.getOpt('suffix')) === "" ? suffix : suffix || "\u200D\u00A0";
            const suffixNode = this.app.document.createTextNode(suffix);
            range.insertNode(suffixNode);
            this._setRange('after', suffixNode, range);
          }
        }
        if (!this.$inputor.is(':focus')) {
          this.$inputor.focus();
        }
        return this.$inputor.change();
      },
    }
  };

  $.fn.tagSuggester = function(settings) {
    var _args = arguments;
    var $this = $(this);
    var app = $this.data("tagSuggester");
    const resetExistingEditor = app && (typeof settings === 'object');
    if (resetExistingEditor && settings.avoidReset) {
      // The editor is already initialized,
      // thus we ignore new initialization
      return;
    }
    if (!app) {
      $this.data('tagSuggester', (app = new App(this)));
    }
    var $input = $this, $editable;

    if (!(typeof settings === 'object' || !settings)) {
      if (API[settings]) {
        return API[settings].apply(app, Array.prototype.slice.call(_args, 1));
      } else {
        return $.error("Method " + method + " does not exist on eXo Tag suggester");
      }
    }

    if (!settings) settings = {};
    if (settings.providers) {
      $.each(settings.providers, function(name, provider) {
        providers[name] = provider;
      });
    }
    if (!settings.callbacks) {
      settings.callbacks = {};
    }

    $editable = $input;

    settings = $.extend(true, {}, defaultAtConfig, settings);
    if (settings.iframe) {
      $editable.atwho('setIframe', settings.iframe);
    }

    var source = settings.source;
    if (!(source && source.length) && settings.sourceProviders && settings.sourceProviders.length) {
      settings.source = function(query, callback) {
        loadFromProvider.call(app, query, callback);
      };
    } else if ($.isArray(settings.source)) {
      settings.data = source;
      settings.source = null;
    }
    settings.callbacks.remoteFilter = settings.source;
    settings.callbacks.tplEval = function(tpl, item, phase) {
      if (phase === "onDisplay") {
        return $(`<li class="option"><span>#${item.name}<span></li>`);
      } else if (phase == "onInsert") {
        return $(`<a class="metadata-tag">#${item.name}</a>`);
      } else {
        return atWhoCallback.tplEval(tpl, item);
      }
    };
    app.settings = settings;
    app.atWho = $editable.atwho(app.settings);
  };

  return $;
})($);
