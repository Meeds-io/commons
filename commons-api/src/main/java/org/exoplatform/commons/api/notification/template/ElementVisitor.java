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

import java.io.Writer;

import org.exoplatform.commons.api.notification.service.template.TemplateContext;

public interface ElementVisitor {
  /**
   * To visit the element and generate
   * @param element
   * @return
   */
	ElementVisitor visit(Element element);
	
	/**
	 * Gets the content of template after generates.
	 * @return
	 */
	String out();
	
	/**
   * Gets the writer.
   * @return
   */
  Writer getWriter();
	
	/**
	 * Gets the template context
	 * @return
	 */
	TemplateContext getTemplateContext();
	
	/**
	 * Attaches the Template Context for Template generate.
	 * @param ctx The Template Context for generating
	 * @return
	 */
	ElementVisitor with(TemplateContext ctx);
}
