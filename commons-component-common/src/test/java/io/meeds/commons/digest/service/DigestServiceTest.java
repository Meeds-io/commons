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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;

import io.meeds.commons.digest.DigestCategoryRegistry;
import io.meeds.commons.digest.dao.DigestItemDAO;
import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.model.DigestUserSettings;
import io.meeds.commons.digest.plugin.DigestCategoryPlugin;
import io.meeds.commons.digest.plugin.DigestCategoryProvider;

/**
 * Checks the rules the service adds on top of the storages: what a valid choice
 * is, what happens to the categories of an uninstalled addon, and in which
 * order the two storages are written.
 */
@RunWith(MockitoJUnitRunner.class)
public class DigestServiceTest {

  private static final String     USERNAME  = "digestUser";

  private static final String     TIME_ZONE = "Europe/Paris";

  @Mock
  private DigestSettingStorage    settingStorage;

  @Mock
  private DigestEnrollmentStorage enrollmentStorage;

  @Mock
  private DigestItemDAO           digestItemDAO;

  private DigestServiceImpl       digestService;

  @Before
  public void setUp() {
    DigestCategoryRegistry categoryRegistry = new DigestCategoryRegistry();
    categoryRegistry.addCategoryProvider(categoryPlugin("spaces", 20, "SpaceInvitationPlugin"));
    categoryRegistry.addCategoryProvider(categoryPlugin("feed", 10, "PostActivityPlugin"));
    digestService = new DigestServiceImpl(settingStorage, enrollmentStorage, categoryRegistry, digestItemDAO);
  }

  @Test
  public void testCategoriesAreReturnedInDisplayOrder() {
    List<DigestCategoryProvider> categories = digestService.getCategories();
    assertEquals(Arrays.asList("feed", "spaces"), categories.stream().map(DigestCategoryProvider::getId).toList());
  }

  @Test
  public void testSaveRejectsNoSettings() {
    assertThrows(IllegalArgumentException.class, () -> digestService.saveUserSettings(USERNAME, null, TIME_ZONE));
    verifyNothingWasSaved();
  }

  @Test
  public void testSaveRejectsAFrequencyEnabledWithoutCategory() {
    DigestUserSettings settings = new DigestUserSettings(true, Collections.emptyList(), false, Collections.emptyList());
    assertThrows(IllegalArgumentException.class, () -> digestService.saveUserSettings(USERNAME, settings, TIME_ZONE));
    verifyNothingWasSaved();
  }

  @Test
  public void testSaveRejectsAFrequencyLeftWithNoInstalledCategory() {
    DigestUserSettings settings = new DigestUserSettings(true,
                                                         Collections.singletonList("uninstalled"),
                                                         false,
                                                         Collections.emptyList());
    assertThrows(IllegalArgumentException.class, () -> digestService.saveUserSettings(USERNAME, settings, TIME_ZONE));
    verifyNothingWasSaved();
  }

  @Test
  public void testSaveLeavesOutTheCategoriesOfAnUninstalledAddon() {
    // The user enabled the tasks category before the addon was uninstalled: he
    // must still be able to save, without the category that doesn't exist
    digestService.saveUserSettings(USERNAME,
                                   new DigestUserSettings(true,
                                                          Arrays.asList("spaces", "uninstalled"),
                                                          false,
                                                          Collections.singletonList("uninstalled")),
                                   TIME_ZONE);

    DigestUserSettings saved = captureSavedSettings();
    assertEquals(Collections.singletonList("spaces"), saved.getDailyCategories());
    assertEquals(Collections.emptyList(), saved.getWeeklyCategories());
  }

  @Test
  public void testSaveKeepsEachCategoryOnce() {
    digestService.saveUserSettings(USERNAME,
                                   new DigestUserSettings(true,
                                                          Arrays.asList("spaces", "feed", "spaces"),
                                                          false,
                                                          Collections.emptyList()),
                                   TIME_ZONE);

    assertEquals(Arrays.asList("spaces", "feed"), captureSavedSettings().getDailyCategories());
  }

