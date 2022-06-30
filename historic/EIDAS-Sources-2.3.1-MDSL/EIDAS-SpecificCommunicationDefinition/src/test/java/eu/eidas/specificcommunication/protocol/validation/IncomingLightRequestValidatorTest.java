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

package eu.eidas.specificcommunication.protocol.validation;

import org.junit.Assert;
import org.junit.Test;


/**
 * Test class for {@link IncomingLightRequestValidator}.
 */
public class IncomingLightRequestValidatorTest {

    /**
     * The a string to represent a Light Request
     */
    private static final String LIGHT_REQUEST = "LIGHT_REQUEST";

    /**
     * The maximum number of characters for the Light Request
     */
    private static final String LIGHT_REQUEST_MAX_NUMBER_CHAR = String.valueOf(LIGHT_REQUEST.length());

    /**
     * Test method for
     * {@link IncomingLightRequestValidator#isInvalid(String)}
     * when the light request has the same number of characters as the maximum allowed.
     *
     * Must succeed.
     */
    @Test
    public void isInvalid() {
        IncomingLightRequestValidator incomingLightRequestValidator =
                new IncomingLightRequestValidator(LIGHT_REQUEST_MAX_NUMBER_CHAR);

        boolean isInvalid = incomingLightRequestValidator.isInvalid(LIGHT_REQUEST);

        Assert.assertFalse(isInvalid);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidator#isInvalid(String)}
     * when the light request exceeds by 1 the maximum allowed number of characters.
     *
     * Must succeed.
     */
    @Test
    public void isInvalidWhenLightRequestNumberCharsExceedsMaxAllowed() {
        final String lightRequestMaxNumberChar = String.valueOf(LIGHT_REQUEST.length() - 1);
        IncomingLightRequestValidator incomingLightRequestValidator =
                new IncomingLightRequestValidator(lightRequestMaxNumberChar);

        boolean isInvalid = incomingLightRequestValidator.isInvalid(LIGHT_REQUEST);

        Assert.assertTrue(isInvalid);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidator#isInvalid(String)}
     * when the light request is null.
     *
     * Must succeed.
     */
    @Test
    public void isInvalidWhenLightRequestIsNull() {
        IncomingLightRequestValidator incomingLightRequestValidator =
                new IncomingLightRequestValidator(LIGHT_REQUEST_MAX_NUMBER_CHAR);

        boolean isInvalid = incomingLightRequestValidator.isInvalid(null);

        Assert.assertTrue(isInvalid);
    }
}