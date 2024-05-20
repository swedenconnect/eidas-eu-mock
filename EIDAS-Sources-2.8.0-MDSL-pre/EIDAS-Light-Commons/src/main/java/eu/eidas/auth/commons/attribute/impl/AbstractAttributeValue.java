/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.commons.attribute.impl;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlType;

import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.util.Preconditions;

/**
 * Abstract AttributeValue
 *
 * @since 1.1
 */
@XmlType
public abstract class AbstractAttributeValue<T extends Serializable> implements AttributeValue<T> {

    private static final long serialVersionUID = 7154869930698510327L;

    /**
     * @serial
     */
    @Nonnull
    private final T value;

    /**
     * @serial
     */
    private final boolean nonLatinScriptAlternateVersion;

    public AbstractAttributeValue() {
        value = null;
        nonLatinScriptAlternateVersion = false;
    }

    protected AbstractAttributeValue(@Nonnull T val, boolean nonLatinScriptAlternateVer) {
        Preconditions.checkNotNull(val, "value");
        value = val;
        nonLatinScriptAlternateVersion = nonLatinScriptAlternateVer;
    }

    @Nonnull
    @Override
    public T getValue() {
        return value;
    }

    @Override
    public boolean isNonLatinScriptAlternateVersion() {
        return nonLatinScriptAlternateVersion;
    }

    @Override
    @SuppressWarnings("squid:S2097")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || (!AttributeValue.class.isAssignableFrom(o.getClass()))) {
            return false;
        }

        AttributeValue<?> that = (AttributeValue<?>) o;

        if (nonLatinScriptAlternateVersion != that.isNonLatinScriptAlternateVersion()) {
            return false;
        }
        return value.equals(that.getValue());
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + (nonLatinScriptAlternateVersion ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
