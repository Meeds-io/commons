/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.commons.embedder;

/**
 * @since 4.0.0-GA  
 */
public class ExoMedia {
  
  private String title;
  private String type;
  private String url;
  private String description;
  // html embed code
  private String html;
  // name of provider: youtube, vimeo,...
  private String provider;
  private String thumbnailUrl;
  private String thumbnailWidth;
  private String thumbnailHeight;
  
  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setHtml(String html) {
    this.html = html;
  }

  public String getHtml() {
    return html;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public String getProvider() {
    return provider;
  }
  /**
   * @return the thumbnailUrl
   */
  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  /**
   * @param thumbnailUrl the thumbnailUrl to set
   */
  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  /**
   * @return the thumbnailWidth
   */
  public String getThumbnailWidth() {
    return thumbnailWidth;
  }

  /**
   * @param thumbnailWidth the thumbnailWidth to set
   */
  public void setThumbnailWidth(String thumbnailWidth) {
    this.thumbnailWidth = thumbnailWidth;
  }

  /**
   * @return the thumbnailHeight
   */
  public String getThumbnailHeight() {
    return thumbnailHeight;
  }

  /**
   * @param thumbnailHeight the thumbnailHeight to set
   */
  public void setThumbnailHeight(String thumbnailHeight) {
    this.thumbnailHeight = thumbnailHeight;
  }
}
