package eu.eidas.auth.engine.core.validator.stork;

import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.validator.AudienceRestrictionSchemaValidator;
import org.opensaml.xml.validation.ValidationException;


/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 23/02/14
 * Time: 15:42
 * To change this template use File | Settings | File Templates.
 */
public class StorkAudienceRestrictionValidator extends
        AudienceRestrictionSchemaValidator {

    /**
     * Constructor
     */
    public StorkAudienceRestrictionValidator() {

        super();
    }

    @Override
    public void validate(AudienceRestriction res) throws ValidationException {

        super.validate(res);

        if (res.getAudiences() == null || res.getAudiences().isEmpty()) {

            throw new ValidationException("Audience is required.");
        }

    }

}

