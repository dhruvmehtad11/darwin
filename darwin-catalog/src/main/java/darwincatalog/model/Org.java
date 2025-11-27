package darwincatalog.model;

import lombok.Getter;

@Getter
public enum Org {
  EXAMPLE("example");
  private final String name;

  Org(String name) {
    this.name = name;
  }

  public static Org fromValue(String name) {
    for (Org org : Org.values()) {
      if (org.getName().equalsIgnoreCase(name)) {
        return org;
      }
    }
    throw new IllegalArgumentException("No Org found with name: " + name);
  }

  @Override
  public String toString() {
    return name;
  }
}
