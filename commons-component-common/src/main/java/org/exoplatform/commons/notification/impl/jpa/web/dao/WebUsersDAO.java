package org.exoplatform.commons.notification.impl.jpa.web.dao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.impl.jpa.web.entity.WebUsersEntity;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;

public class WebUsersDAO extends GenericDAOJPAImpl<WebUsersEntity, Long> {

  private static final String  QUERY_FILTER_FIND_PREFIX  = "NotificationsWebUsersEntity.findFilteredWebNotifs";

  private static final String  QUERY_FILTER_COUNT_PREFIX = "NotificationsWebUsersEntity.countFilteredWebNotifs";

  private static final String  RECEIVER_PARAM            = "receiver";

  private static final String  DATE_PARAM                = "calendar";

  private static final String  PARAM_VALUE               = "paramValue";

  private static final String  PARAM_NAME                = "paramName";

  private static final String  PLUGIN_ID_PARAM           = "pluginId";

  private static final String  IS_ON_POPOVER_PARAM       = "isOnPopover";

  private static final String  USER_ID_PARAM             = "userId";

  private static final String  IS_READ_PARAM             = "isRead";

  private static final String  PLUGIN_IDS_PARAM          = "pluginIds";

  private Map<String, Boolean> filterNamedQueries        = new HashMap<>();

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
  public List<WebUsersEntity> findWebNotifsByLastUpdatedDate(Calendar date) {
    return getEntityManager().createNamedQuery("NotificationsWebUsersEntity.findWebNotifsByLastUpdatedDate", WebUsersEntity.class)
                             .setParameter(DATE_PARAM, date)
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

  public List<WebUsersEntity> findWebNotificationsByFilter(WebNotificationFilter filter, int offset, int limit) {
    TypedQuery<WebUsersEntity> query = buildQueryFromFilter(filter, WebUsersEntity.class, false);
    if (offset > 0) {
      query.setFirstResult(offset);
    }
    if (limit > 0) {
      query.setMaxResults(limit);
    }
    List<WebUsersEntity> result = query.getResultList();
    if (CollectionUtils.isEmpty(result)) {
      return Collections.emptyList();
    } else {
      return result;
    }
  }

  public int countWebNotificationsByFilter(WebNotificationFilter filter) {
    TypedQuery<Long> query = buildQueryFromFilter(filter, Long.class, true);
    return query.getSingleResult().intValue();
  }

  private <T> TypedQuery<T> buildQueryFromFilter(WebNotificationFilter filter, Class<T> clazz, boolean count) {
    List<String> suffixes = new ArrayList<>();
    List<String> joins = new ArrayList<>();
    List<String> predicates = new ArrayList<>();
    buildPredicates(filter, suffixes, joins, predicates);

    TypedQuery<T> query;
    String queryName = getQueryFilterName(suffixes, count);
    if (filterNamedQueries.containsKey(queryName)) {
      query = getEntityManager().createNamedQuery(queryName, clazz);
    } else {
      String queryContent = getQueryFilterContent(joins, predicates, count);
      query = getEntityManager().createQuery(queryContent, clazz);
      getEntityManager().getEntityManagerFactory().addNamedQuery(queryName, query);
      filterNamedQueries.put(queryName, true);
    }

    addQueryFilterParameters(filter, query);
    return query;
  }

  private <T> void addQueryFilterParameters(WebNotificationFilter filter, TypedQuery<T> query) { // NOSONAR
    if (StringUtils.isNotBlank(filter.getUserId())) {
      query.setParameter(USER_ID_PARAM, filter.getUserId());
    }
    if (filter.getIsRead() != null) {
      query.setParameter(IS_READ_PARAM, filter.getIsRead());
    }
    if (CollectionUtils.isNotEmpty(filter.getPluginKeys())) {
      query.setParameter(PLUGIN_IDS_PARAM, filter.getPluginKeys().stream().map(PluginKey::getId).toList());
    }
    if (filter.getLimitDay() > 0) {
      Calendar cal = Calendar.getInstance();
      long delayTime = System.currentTimeMillis() - (filter.getLimitDay() * 86400000l);
      cal.setTimeInMillis(delayTime);
      query.setParameter(DATE_PARAM, delayTime);
    }
    if (filter.isOnPopover()) {
      query.setParameter(IS_ON_POPOVER_PARAM, filter.isOnPopover());
    }
    if (filter.getParameter() != null) {
      query.setParameter(PARAM_NAME, filter.getParameter().getKey());
      query.setParameter(PARAM_VALUE, filter.getParameter().getValue());
    }
  }

  private String getQueryFilterName(List<String> suffixes, boolean count) {
    String queryName;
    if (suffixes.isEmpty()) {
      queryName = count ? QUERY_FILTER_COUNT_PREFIX : QUERY_FILTER_FIND_PREFIX;
    } else {
      queryName = (count ? QUERY_FILTER_COUNT_PREFIX : QUERY_FILTER_FIND_PREFIX) + "By" + StringUtils.join(suffixes, "And");
    }
    return queryName;
  }

  private String getQueryFilterContent(List<String> joins, List<String> predicates, boolean count) {
    String querySelect = count ? "SELECT COUNT(u) FROM NotificationsWebUsersEntity u "
                               : "SELECT u FROM NotificationsWebUsersEntity u ";

    String queryContent;
    if (predicates.isEmpty() && joins.isEmpty()) {
      queryContent = querySelect;
    } else {
      queryContent = querySelect;
      if (!joins.isEmpty()) {
        queryContent += " " + StringUtils.join(joins, " ");
      }
      if (!predicates.isEmpty()) {
        queryContent += " WHERE " + StringUtils.join(predicates, " AND ");
      }
    }
    if (!count) {
      queryContent += " ORDER BY u.updateDate DESC ";
    }
    return queryContent;
  }

  private void buildPredicates(WebNotificationFilter filter, List<String> suffixes, List<String> joins, List<String> predicates) { // NOSONAR
    if (filter.getParameter() != null || CollectionUtils.isNotEmpty(filter.getPluginKeys())) {
      if (CollectionUtils.isNotEmpty(filter.getPluginKeys())) {
        joins.add("INNER JOIN u.webNotification w ON w.type IN (:" + PLUGIN_IDS_PARAM + ")");
        suffixes.add("PluginKeys");
      } else {
        joins.add("INNER JOIN u.webNotification w");
      }
      if (filter.getParameter() != null) {
        suffixes.add("Parameter");
        joins.add("INNER JOIN w.parameters p ON p.name = :" + PARAM_NAME + " AND p.value = :" + PARAM_VALUE);
      }
    }

    if (StringUtils.isNotBlank(filter.getUserId())) {
      suffixes.add("UserId");
      predicates.add("u.receiver = :" + USER_ID_PARAM);
    }
    if (filter.getIsRead() != null) {
      suffixes.add("IsRead");
      predicates.add("u.read = :" + IS_READ_PARAM);
    }
    if (filter.getLimitDay() > 0) {
      suffixes.add("LimitDay");
      predicates.add("u.updateDate < " + DATE_PARAM);
    }
    if (filter.isOnPopover()) {
      suffixes.add("IsOnPopover");
      predicates.add("u.showPopover = :" + IS_ON_POPOVER_PARAM);
    }
  }

}
