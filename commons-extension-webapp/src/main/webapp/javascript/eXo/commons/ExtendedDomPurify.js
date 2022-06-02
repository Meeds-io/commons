(function() {
    let extendDomPurify = function () {
    };
    extendDomPurify.prototype.purify = function (content) {
        const pureHtml = DOMPurify.sanitize(Autolinker.link(content, {newWindow: true}),
            {
                USE_PROFILES: {html: true},
                ADD_TAGS: ["iframe"],
                ADD_ATTR: ['target', 'allow', 'allowfullscreen', 'frameborder', 'scrolling']
            });
        DOMPurify.addHook('afterSanitizeAttributes', function (node) {
            // add noopener attribute to external links to eliminate vulnerabilities
            if ('target' in node) {
                node.setAttribute('rel', 'noopener');
            }
        });
        DOMPurify.addHook('uponSanitizeElement', function (node) {
            if (node.tagName === 'iframe') {
                const src = node.getAttribute('src') || '';
                if (!src.startsWith('https://www.youtube.com/embed/')
                    || !src.startsWith('https://player.vimeo.com/video/') || !src.startsWith('https://www.dailymotion.com/embed/video/')) {
                    return node.parentNode?.removeChild(node);
                }
            }
        });
        return pureHtml;
    }
    return new extendDomPurify();
})()
