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

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;

/**
 * AttributeValueMarshaller for Integer values.
 *
 * @since 1.1
 */
public final class IntegerAttributeValueMarshaller implements AttributeValueMarshaller<Integer> {

    @Nonnull
    @Override
    public String marshal(@Nonnull AttributeValue<Integer> value) {
        return value.getValue().toString();
    }

    @Nonnull
    @Override
    public AttributeValue<Integer> unmarshal(@Nonnull String value, boolean isNonLatinScriptAlternateVersion)
            throws AttributeValueMarshallingException {
        try {
            return new IntegerAttributeValue(Integer.valueOf(value));
        } catch (NumberFormatException nfe) {
            throw new AttributeValueMarshallingException(nfe);
        }
    }
}
