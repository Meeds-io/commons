package org.exoplatform.commons.search.es;

import org.exoplatform.commons.search.es.client.ElasticSearchingClient;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * exo@exoplatform.com
 * 9/9/15
 */
public class ElasticSearchServiceConnectorTest {

  @Mock
  private ElasticSearchingClient elasticSearchingClient;
  
  @Test
  public void testMembership() {
      //Given
      setCurrentIdentity();
      ElasticSearchServiceConnector connector = new ElasticSearchServiceConnector(getInitParams(), elasticSearchingClient);
      //When
      String query = connector.buildQuery("My Wiki", null, 0, 20, "name", "asc");
      //Then
      assertThat(query, containsString("\"term\" : { \"permissions\" : \"BCH\" }"));
      assertThat(query, containsString("\"term\" : { \"permissions\" : \"any\" }"));
      assertThat(query, containsString("\"regexp\" : { \"permissions\" : \".*:Admin\" }"));
  }
  
  @Test
  public void testBuilQueryWithEntityId() {
  
    //Given
    setCurrentIdentity();
    ElasticSearchServiceConnector connector = new ElasticSearchServiceConnector(getInitParams(), elasticSearchingClient);
    //When
    String id = "123456789";
    String query = connector.builQueryWithEntityId("keyword1,keyword2", id);
  
    assertThat(query, containsString("\"term\" : { \"_id\" : \""+id+"\" }"));
    assertThat(query, containsString("\"query\" : \"keyword1~1 OR keyword2~1\""));

    query = connector.builQueryWithEntityId("keyword1,keyword2,composed keyword", id);

    assertThat(query, containsString("\"type\" : \"phrase\""));
    assertThat(query, containsString("\"query\" : \"composed keyword\""));
  }

  @Test
  public void testSortIsRelevancyByDefault() {
      //Given
      setCurrentIdentity();
      ElasticSearchServiceConnector connector = new ElasticSearchServiceConnector(getInitParams(), elasticSearchingClient);
      //When
      String query = connector.buildQuery("My Wiki", null, 0, 20, null, null);
      //Then
      assertThat(query, containsString("{ \"_score\" : {\"order\" : \"desc\"}}"));
  }

  @Test
  public void testSortRelevancyIsEqual_score() {
    //Given
    setCurrentIdentity();
    ElasticSearchServiceConnector connector = new ElasticSearchServiceConnector(getInitParams(), elasticSearchingClient);
    //When
    String query = connector.buildQuery("My Wiki", null, 0, 20, "relevancy", null);
    //Then
    assertThat(query, containsString("{ \"_score\" : {\"order\" : \"desc\"}}"));
  }

  @Test
  public void testDateRelevancyIsEqualLastUpdatedDate() {
    //Given
    setCurrentIdentity();
    ElasticSearchServiceConnector connector = new ElasticSearchServiceConnector(getInitParams(), elasticSearchingClient);
    //When
    String query = connector.buildQuery("My Wiki", null, 0, 20, "date", null);
    //Then
    assertThat(query, containsString("{ \"lastUpdatedDate\" : {\"order\" : \"desc\"}}"));
  }

  @Test
  public void testOrderIsDescByDefault() {
      //Given
      setCurrentIdentity();
      ElasticSearchServiceConnector connector = new ElasticSearchServiceConnector(getInitParams(), elasticSearchingClient);
      //When
      String query = connector.buildQuery("My Wiki", null, 0, 20, "name", null);
      //Then
      assertThat(query, containsString("\"sort\""));
      assertThat(query, containsString("{\"order\" : \"desc\"}"));
  }

  @Test
  public void testScoresAreTracked() {
      //Given
      setCurrentIdentity();
      ElasticSearchServiceConnector connector = new ElasticSearchServiceConnector(getInitParams(), elasticSearchingClient);
      //When
      String query = connector.buildQuery("My Wiki", null, 0, 20, "name", "asc");
      //Then
      assertThat(query, containsString("\"track_scores\": true"));
  }

  @Test
  public void testHighlightParamsAreSetByDefault() {
    //Given
    setCurrentIdentity();
    ElasticSearchServiceConnector connector = new ElasticSearchServiceConnector(getInitParams(), elasticSearchingClient);
    //When
    String query = connector.buildQuery("My Wiki", null, 0, 20, null, null);
    //Then
    assertThat(query, containsString("\"fragment_size\" : " + ElasticSearchServiceConnector.HIGHLIGHT_FRAGMENT_SIZE_DEFAULT_VALUE + ","));
    assertThat(query, containsString("\"number_of_fragments\" : " + ElasticSearchServiceConnector.HIGHLIGHT_FRAGMENT_NUMBER_DEFAULT_VALUE + "}"));
  }

    @Test(expected = IllegalStateException.class)
    public void testSearchWithoutIdentity() {
        //Given
        ConversationState.setCurrent(null);
        ElasticSearchServiceConnector connector = new ElasticSearchServiceConnector(getInitParams(), elasticSearchingClient);
        //When
        connector.buildQuery("My Wiki", null, 0, 20, null, null);
        //Then
        fail("IllegalStateException 'No identity found' expected");
    }

    private InitParams getInitParams() {
        InitParams params = new InitParams();
        PropertiesParam constructorParams = new PropertiesParam();
        constructorParams.setName("constructor.params");
        constructorParams.setProperty("searchType", "wiki");
        constructorParams.setProperty("displayName", "wiki");
        constructorParams.setProperty("index", "wiki");
        constructorParams.setProperty("type", "wiki");
        constructorParams.setProperty("searchFields", "name");
        params.addParam(constructorParams);
        return params;
    }

    private void setCurrentIdentity() {
        Identity identity = new Identity("BCH", Collections.singletonList(new MembershipEntry("Admin")));
        ConversationState.setCurrent(new ConversationState(identity));
    }
}
