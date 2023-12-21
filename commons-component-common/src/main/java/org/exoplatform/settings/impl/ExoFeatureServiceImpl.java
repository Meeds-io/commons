/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.settings.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.settings.*;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.management.annotations.*;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.IdentityRegistry;
import org.picocontainer.Startable;

@Managed
@ManagedDescription("eXo Feature Service")
@NameTemplate(
  {
      @Property(key = "service", value = "feature"),
      @Property(key = "view", value = "ExoFeatureService")
  }
)
@RESTEndpoint(path = "featureservice")
public class ExoFeatureServiceImpl implements ExoFeatureService, Startable {

  private static final String        NAME_SPACES        = "exo:";

  private SettingService             settingService;

  private OrganizationService        organizationService;

  private IdentityRegistry           identityRegistry;

  private Map<String, Boolean>       featuresProperties = new HashMap<>();

  private Map<String, FeaturePlugin> plugins            = new HashMap<>();

  public ExoFeatureServiceImpl(SettingService settingService,
                               IdentityRegistry identityRegistry,
                               OrganizationService organizationService) {
    this.settingService = settingService;
    this.organizationService = organizationService;
    this.identityRegistry = identityRegistry;
  }

  @Managed
  @ManagedDescription("Determine if the feature is active")
  @Impact(ImpactType.READ)
  @Override
  public boolean isActiveFeature(@ManagedDescription("Feature name") @ManagedName("featureName") String featureName) {
    Boolean active;
    SettingValue<?> sValue = settingService.get(Context.GLOBAL, Scope.GLOBAL.id(null), (NAME_SPACES + featureName));
    if (sValue != null) {
      active = Boolean.valueOf(sValue.getValue().toString());
    } else {
      active = getFeaturePropertyValue(featureName);
    }
    return active == null || active.booleanValue();
  }

  @Override
  public void saveActiveFeature(String featureName, boolean isActive) {
    settingService.set(Context.GLOBAL, Scope.GLOBAL.id(null), (NAME_SPACES + featureName), SettingValue.create(isActive));
  }

  @Managed
  @ManagedDescription("Activate/Deactivate feature")
  @Impact(ImpactType.WRITE)
  public void changeFeatureActivation(@ManagedDescription("Feature name") @ManagedName("featureName") String featureName,
                                      @ManagedDescription("Is active") @ManagedName("isActive") String isActive) {
    boolean isActiveBool = Boolean.parseBoolean(isActive);
    saveActiveFeature(featureName, isActiveBool);
  }

  @Override
  public void addFeaturePlugin(FeaturePlugin featurePlugin) {
    plugins.put(featurePlugin.getName(), featurePlugin);
  }

  @Override
  public boolean isFeatureActiveForUser(@ManagedDescription("Feature name") @ManagedName("featureName") String featureName,
                                        @ManagedDescription("Username") @ManagedName("userName") String username) {
    if (!isActiveFeature(featureName)) {
      return false;
    }
    FeaturePlugin featurePlugin = plugins.get(featureName);
    if (featurePlugin != null) {
      return featurePlugin.isFeatureActiveForUser(featureName, username);
    } else {
      List<String> permissions = getFeaturePermissionPropertyValues(featureName);
      return permissions.isEmpty() || permissions.stream().anyMatch(permission -> isUserMemberOf(username, permission));
    }
  }

  private Boolean getFeaturePropertyValue(String featureName) {
    String propertyName = "exo.feature." + featureName + ".enabled";
    if (featuresProperties.containsKey(propertyName)) {
      return featuresProperties.get(propertyName);
    } else {
      String propertyValue = System.getProperty(propertyName);
      Boolean active = propertyValue != null ? Boolean.valueOf(propertyValue) : null;
      featuresProperties.put(propertyName, active);
      return active;
    }
  }

  private List<String> getFeaturePermissionPropertyValues(String featureName) {
    String propertyName = "exo.feature." + featureName + ".permissions";
    String propertyValue = System.getProperty(propertyName);
    if (StringUtils.isNotBlank(propertyValue)) {
      return Arrays.stream(StringUtils.split(propertyValue, ",")).map(String::trim).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  private final boolean isUserMemberOf(String username, String permissionExpression) {
    if (StringUtils.isBlank(username)) {
      return false;
    }
    if (StringUtils.isBlank(permissionExpression)) {
      return false;
    }

    permissionExpression = permissionExpression.replace("*:", "");
    if (permissionExpression.contains(":")) {
      String[] permissionParts = permissionExpression.split(":");
      String membershipType = permissionParts[0];
      String group = permissionParts[1];

      org.exoplatform.services.security.Identity identity = identityRegistry.getIdentity(username);
      if (identity != null) {
        return identity.isMemberOf(group, membershipType);
      } else {
        try {
          Collection<Membership> memberships = organizationService.getMembershipHandler()
                                                                  .findMembershipsByUserAndGroup(username, group);
          return memberships != null
              && memberships.stream().anyMatch(membership -> StringUtils.equals(membership.getMembershipType(), membershipType));
        } catch (Exception e) {
          throw new IllegalStateException("Error getting memberships of user " + username, e);
        }
      }

    } else if (permissionExpression.contains("/")) {
      org.exoplatform.services.security.Identity identity = identityRegistry.getIdentity(username);
      if (identity != null) {
        return identity.isMemberOf(permissionExpression);
      }
      try {
        Collection<Membership> memberships = organizationService.getMembershipHandler()
                                                                .findMembershipsByUserAndGroup(username, permissionExpression);
        return memberships != null && !memberships.isEmpty();
      } catch (Exception e) {
        throw new IllegalStateException("Error getting memberships of user " + username, e);
      }

    } else {
      return StringUtils.equals(username, permissionExpression);
    }
  }

  @Override
  public void start() {
    for (Map.Entry<String, FeaturePlugin> entry : plugins.entrySet()) {
      entry.getValue().init();
    }
  }

  @Override
  public void stop() {
    // Nothing to stop
  }
}
