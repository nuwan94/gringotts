package wiz.gringotts.test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;

import com.tngtech.archunit.lang.ArchRule;

/**
 * The hexagonal layout every service follows, as executable rules. A service's architecture test is
 * two lines:
 *
 * <pre>{@code
 * @AnalyzeClasses(packages = "wiz.gringotts.ledger")
 * class ArchitectureTest {
 *   @ArchTest ArchRule layers = ArchitectureRules.hexagonalLayers("wiz.gringotts.ledger");
 *   @ArchTest ArchRule pureDomain = ArchitectureRules.domainIsFrameworkFree("wiz.gringotts.ledger");
 * }
 * }</pre>
 *
 * Layers under {@code basePackage}: {@code domain} (model and rules), {@code application} (use
 * cases and ports), {@code api} (inbound adapters: REST, Kafka listeners) and {@code
 * infrastructure} (outbound adapters: persistence, clients).
 */
public final class ArchitectureRules {

  private ArchitectureRules() {}

  /** Dependencies point inwards: adapters → application → domain, and adapters never meet. */
  public static ArchRule hexagonalLayers(String basePackage) {
    return onionArchitecture()
        .domainModels(basePackage + ".domain..")
        .applicationServices(basePackage + ".application..")
        .adapter("api", basePackage + ".api..")
        .adapter("infrastructure", basePackage + ".infrastructure..")
        .withOptionalLayers(true);
  }

  /** The domain model is plain Java so it can be tested without a framework. */
  public static ArchRule domainIsFrameworkFree(String basePackage) {
    return noClasses()
        .that()
        .resideInAPackage(basePackage + ".domain..")
        .should()
        .dependOnClassesThat()
        .resideInAnyPackage(
            "org.springframework..",
            "jakarta.persistence..",
            "jakarta.servlet..",
            "tools.jackson..",
            "com.fasterxml.jackson..",
            "org.apache.kafka..")
        .because("the domain model must stay framework-free");
  }
}
