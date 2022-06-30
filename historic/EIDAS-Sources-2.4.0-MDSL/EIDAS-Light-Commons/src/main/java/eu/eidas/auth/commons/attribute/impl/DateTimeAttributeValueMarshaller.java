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

import java.util.Locale;

import javax.annotation.Nonnull;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;

/**
 * AttributeValueMarshaller for DateTime values.
 *
 * @since 1.1
 */
public final class DateTimeAttributeValueMarshaller implements AttributeValueMarshaller<DateTime> {

    private static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd")
            .withChronology(ISOChronology.getInstance())
            .withLocale(Locale.ENGLISH)
            .withZoneUTC();

    public static String printDateTime(@Nonnull DateTime dateTime) {
        return FORMAT.print(dateTime);
    }

    @Nonnull
    @Override
    public String marshal(@Nonnull AttributeValue<DateTime> value) {
        return printDateTime(value.getValue());
    }

    @Nonnull
    @Override
    public AttributeValue<DateTime> unmarshal(@Nonnull String value, boolean isNonLatinScriptAlternateVersion)
            throws AttributeValueMarshallingException {
        try {
            return new DateTimeAttributeValue(FORMAT.parseDateTime(value));
        } catch (IllegalArgumentException iae) {
            throw new AttributeValueMarshallingException(iae);
        }
    }
}
