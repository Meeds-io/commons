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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;

import io.meeds.commons.digest.model.DigestUserSettings;
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

  private DigestServiceImpl       digestService;

  @Before
  public void setUp() {
    digestService = new DigestServiceImpl(settingStorage, enrollmentStorage, categoryProviders());
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

  private List<DigestCategoryProvider> categoryProviders() {
    return Arrays.asList(categoryProvider("spaces", 20), categoryProvider("feed", 10));
  }

  private DigestCategoryProvider categoryProvider(String id, int order) {
    return new DigestCategoryProvider() {
      @Override
      public String getId() {
        return id;
      }

      @Override
      public String getLabelKey() {
        return "digest.category." + id;
      }

      @Override
      public int getOrder() {
        return order;
      }

      @Override
      public List<String> getPluginIds() {
        return Collections.singletonList(id + "Plugin");
      }
    };
  }

}
