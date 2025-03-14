/*
 * Copyright (c) 2024 by European Commission
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
package eu.eidas.auth.commons.attribute.impl;

import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;

import javax.annotation.Nonnull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
/**
 * AttributeValueMarshaller for LocalDate values.
 *
 * @since 1.1
 */
public final class DateTimeAttributeValueMarshaller implements AttributeValueMarshaller<LocalDate> {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withLocale(Locale.ENGLISH)
            .withZone(java.time.ZoneOffset.UTC);

    public static String printDateTime(@Nonnull LocalDate dateTime) {
        return FORMAT.format(dateTime);
    }

    @Nonnull
    @Override
    public String marshal(@Nonnull AttributeValue<LocalDate> value) {
        return printDateTime(value.getValue());
    }

    @Nonnull
    @Override
    public AttributeValue<LocalDate> unmarshal(@Nonnull String value, boolean isNonLatinScriptAlternateVersion)
            throws AttributeValueMarshallingException {
        try {
            return new DateTimeAttributeValue(LocalDate.parse(value, FORMAT));
        } catch (DateTimeParseException e) {
            throw new AttributeValueMarshallingException(e);
        }
    }
}
