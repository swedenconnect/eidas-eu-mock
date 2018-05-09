package eu.eidas.auth.commons;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * JUnit test for {@link CountryCodes}.
 *
 * @since 1.1
 */
public final class CountryCodesTest {

    /**
     * ISO 3166-1 Alpha 3 Country Codes.
     * See https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3
     */
    private static final List<String> COUNTRIES_ISO_ALPHA_3 = Collections.unmodifiableList(
            Arrays.asList(
            "ABW",// Aruba
            "AFG",// Afghanistan
            "AGO",// Angola
            "AIA",// Anguilla
            "ALA",// \u00C5land Islands
            "ALB",// Albania
            "AND",// Andorra
            "ANT",// Netherlands Antilles
            "ARE",// United Arab Emirates
            "ARG",// Argentina
            "ARM",// Armenia
            "ASM",// American Samoa
            "ATA",// Antarctica
            "ATF",// French Southern Territories
            "ATG",// Antigua and Barbuda
            "AUS",// Australia
            "AUT",// Austria
            "AZE",// Azerbaijan
            "BDI",// Burundi
            "BEL",// Belgium
            "BEN",// Benin
            "BFA",// Burkina Faso
            "BGD",// Bangladesh
            "BGR",// Bulgaria
            "BHR",// Bahrain
            "BHS",// Bahamas
            "BIH",// Bosnia and Herzegovina
            "BLM",// Saint Barth\u00E9lemy
            "BLR",// Belarus
            "BLZ",// Belize
            "BMU",// Bermuda
            "BOL",// Bolivia, Plurinational State of
            "BRA",// Brazil
            "BRB",// Barbados
            "BRN",// Brunei Darussalam
            "BTN",// Bhutan
            "BVT",// Bouvet Island
            "BWA",// Botswana
            "CAF",// Central African Republic
            "CAN",// Canada
            "CCK",// Cocos (Keeling) Islands
            "CHE",// Switzerland
            "CHL",// Chile
            "CHN",// China
            "CIV",// C\u00F4te d'Ivoire
            "CMR",// Cameroon
            "COD",// Congo, the Democratic Republic of the
            "COG",// Congo
            "COK",// Cook Islands
            "COL",// Colombia
            "COM",// Comoros
            "CPV",// Cape Verde
            "CRI",// Costa Rica
            "CUB",// Cuba
            "CXR",// Christmas Island
            "CYM",// Cayman Islands
            "CYP",// Cyprus
            "CZE",// Czech Republic
            "DEU",// Germany
            "DJI",// Djibouti
            "DMA",// Dominica
            "DNK",// Denmark
            "DOM",// Dominican Republic
            "DZA",// Algeria
            "ECU",// Ecuador
            "EGY",// Egypt
            "ERI",// Eritrea
            "ESH",// Western Sahara
            "ESP",// Spain
            "EST",// Estonia
            "ETH",// Ethiopia
            "FIN",// Finland
            "FJI",// Fiji
            "FLK",// Falkland Islands (Malvinas)
            "FRA",// France
            "FRO",// Faroe Islands
            "FSM",// Micronesia, Federated States of
            "GAB",// Gabon
            "GBR",// United Kingdom
            "GEO",// Georgia
            "GGY",// Guernsey
            "GHA",// Ghana
            "GIB",// Gibraltar
            "GIN",// Guinea
            "GLP",// Guadeloupe
            "GMB",// Gambia
            "GNB",// Guinea-Bissau
            "GNQ",// Equatorial Guinea
            "GRC",// Greece
            "GRD",// Grenada
            "GRL",// Greenland
            "GTM",// Guatemala
            "GUF",// French Guiana
            "GUM",// Guam
            "GUY",// Guyana
            "HKG",// Hong Kong
            "HMD",// Heard Island and McDonald Islands
            "HND",// Honduras
            "HRV",// Croatia
            "HTI",// Haiti
            "HUN",// Hungary
            "IDN",// Indonesia
            "IMN",// Isle of Man
            "IND",// India
            "IOT",// British Indian Ocean Territory
            "IRL",// Ireland
            "IRN",// Iran, Islamic Republic of
            "IRQ",// Iraq
            "ISL",// Iceland
            "ISR",// Israel
            "ITA",// Italy
            "JAM",// Jamaica
            "JEY",// Jersey
            "JOR",// Jordan
            "JPN",// Japan
            "KAZ",// Kazakhstan
            "KEN",// Kenya
            "KGZ",// Kyrgyzstan
            "KHM",// Cambodia
            "KIR",// Kiribati
            "KNA",// Saint Kitts and Nevis
            "KOR",// Korea, Republic of
            "KWT",// Kuwait
            "LAO",// Lao People's Democratic Republic
            "LBN",// Lebanon
            "LBR",// Liberia
            "LBY",// Libyan Arab Jamahiriya
            "LCA",// Saint Lucia
            "LIE",// Liechtenstein
            "LKA",// Sri Lanka
            "LSO",// Lesotho
            "LTU",// Lithuania
            "LUX",// Luxembourg
            "LVA",// Latvia
            "MAC",// Macao
            "MAF",// Saint Martin (French part)
            "MAR",// Morocco
            "MCO",// Monaco
            "MDA",// Moldova, Republic of
            "MDG",// Madagascar
            "MDV",// Maldives
            "MEX",// Mexico
            "MHL",// Marshall Islands
            "MKD",// Macedonia, the former Yugoslav Republic of
            "MLI",// Mali
            "MLT",// Malta
            "MMR",// Myanmar
            "MNE",// Montenegro
            "MNG",// Mongolia
            "MNP",// Northern Mariana Islands
            "MOZ",// Mozambique
            "MRT",// Mauritania
            "MSR",// Montserrat
            "MTQ",// Martinique
            "MUS",// Mauritius
            "MWI",// Malawi
            "MYS",// Malaysia
            "MYT",// Mayotte
            "NAM",// Namibia
            "NCL",// New Caledonia
            "NER",// Niger
            "NFK",// Norfolk Island
            "NGA",// Nigeria
            "NIC",// Nicaragua
            "NIU",// Niue
            "NLD",// Netherlands
            "NOR",// Norway
            "NPL",// Nepal
            "NRU",// Nauru
            "NZL",// New Zealand
            "OMN",// Oman
            "PAK",// Pakistan
            "PAN",// Panama
            "PCN",// Pitcairn
            "PER",// Peru
            "PHL",// Philippines
            "PLW",// Palau
            "PNG",// Papua New Guinea
            "POL",// Poland
            "PRI",// Puerto Rico
            "PRK",// Korea, Democratic People's Republic of
            "PRT",// Portugal
            "PRY",// Paraguay
            "PSE",// Palestinian Territory, Occupied
            "PYF",// French Polynesia
            "QAT",// Qatar
            "REU",// R\u00E9union
            "ROU",// Romania
            "RUS",// Russian Federation
            "RWA",// Rwanda
            "SAU",// Saudi Arabia
            "SDN",// Sudan
            "SEN",// Senegal
            "SGP",// Singapore
            "SGS",// South Georgia and the South Sandwich Islands
            "SHN",// Saint Helena, Ascension and Tristan da Cunha
            "SJM",// Svalbard and Jan Mayen
            "SLB",// Solomon Islands
            "SLE",// Sierra Leone
            "SLV",// El Salvador
            "SMR",// San Marino
            "SOM",// Somalia
            "SPM",// Saint Pierre and Miquelon
            "SRB",// Serbia
            "STP",// Sao Tome and Principe
            "SUR",// Suriname
            "SVK",// Slovakia
            "SVN",// Slovenia
            "SWE",// Sweden
            "SWZ",// Swaziland
            "SYC",// Seychelles
            "SYR",// Syrian Arab Republic
            "TCA",// Turks and Caicos Islands
            "TCD",// Chad
            "TGO",// Togo
            "THA",// Thailand
            "TJK",// Tajikistan
            "TKL",// Tokelau
            "TKM",// Turkmenistan
            "TLS",// Timor-Leste
            "TON",// Tonga
            "TTO",// Trinidad and Tobago
            "TUN",// Tunisia
            "TUR",// Turkey
            "TUV",// Tuvalu
            "TWN",// Taiwan, Province of China
            "TZA",// Tanzania, United Republic of
            "UGA",// Uganda
            "UKR",// Ukraine
            "UMI",// United States Minor Outlying Islands
            "URY",// Uruguay
            "USA",// United States
            "UZB",// Uzbekistan
            "VAT",// Holy See (Vatican City State)
            "VCT",// Saint Vincent and the Grenadines
            "VEN",// Venezuela, Bolivarian Republic of
            "VGB",// Virgin Islands, British
            "VIR",// Virgin Islands, U.S.
            "VNM",// Viet Nam
            "VUT",// Vanuatu
            "WLF",// Wallis and Futuna
            "WSM",// Samoa
            "YEM",// Yemen
            "ZAF",// South Africa
            "ZMB",// Zambia
            "ZWE" // Zimbabwe
            ));


