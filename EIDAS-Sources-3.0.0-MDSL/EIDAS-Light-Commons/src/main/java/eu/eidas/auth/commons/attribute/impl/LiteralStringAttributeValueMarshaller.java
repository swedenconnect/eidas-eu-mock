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

/**
 * An {@link eu.eidas.auth.commons.attribute.AttributeValueMarshaller} for "literal" {@code String}s which must not be
 * transliterated.
 * <p>
 * <em>Note</em>: using this implementation for an {@link eu.eidas.auth.commons.attribute.AttributeDefinition} where the
 * {@link eu.eidas.auth.commons.attribute.AttributeDefinition#isTransliterationMandatory()} is {@code true} and the
 * supplied value is not in LatinScript would thow an exception in {@link eu.eidas.auth.commons.attribute.ImmutableAttributeMap.Builder#build()}.
 *
 * @since 1.1.1
 */
public final class LiteralStringAttributeValueMarshaller implements AttributeValueMarshaller<String> {

    @Nonnull
    @Override
    public String marshal(@Nonnull AttributeValue<String> value) {
        return value.getValue();
    }

    @Nonnull
    @Override
    public AttributeValue<String> unmarshal(@Nonnull String value, boolean isNonLatinScriptAlternateVersion) {
        return new LiteralStringAttributeValue(value);
    }
}
