/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
package io.meeds.commons.digest.service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.notification.template.TemplateUtils;

import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import lombok.SneakyThrows;

/**
 * Renders the digest email template. The layout lives in the template, the
 * words come already translated and escaped in the binding: the template never
 * looks anything up. A template that can't be loaded is an error, never an
 * empty email: the occurrence is then given back and retried.
 */
@Component
public class DigestMailRenderer {

  public static final String     TEMPLATE_PATH = "war:/notification/templates/mail/DigestMail.gtmpl";

  private final Supplier<String> templateLoader;

  private Template               template;

  public DigestMailRenderer() {
    this(() -> TemplateUtils.loadGroovyTemplate(TEMPLATE_PATH));
  }

  DigestMailRenderer(Supplier<String> templateLoader) {
    this.templateLoader = templateLoader;
  }

  @SneakyThrows
  public String render(Map<String, Object> binding) {
    StringWriter writer = new StringWriter();
    getTemplate().make(new HashMap<>(binding)).writeTo(writer);
    return writer.toString();
  }

  @SneakyThrows
  private synchronized Template getTemplate() {
    if (template == null) {
      String text = templateLoader.get();
      if (StringUtils.isBlank(text)) {
        // Not cached: the next occurrence tries again, a fixed deployment heals
        // without a restart
        throw new IllegalStateException("The digest email template " + TEMPLATE_PATH + " can't be loaded");
      }
      template = new GStringTemplateEngine().createTemplate(text);
    }
    return template;
  }

}
