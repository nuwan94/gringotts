package wiz.gringotts.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DomainExceptionTest {

  @Test
  void notFoundDerivesKebabCaseErrorCode() {
    var ex = new NotFoundException("VaultItem", 713);

    assertThat(ex.errorCode()).isEqualTo("vault-item-not-found");
    assertThat(ex).hasMessage("VaultItem 713 not found");
  }

  @Test
  void eventTypeDefaultsToClassName() {
    record VaultOpened(String aggregateId) implements DomainEvent {
      @Override
      public String aggregateType() {
        return "vault";
      }
    }

    assertThat(new VaultOpened("713").eventType()).isEqualTo("VaultOpened");
  }
}
