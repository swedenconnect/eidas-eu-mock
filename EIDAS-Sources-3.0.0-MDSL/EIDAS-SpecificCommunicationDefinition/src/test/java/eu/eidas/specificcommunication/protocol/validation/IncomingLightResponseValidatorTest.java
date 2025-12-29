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
 * Test class for {@link IncomingLightResponseValidator}.
 */
public class IncomingLightResponseValidatorTest {

    /**
     * The a string to represent a Light Response
     */
    private static final String LIGHT_RESPONSE = "LIGHT_RESPONSE";

    /**
     * The maximum number of characters for the Light Response
     */
    private static final String LIGHT_RESPONSE_MAX_NUMBER_CHAR = String.valueOf(LIGHT_RESPONSE.length());

    /**
     * Test method for
     * {@link IncomingLightResponseValidator#isInvalid(String)}
     * when the light response has the same number of characters as the maximum allowed.
     *
     * Must succeed.
     */
    @Test
    public void isInvalid() {
        IncomingLightResponseValidator incomingLightRequestValidator =
                new IncomingLightResponseValidator(LIGHT_RESPONSE_MAX_NUMBER_CHAR);

        boolean isInvalid = incomingLightRequestValidator.isInvalid(LIGHT_RESPONSE);

        Assert.assertFalse(isInvalid);
    }

    /**
     * Test method for
     * {@link IncomingLightResponseValidator#isInvalid(String)}
     * when the light response exceeds by 1 the maximum allowed number of characters.
     *
     * Must succeed.
     */
    @Test
    public void isInvalidWhenLightResponseNumberCharsExceedsMaxAllowed() {
        final String lightResponseMaxNumberChar = String.valueOf(LIGHT_RESPONSE.length() - 1);
        IncomingLightRequestValidator incomingLightResponseValidator =
                new IncomingLightRequestValidator(lightResponseMaxNumberChar);

        boolean isInvalid = incomingLightResponseValidator.isInvalid(LIGHT_RESPONSE);

        Assert.assertTrue(isInvalid);
    }

    /**
     * Test method for
     * {@link IncomingLightResponseValidator#isInvalid(String)}
     * when the light request is null.
     *
     * Must succeed.
     */
    @Test
    public void isInvalidWhenLightResponseIsNull() {
        IncomingLightResponseValidator incomingLightResponseValidator =
                new IncomingLightResponseValidator(LIGHT_RESPONSE_MAX_NUMBER_CHAR);

        boolean isInvalid = incomingLightResponseValidator.isInvalid(null);

        Assert.assertTrue(isInvalid);
    }
}