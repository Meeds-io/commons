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
package org.exoplatform.commons.search.es.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * A document the search engine rejects is logged without the values the
 * engine quotes from it.
 */
public class ElasticIndexingAuditTrailTest {

  private static final String ES_9_3_4 = "elasticsearch-9.3.4";

  private static final String ES_9_5_3 = "elasticsearch-9.5.3";

  /** The reason Elasticsearch 9.3.4 gives for a value it cannot parse. */
  private static final String REJECTED_FIELD = "[1:7] failed to parse field [n] of type [long] in document with id '1'."
      + " Preview of field's value: 'It's the article's secret\nsecond line 'quoted' end'";

  @Test
  public void aRejectedFieldIsLoggedWithoutItsValue() {
    assertEquals("[1:7] failed to parse field [n] of type [long] in document with id '1'."
        + " Preview of field's value: [not logged]", ElasticIndexingAuditTrail.escape(REJECTED_FIELD));
  }

  @Test
  public void aRejectedDocumentOfABulkAnswerIsAuditedWithoutItsValue() throws Exception {
    ElasticIndexingAuditTrail auditTrail = mock(ElasticIndexingAuditTrail.class);
    sendBulk(auditTrail, engineAnswer(ES_9_3_4, "bulk-rejected-document.json"));

    assertEquals("[1:7] failed to parse field [n] of type [long] in document with id '9'."
        + " Preview of field's value: [not logged]", ElasticIndexingAuditTrail.escape(auditedBulkReason(auditTrail, "9", "dst")));
  }

  @Test
  public void aTooLongTermOfABulkAnswerIsAuditedWithoutItsValue() throws Exception {
    ElasticIndexingAuditTrail auditTrail = mock(ElasticIndexingAuditTrail.class);
    sendBulk(auditTrail, engineAnswer(ES_9_3_4, "bulk-immense-term.json"));

    String logged = ElasticIndexingAuditTrail.escape(auditedBulkReason(auditTrail, "3", "probe2"));

    assertEquals("[1:10] failed to parse field [body.raw] of type [keyword] in document with id '3'."
        + " Preview of field's value: [not logged]", logged);
  }

  @Test
  public void aMalformedDocumentOfABulkAnswerIsAuditedWithoutItsText() throws Exception {
    ElasticIndexingAuditTrail auditTrail = mock(ElasticIndexingAuditTrail.class);
    sendBulk(auditTrail, engineAnswer(ES_9_5_3, "bulk-malformed-document.json"));

    String logged = ElasticIndexingAuditTrail.escape(auditedBulkReason(auditTrail, "7", "dst"));

    assertEquals("[1:2] failed to parse: [not logged]", logged);
  }

  @Test
  public void aBulkRequestRefusedAsAWholeIsAuditedWithItsReason() throws Exception {
    ElasticIndexingAuditTrail auditTrail = mock(ElasticIndexingAuditTrail.class);
    sendBulk(auditTrail, engineAnswer(ES_9_5_3, "bulk-refused-request.json"));

    verify(auditTrail).logRejectedDocumentBulkOperation(eq(ElasticIndexingAuditTrail.BULK_REQUEST),
                                                        isNull(),
                                                        isNull(),
                                                        eq(400),
                                                        eq("Malformed action/metadata line [1], expected field [create],"
                                                            + " [delete], [index] or [update] but found [bogus]"),
                                                        anyLong());
  }

  @Test
  public void aBulkAnswerWithNeitherItemNorErrorIsNotAudited() {
    ElasticIndexingAuditTrail auditTrail = mock(ElasticIndexingAuditTrail.class);
    sendBulk(auditTrail, "{\"took\":1}");

    verifyNoInteractions(auditTrail);
  }

  @Test
  public void aRejectedDocumentAnswerIsLoggedWithoutTheValuesItQuotes() throws Exception {
    String answer = engineAnswer(ES_9_3_4, "bulk-rejected-document.json");

    String logged = ElasticIndexingAuditTrail.hideAnswerValues(answer);

    assertEquals("{\"errors\":true,\"took\":0,\"items\":[{\"index\":{\"_index\":\"dst\",\"_id\":\"9\",\"status\":400,"
        + "\"error\":{\"type\":\"document_parsing_exception\",\"reason\":\"[not logged]\",\"caused_by\":"
        + "{\"type\":\"illegal_argument_exception\",\"reason\":\"[not logged]\"}}}}]}", logged.strip());
  }

