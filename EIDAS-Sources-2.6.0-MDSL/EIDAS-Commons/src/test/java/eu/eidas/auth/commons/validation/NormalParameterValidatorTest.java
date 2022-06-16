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

package eu.eidas.auth.commons.validation;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasParameters;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.MarkerFactory;

import java.util.stream.IntStream;


/**
 * Test class for {@link NormalParameterValidator}
 */
public class NormalParameterValidatorTest {

    private enum GenerationType {
        NORMAL, ATLIMIT, OVERLIMIT
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test validation of a valid response value for a given pattern.
     * <p>
     * Must succeed
     */
    @Test
    public void testValidResponseValueValid() {
        String SAMLResponseValid = generateResponseSaml(GenerationType.NORMAL);

        boolean valid = NormalParameterValidator.paramName(EidasParameterKeys.SAML_RESPONSE).paramValue(SAMLResponseValid).isValid();
        Assert.assertTrue(valid);
    }

    /**
     * Test validation of a valid request value for a given pattern.
     * <p>
     * Must succeed
     */
    @Test
    public void testValidRequestValueValid() {
        String SAMLResponseValid = generateRequestSaml(GenerationType.NORMAL);

        boolean valid = NormalParameterValidator.paramName(EidasParameterKeys.SAML_REQUEST).paramValue(SAMLResponseValid).isValid();
        Assert.assertTrue(valid);
    }

    /**
     * Test validation of a valid response value for a given pattern.
     * <p>
     * Must succeed
     */
    @Test
    public void testParamNameStringValidResponseValueValid() {
        String SAMLResponseValid = generateResponseSaml(GenerationType.NORMAL);

        boolean valid = NormalParameterValidator.paramName("SAMLResponse").paramValue(SAMLResponseValid).isValid();
        Assert.assertTrue(valid);
    }

    /**
     * Test validation of a valid request value for a given pattern.
     * <p>
     * Must succeed
     */
    @Test
    public void testParamNameStringValidRequestValueValid() {
        String SAMLResponseValid = generateRequestSaml(GenerationType.NORMAL);

        boolean valid = NormalParameterValidator.paramName("SAMLRequest").paramValue(SAMLResponseValid).isValid();
        Assert.assertTrue(valid);
    }

    /**
     * Test validation of a valid response value for a given pattern.
     * Length of value is exactly at maximum allowed length
     * <p>
     * Must succeed
     */
    @Test
    public void testValidResponseValueAtMaxLength() {
        String SAMLResponseMaximumLength = generateResponseSaml(GenerationType.ATLIMIT);

        boolean valid = NormalParameterValidator.paramName(EidasParameterKeys.SAML_RESPONSE).paramValue(SAMLResponseMaximumLength).isValid();
        Assert.assertTrue(valid);
    }

    /**
     * Test validation of a valid request value for a given pattern.
     * Length of value is exactly at maximum allowed length
     * <p>
     * Must succeed
     */
    @Test
    public void testValidRequestValueAtMaxLength() {
        String SAMLResponseMaximumLength = generateRequestSaml(GenerationType.ATLIMIT);

        boolean valid = NormalParameterValidator.paramName(EidasParameterKeys.SAML_REQUEST).paramValue(SAMLResponseMaximumLength).isValid();
        Assert.assertTrue(valid);
    }

    /**
     * Test validation of a valid response value for a given pattern.
     * Length of value exceeds maximum allowed length
     * <p>
     * Must succeed
     */
    @Test
    public void testValidResponseValueTooLong() {
        String SAMLResponseExceedsLimit = generateResponseSaml(GenerationType.OVERLIMIT);

        boolean valid = NormalParameterValidator.paramName(EidasParameterKeys.SAML_RESPONSE).paramValue(SAMLResponseExceedsLimit).isValid();
        Assert.assertFalse(valid);
    }

