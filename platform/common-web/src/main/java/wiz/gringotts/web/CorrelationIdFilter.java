package wiz.gringotts.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Ensures every request carries a correlation ID. An incoming {@value #HEADER} is reused when it is
 * well-formed, otherwise a new one is generated. The ID is put in the logging MDC under {@value
 * #MDC_KEY} and echoed on the response so callers can quote it to support goblins.
 */
public class CorrelationIdFilter extends OncePerRequestFilter implements Ordered {

  public static final String HEADER = "X-Correlation-Id";
  public static final String MDC_KEY = "correlationId";

  // Bounded and restricted so a caller cannot inject log lines or huge values.
  private static final Pattern VALID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String correlationId = resolve(request.getHeader(HEADER));
    MDC.put(MDC_KEY, correlationId);
    response.setHeader(HEADER, correlationId);
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  private static String resolve(String incoming) {
    if (incoming != null && VALID.matcher(incoming).matches()) {
      return incoming;
    }
    return UUID.randomUUID().toString();
  }
}
