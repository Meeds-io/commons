package org.exoplatform.mfa.api.fido;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.mfa.api.otp.OtpService;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;

public class FidoServiceTest {
  
  private FidoConnector addConnector(String connectorName, FidoService fidoService) {
    FidoConnector testConnector = Mockito.mock(FidoConnector.class);
    Mockito.when(testConnector.getName()).thenReturn(connectorName);
    
    fidoService.addConnector(testConnector);
    return testConnector;
  }
  
  @Test
  public void testStartAuthentication() {
    InitParams initParams = new InitParams();
    ValueParam valueParam = new ValueParam();
    valueParam.setName("activeConnector");
    valueParam.setValue("testConnector");
    initParams.addParam(valueParam);
    FidoService fidoService=new FidoService(initParams);
    FidoConnector testConnector = addConnector("testConnector", fidoService);
  
    String userId="test";
    String rpHost="test.exoplatform.com";
    fidoService.startAuthentication(userId,rpHost);
    Mockito.verify(testConnector,times(1)).startAuthentication(Mockito.eq(userId),Mockito.eq(rpHost));
  }
  
  @Test
  public void testFinishAuthentication() {
    InitParams initParams = new InitParams();
    ValueParam valueParam = new ValueParam();
    valueParam.setName("activeConnector");
    valueParam.setValue("testConnector");
    initParams.addParam(valueParam);
    FidoService fidoService=new FidoService(initParams);
    FidoConnector testConnector = addConnector("testConnector", fidoService);
    String userId="test";

    fidoService.finishAuthentication(userId, new JSONObject());
    Mockito.verify(testConnector,times(1)).finishAuthentication(Mockito.eq(userId), Mockito.any());
  }
  
  @Test
  public void testStartRegistration() {
    InitParams initParams = new InitParams();
    ValueParam valueParam = new ValueParam();
    valueParam.setName("activeConnector");
    valueParam.setValue("testConnector");
    initParams.addParam(valueParam);
    FidoService fidoService=new FidoService(initParams);
    FidoConnector testConnector = addConnector("testConnector", fidoService);
    String userId="test";
    String rpHost="test.exoplatform.com";
    fidoService.startRegistration(userId,rpHost);
    Mockito.verify(testConnector,times(1)).startRegistration(Mockito.eq(userId),Mockito.eq(rpHost));
  }
  
  @Test
  public void testFinishRegistration() {
    InitParams initParams = new InitParams();
    ValueParam valueParam = new ValueParam();
    valueParam.setName("activeConnector");
    valueParam.setValue("testConnector");
    initParams.addParam(valueParam);
    FidoService fidoService=new FidoService(initParams);
    FidoConnector testConnector = addConnector("testConnector", fidoService);
    String userId="test";
    
    fidoService.finishRegistration(userId, new JSONObject());
    Mockito.verify(testConnector,times(1)).finishRegistration(Mockito.eq(userId), Mockito.any());
  }
  
  @Test
  public void testStartAuthenticationWithoutConnector() {
    InitParams initParams = new InitParams();
    FidoService fidoService=new FidoService(initParams);
    
    String userId="test";
    String rpHost="test.exoplatform.com";
    JSONObject result = fidoService.startAuthentication(userId,rpHost);
    assertNull(result);
  }
  
  @Test
  public void testFinishAuthenticationWithoutConnector() {
    InitParams initParams = new InitParams();
    
    FidoService fidoService=new FidoService(initParams);
    String userId="test";
    
    JSONObject result=fidoService.finishAuthentication(userId, new JSONObject());
    assertNull(result);
  }
  
  @Test
  public void testStartRegistrationWithoutConnector() {
    InitParams initParams = new InitParams();
    
    FidoService fidoService=new FidoService(initParams);
    String userId="test";
    String rpHost="test.exoplatform.com";
    JSONObject result=fidoService.startRegistration(userId,rpHost);
    assertNull(result);
  }
  
  
  @Test
  public void testFinishRegistrationWithoutConnector() {
    InitParams initParams = new InitParams();
    
    FidoService fidoService=new FidoService(initParams);
    String userId="test";
  
    JSONObject result=fidoService.finishRegistration(userId, new JSONObject());
    assertNull(result);
  
  }
}
