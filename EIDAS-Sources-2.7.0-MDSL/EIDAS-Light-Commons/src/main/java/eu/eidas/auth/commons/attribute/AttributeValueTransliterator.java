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
package eu.eidas.auth.commons.attribute;

import javax.annotation.Nullable;

import com.ibm.icu.text.Transliterator;

import eu.eidas.auth.commons.lang.Charsets;

/**
 * AttributeValue Transliterator to Latin script.
 *
 * @since 1.1.1
 */
public final class AttributeValueTransliterator {

    // See http://userguide.icu-project.org/transforms/general
    // and http://icu-project.org/charts/comparison/transforms.html

    private static final String TRANSLITERATOR_ID = "Latin; NFD; [:Nonspacing Mark:] Remove; NFC;";

    private static final Transliterator TRANSLITERATOR = Transliterator.getInstance(TRANSLITERATOR_ID);
    private static Transliterator LAX_COMPARATOR = Transliterator.getInstance(TRANSLITERATOR_ID);

    /**
     * Returns {@code true} if and only if the given values are transliteration variants of each other.
     *
     * @param value1 the first value
     * @param value2 the second value
     * @return {@code true} if and only if the given values are transliteration variants of each other.
     */
    public static boolean areTransliterations(@Nullable String value1, @Nullable String value2) {
        if (null == value1 || null == value2) {
            return false;
        }
        if (value1.equals(value2)) {
            return false;
        }
        if (needsTransliteration(value1) ^ needsTransliteration(value2)) {
            return (LAX_COMPARATOR.transliterate(value1).equals(LAX_COMPARATOR.transliterate(value2)));
        }
        return false;
    }

    /**
     * Returns {@code true} if and only if the given value is in Latin script according to Unicode.
     *
     * @param value the value to test
     * @return {@code true} if and only if the given value is in Latin script according to Unicode.
     */
    public static boolean needsTransliteration(@Nullable CharSequence value) {
        return !Charsets.isLatinScript(value);
    }

    /**
     * Transliterates the given value to Latin script according to Unicode.
     *
     * @param value the value to transliterate
     * @return the given value transliterated to Latin script according to Unicode.
     */
    @Nullable
    public static String transliterate(@Nullable String value) {
        if (null == value) {
            return null;
        }
        return TRANSLITERATOR.transliterate(value);
    }

    private AttributeValueTransliterator() {
    }
}
