package org.exoplatform.commons.notification.impl.jpa.web.dao;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebUsersEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class WebUsersDAO extends GenericDAOJPAImpl<WebUsersEntity, Long> {

  private static final String RECEIVER_PARAM      = "receiver";

  private static final String DATE_PARAM          = "calendar";

  private static final String PARAM_VALUE         = "paramValue";

  private static final String PARAM_NAME          = "paramName";

  private static final String PLUGIN_ID_PARAM     = "pluginId";

  private static final String IS_ON_POPOVER_PARAM = "isOnPopover";

  private static final String USER_ID_PARAM       = "userId";

  private static final String PLUGIN_IDS_PARAM    = "pluginIds";

  @ExoTransactional
  public List<WebUsersEntity> findWebNotifsByFilter(List<String> pluginIds,
                                                    String userId,
                                                    Boolean isOnPopover,
                                                    int offset,
                                                    int limit) {
    TypedQuery<WebUsersEntity> query =
                                     getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findWebNotifsByPluginFilter",
                                                                         WebUsersEntity.class)
                                                       .setParameter(PLUGIN_IDS_PARAM, pluginIds)
                                                       .setParameter(USER_ID_PARAM, userId)
                                                       .setParameter(IS_ON_POPOVER_PARAM, isOnPopover)
                                                       .setFirstResult(offset);
    if (limit >= 0) {
      return query.setMaxResults(limit).getResultList();
    } else {
      return query.getResultList();
    }
  }

  @ExoTransactional
  public List<WebUsersEntity> findNotificationsByTypeAndParams(List<String> pluginIds,
                                                               String paramName,
                                                               String paramValue,
                                                               String receiver,
                                                               int offset,
                                                               int limit) {
    TypedQuery<WebUsersEntity> query =
                                     getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findNotificationsByTypeAndParams",
                                                                         WebUsersEntity.class)
                                                       .setParameter(PLUGIN_IDS_PARAM, pluginIds)
                                                       .setParameter(PARAM_NAME, paramName)
                                                       .setParameter(PARAM_VALUE, paramValue)
                                                       .setParameter(RECEIVER_PARAM, receiver)
                                                       .setFirstResult(offset);
    if (limit >= 0) {
      return query.setMaxResults(limit).getResultList();
    } else {
      return query.getResultList();
    }
  }
  @ExoTransactional
  public List<WebUsersEntity> findWebNotifsByFilter(String userId, int offset, int limit) {
    TypedQuery<WebUsersEntity> query =
                                     getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findWebNotifsByUserFilter",
                                                                         WebUsersEntity.class)
                                                       .setParameter(USER_ID_PARAM, userId)
                                                       .setFirstResult(offset);
    if (limit >= 0) {
      return query.setMaxResults(limit).getResultList();
    } else {
      return query.getResultList();
    }
  }

  @ExoTransactional
  public List<WebUsersEntity> findWebNotifsByFilter(String userId, boolean isOnPopover, int offset, int limit) {
    TypedQuery<WebUsersEntity> query =
                                     getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findWebNotifsByPopoverFilter",
                                                                         WebUsersEntity.class)
                                                       .setParameter(USER_ID_PARAM, userId)
                                                       .setParameter(IS_ON_POPOVER_PARAM, isOnPopover)
                                                       .setFirstResult(offset);
    if (limit >= 0) {
      return query.setMaxResults(limit).getResultList();
    } else {
      return query.getResultList();
    }
  }

  @ExoTransactional
  public List<WebUsersEntity> findWebNotifsOfUserByLastUpdatedDate(String userId, Calendar calendar) {
    return getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findWebNotifsOfUserByLastUpdatedDate",
                                               WebUsersEntity.class)
                             .setParameter(USER_ID_PARAM, userId)
                             .setParameter(DATE_PARAM, calendar)
                             .getResultList();
  }

  @ExoTransactional
  public List<WebUsersEntity> findUnreadNotification(String pluginId, String userId, String paramName, String paramValue) {
    return getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findUnreadNotification", WebUsersEntity.class)
                             .setParameter(PLUGIN_ID_PARAM, pluginId)
                             .setParameter(USER_ID_PARAM, userId)
                             .setParameter(PARAM_NAME, paramName)
                             .setParameter(PARAM_VALUE, paramValue)
                             .getResultList();
  }

  @ExoTransactional
  public void markAllRead(String userId) {
    getEntityManager().createNamedQuery("NotificationsWebUsersEntity.markWebNotifsAsReadByUser")
                      .setParameter(USER_ID_PARAM, userId)
                      .executeUpdate();
  }

  @ExoTransactional
  public List<WebUsersEntity> findWebNotifsByLastUpdatedDate(Calendar fiveDaysAgo) {
    return getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findWebNotifsByLastUpdatedDate", WebUsersEntity.class)
                             .setParameter(DATE_PARAM, fiveDaysAgo)
                             .getResultList();
  }

  @ExoTransactional
  public int getNumberOnBadge(String userId) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("NotificationsWebUsersEntity.getNumberOnBadge", Long.class)
                                               .setParameter(USER_ID_PARAM, userId);
    return query.getSingleResult().intValue();
  }

  public Map<String, Integer> countUnreadByPlugin(String userId) {
    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("NotificationsWebUsersEntity.countUnreadByPlugin", Tuple.class)
                                               .setParameter(USER_ID_PARAM, userId);
    List<Tuple> result = query.getResultList();
    if (CollectionUtils.isEmpty(result)) {
      return Collections.emptyMap();
    } else {
      return result.stream().collect(Collectors.toMap(t -> t.get(0, String.class), t -> t.get(1, Long.class).intValue()));
    }
  }

  @ExoTransactional
  public List<WebUsersEntity> findNotifsWithBadge(String userId) {
    return getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findNotifsWithBadge", WebUsersEntity.class)
                             .setParameter(USER_ID_PARAM, userId)
                             .getResultList();
  }

  public List<WebUsersEntity> findNotifsWithBadgeByPlugins(List<String> pluginIds, String username) {
    return getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findNotifsWithBadgeByPlugins", WebUsersEntity.class)
        .setParameter(USER_ID_PARAM, username)
        .setParameter(PLUGIN_IDS_PARAM, pluginIds)
        .getResultList();
  }

  public List<WebUsersEntity> findUnreadByUserAndPlugins(List<String> pluginIds, String username) {
    return getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findUnreadByUserAndPlugins", WebUsersEntity.class)
        .setParameter(USER_ID_PARAM, username)
        .setParameter(PLUGIN_IDS_PARAM, pluginIds)
        .getResultList();
  }

}
