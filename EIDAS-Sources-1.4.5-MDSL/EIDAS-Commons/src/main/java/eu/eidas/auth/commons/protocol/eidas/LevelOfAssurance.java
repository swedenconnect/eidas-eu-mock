/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.auth.commons.protocol.eidas;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * Supported Levels of Assurance as per the eIDAS specification.
 */
public enum LevelOfAssurance {

    LOW("http://eidas.europa.eu/LoA/low", 1),

    SUBSTANTIAL("http://eidas.europa.eu/LoA/substantial", 2),

    HIGH("http://eidas.europa.eu/LoA/high", 3);

    private static final EnumMapper<String, LevelOfAssurance> URI_MAPPER =
            new EnumMapper<String, LevelOfAssurance>(new KeyAccessor<String, LevelOfAssurance>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull LevelOfAssurance levelOfAssurance) {
                    return levelOfAssurance.stringValue();
                }
            }, Canonicalizers.trimLowerCase(), values());

    private static final EnumMapper<Integer, LevelOfAssurance> NUMERIC_MAPPER =
            new EnumMapper<Integer, LevelOfAssurance>(new KeyAccessor<Integer, LevelOfAssurance>() {

                @Nonnull
                @Override
                public Integer getKey(@Nonnull LevelOfAssurance levelOfAssurance) {
                    return Integer.valueOf(levelOfAssurance.numericValue());
                }
            }, values());

    @Nullable
    public static LevelOfAssurance fromString(@Nonnull String val) {
        return URI_MAPPER.fromKey(val);
    }

    @Nullable
    public static LevelOfAssurance fromNumeric(int numericValue) {
        return NUMERIC_MAPPER.fromKey(Integer.valueOf(numericValue));
    }

    @Nullable
    public static LevelOfAssurance getLevel(@Nonnull String val) {
        return fromString(val);
    }

    public static EnumMapper<String, LevelOfAssurance> uriMapper() {
        return URI_MAPPER;
    }

    @Nonnull
    private final transient String value;

    private final transient int order;

    LevelOfAssurance(@Nonnull String value, int order) {
        this.value = value;
        this.order = order;
    }

    public int numericValue() {
        return order;
    }

    @Nonnull
    public String stringValue() {
        return value;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
