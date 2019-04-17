/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */
package eu.eidas.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

/**
 * Simple static methods to be called at the start of your own methods to verify correct arguments and state. This
 * allows constructs such as
 * <pre>
 *     if (null == reference) {
 *       throw new IllegalArgumentException("reference cannot be null");
 *     }
 * </pre>
 * <p/>
 * to be replaced with the more compact
 * <pre>
 *     checkNotNull(reference, "reference");
 * </pre>
 * <p/>
 * Note that the sense of the expression is inverted; with {@code Preconditions} you declare what you expect to be
 * <i>true</i>, just as you do with an <a href="http://java.sun.com/j2se/1.5.0/docs/guide/language/assert.html"> {@code
 * assert}</a> or a JUnit {@code assertTrue} call.
 * <p/>
 * Take care not to confuse precondition checking with other similar types of checks! Precondition exceptions --
 * including those provided here, but also {@link IndexOutOfBoundsException}, {@link java.util.NoSuchElementException},
 * {@link UnsupportedOperationException} and others -- are used to signal that the <i>calling method</i> has made an
 * error. This tells the caller that it should not have invoked the method when it did, with the arguments it did, or
 * perhaps ever. Postcondition or other invariant failures should not throw these types of exceptions.
 * <p/>
 * See the Guava User Guide on <a href= "http://code.google.com/p/guava-libraries/wiki/PreconditionsExplained"> using
 * {@code Preconditions}</a>.
 *
 * @see com.google.common.base.Preconditions
 * @since 2013-10-04
 */
public final class Preconditions {

    private interface Sizer<T> {

        int size(@Nonnull T t);
    }

    private static final Sizer<CharSequence> CHAR_SEQUENCE_SIZER = new Sizer<CharSequence>() {

        @Override
        public int size(@Nonnull CharSequence charSequence) {
            return charSequence.length();
        }
    };

    private static final Sizer<Collection<?>> COLLECTION_SIZER = new Sizer<Collection<?>>() {

        @Override
        public int size(@Nonnull Collection<?> collection) {
            return collection.size();
        }
    };

    private static final Sizer<Map<?, ?>> MAP_SIZER = new Sizer<Map<?, ?>>() {

        @Override
        public int size(@Nonnull Map<?, ?> map) {
            return map.size();
        }
    };

    private static final Sizer<Object[]> OBJECT_ARRAY_SIZER = new Sizer<Object[]>() {

        @Override
        public int size(@Nonnull Object[] array) {
            return array.length;
        }
    };

    private static final Sizer<byte[]> BYTE_ARRAY_SIZER = new Sizer<byte[]>() {

        @Override
        public int size(@Nonnull byte[] array) {
            return array.length;
        }
    };

    /**
     * Ensures that a {@code CharSequence} passed as a parameter to the calling method is not shorter than a given
     * minimum length and not greater than a maximum length.
     *
     * @param reference a {@code CharSequence}
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param minLength the minimum length allowed
     * @param maxLength the maximum length allowed
     * @return the non-null {@code CharSequence} that was validated
     * @throws IllegalArgumentException if the {@code CharSequence} is shorter than the given {@code minLength} or
     * longer than the given {@code maxLength}
     */
    @Nonnull
    public static CharSequence checkLength(CharSequence reference,
                                           @Nonnull String referenceName,
                                           int minLength,
                                           int maxLength) throws IllegalArgumentException {
        return checkLength(reference, referenceName, minLength, maxLength, CHAR_SEQUENCE_SIZER);
    }

    /**
     * Ensures that a {@code Collection} passed as a parameter to the calling method is not shorter than a given minimum
     * length and not greater than a maximum length.
     *
     * @param reference a {@code Collection}
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param minLength the minimum length allowed
     * @param maxLength the maximum length allowed
     * @return the non-null {@code Collection} that was validated
     * @throws IllegalArgumentException if the {@code Collection} is shorter than the given {@code minLength} or longer
     * than the given {@code maxLength}
     */
    @Nonnull
    public static <T, C extends Collection<T>> C checkLength(C reference,
                                                             @Nonnull String referenceName,
                                                             int minLength,
                                                             int maxLength) throws IllegalArgumentException {
        return (C) checkLength(reference, referenceName, minLength, maxLength, COLLECTION_SIZER);
    }

