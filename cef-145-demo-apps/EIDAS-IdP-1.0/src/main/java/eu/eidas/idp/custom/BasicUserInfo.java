package eu.eidas.idp.custom;

public class BasicUserInfo {
  private String id, displayName, description;

  public BasicUserInfo() {
  }

  public BasicUserInfo(String id, String displayName, String description) {
    this.id = id;
    this.displayName = displayName;
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
