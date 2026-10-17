package wiz.gringotts.test;

import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Container definitions shared by every integration test, pinned to the same images as {@code
 * deploy/compose}, so tests run against what developers run locally.
 *
 * <p>In a Spring Boot test, expose one as a {@code @Bean @ServiceConnection} or a static
 * {@code @Container @ServiceConnection} field.
 */
public final class GringottsContainers {

  public static final DockerImageName POSTGRES = DockerImageName.parse("postgres:17.11-alpine");
  public static final DockerImageName KAFKA = DockerImageName.parse("apache/kafka:4.3.1");

  private GringottsContainers() {}

  public static PostgreSQLContainer postgres() {
    return new PostgreSQLContainer(POSTGRES)
        .withDatabaseName("gringotts")
        .withUsername("goblin")
        .withPassword("goblin");
  }

  public static KafkaContainer kafka() {
    return new KafkaContainer(KAFKA);
  }
}
