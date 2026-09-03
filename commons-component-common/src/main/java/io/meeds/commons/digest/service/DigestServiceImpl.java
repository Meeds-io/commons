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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import org.exoplatform.commons.api.notification.model.NotificationInfo;

import io.meeds.commons.digest.DigestCategoryRegistry;
import io.meeds.commons.digest.DigestService;
import io.meeds.commons.digest.dao.DigestItemDAO;
import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.model.DigestUserSettings;
import io.meeds.commons.digest.plugin.DigestCategoryProvider;

@Service
public class DigestServiceImpl implements DigestService {

  private final DigestSettingStorage         settingStorage;

  private final DigestEnrollmentStorage      enrollmentStorage;

  private final DigestCategoryRegistry      categoryRegistry;

  private final DigestItemDAO                digestItemDAO;

  private final DigestSender                 digestSender;

  public DigestServiceImpl(DigestSettingStorage settingStorage,
                           DigestEnrollmentStorage enrollmentStorage,
                           DigestCategoryRegistry categoryRegistry,
                           DigestItemDAO digestItemDAO,
                           DigestSender digestSender) {
    this.settingStorage = settingStorage;
    this.enrollmentStorage = enrollmentStorage;
    this.categoryRegistry = categoryRegistry;
    this.digestItemDAO = digestItemDAO;
    this.digestSender = digestSender;
  }

  @Override
  public boolean isDigestAllowed() {
    return settingStorage.isDigestAllowed();
  }

  @Override
  public void saveDigestAllowed(boolean allowed) {
    settingStorage.saveDigestAllowed(allowed);
  }

  @Override
  public DigestUserSettings getUserSettings(String username) {
    // The categories of an addon uninstalled since the last save don't exist
    // any more, the user must never be shown, nor blocked by, a choice he can't
    // act on
    return keepInstalledCategories(settingStorage.getUserSettings(username));
  }

  @Override
  public void saveUserSettings(String username, DigestUserSettings settings, String timeZone) {
    if (settings == null) {
      throw new IllegalArgumentException("Digest settings are mandatory");
    }
    DigestUserSettings cleanedSettings = keepInstalledCategories(settings);
    validate(cleanedSettings);
    // The work list of the job is written first, and in its own transaction:
    // the settings go through the SettingService, which no transaction rolls
    // back, so writing them last is the only way to never end up with a user
    // seeing a digest enabled that the job doesn't know about
    enroll(username, cleanedSettings, timeZone);
    settingStorage.saveUserSettings(username, cleanedSettings);
  }

  @Override
  public List<DigestCategoryProvider> getCategories() {
    return categoryRegistry.getCategoryProviders();
  }

  @Override
  public void capture(NotificationInfo notification) {
    if (notification == null || notification.getKey() == null || !isDigestAllowed()) {
      return;
    }
    if (notification.isSendAll() || notification.isSendAllInternals()) {
      // The digest is about what happened to me, never about announcements to
      // everyone
      return;
    }
    String pluginId = notification.getKey().getId();
    String category = findCategory(pluginId);
    if (category == null || CollectionUtils.isEmpty(notification.getSendToUserIds())) {
      return;
    }
    Instant itemDate = notification.getDateCreated() == null ? Instant.now()
                                                              : notification.getDateCreated().toInstant();
    String params = DigestParamsCodec.serialize(notification.getOwnerParameter());
    // One saveAll, one transaction: either every enrolled recipient gets his
    // row or none does — the capture never half succeeds
    List<DigestItemEntity> items = notification.getSendToUserIds()
                                               .stream()
                                               .filter(StringUtils::isNotBlank)
                                               .distinct()
                                               .filter(recipient -> !notification.isExcluded(recipient))
                                               // The digest is about what happened to me, never
                                               // about what I did myself
                                               .filter(recipient -> !StringUtils.equals(recipient, notification.getFrom()))
                                               .filter(recipient -> wantsCategory(recipient, category))
                                               // The same notification fired again, like an
                                               // invitation cancelled and sent anew, must not
                                               // fill the digest with the same line twice
                                               .filter(recipient -> params == null
                                                   || !digestItemDAO.existsByUserIdAndPluginIdAndParams(recipient, pluginId, params))
                                               .map(recipient -> new DigestItemEntity(null,
                                                                                      recipient,
                                                                                      pluginId,
                                                                                      category,
                                                                                      itemDate,
                                                                                      params))
                                               .toList();
    if (!items.isEmpty()) {
      digestItemDAO.saveAll(items);
    }
  }

  @Override
  public void discard(String username, String pluginId, String parameterName, String parameterValue) {
    if (StringUtils.isBlank(username) || StringUtils.isBlank(pluginId) || StringUtils.isBlank(parameterName)
        || parameterValue == null) {
      return;
    }
    String fragment = DigestParamsCodec.fragment(parameterName, parameterValue);
    List<DigestItemEntity> items = digestItemDAO.findByUserIdAndPluginIdAndParamsContaining(username, pluginId, fragment);
    if (!items.isEmpty()) {
      digestItemDAO.deleteAll(items);
    }
  }

  @Override
  public void processDueDigests() {
    digestSender.processDueDigests();
  }

  /**
   * First half of the double check: the category lists are applied here at
   * capture, and again at send time. Unchecking a category applies immediately,
   * checking one applies from now on.
   */
  private boolean wantsCategory(String recipient, String category) {
    DigestUserSettings settings = getUserSettings(recipient);
    return settings.isDaily() && settings.getDailyCategories().contains(category)
        || settings.isWeekly() && settings.getWeeklyCategories().contains(category);
  }

  private String findCategory(String pluginId) {
    return categoryRegistry.getCategoryProviders()
                           .stream()
                           .filter(category -> category.getPluginIds().contains(pluginId))
                           .map(DigestCategoryProvider::getId)
                           .findFirst()
                           .orElse(null);
  }

  private void enroll(String username, DigestUserSettings settings, String timeZone) {
    try {
      enrollmentStorage.enroll(username, settings, timeZone);
    } catch (DataIntegrityViolationException e) {
      // Two saves of the same user at once, the row this one wanted to create
      // has just been created by the other: enrolling again now updates it
      enrollmentStorage.enroll(username, settings, timeZone);
    }
  }

  private DigestUserSettings keepInstalledCategories(DigestUserSettings settings) {
    Set<String> installedCategories = categoryRegistry.getCategoryProviders()
                                                      .stream()
                                                      .map(DigestCategoryProvider::getId)
                                                      .collect(Collectors.toSet());
    return new DigestUserSettings(settings.isDaily(),
                                  keepInstalledCategories(settings.getDailyCategories(), installedCategories),
                                  settings.isWeekly(),
                                  keepInstalledCategories(settings.getWeeklyCategories(), installedCategories));
  }

  private List<String> keepInstalledCategories(List<String> categories, Set<String> installedCategories) {
    if (CollectionUtils.isEmpty(categories)) {
      return Collections.emptyList();
    }
    return categories.stream().distinct().filter(installedCategories::contains).toList();
  }

  private void validate(DigestUserSettings settings) {
    if (settings.isDaily() && settings.getDailyCategories().isEmpty()) {
      throw new IllegalArgumentException("Daily digest can't be enabled without category");
    }
    if (settings.isWeekly() && settings.getWeeklyCategories().isEmpty()) {
      throw new IllegalArgumentException("Weekly digest can't be enabled without category");
    }
  }

}
