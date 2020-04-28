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
package org.exoplatform.commons.notification.template;

import org.exoplatform.commons.api.notification.template.Element;
import org.exoplatform.commons.api.notification.template.ElementVisitor;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 1, 2013  
 */

public class DigestTemplate extends SimpleElement {
  
  public enum ElementType {
    DIGEST_ONE(1),
    DIGEST_THREE(3),
    DIGEST_MORE(4);
    
    private int value = 0;
    
    ElementType(int value) {
      this.value = value;
    }
    
    public int getValue() {
      return this.value;
    }
   
  }
  
  private final Element digestOne;

  private final Element digestThree;

  private final Element digestMore;
  
  public DigestTemplate() {
    
    digestOne = new SimpleElement().addNewLine(false);
    digestThree = new SimpleElement().addNewLine(false);
    digestMore = new SimpleElement().addNewLine(false);
  }
 
  
  /**
   * Sets the digestOne
   * @param template
   * @return
   */
  public DigestTemplate digestOne(String template) {
    digestOne.template(template);
    return this;
  }
  
  /**
   * Sets the digestThree
   * @param template
   * @return
   */
  public DigestTemplate digestThree(String template) {
    digestThree.template(template);
    return this;
  }
  
  /**
   * Sets the digestThree
   * @param template
   * @return
   */
  public DigestTemplate digestMore(String template) {
    digestMore.template(template);
    return this;
  }
  
  @Override
  public ElementVisitor accept(ElementVisitor visitor) {
    if (visitor.getTemplateContext().getDigestSize() == ElementType.DIGEST_ONE.getValue()) {
      visitor.visit(this.digestOne);
    } else if (visitor.getTemplateContext().getDigestSize() <= ElementType.DIGEST_THREE.getValue()) {
      visitor.visit(this.digestThree);
    } else if (visitor.getTemplateContext().getDigestSize() >= ElementType.DIGEST_MORE.getValue()) {
      visitor.visit(this.digestMore);
    }
    
    return visitor;
  }

}
