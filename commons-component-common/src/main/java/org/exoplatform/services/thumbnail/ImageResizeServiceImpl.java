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

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageResizeServiceImpl implements ImageResizeService {

  @Override
  public byte[] scaleImage(byte[] image, int width, int height, boolean fitExact, boolean ultraQuality) throws Exception {
    if (width == 0 && height == 0) {
      return image;
    }
    Scalr.Method resizeMethod = ultraQuality ? Scalr.Method.ULTRA_QUALITY : Scalr.Method.QUALITY;
    BufferedImage bufferedImage = toBufferedImage(image);
    int originWidth = bufferedImage.getWidth();
    int originHeight = bufferedImage.getHeight();

    if (width>originWidth || height>originHeight) {
      //we don't want to increase image size, so return original image
      return image;
    }


    if (width == 0) {
      bufferedImage = Scalr.resize(bufferedImage, resizeMethod, Scalr.Mode.FIT_TO_HEIGHT, width, height, Scalr.OP_ANTIALIAS);
    } else if (height == 0) {
      bufferedImage = Scalr.resize(bufferedImage, resizeMethod, Scalr.Mode.FIT_TO_WIDTH, width, height, Scalr.OP_ANTIALIAS);
    } else if (fitExact) {
      bufferedImage = Scalr.resize(bufferedImage, resizeMethod, Scalr.Mode.FIT_EXACT, width, height, Scalr.OP_ANTIALIAS);
    } else {

      Scalr.Mode fitMode = width > height ? Scalr.Mode.FIT_TO_WIDTH : Scalr.Mode.FIT_TO_HEIGHT;
      bufferedImage = Scalr.resize(bufferedImage, resizeMethod, fitMode, width, height, Scalr.OP_ANTIALIAS);
    }

    byte[] response = toByteArray(bufferedImage);
    if (response.length > image.length) {
      //if the original image is smaller in weight from the resized image, we must keep the original image
      return image;
    } else {
      return response;
    }
  }

  private BufferedImage toBufferedImage(byte[] imageBytes) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
    return ImageIO.read(bis);
  }

  private byte[] toByteArray(BufferedImage bufferedImage) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
    return byteArrayOutputStream.toByteArray();
  }

}
