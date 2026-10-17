package wiz.gringotts.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ArchitectureRulesTest {

  private static final String GOOD = "wiz.gringotts.test.fixture.good";
  private static final String BAD = "wiz.gringotts.test.fixture.bad";

  private final JavaClasses good = new ClassFileImporter().importPackages(GOOD);
  private final JavaClasses bad = new ClassFileImporter().importPackages(BAD);

  @Test
  void wellLayeredServicePasses() {
    assertThat(ArchitectureRules.hexagonalLayers(GOOD).evaluate(good).hasViolation()).isFalse();
    assertThat(ArchitectureRules.domainIsFrameworkFree(GOOD).evaluate(good).hasViolation())
        .isFalse();
  }

  @Test
  void domainDependingOnAdapterFails() {
    assertThat(ArchitectureRules.hexagonalLayers(BAD).evaluate(bad).hasViolation()).isTrue();
  }

  @Test
  void domainDependingOnSpringFails() {
    assertThat(ArchitectureRules.domainIsFrameworkFree(BAD).evaluate(bad).hasViolation()).isTrue();
  }
}
