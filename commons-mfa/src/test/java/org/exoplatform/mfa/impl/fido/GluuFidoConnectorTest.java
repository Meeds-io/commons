package org.exoplatform.mfa.impl.fido;

import org.apache.commons.io.IOUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.idm.UserDAOImpl;
import org.exoplatform.services.organization.idm.UserImpl;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommonsUtils.class, URL.class, IOUtils.class, GluuFido2Connector.class })
public class GluuFidoConnectorTest {
  private GluuFido2Connector gluuFido2Connector;
  
  private String rpHost = "http://test.exoplatform.com";
  private String userId="test";
  private String userDisplayName="Test User";
  
  private OutputStream outStream;
  
  
  @Before
  public void setUp() throws Exception {
    InitParams initParams = new InitParams();
    ValueParam valueParam = new ValueParam();
    valueParam.setName("serverUrl");
    valueParam.setValue(rpHost);
    initParams.addParam(valueParam);
    this.gluuFido2Connector=new GluuFido2Connector(initParams);
    PowerMockito.mockStatic(CommonsUtils.class);
    
    HttpURLConnection connection = PowerMockito.mock(HttpURLConnection.class);
    // mock connection and out stream methods
    URL url = PowerMockito.mock(URL.class);
    PowerMockito.whenNew(URL.class).withParameterTypes(String.class).withArguments(anyString()).thenReturn(url);
    when(url.openConnection()).thenReturn(connection);
  
    outStream=mock(OutputStream.class);
    when(connection.getOutputStream()).thenReturn(outStream);
    when(connection.getResponseCode()).thenReturn(200);
  
    InputStream inputStream = mock(InputStream.class);
    when(connection.getInputStream()).thenReturn(inputStream);
    
  }
  
  @Test
  public void testStartRegistration() throws Exception {
    OrganizationService organizationService = mock(OrganizationService.class);
    when(CommonsUtils.getService(OrganizationService.class)).thenReturn(organizationService);
    UserHandler userHandler=mock(UserDAOImpl.class);
    User user = new UserImpl(userId);
    user.setDisplayName(userDisplayName);
    
    when(userHandler.findUserByName(any())).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
  
    PowerMockito.mockStatic(IOUtils.class);
    String response = "{\"attestation\":\"direct\",\"authenticatorSelection\":{\"authenticatorAttachment\":\"cross-platform\","
        + "\"requireResidentKey\":false,\"userVerification\":\"preferred\"},"
        + "\"challenge\":\"MqJkTE3TYrQiNUARN9cjgih9Lp-bVOz95qcfxSmpVMc\",\"pubKeyCredParams\":[{\"type\":\"public-key\","
        + "\"alg\":-257},{\"type\":\"public-key\",\"alg\":-7}],\"rp\":{\"name\":\"Exo Gluu\",\"id\":\"exo.gluu.org\"},"
        + "\"user\":{\"id\":\"LKhwBAYgK738HmsIf1_XsRJmo9TopktGKN8ZEXQS7bo\",\"name\":\""+user.getUserName()+"\",\"displayName"
        + "\":\""+user.getDisplayName()+"\"},"
        + "\"excludeCredentials\":[{\"type\":\"public-key\",\"transports\":[\"usb\",\"ble\",\"nfc\"],"
        + "\"id\":\"ahhCZWLKnG5HsotoZ5kf74QMKuwuIEY2-vshWLNyZeY\"}],\"timeout\":90,\"status\":\"ok\",\"errorMessage\":\"\"}";
    when(IOUtils.toString(any(InputStream.class),any(String.class))).thenReturn(response);
  
    JSONObject result = gluuFido2Connector.startRegistration(userId, rpHost);
  
    ArgumentCaptor<byte[]> argument = ArgumentCaptor.forClass(byte[].class);
    verify(outStream).write(argument.capture());
  
    String message = "{\"documentDomain\":\""+rpHost+"\",\"attestation\":\"direct\",\"displayName\":\"Test User\","
        + "\"username\":\"test\"}";
    assertEquals("Check data before sending", message, new String(argument.getValue()));
  
    assertEquals("ahhCZWLKnG5HsotoZ5kf74QMKuwuIEY2+vshWLNyZeY=",
                 result.getJSONArray("excludeCredentials").getJSONObject(0).getString("id"));
    assertEquals("MqJkTE3TYrQiNUARN9cjgih9Lp+bVOz95qcfxSmpVMc=",
                 result.getString("challenge"));
    assertEquals("LKhwBAYgK738HmsIf1/XsRJmo9TopktGKN8ZEXQS7bo=",
                 result.getJSONObject("user").getString("id"));
    
  
  }
  
