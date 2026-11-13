/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2026 Meeds Association contact@meeds.io
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
package org.mortbay.cometd.continuation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.web.security.codec.AbstractCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OortSecretResolverTest {

  private static final String                  ENCODED = "encoded:";

  @Mock
  private ExoContainer                         container;

  @Mock
  private SettingService                       settingService;

  @Mock
  private CacheService                         cacheService;

  @Mock
  @SuppressWarnings("rawtypes")
  private ExoCache                             settingCache;

  @Mock
  private AbstractCodec                        codec;

  private OortSecretResolver                   resolver;

  @BeforeEach
  @SuppressWarnings("unchecked")
  void setUp() {
    when(cacheService.getCacheInstance(OortSecretResolver.SETTING_CACHE)).thenReturn(settingCache);
    when(codec.encode(anyString())).thenAnswer(invocation -> ENCODED + invocation.getArgument(0));
    when(codec.decode(anyString())).thenAnswer(invocation -> {
      String value = invocation.getArgument(0);
      if (!value.startsWith(ENCODED)) {
        throw new IllegalStateException("not encoded with this key");
      }
      return value.substring(ENCODED.length());
    });
    resolver = new OortSecretResolver(container, settingService, cacheService, codec, 0);
  }

  @Test
  void testTheDeploymentOwnSecretIsKept() throws Exception {
    assertEquals("a-deployment-secret", resolver.resolve("a-deployment-secret"));
    verifyNoInteractions(settingService, cacheService);
  }

  @Test
  void testAStoredSecretIsReadWithoutStoringNorClearingTheCache() throws Exception {
    stubStored(ENCODED + "stored-secret");

    assertEquals("stored-secret", resolver.resolve(null));
    verify(settingService, never()).set(any(), any(), any(), any());
    verifyNoInteractions(cacheService);
  }

  @Test
  void testWithoutOwnSecretTheFirstNodeStoresARandomOne() throws Exception {
    AtomicReference<SettingValue<?>> stored = new AtomicReference<>();
    doAnswer(invocation -> {
      stored.set(invocation.getArgument(3));
      return null;
    }).when(settingService).set(eq(Context.GLOBAL), eq(OortSecretResolver.SETTING_SCOPE), eq(OortSecretResolver.SETTING_KEY), any());
    when(settingService.get(Context.GLOBAL, OortSecretResolver.SETTING_SCOPE, OortSecretResolver.SETTING_KEY))
                                                                                                             .thenAnswer(invocation -> stored.get());

    String secret = resolver.resolve("exo");

    assertEquals(ENCODED + secret, stored.get().getValue());
    assertTrue(OortSecretResolver.isOwnSecret(secret));
    assertTrue(secret.length() >= 40, secret);
  }

  @Test
  void testAfterStoringTheCacheIsClearedBeforeTheDatabaseIsReadBack() throws Exception {
    when(settingService.get(Context.GLOBAL, OortSecretResolver.SETTING_SCOPE, OortSecretResolver.SETTING_KEY))
                                                                                                             .thenReturn(null)
                                                                                                             .thenAnswer(invocation -> SettingValue.create(ENCODED + "other-node-secret"));

    assertEquals("other-node-secret", resolver.resolve(" "));

    InOrder order = inOrder(settingService, settingCache);
    order.verify(settingService).set(any(), any(), any(), any());
    order.verify(settingCache).clearCache();
    order.verify(settingService).get(Context.GLOBAL, OortSecretResolver.SETTING_SCOPE, OortSecretResolver.SETTING_KEY);
  }

  @Test
  void testTheReadBackWaitsForTheOtherStoresToLand() throws Exception {
    long[] times = new long[2];
    doAnswer(invocation -> {
      times[0] = System.nanoTime();
      return null;
    }).when(settingService).set(any(), any(), any(), any());
    doAnswer(invocation -> {
      times[1] = System.nanoTime();
      return null;
    }).when(settingCache).clearCache();
    when(settingService.get(Context.GLOBAL, OortSecretResolver.SETTING_SCOPE, OortSecretResolver.SETTING_KEY))
                                                                                                             .thenReturn(null)
                                                                                                             .thenAnswer(invocation -> SettingValue.create(ENCODED + "other-node-secret"));

    new OortSecretResolver(container, settingService, cacheService, codec, 50).resolve(null);

    assertTrue(times[1] - times[0] >= 50_000_000L, "settle delay not waited");
  }

  @Test
  void testAFailedStoreReadsTheOtherNodeSecret() throws Exception {
    doThrow(new IllegalStateException("duplicate key")).when(settingService).set(any(), any(), any(), any());
    when(settingService.get(Context.GLOBAL, OortSecretResolver.SETTING_SCOPE, OortSecretResolver.SETTING_KEY))
                                                                                                             .thenReturn(null)
                                                                                                             .thenAnswer(invocation -> SettingValue.create(ENCODED + "other-node-secret"));

    assertEquals("other-node-secret", resolver.resolve(null));
    verify(settingCache).clearCache();
  }

  @Test
  void testAStoredValueThisNodeCannotDecodeIsNeitherUsedNorReplaced() throws Exception {
    stubStored("stored-secret");

    assertNull(resolver.resolve(null));
    verify(settingService, never()).set(any(), any(), any(), any());
    verifyNoInteractions(cacheService);
  }

  @Test
  void testAStoredValueThatDecodesToNoOwnSecretIsNotUsed() throws Exception {
    stubStored(ENCODED + "exo");

    assertNull(resolver.resolve(null));
    verify(settingService, never()).set(any(), any(), any(), any());
  }

  @Test
  void testAReadBackValueThisNodeCannotDecodeIsNotUsed() throws Exception {
    when(settingService.get(Context.GLOBAL, OortSecretResolver.SETTING_SCOPE, OortSecretResolver.SETTING_KEY))
                                                                                                             .thenReturn(null)
                                                                                                             .thenAnswer(invocation -> SettingValue.create("other-node-secret"));

    assertNull(resolver.resolve(null));
  }

  @Test
  void testABlankStoredValueIsReplaced() throws Exception {
    when(settingService.get(Context.GLOBAL, OortSecretResolver.SETTING_SCOPE, OortSecretResolver.SETTING_KEY))
                                                                                                             .thenAnswer(invocation -> SettingValue.create(" "))
                                                                                                             .thenAnswer(invocation -> SettingValue.create(ENCODED + "new-secret"));

    assertEquals("new-secret", resolver.resolve(null));
    verify(settingService).set(any(), any(), any(), any());
  }

  @Test
  void testOnlyADeploymentOwnSecretCounts() {
    assertTrue(OortSecretResolver.isOwnSecret("x"));
    assertFalse(OortSecretResolver.isOwnSecret(null));
    assertFalse(OortSecretResolver.isOwnSecret(""));
    assertFalse(OortSecretResolver.isOwnSecret(" exo "));
  }

  private void stubStored(String secret) {
    when(settingService.get(Context.GLOBAL, OortSecretResolver.SETTING_SCOPE, OortSecretResolver.SETTING_KEY))
                                                                                                             .thenAnswer(invocation -> SettingValue.create(secret));
  }
}
