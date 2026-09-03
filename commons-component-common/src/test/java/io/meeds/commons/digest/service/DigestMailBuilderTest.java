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
package io.meeds.commons.digest.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.exoplatform.commons.api.notification.model.MessageInfo;

import io.meeds.commons.digest.DigestCategoryRegistry;
import io.meeds.commons.digest.entity.DigestItemEntity;
import io.meeds.commons.digest.entity.DigestUserEntity;
import io.meeds.commons.digest.model.DigestFrequency;
import io.meeds.commons.digest.model.DigestItem;
import io.meeds.commons.digest.model.DigestLine;
import io.meeds.commons.digest.model.DigestUserSettings;
import io.meeds.commons.digest.plugin.DigestCategoryPlugin;
import io.meeds.commons.digest.plugin.DigestLineContext;
import io.meeds.commons.digest.plugin.DigestLinePlugin;

/**
 * The email built from the waiting items: the categories of the frequency
 * only, the display order, the caps and the "+ N more", the lines the addons
 * can't build, the escaping, and the "never send empty" rule. The real
 * template is used, so a broken template fails here and not in production.
 */
@RunWith(MockitoJUnitRunner.class)
public class DigestMailBuilderTest {

  private static final String     USERNAME      = "ayoub";

  private static final String     TEMPLATE_FILE =
                                                "../commons-extension-webapp/src/main/webapp/WEB-INF/notification/templates/mail/DigestMail.gtmpl";

  private static final Instant    FROM          = Instant.parse("2026-09-02T16:00:00Z");

  private static final Instant    UNTIL         = Instant.parse("2026-09-03T16:00:00Z");

  @Mock
  private DigestLabelResolver     labelResolver;

  @Mock
  private DigestRecipientResolver recipientResolver;

  private DigestCategoryRegistry  registry;

  private DigestMailBuilder       builder;

  private DigestUserEntity        user;

  @Before
  public void setUp() throws IOException {
    registry = new DigestCategoryRegistry();
    registry.addCategoryProvider(category("spaces", 10, "SpaceInvitationPlugin", "RequestJoinSpacePlugin"));
    registry.addCategoryProvider(category("feed", 20, "PostActivitySpaceStreamPlugin"));
    registry.addCategoryProvider(category("tasks", 40, "TaskAssignPlugin"));
    registry.addLineProvider(new TestLinePlugin("SpaceInvitationPlugin", "RequestJoinSpacePlugin", "PostActivitySpaceStreamPlugin"));
    // no line provider for tasks: its addon didn't declare one

    lenient().when(labelResolver.resolve(anyString(), anyString(), any())).thenAnswer(call -> "{0} did {1}");
    lenient().when(labelResolver.categoryLabel(any(), any())).thenAnswer(call -> "Label " + call.getArgument(0, io.meeds.commons.digest.plugin.DigestCategoryProvider.class).getId());
    lenient().when(labelResolver.commons(anyString(), any())).thenAnswer(call -> switch (call.getArgument(0, String.class)) {
      case "Notification.digest.subject.daily" -> "Your daily recap on {0} - {1} updates";
      case "Notification.digest.subject.weekly" -> "Your weekly recap on {0} - {1} updates";
      case "Notification.digest.greeting" -> "Hi {0},";
      case "Notification.digest.intro.daily" -> "Here''s what happened today on {0}:";
      case "Notification.digest.intro.weekly" -> "Your week from {1} to {2} on {0}:";
      case "Notification.digest.more" -> "+ {0} more";
      case "Notification.digest.viewAll" -> "View all on {0}";
      case "Notification.digest.manage" -> "Manage preferences";
      default -> call.getArgument(0, String.class);
    });
    lenient().when(recipientResolver.getEmail(USERNAME)).thenReturn("Ayoub Z<ayoub@example.com>");
    lenient().when(recipientResolver.getFirstName(USERNAME)).thenReturn("Ayoub <3");
    lenient().when(recipientResolver.getLocale(USERNAME)).thenReturn(Locale.ENGLISH);
    lenient().when(recipientResolver.getSender()).thenReturn("Platform<noreply@example.com>");
    lenient().when(recipientResolver.getPlatformName()).thenReturn("Meeds & Co");
    lenient().when(recipientResolver.getPlatformUrl()).thenReturn("https://platform/portal");
    lenient().when(recipientResolver.getSettingsUrl(USERNAME)).thenReturn("https://platform/settings");

    String template = Files.readString(Paths.get(TEMPLATE_FILE), StandardCharsets.UTF_8);
    builder = new DigestMailBuilder(registry, labelResolver, recipientResolver, new DigestMailRenderer(() -> template), 2, 3);
    user = new DigestUserEntity(1L, USERNAME, true, true, "Europe/Paris", FROM, FROM);
  }

