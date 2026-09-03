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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.notification.model.MessageInfo;

import io.meeds.commons.digest.DigestCategoryRegistry;
import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestFrequency;
import io.meeds.commons.digest.model.DigestItem;
import io.meeds.commons.digest.model.DigestLine;
import io.meeds.commons.digest.model.DigestUserSettings;
import io.meeds.commons.digest.plugin.DigestCategoryProvider;
import io.meeds.commons.digest.plugin.DigestLineContext;
import io.meeds.commons.digest.plugin.DigestLineProvider;

/**
 * Builds one digest email from the waiting items of one occurrence: keeps the
 * categories the user selected for this frequency (the second half of the
 * double check), groups them in display order, asks each addon for its lines,
 * caps every category and counts the rest, then lays everything out with the
 * template. Zero lines means no email at all.
 */
@Component
public class DigestMailBuilder {

  private static final Logger           LOG               = LoggerFactory.getLogger(DigestMailBuilder.class);

  private static final String           MESSAGE_PLUGIN_ID = "digest";

  private final DigestCategoryRegistry  categoryRegistry;

  private final DigestLabelResolver     labelResolver;

  private final DigestRecipientResolver recipientResolver;

  private final DigestMailRenderer      renderer;

  private final int                     dailyCap;

  private final int                     weeklyCap;

  public DigestMailBuilder(DigestCategoryRegistry categoryRegistry,
                           DigestLabelResolver labelResolver,
                           DigestRecipientResolver recipientResolver,
                           DigestMailRenderer renderer,
                           @Value("${exo.notification.digest.daily.cap:5}") int dailyCap,
                           @Value("${exo.notification.digest.weekly.cap:10}") int weeklyCap) {
    this.categoryRegistry = categoryRegistry;
    this.labelResolver = labelResolver;
    this.recipientResolver = recipientResolver;
    this.renderer = renderer;
    this.dailyCap = Math.max(1, dailyCap);
    this.weeklyCap = Math.max(1, weeklyCap);
  }

  /**
   * @param user the recipient row of the work list
   * @param frequency the digest being sent
   * @param settings his current choices, the category list of the frequency is
   *          applied here
   * @param items the waiting items of the occurrence, most recent first
   * @param from the previous watermark, start of the covered period
   * @param until the claim moment, end of the covered period
   * @return the email ready for the queue, or null when nothing is left to say
   */
  public MessageInfo build(DigestUserEntity user,
                           DigestFrequency frequency,
                           DigestUserSettings settings,
                           List<DigestItemEntity> items,
                           Instant from,
                           Instant until) {
    String username = user.getUserId();
    String email = recipientResolver.getEmail(username);
    if (StringUtils.isBlank(email)) {
      LOG.warn("No email address for the digest recipient {}, his digest is skipped", username);
      return null;
    }
    Locale locale = recipientResolver.getLocale(username);
    ZoneId zone = DigestDueCalculator.zoneOf(user.getTimeZone());
    DigestLineContext lineContext = new DigestLineContext(username, locale, zone);
    List<String> selectedCategories = frequency == DigestFrequency.DAILY ? settings.getDailyCategories()
                                                                        : settings.getWeeklyCategories();
    int cap = frequency == DigestFrequency.DAILY ? dailyCap : weeklyCap;

    List<Map<String, Object>> sections = new ArrayList<>();
    int totalCount = 0;
    for (DigestCategoryProvider category : categoryRegistry.getCategoryProviders()) {
      if (selectedCategories == null || !selectedCategories.contains(category.getId())) {
        continue;
      }
      List<Map<String, String>> lines = new ArrayList<>();
      for (DigestItemEntity item : items) {
        if (category.getId().equals(item.getCategory())) {
          Map<String, String> line = buildLine(item, lineContext);
          if (line != null) {
            lines.add(line);
          }
        }
      }
      if (lines.isEmpty()) {
        continue;
      }
      totalCount += lines.size();
      int remaining = Math.max(0, lines.size() - cap);
      Map<String, Object> section = new HashMap<>();
      section.put("label", escape(labelResolver.categoryLabel(category, locale)));
      section.put("count", lines.size());
      section.put("lines", lines.subList(0, Math.min(cap, lines.size())));
      section.put("remaining", remaining);
      section.put("moreLabel", remaining == 0 ? "" : format(commons("Notification.digest.more", locale), remaining));
      sections.add(section);
    }
    if (sections.isEmpty()) {
      return null;
    }

    String platformName = recipientResolver.getPlatformName();
    String platformUrl = recipientResolver.getPlatformUrl();
    String platformLink = "<a href=\"" + escape(platformUrl) + "\">" + escape(platformName) + "</a>";
    String firstName = recipientResolver.getFirstName(username);

    Map<String, Object> binding = new HashMap<>();
    binding.put("greeting", format(commons("Notification.digest.greeting", locale), escape(StringUtils.defaultString(firstName, username))));
    binding.put("intro", intro(frequency, locale, zone, from, until, platformLink));
    binding.put("sections", sections);
    binding.put("moreUrl", escape(platformUrl));
    binding.put("viewAllLabel", format(commons("Notification.digest.viewAll", locale), escape(platformName)));
    binding.put("viewAllUrl", escape(platformUrl));
    binding.put("manageLabel", commons("Notification.digest.manage", locale));
    binding.put("manageUrl", escape(recipientResolver.getSettingsUrl(username)));
    binding.put("platformName", escape(platformName));

    String subjectKey = frequency == DigestFrequency.DAILY ? "Notification.digest.subject.daily"
                                                           : "Notification.digest.subject.weekly";
    String subject = format(commons(subjectKey, locale), platformName, totalCount);
    return new MessageInfo().pluginId(MESSAGE_PLUGIN_ID)
                            .from(recipientResolver.getSender())
                            .to(email)
                            .subject(subject)
                            .body(renderer.render(binding))
                            .end();
  }

