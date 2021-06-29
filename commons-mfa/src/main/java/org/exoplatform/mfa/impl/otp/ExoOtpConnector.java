package org.exoplatform.mfa.impl.otp;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.mfa.api.otp.OtpConnector;
import org.exoplatform.portal.branding.BrandingService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class ExoOtpConnector extends OtpConnector {

  private static final String OTP_SECRET = "otpSecret";
  private static final String OTP_SECRET_CHECKED = "otpSecretChecked";


  private int TIME_PERIOD   = 30;
  private int SECRET_LENGTH = 64;
  private int CODE_DIGITS   = 6;
  private String ALGORITHM = "SHA1";

  // If the current bucket is n, mark a code for the bucket interval
  // [n-TIME_PERIOD_DISCREPANCY;n+TIME_PERIOD_DISCREPANCY] as valid
  // It answer to the problem of hardware totp token desynchronization in time
  // With default parameters, it allows a code to be valid during 1m30 instead of 30s
  private int    TIME_PERIOD_DISCREPANCY = 1;
  private String ISSUER                  ="";

  private SettingService settingservice;
  private CodeGenerator codeGenerator;

  public ExoOtpConnector(InitParams initParams, SettingService settingService, BrandingService brandingService) {
    this.settingservice=settingService;
    if (initParams.getValueParam("issuer")!=null && !initParams.getValueParam("issuer").getValue().isBlank()) {
      ISSUER =initParams.getValueParam("issuer").getValue();
    } else {
      ISSUER =brandingService.getCompanyName();
    }

    if (initParams.getValueParam("timePeriod")!=null) {
      TIME_PERIOD=Integer.parseInt(initParams.getValueParam("timePeriod").getValue());
    }
    if (initParams.getValueParam("secretLength")!=null) {
      SECRET_LENGTH=Integer.parseInt(initParams.getValueParam("secretLength").getValue());
    }
    if (initParams.getValueParam("codeDigits")!=null) {
      CODE_DIGITS=Integer.parseInt(initParams.getValueParam("codeDigits").getValue());
    }
    if (initParams.getValueParam("timePeriodDiscrepancy")!=null) {
      TIME_PERIOD_DISCREPANCY=Integer.parseInt(initParams.getValueParam("timePeriodDiscrepancy").getValue());
    }
    if (initParams.getValueParam("algorithm")!=null) {
      ALGORITHM=initParams.getValueParam("algorithm").getValue();
    }

    this.codeGenerator=new CodeGenerator(CODE_DIGITS);


  }
  

  @Override
  public boolean isMfaInitializedForUser(String user) {
    return settingservice.get(new Context(Context.USER.getName(),user), Scope.APPLICATION,OTP_SECRET)!=null &&
        settingservice.get(new Context(Context.USER.getName(),user), Scope.APPLICATION,OTP_SECRET_CHECKED)!=null &&
        Boolean.valueOf(settingservice.get(new Context(Context.USER.getName(),user), Scope.APPLICATION, OTP_SECRET_CHECKED).getValue().toString());
  }

  @Override
  public String generateSecret(String user) {
    SecretGenerator secretGenerator = new SecretGenerator(SECRET_LENGTH);
    String secret = secretGenerator.generate();
    settingservice.set(new Context(Context.USER.getName(), user), Scope.APPLICATION, OTP_SECRET, new SettingValue<>(secret));
    settingservice.set(new Context(Context.USER.getName(), user), Scope.APPLICATION, OTP_SECRET_CHECKED,
                       new SettingValue<>("false"));
    //store the secret but mark it as a non validated until first verification

    return secret;
  }

  @Override
  public String generateUrlFromSecret(String user, String secret) {
    return "otpauth://totp/"+uriEncode(user)+"?"+
            "secret="+uriEncode(secret)+
            "&issuer="+uriEncode(ISSUER)+
            "&algorithm="+uriEncode(ALGORITHM)+
            "&digits="+CODE_DIGITS+
            "&period="+TIME_PERIOD;
  }
  
  @Override
  public boolean validateToken(String user, String code) {
    long time = Instant.now().getEpochSecond();

    String secret = (String)settingservice.get(new Context(Context.USER.getName(),user), Scope.APPLICATION,OTP_SECRET).getValue();


    // Get the current number of seconds since the epoch and
    // calculate the number of time periods passed.
    long currentBucket = Math.floorDiv(time, TIME_PERIOD);

    // Calculate and compare the codes for all the "valid" time periods,
    // even if we get an early match, to avoid timing attacks
    boolean success = false;
    for (int i = -TIME_PERIOD_DISCREPANCY; i <= TIME_PERIOD_DISCREPANCY; i++) {
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

  private String uriEncode(String text)  {
    // Null check
    if (text == null) {
      return "";
    }

    try {
      return URLEncoder.encode(text, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
    } catch (UnsupportedEncodingException e) {
      // This should never throw, as we are certain the charset specified (UTF-8) is valid
      throw new RuntimeException("Could not URI encode.");
    }
  }
}
