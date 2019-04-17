/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.eidas.auth.commons;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class contains convenient methods to work with ISO 3166-1 Alpha 3 Country Codes.
 * <p/>
 * Its implementation relies on the JDK {@link java.util.Locale}.
 */
@SuppressWarnings("squid:S1166")
public final class CountryCodes {

    private static final Logger LOG = LoggerFactory.getLogger(CountryCodes.class.getName());

    private static final Map<String, Locale> ISO_ALPHA2_TO_COUNTRY_LOCALES;

    private static final Map<String, Locale> ISO_ALPHA3_TO_COUNTRY_LOCALES;

    static {
        Map<String, Locale> iso2ToCountryLocales = new HashMap<String, Locale>();
        Map<String, Locale> iso3ToCountryLocales = new HashMap<String, Locale>();
        String[] iso2Countries = Locale.getISOCountries();
        for (final String alpha2CountryCode : iso2Countries) {
            Locale countryLocale = new Locale("", alpha2CountryCode);
            try {
                iso2ToCountryLocales.put(alpha2CountryCode, countryLocale);
                String alpha3CountryCode = countryLocale.getISO3Country();
                if (alpha3CountryCode.trim().length() != 0) {
                    iso3ToCountryLocales.put(alpha3CountryCode, countryLocale);
                }
            } catch (MissingResourceException mre) {
                LOG.trace("CountryCodeAlpha3 not available for "+alpha2CountryCode+" "+mre.getMessage());
            }
        }
        ISO_ALPHA2_TO_COUNTRY_LOCALES = Collections.unmodifiableMap(iso2ToCountryLocales);
        ISO_ALPHA3_TO_COUNTRY_LOCALES = Collections.unmodifiableMap(iso3ToCountryLocales);
    }

    /**
     * Private Constructor.
     */
    private CountryCodes() {
    }

    /**
     * Returns the CountryCode (3166-1 alpha3 format) based on the given alpha2 country code or null if it does not
     * exist.
     *
     * @param alpha2CountryCode The alpha2 Country code to search.
     * @return the CountryCode (3166-1 alpha3 format) or null if it does not exist
     * @throws MissingResourceException Throws MissingResourceException if the three-letter country abbreviation is not
     * available for this countryCode.
     * @since 1.1
     */
    public static String getCountryCodeAlpha3(final String alpha2CountryCode) throws MissingResourceException {
        try {
            Locale countryLocale = getCountryLocale(alpha2CountryCode);
            if (null == countryLocale) {
                return null;
            }
            String alpha3Country = countryLocale.getISO3Country();
            if (alpha3Country.trim().length() != 0) {
                return alpha3Country;
            }
        } catch (MissingResourceException mre) {
            LOG.error("CountryCodeAlpha3 not available for "+alpha2CountryCode+" "+mre.getMessage());
        }
        return null;
    }

    /**
     * Returns the Country Locale based on the given ISO alpha2 or alpha3 country code or null if it does not exist.
     *
     * @param isoCountryCode The ISO alpha2 or alpha3 Country code to search.
     * @return the Country Locale or null if it does not exist
     * @throws MissingResourceException Throws MissingResourceException if the three-letter country abbreviation is not
     * available for this countryCode.
     * @since 1.1
     */
    public static Locale getCountryLocale(String isoCountryCode) throws MissingResourceException {
        if (StringUtils.isBlank(isoCountryCode)) {
            return null;
        }
        String countryCode = isoCountryCode.trim();
        if (countryCode.length() == 2) {
            return ISO_ALPHA2_TO_COUNTRY_LOCALES.get(countryCode);
        }
        if (countryCode.length() == 3) {
            return ISO_ALPHA3_TO_COUNTRY_LOCALES.get(countryCode);
        }
        return null;
    }

    /**
     * Returns the Map of ISO Alpha2 country codes to corresponding Java Locales.
     * @return the Map of ISO Alpha2 country codes to corresponding Java Locales.
     * @since 1.1
     */
    public static Map<String, Locale> getIsoAlpha2ToCountryLocales() {
        return ISO_ALPHA2_TO_COUNTRY_LOCALES;
    }

    /**
     * Returns the Map of ISO Alpha3 country codes to corresponding Java Locales.
     * @return the Map of ISO Alpha3 country codes to corresponding Java Locales.
     * @since 1.1
     */
    public static Map<String, Locale> getIsoAlpha3ToCountryLocales() {
        return ISO_ALPHA3_TO_COUNTRY_LOCALES;
    }

    /**
     * Returns {@code true} only if the given alpha3 country code exists, returns {@code false} otherwise.
     *
     * @param alpha3CountryCode The alpha3 Country code to check.
     * @return {@code true} if the alpha3 CountryCode exists, {@code false} otherwise.
     */
    public static boolean hasCountryCodeAlpha3(@Nullable String alpha3CountryCode) {
        //noinspection SimplifiableIfStatement
        if (alpha3CountryCode == null || StringUtils.isBlank(alpha3CountryCode) || alpha3CountryCode.length() != 3) {
            return false;
        }
        return ISO_ALPHA3_TO_COUNTRY_LOCALES.containsKey(alpha3CountryCode);
    }

    /**
     * Returns {@code true} only if the given ISO alpha2 or alpha3 country code exists, returns {@code false}
     * otherwise.
     *
     * @param isoCountryCode The ISO alpha2 or alpha3 Country code to check.
     * @return {@code true} if the isoCountryCode exists, {@code false} otherwise.
     */
    public static boolean isValidIsoCountryCode(@Nullable String isoCountryCode) {
        return null != getCountryLocale(isoCountryCode);
    }

}
