package wiz.gringotts.events;

import java.time.Clock;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import tools.jackson.databind.json.JsonMapper;

/**
 * Provides an {@link OutboxWriter} to any service with a database. The service must also add {@code
 * classpath:db/migration-common} to {@code spring.flyway.locations} to get the table.
 */
@AutoConfiguration(
    afterName = {
      "org.springframework.boot.jdbc.autoconfigure.JdbcClientAutoConfiguration",
      "org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration"
    })
@ConditionalOnBean(JdbcClient.class)
public class OutboxAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  OutboxWriter outboxWriter(
      JdbcClient jdbcClient, ObjectProvider<JsonMapper> jsonMapper, ObjectProvider<Clock> clock) {
    return new JdbcOutboxWriter(
        jdbcClient,
        jsonMapper.getIfAvailable(() -> JsonMapper.builder().build()),
        clock.getIfAvailable(Clock::systemUTC));
  }
}
