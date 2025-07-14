/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.commons.comparators;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class NaturalComparatorTest extends TestCase {

  @Test
  public void testNaturalCompare(){
    List<String> list = new ArrayList<>();
    list.add("1");
    list.add("3");
    list.add("2");

    list.sort(new NaturalComparator());
    //assert numeric sort
    assertEquals("1", list.get(0));
    assertEquals("3", list.get(2));

    list.add("Afile");
    list.add("bfile");
    list.sort(new NaturalComparator());
    //assert numeric sort then literal sort
    assertEquals("1", list.get(0));
    assertEquals("Afile", list.get(3));

    list.add("file1");
    list.add("file10");
    list.add("file2");
    list.add("file20");
    list.add("file3");
    list.add("2 test");

    list.sort(new NaturalComparator());
    String[] expectedSortedArray = new String[]{"1", "2", "2 test", "3", "Afile", "bfile", "file1", "file2", "file3" ,"file10", "file20"};
    for(int i = 0; i < list.size(); i++) {
      assertEquals(expectedSortedArray[i], list.get(i));
    }

  }

}
