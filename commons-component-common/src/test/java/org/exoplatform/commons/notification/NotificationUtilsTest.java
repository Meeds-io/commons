/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.notification;

import java.util.Locale;

import junit.framework.TestCase;

public class NotificationUtilsTest extends TestCase {

  public void testGetLocale() {
    String language = null;
    Locale actual = NotificationUtils.getLocale(language);
    assertEquals(Locale.ENGLISH, actual); // NOSONAR

    language = "";
    actual = NotificationUtils.getLocale(language);
    assertEquals(Locale.ENGLISH, actual);

    language = "fr";
    actual = NotificationUtils.getLocale(language);
    assertEquals(Locale.FRENCH, actual);

    language = "pt_BR";
    actual = NotificationUtils.getLocale(language);
    assertEquals(new Locale("pt", "BR"), actual);

    language = "pt_BR_BR";
    actual = NotificationUtils.getLocale(language);
    assertEquals(new Locale("pt", "BR", "BR"), actual);
  }

  public void testIsValidNotificationSenderName() {
    String senderName = "";
    // senderName is empty
    assertFalse(NotificationUtils.isValidNotificationSenderName(senderName));
    // senderName contains only spaces
    senderName = "    ";
    assertFalse(NotificationUtils.isValidNotificationSenderName(senderName));
    // senderName starts with space(s) and contain letters and numbers
    senderName = "    test123456";
    assertTrue(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "test12, 34 56; test";
    assertFalse(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "testéé12, 34 56; test";
    assertFalse(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "testéé12@test";
    assertFalse(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "testéé12>test";
    assertFalse(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "testéé12#test";
    assertFalse(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "testéé12$test";
    assertFalse(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "testéé12-_test";
    assertTrue(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "123456";
    assertTrue(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "123 test 456";
    assertTrue(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "123 45 6";
    assertTrue(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "test123456";
    assertTrue(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "test12 34 56-test";
    assertTrue(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "test";
    assertTrue(NotificationUtils.isValidNotificationSenderName(senderName));
    senderName = "test  test";
    assertTrue(NotificationUtils.isValidNotificationSenderName(senderName));
  }

  public void testIsValidEmailAddresses() {
    String emails = "";
    // email is empty
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    // email only text not @
    emails = "test";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));
    // email have @ but not '.'
    emails = "test@test";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));
    // email have charter strange
    emails = "#%^&test@test.com";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));
    // email have before '.' is number
    emails = "test@test.787";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));

    emails = "no reply aaa@xyz.com, demo+aaa@demo.com, ";
    assertEquals(false, NotificationUtils.isValidEmailAddresses(emails));

    // email contains printed characters
    emails = "test.ABC@demo.COM";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));

    // email contains printed characters, local part has 2 dots
    emails = "test.ABC.def@demo2test.vn";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));

    // basic case
    emails = "test@test.com";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test@test.com.vn";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test@test.com, demo@demo.com, ";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test@test.com ,  demo@demo.com, ";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "test+test@test.com, demo+aaa@demo.com, ";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "Justin Dickey<justin.dickey@apprentice.university>";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
    emails = "eXo Admin<team-cwi@exoplatform.com>";
    assertEquals(true, NotificationUtils.isValidEmailAddresses(emails));
  }

  public void testProcessLinkInActivityTitle() {
    String title = "<a href=\"www.yahoo.com\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\">Hotmail Site</a>";
    title = NotificationUtils.processLinkTitle(title);
    assertEquals("<a href=\"www.yahoo.com\" style=\"color: #2f5e92; text-decoration: none;\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\" style=\"color: #2f5e92; text-decoration: none;\">Hotmail Site</a>",
                 title);
  }

}
