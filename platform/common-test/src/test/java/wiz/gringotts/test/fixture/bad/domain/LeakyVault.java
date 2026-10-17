package wiz.gringotts.test.fixture.bad.domain;

import org.springframework.stereotype.Component;
import wiz.gringotts.test.fixture.bad.infrastructure.VaultTable;

/** Breaks both rules: depends on Spring and on an outbound adapter. */
@Component
public class LeakyVault {

  public VaultTable table() {
    return new VaultTable();
  }
}
