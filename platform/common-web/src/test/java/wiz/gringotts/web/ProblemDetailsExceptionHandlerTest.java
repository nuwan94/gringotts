package wiz.gringotts.web;

import static org.hamcrest.Matchers.hasLength;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import wiz.gringotts.domain.BusinessRuleViolationException;
import wiz.gringotts.domain.ConflictException;
import wiz.gringotts.domain.NotFoundException;

class ProblemDetailsExceptionHandlerTest {

  @RestController
  static class VaultController {

    @GetMapping("/vaults/{id}")
    String vault(@PathVariable String id) {
      return switch (id) {
        case "missing" -> throw new NotFoundException("Vault", id);
        case "duplicate" -> throw new ConflictException("vault-exists", "Vault already exists");
        case "broke" ->
            throw new BusinessRuleViolationException("insufficient-funds", "Not enough Galleons");
        case "boom" -> throw new IllegalStateException("dragon escaped, secret detail");
        default -> "Vault " + id;
      };
    }
  }

  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    mvc =
        MockMvcBuilders.standaloneSetup(new VaultController())
            .setControllerAdvice(new ProblemDetailsExceptionHandler())
            .addFilters(new CorrelationIdFilter())
            .build();
  }

  @Test
  void notFoundBecomes404Problem() throws Exception {
    mvc.perform(get("/vaults/missing").header(CorrelationIdFilter.HEADER, "owl-42"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.type").value("https://gringotts.wiz/problems/vault-not-found"))
        .andExpect(jsonPath("$.errorCode").value("vault-not-found"))
        .andExpect(jsonPath("$.detail").value("Vault missing not found"))
        .andExpect(jsonPath("$.correlationId").value("owl-42"))
        .andExpect(header().string(CorrelationIdFilter.HEADER, "owl-42"));
  }

  @Test
  void conflictBecomes409() throws Exception {
    mvc.perform(get("/vaults/duplicate"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errorCode").value("vault-exists"));
  }

  @Test
  void businessRuleBecomes422() throws Exception {
    mvc.perform(get("/vaults/broke"))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.errorCode").value("insufficient-funds"));
  }

  @Test
  void unexpectedErrorHidesInternals() throws Exception {
    mvc.perform(get("/vaults/boom"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.errorCode").value("internal-error"))
        .andExpect(
            jsonPath("$.detail")
                .value(
                    "The goblins have encountered an unexpected problem. Quote the correlation"
                        + " ID."))
        .andExpect(jsonPath("$.correlationId", hasLength(36)));
  }

  @Test
  void rejectsMalformedCorrelationIdAndGeneratesOne() throws Exception {
    mvc.perform(get("/vaults/713").header(CorrelationIdFilter.HEADER, "bad\nheader"))
        .andExpect(status().isOk())
        .andExpect(header().string(CorrelationIdFilter.HEADER, hasLength(36)));
  }
}