    /**
     * Ensures that a {@code Map} passed as a parameter to the calling method is not shorter than a given minimum length
     * and not greater than a maximum length.
     *
     * @param reference a {@code Map}
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param minLength the minimum length allowed
     * @param maxLength the maximum length allowed
     * @return the non-null {@code Map} that was validated
     * @throws IllegalArgumentException if the {@code Map} is shorter than the given {@code minLength} or longer than
     * the given {@code maxLength}
     */
    @Nonnull
    public static <K, V, C extends Map<K, V>> C checkLength(C reference,
                                                            @Nonnull String referenceName,
                                                            int minLength,
                                                            int maxLength) throws IllegalArgumentException {
        return (C) checkLength(reference, referenceName, minLength, maxLength, MAP_SIZER);
    }

    /**
     * Ensures that an array passed as a parameter to the calling method is not shorter than a given minimum length and
     * not greater than a maximum length.
     *
     * @param reference an array
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param minLength the minimum length allowed
     * @param maxLength the maximum length allowed
     * @return the non-null array that was validated
     * @throws IllegalArgumentException if the array is shorter than the given {@code minLength} or longer than the
     * given {@code maxLength}
     */
    @Nonnull
    public static <T> T[] checkLength(T[] reference, @Nonnull String referenceName, int minLength, int maxLength)
            throws IllegalArgumentException {
        return (T[]) checkLength(reference, referenceName, minLength, maxLength, OBJECT_ARRAY_SIZER);
    }

    /**
     * Ensures that an array passed as a parameter to the calling method is not shorter than a given minimum length and
     * not greater than a maximum length.
     *
     * @param reference an array
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param minLength the minimum length allowed
     * @param maxLength the maximum length allowed
     * @return the non-null array that was validated
     * @throws IllegalArgumentException if the array is shorter than the given {@code minLength} or longer than the
     * given {@code maxLength}
     */
    @Nonnull
    public static byte[] checkLength(byte[] reference, @Nonnull String referenceName, int minLength, int maxLength)
            throws IllegalArgumentException {
        return checkLength(reference, referenceName, minLength, maxLength, BYTE_ARRAY_SIZER);
    }

    @Nonnull
    private static <T> T checkLength(T reference,
                                     @Nonnull String referenceName,
                                     int minLength,
                                     int maxLength,
                                     Sizer<T> sizer) throws IllegalArgumentException {

        if (minLength > maxLength) {
            throw new IllegalArgumentException("minLength cannot be greater than maxLength");
        }
        checkNotShorter(reference, referenceName, minLength, sizer);
        checkNotLonger(reference, referenceName, maxLength, sizer);

        return reference;
    }

    /**
     * Ensures that a {@code String} passed as a parameter to the calling method is not blank.
     *
     * @param reference a {@code String}
     * @param referenceName the {@code String} name which will appear in the exception message
     * @return the not-blank {@code String} that was validated
     * @throws IllegalArgumentException if {@code reference} is null, empty or blank
     */
    @Nonnull
    public static String checkNotBlank(String reference, @Nonnull String referenceName)
            throws IllegalArgumentException {
        if (StringUtils.isBlank(reference)) {
            throw new IllegalArgumentException(referenceName + " cannot be null, empty or blank");
        }

        return reference;
    }

    /**
     * Ensures that an array reference passed as a parameter to the calling method is neither null, empty nor blank.
     *
     * @param reference an array reference
     * @param referenceName the array name which will appear in the exception message
     * @return the non-null array reference that was validated
     * @throws IllegalArgumentException if {@code reference} is null or empty
     */
    @Nonnull
    public static char[] checkNotBlank(char[] reference, @Nonnull String referenceName)
            throws IllegalArgumentException {
        checkNotEmpty(reference, referenceName);
        for (char ch : reference) {
            if (!Character.isWhitespace(ch) && ch != '\0') {
                return reference;
            }
        }
        throw new IllegalArgumentException(referenceName + " cannot be blank");
    }

    /**
     * Ensures that a {@code String} (involved in the state of the calling instance) is not blank.
     *
     * @param reference a {@code String}
     * @param referenceName the {@code String} name which will appear in the exception message
     * @return the not-blank {@code String} that was validated
     * @throws IllegalStateException if {@code reference} is null, empty or blank
     */
    @Nonnull
    public static String checkNotBlankState(String reference, @Nonnull String referenceName)
            throws IllegalStateException {
        if (StringUtils.isBlank(reference)) {
            throw new IllegalStateException(referenceName + " cannot be null, empty or blank");
        }

        return reference;
    }

