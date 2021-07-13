package org.exoplatform.mfa.api.otp;

import org.exoplatform.container.component.ComponentPlugin;

import java.time.Clock;

public interface OtpConnector extends ComponentPlugin {

  default boolean validateToken(String user, String token, Clock clock) {
    return false;
  }

  @Override
  default String getDescription() {
    return null;
  }

  @Override
  default void setDescription(String s) {

  }

  default boolean isMfaInitializedForUser(String userId) {
    return false;
  }

  default String generateSecret(String userId) {
    return null;
  }
  default void removeSecret(String userId) {
  }

  default String generateUrlFromSecret(String user, String secret) {
    return null;
  }
}
