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
package org.exoplatform.mfa.storage;

import org.exoplatform.mfa.storage.dao.RevocationRequestDAO;
import org.exoplatform.mfa.storage.dto.RevocationRequest;
import org.exoplatform.mfa.storage.entity.RevocationRequestEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Storage service to access / load and save applications. This service will be
 * used , as well, to convert from JPA entity to DTO.
 */
public class MfaStorage {
  private final RevocationRequestDAO revocationRequestDAO;

  public MfaStorage(RevocationRequestDAO revocationRequestDAO) {
    this.revocationRequestDAO = revocationRequestDAO;
  }

  public RevocationRequest createRevocationRequest(RevocationRequest revocationRequest) {
    if (revocationRequest == null) {
      throw new IllegalArgumentException("RevocationRequest is mandatory");
    }
    RevocationRequestEntity revocationRequestEntity = toEntity(revocationRequest);
    revocationRequestEntity.setId(null);
    revocationRequestEntity = revocationRequestDAO.create(revocationRequestEntity);
    return toDTO(revocationRequestEntity);
  }

  public RevocationRequest findById(Long id) {
    return toDTO(revocationRequestDAO.findById(id));

  }

  public List<RevocationRequest> findAll() {
    return revocationRequestDAO.findAll().stream().map(this::toDTO).collect(Collectors.toList());

  }
  public long countByUsernameAndType(String username, String type) {
    return revocationRequestDAO.countByUsernameAndType(username,type);
  }

  public void deleteRevocationRequest(String username, String type) {
    revocationRequestDAO.deleteByUsernameAndType(username, type);
  }

  public void deleteById(Long id) {
    revocationRequestDAO.deleteById(id);
  }

  private RevocationRequestEntity toEntity(RevocationRequest revocationRequest) {
    if (revocationRequest == null) {
      return null;
    }
    return new RevocationRequestEntity(revocationRequest.getId(), revocationRequest.getUser(), revocationRequest.getType());
  }

  private RevocationRequest toDTO(RevocationRequestEntity revocationRequestEntity) {
    if (revocationRequestEntity == null) {
      return null;
    }
    return new RevocationRequest(revocationRequestEntity.getId(),
                                  revocationRequestEntity.getUsername(),
                                  revocationRequestEntity.getType());
  }
}
