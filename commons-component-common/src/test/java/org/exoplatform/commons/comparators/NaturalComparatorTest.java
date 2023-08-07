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
