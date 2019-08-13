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
 * limitations under the Licence
 */
package eu.eidas.auth.commons.attribute;

import javax.annotation.Nonnull;

/**
 * Marshals and unmarshals a value of type T to and from a {@link java.lang.String}.
 *
 * @param <T> the type of the value
 * @since 1.1
 */
public interface AttributeValueMarshaller<T> {

    /**
     * Marshals the given typed value as a {@link java.lang.String}.
     *
     * @param value the typed value to marshal as a {@link java.lang.String}.
     * @return a {@link java.lang.String} representation of the given typed value.
     * @throws AttributeValueMarshallingException thrown when marshalling an attribute value
     */
    @Nonnull
    String marshal(@Nonnull AttributeValue<T> value) throws AttributeValueMarshallingException;

    /**
     * Unmarshals the given {@link java.lang.String} as a typed value.
     *
     * @param value the {@link java.lang.String} representation to unmarshal to a typed value.
     * @param isNonLatinScriptAlternateVersion indicates whether the provided value is the non-latin script alternate
     * version of another attribute value.
     * @return a typed value corresponding to the given {@link java.lang.String} representation.
     * @throws AttributeValueMarshallingException thrown when unmarshalling an attribute value
     */
    @Nonnull
    AttributeValue<T> unmarshal(@Nonnull String value, boolean isNonLatinScriptAlternateVersion)
            throws AttributeValueMarshallingException;
}
