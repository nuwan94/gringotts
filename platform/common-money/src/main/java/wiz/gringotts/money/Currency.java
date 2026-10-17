package wiz.gringotts.money;

/**
 * Currencies held at Gringotts. Every currency is stored as a whole number of its smallest unit:
 * Knuts for wizarding gold, pence for Muggle pounds.
 */
public enum Currency {
  /** Wizarding gold. Minor unit is the Knut; 29 Knuts to a Sickle, 17 Sickles to a Galleon. */
  WZG("Wizarding Gold", "Knut"),
  /** Pound sterling. Minor unit is the penny; 100 pence to a pound. */
  GBP("Pound Sterling", "penny");

  private final String displayName;
  private final String minorUnitName;

  Currency(String displayName, String minorUnitName) {
    this.displayName = displayName;
    this.minorUnitName = minorUnitName;
  }

  public String displayName() {
    return displayName;
  }

  public String minorUnitName() {
    return minorUnitName;
  }
}
