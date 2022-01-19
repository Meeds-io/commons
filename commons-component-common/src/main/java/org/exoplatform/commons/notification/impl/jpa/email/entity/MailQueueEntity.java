package org.exoplatform.commons.notification.impl.jpa.email.entity;

import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Calendar;

@Entity(name = "NotificationsMailQueueEntity")
@ExoEntity
@Table(name = "EMAIL_QUEUE")
@NamedQueries({
    @NamedQuery(name = "NotificationsMailQueueEntity.getMessagesInQueue", query = "SELECT m FROM NotificationsMailQueueEntity m " +
        "ORDER BY m.creationDate ASC "),
})
public class MailQueueEntity {
  @Id
  @Column(name = "EMAIL_ID")
  @SequenceGenerator(name="SEQ_NTF_EMAIL_QUEUE", sequenceName="SEQ_NTF_EMAIL_QUEUE", allocationSize = 1)
  @GeneratedValue(strategy= GenerationType.AUTO, generator="SEQ_NTF_EMAIL_QUEUE")
  private long id;

  @Column(name = "CREATION_DATE")
  private Calendar creationDate;

  @Column(name = "TYPE")
  private String type;

  @Column(name = "SENDER")
  private String from;

  @Column(name = "RECEIVER")
  private String to;

  @Column(name = "SUBJECT")
  private String subject;

  @Column(name = "BODY")
  private String body;

  @Column(name = "FOOTER")
  private String footer;

  public long getId() {
    return id;
  }

  public Calendar getCreationDate() {
    return creationDate;
  }

  public MailQueueEntity setCreationDate(Calendar creationDate) {
    this.creationDate = creationDate;
    return this;
  }

  public String getType() {
    return type;
  }

  public MailQueueEntity setType(String type) {
    this.type = type;
    return this;
  }

  public String getFrom() {
    return from;
  }

  public MailQueueEntity setFrom(String from) {
    this.from = from;
    return this;
  }

  public String getTo() {
    return to;
  }

  public MailQueueEntity setTo(String to) {
    this.to = to;
    return this;
  }

  public String getSubject() {
    return subject;
  }

  public MailQueueEntity setSubject(String subject) {
    this.subject = subject;
    return this;
  }

  public String getBody() {
    return body;
  }

  public MailQueueEntity setBody(String body) {
    this.body = body;
    return this;
  }

  public String getFooter() {
    return footer;
  }

  public MailQueueEntity setFooter(String footer) {
    this.footer = footer;
    return this;
  }

}