  @Test
  public void aFailedIndexCopyIsReportedWithoutTheValuesItQuotes() throws Exception {
    ElasticIndexingClient client = spy(new ElasticIndexingClient(mock(ElasticIndexingAuditTrail.class)));
    doReturn(new ElasticResponse(engineAnswer(ES_9_3_4, "reindex-rejected-documents.json"), 400)).when(client)
                                                                                       .sendHttpPostRequest(anyString(),
                                                                                                            anyString());

    ElasticClientException exception = assertThrows(ElasticClientException.class,
                                                    () -> client.sendReindexTypeRequest("news_v3", "news_v2", null));

    String message = exception.getMessage();
    assertFalse(message, message.contains("secret"));
    assertTrue(message, message.contains("{\"index\":\"dst\",\"id\":\"1\",\"cause\":{\"type\":\"document_parsing_exception\","
        + "\"reason\":\"[not logged]\",\"caused_by\":{\"type\":\"illegal_argument_exception\",\"reason\":\"[not logged]\"}},"
        + "\"status\":400}"));
    assertTrue(message, message.contains("{\"index\":\"dst\",\"id\":\"2\",\"cause\":{\"type\":\"document_parsing_exception\","
        + "\"reason\":\"[not logged]\",\"caused_by\":{\"type\":\"illegal_argument_exception\",\"reason\":\"[not logged]\"}},"
        + "\"status\":400}"));
  }

  @Test
  public void aLongReasonIsHiddenWhateverItHolds() {
    String value = "It's a secret \\\"quoted\\\" ".repeat(10000);
    String answer = "{\"error\":{\"root_cause\":[{\"type\":\"x\",\"reason\":\"" + value + "\"}],\"reason\" : \"" + value
        + "\",\"status\":400}}";

    assertEquals("{\"error\":{\"root_cause\":[{\"type\":\"x\",\"reason\":\"[not logged]\"}],\"reason\" : \"[not logged]\","
        + "\"status\":400}}", ElasticIndexingAuditTrail.hideAnswerValues(answer));
  }

  @Test
  public void aReasonWordInAValueIsKept() {
    assertEquals("{\"type\":\"reason\",\"reason\":\"[not logged]\",\"index\":\"reason\"}",
                 ElasticIndexingAuditTrail.hideAnswerValues("{\"type\":\"reason\",\"reason\":\"secret\",\"index\":\"reason\"}"));
  }

  @Test
  public void aFailedIndexCopyIsAuditedWithoutTheValuesItQuotes() throws Exception {
    ElasticIndexingAuditTrail auditTrail = mock(ElasticIndexingAuditTrail.class);
    ElasticIndexingClient client = spy(new ElasticIndexingClient(auditTrail));
    doReturn(new ElasticResponse(engineAnswer(ES_9_3_4, "reindex-rejected-documents.json"), 400)).when(client)
                                                                                       .sendHttpPostRequest(anyString(),
                                                                                                            anyString());

    assertThrows(ElasticClientException.class, () -> client.sendReindexTypeRequest("news_v3", "news_v2", null));

    ArgumentCaptor<String> audited = ArgumentCaptor.forClass(String.class);
    verify(auditTrail).audit(eq(ElasticIndexingAuditTrail.REINDEX_TYPE),
                             isNull(),
                             eq("news_v3"),
                             eq(400),
                             audited.capture(),
                             anyLong());
    assertFalse(audited.getValue(), audited.getValue().contains("secret"));
    assertTrue(audited.getValue(), audited.getValue().contains("\"id\":\"2\""));
  }

  /** Sends the answer to the client as a 200 answer of its bulk request. */
  private static void sendBulk(ElasticIndexingAuditTrail auditTrail, String answer) {
    ElasticIndexingClient client = spy(new ElasticIndexingClient(auditTrail));
    doReturn(new ElasticResponse(answer, 200)).when(client).sendHttpPostRequest(anyString(), anyString());
    client.sendCUDRequest("{}");
  }

  /** @return the reason the client audited for the rejected document of the given id */
  private static String auditedBulkReason(ElasticIndexingAuditTrail auditTrail, String id, String index) {
    ArgumentCaptor<String> audited = ArgumentCaptor.forClass(String.class);
    verify(auditTrail).logRejectedDocumentBulkOperation(eq("index"), eq(id), eq(index), eq(400), audited.capture(), anyLong());
    return audited.getValue();
  }

  /** An answer captured from the given Elasticsearch version. */
  private static String engineAnswer(String version, String name) throws Exception {
    try (InputStream answer = ElasticIndexingAuditTrailTest.class.getResourceAsStream("/" + version + "/" + name)) {
      return new String(answer.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  @Test
  public void anyOtherMessageKeepsItsTextWithoutTheSeparator() {
    assertEquals("index not found, news_v3", ElasticIndexingAuditTrail.escape("index not found; news_v3"));
    assertNull(ElasticIndexingAuditTrail.escape(null));
  }
}