  @Test
  public void testDailyEmailKeepsTheDailyCategoriesInDisplayOrderWithCapAndCount() {
    List<DigestItemEntity> items = new ArrayList<>();
    items.add(item(1, "PostActivitySpaceStreamPlugin", "feed", "poster", "john"));
    items.add(item(2, "SpaceInvitationPlugin", "spaces", "spaceId", "1"));
    items.add(item(3, "SpaceInvitationPlugin", "spaces", "spaceId", "2"));
    items.add(item(4, "RequestJoinSpacePlugin", "spaces", "spaceId", "3"));
    DigestUserSettings settings = settings(List.of("spaces", "feed"), List.of("feed"));

    MessageInfo message = builder.build(user, DigestFrequency.DAILY, settings, items, FROM, UNTIL);

    assertNotNull(message);
    assertEquals("Your daily recap on Meeds & Co - 4 updates", message.getSubject());
    assertEquals("Ayoub Z<ayoub@example.com>", message.getTo());
    assertEquals("Platform<noreply@example.com>", message.getFrom());
    String body = message.getBody();
    assertTrue(body.indexOf("Label spaces (3)") < body.indexOf("Label feed (1)"));
    // daily cap is 2: two space lines shown, one more
    assertTrue(body.contains("SpaceInvitationPlugin did 1"));
    assertTrue(body.contains("SpaceInvitationPlugin did 2"));
    assertFalse(body.contains("RequestJoinSpacePlugin did 3"));
    assertTrue(body.contains("+ 1 more"));
    assertTrue(body.contains("href=\"https://object/1\""));
    assertTrue(body.contains("Hi Ayoub &lt;3,"));
    assertTrue(body.contains("Here's what happened today on <a href=\"https://platform/portal\">Meeds &amp; Co</a>:"));
    assertTrue(body.contains("View all on Meeds &amp; Co"));
    assertTrue(body.contains("https://platform/settings"));
  }

  @Test
  public void testWeeklyEmailAppliesTheWeeklyListAndItsOwnCap() {
    List<DigestItemEntity> items = new ArrayList<>();
    items.add(item(1, "SpaceInvitationPlugin", "spaces", "spaceId", "1"));
    for (int i = 2; i <= 6; i++) {
      items.add(item(i, "PostActivitySpaceStreamPlugin", "feed", "poster", "john" + i));
    }
    DigestUserSettings settings = settings(List.of("spaces", "feed"), List.of("feed"));

    MessageInfo message = builder.build(user, DigestFrequency.WEEKLY, settings, items, FROM, UNTIL);

    assertNotNull(message);
    assertEquals("Your weekly recap on Meeds & Co - 5 updates", message.getSubject());
    String body = message.getBody();
    assertFalse(body.contains("Label spaces"));
    assertTrue(body.contains("Label feed (5)"));
    // weekly cap is 3
    assertTrue(body.contains("+ 2 more"));
    assertTrue(body.contains("Your week from Sep 2, 2026 to Sep 3, 2026"));
  }

  @Test
  public void testVanishedObjectsAndUnknownTypesAreLeftOutAndNotCounted() {
    List<DigestItemEntity> items = new ArrayList<>();
    items.add(item(1, "SpaceInvitationPlugin", "spaces", "spaceId", "1"));
    items.add(item(2, "SpaceInvitationPlugin", "spaces", "spaceId", TestLinePlugin.DELETED));
    items.add(item(3, "SpaceInvitationPlugin", "spaces", "spaceId", TestLinePlugin.FAILING));
    items.add(item(4, "TaskAssignPlugin", "tasks", "taskId", "9"));
    DigestUserSettings settings = settings(List.of("spaces", "tasks"), List.of());

    MessageInfo message = builder.build(user, DigestFrequency.DAILY, settings, items, FROM, UNTIL);

    assertNotNull(message);
    assertEquals("Your daily recap on Meeds & Co - 1 updates", message.getSubject());
    assertTrue(message.getBody().contains("Label spaces (1)"));
    assertFalse(message.getBody().contains("Label tasks"));
  }

