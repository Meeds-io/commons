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

import java.util.Locale;

import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;

/**
 * What the digest email needs to know about its recipient and about the
 * platform, gathered from the existing notification helpers so that the digest
 * emails come from the same sender and use the same links as the instant ones.
 */
@Component
public class DigestRecipientResolver {

  private final OrganizationService organizationService;

  public DigestRecipientResolver(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  /**
   * @return the recipient in the "Full Name&lt;email&gt;" form the mail queue
   *         expects, null when the user has no email or when his account is
   *         disabled: a username that is itself an address would otherwise be
   *         used as the recipient by the fallback of
   *         {@link NotificationPluginUtils#getTo(String)}. A username that
   *         is no account at all is just an address and is kept.
   */
  public String getEmail(String username) {
    if (isDisabledAccount(username)) {
      return null;
    }
    return NotificationPluginUtils.getTo(username);
  }

  public String getFirstName(String username) {
    return NotificationPluginUtils.getFirstName(username);
  }

  public Locale getLocale(String username) {
    return NotificationUtils.getLocale(NotificationPluginUtils.getLanguage(username));
  }

  /** The platform sender, the same as for the instant notification emails */
  public String getSender() {
    return NotificationPluginUtils.getFrom(null);
  }

  public String getPlatformName() {
    return NotificationPluginUtils.getBrandingPortalName();
  }

  /**
   * The platform itself (/portal), for the platform name and the "+ N more"
   * links: a direct link, not the notification redirection which lands on the
   * home page of the default site (EXO-90020)
   */
  public String getPlatformUrl() {
    return CommonsUtils.getCurrentDomain() + "/" + NotificationPluginUtils.getPortalName();
  }

  /** The user notification settings page, the same link as the instant emails footer */
  public String getSettingsUrl(String username) {
    return NotificationUtils.getProfileUrl(username);
  }

  private boolean isDisabledAccount(String username) {
    boolean lifecycle = organizationService instanceof ComponentRequestLifecycle;
    if (lifecycle) {
      RequestLifeCycle.begin((ComponentRequestLifecycle) organizationService);
    }
    try {
      User user = organizationService.getUserHandler().findUserByName(username, UserStatus.ANY);
      return user != null && !user.isEnabled();
    } catch (Exception e) {
      // Not swallowed: the occurrence is rolled back and retried, never sent
      // to an account whose status is unknown
      throw new IllegalStateException("Cannot read the status of the digest recipient " + username, e);
    } finally {
      if (lifecycle) {
        RequestLifeCycle.end();
      }
    }
  }

}
