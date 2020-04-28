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
package org.exoplatform.commons.persistence.impl;

import org.exoplatform.commons.api.persistence.ExoTransactional;

import javax.persistence.Query;
import java.util.List;

public class TaskDao extends GenericDAOJPAImpl<Task, Long> {

  @ExoTransactional
  public void createWithRollback(Task task) {
    getEntityManager().persist(task);
    getEntityManager().getTransaction().rollback();
  }

  public void nonTransactionalDeleteAll() {
    Query query = getEntityManager().createQuery("Delete from Task");
    query.executeUpdate();
  }

  @ExoTransactional
  public void createWithCommit(Task task) {
    getEntityManager().persist(task);
    getEntityManager().getTransaction().commit();
  }

  @ExoTransactional
  public void createWithSetRollbackOnly(Task task) {
    getEntityManager().persist(task);
    getEntityManager().getTransaction().setRollbackOnly();
  }

  @Override
  @ExoTransactional
  //We invoke this method within an @ExoTransactional context because
  //our TU are not run within a portal lifecycle => there is no EM in the threadLocal
  public List<Task> findAll() {
    return super.findAll();
  }
}
