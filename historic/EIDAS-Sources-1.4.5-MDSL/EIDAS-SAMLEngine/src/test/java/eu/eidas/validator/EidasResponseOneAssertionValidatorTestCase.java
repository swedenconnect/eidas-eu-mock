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

package eu.eidas.validator;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextDecl;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.xml.validation.ValidationException;

import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.core.validator.eidas.EidasResponseOneAssertionValidator;
import eu.eidas.auth.engine.xml.opensaml.BuilderFactoryUtil;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * Functional testing class to {@link eu.eidas.auth.engine.core.validator.eidas.EidasResponseOneAssertionValidator}.
 *
 */

public class EidasResponseOneAssertionValidatorTestCase {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    /**
     * Need to load the EIDASSAMLEngine.
     *
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        ProtocolEngineFactory.getDefaultProtocolEngine("CONF1");
    }

    /**
     * Test method for
     * {@link EidasResponseOneAssertionValidator#validate(Response response)}
     * . Must Succeed.
     */
    @Test
    public void testOneAssertionOneAuthnStatementOneAttributeStatementInResponse() throws ValidationException, EIDASSAMLEngineException {
        Response response = generateTestResponse(1, 1, 1);
        EidasResponseOneAssertionValidator eidasResponseOneAssertionValidator = new EidasResponseOneAssertionValidator();
        eidasResponseOneAssertionValidator.validate(response);
    }

