package eu.eidas.auth.engine.core.validator.stork;

import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.validator.AuthnStatementSchemaValidator;
import org.opensaml.xml.validation.ValidationException;

/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 23/02/14
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
public class StorkAuthnStatementValidator extends
        AuthnStatementSchemaValidator {

    /**
     * Constructor
     */
    public StorkAuthnStatementValidator() {

        super();
    }

    @Override
    public void validate(AuthnStatement stmnt) throws ValidationException {

        super.validate(stmnt);

        if (stmnt.getAuthnInstant() == null) {

            throw new ValidationException("AuthnInstant is required.");
        }

        if (stmnt.getSubjectLocality() == null) {

            throw new ValidationException("SubjectLocality is required.");
        }

    }


}

