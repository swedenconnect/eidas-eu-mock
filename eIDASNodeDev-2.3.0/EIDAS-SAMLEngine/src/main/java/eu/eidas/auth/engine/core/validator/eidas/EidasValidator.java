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
 * limitations under the Licence.
 *
 */

package eu.eidas.auth.engine.core.validator.eidas;

import eu.eidas.engine.exceptions.ValidationException;

/**
 * Eidas validator indicator, bundling common validation methods.
 */
public interface EidasValidator {

    /**
     * Throws a {@link ValidationException} based on the <tt>toValidate</tt> , if the given value is not null , no
     * {@link ValidationException} is thrown, otherwise a {@link ValidationException} is thrown.
     * <p>
     * example usage: <code>validateNotNull(myObject, "myObject cannot be null")</code>
     * </p>
     *
     * @param toValidate object to validate for a non-null value
     * @param message    the message to show in the {@link ValidationException}
     * @throws ValidationException when the object to validate is null
     */
    static void validateNotNull(final Object toValidate, final String message) throws ValidationException {
        validateOK(toValidate != null, message);
    }

    /**
     * Throws a {@link ValidationException} based on the <tt>resultOK</tt> boolean value, if the result is ok (true), no
     * {@link ValidationException} is thrown, if not ok (false) a {@link ValidationException} is thrown.
     * <p>
     * example usage: <code>validateOK(x.equals(y), "x should be equal to y")</code>
     * </p>
     *
     * @param resultOK true if the result is ok, and should not throw the  {@link ValidationException}, false otherwise.
     * @param message  the message to show in the {@link ValidationException}
     * @throws ValidationException when the result is not ok (false)
     */
    static void validateOK(final boolean resultOK, final String message) throws ValidationException {
        if (!resultOK) throw new ValidationException(message);
    }

}
