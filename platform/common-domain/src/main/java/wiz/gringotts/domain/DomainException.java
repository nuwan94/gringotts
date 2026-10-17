package wiz.gringotts.domain;

import java.util.Objects;

/**
 * Base class for expected business failures. The {@link #errorCode()} is a stable, kebab-case
 * identifier that clients can rely on (for example {@code insufficient-funds}); the message is for
 * humans and may change.
 */
public abstract class DomainException extends RuntimeException {

  private final String errorCode;

  protected DomainException(String errorCode, String message) {
    super(message);
    this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
  }

  public String errorCode() {
    return errorCode;
  }
}
