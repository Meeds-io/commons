package org.exoplatform.services.thumbnail;

public interface ImageResizeService {


    /**
     * Create a scaled image by resizing an initial given image
     *
     * @param image Target image content to be resized
     * @param width Target resized image width
     * @param height Target resized image height
     * @param fitExact Fit resized image to the exact given with and height (true) or automatic fit (false)
     * @param ultraQuality return an ultra quality resized image (may take more execution time)
     * @return byte array of image content
     * @throws Exception
     */
    byte[] scaleImage(byte[] image, int width, int height, boolean fitExact, boolean ultraQuality) throws Exception;

}