  @Test
  public void testSaveEnrollsBeforeWritingTheSettings() {
    digestService.saveUserSettings(USERNAME, dailyOn(), TIME_ZONE);

    // The settings don't belong to any transaction: writing them before a
    // failing enrollment would leave the user with a digest the job ignores
    InOrder inOrder = Mockito.inOrder(enrollmentStorage, settingStorage);
    inOrder.verify(enrollmentStorage).enroll(eq(USERNAME), any(), eq(TIME_ZONE));
    inOrder.verify(settingStorage).saveUserSettings(eq(USERNAME), any());
  }

  @Test
  public void testSaveDoesNotWriteTheSettingsWhenTheEnrollmentFails() {
    doThrow(new IllegalStateException("Database is down")).when(enrollmentStorage).enroll(any(), any(), any());

    assertThrows(IllegalStateException.class, () -> digestService.saveUserSettings(USERNAME, dailyOn(), TIME_ZONE));

    verify(settingStorage, never()).saveUserSettings(any(), any());
  }

  @Test
  public void testSaveEnrollsAgainWhenTheSameUserIsSavedTwiceAtOnce() {
    doThrow(new DataIntegrityViolationException("Duplicate USER_ID")).doNothing()
                                                                    .when(enrollmentStorage)
                                                                    .enroll(any(), any(), any());

    digestService.saveUserSettings(USERNAME, dailyOn(), TIME_ZONE);

    // The row the concurrent save created is now updated instead of inserted
    verify(enrollmentStorage, times(2)).enroll(eq(USERNAME), any(), eq(TIME_ZONE));
    verify(settingStorage).saveUserSettings(eq(USERNAME), any());
  }

  @Test
  public void testReadLeavesOutTheCategoriesOfAnUninstalledAddon() {
    when(settingStorage.getUserSettings(USERNAME)).thenReturn(new DigestUserSettings(true,
                                                                                     Arrays.asList("spaces", "uninstalled"),
                                                                                     false,
                                                                                     Collections.emptyList()));

    assertEquals(Collections.singletonList("spaces"), digestService.getUserSettings(USERNAME).getDailyCategories());
  }

  @Test
  public void testCaptureStoresOneRowPerEnrolledRecipient() {
    when(settingStorage.isDigestAllowed()).thenReturn(true);
    when(settingStorage.getUserSettings("mary")).thenReturn(dailyOn());
    when(settingStorage.getUserSettings("john")).thenReturn(offSettings());

    digestService.capture(notification("SpaceInvitationPlugin", "mary", "john"));

    ArgumentCaptor<List<DigestItemEntity>> captor = ArgumentCaptor.forClass(List.class);
    verify(digestItemDAO, times(1)).saveAll(captor.capture());
    assertEquals(1, captor.getValue().size());
    DigestItemEntity item = captor.getValue().get(0);
    assertEquals("mary", item.getUserId());
    assertEquals("SpaceInvitationPlugin", item.getPluginId());
    assertEquals("spaces", item.getCategory());
    assertEquals("{\"spaceId\":\"42\"}", item.getParams());
  }

  @Test
  public void testCaptureStoresNothingWhenTheAdminSwitchIsOff() {
    when(settingStorage.isDigestAllowed()).thenReturn(false);
    digestService.capture(notification("SpaceInvitationPlugin", "mary"));
    verify(digestItemDAO, never()).saveAll(any());
  }

  @Test
  public void testCaptureIgnoresAPluginOfNoInstalledCategory() {
    when(settingStorage.isDigestAllowed()).thenReturn(true);
    digestService.capture(notification("LikePlugin", "mary"));
    verify(digestItemDAO, never()).saveAll(any());
  }

  @Test
  public void testCaptureIgnoresARecipientWhoUnselectedTheCategory() {
    when(settingStorage.isDigestAllowed()).thenReturn(true);
    when(settingStorage.getUserSettings("mary")).thenReturn(new DigestUserSettings(true,
                                                                                  Collections.singletonList("feed"),
                                                                                  false,
                                                                                  Collections.emptyList()));
    digestService.capture(notification("SpaceInvitationPlugin", "mary"));
    verify(digestItemDAO, never()).saveAll(any());
  }

