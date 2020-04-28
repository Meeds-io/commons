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
package org.exoplatform.commons.api.notification.template;

import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;

public interface Element {
  /**
   * Gets the language what belongs to the template
   * @return
   */
  String getLanguage();
  
  /**
   * Accept the visitor to visit the element 
   * @param visitor
   * @return
   */
  ElementVisitor accept(ElementVisitor visitor);
  
  /**
   * Gets the template of specified element
   * @return
   */
  String getTemplate();

  /**
   * Assigns the language to 
   * @param language
   * @return
   */
  Element language(String language);
  
  /**
   * Assigns the template to the element
   * @param template
   * @return
   */
  Element template(String template);
  
  /**
   * Gets template configure for the element
   * uses it in the case when we need to get the groovy template.
   * @return
   */
  PluginConfig getPluginConfig();
  
  /**
   * Sets the template configure for the element 
   * @param templateConfig
   * @return
   */
  Element config(PluginConfig templateConfig);

  /**
   * Set the value isNewLine for the case digest
   * @param needNewLine
   * @return
   */
  Element addNewLine(boolean needNewLine);

  /**
   * Get the value of isNewLine
   * @return
   */
  boolean isNewLine();
}