  @Test
  public void testFinishRegistration() throws Exception {
  
  
    JSONObject data = new JSONObject();
    data.put("type","public-key");
    data.put("id","NatAEzOzEPXbrB09tm09T1GgO_znJlfCs-Cyhlqk0vo");
    JSONObject response = new JSONObject();
    response.put("clientDataJSON","{\"type\":\"webauthn.create\",\"challenge\":\"sbAVewu2GJCzgitFcEJgCh9VsYZkyUtRgMKhrPBDZPI\","
        + "\"origin\":\"https://exo.gluu.org\",\"crossOrigin\":false}");
    response.put("attestationObject","o2NmbXRoZmlkby11MmZnYXR0U3RtdKJjc2lnWEgwRgIhAOYMyMmApPPEFjJAnNuXy5tFz1x1GpGSUG9RYz3Z5g7VAiEAk1XLTYIezblGKrBHOkAXra3Vh+dlyEhRvoleroQARBtjeDVjgVkB4DCCAdwwggGAoAMCAQICAQEwDQYJKoZIhvcNAQELBQAwYDELMAkGA1UEBhMCVVMxETAPBgNVBAoMCENocm9taXVtMSIwIAYDVQQLDBlBdXRoZW50aWNhdG9yIEF0dGVzdGF0aW9uMRowGAYDVQQDDBFCYXRjaCBDZXJ0aWZpY2F0ZTAeFw0xNzA3MTQwMjQwMDBaFw00MTA0MDMwNzM5MDlaMGAxCzAJBgNVBAYTAlVTMREwDwYDVQQKDAhDaHJvbWl1bTEiMCAGA1UECwwZQXV0aGVudGljYXRvciBBdHRlc3RhdGlvbjEaMBgGA1UEAwwRQmF0Y2ggQ2VydGlmaWNhdGUwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAASNYX5lyVCOZLzFZzrIKmeZ2jwURmgsJYxGP//fWN/S+j5sN4tT15XEpN/7QZnt14YvI6uvAgO0uJEboFaZlOEBoygwJjATBgsrBgEEAYLlHAIBAQQEAwIFIDAPBgNVHRMBAf8EBTADAQEAMA0GCSqGSIb3DQEBCwUAA0cAMEQCIEe7MYRUgcxqTDXyIAbbiWEvmuysjsbtqKVSsbVbmyvkAiB2JP29lfW2yq08xMewFTOF6rDPezCl4QIpkgrdLHUsJmhhdXRoRGF0YVikJ8K+BWPVHhSnfdznSUhaIVEuADn2ui3Xvhph4rt6pJhBAAAAAAAAAAAAAAAAAAAAAAAAAAAAIDWrQBMzsxD126wdPbZtPU9RoDv85yZXwrPgsoZapNL6pQECAyYgASFYIJRaJ79nktmEyO1R0ZGMQ6Un1C+JkpPo+EoJILcnHWX+Ilgg6+ohAVShAVPZgX6e2kS4HjQxjru6kmywsIg0Mw3Zr8Y=");
    data.put("response",response);
  
  
  
    PowerMockito.mockStatic(IOUtils.class);
    String responseFromServer = "{\"createdCredentials\":{\"type\":\"public-key\","
        + "\"id\":\"NatAEzOzEPXbrB09tm09T1GgO_znJlfCs-Cyhlqk0vo\"},\"status\":\"ok\",\"errorMessage\":\"\"}";
    when(IOUtils.toString(any(InputStream.class),any(String.class))).thenReturn(responseFromServer);
    
    JSONObject result = gluuFido2Connector.finishRegistration(userId, data);
    
    ArgumentCaptor<byte[]> argument = ArgumentCaptor.forClass(byte[].class);
    verify(outStream).write(argument.capture());
  
    String dataToSend = "{\"response\":{\"clientDataJSON"
        +
        "\":\"eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoic2JBVmV3dTJHSkN6Z2l0RmNFSmdDaDlWc1laa3lVdFJnTUtoclBCRFpQSSIsIm9yaWdpbiI6Imh0dHBzOi8vZXhvLmdsdXUub3JnIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==\",\"attestationObject\":\"o2NmbXRoZmlkby11MmZnYXR0U3RtdKJjc2lnWEgwRgIhAOYMyMmApPPEFjJAnNuXy5tFz1x1GpGSUG9RYz3Z5g7VAiEAk1XLTYIezblGKrBHOkAXra3Vh-dlyEhRvoleroQARBtjeDVjgVkB4DCCAdwwggGAoAMCAQICAQEwDQYJKoZIhvcNAQELBQAwYDELMAkGA1UEBhMCVVMxETAPBgNVBAoMCENocm9taXVtMSIwIAYDVQQLDBlBdXRoZW50aWNhdG9yIEF0dGVzdGF0aW9uMRowGAYDVQQDDBFCYXRjaCBDZXJ0aWZpY2F0ZTAeFw0xNzA3MTQwMjQwMDBaFw00MTA0MDMwNzM5MDlaMGAxCzAJBgNVBAYTAlVTMREwDwYDVQQKDAhDaHJvbWl1bTEiMCAGA1UECwwZQXV0aGVudGljYXRvciBBdHRlc3RhdGlvbjEaMBgGA1UEAwwRQmF0Y2ggQ2VydGlmaWNhdGUwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAASNYX5lyVCOZLzFZzrIKmeZ2jwURmgsJYxGP__fWN_S-j5sN4tT15XEpN_7QZnt14YvI6uvAgO0uJEboFaZlOEBoygwJjATBgsrBgEEAYLlHAIBAQQEAwIFIDAPBgNVHRMBAf8EBTADAQEAMA0GCSqGSIb3DQEBCwUAA0cAMEQCIEe7MYRUgcxqTDXyIAbbiWEvmuysjsbtqKVSsbVbmyvkAiB2JP29lfW2yq08xMewFTOF6rDPezCl4QIpkgrdLHUsJmhhdXRoRGF0YVikJ8K-BWPVHhSnfdznSUhaIVEuADn2ui3Xvhph4rt6pJhBAAAAAAAAAAAAAAAAAAAAAAAAAAAAIDWrQBMzsxD126wdPbZtPU9RoDv85yZXwrPgsoZapNL6pQECAyYgASFYIJRaJ79nktmEyO1R0ZGMQ6Un1C-JkpPo-EoJILcnHWX-Ilgg6-ohAVShAVPZgX6e2kS4HjQxjru6kmywsIg0Mw3Zr8Y=\"},\"id\":\"NatAEzOzEPXbrB09tm09T1GgO_znJlfCs-Cyhlqk0vo\",\"type\":\"public-key\"}";
    assertEquals("Check data before sending", dataToSend, new String(argument.getValue()));
  
    assertEquals("Check type after receiving response","public-key",
                result.getJSONObject("createdCredentials").getString("type"));
    assertEquals("Check id after receiving response","NatAEzOzEPXbrB09tm09T1GgO_znJlfCs-Cyhlqk0vo",
                result.getJSONObject("createdCredentials").getString("id"));
    assertEquals("Check status after receiving response","ok", result.getString("status"));
    assertEquals("Check errorMessage after receiving response","", result.getString("errorMessage"));
  }
  
