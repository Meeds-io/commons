/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.commons.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.impl.mock.DummyOrganizationService;

/**
 * Created by eXo Platform SAS.
 *
 * @author Ali Hamdi <ahamdi@exoplatform.com>
 * @since 16/02/18 16:33
 */
public class MockOrganizationService extends DummyOrganizationService {

  static List<UserEventListener> listeners = new ArrayList<UserEventListener>();

  public MockOrganizationService() {
    super();
    this.userDAO_ = new MockUserHandlerImpl();
    this.userProfileDAO_ = new MockUserProfileHandler();
  }

  public void setMockUserHandlerImpl(UserHandlerImpl handlerImpl) {
    this.userDAO_ = handlerImpl;
  }

  public static class MockUserHandlerImpl extends UserHandlerImpl {

  }

  public class MockUserProfileHandler extends DummyUserProfileHandler {

    private Map<String,UserProfile> profiles = new HashMap<>();

    @Override
    public void saveUserProfile(UserProfile userProfile, boolean b) throws Exception {
      profiles.put(userProfile.getUserName(),userProfile);
    }

    @Override
    public UserProfile findUserProfileByName(String s) throws Exception {
      return profiles.get(s);
    }

  }

}