    /**
     * Ensures that an array reference passed as a parameter to the calling method is neither null nor empty.
     *
     * @param reference an array reference
     * @param referenceName the array name which will appear in the exception message
     * @return the non-null array reference that was validated
     * @throws IllegalArgumentException if {@code reference} is null or empty
     */
    @Nonnull
    public static char[] checkNotEmpty(char[] reference, @Nonnull String referenceName)
            throws IllegalArgumentException {
        if (null == reference) {
            throw new IllegalArgumentException(referenceName + " cannot be null");
        }
        if (reference.length == 0) {
            throw new IllegalArgumentException(referenceName + " cannot be empty");
        }
        return reference;
    }

    /**
     * Ensures that an array reference passed as a parameter to the calling method is neither null nor empty.
     *
     * @param reference an array reference
     * @param referenceName the array name which will appear in the exception message
     * @return the non-null array reference that was validated
     * @throws IllegalArgumentException if {@code reference} is null or empty
     */
    @Nonnull
    public static byte[] checkNotEmpty(byte[] reference, @Nonnull String referenceName) throws IllegalArgumentException {
        if (null == reference) {
            throw new IllegalArgumentException(referenceName + " cannot be null");
        }
        if (reference.length == 0) {
            throw new IllegalArgumentException(referenceName + " cannot be empty");
        }
        return reference;
    }

    /**
     * Ensures that an array reference passed as a parameter to the calling method is neither null nor empty.
     *
     * @param reference an array reference
     * @param referenceName the array name which will appear in the exception message
     * @return the non-null array reference that was validated
     * @throws IllegalArgumentException if {@code reference} is null or empty
     */
    @Nonnull
    public static int[] checkNotEmpty(int[] reference, @Nonnull String referenceName) throws IllegalArgumentException {
        if (null == reference) {
            throw new IllegalArgumentException(referenceName + " cannot be null");
        }
        if (reference.length == 0) {
            throw new IllegalArgumentException(referenceName + " cannot be empty");
        }
        return reference;
    }

    /**
     * Ensures that an array reference passed as a parameter to the calling method is neither null nor empty.
     *
     * @param reference an array reference
     * @param referenceName the array name which will appear in the exception message
     * @return the non-null array reference that was validated
     * @throws IllegalArgumentException if {@code reference} is null or empty
     */
    @Nonnull
    public static <T> T[] checkNotEmpty(T[] reference, @Nonnull String referenceName) throws IllegalArgumentException {
        checkNotNull(reference, referenceName);
        if (reference.length == 0) {
            throw new IllegalArgumentException(referenceName + " cannot be empty");
        }
        return reference;
    }

    /**
     * Ensures that a Collection reference passed as a parameter to the calling method is neither null nor empty.
     *
     * @param reference a Collection reference
     * @param referenceName the Collection name which will appear in the exception message
     * @return the non-null Collection reference that was validated
     * @throws IllegalArgumentException if {@code reference} is null or empty
     */
    @Nonnull
    public static <E> Collection<E> checkNotEmpty(Collection<E> reference, @Nonnull String referenceName)
            throws IllegalArgumentException {
        checkNotNull(reference, referenceName);
        if (reference.isEmpty()) {
            throw new IllegalArgumentException(referenceName + " cannot be empty");
        }
        return reference;
    }

    /**
     * Ensures that a Map reference passed as a parameter to the calling method is neither null nor empty.
     *
     * @param reference a Map reference
     * @param referenceName the Map name which will appear in the exception message
     * @return the non-null Map reference that was validated
     * @throws IllegalArgumentException if {@code reference} is null or empty
     */
    @Nonnull
    public static <K, V> Map<K, V> checkNotEmpty(Map<K, V> reference, @Nonnull String referenceName)
            throws IllegalArgumentException {
        checkNotNull(reference, referenceName);
        if (reference.isEmpty()) {
            throw new IllegalArgumentException(referenceName + " cannot be empty");
        }
        return reference;
    }

