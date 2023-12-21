package org.exoplatform.commons.comparators;

import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;

public class NaturalComparator implements Comparator<String> {
  @Override
  public int compare(String s1, String s2) {
      /*
      Split string by matching a position where a non-digit (\\D) precedes and a digit (\\d) follows or vice versa
      (?<=) matches the position immediately following a non-digit (\\D) or a digit (\\d) character
      (?=) matches the position immediately preceding a non-digit (\\D) or a digit (\\d) character
       */
    if(s1 == null) {
      return 1;
    }
    if(s2 == null) {
      return -1;
    }
    String[] arr1 = s1.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
    String[] arr2 = s2.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
    int i = 0;
    while (i < arr1.length && i < arr2.length) {
      if (StringUtils.isNotBlank(arr1[i]) && Character.isDigit(arr1[i].charAt(0)) && StringUtils.isNotBlank(arr2[i]) && Character.isDigit(arr2[i].charAt(0))) {
        int num1 = Integer.parseInt(arr1[i]);
        int num2 = Integer.parseInt(arr2[i]);
        if (num1 != num2) {
          return Integer.compare(num1, num2);
        }
      } else {
        int result = arr1[i].compareToIgnoreCase(arr2[i]);
        if (result != 0) {
          return result;
        }
      }
      i++;
    }
    return s1.compareToIgnoreCase(s2);
  }
}