  /**
   * One item becomes one line through the addon owning its notification type. A
   * missing provider, a vanished object or a failing addon all mean the same
   * thing for the email: no line, and nothing counted.
   */
  private Map<String, String> buildLine(DigestItemEntity item, DigestLineContext context) {
    DigestLineProvider provider = categoryRegistry.getLineProvider(item.getPluginId());
    if (provider == null) {
      LOG.debug("No digest line provider for {}, the item {} is left out", item.getPluginId(), item.getId());
      return null;
    }
    try {
      DigestLine line = provider.buildLine(toModel(item), context);
      if (line == null || StringUtils.isBlank(line.getLabelKey())) {
        return null;
      }
      String wording = labelResolver.resolve(line.getLabelKey(), item.getPluginId(), context.getLocale());
      Object[] args = line.getArgs() == null ? new Object[0]
                                             : line.getArgs().stream().map(DigestMailBuilder::escape).toArray();
      Map<String, String> rendered = new HashMap<>();
      rendered.put("text", format(wording, args));
      rendered.put("url", StringUtils.isBlank(line.getUrl()) ? "" : escape(line.getUrl()));
      return rendered;
    } catch (Exception e) {
      LOG.warn("The digest line of the item {} ({}) can't be built, it is left out", item.getId(), item.getPluginId(), e);
      return null;
    }
  }

  private String intro(DigestFrequency frequency, Locale locale, ZoneId zone, Instant from, Instant until, String platformLink) {
    if (frequency == DigestFrequency.DAILY) {
      return format(commons("Notification.digest.intro.daily", locale), platformLink);
    }
    DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
    return format(commons("Notification.digest.intro.weekly", locale),
                  platformLink,
                  dateFormat.format(from.atZone(zone).toLocalDate()),
                  dateFormat.format(until.atZone(zone).toLocalDate()));
  }

  private String commons(String key, Locale locale) {
    return labelResolver.commons(key, locale);
  }

  /**
   * Fills the placeholders {0}, {1}... by plain replacement, the convention of
   * every notification bundle of the platform: translators keep their
   * apostrophes single and never learn a quoting rule.
   */
  static String format(String pattern, Object... args) {
    String result = pattern;
    for (int i = 0; i < args.length; i++) {
      result = StringUtils.replace(result, "{" + i + "}", String.valueOf(args[i]));
    }
    return result;
  }

  private static String escape(String value) {
    return value == null ? "" : StringEscapeUtils.escapeHtml4(value);
  }

  static DigestItem toModel(DigestItemEntity entity) {
    return new DigestItem(entity.getId() == null ? 0 : entity.getId(),
                          entity.getUserId(),
                          entity.getPluginId(),
                          entity.getCategory(),
                          entity.getItemDate(),
                          DigestParamsCodec.parse(entity.getParams()));
  }

}