    /**
     * Test method for
     * . Testing with wrong number of Assertion in a Response. Must throw a
     * {@link ValidationException} with a specific message.
     */
    @Test
    public void testTwoAssertionInResponse() throws ValidationException, EIDASSAMLEngineException {
        exception.expect(ValidationException.class);
        exception.expectMessage("Number of Assertion in Response 2, differs from number of allowed ones:1.");
        Response response = generateTestResponse(2, 1, 1);
        EidasResponseOneAssertionValidator eidasResponseOneAssertionValidator = new EidasResponseOneAssertionValidator();
        eidasResponseOneAssertionValidator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseOneAssertionValidator#validate(Response response)}
     * . Testing with wrong number of AttributeStatement in an Assertion. Must throw a
     * {@link ValidationException} with a specific message.
     *
     */
    @Test
    public void testOneAuthnStatementZeroAttributeStatementInResponseAssertion() throws ValidationException, EIDASSAMLEngineException {
        exception.expect(ValidationException.class);
        exception.expectMessage("Number of AttributeStatement 0 in Assertion differs from number of allowed ones:1.");
        Response response = generateTestResponse(1, 1, 0);
        EidasResponseOneAssertionValidator eidasResponseOneAssertionValidator = new EidasResponseOneAssertionValidator();
        eidasResponseOneAssertionValidator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseOneAssertionValidator#validate(Response response)}
     * . Testing with wrong number of AttributeStatement in an Assertion. Must throw a
     * {@link ValidationException} with a specific message.
     *
     */
    @Test
    public void testZeroAuthnStatementOneAttributeStatementInResponseAssertion() throws ValidationException, EIDASSAMLEngineException {
        exception.expect(ValidationException.class);
        exception.expectMessage("Number of AuthnStatement 0 in Assertion differs from number of allowed ones:1.");
        Response response = generateTestResponse(1, 0, 1);
        EidasResponseOneAssertionValidator eidasResponseOneAssertionValidator = new EidasResponseOneAssertionValidator();
        eidasResponseOneAssertionValidator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseOneAssertionValidator#validate(Response response)}
     * . Testing with wrong number of authStatement in Assertion. Must throw a
     * {@link ValidationException} with a specific message.
     *
     */
    @Test
    public void testTwoAuthnStatementOneAttributeStatementInResponseAssertion() throws ValidationException, EIDASSAMLEngineException {
        exception.expect(ValidationException.class);
        exception.expectMessage("Number of AuthnStatement 2 in Assertion differs from number of allowed ones:1.");
        Response response = generateTestResponse(1, 2, 1);
        EidasResponseOneAssertionValidator eidasResponseOneAssertionValidator = new EidasResponseOneAssertionValidator();
        eidasResponseOneAssertionValidator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseOneAssertionValidator#validate(Response response)}
     * . Testing with wrong number of attributeStatement in an Assertion. Must throw a
     * {@link ValidationException} with a specific message.
     *
     */
    @Test
    public void testOneAuthnStatementTwoAttributeStatementInResponseAssertion() throws ValidationException, EIDASSAMLEngineException {
        exception.expect(ValidationException.class);
        exception.expectMessage("Number of AttributeStatement 2 in Assertion differs from number of allowed ones:1.");
        Response response = generateTestResponse(1, 1, 2);
        EidasResponseOneAssertionValidator eidasResponseOneAssertionValidator = new EidasResponseOneAssertionValidator();
        eidasResponseOneAssertionValidator.validate(response);
    }

    /**
     * In order to test the
     * {@link EidasResponseOneAssertionValidator#validate(Response response)}
     * a response must be generated.
     *
     * @param numberAssertions  The number of Assertions to be generated.
     *
     * @param numberAuthnStatements  The number of AuthnStatements to be generated.
     *
     * @param numberAttributeStatements  The number of AttributeStatements to be generated.
     *
     * @return The response.
     */
    private Response generateTestResponse(int numberAssertions, int numberAuthnStatements, int numberAttributeStatements) throws EIDASSAMLEngineException {
        final StatusCode statusCode = BuilderFactoryUtil.generateStatusCode(StatusCode.SUCCESS_URI);
        final Status status = BuilderFactoryUtil.generateStatus(statusCode);

        List<Assertion> assertions = generateAssertion(numberAssertions);
        for (Assertion assertion : assertions) {
            assertion.getAuthnStatements().addAll(generateAuthnStatements(numberAuthnStatements));
            assertion.getAttributeStatements().addAll(generateAttributeStatements(numberAttributeStatements));
        }

        final Response response = BuilderFactoryUtil.generateResponse(
                SAMLEngineUtils.generateNCName(),
                SAMLEngineUtils.getCurrentTime(), status);
        response.getAssertions().addAll(assertions);
        return response;
    }

    /**
     * In order to test the
     * {@link EidasResponseOneAssertionValidator#validate(Response response)}
     * a list of assertions must be generated to be included in a response.
     *
     * @return The list of assertions.
     */
    private List<Assertion> generateAssertion(int numberAssertions) throws EIDASSAMLEngineException {
         List<Assertion> output = new ArrayList<Assertion>();
        for (int i = 0; i < numberAssertions; i++) {
            final Issuer issuerAssertion = BuilderFactoryUtil.generateIssuer();
            final Assertion assertion = BuilderFactoryUtil.generateAssertion(
                    SAMLVersion.VERSION_20, SAMLEngineUtils.generateNCName(),
                    SAMLEngineUtils.getCurrentTime(), issuerAssertion);
            output.add(assertion);
        }

        return output;
    }

    /**
     * In order to test the
     * {@link EidasResponseOneAssertionValidator#validate(Response response)}
     * a list of authn statements must be generated to be included in an assertion.
     *
     * @param numberStatements  The number Statements to be generated.
     *
     * @return The list of authn statements.
     */
    private List<AuthnStatement> generateAuthnStatements(int numberStatements) throws EIDASSAMLEngineException {
        List<AuthnStatement> output = new ArrayList<AuthnStatement>();
        for (int i = 0; i < numberStatements; i++) {
            final AuthnContext authnContext = (AuthnContext) BuilderFactoryUtil.buildXmlObject(AuthnContext.DEFAULT_ELEMENT_NAME);
            final AuthnContextDecl authnContextDecl = (AuthnContextDecl) BuilderFactoryUtil.buildXmlObject(AuthnContextDecl.DEFAULT_ELEMENT_NAME);
            authnContext.setAuthnContextDecl(authnContextDecl);
            final AuthnStatement authnStatement = BuilderFactoryUtil.generateAuthnStatement(new DateTime(), authnContext);
            output.add(authnStatement);
        }

        return output;
    }

     /**
     * In order to test the
     * {@link EidasResponseOneAssertionValidator#validate(Response response)}
     * a list of attribute statements must be generated to be included in an assertion.
     *
     * @param numberStatements  The number Statements to be generated.
     *
     * @return The list of attribute statements.
     */
    private List<AttributeStatement> generateAttributeStatements(int numberStatements) throws EIDASSAMLEngineException {
        List<AttributeStatement> output = new ArrayList<AttributeStatement>();
        for (int i = 0; i < numberStatements; i++) {
            final AttributeStatement attrStatement = (AttributeStatement) BuilderFactoryUtil.buildXmlObject(AttributeStatement.DEFAULT_ELEMENT_NAME);
            output.add(attrStatement);
        }

        return output;
    }
}
