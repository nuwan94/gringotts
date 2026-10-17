package wiz.gringotts.money;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An immutable amount of money held as a whole number of the currency's minor unit (Knuts or
 * pence). Arithmetic is exact: overflow throws rather than wrapping, and amounts in different
 * currencies can never be combined.
 */
public record Money(long minorUnits, Currency currency) implements Comparable<Money> {

  public Money {
    Objects.requireNonNull(currency, "currency");
  }

  public static Money of(long minorUnits, Currency currency) {
    return new Money(minorUnits, currency);
  }

  public static Money zero(Currency currency) {
    return new Money(0, currency);
  }

  public static Money knuts(long knuts) {
    return new Money(knuts, Currency.WZG);
  }

  public static Money galleons(long galleons, int sickles, int knuts) {
    return new Money(new WizardingCoins(false, galleons, sickles, knuts).toKnuts(), Currency.WZG);
  }

  public static Money pence(long pence) {
    return new Money(pence, Currency.GBP);
  }

  public Money plus(Money other) {
    requireSameCurrency(other);
    return new Money(Math.addExact(minorUnits, other.minorUnits), currency);
  }

  public Money minus(Money other) {
    requireSameCurrency(other);
    return new Money(Math.subtractExact(minorUnits, other.minorUnits), currency);
  }

  public Money times(long factor) {
    return new Money(Math.multiplyExact(minorUnits, factor), currency);
  }

  public Money negate() {
    return new Money(Math.negateExact(minorUnits), currency);
  }

  public boolean isZero() {
    return minorUnits == 0;
  }

  public boolean isPositive() {
    return minorUnits > 0;
  }

  public boolean isNegative() {
    return minorUnits < 0;
  }

  public boolean isGreaterThan(Money other) {
    return compareTo(other) > 0;
  }

  public boolean isLessThan(Money other) {
    return compareTo(other) < 0;
  }

  /**
   * Splits this amount into {@code parts} shares that sum exactly to the original. Leftover minor
   * units go one each to the first shares, so no Knut is ever created or lost.
   */
  public List<Money> allocate(int parts) {
    if (parts <= 0) {
      throw new IllegalArgumentException("parts must be positive");
    }
    long base = minorUnits / parts;
    long remainder = Math.abs(minorUnits % parts);
    long step = minorUnits < 0 ? -1 : 1;
    List<Money> shares = new ArrayList<>(parts);
    for (int i = 0; i < parts; i++) {
      shares.add(new Money(i < remainder ? base + step : base, currency));
    }
    return List.copyOf(shares);
  }

  /** Breaks a wizarding amount into Galleons, Sickles and Knuts. */
  public WizardingCoins toCoins() {
    if (currency != Currency.WZG) {
      throw new IllegalStateException(currency + " has no wizarding coin breakdown");
    }
    return WizardingCoins.fromKnuts(minorUnits);
  }

  /** Human-readable form: {@code 12G 5S 3K} for wizarding gold, {@code £12.34} for pounds. */
  public String format() {
    return switch (currency) {
      case WZG -> toCoins().toString();
      case GBP -> {
        long abs = Math.absExact(minorUnits);
        yield "%s£%d.%02d".formatted(minorUnits < 0 ? "-" : "", abs / 100, abs % 100);
      }
    };
  }

  @Override
  public int compareTo(Money other) {
    requireSameCurrency(other);
    return Long.compare(minorUnits, other.minorUnits);
  }

  @Override
  public String toString() {
    return format() + " " + currency;
  }

  private void requireSameCurrency(Money other) {
    Objects.requireNonNull(other, "other");
    if (currency != other.currency) {
      throw new CurrencyMismatchException(currency, other.currency);
    }
  }
}
