package eu.eidas.auth.engine.core.validator.stork;

/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 23/02/14
 * Time: 17:03
 * To change this template use File | Settings | File Templates.
 */

import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.validator.NameIDSchemaValidator;
import org.opensaml.xml.validation.ValidationException;

public class StorkNameIDValidator extends NameIDSchemaValidator {

    private static final String FORMAT_ALLOWED_VALUE = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
    private static final String FORMAT_ALLOWED_VALUE_OLD = "urn:oasis:names:tc:SAML:2.0:nameid-format:unspecified";

    /**
     * Constructor
     */
    public StorkNameIDValidator() {

        super();
    }

    @Override
    public void validate(NameID nameID) throws ValidationException {

        super.validate(nameID);

        if (nameID.getNameQualifier() == null) {

            throw new ValidationException("NameQualifier is required.");
        }

        if (nameID.getFormat() == null) {

            throw new ValidationException("Format is required.");

        } else if (!(nameID.getFormat().equals(FORMAT_ALLOWED_VALUE) || nameID.getFormat().equals(FORMAT_ALLOWED_VALUE_OLD))) {

            throw new ValidationException("Format is invalid.");
        }

    }

}
