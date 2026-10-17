package wiz.gringotts.domain;

/** The request clashes with the current state, e.g. a duplicate or a concurrent modification. */
public class ConflictException extends DomainException {

  public ConflictException(String errorCode, String message) {
    super(errorCode, message);
  }
}