  @Test
  public void testStartAuthentication() throws Exception {
    OrganizationService organizationService = mock(OrganizationService.class);
    when(CommonsUtils.getService(OrganizationService.class)).thenReturn(organizationService);
    UserHandler userHandler=mock(UserDAOImpl.class);
    User user = new UserImpl(userId);
    user.setDisplayName(userDisplayName);
  
    when(userHandler.findUserByName(any())).thenReturn(user);
    when(organizationService.getUserHandler()).thenReturn(userHandler);
  
    PowerMockito.mockStatic(IOUtils.class);
    String response = "{\"userVerification\":\"preferred\",\"challenge\":\"KUBHWg1TEfjgepso5_CmPqRH2vaqiNwN7DjPeRZL0mE\","
        + "\"rpId\":\"exo.gluu.org\",\"allowCredentials\":[{\"type\":\"public-key\",\"transports\":[\"usb\",\"ble\",\"nfc\"],"
        + "\"id\":\"qkHelobkQx8pej9aiZtxJB4zENDfM-WDN53Dk0iooe0\"}],\"timeout\":90,\"status\":\"ok\",\"errorMessage\":\"\"}";
    when(IOUtils.toString(any(InputStream.class),any(String.class))).thenReturn(response);
    JSONObject result = gluuFido2Connector.startAuthentication(userId, rpHost);
  
    ArgumentCaptor<byte[]> argument = ArgumentCaptor.forClass(byte[].class);
    verify(outStream).write(argument.capture());
  
    String message = "{\"documentDomain\":\""+rpHost+"\",\"username\":\""+userId+"\"}";
    assertEquals("Check data before sending", message, new String(argument.getValue()));
  
    //{"userVerification":"preferred","errorMessage":"","challenge":"KUBHWg1TEfjgepso5/CmPqRH2vaqiNwN7DjPeRZL0mE=",
    // "rpId":"exo.gluu.org",
    // "allowCredentials":[{"transports":["usb","ble","nfc"],"id":"qkHelobkQx8pej9aiZtxJB4zENDfM+WDN53Dk0iooe0=","type":"public-key"}],"timeout":90,"status":"ok"}
  
  
    assertEquals("preferred",
                 result.getString("userVerification"));
    assertEquals("",
                 result.getString("errorMessage"));
    assertEquals("KUBHWg1TEfjgepso5/CmPqRH2vaqiNwN7DjPeRZL0mE=",
                 result.getString("challenge"));
    assertEquals("qkHelobkQx8pej9aiZtxJB4zENDfM+WDN53Dk0iooe0=",
                 result.getJSONArray("allowCredentials").getJSONObject(0).getString("id"));
  }
  
  
  @Test
  public void testFinishAuthentication() throws Exception {
    
    JSONObject data = new JSONObject();
    data.put("type","public-key");
    data.put("id","m6epTDX_-9mhsyJg0-sbf_EPYkxXl09aIhUbvTo2ShM");
    data.put("rawId","m6epTDX/+9mhsyJg0+sbf/EPYkxXl09aIhUbvTo2ShM=");
    JSONObject response = new JSONObject();
    response.put("clientDataJSON","{\"type\":\"webauthn.get\","
        + "\"challenge\":\"h4101onO_wv7z3nXq4ACgf-gTyy906ztaj4W62V5Aq8\",\"origin\":\"https://exo.gluu.org\","
        + "\"crossOrigin\":false}");
    response.put("signature","MEUCIQCoD5xsTwXPZQqT9xhZn+lIEfIq5s1u946R+bhVhIBuygIgHS30cLiYFFFjMw/931Ddmc/eQxBgy4E+53iWTbgXS7c=");
    response.put("authenticatorData","J8K+BWPVHhSnfdznSUhaIVEuADn2ui3Xvhph4rt6pJgBAAAABA==");
    data.put("response",response);
    
    PowerMockito.mockStatic(IOUtils.class);
    String responseFromServer = "{\"authenticatedCredentials\":{\"type\":\"public-key\","
        + "\"id\":\"m6epTDX_-9mhsyJg0-sbf_EPYkxXl09aIhUbvTo2ShM\"},\"status\":\"ok\",\"errorMessage\":\"\"}";
    when(IOUtils.toString(any(InputStream.class),any(String.class))).thenReturn(responseFromServer);
    
    JSONObject result = gluuFido2Connector.finishAuthentication(userId, data);
    
    ArgumentCaptor<byte[]> argument = ArgumentCaptor.forClass(byte[].class);
    verify(outStream).write(argument.capture());
    
    String dataToSend = "{\"response\":{\"clientDataJSON"
        +
        "\":\"eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoiaDQxMDFvbk9fd3Y3ejNuWHE0QUNnZi1nVHl5OTA2enRhajRXNjJWNUFxOCIsIm9yaWdpbiI6Imh0dHBzOi8vZXhvLmdsdXUub3JnIiwiY3Jvc3NPcmlnaW4iOmZhbHNlfQ==\",\"signature\":\"MEUCIQCoD5xsTwXPZQqT9xhZn-lIEfIq5s1u946R-bhVhIBuygIgHS30cLiYFFFjMw_931Ddmc_eQxBgy4E-53iWTbgXS7c=\",\"authenticatorData\":\"J8K-BWPVHhSnfdznSUhaIVEuADn2ui3Xvhph4rt6pJgBAAAABA==\"},\"rawId\":\"m6epTDX/+9mhsyJg0+sbf/EPYkxXl09aIhUbvTo2ShM=\",\"id\":\"m6epTDX_-9mhsyJg0-sbf_EPYkxXl09aIhUbvTo2ShM\",\"type\":\"public-key\"}";
    assertEquals("Check data before sending", dataToSend, new String(argument.getValue()));
    
    assertEquals("Check type after receiving response","public-key",
                 result.getJSONObject("authenticatedCredentials").getString("type"));
    assertEquals("Check id after receiving response","m6epTDX_-9mhsyJg0-sbf_EPYkxXl09aIhUbvTo2ShM",
                 result.getJSONObject("authenticatedCredentials").getString("id"));
    assertEquals("Check status after receiving response","ok", result.getString("status"));
    assertEquals("Check errorMessage after receiving response","", result.getString("errorMessage"));
  }
}
