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
package org.exoplatform.services.bench;

/**
 * Simple lorem ipsum text generator.
 * <p>
 * Suitable for creating sample data for test cases and performance tests.
 * </p>
 * 
 */

public class LoremIpsum4J {

  public static final String LOREM_IPSUM = new StringBuilder("Lorem ipsum dolor sit amet, consetetur sadipscing elitr, ")
                                                 .append("sed diam nonumy eirmod tempor invidunt ut labore ")
                                                 .append("et dolore magna aliquyam erat, sed diam voluptua.")
                                                 .append("At vero eos et accusam et justo duo dolores et ea rebum.")
                                                 .append("Stet clita kasd gubergren, ")
                                                 .append("no sea takimata sanctus est Lorem ipsum dolor sit amet.")
                                                 .toString();

  private String[]           loremIpsumWords;

  public LoremIpsum4J() {

    this.loremIpsumWords = LOREM_IPSUM.split("\\s");

  }

  /**
   * Returns one sentence (50 words) of the lorem ipsum text.
   * 
   * @return 50 words of lorem ipsum text
   */

  public String getWords() {

    return getWords(50);

  }

  /**
   * Returns words from the lorem ipsum text.
   * 
   * @param amount Amount of words
   * @return Lorem ipsum text
   */

  public String getWords(int amount) {

    return getWords(amount, 0);

  }

  /**
   * Returns words from the lorem ipsum text.
   * 
   * @param amount Amount of words
   * @param startIndex Start index of word to begin with {@literal (must be >= 0 and < 50)}
   * @return Lorem ipsum text
   * @throws IndexOutOfBoundsException {@literal If startIndex is < 0 or > 49}
   */

  public String getWords(int amount, int startIndex) {

    if (startIndex < 0 || startIndex > 49) {

      throw new IndexOutOfBoundsException("startIndex must be >= 0 and < 50");

    }

    int word = startIndex;

    StringBuilder lorem = new StringBuilder();

    for (int i = 0; i < amount; i++) {

      if (word == 50) {

        word = 0;

      }

      lorem.append(loremIpsumWords[word]);

      if (i < amount - 1) {

        lorem.append(' ');

      }

      word++;

    }

    return lorem.toString();

  }

  /**
   * Returns two paragraphs of lorem ipsum.
   * 
   * @return Lorem ipsum paragraphs
   */

  public String getParagraphs() {

    return getParagraphs(2);

  }

  /**
   * Returns paragraphs of lorem ipsum.
   * 
   * @param amount Amount of paragraphs
   * @return Lorem ipsum paragraphs
   */

  public String getParagraphs(int amount) {

    StringBuilder lorem = new StringBuilder();

    for (int i = 0; i < amount; i++) {

      lorem.append(LOREM_IPSUM);

      if (i < amount - 1) {

        lorem.append("\n\n");

      }

    }

    return lorem.toString();

  }

}
