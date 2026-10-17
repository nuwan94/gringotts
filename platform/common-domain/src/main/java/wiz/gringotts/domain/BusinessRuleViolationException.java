package wiz.gringotts.domain;

/**
 * A well-formed request that the bank's rules forbid, e.g. insufficient funds or a frozen account.
 */
public class BusinessRuleViolationException extends DomainException {

  public BusinessRuleViolationException(String errorCode, String message) {
    super(errorCode, message);
  }
}
