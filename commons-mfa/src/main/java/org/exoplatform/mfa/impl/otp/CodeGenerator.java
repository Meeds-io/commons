package org.exoplatform.mfa.impl.otp;
import org.apache.commons.codec.binary.Base32;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;

public class CodeGenerator  {

  private static final String ALGORITHM = "HmacSHA1";
  private final int           digits;

  public CodeGenerator() {
    this(6);
  }


  public CodeGenerator(int digits) {
    if (digits < 1) {
      throw new InvalidParameterException("Number of digits must be higher than 0.");
    }
    this.digits = digits;
  }

  public String generate(String key, long counter) throws NoSuchAlgorithmException, InvalidKeyException {
    byte[] hash = generateHash(key, counter);
    return getDigitsFromHash(hash);
  }

  /**
   * Generate a HMAC-SHA1 hash of the counter number.
   */
  private byte[] generateHash(String key, long counter) throws InvalidKeyException, NoSuchAlgorithmException {
    byte[] data = new byte[8];
    long value = counter;
    for (int i = 8; i-- > 0; value >>>= 8) {
      data[i] = (byte) value;
    }

    // Create a HMAC-SHA1 signing key from the shared key
    Base32 codec = new Base32();
    byte[] decodedKey = codec.decode(key);
    SecretKeySpec signKey = new SecretKeySpec(decodedKey, ALGORITHM);
    Mac mac = Mac.getInstance(ALGORITHM);
    mac.init(signKey);

    // Create a hash of the counter value
    return mac.doFinal(data);
  }

  /**
   * Get the n-digit code for a given hash.
   */
  private String getDigitsFromHash(byte[] hash) {
    int offset = hash[hash.length - 1] & 0xF;

    long truncatedHash = 0;

    for (int i = 0; i < 4; ++i) {
      truncatedHash <<= 8;
      truncatedHash |= (hash[offset + i] & 0xFF);
    }

    truncatedHash &= 0x7FFFFFFF;
    truncatedHash %= Math.pow(10, digits);

    // Left pad with 0s for a n-digit code
    String format = "%0"+digits+"d";
    return String.format(format, truncatedHash);
  }
}
