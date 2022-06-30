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
package eu.eidas.auth.commons.lang;

import java.util.Locale;

import javax.annotation.Nullable;

/**
 * Static utility methods pertaining to {@link Canonicalizer}s.
 *
 * @since 1.1
 */
public final class Canonicalizers {

    private enum IdemCanonicalizer implements Canonicalizer<Object> {

        INSTANCE;

        @Override
        @Nullable
        public Object canonicalize(@Nullable Object value) {
            return value;
        }
    }

    private enum NullKeyTrimLowerCaseCanonicalizer implements Canonicalizer<String> {

        INSTANCE;

        @Override
        @Nullable
        public String canonicalize(@Nullable String value) {
            if (null == value) {
                return NULL_KEY;
            }
            return value.trim().toLowerCase(Locale.ENGLISH);
        }
    }

    private enum TrimCanonicalizer implements Canonicalizer<String> {

        INSTANCE;

        @Override
        @Nullable
        public String canonicalize(@Nullable String value) {
            if (null == value) {
                return null;
            }
            return value.trim();
        }
    }

    private enum TrimLowerCaseCanonicalizer implements Canonicalizer<String> {

        INSTANCE;

        @Override
        @Nullable
        public String canonicalize(@Nullable String value) {
            if (null == value) {
                return null;
            }
            return value.trim().toLowerCase(Locale.ENGLISH);
        }
    }

    private enum TrimUpperCaseCanonicalizer implements Canonicalizer<String> {

        INSTANCE;

        @Override
        @Nullable
        public String canonicalize(@Nullable String value) {
            if (null == value) {
                return null;
            }
            return value.trim().toUpperCase(Locale.ENGLISH);
        }
    }

    public static final String NULL_KEY = "null";

    /**
     * Generic method returning a correctly-typed idempotent {@code Canonicalizer}.
     *
     * @param <T> the type
     * @return a correctly-typed idempotent {@code Canonicalizer}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Canonicalizer<T> idem() {
        return (Canonicalizer<T>) IdemCanonicalizer.INSTANCE;
    }

    /**
     * Returns a {@code Canonicalizer} which applies {@link String#trim()}.
     *
     * @param <T> the type
     * @return a {@code Canonicalizer} which applies {@link String#trim()}.
     */
    @SuppressWarnings("unchecked")
    public static Canonicalizer<String> trim() {
        return TrimCanonicalizer.INSTANCE;
    }

    /**
     * Returns a {@code Canonicalizer} which applies {@link String#trim()} and {@link String#toLowerCase(Locale)}.
     *
     * @param <T> the type
     * @return a {@code Canonicalizer} which applies {@link String#trim()} and {@link String#toLowerCase(Locale)}.
     */
    @SuppressWarnings("unchecked")
    public static Canonicalizer<String> trimLowerCase() {
        return TrimLowerCaseCanonicalizer.INSTANCE;
    }

    /**
     * Returns a {@code Canonicalizer} which applies {@link String#trim()} and {@link String#toLowerCase(Locale)} and
     * returns the {@link #NULL_KEY} when the value is {@code null}.
     *
     * @param <T> the type
     * @return a {@code Canonicalizer} which applies {@link String#trim()} and {@link String#toLowerCase(Locale)}.
     */
    @SuppressWarnings("unchecked")
    public static Canonicalizer<String> trimLowerCaseWithNullKey() {
        return NullKeyTrimLowerCaseCanonicalizer.INSTANCE;
    }

    /**
     * Returns a {@code Canonicalizer} which applies {@link String#trim()} and {@link String#toUpperCase(Locale)}.
     *
     * @param <T> the type
     * @return a {@code Canonicalizer} which applies {@link String#trim()} and {@link String#toUpperCase(Locale)}.
     */
    @SuppressWarnings("unchecked")
    public static Canonicalizer<String> trimUpperCase() {
        return TrimUpperCaseCanonicalizer.INSTANCE;
    }

    private Canonicalizers() {
    }
}
