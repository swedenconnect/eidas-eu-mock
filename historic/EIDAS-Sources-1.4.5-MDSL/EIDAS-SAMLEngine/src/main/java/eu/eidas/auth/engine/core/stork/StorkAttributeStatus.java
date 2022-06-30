/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.eidas.auth.engine.core.stork;

import java.util.Collection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * AttributeStatus from the STORK spec.
 *
 * @since 1.1
 */
public enum StorkAttributeStatus {

    /**
     * Attribute is Available.
     */
    AVAILABLE("Available"),

    /**
     * Attribute is NotAvailable.
     */
    NOT_AVAILABLE("NotAvailable"),

    /**
     * Attribute is Withheld.
     */
    WITHHELD("Withheld");

    private static final EnumMapper<String, StorkAttributeStatus> MAPPER =
            new EnumMapper<String, StorkAttributeStatus>(new KeyAccessor<String, StorkAttributeStatus>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull StorkAttributeStatus stat) {
                    return stat.getValue();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static StorkAttributeStatus fromString(@Nonnull String val) {
        return MAPPER.fromKey(val);
    }

    @Nonnull
    public static StorkAttributeStatus fromValues(@Nullable Collection<String> values) {
        if (null == values || values.isEmpty()) {
            return NOT_AVAILABLE;
        }
        return AVAILABLE;
    }

    public static EnumMapper<String, StorkAttributeStatus> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String value;

    StorkAttributeStatus(@Nonnull String value) {
        this.value = value;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    @Nonnull
    @Override
    public String toString() {
        return value;
    }
}
