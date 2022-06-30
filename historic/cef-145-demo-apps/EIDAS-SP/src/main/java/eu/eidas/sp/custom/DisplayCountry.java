package eu.eidas.sp.custom;

public class DisplayCountry {
  private String langCode;
  private String displayName;
  private String dataContent;
  private String flag;

  public DisplayCountry(String langCode) {
    this.langCode = langCode;
    EuropeCountry europeCountry = null;
    try {
      europeCountry = EuropeCountry.valueOf(langCode);
      flag = "sc-img/flags/"+langCode+".png";
      displayName = europeCountry.getShortEnglishName();
    } catch (Exception ex){
      if (langCode.equals("XE")){
        flag = "sc-img/flags/SE.png";
        displayName = "Sweden Sandbox (" + langCode +")";
      } else {
        flag = "sc-img/flags/EU.png";
        displayName = "Test Country (" + langCode +")";
      }
    }
    dataContent = "<img src='"+ flag + "' class='list-flag-img'> "+ displayName;
  }

  public String getLangCode() {
    return langCode;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getFlag() {
    return flag;
  }

  public String getDataContent() {
    return dataContent;
  }
}
