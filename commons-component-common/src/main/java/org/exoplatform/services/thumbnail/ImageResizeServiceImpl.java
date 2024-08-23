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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.imgscalr.Scalr;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class ImageResizeServiceImpl implements ImageResizeService {

  private static final Log LOG = ExoLogger.getLogger(ImageResizeServiceImpl.class);


  @Override
  public byte[] scaleImage(byte[] image, int width, int height, boolean fitExact, boolean ultraQuality) throws Exception {
    if (width == 0 && height == 0) {
      return image;
    }
    Scalr.Method resizeMethod = ultraQuality ? Scalr.Method.ULTRA_QUALITY : Scalr.Method.QUALITY;
    BufferedImage bufferedImage = toBufferedImage(image);
    // Image could not be loaded using ImageIO API
    if(bufferedImage == null) {
      return image;
    }
    int originWidth = bufferedImage.getWidth();
    int originHeight = bufferedImage.getHeight();

    if (width>originWidth || height>originHeight) {
      //we don't want to increase image size, so return original image
      return image;
    }

    ImageReader imageReader = getImageReader(image);
    if (width == 0) {
      bufferedImage = Scalr.resize(bufferedImage, resizeMethod, Scalr.Mode.FIT_TO_HEIGHT, width, height, Scalr.OP_ANTIALIAS);
    } else if (height == 0) {
      bufferedImage = Scalr.resize(bufferedImage, resizeMethod, Scalr.Mode.FIT_TO_WIDTH, width, height, Scalr.OP_ANTIALIAS);
    } else if (fitExact) {
      bufferedImage = Scalr.resize(bufferedImage, resizeMethod, Scalr.Mode.FIT_EXACT, width, height, Scalr.OP_ANTIALIAS);
    } else {
      float ratio = (float)originHeight / (float)originWidth;
      int calculatedTargetHeight= Math.round(width * ratio);

      Scalr.Mode fitMode = width > height ? Scalr.Mode.FIT_TO_WIDTH : Scalr.Mode.FIT_TO_HEIGHT;
      if (calculatedTargetHeight < height) {
        fitMode=Scalr.Mode.FIT_TO_HEIGHT;
      }
      bufferedImage = Scalr.resize(bufferedImage, resizeMethod, fitMode, width, height, Scalr.OP_ANTIALIAS);
    }


    byte[] response = toByteArray(bufferedImage, imageReader);

    if (!fitExact && response.length > image.length) {
      // if the original image is smaller in weight from the resized image, we
      // must keep the original image
      return image;
    } else {
      return response;
    }
  }

  private ImageReader getImageReader(byte[] bytes) throws IOException {
    ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
    Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInputStream);
    ImageReader reader = readers.next();
    reader.setInput(imageInputStream);
    return reader;
  }

  private BufferedImage toBufferedImage(byte[] imageBytes) {
    try {
      ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
      return ImageIO.read(bis);
    } catch (Exception e) {
      LOG.error("Unable to read image",e);
      return null;
    }
  }

  private byte[] toByteArray(BufferedImage targetBufferedImage, ImageReader sourceImageReader) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageWriter writer = ImageIO.getImageWriter(sourceImageReader);
    writer.setOutput(ImageIO.createImageOutputStream(byteArrayOutputStream));

    IIOMetadata metadata = sourceImageReader.getImageMetadata(0);
    writer.write(new IIOImage(targetBufferedImage, null, metadata));
    writer.dispose();
    return byteArrayOutputStream.toByteArray();
  }

}
