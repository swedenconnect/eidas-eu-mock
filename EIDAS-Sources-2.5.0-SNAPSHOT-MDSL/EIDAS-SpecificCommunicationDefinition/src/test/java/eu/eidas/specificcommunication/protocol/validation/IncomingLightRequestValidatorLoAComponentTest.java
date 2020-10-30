/*
 * Copyright (c) 2020 by European Commission
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

package eu.eidas.specificcommunication.protocol.validation;

import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class IncomingLightRequestValidatorLoAComponentTest {


    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when a single notified level of assurance value is passed
     * <p>
     * Must succeed.
     */
    @Test
    public void passOnNotified() throws SpecificCommunicationException {
        final String lightRequest = createXmlStringLightRequestWithLoas(
                createXmlLevelOfAssurance("notified", ILevelOfAssurance.EIDAS_LOA_HIGH)
        );
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when a single non notified level of assurance value is passed in the correct fields
     * <p>
     * Must succeed.
     */
    @Test
    public void passOnNonNotified() throws SpecificCommunicationException {
        final String lightRequest = createXmlStringLightRequestWithLoas(
                createXmlLevelOfAssurance("nonNotified", "http://non.notified.loa/A")
        );
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when a notified and non notified level of assurance value are passed in the correct fields
     * <p>
     * Must succeed.
     */
    @Test
    public void passOnNotifiedAndNonNotified() throws SpecificCommunicationException {
        final String lightRequest = createXmlStringLightRequestWithLoas(
                createXmlLevelOfAssurance("notified", ILevelOfAssurance.EIDAS_LOA_HIGH),
                createXmlLevelOfAssurance("nonNotified", "http://non.notified.loa/A")
        );
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when a notified and multiple non notified level of assurance value are passed in the correct fields
     * <p>
     * Must succeed.
     */
    @Test
    public void passOnNotifiedAndMultipleNonNotified() throws SpecificCommunicationException {
        final String lightRequest = createXmlStringLightRequestWithLoas(
                createXmlLevelOfAssurance("notified", ILevelOfAssurance.EIDAS_LOA_HIGH),
                createXmlLevelOfAssurance("nonNotified", "http://non.notified.loa/A"),
                createXmlLevelOfAssurance("nonNotified", "http://non.notified.loa/B"),
                createXmlLevelOfAssurance("nonNotified", "http://non.notified.loa/C")
        );
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when a notified and multiple non notified level of assurance value are passed in the correct fields
     * <p>
     * Must succeed.
     */
    @Test
    public void passOnNotifiedLowAndMultipleNonNotified() throws SpecificCommunicationException {
        final String lightRequest = createXmlStringLightRequestWithLoas(
                createXmlLevelOfAssurance("notified", ILevelOfAssurance.EIDAS_LOA_LOW),
                createXmlLevelOfAssurance("nonNotified", "http://non.notified.loa/A"),
                createXmlLevelOfAssurance("nonNotified", "http://non.notified.loa/B"),
                createXmlLevelOfAssurance("nonNotified", "http://non.notified.loa/C")
        );
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when a non notified level of assurance value are passed in the incorrect field
     * <p>
     * Must fail.
     */
    @Test
    public void failOnWrongTypeInSingleNotifiedField() throws SpecificCommunicationException {
        expectedException.expect(SpecificCommunicationException.class);
        expectedException.expectMessage(IncomingLightRequestValidatorLoAComponent.ERROR_WRONG_TYPE_NOTIFIED);
        final String lightRequest = createXmlStringLightRequestWithLoas(
                createXmlLevelOfAssurance("notified", "http://non.notified.loa/A")
        );
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when a non notified level of assurance value are passed in the incorrect field
     * <p>
     * Must fail.
     */
    @Test
    public void failOnWrongTypeInNotifiedField() throws SpecificCommunicationException {
        expectedException.expect(SpecificCommunicationException.class);
        expectedException.expectMessage(IncomingLightRequestValidatorLoAComponent.ERROR_WRONG_TYPE_NOTIFIED);
        final String lightRequest = createXmlStringLightRequestWithLoas(
                createXmlLevelOfAssurance("notified", "http://non.notified.loa/A"),
                createXmlLevelOfAssurance("notified", ILevelOfAssurance.EIDAS_LOA_HIGH)
        );
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when a notified level of assurance value are passed in the incorrect field
     * <p>
     * Must fail.
     */
    @Test
    public void failOnWrongTypeInSingleNonNotifiedField() throws SpecificCommunicationException {
        expectedException.expect(SpecificCommunicationException.class);
        expectedException.expectMessage(IncomingLightRequestValidatorLoAComponent.ERROR_WRONG_TYPE_NON_NOTIFIED);
        final String lightRequest = createXmlStringLightRequestWithLoas(
                createXmlLevelOfAssurance("nonNotified", ILevelOfAssurance.EIDAS_LOA_HIGH)
        );
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when a notified level of assurance value are passed in the incorrect field
     * <p>
     * Must fail.
     */
    @Test
    public void failOnWrongTypeInNonNotifiedField() throws SpecificCommunicationException {
        expectedException.expect(SpecificCommunicationException.class);
        expectedException.expectMessage(IncomingLightRequestValidatorLoAComponent.ERROR_WRONG_TYPE_NON_NOTIFIED);
        final String lightRequest = createXmlStringLightRequestWithLoas(
                createXmlLevelOfAssurance("nonNotified", "http://non.notified.loa/A"),
                createXmlLevelOfAssurance("nonNotified", ILevelOfAssurance.EIDAS_LOA_HIGH)
        );
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when no loas in the light request
     * <p>
     * Must fail.
     */
    @Test
    public void failOnNoLoa() throws SpecificCommunicationException {
        expectedException.expect(SpecificCommunicationException.class);
        expectedException.expectMessage(IncomingLightRequestValidatorLoAComponent.ERROR_NO_LOAS_FOUND);
        final String lightRequest = createXmlStringLightRequestWithLoas();
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when two notified LoA's are used in the correct notified field
     * <p>
     * Must fail.
     */
    @Test
    public void failOnDoubleNotifiedField() throws SpecificCommunicationException {
        expectedException.expect(SpecificCommunicationException.class);
        expectedException.expectMessage(IncomingLightRequestValidatorLoAComponent.ERROR_MORE_THEN_ONE_NOTIFIED_LOA);
        final String lightRequest = createXmlStringLightRequestWithLoas(
                createXmlLevelOfAssurance("notified", ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL),
                createXmlLevelOfAssurance("notified", ILevelOfAssurance.EIDAS_LOA_HIGH)
        );
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when a non notified level of assurance value tries to use the prefix in the notified field
     * <p>
     * Must fail.
     */
    @Test
    public void failOnIllegalTypeInNotifiedFieldPrefixCaseTamper() throws SpecificCommunicationException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Loa with EIDAS prefix does not match any Notified Values");
        final String lightRequest = createXmlStringLightRequestWithLoas(
                createXmlLevelOfAssurance("notified", "http://eidas.europa.eu/loa/A")
        );
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when a notified level of assurance value does not respect URI path capitalisation
     * <p>
     * Must fail.
     */
    @Test
    public void failOnIllegalTypeInNotifiedFieldPathCaseTamper() throws SpecificCommunicationException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Loa with EIDAS prefix does not match any Notified Values");
        final String lightRequest = createXmlStringLightRequestWithLoas(
                createXmlLevelOfAssurance("notified", "http://eidas.europa.eu/LoA/HIGH")
        );
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    /**
     * Test method for
     * {@link IncomingLightRequestValidatorLoAComponent#validate(String)}
     * when a notified level of assurance value does not exact string match any of the 3 notified levels of assurance
     * <p>
     * Must fail.
     */
    @Test
    public void failOnIllegalTypeInNotifiedFieldhostnameCaseTamper() throws SpecificCommunicationException {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Loa with EIDAS prefix does not match any Notified Values");
        final String lightRequest = createXmlStringLightRequestWithLoas(
                createXmlLevelOfAssurance("notified", "http://EIDAS.europa.eu/LoA/high")
        );
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }

    @Test
    public void failOnTamperedLevelOfAssurancePosition() throws SpecificCommunicationException {
        expectedException.expect(AssertionError.class);
        final String lightRequest = "<lightRequest xmlns=\"http://cef.eidas.eu/LightRequest\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "              xsi:schemaLocation=\"http://cef.eidas.eu/LightRequest lightRequest.xsd\">\n" +
                "    <citizenCountryCode>ES</citizenCountryCode>\n" +
                "    <id>f5e7e0f5-b9b8-4256-a7d0-4090141b326d</id>\n" +
                "    <issuer>http://localhost:7001/SP/metadata</issuer>\n" +
                "    <nameIdFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</nameIdFormat>\n" +
                "    <providerName><levelOfAssurance type=\"notified\">http://eidas.europa.eu/LoA/high</levelOfAssurance></providerName>\n" +
                "    <spType>public</spType>\n" +
                "    <relayState>MyRelayState</relayState>\n" +
                "</lightRequest>";
        IncomingLightRequestValidatorLoAComponent.validate(lightRequest);
    }


    private String createXmlLevelOfAssurance(String type, String uri) {
        return "<levelOfAssurance type=\"" + type + "\">" + uri + "</levelOfAssurance>";
    }

    private String createXmlStringLightRequestWithLoas(String... levelsOfAssurance) {
        return "<lightRequest xmlns=\"http://cef.eidas.eu/LightRequest\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "              xsi:schemaLocation=\"http://cef.eidas.eu/LightRequest lightRequest.xsd\">\n" +
                "    <citizenCountryCode>ES</citizenCountryCode>\n" +
                "    <id>f5e7e0f5-b9b8-4256-a7d0-4090141b326d</id>\n" +
                "    <issuer>http://localhost:7001/SP/metadata</issuer>\n" +
                "    " + String.join("\n    ", levelsOfAssurance) + "\n" +
                "    <nameIdFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</nameIdFormat>\n" +
                "    <providerName>ProviderName</providerName>\n" +
                "    <spType>public</spType>\n" +
                "    <relayState>MyRelayState</relayState>\n" +
                "</lightRequest>";
    }
}

