package wiz.gringotts.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class OutboxAutoConfigurationTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(OutboxAutoConfiguration.class));

  @Test
  void providesWriterWhenDatabaseIsPresent() {
    runner
        .withBean(JdbcClient.class, () -> JdbcClient.create(new DriverManagerDataSource()))
        .run(context -> assertThat(context).hasSingleBean(OutboxWriter.class));
  }

  @Test
  void backsOffWithoutDatabase() {
    runner.run(context -> assertThat(context).doesNotHaveBean(OutboxWriter.class));
  }
}
