package wiz.gringotts.test.fixture.good.application;

import wiz.gringotts.test.fixture.good.domain.Vault;

public class OpenVault {

  public Vault open(int number) {
    return new Vault(number);
  }
}
