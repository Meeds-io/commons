package org.exoplatform.mfa.impl.otp;

import org.apache.commons.codec.binary.Base32;
import java.security.SecureRandom;

public class SecretGenerator {

  private final SecureRandom randomBytes = new SecureRandom();
  private static final Base32 encoder = new Base32();
  private final int numCharacters;

  public SecretGenerator() {
    this.numCharacters = 64;
  }

  /**
   * @param numCharacters The number of characters the secret should consist of.
   */
  public SecretGenerator(int numCharacters) {
    this.numCharacters = numCharacters;
  }

  public String generate() {
    return new String(encoder.encode(getRandomBytes()));
  }

  private byte[] getRandomBytes() {
    // 5 bits per char in base32
    byte[] bytes = new byte[(numCharacters * 5) / 8];
    randomBytes.nextBytes(bytes);

    return bytes;
  }
}
