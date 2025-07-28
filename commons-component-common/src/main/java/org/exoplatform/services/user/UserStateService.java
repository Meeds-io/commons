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
package org.exoplatform.services.user;

import java.util.*;
import java.util.stream.Collectors;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

public class UserStateService {

  public static final String             DEFAULT_STATUS        = "available";

  public static final String             STATUS_OFFLINE        = "offline";

  public static final String             INVISIBLE             = "invisible";

  public static final String             USER_STATE_CACHE_NAME = "commons.UserStateService";

  private static final int               DEFAULT_OFFLINE_DELAY = 60000;

  private static final Scope             USER_STATE_SCOPE      = Scope.APPLICATION.id("userStateScope");
  
  private static final String            USER_STATUS           = "userStatus";

  public static final String             COMETD_CHANNEL        = "/meeds/Application/UserState";

  public static final String             USER_STATUS_UPDATED   = "user-status-updated";

  private final ExoCache<String, String> userStateCache;

  private final ContinuationService      continuationService;

  private final SettingService           settingService;

  public UserStateService(ContinuationService continuationService, CacheService cacheService, SettingService settingService) {
    this.continuationService = continuationService;
    this.settingService = settingService;
    this.userStateCache = cacheService.getCacheInstance(USER_STATE_CACHE_NAME);
  }

  /**
   * @return {@link List} of {@link UserStateModel} of online users
   */
  public List<UserStateModel> online() {
    long lastActivity = System.currentTimeMillis();
    Set<String> connectedUserIds = continuationService.getConnectedUserIds();
    return connectedUserIds.stream()
                           .map(userId -> {
                             String status = userStateCache.get(userId);
                             if (status == null) {
                               status = DEFAULT_STATUS;
                             } else if (STATUS_OFFLINE.equals(status) || INVISIBLE.equals(status)) {
                               return null;
                             }
                             return new UserStateModel(userId, lastActivity, status);
                           })
                           .filter(Objects::nonNull)
                           .collect(Collectors.toList());
  }

  /**
   * Checks whether a user is online or not
   * 
   * @param userId user name
   * @return true if user is still connected else false
   */
  public boolean isOnline(String userId) {
    return continuationService.isPresent(userId);
  }

  /**
   * Return user connection state
   * 
   * @param userId user name
   * @return {@link UserStateModel}
   */
  public UserStateModel getUserState(String userId) {
    boolean online = continuationService.isPresent(userId);
    if (!online) {
      return new UserStateModel(userId, 0, STATUS_OFFLINE);
    }
    String status = userStateCache.get(userId);
    if (status == null) {
      SettingValue<?> settingValue = settingService.get(Context.USER.id(userId), USER_STATE_SCOPE, USER_STATUS);
      if (settingValue != null && settingValue.getValue() != null) {
        status = settingValue.getValue().toString();
        userStateCache.put(userId, status);
      } else {
        status = DEFAULT_STATUS;
      }
    }
    return new UserStateModel(userId, System.currentTimeMillis(), status);
  }

  /**
   * @return Last logged in user
   */
  public UserStateModel lastLogin() {
    List<UserStateModel> online = online();
    if (!online.isEmpty()) {
      return online.get(online.size() - 1);
    }
    return null;
  }

  /**
   * Changes online status of the user: donotditurb, absent, available ...
   * 
   * @param userId user name
   * @param status Status of the online user
   */
  public void saveStatus(String userId, String status) {
    userStateCache.put(userId, status);
    settingService.set(Context.USER.id(userId), USER_STATE_SCOPE, USER_STATUS, SettingValue.create(String.valueOf(status)));
    UserStateWSMessage message = new UserStateWSMessage(new UserStateModel(userId, System.currentTimeMillis(), status),
                                                        USER_STATUS_UPDATED);
    continuationService.sendBroadcastMessage(COMETD_CHANNEL, message.toString());
  }

  /**
   * @return default delay to consider user as inactive
   * @deprecated Not needed anymore since we check connected users on WebSocket
   *             Channel
   */
  @Deprecated
  public int getDelay() {
    return DEFAULT_OFFLINE_DELAY;
  }

  /**
   * Changes user online/offline status
   * 
   * @param model {@link UserStateModel}
   * @deprecated use {@link UserStateService#saveStatus(String, String)} instead
   */
  @Deprecated
  public void save(UserStateModel model) {
    saveStatus(model.getUserId(), model.getStatus());
  }

  /**
   * Changes user status to online and saves the last activity time
   * 
   * @param userId user name
   * @return saved {@link UserStateModel}
   * @deprecated not needed anymore since the user online status is managed in
   *             realtime with WebSocket Channel
   */
  @Deprecated
  public UserStateModel ping(String userId) {
    UserStateModel userState = getUserState(userId);
    if (userState == null) {
      saveStatus(userId, DEFAULT_STATUS);
      return new UserStateModel(userId, System.currentTimeMillis(), DEFAULT_STATUS);
    } else {
      return userState;
    }
  }

  public void clearCache() {
    userStateCache.clearCache();
  }
}
