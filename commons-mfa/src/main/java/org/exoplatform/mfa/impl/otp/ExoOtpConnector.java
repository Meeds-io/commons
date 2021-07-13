package org.exoplatform.mfa.impl.otp;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.mfa.api.otp.OtpConnector;
import org.exoplatform.portal.branding.BrandingService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;

public class ExoOtpConnector implements OtpConnector {

  private static final String OTP_SECRET = "otpSecret";
  private static final String OTP_SECRET_CHECKED = "otpSecretChecked";

  public static final String ISSUER_PARAM = "issuer";

  private int timePeriod   = 30;
  private int    secretLength = 64;
  private int    codeDigits   = 6;
  private String algorithm = "SHA1";


  // If the current bucket is n, mark a code for the bucket interval
  // [n-TIME_PERIOD_DISCREPANCY;n+TIME_PERIOD_DISCREPANCY] as valid
  // It answer to the problem of hardware totp token desynchronization in time
  // With default parameters, it allows a code to be valid during 1m30 instead of 30s
  private int    timePeriodDiscrepancy = 1;
  private String issuer                ="";

  private SettingService settingservice;
  private CodeGenerator codeGenerator;

  String name="ExoOtpConnector";

  private static final Log LOGGER = ExoLogger.getExoLogger(ExoOtpConnector.class);



  public ExoOtpConnector(InitParams initParams, SettingService settingService, BrandingService brandingService) {
    this.settingservice=settingService;
    if (initParams.getValueParam(ISSUER_PARAM)!=null && !initParams.getValueParam(ISSUER_PARAM).getValue().isBlank()) {
      issuer = initParams.getValueParam(ISSUER_PARAM).getValue();
    } else {
      issuer = brandingService.getCompanyName();
    }

    if (initParams.getValueParam("timePeriod")!=null) {
      timePeriod =Integer.parseInt(initParams.getValueParam("timePeriod").getValue());
    }
    if (initParams.getValueParam("secretLength")!=null) {
      secretLength =Integer.parseInt(initParams.getValueParam("secretLength").getValue());
    }
    if (initParams.getValueParam("codeDigits")!=null) {
      codeDigits =Integer.parseInt(initParams.getValueParam("codeDigits").getValue());
    }
    if (initParams.getValueParam("timePeriodDiscrepancy")!=null) {
      timePeriodDiscrepancy =Integer.parseInt(initParams.getValueParam("timePeriodDiscrepancy").getValue());
    }
    if (initParams.getValueParam("algorithm")!=null) {
      algorithm =initParams.getValueParam("algorithm").getValue();
    }

    this.codeGenerator=new CodeGenerator(codeDigits);


  }
  

  @Override
  public boolean isMfaInitializedForUser(String user) {
    return settingservice.get(new Context(Context.USER.getName(),user), Scope.APPLICATION,OTP_SECRET)!=null &&
        settingservice.get(new Context(Context.USER.getName(),user), Scope.APPLICATION,OTP_SECRET_CHECKED)!=null &&
        Boolean.valueOf(settingservice.get(new Context(Context.USER.getName(),user), Scope.APPLICATION, OTP_SECRET_CHECKED).getValue().toString());
  }

  @Override
  public String generateSecret(String user) {
    SecretGenerator secretGenerator = new SecretGenerator(secretLength);
    String secret = secretGenerator.generate();
    settingservice.set(new Context(Context.USER.getName(), user), Scope.APPLICATION, OTP_SECRET, new SettingValue<>(secret));
    settingservice.set(new Context(Context.USER.getName(), user), Scope.APPLICATION, OTP_SECRET_CHECKED,
                       new SettingValue<>("false"));
    //store the secret but mark it as a non validated until first verification

    return secret;
  }

  @Override
  public void removeSecret(String user) {
    settingservice.remove(new Context(Context.USER.getName(), user), Scope.APPLICATION, OTP_SECRET);
    settingservice.remove(new Context(Context.USER.getName(), user), Scope.APPLICATION, OTP_SECRET_CHECKED);
  }

  @Override
  public String generateUrlFromSecret(String user, String secret) {
    return "otpauth://totp/"+uriEncode(user)+"?"+
            "secret="+uriEncode(secret)+
            "&issuer="+uriEncode(issuer)+
            "&algorithm="+uriEncode(algorithm)+
            "&digits="+ codeDigits +
            "&period="+ timePeriod;
  }
  
  @Override
  public boolean validateToken(String user, String code, Clock clock) {
    long time = Instant.now(clock).getEpochSecond();

    String secret = (String)settingservice.get(new Context(Context.USER.getName(),user), Scope.APPLICATION,OTP_SECRET).getValue();


    // Get the current number of seconds since the epoch and
    // calculate the number of time periods passed.
    long currentBucket = Math.floorDiv(time, timePeriod);

    // Calculate and compare the codes for all the "valid" time periods,
    // even if we get an early match, to avoid timing attacks
    boolean success = false;
    for (int i = -timePeriodDiscrepancy; i <= timePeriodDiscrepancy; i++) {
      success = checkCode(secret, currentBucket + i, code) || success;
    }

    if (success && !isMfaInitializedForUser(user)) {
      settingservice.set(new Context(Context.USER.getName(),user), Scope.APPLICATION, OTP_SECRET_CHECKED,
                         new SettingValue<>("true"));
    }
    return success;
  }

  /**
   * Check if a code matches for a given secret and counter.
   */
  private boolean checkCode(String secret, long counter, String code) {
    try {
      String actualCode = codeGenerator.generate(secret, counter);
      return timeSafeStringComparison(actualCode, code);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Compare two strings for equality without leaking timing information.
   */
  private boolean timeSafeStringComparison(String a, String b) {
    byte[] aBytes = a.getBytes();
    byte[] bBytes = b.getBytes();
    if (aBytes.length != bBytes.length) {
      return false;
    }
    int result = 0;
    for (int i = 0; i < aBytes.length; i++) {
      result |= aBytes[i] ^ bBytes[i];
    }
    return result == 0;
  }

  private String uriEncode(String text) {
    // Null check
    if (text == null) {
      return "";
    }

    try {
      return URLEncoder.encode(text, StandardCharsets.UTF_8.toString()).replace("+", "%20");
    } catch (UnsupportedEncodingException e) {
     LOGGER.error("Unable to encode {}",text);
    }
    return "";

  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name=name;
  }
}
