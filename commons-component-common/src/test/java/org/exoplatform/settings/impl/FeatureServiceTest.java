/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.settings.impl;

import org.exoplatform.commons.api.settings.ExoFeatureService;
import org.exoplatform.commons.testing.BaseCommonsTestCase;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import java.util.HashSet;
import java.util.Set;

public class FeatureServiceTest extends BaseCommonsTestCase {

  private ExoFeatureService featureService;

  private IdentityRegistry identityRegistry;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    featureService = getService(ExoFeatureService.class);
    identityRegistry = getService(IdentityRegistry.class);
  }
  
  public void testShouldFeatureBeActiveWhenSettingIsTrue() {
    // Given
    featureService.saveActiveFeature("feature1", true);

    // When
    boolean activeFeature = featureService.isActiveFeature("feature1");

    // Then
    assertTrue(activeFeature);
  }

  public void testShouldFeatureNotBeActiveWhenSettingIsFalse() {
    // Given
    featureService.saveActiveFeature("feature2", false);

    // When
    boolean activeFeature = featureService.isActiveFeature("feature2");

    // Then
    assertFalse(activeFeature);
  }

  public void testShouldFeatureBeActiveWhenNoSettingAndNoProperty() {
    // Given

    // When
    boolean activeFeature = featureService.isActiveFeature("feature3");

    // Then
    assertTrue(activeFeature);
  }

  public void testShouldFeatureBeActiveWhenNoSettingAndPropertyIsTrue() {
    // Given
    System.setProperty("exo.feature.feature3.enabled", "true");

    // When
    boolean activeFeature = featureService.isActiveFeature("feature3");

    // Then
    assertTrue(activeFeature);
  }

  public void testShouldFeatureNotBeActiveWhenNoSettingAndPropertyIsFalse() {
    // Given
    System.setProperty("exo.feature.feature4.enabled", "false");

    // When
    boolean activeFeature = featureService.isActiveFeature("feature4");

    // Then
    assertFalse(activeFeature);
  }

  public void testShouldFeatureBeActiveWhenSettingIsTrueAndPropertyIsFalse() {
    // Given
    featureService.saveActiveFeature("feature5", true);
    System.setProperty("exo.feature.feature5.enabled", "false");

    // When
    boolean activeFeature = featureService.isActiveFeature("feature5");

    // Then
    assertTrue(activeFeature);
  }

  public void testShouldFeatureNotBeActiveWhenSettingIsFalseAndPropertyIsTrue() {
    // Given
    featureService.saveActiveFeature("feature6", false);
    System.setProperty("exo.feature.feature6.enabled", "true");

    // When
    boolean activeFeature = featureService.isActiveFeature("feature6");

    // Then
    assertFalse(activeFeature);
  }

  public void testSaveActiveFeature() throws Exception {
    //
    featureService.saveActiveFeature("notification", false);
    assertFalse(featureService.isActiveFeature("notification"));

    //
    featureService.saveActiveFeature("notification", true);
    assertTrue(featureService.isActiveFeature("notification"));
  }

  public void testActiveFeatureForUser() throws Exception {
    // Given
    System.setProperty("exo.feature.feature1.permissions", "*:/platform/developers");
    Set<MembershipEntry> memberships = new HashSet<MembershipEntry>();
    memberships.add(new MembershipEntry("/platform/developers", "manager"));
    Identity identity = new Identity("usera", memberships);
    identityRegistry.register(identity);

    // When
    featureService.saveActiveFeature("feature1", true);
    boolean test1 = featureService.isFeatureActiveForUser("feature1", identity.getUserId());

    // Then
    assertTrue(test1);

    // Given
    Set<MembershipEntry> memberships1 = new HashSet<MembershipEntry>();
    memberships1.add(new MembershipEntry("/platform/designers", "member"));
    Identity identity1 = new Identity("userb", memberships1);
    identityRegistry.register(identity1);

    // When
    featureService.saveActiveFeature("feature1", true);
    boolean test2 = featureService.isFeatureActiveForUser("feature1", identity1.getUserId());

    // Then
    assertFalse(test2);
  }

  public void testIsActiveFeature() throws Exception {
    assertTrue("Feature should be enabled if no flag is stored in DB", featureService.isActiveFeature("wallet"));
    featureService.saveActiveFeature("wallet", false);
    assertFalse("Feature should be disabled after disabling it", featureService.isActiveFeature("wallet"));
    featureService.saveActiveFeature("wallet", true);
    assertTrue("Feature should be enabled if it's changed on DB", featureService.isActiveFeature("wallet"));
  }

}