    @Test
    public void testNoRegressionInHasCountryCodeAlpha3() throws Exception {
        for (final String isoAlpha3 : COUNTRIES_ISO_ALPHA_3) {
            Locale countryLocale = CountryCodes.getIsoAlpha3ToCountryLocales().get(isoAlpha3);
            if (null == countryLocale) {
                fail("Missing isoAlpha3 country code \"" + isoAlpha3 + "\"");
            }
            boolean hasCountryCodeAlpha3 = CountryCodes.hasCountryCodeAlpha3(countryLocale.getISO3Country());
            if (!hasCountryCodeAlpha3) {
                fail("Missing isoAlpha3 country code \"" + isoAlpha3 + "\"");
            }
        }
    }

    @Test
    public void testAllCountryCodeAlpha3() throws Exception {
        String[] isoAlpha2Countries = Locale.getISOCountries();
        for (final String isoAlpha2Country : isoAlpha2Countries) {
            String countryCodeAlpha3 = CountryCodes.getCountryCodeAlpha3(isoAlpha2Country);
            assertNotNull("\"" + isoAlpha2Country + "\" must exist in the list of ISO alpha 3 country codes",
                          countryCodeAlpha3);
        }
    }

    @Test
    public void testHasCountryCodeAlpha3() throws Exception {
        assertTrue("BEL must have an Alpha3 Code", CountryCodes.hasCountryCodeAlpha3("BEL"));
    }

    @Test
    public void testGetCountryCodeAlpha3() throws Exception {
        assertEquals("BEL", CountryCodes.getCountryCodeAlpha3("BE"));
    }

    @Test
    public void testGetCountryLocaleByAlpha2() throws Exception {
        Locale countryLocale = CountryCodes.getCountryLocale("BE");
        assertEquals("BEL", countryLocale.getISO3Country());
    }

    @Test
    public void testGetCountryLocaleByAlpha3() throws Exception {
        Locale countryLocale = CountryCodes.getCountryLocale("BEL");
        assertEquals("BE", countryLocale.getCountry());
    }
}