    /**
     * Test validation of a valid request value for a given pattern.
     * Length of value exceeds maximum allowed length
     * <p>
     * Must succeed
     */
    @Test
    public void testValidRequestValueTooLong() {
        String SAMLResponseExceedsLimit = generateRequestSaml(GenerationType.OVERLIMIT);

        boolean valid = NormalParameterValidator.paramName(EidasParameterKeys.SAML_REQUEST).paramValue(SAMLResponseExceedsLimit).isValid();
        Assert.assertFalse(valid);
    }

    /**
     * Test validation of a valid request value for a given pattern.
     * Length of value exceeds maximum allowed length and marker for full message logging is present
     * <p>
     * Must succeed
     */
    @Test
    public void testValidResponseValueTooLongWithMarker() {
        String SAMLResponseExceedsLimit = generateResponseSaml(GenerationType.OVERLIMIT);

        boolean valid = NormalParameterValidator
                .paramName(EidasParameterKeys.SAML_REQUEST)
                .paramValue(SAMLResponseExceedsLimit)
                .marker(MarkerFactory.getMarker("FULL_MESSAGE_EXCHANGE"))
                .isValid();
        Assert.assertFalse(valid);
    }

    /**
     * Test validation of a valid request value for a given pattern.
     * When paramName string is empty
     * <p>
     * Must succeed
     */
    @Test
    public void testParamValueBlank() {
        boolean valid = NormalParameterValidator.paramName(EidasParameterKeys.SAML_REQUEST).paramValue("").isValid();
        Assert.assertFalse(valid);
    }


