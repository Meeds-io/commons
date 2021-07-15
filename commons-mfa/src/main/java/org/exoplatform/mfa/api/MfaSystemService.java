package org.exoplatform.mfa.api;

import java.util.Locale;

public interface MfaSystemService {

  String getType();

  String getHelpTitle(Locale locale);

  String getHelpContent(Locale locale);

  default void removeSecret(String user) {}

}
