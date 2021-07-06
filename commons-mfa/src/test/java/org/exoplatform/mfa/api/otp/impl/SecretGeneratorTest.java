package org.exoplatform.mfa.api.otp.impl;

import org.exoplatform.mfa.impl.otp.SecretGenerator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SecretGeneratorTest {
  @Test
  public void testSecretGenerator() {
    //given
    int secretLength=32;
    SecretGenerator secretGenerator=new SecretGenerator(secretLength);

    //when
    String secret = secretGenerator.generate();

    //then
    assertEquals(secretLength,secret.length());
  }
}
