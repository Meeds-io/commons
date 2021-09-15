/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.commons.api.notification.utils;

import java.util.Calendar;
import java.util.Locale;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS Author : Vu Duy Tu tu.duy@exoplatform.com Sep
 * 30, 2011
 */
public class TestTimeUtils extends TestCase {
  public TestTimeUtils() throws Exception {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    ExoContainerContext.setCurrentContainer(PortalContainer.getInstance());
  }

  public void testConvertDateTimeByCurrentDate() throws Exception {
    Calendar cal = Calendar.getInstance();
    long day = 24 * 60 * 60 * 1000;
    cal.setTimeInMillis(cal.getTimeInMillis() - 3 * (31 * day));
    assertEquals("3 months", TimeUtils.convertXTimeAgoByTimeServer(cal.getTime(), "EE, dd yyyy", Locale.ENGLISH, TimeUtils.YEAR));

    cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 9);
    assertEquals("1 week", TimeUtils.convertXTimeAgoByTimeServer(cal.getTime(), "EE, dd yyyy", Locale.ENGLISH, TimeUtils.YEAR));

    cal = Calendar.getInstance();
    cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) - 5);
    assertEquals("5 days", TimeUtils.convertXTimeAgoByTimeServer(cal.getTime(), "EE, dd yyyy", Locale.ENGLISH, TimeUtils.YEAR));

    cal = Calendar.getInstance();
    cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) - 1);
    assertEquals("1 hour", TimeUtils.convertXTimeAgoByTimeServer(cal.getTime(), "EE, dd yyyy", Locale.ENGLISH, TimeUtils.YEAR));

    cal = Calendar.getInstance();
    cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) - 25);
    assertEquals("25 minutes",
                 TimeUtils.convertXTimeAgoByTimeServer(cal.getTime(), "EE, dd yyyy", Locale.ENGLISH, TimeUtils.YEAR));

    cal = Calendar.getInstance();
    cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - 10);
    assertEquals("justnow", TimeUtils.convertXTimeAgoByTimeServer(cal.getTime(), "EE, dd yyyy", Locale.ENGLISH, TimeUtils.YEAR));
  }

  public void testConvertDateTime() throws Exception {
    long timeNow = TimeUtils.getGreenwichMeanTime().getTimeInMillis();
    Calendar calendar = Calendar.getInstance();
    String format = "M-d-yyyy";
    long day = 24 * 60 * 60 * 1000;
    // test for 1 year ago
    calendar.setTimeInMillis(timeNow - 366 * day);
    assertEquals("1 year", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    assertEquals(TimeUtils.getFormatDate(calendar.getTime(), format, Locale.getDefault()),
                 TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, TimeUtils.YEAR));
    // test for 2 years ago
    calendar.setTimeInMillis(timeNow - 2 * 366 * day);
    // set limit is year
    assertEquals("2 years", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));

    // test for 1 month ago
    calendar.setTimeInMillis(timeNow - 31 * day);
    assertEquals("1 month", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // set limit is year
    assertEquals("1 month", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, TimeUtils.YEAR));
    // test for 2 months ago
    calendar.setTimeInMillis(timeNow - (2 * 31 * day));
    assertEquals("2 months", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // set limit is month
    assertEquals(TimeUtils.getFormatDate(calendar.getTime(), format, Locale.getDefault()),
                 TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, TimeUtils.MONTH));

    // test for 1 week ago
    calendar.setTimeInMillis(timeNow - 7 * day);
    assertEquals("1 week", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // set limit is month
    assertEquals("1 week", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, TimeUtils.MONTH));
    // test for 2 weeks ago
    calendar.setTimeInMillis(timeNow - 2 * 7 * day);
    assertEquals("2 weeks", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));

    // test for 1 day ago
    calendar.setTimeInMillis(timeNow - day);
    assertEquals("1 day", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // test for 2 days ago
    calendar.setTimeInMillis(timeNow - 2 * day);
    assertEquals("2 days", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // set limit day
    assertEquals(TimeUtils.getFormatDate(calendar.getTime(), format, Locale.getDefault()),
                 TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, TimeUtils.DAY));

    // test for 1 hour ago
    calendar.setTimeInMillis(timeNow - 60 * 60 * 1000);
    assertEquals("1 hour", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // test for 2 hours ago
    calendar.setTimeInMillis(timeNow - 2 * 60 * 60 * 1000);
    assertEquals("2 hours", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // set limit day
    assertEquals("2 hours", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, TimeUtils.DAY));

    // test for 1 minute ago
    calendar.setTimeInMillis(timeNow - 60 * 1000);
    assertEquals("1 minute", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
    // test for 2 minute ago
    calendar.setTimeInMillis(timeNow - 2 * 60 * 1000);
    assertEquals("2 minutes", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));

    // test for less than 1 minute ago
    calendar.setTimeInMillis(timeNow - 40 * 1000);
    assertEquals("justnow", TimeUtils.convertXTimeAgo(calendar.getTime(), format, null, 0));
  }

  public void testGetResourceBundle() {
    // Can not test this function because: can not get resource bundle from test
    // case.
  }

  public void testGetLocale() {
    // Can not test this function because: can not get PortalRequestContext.
  }

  public void testGetFormatDate() {
    Locale locale = new Locale(Locale.ENGLISH.getLanguage(), Locale.UK.getCountry());
    Calendar calendar = Calendar.getInstance();
    // set date time: 28/08/2011 at 15h 30m
    calendar.set(2011, 07, 28, 15, 30);
    assertEquals("", TimeUtils.getFormatDate(null, "M-d-yyyy", locale));
    assertEquals("", TimeUtils.getFormatDate(calendar.getTime(), "", locale));

    assertEquals("8-28-2011", TimeUtils.getFormatDate(calendar.getTime(), "M-d-yyyy", locale));
    assertEquals("8-28-11", TimeUtils.getFormatDate(calendar.getTime(), "M-d-yy", locale));
    assertEquals("08-28-11", TimeUtils.getFormatDate(calendar.getTime(), "MM-dd-yy", locale));
    assertEquals("08-28-2011", TimeUtils.getFormatDate(calendar.getTime(), "MM-dd-yyyy", locale));
    assertEquals("2011-08-28", TimeUtils.getFormatDate(calendar.getTime(), "yyyy-MM-dd", locale));
    assertEquals("11-08-28", TimeUtils.getFormatDate(calendar.getTime(), "yy-MM-dd", locale));
    assertEquals("28-08-2011", TimeUtils.getFormatDate(calendar.getTime(), "dd-MM-yyyy", locale));
    assertEquals("28-08-11", TimeUtils.getFormatDate(calendar.getTime(), "dd-MM-yy", locale));
    assertEquals("8/28/2011", TimeUtils.getFormatDate(calendar.getTime(), "M/d/yyyy", locale));
    assertEquals("8/28/11", TimeUtils.getFormatDate(calendar.getTime(), "M/d/yy", locale));
    assertEquals("08/28/11", TimeUtils.getFormatDate(calendar.getTime(), "MM/dd/yy", locale));
    assertEquals("08/28/2011", TimeUtils.getFormatDate(calendar.getTime(), "MM/dd/yyyy", locale));
    assertEquals("2011/08/28", TimeUtils.getFormatDate(calendar.getTime(), "yyyy/MM/dd", locale));
    assertEquals("11/08/28", TimeUtils.getFormatDate(calendar.getTime(), "yy/MM/dd", locale));
    assertEquals("28/08/2011", TimeUtils.getFormatDate(calendar.getTime(), "dd/MM/yyyy", locale));
    assertEquals("28/08/11", TimeUtils.getFormatDate(calendar.getTime(), "dd/MM/yy", locale));

    assertEquals("Sun, August 28, 2011", TimeUtils.getFormatDate(calendar.getTime(), "EEE, MMMM dd, yyyy", locale));
    assertEquals("Sunday, August 28, 2011", TimeUtils.getFormatDate(calendar.getTime(), "EEEE, MMMM dd, yyyy", locale));
    assertEquals("Sunday, 28 August, 2011", TimeUtils.getFormatDate(calendar.getTime(), "EEEE, dd MMMM, yyyy", locale));
    assertEquals("Sun, Aug 28, 2011", TimeUtils.getFormatDate(calendar.getTime(), "EEE, MMM dd, yyyy", locale));
    assertEquals("Sunday, Aug 28, 2011", TimeUtils.getFormatDate(calendar.getTime(), "EEEE, MMM dd, yyyy", locale));
    assertEquals("Sunday, 28 Aug, 2011", TimeUtils.getFormatDate(calendar.getTime(), "EEEE, dd MMM, yyyy", locale));
    assertEquals("August 28, 2011", TimeUtils.getFormatDate(calendar.getTime(), "MMMM dd, yyyy", locale));
    assertEquals("28 August, 2011", TimeUtils.getFormatDate(calendar.getTime(), "dd MMMM, yyyy", locale));
    assertEquals("Aug 28, 2011", TimeUtils.getFormatDate(calendar.getTime(), "MMM dd, yyyy", locale));
    assertEquals("28 Aug, 2011", TimeUtils.getFormatDate(calendar.getTime(), "dd MMM, yyyy", locale));

    assertEquals("28 Aug, 2011, 03:30 PM".toLowerCase(),
                 TimeUtils.getFormatDate(calendar.getTime(), "dd MMM, yyyy, hh:mm a", locale).toLowerCase());
    assertEquals("28 Aug, 2011, 15:30", TimeUtils.getFormatDate(calendar.getTime(), "dd MMM, yyyy, HH:mm", locale));
  }

  public void testValidJavaVariable() {
    String input = "123 sdkjh s;:sdlkjh d";
    assertEquals("_123_sdkjh_s__sdlkjh_d", TimeUtils.santializeJavaVariable(input));
  }

}
