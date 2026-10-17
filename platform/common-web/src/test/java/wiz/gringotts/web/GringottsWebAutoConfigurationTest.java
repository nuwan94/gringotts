package wiz.gringotts.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

class GringottsWebAutoConfigurationTest {

  private static final AutoConfigurations CONFIG =
      AutoConfigurations.of(GringottsWebAutoConfiguration.class);

  @Test
  void registersBeansInServletApps() {
    new WebApplicationContextRunner()
        .withConfiguration(CONFIG)
        .run(
            context ->
                assertThat(context)
                    .hasSingleBean(CorrelationIdFilter.class)
                    .hasSingleBean(ProblemDetailsExceptionHandler.class));
  }

  @Test
  void backsOffOutsideWebApps() {
    new ApplicationContextRunner()
        .withConfiguration(CONFIG)
        .run(context -> assertThat(context).doesNotHaveBean(CorrelationIdFilter.class));
  }
}