  @Test
  public void testNothingLeftToSayMeansNoEmail() {
    List<DigestItemEntity> items = List.of(item(1, "SpaceInvitationPlugin", "spaces", "spaceId", TestLinePlugin.DELETED),
                                           item(2, "PostActivitySpaceStreamPlugin", "feed", "poster", "john"));
    // feed is not in the daily list, the only space object was deleted
    DigestUserSettings settings = settings(List.of("spaces"), List.of("feed"));
    assertNull(builder.build(user, DigestFrequency.DAILY, settings, items, FROM, UNTIL));
    assertNull(builder.build(user, DigestFrequency.DAILY, settings, List.of(), FROM, UNTIL));
  }

  @Test
  public void testRecipientWithoutEmailGetsNothing() {
    when(recipientResolver.getEmail(USERNAME)).thenReturn(null);
    List<DigestItemEntity> items = List.of(item(1, "SpaceInvitationPlugin", "spaces", "spaceId", "1"));
    assertNull(builder.build(user, DigestFrequency.DAILY, settings(List.of("spaces"), List.of()), items, FROM, UNTIL));
  }

  @Test
  public void testLineArgumentsAreEscaped() {
    List<DigestItemEntity> items = List.of(item(1, "SpaceInvitationPlugin", "spaces", "spaceId", "<script>"));
    MessageInfo message = builder.build(user, DigestFrequency.DAILY, settings(List.of("spaces"), List.of()), items, FROM, UNTIL);
    assertNotNull(message);
    assertFalse(message.getBody().contains("<script>"));
    assertTrue(message.getBody().contains("&lt;script&gt;"));
  }

  private static DigestUserSettings settings(List<String> daily, List<String> weekly) {
    return new DigestUserSettings(!daily.isEmpty(), daily, !weekly.isEmpty(), weekly);
  }

  private static DigestItemEntity item(long id, String pluginId, String category, String paramName, String paramValue) {
    return new DigestItemEntity(id,
                                USERNAME,
                                pluginId,
                                category,
                                UNTIL.minusSeconds(id),
                                DigestParamsCodec.serialize(Map.of(paramName, paramValue)));
  }

  private static DigestCategoryPlugin category(String id, int order, String... pluginIds) {
    InitParams params = new InitParams();
    ValueParam idParam = new ValueParam();
    idParam.setName("id");
    idParam.setValue(id);
    params.addParameter(idParam);
    ValueParam orderParam = new ValueParam();
    orderParam.setName("order");
    orderParam.setValue(String.valueOf(order));
    params.addParameter(orderParam);
    params.addParameter(pluginIds(pluginIds));
    return new DigestCategoryPlugin(params);
  }

  private static ValuesParam pluginIds(String... pluginIds) {
    ValuesParam valuesParam = new ValuesParam();
    valuesParam.setName("pluginIds");
    valuesParam.setValues(new ArrayList<>(List.of(pluginIds)));
    return valuesParam;
  }

  /**
   * An addon line provider: the line says which type and which object, a
   * "deleted" object gives no line, a "failing" one throws.
   */
  static class TestLinePlugin extends DigestLinePlugin {

    static final String DELETED = "deleted";

    static final String FAILING = "failing";

    TestLinePlugin(String... pluginIds) {
      super(params(pluginIds));
    }

    @Override
    public DigestLine buildLine(DigestItem item, DigestLineContext context) {
      String objectId = item.getParams().values().iterator().next();
      if (DELETED.equals(objectId)) {
        return null;
      }
      if (FAILING.equals(objectId)) {
        throw new IllegalStateException("addon failure");
      }
      return DigestLine.of("digest.line." + item.getPluginId(), "https://object/" + objectId, item.getPluginId(), objectId);
    }

    private static InitParams params(String... pluginIds) {
      InitParams params = new InitParams();
      params.addParameter(pluginIds(pluginIds));
      return params;
    }
  }

}