    /**
     * Ensures that a {@code CharSequence} passed as a parameter to the calling method is not longer than a given
     * maximum length.
     *
     * @param reference a {@code CharSequence}
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param maxLength the maximum length allowed
     * @return the non-null {@code CharSequence} that was validated
     * @throws IllegalArgumentException if the {@code CharSequence} is longer than the given {@code maxLength}
     */
    @Nonnull
    public static CharSequence checkNotLonger(CharSequence reference, @Nonnull String referenceName, int maxLength)
            throws IllegalArgumentException {
        return checkNotLonger(reference, referenceName, maxLength, CHAR_SEQUENCE_SIZER);
    }

    /**
     * Ensures that a {@code Collection} passed as a parameter to the calling method is not longer than a given maximum
     * length.
     *
     * @param reference a {@code Collection}
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param maxLength the maximum length allowed
     * @return the non-null {@code Collection} that was validated
     * @throws IllegalArgumentException if the {@code Collection} is longer than the given {@code maxLength}
     */
    @Nonnull
    public static <T, C extends Collection<T>> C checkNotLonger(C reference,
                                                                @Nonnull String referenceName,
                                                                int maxLength) throws IllegalArgumentException {
        return (C) checkNotLonger(reference, referenceName, maxLength, COLLECTION_SIZER);
    }

    /**
     * Ensures that a {@code Map} passed as a parameter to the calling method is not longer than a given maximum
     * length.
     *
     * @param reference a {@code Map}
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param maxLength the maximum length allowed
     * @return the non-null {@code Map} that was validated
     * @throws IllegalArgumentException if the {@code Map} is longer than the given {@code maxLength}
     */
    @Nonnull
    public static <K, V, C extends Map<K, V>> C checkNotLonger(C reference,
                                                               @Nonnull String referenceName,
                                                               int maxLength) throws IllegalArgumentException {
        return (C) checkNotLonger(reference, referenceName, maxLength, MAP_SIZER);
    }

    /**
     * Ensures that an array passed as a parameter to the calling method is not longer than a given maximum length.
     *
     * @param reference an array
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param maxLength the maximum length allowed
     * @return the non-null array that was validated
     * @throws IllegalArgumentException if the array is longer than the given {@code maxLength}
     */
    @Nonnull
    public static <T> T[] checkNotLonger(T[] reference, @Nonnull String referenceName, int maxLength)
            throws IllegalArgumentException {
        return (T[]) checkNotLonger(reference, referenceName, maxLength, OBJECT_ARRAY_SIZER);
    }

    /**
     * Ensures that an array passed as a parameter to the calling method is not longer than a given maximum length.
     *
     * @param reference an array
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param maxLength the maximum length allowed
     * @return the non-null array that was validated
     * @throws IllegalArgumentException if the array is longer than the given {@code maxLength}
     */
    @Nonnull
    public static byte[] checkNotLonger(byte[] reference, @Nonnull String referenceName, int maxLength)
            throws IllegalArgumentException {
        return checkNotLonger(reference, referenceName, maxLength, BYTE_ARRAY_SIZER);
    }

    private static <T> T checkNotLonger(T reference,
                                        @Nonnull String referenceName,
                                        int maxLength,
                                        @Nonnull Sizer<T> sizer) throws IllegalArgumentException {
        checkNotNull(reference, referenceName);
        checkNotNegative(maxLength, "maxLength");
        if (sizer.size(reference) > maxLength) {
            throw new IllegalArgumentException(referenceName + " cannot be longer than " + maxLength);
        }

        return reference;
    }