    /**
     * Test validation of a valid Response value for a given pattern.
     * <p>
     * Must succeed
     */
    @Test
    public void testValidResponseValueBaseValidatorValid() {
        String SAMLResponseValid = generateResponseSaml(GenerationType.NORMAL);
        try {
            NormalParameterValidator.paramName(EidasParameterKeys.SAML_RESPONSE).paramValue(SAMLResponseValid).validate();
        } catch (InvalidParameterEIDASException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test validation of a valid Request value for a given pattern.
     * <p>
     * Must succeed
     */
    @Test
    public void testValidRequestValueBaseValidatorValid() {
        String SAMLResponseValid = generateRequestSaml(GenerationType.NORMAL);
        try {
            NormalParameterValidator.paramName(EidasParameterKeys.SAML_REQUEST).paramValue(SAMLResponseValid).validate();
        } catch (InvalidParameterEIDASException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test validation of a valid value for a given pattern.
     * Length of response value exceeds maximum allowed length
     * Validation must throw InvalidParameterEIDASException
     * <p>
     * Must succeed
     */
    @Test
    public void testValidResponseValueBaseValidatorInValid() {
        String SAMLResponseExceedsLimit = generateResponseSaml(GenerationType.OVERLIMIT);
        try {
            NormalParameterValidator.paramName(EidasParameterKeys.SAML_RESPONSE).paramValue(SAMLResponseExceedsLimit).validate();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof InvalidParameterEIDASException);
        }
    }

    /**
     * Test validation of a valid value for a given pattern.
     * Length of request value exceeds maximum allowed length
     * Validation must throw InvalidParameterEIDASException
     * <p>
     * Must succeed
     */
    @Test
    public void testValidRequestValueBaseValidatorInValid() {
        String SAMLResponseExceedsLimit = generateRequestSaml(GenerationType.OVERLIMIT);
        try {
            NormalParameterValidator.paramName(EidasParameterKeys.SAML_REQUEST).paramValue(SAMLResponseExceedsLimit).validate();
        } catch (Exception e) {
            Assert.assertTrue(e instanceof InvalidParameterEIDASException);
        }
    }

    /**
     * Test validation of a valid value for a given pattern.
     * Length of response value exceeds maximum allowed length but size limit for given key is not set
     * <p>
     * Must succeed
     */
    @Test
    public void testValidResponseValueBaseValidatorMissingKey() {
        String SAMLResponseValid = generateResponseSaml(GenerationType.OVERLIMIT);
        try {
            //In this test, the EidasParameterKeys.SAML_RESPONSE is replaced by EidasParameterKeys.SAML_IN_RESPONSE_TO to simulate a missing configuration.
            NormalParameterValidator.paramName(EidasParameterKeys.SAML_IN_RESPONSE_TO).paramValue(SAMLResponseValid).validate();
        } catch (InvalidParameterEIDASException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test validation of a valid value for a given pattern.
     * Length of request value exceeds maximum allowed length but size limit for given key is not set
     * <p>
     * Must succeed
     */
    @Test
    public void testValidRequestValueBaseValidatorMissingKey() {
        String SAMLResponseValid = generateRequestSaml(GenerationType.OVERLIMIT);
        try {
            //In this test, the EidasParameterKeys.SAML_RESPONSE is replaced by EidasParameterKeys.SAML_IN_RESPONSE_TO to simulate a missing configuration.
            NormalParameterValidator.paramName(EidasParameterKeys.SAML_IN_RESPONSE_TO).paramValue(SAMLResponseValid).validate();
        } catch (InvalidParameterEIDASException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper methods for tests
     * Generates SAMLResponse messages of length below, at, or exceeding maximum character limit based on passed boolean values
     */
    private String generateResponseSaml(GenerationType generationType) {
        StringBuilder builder = new StringBuilder("<samlp:Response xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" " +
                "xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" " +
                "ID=\"_8e8dc5f69a98cc4c1ff3427e5ce34606fd672f91e6\" " +
                "Version=\"2.0\" IssueInstant=\"2014-07-17T01:01:48Z\" " +
                "Destination=\"http://sp.example.com/demo1/index.php?acs\" " +
                "InResponseTo=\"ONELOGIN_4fee3b046395c4e751011e97f8900b5273d56685\">)");
        if (generationType == GenerationType.ATLIMIT || generationType == GenerationType.OVERLIMIT) {
            IntStream.range(0, EidasParameters.getMaxSizeFor(EidasParameterKeys.SAML_RESPONSE) - builder.toString().length() - "</samlp:Response>".length()).forEach((i) -> builder.append('X'));
        }
        if (generationType == GenerationType.OVERLIMIT) {
            builder.append("exceedDataLimit");
        }

        builder.append("</samlp:Response>");
        return builder.toString();
    }

    /**
     * Helper methods for tests
     * Generates SAMLRequest messages of length below, at, or exceeding maximum character limit based on passed boolean values
     */
    private String generateRequestSaml(GenerationType generationType) {
        StringBuilder builder = new StringBuilder("<saml2p:AuthnRequest xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\"\n" +
                "    xmlns:eidas=\"http://eidas.europa.eu/saml-extensions\"\n" +
                "    xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\"\n" +
                "    xmlns:saml2p=\"urn:oasis:names:tc:SAML:2.0:protocol\"\n" +
                "    Consent=\"urn:oasis:names:tc:SAML:2.0:consent:unspecified\"\n" +
                "    Destination=\"http://localhost:8080/EidasNode/ColleagueRequest\"\n" +
                "    ForceAuthn=\"true\"\n" +
                "    ID=\"__g1Ccja1m2p_Gq8YXeC7g0_lF_0lVItLOf4mq-qyzIaGz-86XiqKqsK6qi-jrHl\"\n" +
                "    IsPassive=\"false\"\n" +
                "    IssueInstant=\"2020-07-20T11:30:43.223Z\"\n" +
                "    ProviderName=\"DEMO-SP-CA\"\n" +
                "    Version=\"2.0\"\n" +
                "            >");
        if (generationType == GenerationType.ATLIMIT || generationType == GenerationType.OVERLIMIT) {
            IntStream.range(0, EidasParameters.getMaxSizeFor(EidasParameterKeys.SAML_RESPONSE) - builder.toString().length() - "</saml2p:AuthnRequest>".length()).forEach((i) -> builder.append('X'));
        }
        if (generationType == GenerationType.OVERLIMIT) {
            builder.append("exceedDataLimit");
        }

        builder.append("</saml2p:AuthnRequest>");
        return builder.toString();
    }

}
