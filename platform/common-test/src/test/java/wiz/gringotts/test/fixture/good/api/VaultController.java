package wiz.gringotts.test.fixture.good.api;

import wiz.gringotts.test.fixture.good.application.OpenVault;

public class VaultController {

  private final OpenVault openVault = new OpenVault();

  public int open(int number) {
    return openVault.open(number).number();
  }
}
