package eu.eidas.sp.custom;

public class ResultAttribute {
  private boolean required;
  private String value;
  private String friendlyName;
  private String name;
  private boolean naturalPerson;

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getFriendlyName() {
    return friendlyName;
  }

  public void setFriendlyName(String friendlyName) {
    this.friendlyName = friendlyName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isNaturalPerson() {
    return naturalPerson;
  }

  public void setNaturalPerson(boolean naturalPerson) {
    this.naturalPerson = naturalPerson;
  }
}
