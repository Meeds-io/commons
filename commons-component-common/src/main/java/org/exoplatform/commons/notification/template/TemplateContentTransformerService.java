/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.commons.notification.template;

import static org.exoplatform.commons.notification.template.TemplateUtils.getExcerptSubject;
import static org.exoplatform.commons.notification.template.TemplateUtils.getPluginConfig;
import static org.exoplatform.commons.notification.template.TemplateUtils.loadGroovyElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.api.notification.template.Element;
import org.exoplatform.commons.api.notification.template.ElementVisitor;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

public class TemplateContentTransformerService {

  private static final String       DIGEST_TEMPLATE_KEY = "Digest.%s.%s";

  private static final String       SIMPLE_TEMPLATE_KEY = "Simple.%s.%s";

  private ExoCache<String, Element> cacheTemplate;

  public TemplateContentTransformerService(CacheService cacheService) {
    this.cacheTemplate = cacheService.getCacheInstance("commons.subject");
  }

  /**
   * Transform a {@link TemplateContext} with groovy template to a Text (used in
   * Mail Notif by example)
   * 
   * @param ctx {@link TemplateContext}
   * @return String result after executing Groovy Template content
   */
  public String processGroovy(TemplateContext ctx) {
    Element groovyElement = loadGroovyElement(ctx.getPluginId(), ctx.getLanguage());
    ElementVisitor visitor = new GroovyElementVisitor();
    return visitor.with(ctx).visit(groovyElement).out();
  }

  /**
   * Transform a {@link TemplateContext} to a Subject Text (used in Mail by
   * example)
   * 
   * @param ctx {@link TemplateContext}
   * @return Subject
   */
  public String processSubject(TemplateContext ctx) {
    Element subjectElement = getSubjectElement(ctx);
    ctx.forEach((k, v) -> {
      if (v instanceof String s) {
        ctx.put(k, transform(s, ctx));
      }
    });
    String subject = getSubject(ctx);
    if (StringUtils.isNotBlank(subject)) {
      ctx.put("SUBJECT", getExcerptSubject(subject));
      return subjectElement.accept(SimpleElementVistior.instance().with(ctx)).out();
    } else {
      subject = subjectElement.accept(SimpleElementVistior.instance().with(ctx)).out();
      return getExcerptSubject(subject);
    }
  }

  /**
   * Render for digest template
   * 
   * @param ctx {@link TemplateContext} containing all Template Variables to use
   *          for processing Template
   * @return digest HTML content
   */
  public String processDigest(TemplateContext ctx) {
    DigestTemplate digest = getDigestTemplate(ctx);
    return digest.accept(SimpleElementVistior.instance().with(ctx)).out();
  }

  protected String getSubject(TemplateContext ctx) {
    return transform((String) ctx.get("SUBJECT"), ctx);
  }

  protected String transform(String value, TemplateContext ctx) { // NOSONAR
    return value == null ? null : StringEscapeUtils.unescapeHtml4(value);
  }

  private DigestTemplate getDigestTemplate(TemplateContext ctx) {
    String key = getCacheKey(DIGEST_TEMPLATE_KEY, ctx.getPluginId(), ctx.getLanguage());
    DigestTemplate digest = (DigestTemplate) cacheTemplate.get(key);
    if (digest == null) {
      PluginConfig templateConfig = getPluginConfig(ctx.getPluginId());
      digest = NotificationUtils.getDigest(templateConfig, ctx.getPluginId(), ctx.getLanguage());
      cacheTemplate.put(key, digest);
    }
    return digest;
  }

  private Element getSubjectElement(TemplateContext ctx) {
    String key = getCacheKey(SIMPLE_TEMPLATE_KEY, ctx.getPluginId(), ctx.getLanguage());
    Element subjectElement = cacheTemplate.get(key);
    if (subjectElement == null) {
      PluginConfig templateConfig = getPluginConfig(ctx.getPluginId());
      subjectElement = NotificationUtils.getSubject(templateConfig, ctx.getPluginId(), ctx.getLanguage()).addNewLine(false);
      cacheTemplate.put(key, subjectElement);
    }
    return subjectElement;
  }

  protected String getCacheKey(String pattern, String pluginId, String language) {
    return String.format(pattern, pluginId, language);
  }

}
