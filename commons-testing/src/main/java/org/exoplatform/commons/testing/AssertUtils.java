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
package org.exoplatform.commons.testing;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

/**
 * A collection of assertion helper to make UT easier to read
 * 
 * @author patricelamarque
 */
public class AssertUtils {

  private AssertUtils() {
    // hidden
  }

  /**
   * Assert a set of expected items to be all contained in a collection
   * 
   * @param actual containment
   * @param expected items expected to be contained
   */
  public static <T> void assertContains(Collection<T> actual, T... expected) {

    for (T item : expected) {
      boolean found = false;
      for (T obj : actual) {
        if (obj.equals(item)) {
          found = true;
        }
      }
      Assert.assertTrue("expected item was not found " + item + "@" + item.hashCode(), found);
    }
  }

  /**
   * Assert a set of expected items NOT to be all contained in a collection
   * 
   * @param actual containment
   * @param expected items expected to be contained
   */
  public static <T> void assertNotContains(Collection<T> actual, T... expected) {
    Assert.assertFalse(actual.containsAll(Arrays.asList(expected)));
  }

  /**
   * Assert a set of expected string items to be all contained in a collection
   * 
   * @param actual containment
   * @param expected items expected to be contained
   */
  public static void assertContains(List<String> actual, String... expected) {

    for (String item : expected) {
      boolean found = false;
      for (String obj : actual) {
        if (obj.equals(item)) {
          found = true;
        }
      }
      Assert.assertTrue("expected item was not found " + item + "@" + item.hashCode(), found);
    }

  }

  /**
   * Assert a set of expected string items to be all contained in a string array
   * 
   * @param actual containment
   * @param expected items expected to be contained
   */
  public static void assertContains(String[] actual, String... expected) {
    Assert.assertTrue(Arrays.asList(actual).containsAll(Arrays.asList(expected)));
  }

  /**
   * Assert a set of expected string items NOT to be all contained in a
   * collection
   * 
   * @param actual containment
   * @param expected items expected to be contained
   */
  public static void assertNotContains(List<String> actual, String... expected) {
    Assert.assertFalse(actual.containsAll(Arrays.asList(expected)));
  }

  /**
   * Assert a collection is empty (not null)
   * 
   * @param value
   */
  @SuppressWarnings(value = "unchecked")
  public static void assertEmpty(Collection value) {
    Assert.assertNotNull(value);
    Assert.assertEquals(0, value.size());
  }

  /**
   * Assert a collection is not empty and not null
   * 
   * @param <T>
   * @param value
   */
  public static <T> void assertNotEmpty(Collection<T> value) {
    Assert.assertNotNull(value);
    Assert.assertTrue(value.size() > 0);
  }

  public static <T> void assertEmpty(T[] value) {
    Assert.assertNotNull(value);
    Assert.assertEquals(0, value.length);
  }

  /**
   * All elements of a list should be contained in the expected array of String
   * 
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertContainsAll(String message, List<String> expected, List<String> actual) {
    Assert.assertEquals(message, expected.size(), actual.size());
    Assert.assertTrue(message, expected.containsAll(actual));
  }

  /**
   * Assertion method on string arrays
   * 
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertEquals(String message, String[] expected, String[] actual) {
    Assert.assertEquals(message, expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals(message, expected[i], actual[i]);
    }
  }

  public static void assertException(Closure code) {
    try {
      code.dothis();
    } catch (Exception e) { //we want to catch all exceptions here
      return;// Exception correctly thrown
    }
    throw new AssertionFailedError("An exception should have been thrown.");
  }

  /**
   * Assert an exception of a given type is thrown by he code in closure
   * 
   * @param exceptionType
   * @param code
   */
  public static void assertException(Class<? extends Exception> exceptionType, Closure code) {
    try {
      code.dothis();
    } catch (Exception e) {  //we want to catch all exceptions here
      Assert.assertEquals("Wrong exception type", exceptionType, e.getClass());
      return;// Exception correctly thrown
    }
    throw new AssertionFailedError("An exception should have been thrown.");
  }

}
