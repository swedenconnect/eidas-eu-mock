/*
 * Copyright 2012 Swedish Agency for Economic and Regional Growth - Tillväxtverket 
 *  		 
 * Licensed under the EUPL, Version 1.1 or ñ as soon they will be approved by the 
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. 
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations 
 * under the Licence.
 */
package eu.eidas.sp.custom;

/**
 * European country names
 */
public enum EuropeCountry {

    EU("EU", "European Union", "European Union", "European Union"),
    BE("BE", "Belgique/België", "Belgium", "Kingdom of Belgium"),
    BG("BG", "България (*)", "Bulgaria", "Republic of Bulgaria"),
    CZ("CZ", "Česká republika", "Czech Republic", "Czech Republic"),
    DK("DK", "Danmark", "Denmark", "Kingdom of Denmark"),
    DE("DE", "Deutschland", "Germany", "Federal Republic of Germany"),
    EE("EE", "Eesti", "Estonia", "Republic of Estonia"),
    IE("IE", "Éire/Ireland", "Ireland", "Ireland"),
    EL("GR", "Ελλάδα (*)", "Greece", "Hellenic Republic"),
    GR("GR", "Ελλάδα (*)", "Greece", "Hellenic Republic"),
    ES("ES", "España", "Spain", "Kingdom of Spain"),
    FR("FR", "France", "France", "French Republic"),
    IT("IT", "Italia", "Italy", "Italian Republic"),
    IS("IS", "Ísland", "Iceland", "Iceland"),
    CY("CY", "Κύπρος/Kıbrıs (*)", "Cyprus", "Republic of Cyprus"),
    LI("LI", "Liechtenstein", "Liechtenstein", "Principality of Liechtenstein"),
    LV("LV", "Latvija", "Latvia", "Republic of Latvia"),
    LT("LT", "Lietuva", "Lithuania", "Republic of Lithuania"),
    LU("LU", "Luxembourg", "Luxembourg", "Grand Duchy of Luxembourg"),
    HU("HU", "Magyarország", "Hungary", "Republic of Hungary"),
    MT("MT", "Malta", "Malta", "Republic of Malta"),
    NL("NL", "Nederland", "Netherlands", "Kingdom of the Netherlands"),
    AT("AT", "Österreich", "Austria", "Republic of Austria"),
    PL("PL", "Polska", "Poland", "Republic of Poland"),
    PT("PT", "Portugal", "Portugal", "Portuguese Republic"),
    RO("RO", "România", "Romania", "Romania"),
    SI("SI", "Slovenija", "Slovenia", "Republic of Slovenia"),
    SK("SK", "Slovensko", "Slovakia", "Slovak Republic"),
    FI("FI", "Suomi/Finland", "Finland", "Republic of Finland"),
    SE("SE", "Sverige", "Sweden", "Kingdom of Sweden"),
    NO("NO", "Norge", "Norway", "Kingdom of Norway"),
    HR("HR", "Hrvatska", "Croatia", "Republic of Croatia"),
    UK("GB", "United Kingdom", "United Kingdom", "United Kingdom of Great Britain and Northern Ireland"),
    GB("GB", "United Kingdom", "United Kingdom", "United Kingdom of Great Britain and Northern Ireland"),
    FO("FO","Føroyar","Faroe Islands","Faroe Islands");
    private final String isoCode;
    private final String shortSrcLangName;
    private final String shortEnglishName;
    private final String officialEnglishName;

    private EuropeCountry(final String isoCode, final String shortSrcLangName,
            final String shortEnglishName, final String officialEnglishName) {
        this.isoCode = isoCode;
        this.officialEnglishName = officialEnglishName;
        this.shortEnglishName = shortEnglishName;
        this.shortSrcLangName = shortSrcLangName;
    }

    public String getIsoCode() {
        return this.isoCode;
    }

    public String getShortSrcLangName() {
        return this.shortSrcLangName;
    }

    public String getShortEnglishName() {
        return this.shortEnglishName;
    }

    public String getOfficialEnglishName() {
        return this.officialEnglishName;
    }
}
