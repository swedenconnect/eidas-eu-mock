package eu.eidas.auth.engine.core.validator.stork;

import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;

/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 23/02/14
 * Time: 16:14
 * To change this template use File | Settings | File Templates.
 */

public class StorkNameIdPolicyValidator implements Validator<NameIDPolicy> {

    public StorkNameIdPolicyValidator() {

    }

    public void validate(NameIDPolicy nameIDPolicy) throws ValidationException {


        if (nameIDPolicy.getAllowCreate() != null && !nameIDPolicy.getAllowCreate()) {
                throw new ValidationException("AllowCreate is invalid.");
        }

    }


}

