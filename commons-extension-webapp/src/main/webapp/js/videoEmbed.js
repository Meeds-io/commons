/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/*
 * Maps a YouTube or Vimeo page URL to the provider's own player.
 *
 * The oEmbed provider of the editors wraps every embed, videos included, in an
 * iframe of its own CDN that asks for a consent the page cannot give, so the
 * video never plays. The provider's player is embedded instead. Its hosts are
 * in the default iframe allowlist of the HTML sanitizers
 * (io.meeds.sanitizer.iframe.allowedHosts). The video id is matched against
 * the provider's id alphabet only, so nothing else from the URL reaches the
 * iframe src.
 */
(function() {
  const YOUTUBE_HOSTS = ['youtube.com', 'www.youtube.com', 'm.youtube.com', 'music.youtube.com', 'youtube-nocookie.com', 'www.youtube-nocookie.com'];
  const YOUTUBE_ID = /^[A-Za-z0-9_-]{11}$/;
  const VIMEO_HOSTS = ['vimeo.com', 'www.vimeo.com', 'player.vimeo.com'];
  const VIMEO_ID = /^\d{1,12}$/;
  const VIMEO_HASH = /^[0-9a-f]{6,20}$/;
  const GATED_IFRAME_HOST = 'if-cdn.com';

  function parseUrl(url) {
    if (!url || typeof url !== 'string') {
      return null;
    }
    // The embed dialog accepts a URL starting with '//' or 'www.'
    let value = url.trim();
    if (value.startsWith('//')) {
      value = `https:${value}`;
    } else if (/^www\./i.test(value)) {
      value = `https://${value}`;
    }
    try {
      const parsedUrl = new URL(value);
      return ['http:', 'https:'].includes(parsedUrl.protocol) ? parsedUrl : null;
    } catch (e) {
      return null;
    }
  }

  function pathSegments(parsedUrl) {
    return parsedUrl.pathname.split('/').filter(segment => segment);
  }

  function youtubePlayerUrl(parsedUrl) {
    const host = parsedUrl.hostname.toLowerCase();
    const segments = pathSegments(parsedUrl);
    let videoId = null;
    if (host === 'youtu.be') {
      videoId = segments[0];
    } else if (YOUTUBE_HOSTS.includes(host)) {
      if (segments[0] === 'watch') {
        videoId = parsedUrl.searchParams.get('v');
      } else if (['embed', 'shorts', 'live', 'v'].includes(segments[0])) {
        videoId = segments[1];
      }
    }
    // /embed/videoseries?list=… is a playlist, left to the oEmbed provider
    if (!videoId || videoId === 'videoseries' || !YOUTUBE_ID.test(videoId)) {
      return null;
    }
    // A start time in seconds, as youtu.be/<id>?t=42 or &start=42
    const start = parsedUrl.searchParams.get('start') || parsedUrl.searchParams.get('t');
    return /^\d{1,6}s?$/.test(start || '')
      ? `https://www.youtube-nocookie.com/embed/${videoId}?start=${parseInt(start)}`
      : `https://www.youtube-nocookie.com/embed/${videoId}`;
  }

  // Only the URL shapes of a single video: a showcase, an album, an event or
  // a channel page is left to the oEmbed provider
  function vimeoVideo(host, segments) {
    if (host === 'player.vimeo.com') {
      return segments[0] === 'video' && {id: segments[1]};
    } else if (segments.length <= 2 && VIMEO_ID.test(segments[0])) {
      return {id: segments[0], hash: segments[1]};
    } else if (segments[0] === 'channels' && segments.length === 3) {
      return {id: segments[2]};
    } else if (['showcase', 'album'].includes(segments[0]) && segments[2] === 'video' && segments.length === 4) {
      return {id: segments[3]};
    } else if (segments[0] === 'groups' && segments[2] === 'videos' && segments.length === 4) {
      return {id: segments[3]};
    }
    return null;
  }

  function vimeoPlayerUrl(parsedUrl) {
    const host = parsedUrl.hostname.toLowerCase();
    if (!VIMEO_HOSTS.includes(host)) {
      return null;
    }
    const video = vimeoVideo(host, pathSegments(parsedUrl));
    if (!VIMEO_ID.test(video?.id || '')) {
      return null;
    }
    // An unlisted video is reached through the hash following its id
    const hash = parsedUrl.searchParams.get('h') || video.hash;
    return VIMEO_HASH.test(hash || '') ? `https://player.vimeo.com/video/${video.id}?h=${hash}` : `https://player.vimeo.com/video/${video.id}`;
  }

  // Same rule as the sanitizers: a deployment that replaced the allowed iframe
  // hosts (io.meeds.sanitizer.iframe.allowedHosts) without the player's keeps
  // the oEmbed provider's iframe
  function isAllowedHost(playerUrl) {
    const allowedHosts = window.eXo?.env?.portal?.iframeAllowedHosts;
    const host = new URL(playerUrl).hostname;
    return Array.isArray(allowedHosts)
      && allowedHosts.some(allowedHost => (allowedHost.startsWith('*.')
        ? host.endsWith(allowedHost.substring(1))
        : host === allowedHost));
  }

  /**
   * @param {String} url a video page URL, as typed or pasted by a user
   * @returns {String} the provider's player URL, or null when the URL is not a
   *          single video of a supported provider, or when the player's host
   *          is not an allowed iframe host
   */
  function getPlayerUrl(url) {
    const parsedUrl = parseUrl(url);
    const playerUrl = parsedUrl && (youtubePlayerUrl(parsedUrl) || vimeoPlayerUrl(parsedUrl));
    return playerUrl && isAllowedHost(playerUrl) ? playerUrl : null;
  }

  /**
   * @param {String} url a video page URL
   * @returns {String} a responsive 16:9 player iframe, in the shape the oEmbed
   *          provider returns, or null when the URL is not a supported video
   */
  function getPlayerHtml(url) {
    const playerUrl = getPlayerUrl(url);
    if (!playerUrl) {
      return null;
    }
    return '<div><div style="left: 0; width: 100%; height: 0; position: relative; padding-bottom: 56.25%;">'
      + `<iframe src="${playerUrl.replace(/&/g, '&amp;')}" style="top: 0; left: 0; width: 100%; height: 100%; position: absolute; border: 0;"`
      + ' allow="autoplay; encrypted-media; fullscreen; picture-in-picture" allowfullscreen></iframe>'
      + '</div></div>';
  }

  /**
   * Replaces, in an oEmbed response handled by a CKEditor embed widget, the
   * provider's markup of a supported video by its player.
   *
   * @param {Object} data the data of the widget 'handleResponse' event: url,
   *          html and the oEmbed response
   */
  function handleEmbedResponse(data) {
    const playerHtml = data?.response && (getPlayerHtml(data.url) || getPlayerHtml(data.response.url));
    if (playerHtml) {
      data.response.type = 'video';
      data.response.html = playerHtml;
      data.html = playerHtml;
    }
  }

  /**
   * Points a saved embed's iframe at the provider's player. A note or an
   * article keeps the URL it embeds on the wrapper of its iframe
   * (div.embed-wrapper[data-url]), and the iframe it saved is the oEmbed
   * provider's own. Only the provider's consent-gated iframe (if-cdn.com) is
   * replaced. Meant for an HTML sanitizer hook, before its iframe host check.
   *
   * @param {Element} iframe an iframe being sanitized
   */
  function usePlayerInSavedEmbed(iframe) {
    if (parseUrl(iframe?.getAttribute?.('src'))?.hostname.toLowerCase() !== GATED_IFRAME_HOST) {
      return;
    }
    const playerUrl = getPlayerUrl(iframe.parentNode?.getAttribute?.('data-url'));
    if (playerUrl) {
      iframe.setAttribute('src', playerUrl);
      iframe.setAttribute('allow', 'autoplay; encrypted-media; fullscreen; picture-in-picture');
      iframe.setAttribute('allowfullscreen', '');
    }
  }

  const videoEmbed = {
    getPlayerUrl,
    getPlayerHtml,
    handleEmbedResponse,
    usePlayerInSavedEmbed,
  };
  // Read as a global by the CKEditor plugins, which are not AMD modules
  window.eXo = window.eXo || {};
  window.eXo.videoEmbed = videoEmbed;
  return videoEmbed;
})()
