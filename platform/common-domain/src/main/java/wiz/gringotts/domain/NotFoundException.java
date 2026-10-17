package wiz.gringotts.domain;

import java.util.Locale;

/** The requested aggregate does not exist, or the caller may not know that it exists. */
public class NotFoundException extends DomainException {

  /**
   * @param resource aggregate name in PascalCase, e.g. {@code VaultItem}; becomes the error code
   *     {@code vault-item-not-found}
   */
  public NotFoundException(String resource, Object id) {
    super(toKebabCase(resource) + "-not-found", resource + " " + id + " not found");
  }

  private static String toKebabCase(String pascalCase) {
    return pascalCase.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
  }
}