    /**
     * Ensures that a given value is non negative (ie &gt;= 0).
     *
     * @param value the value to check
     * @param referenceName the variable name which will appear in the exception message
     * @return the given {@code value} when valid otherwise throws an IllegalArgumentException.
     * @throws IllegalArgumentException if {@code value} is lower than the given {@code min} or is higher than the given
     * {@code max}.
     * @since 2015-12-14
     */
    public static int checkNotNegative(int value, @Nonnull String referenceName) throws IllegalArgumentException {
        if (value < 0) {
            throw new IllegalArgumentException(referenceName + " cannot be negative (got " + value + ")");
        }
        return value;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @param referenceName the object name which will appear in the exception message
     * @return the non-null reference that was validated
     * @throws IllegalArgumentException if {@code reference} is null
     */
    @Nonnull
    public static <T> T checkNotNull(T reference, @Nonnull String referenceName) throws IllegalArgumentException {
        if (null == reference) {
            throw new IllegalArgumentException(referenceName + " cannot be null");
        }

        return reference;
    }

    /**
     * Ensures that an object reference (involved in the state of the calling instance) is not null.
     *
     * @param reference an object reference
     * @param referenceName the object name which will appear in the exception message
     * @return the non-null reference that was validated
     * @throws IllegalStateException if {@code reference} is null
     */
    @Nonnull
    public static <T> T checkNotNullState(T reference, @Nonnull String referenceName) throws IllegalStateException {
        if (null == reference) {
            throw new IllegalStateException(referenceName + " cannot be null");
        }

        return reference;
    }

    /**
     * Ensures that a {@code CharSequence} passed as a parameter to the calling method is not shorter than a given
     * minimum length.
     *
     * @param reference a {@code CharSequence}
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param minLength the minimum length allowed
     * @return the non-null {@code CharSequence} that was validated
     * @throws IllegalArgumentException if the {@code CharSequence} is shorter than the given {@code minLength}
     */
    @Nonnull
    public static CharSequence checkNotShorter(CharSequence reference, @Nonnull String referenceName, int minLength)
            throws IllegalArgumentException {
        return checkNotShorter(reference, referenceName, minLength, CHAR_SEQUENCE_SIZER);
    }

    /**
     * Ensures that a {@code Collection} passed as a parameter to the calling method is not shorter than a given minimum
     * length.
     *
     * @param reference a {@code Collection}
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param minLength the minimum length allowed
     * @return the non-null {@code Collection} that was validated
     * @throws IllegalArgumentException if the {@code Collection} is shorter than the given {@code minLength}
     */
    @Nonnull
    public static <T, C extends Collection<T>> C checkNotShorter(C reference,
                                                                 @Nonnull String referenceName,
                                                                 int minLength) throws IllegalArgumentException {
        return (C) checkNotShorter(reference, referenceName, minLength, COLLECTION_SIZER);
    }

    /**
     * Ensures that a {@code Map} passed as a parameter to the calling method is not shorter than a given minimum
     * length.
     *
     * @param reference a {@code Map}
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param minLength the minimum length allowed
     * @return the non-null {@code Map} that was validated
     * @throws IllegalArgumentException if the {@code Map} is shorter than the given {@code minLength}
     */
    @Nonnull
    public static <K, V, C extends Map<K, V>> C checkNotShorter(C reference,
                                                                @Nonnull String referenceName,
                                                                int minLength) throws IllegalArgumentException {
        return (C) checkNotShorter(reference, referenceName, minLength, MAP_SIZER);
    }

    /**
     * Ensures that an array passed as a parameter to the calling method is not shorter than a given minimum
     * length.
     *
     * @param reference the array
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param minLength the minimum length allowed
     * @return the non-null {@code Collection} that was validated
     * @throws IllegalArgumentException if the {@code Collection} is shorter than the given {@code minLength}
     */
    @Nonnull
    public static <T> T[] checkNotShorter(T[] reference, @Nonnull String referenceName, int minLength)
            throws IllegalArgumentException {
        return (T[]) checkNotShorter(reference, referenceName, minLength, OBJECT_ARRAY_SIZER);
    }

    /**
     * Ensures that an array passed as a parameter to the calling method is not shorter than a given minimum
     * length.
     *
     * @param reference the array
     * @param referenceName the {@code String} name which will appear in the exception message
     * @param minLength the minimum length allowed
     * @return the non-null {@code Collection} that was validated
     * @throws IllegalArgumentException if the {@code Collection} is shorter than the given {@code minLength}
     */
    @Nonnull
    public static byte[] checkNotShorter(byte[] reference, @Nonnull String referenceName, int minLength)
            throws IllegalArgumentException {
        return checkNotShorter(reference, referenceName, minLength, BYTE_ARRAY_SIZER);
    }

    private static <T> T checkNotShorter(T reference,
                                         @Nonnull String referenceName,
                                         int minLength,
                                         @Nonnull Sizer<T> sizer) throws IllegalArgumentException {
        checkNotNull(reference, referenceName);
        checkNotNegative(minLength, "minLength");
        if (sizer.size(reference) < minLength) {
            throw new IllegalArgumentException(referenceName + " cannot be shorter than " + minLength);
        }

        return reference;
    }

    /**
     * Ensures that a {@code List} of {@code String}s passed as a parameter to the calling method is single-valued i.e.
     * not null and contains exactly one non-blank value.
     *
     * @param referenceList a {@code List} of {@code String}s
     * @param referenceName the {@code String} name which will appear in the exception message
     * @return the reference that was validated
     * @throws IllegalArgumentException if {@code referenceList} is null, empty, contains more than one value or
     * contains a blank value.
     */
    public static List<String> checkSingleValuedList(List<String> referenceList, String referenceName) {
        checkNotNull(referenceList, referenceName);

        if (referenceList.isEmpty()) {
            throw new IllegalArgumentException(referenceName + " cannot be empty");
        }

        if (referenceList.size() > 1) {
            throw new IllegalArgumentException(referenceName + " can only have one value");
        }

        checkNotBlank(referenceList.get(0), referenceName);

        return referenceList;
    }

    /**
     * Ensures that a given value is within the given bounds (greater or equal to the given minimum and lower or eqal to
     * the given maximum).
     *
     * @param value the value to check
     * @param referenceName the variable name which will appear in the exception message
     * @return the given {@code value} when valid otherwise throws an IllegalArgumentException.
     * @throws IllegalArgumentException if {@code value} is lower than the given {@code min} or is higher than the given
     * {@code max}.
     */
    public static int checkWithinBounds(int value, @Nonnull String referenceName, int min, int max)
            throws IllegalArgumentException {
        if (value < min) {
            throw new IllegalArgumentException(referenceName + " cannot be lower than " + min + " (got " + value + ")");
        }
        if (value > max) {
            throw new IllegalArgumentException(
                    referenceName + " cannot be higher than " + max + " (got " + value + ")");
        }
        return value;
    }

    /**
     * Checks if the reference contains only Unicode letters or digits.
     * <p/>
     * {@code null} will throw an {@code IllegalArgumentException}. An empty reference ({@code length()=0} ) will throw
     * an {@code IllegalArgumentException}.
     * <p/>
     * <pre>
     * Preconditions.isAlphanumeric(null)   = IllegalArgumentException
     * Preconditions.isAlphanumeric("")     = IllegalArgumentException
     * Preconditions.isAlphanumeric("  ")   = IllegalArgumentException
     * Preconditions.isAlphanumeric("abc")  = true
     * Preconditions.isAlphanumeric("ab c") = IllegalArgumentException
     * Preconditions.isAlphanumeric("ab2c") = true
     * Preconditions.isAlphanumeric("ab-c") = IllegalArgumentException
     * </pre>
     *
     * @param reference the value to check
     * @param referenceName the variable name which will appear in the exception message
     * @return the given {@code reference} when valid otherwise throws an IllegalArgumentException.
     * @throws IllegalArgumentException if {@code reference} contains non alphanumeric characters.
     */
    public static String isAlphanumeric(@Nullable String reference, @Nonnull String referenceName)
            throws IllegalArgumentException {
        if (null == reference || reference.length() == 0 || !StringUtils.isAlphanumeric(reference)) {
            throw new IllegalArgumentException(referenceName + " is null or contains non alphanumeric characters");
        }
        return reference;
    }

    /**
     * Checks if the reference contains only Unicode letters, digits or space ({@code ' '}).
     * <p/>
     * {@code null} will throw an {@code IllegalArgumentException}. An empty {@code reference} (length()=0) will return
     * {@code reference}.
     * <p/>
     * <pre>
     * Preconditions.isAlphanumericSpace(null)   = IllegalArgumentException
     * Preconditions.isAlphanumericSpace("")     = true
     * Preconditions.isAlphanumericSpace("  ")   = true
     * Preconditions.isAlphanumericSpace("abc")  = true
     * Preconditions.isAlphanumericSpace("ab c") = true
     * Preconditions.isAlphanumericSpace("ab2c") = true
     * Preconditions.isAlphanumericSpace("ab-c") = IllegalArgumentException
     * </pre>
     *
     * @param reference the value to check
     * @param referenceName the variable name which will appear in the exception message
     * @return the given {@code reference} when valid otherwise throws an IllegalArgumentException.
     * @throws IllegalArgumentException if {@code reference} contains non alphanumeric and/or space characters.
     */
    public static String isAlphanumericSpace(@Nullable String reference, @Nonnull String referenceName)
            throws IllegalArgumentException {
        if (!StringUtils.isAlphanumericSpace(reference)) {
            throw new IllegalArgumentException(
                    referenceName + " is null or contains non alphanumeric or space characters");
        }
        return reference;
    }

    private Preconditions() {
        // prevent instantiation
    }
}