  @Test
  public void testCaptureServesTheWeeklyListToo() {
    when(settingStorage.isDigestAllowed()).thenReturn(true);
    when(settingStorage.getUserSettings("mary")).thenReturn(new DigestUserSettings(false,
                                                                                  Collections.emptyList(),
                                                                                  true,
                                                                                  Collections.singletonList("spaces")));
    digestService.capture(notification("SpaceInvitationPlugin", "mary"));
    verify(digestItemDAO, times(1)).saveAll(any());
  }

  @Test
  public void testCaptureIgnoresBroadcastsToEveryone() {
    when(settingStorage.isDigestAllowed()).thenReturn(true);
    digestService.capture(notification("SpaceInvitationPlugin", "mary").setSendAll(true));
    verify(digestItemDAO, never()).saveAll(any());
  }

  @Test
  public void testCaptureIgnoresExcludedAndDuplicatedRecipients() {
    when(settingStorage.isDigestAllowed()).thenReturn(true);
    when(settingStorage.getUserSettings("mary")).thenReturn(dailyOn());
    NotificationInfo notification = notification("SpaceInvitationPlugin", "mary", "mary", "john");
    notification.exclude("john");
    lenient().when(settingStorage.getUserSettings("john")).thenReturn(dailyOn());

    digestService.capture(notification);

    ArgumentCaptor<List<DigestItemEntity>> captor = ArgumentCaptor.forClass(List.class);
    verify(digestItemDAO, times(1)).saveAll(captor.capture());
    assertEquals(1, captor.getValue().size());
  }

  @Test
  public void testCaptureLeavesOutTheValuesThatAreNoIdentifiers() {
    when(settingStorage.isDigestAllowed()).thenReturn(true);
    when(settingStorage.getUserSettings("mary")).thenReturn(dailyOn());
    NotificationInfo notification = notification("SpaceInvitationPlugin", "mary")
                                                                                 .with("comment",
                                                                                       "x".repeat(300));

    digestService.capture(notification);

    ArgumentCaptor<List<DigestItemEntity>> captor = ArgumentCaptor.forClass(List.class);
    verify(digestItemDAO).saveAll(captor.capture());
    assertEquals("{\"spaceId\":\"42\"}", captor.getValue().get(0).getParams());
  }

  @Test
  public void testCaptureSurvivesANullNotification() {
    digestService.capture(null);
    verify(digestItemDAO, never()).saveAll(any());
  }

  private NotificationInfo notification(String pluginId, String... recipients) {
    return NotificationInfo.instance()
                           .key(new PluginKey(pluginId))
                           .to(new ArrayList<>(Arrays.asList(recipients)))
                           .with("spaceId", "42");
  }

  private DigestUserSettings offSettings() {
    return new DigestUserSettings(false, Collections.emptyList(), false, Collections.emptyList());
  }

  private DigestUserSettings dailyOn() {
    return new DigestUserSettings(true, Collections.singletonList("spaces"), false, Collections.emptyList());
  }

  private DigestUserSettings captureSavedSettings() {
    ArgumentCaptor<DigestUserSettings> captor = ArgumentCaptor.forClass(DigestUserSettings.class);
    verify(settingStorage).saveUserSettings(eq(USERNAME), captor.capture());
    return captor.getValue();
  }

  private void verifyNothingWasSaved() {
    verify(settingStorage, never()).saveUserSettings(any(), any());
    verify(enrollmentStorage, never()).enroll(any(), any(), any());
  }

  private DigestCategoryPlugin categoryPlugin(String id, int order, String pluginId) {
    InitParams params = new InitParams();
    params.addParam(valueParam("id", id));
    params.addParam(valueParam("labelKey", "digest.category." + id));
    params.addParam(valueParam("order", String.valueOf(order)));
    ValuesParam pluginIds = new ValuesParam();
    pluginIds.setName("pluginIds");
    pluginIds.setValues(new ArrayList<>(List.of(pluginId)));
    params.addParam(pluginIds);
    return new DigestCategoryPlugin(params);
  }

  private ValueParam valueParam(String name, String value) {
    ValueParam valueParam = new ValueParam();
    valueParam.setName(name);
    valueParam.setValue(value);
    return valueParam;
  }

}
