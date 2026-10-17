package wiz.gringotts.money;

/**
 * A wizarding amount broken down into coins. Components are always non-negative; the sign is held
 * separately so that {@code -1G 2S 3K} means "minus (1 Galleon, 2 Sickles, 3 Knuts)".
 */
public record WizardingCoins(boolean negative, long galleons, int sickles, int knuts) {

  public static final int KNUTS_PER_SICKLE = 29;
  public static final int SICKLES_PER_GALLEON = 17;
  public static final int KNUTS_PER_GALLEON = KNUTS_PER_SICKLE * SICKLES_PER_GALLEON;

  public WizardingCoins {
    if (galleons < 0 || sickles < 0 || knuts < 0) {
      throw new IllegalArgumentException("Coin counts must be non-negative; use the sign flag");
    }
    if (sickles >= SICKLES_PER_GALLEON) {
      throw new IllegalArgumentException("Sickles must be below " + SICKLES_PER_GALLEON);
    }
    if (knuts >= KNUTS_PER_SICKLE) {
      throw new IllegalArgumentException("Knuts must be below " + KNUTS_PER_SICKLE);
    }
  }

  static WizardingCoins fromKnuts(long totalKnuts) {
    boolean negative = totalKnuts < 0;
    // Long.MIN_VALUE cannot be negated; Math.absExact makes that an explicit failure.
    long abs = Math.absExact(totalKnuts);
    long galleons = abs / KNUTS_PER_GALLEON;
    long remainder = abs % KNUTS_PER_GALLEON;
    return new WizardingCoins(
        negative,
        galleons,
        (int) (remainder / KNUTS_PER_SICKLE),
        (int) (remainder % KNUTS_PER_SICKLE));
  }

  long toKnuts() {
    long abs =
        Math.addExact(
            Math.multiplyExact(galleons, KNUTS_PER_GALLEON),
            (long) sickles * KNUTS_PER_SICKLE + knuts);
    return negative ? -abs : abs;
  }

  @Override
  public String toString() {
    return (negative ? "-" : "") + galleons + "G " + sickles + "S " + knuts + "K";
  }
}
