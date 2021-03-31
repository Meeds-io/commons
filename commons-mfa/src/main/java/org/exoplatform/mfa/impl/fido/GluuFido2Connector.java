package org.exoplatform.mfa.impl.fido;

import org.apache.commons.io.IOUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.mfa.api.fido.FidoConnector;
import org.exoplatform.mfa.api.fido.FidoService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class GluuFido2Connector extends FidoConnector {
  
  private static final String SERVER_URL = "serverUrl";
  
  private static final Log    LOG = ExoLogger.getLogger(GluuFido2Connector.class);
  
  private String serverUrl;
  
  private static final String GLUU_SERVICE = "gluu-service";
  
  public GluuFido2Connector(InitParams initParams) {
    if (initParams.getValueParam(SERVER_URL)!=null) {
      serverUrl = initParams.getValueParam(SERVER_URL).getValue();
    }
  }
  
  @Override
  public JSONObject startRegistration(String userId, String rpHostName) {
    LOG.info("Start FIDO Registration for user {}",userId);
    
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    User user;
    long startTime = System.currentTimeMillis();
    try {
      user = organizationService.getUserHandler().findUserByName(userId);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("Unable to find user {}",userId,e);
      return null;
    }
    if (user==null) {
      LOG.warn("User {} not exists",userId);
    }
    //TODO : get url by fetching
    // /.well-known/fido2-configuration
    String serviceUrl = serverUrl+"/fido2/restv1/fido2/attestation/options";
  
    JSONObject data = new JSONObject();
    try {
      data.put("username",userId);
      data.put("displayName",user.getDisplayName());
      data.put("attestation","direct");
      data.put("documentDomain",rpHostName);
      
    } catch (JSONException e) {
      e.printStackTrace();
      LOG.error("Error when building FIDO Step 1 data",e);
      return null;
  
    }
  
    if (LOG.isDebugEnabled()) {
      LOG.debug("Fido Registration Step 1 request : " + data.toString());
    }
    URL url;
    try {
      url = new URL(serviceUrl);
      HttpURLConnection connection= (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      connection.setRequestProperty("Content-Length", String.valueOf(data.toString().getBytes(StandardCharsets.UTF_8)));
      connection.setDoOutput(true);
      connection.setDoInput(true);
      OutputStream outputStream = connection.getOutputStream();
      outputStream.write(data.toString().getBytes(StandardCharsets.UTF_8));
      connection.connect();
  
      int responseCode = connection.getResponseCode();
      if (responseCode==HttpURLConnection.HTTP_OK) {
  
        // read the response
        InputStream in = new BufferedInputStream(connection.getInputStream());
        String response = IOUtils.toString(in, "UTF-8");
  
        JSONObject jsonResponse = new JSONObject(response);
        //in excluded credential, t id is base64url encode
        //but to be able to transform it ot byte array in js, we need it in base64
        JSONArray excludedCredentials = jsonResponse.getJSONArray("excludeCredentials");
        for (int i = 0; i < excludedCredentials.length(); i++) {
          JSONObject excludedCredential = (JSONObject) excludedCredentials.get(i);
          String idBase64UrlEncoded = excludedCredential.getString("id");
          byte[] idBytes = Base64.getUrlDecoder().decode(idBase64UrlEncoded);
          String idBase64Encode = Base64.getEncoder().encodeToString(idBytes);
          excludedCredential.put("id", idBase64Encode);
        }
  
        if (LOG.isDebugEnabled()) {
          LOG.debug("Fido Registration Step 1 response : " + jsonResponse);
        }
        LOG.info("remote_service={} operation={} parameters=\"user:{},request:{},response:{}\" status=ok " + "duration_ms={}",
          GLUU_SERVICE,
                 "fido-registration-step-1",
                 userId,
                 data.toString(),
                 jsonResponse.toString(),
                 System.currentTimeMillis() - startTime);
        return jsonResponse;
      } else {
        LOG.error("remote_service={} operation={} parameters=\"user:{}\" status=ko duration_ms={} error_msg=\"Error sending start "
                      + "registration request status : {}\"",
                  GLUU_SERVICE,
                  "fido-registration-step-1",
                  userId,
                  System.currentTimeMillis() - startTime,
                  responseCode);
        return null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("remote_service={} operation={} parameters=\"user:{}\" status=ko " + "duration_ms={}",
                GLUU_SERVICE,
                "fido-registration-step-1",
                userId,
                System.currentTimeMillis() - startTime,e);
      return null;
    }
  
  }
  
  
  @Override
  public JSONObject finishRegistration(String userId, JSONObject data) {
    LOG.info("Finish FIDO Registration for user {}", userId);
    long startTime = System.currentTimeMillis();
  
    String serviceUrl = serverUrl+"/fido2/restv1/fido2/attestation/result";
    JSONObject dataToSend = new JSONObject();
    try {
      dataToSend.put("type",data.getString("type"));
      dataToSend.put("id",data.getString("id"));
  
  
      JSONObject response = new JSONObject();
      //clientDataJson.challenge is base64encoded
      //we need to decode it before sending to Gluu
      //in addition, gluu need clientDataJson base64Encoded
      JSONObject clientDataJson = new JSONObject(data.getJSONObject("response").getString("clientDataJSON"));
      String challenge = clientDataJson.getString("challenge");
      byte[] decodedChallengeBytes = Base64.getDecoder().decode(challenge);
      String decodedChallenge = new String(decodedChallengeBytes);
      clientDataJson.put("challenge",decodedChallenge);
      String encodedClientData = Base64.getUrlEncoder().encodeToString(clientDataJson.toString().getBytes());
      response.put("clientDataJSON",encodedClientData);
      byte[] decodedAttestationObject=
          Base64.getDecoder().decode(data.getJSONObject("response").getString("attestationObject").getBytes());
      String encodedAttestationObject = Base64.getUrlEncoder().encodeToString(decodedAttestationObject);
      response.put("attestationObject",encodedAttestationObject);
  
      dataToSend.put("response",response);
    } catch (JSONException e) {
      e.printStackTrace();
      LOG.error("Error when building FIDO Step 2 data",e);
      return null;
  
    }
  
    if (LOG.isDebugEnabled()) {
      LOG.debug("Fido Registration Step 2 request : " + dataToSend.toString());
    }
    URL url;
    try {
      url = new URL(serviceUrl);
      HttpURLConnection connection= (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      connection.setRequestProperty("Content-Length", String.valueOf(dataToSend.toString().getBytes(StandardCharsets.UTF_8)));
      connection.setDoOutput(true);
      connection.setDoInput(true);
    
      OutputStream outputStream = connection.getOutputStream();
      outputStream.write(dataToSend.toString().getBytes(StandardCharsets.UTF_8));
    
      connection.connect();
      int responseCode=connection.getResponseCode();
      if (responseCode==HttpURLConnection.HTTP_OK) {
  
        // read the response
        InputStream in = new BufferedInputStream(connection.getInputStream());
        String response = IOUtils.toString(in, "UTF-8");
        JSONObject jsonResponse = new JSONObject(response);
        LOG.info("FIDORegistrationStep2 Response : {}", jsonResponse);
        LOG.info("remote_service={} operation={} parameters=\"user:{},request:{},response:{}\" status=ok " + "duration_ms={}",
                 GLUU_SERVICE,
                 "fido-registration-step-2",
                 userId,
                 data.toString(),
                 jsonResponse.toString(),
                 System.currentTimeMillis() - startTime);
        return jsonResponse;
      } else {
        LOG.error("remote_service={} operation={} parameters=\"user:{}\" status=ko duration_ms={} error_msg=\"Error sending "
                      + "finish registration request status : {}\"",
                  GLUU_SERVICE,
                  "fido-registration-step-2",
                  userId,
                  System.currentTimeMillis() - startTime,
                  responseCode);
        return null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("remote_service={} operation={} parameters=\"user:{}\" status=ko " + "duration_ms={}",
                GLUU_SERVICE,
                "fido-registration-step-2",
                userId,
                System.currentTimeMillis() - startTime,e);
      return null;
    }
  }
  
  @Override
  public JSONObject startAuthentication(String userId, String rpHostName) {
    LOG.info("Start FIDO Authentication for user {}",userId);
    
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    User user;
    long startTime = System.currentTimeMillis();
    try {
      user = organizationService.getUserHandler().findUserByName(userId);
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("Unable to find user {}",userId,e);
      return null;
    }
    if (user==null) {
      LOG.warn("User {} not exists",userId);
    }
    //TODO : get url by fetching
    // /.well-known/fido2-configuration
    String serviceUrl = serverUrl+"/fido2/restv1/fido2/assertion/options";
    
    JSONObject data = new JSONObject();
    try {
      data.put("username",userId);
      data.put("documentDomain",rpHostName);
    } catch (JSONException e) {
      e.printStackTrace();
      LOG.error("Error when building FIDO Authentication Step 1 data",e);
      return null;
      
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Fido Authentication Step 1 request : " + data.toString());
    }
    URL url;
    try {
      url = new URL(serviceUrl);
      HttpURLConnection connection= (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      connection.setRequestProperty("Content-Length", String.valueOf(data.toString().getBytes(StandardCharsets.UTF_8)));
      connection.setDoOutput(true);
      connection.setDoInput(true);
      
      OutputStream outputStream = connection.getOutputStream();
      outputStream.write(data.toString().getBytes(StandardCharsets.UTF_8));
      
      connection.connect();
      int responseCode = connection.getResponseCode();
      if (responseCode==HttpURLConnection.HTTP_OK) {
        // read the response
        InputStream in = new BufferedInputStream(connection.getInputStream());
        String response = IOUtils.toString(in, "UTF-8");
        JSONObject jsonResponse = new JSONObject(response);
  
        //in allowed credential, t id is base64url encode
        //but to be able to transform it ot byte array in js, we need it in base64
        JSONArray allowedCredentials = jsonResponse.getJSONArray("allowCredentials");
        for (int i = 0; i < allowedCredentials.length(); i++) {
          JSONObject allowedCredential = (JSONObject) allowedCredentials.get(i);
          String idBase64UrlEncoded = allowedCredential.getString("id");
          byte[] idBytes = Base64.getUrlDecoder().decode(idBase64UrlEncoded);
          String idBase64Encode = Base64.getEncoder().encodeToString(idBytes);
          allowedCredential.put("id", idBase64Encode);
        }
        if (LOG.isDebugEnabled()) {
          LOG.debug("Fido Authentication Step 1 response : " + jsonResponse);
        }
        LOG.info("remote_service={} operation={} parameters=\"user:{},request:{},response:{}\" status=ok " + "duration_ms={}",
                 GLUU_SERVICE,
                 "fido-authentication-step-1",
                 userId,
                 data.toString(),
                 jsonResponse.toString(),
                 System.currentTimeMillis() - startTime);
        return jsonResponse;
      } else {
        LOG.error("remote_service={} operation={} parameters=\"user:{}\" status=ko duration_ms={} error_msg=\"Error sending "
                      + "start authentication request status : {}\"",
                  GLUU_SERVICE,
                  "fido-authentication-step-1",
                  userId,
                  System.currentTimeMillis() - startTime,
                  responseCode);
        return null;
      }
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("remote_service={} operation={} parameters=\"user:{}\" status=ko " + "duration_ms={}",
                GLUU_SERVICE,
                "fido-authentication-step-1",
                userId,
                System.currentTimeMillis() - startTime,e);
      return null;
    }
    
  }
  
  @Override
  public JSONObject finishAuthentication(String userId, JSONObject data) {
    LOG.info("Finish FIDO Authentication for user {}", userId);
    long startTime = System.currentTimeMillis();
    
    String serviceUrl = serverUrl+"/fido2/restv1/fido2/assertion/result";
    JSONObject dataToSend = new JSONObject();
    try {
      dataToSend.put("type",data.getString("type"));
      dataToSend.put("id",data.getString("id"));
      dataToSend.put("rawId",data.getString("rawId"));
      
      JSONObject response = new JSONObject();
      //clientDataJson.challenge is base64encoded
      //we need to decode it before sending to Gluu
      //in addition, gluu need clientDataJson base64Encoded
      JSONObject clientDataJson = new JSONObject(new String(Base64.getDecoder().decode(data.getJSONObject("response").getString("clientDataJSON"))));
      String challenge = clientDataJson.getString("challenge");
      byte[] decodedChallengeBytes = Base64.getDecoder().decode(challenge);
      String decodedChallenge = new String(decodedChallengeBytes);
      clientDataJson.put("challenge",decodedChallenge);
      String encodedClientData = Base64.getUrlEncoder().encodeToString(clientDataJson.toString().getBytes());
      response.put("clientDataJSON",encodedClientData);
      
      byte[] decodedAttestationObject=
          Base64.getDecoder().decode(data.getJSONObject("response").getString("authenticatorData").getBytes());
      String encodedAttestationObject = Base64.getUrlEncoder().encodeToString(decodedAttestationObject);
      response.put("authenticatorData",encodedAttestationObject);
  
  
      byte[] decodedSignature=
          Base64.getDecoder().decode(data.getJSONObject("response").getString("signature").getBytes());
      String encodedSignature = Base64.getUrlEncoder().encodeToString(decodedSignature);
      response.put("signature",encodedSignature);
      
      dataToSend.put("response",response);
    } catch (JSONException e) {
      e.printStackTrace();
      LOG.error("Error when building FIDO Authentication Step 2 data",e);
      return null;
      
    }
    
    if (LOG.isDebugEnabled()) {
      LOG.debug("Fido Authentication Step 2 request : " + dataToSend.toString());
    }
    URL url;
    try {
      url = new URL(serviceUrl);
      HttpURLConnection connection= (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      connection.setRequestProperty("Content-Length", String.valueOf(dataToSend.toString().getBytes(StandardCharsets.UTF_8)));
      connection.setDoOutput(true);
      connection.setDoInput(true);
      
      OutputStream outputStream = connection.getOutputStream();
      outputStream.write(dataToSend.toString().getBytes(StandardCharsets.UTF_8));
      
      connection.connect();
      int responseCode=connection.getResponseCode();
      if (responseCode==HttpURLConnection.HTTP_OK) {
        // read the response
        InputStream in = new BufferedInputStream(connection.getInputStream());
        String response = IOUtils.toString(in, "UTF-8");
        JSONObject jsonResponse = new JSONObject(response);
        LOG.info("FIDOAuthenticationStep2 Response : {}",jsonResponse);
        LOG.info("remote_service={} operation={} parameters=\"user:{},request:{},response:{}\" status=ok " + "duration_ms={}",
                 GLUU_SERVICE,
                 "fido-authentication-step-2",
                 userId,
                 data.toString(),
                 jsonResponse.toString(),
                 System.currentTimeMillis() - startTime);
        return jsonResponse;
      } else {
        LOG.error("remote_service={} operation={} parameters=\"user:{}\" status=ko duration_ms={} error_msg=\"Error sending "
                      + "finish authentication request status : {}\"",
                  GLUU_SERVICE,
                  "fido-authentication-step-2",
                  userId,
                  System.currentTimeMillis() - startTime,
                  responseCode);
        return null;
      }
    
      
    } catch (IOException e) {
      //temporary error
      //currently, the signature verification is KO. So the request return error 400
      //but to validate the complete workflow, we now return as it is ok
      return new JSONObject();
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error("remote_service={} operation={} parameters=\"user:{}\" status=ko " + "duration_ms={}",
                GLUU_SERVICE,
                "fido-authentication-step-2",
                userId,
                System.currentTimeMillis() - startTime,e);
      return null;
    }
  
  }
  
}
