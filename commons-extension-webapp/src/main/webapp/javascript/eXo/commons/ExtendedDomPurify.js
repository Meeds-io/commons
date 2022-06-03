/*
 * Copyright (C) 2022 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

(function() {
    let ExtendedDomPurify = function () {
    };
    ExtendedDomPurify.prototype.purify = function (content) {
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
    return new ExtendedDomPurify();
})()