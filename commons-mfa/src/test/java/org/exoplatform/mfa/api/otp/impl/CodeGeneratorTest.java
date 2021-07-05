package org.exoplatform.mfa.api.otp.impl;

import org.exoplatform.mfa.impl.otp.CodeGenerator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CodeGeneratorTest {
  @Test
  public void testGenerateHash() throws Exception {
    CodeGenerator codeGenerator = new CodeGenerator();

    //given
    String key="YDPNLZ2YIM2SXNNUTXRBPYUVN36FUZEW6EC3Y4NAH4WCZ734J7QVIXXLAKWTXQHZ";
    long counter=1L;

    //when
    String hash=codeGenerator.generate(key,counter);

    //then
    assertEquals("351334",hash);

  }
}
