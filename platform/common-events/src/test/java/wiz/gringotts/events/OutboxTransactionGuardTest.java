package wiz.gringotts.events;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.IllegalTransactionStateException;
import tools.jackson.databind.json.JsonMapper;
import wiz.gringotts.domain.DomainEvent;

/** Runs without Docker: the guard trips before the database is touched. */
class OutboxTransactionGuardTest {

  record VaultOpened(String aggregateId) implements DomainEvent {
    @Override
    public String aggregateType() {
      return "vault";
    }
  }

  @Test
  void refusesToWriteOutsideTransaction() {
    var writer =
        new JdbcOutboxWriter(
            JdbcClient.create(new DriverManagerDataSource()),
            JsonMapper.builder().build(),
            Clock.systemUTC());

    assertThatThrownBy(() -> writer.append(new VaultOpened("713")))
        .isInstanceOf(IllegalTransactionStateException.class);
  }
}
