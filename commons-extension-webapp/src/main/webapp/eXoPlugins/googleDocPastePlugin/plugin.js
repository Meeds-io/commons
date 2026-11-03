(function () {
  'use strict';

  // Same threshold as the bold content form of the basicstyles plugin
  const BOLD_MIN_WEIGHT = 700;

  // Inline styles Google Docs uses for bold, italic and underline. Each rule
  // returns the element the value becomes (null: none), and whether the style
  // is removed from the span; a value no rule converts stays as inline style.
  const FORMATTING_STYLES = [
    {
      name: 'font-weight',
      convert: value => {
        const bold = value === 'bold' || parseInt(value, 10) >= BOLD_MIN_WEIGHT;
        return {
          elementName: bold ? 'strong' : null,
          removeStyle: bold || value === 'normal' || value === '400',
        };
      },
    },
    {
      name: 'font-style',
      convert: value => ({
        elementName: value === 'italic' ? 'em' : null,
        removeStyle: value === 'italic' || value === 'normal',
      }),
    },
    {
      name: 'text-decoration',
      convert: value => ({
        elementName: value === 'underline' ? 'u' : null,
        removeStyle: value === 'underline' || value === 'none',
      }),
    },
  ];

  CKEDITOR.plugins.add('googleDocPastePlugin', {
    requires: 'pastefromgdocs',
    init: function (editor) {
      editor.pasteTools.register({
        // Runs after the pastefromgdocs handler (default priority 10), on the HTML it produced
        priority: 20,
        canHandle: function (evt) {
          return /id=("|')?docs-internal-guid-/.test(evt.data.dataValue);
        },
        handle: function (evt, next) {
          evt.data.dataValue = convertFormattingStyles(evt.data.dataValue);
          next();
        },
      });
    },
  });

  // Google Docs marks bold, italic and underline with inline styles only.
  // With allowedContent: true the content filter is disabled, so the
  // basicstyles content forms never map these styles to the elements the
  // Bold, Italic and Underline buttons toggle: converting them here lets the
  // buttons show and remove the formatting of pasted text.
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
        if (conversion.elementName) {
          elementNames.push(conversion.elementName);
        }
        if (conversion.removeStyle) {
          delete styles[style.name];
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
