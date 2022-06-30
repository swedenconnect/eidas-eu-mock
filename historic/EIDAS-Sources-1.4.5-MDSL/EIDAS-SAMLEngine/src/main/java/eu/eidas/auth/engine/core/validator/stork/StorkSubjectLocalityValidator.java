package eu.eidas.auth.engine.core.validator.stork;

/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 25/02/14
 * Time: 12:08
 * To change this template use File | Settings | File Templates.
 */

import org.opensaml.saml2.core.SubjectLocality;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;

public class StorkSubjectLocalityValidator implements
        Validator<SubjectLocality> {

    public StorkSubjectLocalityValidator() {

    }

    public void validate(SubjectLocality sloc) throws ValidationException {

        if (sloc.getAddress() == null) {

            throw new ValidationException("Address is required.");
        }
    }

}

