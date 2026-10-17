package wiz.gringotts.money;

/** Thrown when two amounts in different currencies are combined or compared. */
public class CurrencyMismatchException extends IllegalArgumentException {

  public CurrencyMismatchException(Currency left, Currency right) {
    super("Cannot combine " + left + " with " + right + "; convert through the exchange first");
  }
}
