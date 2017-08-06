package org.exoplatform.commons.notification.impl.jpa.email;

import static org.exoplatform.commons.notification.impl.jpa.EntityConverter.convertQueueEntityToMessageInfo;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.picocontainer.Startable;

import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.notification.NotificationContextFactory;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.jpa.email.dao.MailQueueDAO;
import org.exoplatform.commons.notification.impl.jpa.email.entity.MailQueueEntity;
import org.exoplatform.commons.notification.impl.service.SendEmailService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.mail.MailService;

@ManagedBy(SendEmailService.class)
public class JPAQueueMessageImpl implements QueueMessage, Startable {
  private static final Log    LOG                 = ExoLogger.getExoLogger(JPAQueueMessageImpl.class);

  private static final String MAX_TO_SEND_SYS_KEY = "conf.notification.service.QueueMessage.numberOfMailPerBatch";

  private static final String MAX_TO_SEND_KEY     = "numberOfMailPerBatch";

  private static final int    MAX_TO_SEND_DEFAULT = 20;

  private boolean             enabled             = true;

  private int                 maxToSend;

  private MailService         mailService;

  private MailQueueDAO        mailQueueDAO;

  private ListenerService     listenerService;

  public JPAQueueMessageImpl(MailService mailService,
                             MailQueueDAO mailQueueDAO,
                             ListenerService listenerService,
                             DataInitializer dataInitializer,
                             InitParams params) {
    this.mailService = mailService;
    this.mailQueueDAO = mailQueueDAO;
    this.listenerService = listenerService;

    maxToSend = NotificationUtils.getSystemValue(params, MAX_TO_SEND_SYS_KEY, MAX_TO_SEND_KEY, MAX_TO_SEND_DEFAULT);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void enable(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @ExoTransactional
  public boolean put(MessageInfo message) throws Exception {
    if (message == null || message.getTo() == null || message.getTo().length() == 0) {
      return false;
    }
    //
    if (NotificationUtils.isValidEmailAddresses(message.getTo()) == false) {
      LOG.warn(String.format("The email %s is not valid for sending notification", message.getTo()));
      return false;
    }
    //

    saveMessageInfo(message);
    //
    listenerService.broadcast(new Event<QueueMessage, String>(MESSAGE_SENT_FROM_QUEUE, this, message.getId()));
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @ExoTransactional
  public void send() {
    final boolean statsEnabled = NotificationContextFactory.getInstance().getStatistics().isStatisticsEnabled();
    int messagesSize = 0;
    //
    Set<MessageInfo> messages = load();
    //
    int originalMessagesSize = messages.size();
    messagesSize = originalMessagesSize;
    if (messagesSize > 0) {
      LOG.info(messagesSize + " message(s) will be sent.");
    }

    for (MessageInfo messageInfo : messages) {
      if (messageInfo == null) {
        continue;
      }
      try {
        if (sendMessage(messageInfo)) {
          LOG.debug("Mail message '{}' sent to user: {}", messageInfo.getId(), messageInfo.getTo());
          removeMessageInfo(messageInfo.getId());
          LOG.debug("Mail message '{}' removed from queue, to user: {}", messageInfo.getId(), messageInfo.getTo());
          //
          if (statsEnabled) {
            NotificationContextFactory.getInstance().getStatisticsCollector().pollQueue(messageInfo.getPluginId());
          }
        }
      } catch (Exception e) {
        messagesSize--;
        LOG.error("Error sending message from = '" + messageInfo.getFrom() + "', to = '" + messageInfo.getTo() + "', id = '"
            + messageInfo.getId() + "'", e);
      }
    }
    if (originalMessagesSize > 0) {
      LOG.info("{}/{} message(s) are loaded, sent and deleted from queue.", messagesSize, originalMessagesSize);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean sendMessage(MessageInfo message) throws Exception {
    if (message == null) {
      throw new IllegalArgumentException("Message is null");
    }
    if (this.enabled) {
      try {
        // ensure the message is valid
        if (message.getFrom() == null) {
          return false;
        }
        mailService.sendMessage(message.makeEmailNotification());
        return true;
      } catch (Exception e) {
        LOG.error("Error while sending a message - Cause : " + e.getMessage(), e);
        return false;
      }
    }
    //
    listenerService.broadcast(new Event<QueueMessage, String>(MESSAGE_SENT_FROM_QUEUE, this, message.getId()));
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeAll() {
    //
    LOG.debug("Removing messages: ");
    mailQueueDAO.deleteAll();
    LOG.debug("Done to removed messages! ");
  }

  private void saveMessageInfo(MessageInfo message) {
    try {
      MailQueueEntity mailQueueEntity = new MailQueueEntity();
      mailQueueEntity.setType(message.getPluginId());
      mailQueueEntity.setFrom(message.getFrom());
      mailQueueEntity.setTo(message.getTo());
      mailQueueEntity.setSubject(message.getSubject());
      mailQueueEntity.setBody(message.getBody());
      mailQueueEntity.setFooter(message.getFooter());
      mailQueueEntity.setCreationDate(Calendar.getInstance());

      mailQueueDAO.create(mailQueueEntity);

    } catch (Exception e) {
      LOG.error("Failed to save message.", e.getMessage() + message.toJSON(), e);
    }
  }

  private Set<MessageInfo> load() {
    Set<MessageInfo> messages = new HashSet<>();
    for (MailQueueEntity mailQueueEntity : mailQueueDAO.findAll(0, maxToSend)) {
      try {
        messages.add(convertQueueEntityToMessageInfo(mailQueueEntity));
      } catch (Exception e) {
        LOG.error("Failed to load message with id = " + mailQueueEntity.getId(), e);
      }
    }
    return messages;
  }

  private void removeMessageInfo(String id) {
    try {
      LOG.debug("Removing messageId: " + id);
      mailQueueDAO.delete(mailQueueDAO.find(Long.parseLong(id)));
      //
      listenerService.broadcast(new Event<QueueMessage, String>(MESSAGE_DELETED_FROM_QUEUE, this, id));
    } catch (Exception e) {
      LOG.warn("Failed to remove message: " + e.getMessage(), e);
    }
  }

}
