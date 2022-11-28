/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2022 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.services.thumbnail;

import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class ImageResizeServiceImplTest {

  private ImageResizeService imageResizeService;

  private byte[]             testImageContent;

  @Before
  public void setUp() throws Exception {
    this.imageResizeService = new ImageResizeServiceImpl();
    File file = new File(getClass().getClassLoader().getResource("images/testresize.jpg").getFile());
    testImageContent = Files.readAllBytes(file.toPath());
  }

  @Test
  public void scaleImage() throws Exception {
    byte[] resizedImage = imageResizeService.scaleImage(testImageContent, 0, 0, false, false);
    BufferedImage bufferedImage = toBufferedImage(resizedImage);
    assertTrue(resizedImage.length > 0);
    assertEquals(427, bufferedImage.getHeight());
    assertEquals(640, bufferedImage.getWidth());

    resizedImage = imageResizeService.scaleImage(testImageContent, 15, 15, false, false);
    assertTrue(resizedImage.length > 0);
    bufferedImage = toBufferedImage(resizedImage);
    assertEquals(15, bufferedImage.getHeight());
    assertNotEquals(15, bufferedImage.getWidth());
    assertTrue(bufferedImage.getWidth() > bufferedImage.getHeight());


    resizedImage = imageResizeService.scaleImage(testImageContent, 15, 15, true, true);
    assertTrue(resizedImage.length > 0);
    bufferedImage = toBufferedImage(resizedImage);
    assertEquals(15, bufferedImage.getHeight());
    assertEquals(15, bufferedImage.getWidth());

    resizedImage = imageResizeService.scaleImage(testImageContent, 0, 15, true, false);
    assertTrue(resizedImage.length > 0);
    bufferedImage = toBufferedImage(resizedImage);
    assertEquals(15, bufferedImage.getHeight());
    assertNotEquals(15, bufferedImage.getWidth());


    resizedImage = imageResizeService.scaleImage(testImageContent, 15, 0, true, false);
    assertTrue(resizedImage.length > 0);
    bufferedImage = toBufferedImage(resizedImage);
    assertEquals(15, bufferedImage.getWidth());
    assertNotEquals(15, bufferedImage.getHeight());

    resizedImage = imageResizeService.scaleImage(testImageContent, 15, 0, true, false);
    assertTrue(resizedImage.length > 0);
    bufferedImage = toBufferedImage(resizedImage);
    assertEquals(15, bufferedImage.getWidth());
    assertNotEquals(15, bufferedImage.getHeight());
  }

  private BufferedImage toBufferedImage(byte[] content) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(content);
    return ImageIO.read(bis);
  }
}
