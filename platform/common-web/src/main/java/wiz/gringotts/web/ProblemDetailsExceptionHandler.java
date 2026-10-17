package wiz.gringotts.web;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import wiz.gringotts.domain.BusinessRuleViolationException;
import wiz.gringotts.domain.ConflictException;
import wiz.gringotts.domain.DomainException;
import wiz.gringotts.domain.NotFoundException;

/**
 * Renders every error as RFC 9457 {@code application/problem+json}. Domain exceptions map to 404,
 * 409 or 422 with their stable error code; anything unexpected becomes a 500 that reveals nothing
 * beyond the correlation ID. Spring MVC's own errors (validation, bad JSON, wrong method) are
 * handled by the superclass and get the same correlation ID property.
 */
@RestControllerAdvice
public class ProblemDetailsExceptionHandler extends ResponseEntityExceptionHandler {

  public static final String PROBLEM_TYPE_BASE = "https://gringotts.wiz/problems/";

  private static final Logger log = LoggerFactory.getLogger(ProblemDetailsExceptionHandler.class);

  @ExceptionHandler(DomainException.class)
  public ProblemDetail handleDomain(DomainException ex) {
    HttpStatus status = statusFor(ex);
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
    problem.setType(URI.create(PROBLEM_TYPE_BASE + ex.errorCode()));
    problem.setTitle(status.getReasonPhrase());
    problem.setProperty("errorCode", ex.errorCode());
    return withCorrelationId(problem);
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex) {
    log.error("Unhandled exception", ex);
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "The goblins have encountered an unexpected problem. Quote the correlation ID.");
    problem.setType(URI.create(PROBLEM_TYPE_BASE + "internal-error"));
    problem.setProperty("errorCode", "internal-error");
    return withCorrelationId(problem);
  }

  @Override
  protected ProblemDetail createProblemDetail(
      Exception ex,
      HttpStatusCode status,
      String defaultDetail,
      String detailMessageCode,
      Object[] detailMessageArguments,
      WebRequest request) {
    return withCorrelationId(
        super.createProblemDetail(
            ex, status, defaultDetail, detailMessageCode, detailMessageArguments, request));
  }

  private static HttpStatus statusFor(DomainException ex) {
    return switch (ex) {
      case NotFoundException _ -> HttpStatus.NOT_FOUND;
      case ConflictException _ -> HttpStatus.CONFLICT;
      case BusinessRuleViolationException _ -> HttpStatus.UNPROCESSABLE_CONTENT;
      default -> HttpStatus.BAD_REQUEST;
    };
  }

  private static ProblemDetail withCorrelationId(ProblemDetail problem) {
    String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
    if (correlationId != null) {
      problem.setProperty("correlationId", correlationId);
    }
    return problem;
  }
}
