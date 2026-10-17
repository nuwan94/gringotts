package wiz.gringotts.events;

import java.util.UUID;
import wiz.gringotts.domain.DomainEvent;

/**
 * Records a domain event in the service's outbox table, in the same database transaction as the
 * state change that caused it. Debezium tails the table and publishes each row to Kafka, so an
 * event is published if and only if its transaction commits.
 */
public interface OutboxWriter {

  /**
   * @return the event ID, which consumers use for de-duplication
   * @throws org.springframework.transaction.IllegalTransactionStateException if no transaction is
   *     active
   */
  UUID append(DomainEvent event);
}
