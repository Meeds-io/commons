package org.exoplatform.commons.notification;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.model.UserSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.service.storage.MailNotificationStorage;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.commons.notification.plugin.PluginTest;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationServiceTest extends BaseNotificationTestCase {
  
  private NotificationService       notificationService;
  private MailNotificationStorage   mailNotificationStorage;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    notificationService = getService(NotificationService.class);
    mailNotificationStorage = getService(MailNotificationStorage.class);
  }
  
  @Override
  public void tearDown() throws Exception {
    Node homeNode = (Node) session.getItem("/eXoNotification/messageHome");
    NodeIterator iterator = homeNode.getNodes();
    while (iterator.hasNext()) {
      Node node = (iterator.nextNode());
      node.remove();
    }
    session.save();
    super.tearDown();
  }
  
  private NotificationInfo saveNotification(String userDaily, String userWeekly) throws Exception {
    NotificationInfo notification = NotificationInfo.instance();
    Map<String, String> params = new HashMap<String, String>();
    params.put("objectId", "idofobject");
    notification.key(PluginTest.ID).setSendToDaily(userDaily)
                .setSendToWeekly(userWeekly).setOwnerParameter(params).setOrder(1);
    mailNotificationStorage.save(notification);
    addMixin(notification.getId());
    return notification;
  }
  
  public void testServiceNotNull() throws Exception {
    assertNotNull(notificationService);
    assertNotNull(mailNotificationStorage);
    saveNotification("root", "demo");
  }

  public void testSave() throws Exception {
    NotificationInfo notification = saveNotification("root", "demo");
    
    NotificationInfo notification2 = getNotificationInfoByKeyIdAndParam(PluginTest.ID, "objectId=idofobject");
    assertNotNull(notification2);
    
    assertTrue(notification2.equals(notification));
    
  }
  
  public void testNormalGetByUserAndRemoveMessagesSent() throws Exception {
    NotificationInfo notification = saveNotification("root", "demo");
    UserSetting userSetting = UserSetting.getInstance();
    userSetting.setUserId("root").addPlugin(PluginTest.ID, FREQUENCY.DAILY);
    userSetting.setChannelActive(UserSetting.EMAIL_CHANNEL);
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    String dayName = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    context.append(NotificationJob.DAY_OF_JOB, dayName);
    //
    context.append(NotificationJob.JOB_WEEKLY, false);
    Map<PluginKey, List<NotificationInfo>> map = mailNotificationStorage.getByUser(context, userSetting);
    
    List<NotificationInfo> list = map.get(new PluginKey(PluginTest.ID));
    assertEquals(1, list.size());
    
    assertTrue(list.get(0).equals(notification));
    // after sent, user demo will auto remove from property daily
    NotificationInfo notification2 = getNotificationInfoByKeyIdAndParam(PluginTest.ID, "objectId=idofobject");
    assertNotNull(notification2);
    
    assertEquals(0, notification2.getSendToDaily().length);
    
    context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, false);
    context.append(NotificationJob.JOB_WEEKLY, true);
    
    userSetting.setUserId("demo").addPlugin(PluginTest.ID, FREQUENCY.WEEKLY);
    map = mailNotificationStorage.getByUser(context, userSetting);
    list = map.get(new PluginKey(PluginTest.ID));
    assertEquals(1, list.size());
    
    
    mailNotificationStorage.removeMessageAfterSent(context);
    
    notification2 = getNotificationInfoByKeyIdAndParam(PluginTest.ID, "objectId=idofobject");
    assertNull(notification2);
  }

  public void testSpecialGetByUserAndRemoveMessagesSent() throws Exception {
    NotificationInfo notification = NotificationInfo.instance();
    Map<String, String> params = new HashMap<String, String>();
    params.put("objectId", "idofobject");
    notification.key(PluginTest.ID).setSendAll(true).setOwnerParameter(params).setOrder(1);
    mailNotificationStorage.save(notification);
    
    UserSetting userSetting = UserSetting.getInstance();
    userSetting.setUserId("root").addPlugin(PluginTest.ID, FREQUENCY.DAILY);
    userSetting.setChannelActive(UserSetting.EMAIL_CHANNEL);
    // Test send to daily
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    String dayName = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    context.append(NotificationJob.DAY_OF_JOB, dayName);
    //
    context.append(NotificationJob.JOB_WEEKLY, false);
    
    Map<PluginKey, List<NotificationInfo>> map = mailNotificationStorage.getByUser(context, userSetting);
    
    List<NotificationInfo> list = map.get(new PluginKey(PluginTest.ID));
    assertEquals(1, list.size());
    
    assertTrue(list.get(0).equals(notification));
    // check value from node
    NotificationInfo notification2 = getNotificationInfoByKeyIdAndParam(PluginTest.ID, "objectId=idofobject");
    assertNotNull(notification2);

    assertEquals(NotificationInfo.FOR_ALL_USER, notification2.getSendToDaily()[0]);
    // remove value on property sendToDaily
    mailNotificationStorage.removeMessageAfterSent(context);

    //after sent daily, the message's sendToDaily property must be empty
    notification2 = getNotificationInfoByKeyIdAndParam(PluginTest.ID, "objectId=idofobject");
    assertEquals(0, notification2.getSendToDaily().length);
    
    // Test send to weekly
    context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, false);
    context.append(NotificationJob.JOB_WEEKLY, true);
    userSetting.setUserId("demo").addPlugin(PluginTest.ID, FREQUENCY.WEEKLY);
    map = mailNotificationStorage.getByUser(context, userSetting);
    list = map.get(new PluginKey(PluginTest.ID));
    assertEquals(1, list.size());
    
    mailNotificationStorage.removeMessageAfterSent(context);
    
    notification2 = getNotificationInfoByKeyIdAndParam(PluginTest.ID, "objectId=idofobject");
    assertNull(notification2);
  }

  public void testWithUserNameContainSpecialCharacter() throws Exception {
    String userNameSpecial = "Rabe'e \"AbdelWahabô";
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(NotificationJob.JOB_DAILY, true);
    String dayName = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    context.append(NotificationJob.DAY_OF_JOB, dayName);
    //
    context.append(NotificationJob.JOB_WEEKLY, false);
    
    NotificationInfo notification = saveNotification(userNameSpecial, "demo");
    //
    UserSetting userSetting = UserSetting.getInstance();
    userSetting.setUserId(userNameSpecial).addPlugin(PluginTest.ID, FREQUENCY.DAILY);
    userSetting.setChannelActive(UserSetting.EMAIL_CHANNEL);
    //
    Map<PluginKey, List<NotificationInfo>> map = mailNotificationStorage.getByUser(context, userSetting);
    List<NotificationInfo> list = map.get(new PluginKey(PluginTest.ID));
    //
    assertEquals(1, list.size());
    assertTrue(list.get(0).equals(notification));
  }
  
  private void addMixin(String msgId) throws Exception {
    Node msgNode = getMessageNodeById(msgId);
    if (msgNode != null) {
      msgNode.addMixin("exo:datetime");
      msgNode.setProperty("exo:dateCreated", Calendar.getInstance());
      session.save();
    }
  }

  private Node getMessageNodeById(String msgId) throws Exception {
    return getMessageNode(new StringBuffer("exo:name = '").append(msgId).append("'").toString(), "");
  }

  private NotificationInfo getNotificationInfoByKeyIdAndParam(String key, String param) throws Exception {
    Node node = getMessageNode(new StringBuffer("ntf:ownerParameter LIKE '%").append(param).append("%'").toString(), key);

    if(node != null) {
      NotificationInfo message = NotificationInfo.instance()
          .setOrder(Integer.valueOf(node.getProperty(AbstractService.NTF_ORDER).getString()))
          .key(node.getProperty(AbstractService.NTF_PROVIDER_TYPE).getString())
          .setOwnerParameter(node.getProperty(AbstractService.NTF_OWNER_PARAMETER).getValues())
          .setSendToDaily(NotificationUtils.valuesToArray(node.getProperty(AbstractService.NTF_SEND_TO_DAILY).getValues()))
          .setSendToWeekly(NotificationUtils.valuesToArray(node.getProperty(AbstractService.NTF_SEND_TO_WEEKLY).getValues()))
          .setId(node.getName());

      if (node.hasProperty(AbstractService.NTF_FROM)) {
        message.setFrom(node.getProperty(AbstractService.NTF_FROM).getString());
      }

      return message;
    } else {
      return null;
    }
  }
  
  private Node getMessageNode(String strQuery, String key) throws Exception {
    StringBuilder sqlQuery = new StringBuilder("SELECT * FROM ntf:message WHERE ");
    if (key != null && key.length() > 0) {
      sqlQuery.append(" jcr:path LIKE '").append("/eXoNotification/messageHome/").append(key).append("/%' AND ");
    }
    sqlQuery.append(strQuery);

    QueryManager qm = session.getWorkspace().getQueryManager();
    Query query = qm.createQuery(sqlQuery.toString(), Query.SQL);
    NodeIterator iter = query.execute().getNodes();
    return (iter.getSize() > 0) ? iter.nextNode() : null;
  }

}
