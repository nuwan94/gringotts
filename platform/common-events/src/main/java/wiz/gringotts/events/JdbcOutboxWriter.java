package wiz.gringotts.events;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.databind.json.JsonMapper;
import wiz.gringotts.domain.DomainEvent;

/**
 * Writes outbox rows with plain JDBC. Column names follow Debezium's outbox event router defaults
 * ({@code id}, {@code aggregatetype}, {@code aggregateid}, {@code type}, {@code payload}) so the
 * connector needs no field mapping.
 */
public class JdbcOutboxWriter implements OutboxWriter {

  /** Same key that {@code wiz.gringotts.web.CorrelationIdFilter} writes to the MDC. */
  static final String CORRELATION_ID_MDC_KEY = "correlationId";

  private static final String INSERT =
      """
      INSERT INTO outbox_event
          (id, aggregatetype, aggregateid, type, payload, correlation_id, occurred_at)
      VALUES
          (:id, :aggregateType, :aggregateId, :type, CAST(:payload AS jsonb), :correlationId,
           :occurredAt)
      """;

  private final JdbcClient jdbc;
  private final JsonMapper jsonMapper;
  private final Clock clock;

  public JdbcOutboxWriter(JdbcClient jdbc, JsonMapper jsonMapper, Clock clock) {
    this.jdbc = jdbc;
    this.jsonMapper = jsonMapper;
    this.clock = clock;
  }

  @Override
  public UUID append(DomainEvent event) {
    // Checked explicitly rather than with @Transactional(MANDATORY) so it holds even when this
    // class is not behind a Spring proxy.
    if (!TransactionSynchronizationManager.isActualTransactionActive()) {
      throw new IllegalTransactionStateException(
          "Outbox events must be written inside the transaction that changes the aggregate");
    }
    UUID id = UUID.randomUUID();
    jdbc.sql(INSERT)
        .param("id", id)
        .param("aggregateType", event.aggregateType())
        .param("aggregateId", event.aggregateId())
        .param("type", event.eventType())
        .param("payload", jsonMapper.writeValueAsString(event))
        .param("correlationId", MDC.get(CORRELATION_ID_MDC_KEY))
        .param("occurredAt", OffsetDateTime.now(clock))
        .update();
    return id;
  }
}
