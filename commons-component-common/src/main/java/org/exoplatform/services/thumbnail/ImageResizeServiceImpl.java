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
