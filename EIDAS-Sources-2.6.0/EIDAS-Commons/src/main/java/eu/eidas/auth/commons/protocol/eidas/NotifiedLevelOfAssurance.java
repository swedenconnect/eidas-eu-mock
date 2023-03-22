/*
 * Copyright (c) 2020 by European Commission
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
 */

package eu.eidas.auth.commons.protocol.eidas;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.5
 * Supported Notified Levels of Assurance as per the eIDAS specification.
 */
public enum NotifiedLevelOfAssurance {

    LOW("http://eidas.europa.eu/LoA/low", 1),

    SUBSTANTIAL("http://eidas.europa.eu/LoA/substantial", 2),

    HIGH("http://eidas.europa.eu/LoA/high", 3);

    private static final EnumMapper<String, NotifiedLevelOfAssurance> URI_MAPPER =
            new EnumMapper<String, NotifiedLevelOfAssurance>(new KeyAccessor<String, NotifiedLevelOfAssurance>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull NotifiedLevelOfAssurance levelOfAssurance) {
                    return levelOfAssurance.stringValue();
                }
            }, Canonicalizers.trimLowerCase(), values());

    private static final EnumMapper<Integer, NotifiedLevelOfAssurance> NUMERIC_MAPPER =
            new EnumMapper<Integer, NotifiedLevelOfAssurance>(new KeyAccessor<Integer, NotifiedLevelOfAssurance>() {

                @Nonnull
                @Override
                public Integer getKey(@Nonnull NotifiedLevelOfAssurance levelOfAssurance) {
                    return Integer.valueOf(levelOfAssurance.numericValue());
                }
            }, values());

    @Nullable
    public static NotifiedLevelOfAssurance fromString(@Nonnull String val) {
        return URI_MAPPER.fromKey(val);
    }

    @Nullable
    public static NotifiedLevelOfAssurance fromNumeric(int numericValue) {
        return NUMERIC_MAPPER.fromKey(Integer.valueOf(numericValue));
    }

    @Nullable
    public static NotifiedLevelOfAssurance getLevel(@Nonnull String val) {
        return fromString(val);
    }

    public static EnumMapper<String, NotifiedLevelOfAssurance> uriMapper() {
        return URI_MAPPER;
    }

    @Nonnull
    private final transient String value;

    private final transient int order;

    NotifiedLevelOfAssurance(@Nonnull String value, int order) {
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

    @Nonnull
    public List<NotifiedLevelOfAssurance> getHigherLevelsOfAssurance() {
        List<NotifiedLevelOfAssurance> higherLevelsOfAssurance = new ArrayList<>();
        for (NotifiedLevelOfAssurance loa: NotifiedLevelOfAssurance.values()) {
            if (loa.numericValue() > this.numericValue()) {
                higherLevelsOfAssurance.add(loa);
            }
        }
        return higherLevelsOfAssurance;
    }
}
