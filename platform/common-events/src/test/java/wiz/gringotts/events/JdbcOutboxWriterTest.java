package wiz.gringotts.events;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;
import wiz.gringotts.domain.DomainEvent;
import wiz.gringotts.test.GringottsContainers;

@Testcontainers(disabledWithoutDocker = true)
class JdbcOutboxWriterTest {

  record GalleonsDeposited(String accountId, long knuts) implements DomainEvent {
    @Override
    public String aggregateType() {
      return "account";
    }

    @Override
    public String aggregateId() {
      return accountId;
    }
  }

  @Container static PostgreSQLContainer postgres = GringottsContainers.postgres();

  private static final Instant NOW = Instant.parse("1991-07-31T09:00:00Z");

  private static DataSource dataSource;
  private JdbcClient jdbc;
  private TransactionTemplate tx;
  private JdbcOutboxWriter writer;

  @BeforeAll
  static void migrate() {
    dataSource =
        new DriverManagerDataSource(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration-common")
        .load()
        .migrate();
  }

  @BeforeEach
  void setUp() {
    jdbc = JdbcClient.create(dataSource);
    tx = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    writer =
        new JdbcOutboxWriter(jdbc, JsonMapper.builder().build(), Clock.fixed(NOW, ZoneOffset.UTC));
    jdbc.sql("DELETE FROM outbox_event").update();
  }

  @AfterEach
  void clearMdc() {
    MDC.clear();
  }

  @Test
  void writesDebeziumCompatibleRowInsideTransaction() {
    MDC.put(JdbcOutboxWriter.CORRELATION_ID_MDC_KEY, "owl-7");

    UUID id = tx.execute(status -> writer.append(new GalleonsDeposited("acc-713", 493)));

    Map<String, Object> row =
        jdbc.sql(
                "SELECT aggregatetype, aggregateid, type, payload::text AS payload,"
                    + " correlation_id, occurred_at FROM outbox_event WHERE id = ?")
            .param(id)
            .query()
            .singleRow();
    assertThat(row)
        .containsEntry("aggregatetype", "account")
        .containsEntry("aggregateid", "acc-713")
        .containsEntry("type", "GalleonsDeposited")
        .containsEntry("correlation_id", "owl-7");
    assertThat((String) row.get("payload"))
        .contains("\"accountId\": \"acc-713\"")
        .contains("\"knuts\": 493");
    assertThat(
            jdbc.sql("SELECT occurred_at FROM outbox_event").query(OffsetDateTime.class).single())
        .isEqualTo(NOW.atOffset(ZoneOffset.UTC));
  }

  @Test
  void rolledBackTransactionLeavesNoEvent() {
    tx.executeWithoutResult(
        status -> {
          writer.append(new GalleonsDeposited("acc-713", 1));
          status.setRollbackOnly();
        });

    assertThat(jdbc.sql("SELECT count(*) FROM outbox_event").query(Long.class).single()).isZero();
  }
}
