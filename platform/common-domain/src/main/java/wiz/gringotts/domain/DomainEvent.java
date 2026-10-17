package wiz.gringotts.domain;

/**
 * Something that happened to an aggregate. Implementations are immutable records; they become the
 * payload of an outbox message and, from there, a Kafka record keyed by {@link #aggregateId()}.
 */
public interface DomainEvent {

  /** Aggregate type, used as the Kafka topic suffix, e.g. {@code account}. */
  String aggregateType();

  /** Aggregate identifier, used as the Kafka key so events for one aggregate stay ordered. */
  String aggregateId();

  /** Event type, e.g. {@code AccountOpened}. Defaults to the implementing class's simple name. */
  default String eventType() {
    return getClass().getSimpleName();
  }
}
