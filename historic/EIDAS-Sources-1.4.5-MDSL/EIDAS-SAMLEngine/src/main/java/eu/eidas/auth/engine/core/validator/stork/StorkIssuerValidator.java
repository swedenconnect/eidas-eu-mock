package eu.eidas.auth.engine.core.validator.stork;

import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.validator.IssuerSchemaValidator;
import org.opensaml.xml.validation.ValidationException;


/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 23/02/14
 * Time: 16:01
 * To change this template use File | Settings | File Templates.
 */

public class StorkIssuerValidator extends IssuerSchemaValidator {

    private static final String FORMAT_ALLOWED_VALUE = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";

    /**
     * Constructor
     */
    public StorkIssuerValidator() {

        super();
    }

    @Override
    public void validate(Issuer issuer) throws ValidationException {

        super.validate(issuer);

        // format is optional
        if (issuer.getFormat() != null && !issuer.getFormat().equals(FORMAT_ALLOWED_VALUE)) {
                throw new ValidationException("Format has an invalid value.");
        }

    }

}
