package eu.eidas.auth.engine.core.validator.stork;

import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.validator.SubjectSchemaValidator;
import org.opensaml.xml.validation.ValidationException;

/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 23/02/14
 * Time: 17:52
 * To change this template use File | Settings | File Templates.
 */

public class StorkSubjectValidator extends SubjectSchemaValidator {

    /**
     * Constructor
     */
    public StorkSubjectValidator() {

        super();
    }

    @Override
    public void validate(Subject subject) throws ValidationException {

        super.validate(subject);

        if (subject.getNameID() == null && subject.getEncryptedID() == null) {

            throw new ValidationException("Neither NameID nor EncryptedID is provided.");
        }

        if (subject.getSubjectConfirmations() == null || subject.getSubjectConfirmations().isEmpty()) {

            throw new ValidationException("SubjectConfirmation is required.");
        }

    }

}

