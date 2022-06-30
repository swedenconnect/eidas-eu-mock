package eu.eidas.auth.engine.core.validator.stork;

/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 25/02/14
 * Time: 12:04
 * To change this template use File | Settings | File Templates.
 */

import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.validator.StatusSchemaValidator;
import org.opensaml.xml.validation.ValidationException;

public class StorkStatusValidator extends StatusSchemaValidator {

    /**
     * Constructor
     */
    public StorkStatusValidator() {

        super();
    }

    @Override
    public void validate(Status status) throws ValidationException {

        super.validate(status);

        if (status.getStatusCode() == null) {

            throw new ValidationException("StatusCode is required.");
        }

    }
}
