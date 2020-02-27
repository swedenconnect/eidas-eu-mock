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
package eu.eidas.auth.commons;

import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.xml.bind.DatatypeConverter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class EidasStringUtil {

    /**
     * Contains the possible values of delimiters for strings with tokens.
     */
    private static final Pattern STRING_SPLITTER = Pattern.compile("[,;]");

    /**
     * Decodes the given {@link DatatypeConverter} String into a byte array.
     *
     * @param base64String the BASE64 String to be decoded.
     * @return The decoded byte array.
     * @see DatatypeConverter#parseBase64Binary
     */
    @Nonnull
    public static byte[] decodeBytesFromBase64(@Nonnull String base64String) {
        return DatatypeConverter.parseBase64Binary(base64String);
    }

    /**
     * Decodes the given {@link DatatypeConverter} String into a byte array.
     *
     * @param base64String the BASE64 String to be decoded.
     * @return The decoded byte array.
     * @see DatatypeConverter#parseBase64Binary
     */
    @Nonnull
    public static String decodeStringFromBase64(@Nonnull String base64String) {
        return toString(decodeBytesFromBase64(base64String));
    }

    /**
     * {@link DatatypeConverter} encodes the given byte array into a BASE64 string.
     *
     * @param bytes the byte array to be encoded.
     * @return The Base64 String of the encoded bytes.
     * @see DatatypeConverter#printBase64Binary
     */
    @Nonnull
    public static String encodeToBase64(@Nonnull byte[] bytes) {
        if (bytes.length == 0) {
            return StringUtils.EMPTY;
        }
        return DatatypeConverter.printBase64Binary(bytes);
    }

    /**
     * {@link DatatypeConverter} encodes the given normal string into a BASE64 string.
     *
     * @param value the value to be encoded.
     * @return The Base64 String of the encoded bytes coming from the given string.
     * @see DatatypeConverter#printBase64Binary
     */
    @Nonnull
    public static String encodeToBase64(@Nonnull String value) {
        return encodeToBase64(getBytes(value));
    }

    /**
     * @param value input String
     * @return the corresponding array of bytes encoded in UTF-8
     */
    @Nonnull
    public static byte[] getBytes(@Nonnull String value) {
        return value.getBytes(Constants.UTF8);
    }

    /**
     * @param bytes input byte array
     * @return a String created from the given bytes, encoded in UTF-8
     */
    @Nonnull
    public static String toString(@Nonnull byte[] bytes) {
        return new String(bytes, Constants.UTF8);
    }

    /**
     * Splits a string that contains several tokens separated by a delimiter defined in {@link EidasStringUtil#STRING_SPLITTER}
     *
     * @param tokens the string containing tokens
     * @return the {@link List} containing the tokens or an empty list if no token is found.
     */
    public static List<String> getTokens (String tokens) {
        if (StringUtils.isNotEmpty(tokens)) {
            return Arrays.asList(STRING_SPLITTER.split(tokens));
        } else {
            return new ArrayList<>();
        }
    }

    private EidasStringUtil() {
    }
}
