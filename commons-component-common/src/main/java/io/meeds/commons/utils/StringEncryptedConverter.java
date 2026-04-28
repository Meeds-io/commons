/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.commons.utils;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.web.security.codec.AbstractCodec;
import org.exoplatform.web.security.codec.CodecInitializer;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Converter
@Slf4j
public class StringEncryptedConverter implements AttributeConverter<String, String> {

  private static AbstractCodec codec;

  @Override
  public String convertToDatabaseColumn(String value) {
    if (StringUtils.isNotBlank(value)) {
      value = getCodec().encode(value);
    }
    return value;
  }

  @Override
  public String convertToEntityAttribute(String value) {
    if (StringUtils.isNotBlank(value)) {
      try {
        return getCodec().decode(value);
      } catch (Exception e) {
        log.debug("Can't decode DB Value, use original value instead", e);
      }
    }
    return value;
  }

  @SneakyThrows
  public static AbstractCodec getCodec() {
    if (codec == null) {
      codec = ExoContainerContext.getService(CodecInitializer.class).getCodec();
    }
    return codec;
  }

}
