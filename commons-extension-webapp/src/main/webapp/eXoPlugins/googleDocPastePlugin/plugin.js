(function () {
  'use strict';

  // Same threshold as the bold content form of the basicstyles plugin
  const BOLD_MIN_WEIGHT = 700;

  // Elements of the basicstyles Underline and Strike buttons, by text-decoration keyword
  const TEXT_DECORATION_ELEMENTS = {
    underline: 'u',
    'line-through': 's',
  };

  // Inline styles Google Docs uses for bold, italic, underline and strikethrough.
  // Each rule returns the elements the value becomes, and the value left on the
  // span (null: the style is removed); a value no rule converts stays as inline style.
  const FORMATTING_STYLES = [
    {
      name: 'font-weight',
      convert: value => {
        const bold = value === 'bold' || Number.parseInt(value, 10) >= BOLD_MIN_WEIGHT;
        return {
          elementNames: bold ? ['strong'] : [],
          remainingValue: bold || value === 'normal' || value === '400' ? null : value,
        };
      },
    },
    {
      name: 'font-style',
      convert: value => ({
        elementNames: value === 'italic' ? ['em'] : [],
        remainingValue: value === 'italic' || value === 'normal' ? null : value,
      }),
    },
    {
      // A multi-value text-decoration (underline line-through) is split
      name: 'text-decoration',
      convert: value => {
        const keywords = value.split(/\s+/).filter(keyword => keyword !== 'none');
        const remainingKeywords = keywords.filter(keyword => !TEXT_DECORATION_ELEMENTS[keyword]);
        return {
          elementNames: keywords.filter(keyword => TEXT_DECORATION_ELEMENTS[keyword]).map(keyword => TEXT_DECORATION_ELEMENTS[keyword]),
          remainingValue: remainingKeywords.length ? remainingKeywords.join(' ') : null,
        };
      },
    },
  ];

  CKEDITOR.plugins.add('googleDocPastePlugin', {
    requires: 'pastefromgdocs',
    init: function (editor) {
      editor.pasteTools.register({
        // Runs after the pastefromgdocs handler (default priority 10), on the HTML it produced
        priority: 20,
        canHandle: function (evt) {
          return /id=["']?docs-internal-guid-/.test(evt.data.dataValue);
        },
        handle: function (evt, next) {
          evt.data.dataValue = convertFormattingStyles(evt.data.dataValue);
          next();
        },
      });
    },
  });

  // Google Docs marks bold, italic, underline and strikethrough with inline
  // styles only. With allowedContent: true the content filter is disabled, so
  // the basicstyles content forms never map these styles to the elements the
  // Bold, Italic, Underline and Strike buttons toggle: converting them here
  // lets the buttons show and remove the formatting of pasted text.
  function convertFormattingStyles(html) {
    const filter = new CKEDITOR.htmlParser.filter({
      elements: {
        span: convertSpanFormatting,
      },
    });
    const fragment = CKEDITOR.htmlParser.fragment.fromHtml(html);
    filter.applyTo(fragment);
    const writer = new CKEDITOR.htmlParser.basicWriter();
    fragment.writeHtml(writer);
    return writer.getHtml();
  }

  function convertSpanFormatting(span) {
    const styles = CKEDITOR.tools.parseCssText(span.attributes.style || '', true);
    const elementNames = [];
    FORMATTING_STYLES.forEach(style => {
      if (style.name in styles) {
        const conversion = style.convert(styles[style.name].trim().toLowerCase());
        elementNames.push(...conversion.elementNames);
        if (conversion.remainingValue === null) {
          delete styles[style.name];
        } else {
          styles[style.name] = conversion.remainingValue;
        }
      }
    });
    if (CKEDITOR.tools.isEmpty(styles)) {
      delete span.attributes.style;
    } else {
      span.attributes.style = CKEDITOR.tools.writeCssText(styles);
    }

    let container = span;
    elementNames.forEach(elementName => {
      const formattingElement = new CKEDITOR.htmlParser.element(elementName);
      container.children.slice().forEach(child => {
        child.remove();
        formattingElement.add(child);
      });
      container.add(formattingElement);
      container = formattingElement;
    });

    if (CKEDITOR.tools.isEmpty(span.attributes)) {
      // An attribute-less span carries nothing: keep its children only
      for (let i = span.children.length - 1; i >= 0; i--) {
        span.children[i].insertAfter(span);
      }
      return false;
    }
    return span;
  }
})();
