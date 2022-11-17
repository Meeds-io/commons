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
