/*
 * Copyright (c) 2019 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 */
package eu.eidas.auth.commons;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Static helper methods.
 *
 * @deprecated This class has more than one responsibility and relies on a mutable static state with is setup in an
 * awkward way.
 */
@SuppressWarnings("ConstantConditions")
@Deprecated
public class EIDASUtil {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EIDASUtil.class);

    private static final Pattern SEMI_COLON_SEPARATOR_PATTERN = Pattern.compile(";");

    @SuppressWarnings("CollectionDeclaredAsConcreteClass")
    @Nonnull
    static ImmutableMap<String, String> immutableMap(@Nullable Properties properties) {
        if (null == properties || properties.isEmpty()) {
            return ImmutableMap.of();
        }
        return Maps.fromProperties(properties);
    }

    @Nonnull
    static Properties toProperties(@Nonnull ImmutableMap<String, String> immutableMap) {
        Properties properties = new Properties();
        //noinspection UseOfPropertiesAsHashtable
        properties.putAll(immutableMap);
        return properties;
    }

    /**
     * Gets the Eidas error code in the error message if exists!
     *
     * @param errorMessage The message to get the error code if exists;
     * @return the error code if exists. Returns null otherwise.
     */
    public static String getEidasErrorCode(final String errorMessage) {
        if (StringUtils.isNotBlank(errorMessage)
                && errorMessage.indexOf(EIDASValues.ERROR_MESSAGE_SEP.toString()) >= 0) {
            final String[] msgSplitted = errorMessage.split(EIDASValues.ERROR_MESSAGE_SEP.toString());
            if (msgSplitted.length == 2 && StringUtils.isNumeric(msgSplitted[0])) {
                return msgSplitted[0];
            }
        }
        return null;
    }

    /**
     * Gets the Eidas error message in the saml message if exists!
     *
     * @param errorMessage The message to get in the saml message if exists;
     * @return the error message if exists. Returns the original message otherwise.
     */
    public static String getEidasErrorMessage(final String errorMessage) {
        if (StringUtils.isNotBlank(errorMessage)
                && errorMessage.indexOf(EIDASValues.ERROR_MESSAGE_SEP.toString()) >= 0) {
            final String[] msgSplitted = errorMessage.split(EIDASValues.ERROR_MESSAGE_SEP.toString());
            if (msgSplitted.length == 2 && StringUtils.isNumeric(msgSplitted[0])) {
                return msgSplitted[1];
            }
        }
        return errorMessage;
    }

    /**
     * @param values a string containing several chunks separated by ;
     * @return a set of chunks extracted from values
     */
    @Nonnull
    public static Set<String> parseSemicolonSeparatedList(@Nullable String values) {
        Set<String> result = new HashSet<String>();
        if (!StringUtils.isEmpty(values)) {
            String[] valuesArr = SEMI_COLON_SEPARATOR_PATTERN.split(values);
            if (valuesArr != null) {
                for (String value : valuesArr) {
                    value = value.trim();
                    if (!StringUtils.isEmpty(value)) {
                        result.add(value);
                    }
                }
            }
        }
        return result;
    }
}
