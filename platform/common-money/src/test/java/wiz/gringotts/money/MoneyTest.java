package wiz.gringotts.money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoneyTest {

  @Nested
  class WizardingCurrency {

    @Test
    void oneGalleonIs493Knuts() {
      assertThat(Money.galleons(1, 0, 0)).isEqualTo(Money.knuts(493));
      assertThat(Money.galleons(0, 1, 0)).isEqualTo(Money.knuts(29));
    }

    @ParameterizedTest
    @CsvSource({
      "0, 0G 0S 0K",
      "28, 0G 0S 28K",
      "29, 0G 1S 0K",
      "492, 0G 16S 28K",
      "493, 1G 0S 0K",
      "6311, 12G 13S 18K",
      "-524, -1G 1S 2K",
    })
    void formatsAsCoins(long knuts, String expected) {
      assertThat(Money.knuts(knuts).format()).isEqualTo(expected);
    }

    @Test
    void coinBreakdownRoundTrips() {
      Money money = Money.galleons(713, 16, 28);
      WizardingCoins coins = money.toCoins();

      assertThat(coins.galleons()).isEqualTo(713);
      assertThat(coins.sickles()).isEqualTo(16);
      assertThat(coins.knuts()).isEqualTo(28);
      assertThat(Money.knuts(coins.toKnuts())).isEqualTo(money);
    }

    @Test
    void rejectsNonCanonicalCoins() {
      assertThatThrownBy(() -> Money.galleons(0, 17, 0))
          .isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> Money.galleons(0, 0, 29))
          .isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> Money.galleons(-1, 0, 0))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class MuggleCurrency {

    @ParameterizedTest
    @CsvSource({"0, £0.00", "5, £0.05", "1234, £12.34", "-1234, -£12.34"})
    void formatsAsPounds(long pence, String expected) {
      assertThat(Money.pence(pence).format()).isEqualTo(expected);
    }

    @Test
    void hasNoCoinBreakdown() {
      assertThatThrownBy(() -> Money.pence(100).toCoins())
          .isInstanceOf(IllegalStateException.class);
    }
  }

  @Nested
  class Arithmetic {

    @Test
    void addsAndSubtracts() {
      Money a = Money.galleons(1, 2, 3);
      Money b = Money.galleons(0, 16, 28);

      assertThat(a.plus(b)).isEqualTo(Money.galleons(2, 2, 2));
      assertThat(a.minus(b).minus(a)).isEqualTo(b.negate());
    }

    @Test
    void refusesToMixCurrencies() {
      assertThatThrownBy(() -> Money.knuts(1).plus(Money.pence(1)))
          .isInstanceOf(CurrencyMismatchException.class);
      assertThatThrownBy(() -> Money.knuts(1).compareTo(Money.pence(1)))
          .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void overflowThrowsInsteadOfWrapping() {
      Money max = Money.knuts(Long.MAX_VALUE);

      assertThatThrownBy(() -> max.plus(Money.knuts(1))).isInstanceOf(ArithmeticException.class);
      assertThatThrownBy(() -> max.times(2)).isInstanceOf(ArithmeticException.class);
      assertThatThrownBy(() -> Money.knuts(Long.MIN_VALUE).negate())
          .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void comparesWithinCurrency() {
      assertThat(Money.knuts(10).isGreaterThan(Money.knuts(9))).isTrue();
      assertThat(Money.knuts(-1).isLessThan(Money.zero(Currency.WZG))).isTrue();
      assertThat(Money.knuts(-1).isNegative()).isTrue();
      assertThat(Money.zero(Currency.GBP).isZero()).isTrue();
    }
  }

  @Nested
  class Allocation {

    @Test
    void sharesSumExactlyToOriginal() {
      Money pot = Money.knuts(100);

      assertThat(pot.allocate(3))
          .containsExactly(Money.knuts(34), Money.knuts(33), Money.knuts(33));
    }

    @Test
    void negativeAmountsSpreadRemainderTheSameWay() {
      assertThat(Money.knuts(-100).allocate(3))
          .containsExactly(Money.knuts(-34), Money.knuts(-33), Money.knuts(-33));
    }

    @Test
    void rejectsNonPositiveParts() {
      assertThatThrownBy(() -> Money.knuts(1).allocate(0))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }
}
