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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.web.security.codec.AbstractCodec;
import org.exoplatform.web.security.codec.CodecInitializer;

import lombok.SneakyThrows;

@RunWith(MockitoJUnitRunner.class)
public class StringEncryptedConverterTest {

  private static final String      ENCODED_VALUE = "encoded";

  private static final String      TEST_VALUE    = "test_value";

  private StringEncryptedConverter converter;

  private AbstractCodec            codec;

  @Mock
  private CodecInitializer         codecInitializer;

  @Before
  @SneakyThrows
  public void setUp() {
    codec = mock(AbstractCodec.class);
    converter = new StringEncryptedConverter();
    lenient().when(codecInitializer.getCodec()).thenReturn(codec);

    resetConverterStaticCodec();
  }

  @Test
  public void testConvertToDatabaseColumnShouldEncodeValue() {
    try (MockedStatic<ExoContainerContext> mockedStatic = Mockito.mockStatic(ExoContainerContext.class)) {
      mockedStatic.when(() -> ExoContainerContext.getService(CodecInitializer.class))
                  .thenReturn(codecInitializer);

      when(codec.encode(TEST_VALUE)).thenReturn(ENCODED_VALUE);

      assertEquals(ENCODED_VALUE, converter.convertToDatabaseColumn(TEST_VALUE));
      verify(codec).encode(TEST_VALUE);
    }
  }

  @Test
  public void testConvertToEntityAttributeShouldDecodeValue() {
    try (MockedStatic<ExoContainerContext> mockedStatic = Mockito.mockStatic(ExoContainerContext.class)) {

      mockedStatic.when(() -> ExoContainerContext.getService(CodecInitializer.class))
                  .thenReturn(codecInitializer);

      when(codec.decode(ENCODED_VALUE)).thenReturn(TEST_VALUE);

      assertEquals(TEST_VALUE, converter.convertToEntityAttribute(ENCODED_VALUE));
      verify(codec).decode(ENCODED_VALUE);
    }
  }

  @Test
  public void testConvertToEntityAttributeWhenDecodeFailsShouldReturnOriginalValue() {
    try (MockedStatic<ExoContainerContext> mockedStatic = Mockito.mockStatic(ExoContainerContext.class)) {
      mockedStatic.when(() -> ExoContainerContext.getService(CodecInitializer.class))
                  .thenReturn(codecInitializer);

      when(codec.decode("bad")).thenThrow(new RuntimeException("fail"));

      assertEquals("bad", converter.convertToEntityAttribute("bad"));
    }
  }

  @Test
  public void testConvertToDatabaseColumnWithBlankValueShouldReturnSameValue() {
    assertEquals("", converter.convertToDatabaseColumn(""));
  }

  @Test
  public void testConvertToEntityAttributeWithBlankValueShouldReturnSameValue() {
    assertEquals("", converter.convertToEntityAttribute(""));
  }

  @SneakyThrows
  private void resetConverterStaticCodec() {
    var field = StringEncryptedConverter.class.getDeclaredField("codec");
    field.setAccessible(true); // NOSONAR
    field.set(null, null); // NOSONAR
  }

}
