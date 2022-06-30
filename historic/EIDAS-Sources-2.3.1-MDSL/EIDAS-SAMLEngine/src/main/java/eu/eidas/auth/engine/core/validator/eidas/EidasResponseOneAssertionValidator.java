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
package eu.eidas.auth.engine.core.validator.eidas;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.engine.exceptions.ValidationException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Statement;
import org.opensaml.saml.saml2.core.impl.AttributeStatementImpl;
import org.opensaml.saml.saml2.core.impl.AuthnStatementImpl;

import java.util.List;

public class EidasResponseOneAssertionValidator extends ResponseSchemaValidator {

    private static final int NUMBER_ALLOWED_ASSERTIONS_PER_RESPONSE = 1;
    private static final int NUMBER_ALLOWED_AUTHN_STATEMENT_PER_ASSERTION = 1;
    private static final int NUMBER_ALLOWED_ATTRIBUTE_STATEMENT_PER_ASSERTION = 1;

    /**
     * Constructor
     */
    public EidasResponseOneAssertionValidator() {
        super();
    }

    /**
     * {@inheritDoc}
     *
     * @param response
     * @throws eu.eidas.engine.exceptions.ValidationException
     */
    @Override
    public void validate(Response response) throws ValidationException {
        if (EIDASStatusCode.SUCCESS_URI.toString().equals(response.getStatus().getStatusCode().getValue())) {
            super.validate(response);
            List<Assertion> assertions = response.getAssertions();
            checkNumberAllowedAssertionsPerResponse(assertions);
            checkNumberAllowedStatementsPerAssertion(assertions);
        }
    }

    private void checkNumberAllowedStatementsPerAssertion(List<Assertion> assertions) throws ValidationException {
        for (Assertion assertion : assertions) {
            int authnStatementAcounter = 0;
            int attributeStatementcounter = 0;
            for (Statement statement : assertion.getStatements()) {
                authnStatementAcounter = countAuthnStatements(statement, authnStatementAcounter);
                attributeStatementcounter = countAttributeStatements(statement, attributeStatementcounter);
            }

            checkNumberAllowedAuthnStatements(authnStatementAcounter);
            checkNumberAllowedAttributeStatements(attributeStatementcounter);
        }
    }

    private void checkNumberAllowedAssertionsPerResponse(List<Assertion> assertions) throws ValidationException {
        if (assertions.size() != NUMBER_ALLOWED_ASSERTIONS_PER_RESPONSE) {
            throw new ValidationException("Number of Assertion in Response " + assertions.size()
                    + ", differs from number of allowed ones:" + NUMBER_ALLOWED_ASSERTIONS_PER_RESPONSE + ".");
        }
    }

    private void checkNumberAllowedAuthnStatements(int authnStatementAcounter) throws ValidationException {
        if (authnStatementAcounter != NUMBER_ALLOWED_AUTHN_STATEMENT_PER_ASSERTION) {
            throw new ValidationException("Number of AuthnStatement " + authnStatementAcounter + " in Assertion"
                    + " differs from number of allowed ones:" + NUMBER_ALLOWED_AUTHN_STATEMENT_PER_ASSERTION + ".");
        }
    }

    private void checkNumberAllowedAttributeStatements(int attributeStatementcounter) throws ValidationException {
        if (attributeStatementcounter != NUMBER_ALLOWED_ATTRIBUTE_STATEMENT_PER_ASSERTION) {
            throw new ValidationException("Number of AttributeStatement " + attributeStatementcounter + " in Assertion"
                    + " differs from number of allowed ones:" + NUMBER_ALLOWED_ATTRIBUTE_STATEMENT_PER_ASSERTION + ".");
        }
    }

    private int countAuthnStatements(Statement statement, int authnStatementcounter) {
        if (statement instanceof AuthnStatementImpl) {
            authnStatementcounter++;
        }
        return authnStatementcounter;
    }

    private int countAttributeStatements(Statement statement, int attributeStatementcounter) {
        if (statement instanceof AttributeStatementImpl) {
            attributeStatementcounter++;
        }
        return attributeStatementcounter;
    }

}
