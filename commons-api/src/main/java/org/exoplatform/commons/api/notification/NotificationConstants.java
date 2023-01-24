package org.exoplatform.commons.api.notification;

import java.util.regex.Pattern;

public class NotificationConstants {

  public static final String  BRANDING_PORTAL_NAME              = "exo:brandingPortalName";

  public static final String  BRANDING_COMPANY_NAME_SETTING_KEY = "exo.branding.company.name";

  public static final Pattern EMAIL_PATTERN                     =
                                            Pattern.compile("^[_a-zA-Z0-9-+]+(\\.[_a-zA-Z0-9-]+)*@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*(\\.[a-zA-Z]{2,})$");

  public static final String  EXO_IS_ACTIVE                     = "exo:isActive";

  public static final String  DEFAULT_SUBJECT_KEY               = "Notification.subject.{0}";

  public static final String  DEFAULT_SIMPLE_DIGEST_KEY         = "Notification.digest.{0}";

  public static final String  DEFAULT_DIGEST_ONE_KEY            = "Notification.digest.one.{0}";

  public static final String  DEFAULT_DIGEST_THREE_KEY          = "Notification.digest.three.{0}";

  public static final String  DEFAULT_DIGEST_MORE_KEY           = "Notification.digest.more.{0}";

  public static final String  FEATURE_NAME                      = "notification";

  public static final Pattern LINK_PATTERN                      = Pattern.compile("<a ([^>]+)>([^<]+)</a>");

  public static final Pattern NOTIFICATION_SENDER_NAME_PATTERN  = Pattern.compile("^[a-zA-Z]+[a-zA-Z ]*$");

  public static final String  styleCSS                          = " style=\"color: #2f5e92; text-decoration: none;\"";

  /**
   * This value must be the same with
   * CalendarSpaceActivityPublisher.CALENDAR_APP_ID
   */
  public static final String  CALENDAR_ACTIVITY                 = "cs-calendar:spaces";
}
