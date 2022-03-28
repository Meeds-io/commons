package org.exoplatform.commons.version.util;

import junit.framework.TestCase;

public class VersionComparatorTest extends TestCase {

  public void testIsBefore() {
    assertTrue(VersionComparator.isBefore("2.1", "2.2"));
    assertFalse(VersionComparator.isBefore("2.2", "2.1"));
    assertFalse(VersionComparator.isBefore("2.1", "2.1"));
    assertFalse(VersionComparator.isBefore("2.1", ""));
    assertTrue(VersionComparator.isBefore("", "2.2"));
    assertTrue(VersionComparator.isBefore("5.0.0-M32", "5.0-RC1"));
    assertTrue(VersionComparator.isBefore("6.2.0-20210529", "6.2.0-20210531"));
    assertFalse(VersionComparator.isBefore("6.2.0-20210601", "6.2.0-20210531"));
    assertTrue(VersionComparator.isBefore("6.1.1", "6.2.0-20210531"));
    assertFalse(VersionComparator.isBefore("6.2.0", "6.2.0-20210531"));
    assertTrue(VersionComparator.isBefore("6.2.0-20210529", "6.2.0-20210531"));
    assertFalse(VersionComparator.isBefore("6.2.0-20210601", "6.2.0-20210531"));
    assertTrue(VersionComparator.isBefore("6.2.1-20210529", "6.2.1-20210531"));
    assertFalse(VersionComparator.isBefore("6.2.1-20210601", "6.2.1-20210531"));
    assertTrue(VersionComparator.isBefore("6.1.0", "6.2.0-20210531"));
    assertTrue(VersionComparator.isBefore("6.1.1", "6.2.0-20210531"));
    assertFalse(VersionComparator.isBefore("6.2.0", "6.2.0-M20"));
    assertFalse(VersionComparator.isBefore("6.2.0", "6.2.0-20210531"));
    assertTrue(VersionComparator.isBefore("6.2.0-20210531", "6.2.0"));
    assertTrue(VersionComparator.isBefore("6.2.0-20210531", "6.2.1"));
    assertTrue(VersionComparator.isBefore("6.2.0-20210531", "6.2.0-m01"));
    assertTrue(VersionComparator.isBefore("6.2.0-20210531", "6.2.0-rc01"));
    assertTrue(VersionComparator.isBefore("6.2.0-rc02", "6.2.0-rc03"));
    assertTrue(VersionComparator.isBefore("6.2.0-m15", "6.2.0-rc03"));
  }

  public void testIsAfter() {
    assertTrue(VersionComparator.isAfter("5.0.0-GA", "5.0.0-M32"));
    assertTrue(VersionComparator.isAfter("2.2", "2.1"));
    assertTrue(VersionComparator.isAfter("4.0.0-relooking-SNAPSHOT", "2.3.10-SNAPSHOT"));
    assertFalse(VersionComparator.isAfter("2.1", "2.2"));
    assertFalse(VersionComparator.isAfter("2.1", "2.1"));
    assertFalse(VersionComparator.isAfter("", "2.1"));
    assertTrue(VersionComparator.isAfter("2.2", ""));
  }

  public void testIsSame() {
    assertTrue(VersionComparator.isSame("2.1", "2.1"));
    assertFalse(VersionComparator.isSame("2.1", "2.2"));
    assertFalse(VersionComparator.isSame("2.1", ""));
    assertFalse(VersionComparator.isSame("", "2.2"));
  }

  public void testSnapshots() {
    assertTrue(VersionComparator.isAfter("6.3.x-SNAPSHOT", "6.3.x-SNAPSHOT"));
    assertFalse(VersionComparator.isBefore("6.3.x-SNAPSHOT", "6.3.x-SNAPSHOT"));
    assertFalse(VersionComparator.isAfter("6.2.x-SNAPSHOT", "6.3.x-SNAPSHOT"));
    assertTrue(VersionComparator.isBefore("6.2.x-SNAPSHOT", "6.3.x-SNAPSHOT"));
    assertTrue(VersionComparator.isAfter("6.3.x-maintenance-SNAPSHOT", "6.3.x-maintenance-SNAPSHOT"));
    assertFalse(VersionComparator.isAfter("6.2.x-maintenance-SNAPSHOT", "6.3.x-maintenance-SNAPSHOT"));

    assertTrue(VersionComparator.isBefore("6.3.0", "6.3.x-SNAPSHOT"));
    assertTrue(VersionComparator.isBefore("6.3.1", "6.3.x-SNAPSHOT"));
  }
}
