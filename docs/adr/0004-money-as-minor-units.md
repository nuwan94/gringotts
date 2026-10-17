# ADR 0004 — Money is a whole number of minor units

- **Status:** Accepted
- **Date:** 2026-10-18

## Context

Wizarding currency is non-decimal: 29 Knuts make a Sickle, and 17 Sickles make a Galleon (493 Knuts).
Floating point cannot represent money exactly. `BigDecimal` assumes a decimal scale, which does not
fit Galleons, and is easy to misuse with rounding modes.

## Decision

- `wiz.gringotts.money.Money` is a record of `(long minorUnits, Currency currency)`. The minor unit
  is the Knut for `WZG` and the penny for `GBP`.
- Arithmetic is exact: overflow throws `ArithmeticException`, and mixing currencies throws
  `CurrencyMismatchException`. Converting between currencies is the exchange service's job.
- `allocate(n)` splits an amount without losing or creating a Knut. Use it for interest, fees and
  splits.
- APIs and events carry `{ "amount": <minor units>, "currency": "WZG" }`. Formatting such as
  `12G 13S 18K` or `£12.34` is done only at the edges.
- Databases store `BIGINT` minor units next to a currency code column.

## Consequences

- No rounding bugs from binary fractions, and the representation works for any currency
  with a fixed minor unit.
- Interest and exchange calculations must choose an explicit rounding rule and then allocate the
  remainder.
- A `long` holds about 1.87 × 10¹⁶ Galleons, enough even for the Lestrange vault.
