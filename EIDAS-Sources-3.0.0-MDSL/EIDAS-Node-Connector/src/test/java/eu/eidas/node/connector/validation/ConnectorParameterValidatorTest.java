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

package eu.eidas.node.connector.validation;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ConnectorParameterValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    /**
     * replace serviceRedirectUrl {@link EidasErrorKey} with service.redirectUrl {@link EidasParameterKeys}
     */
    @Test
    public void validateServiceRedirectUrlEidasErrorKey() {
        validateMaxNormalParameterValidator(
                EidasErrorKey.SERVICE_REDIRECT_URL.toString(),
                EidasErrorKey.SERVICE_REDIRECT_URL,
                300);
    }

    /**
     * replace serviceRedirectUrl {@link EidasErrorKey} with service.redirectUrl {@link EidasParameterKeys}
     */
    @Test
    public void validateServiceRedirectUrlEidasParameterKeys() {
        validateMaxConnectorParameterValidator(
                EidasParameterKeys.EIDAS_SERVICE_REDIRECT_URL,
                EidasErrorKey.SERVICE_REDIRECT_URL,
                300
        );
    }

    /**
     * replace serviceRedirectUrl {@link EidasErrorKey} with service.redirectUrl {@link EidasParameterKeys}
     */
    @Test
    public void validateRelayStateRedirectUrlEidasErrorKey() {
        validateMaxNormalParameterValidator(
                EidasParameterKeys.RELAY_STATE.toString(),
                EidasErrorKey.CONNECTOR_REDIRECT_URL,
                80);
    }

    /**
     * replace serviceRedirectUrl {@link EidasErrorKey} with service.redirectUrl {@link EidasParameterKeys}
     */
    @Test
    public void validateRelayStateRedirectUrlEidasParameterKeys() {
        validateMaxConnectorParameterValidator(
                EidasParameterKeys.RELAY_STATE,
                EidasErrorKey.SPROVIDER_SELECTOR_INVALID_RELAY_STATE,
                80
        );
    }

    /**
     * replace serviceRedirectUrl {@link EidasErrorKey} with service.redirectUrl {@link EidasParameterKeys}
     */
    @Test
    public void validateConnectorRedirectUrlEidasErrorKey() {
        validateMaxNormalParameterValidator(
                EidasErrorKey.CONNECTOR_REDIRECT_URL.toString(),
                EidasErrorKey.CONNECTOR_REDIRECT_URL,
                300);
    }

    /**
     * replace serviceRedirectUrl {@link EidasErrorKey} with service.redirectUrl {@link EidasParameterKeys}
     */
    @Test
    public void validateConnectorRedirectUrlEidasParameterKeys() {
        validateMaxConnectorParameterValidator(
                EidasParameterKeys.EIDAS_CONNECTOR_REDIRECT_URL,
                EidasErrorKey.CONNECTOR_REDIRECT_URL,
                300
        );
    }


    private static void validateMaxConnectorParameterValidator(EidasParameterKeys parameterKey, EidasErrorKey eidasErrorKey, int expectedMaxValue) {
        Assert.assertTrue(ConnectorParameterValidator.paramName(parameterKey)
                .paramValue(stringWithLength(expectedMaxValue))
                .eidasError(eidasErrorKey)
                .isValid());
        Assert.assertFalse(ConnectorParameterValidator.paramName(parameterKey)
                .paramValue(stringWithLength(expectedMaxValue + 1))
                .eidasError(eidasErrorKey)
                .isValid());
    }

    private static void validateMaxNormalParameterValidator(String parameterKey, EidasErrorKey eidasErrorKey, int expectedMaxValue) {
        Assert.assertTrue(NormalParameterValidator.paramName(parameterKey)
                .paramValue(stringWithLength(expectedMaxValue))
                .eidasError(eidasErrorKey)
                .isValid());
        Assert.assertFalse(NormalParameterValidator.paramName(parameterKey)
                .paramValue(stringWithLength(expectedMaxValue + 1))
                .eidasError(eidasErrorKey)
                .isValid());
    }

    @Nonnull
    private static String stringWithLength(int endExclusive) {
        return IntStream.range(0, endExclusive).mapToObj(i -> "c").collect(Collectors.joining(""));
    }
}