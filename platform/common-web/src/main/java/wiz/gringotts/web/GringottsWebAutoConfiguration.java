package wiz.gringotts.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/** Wires the shared web conventions into any servlet-based Gringotts service. */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GringottsWebAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  CorrelationIdFilter correlationIdFilter() {
    return new CorrelationIdFilter();
  }

  @Bean
  @ConditionalOnMissingBean
  ProblemDetailsExceptionHandler problemDetailsExceptionHandler() {
    return new ProblemDetailsExceptionHandler();
  }
}
