package eu.eidas.auth.engine.core.validator.stork;

import org.joda.time.DateTime;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.validator.ConditionsSpecValidator;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 23/02/14
 * Time: 15:54
 * To change this template use File | Settings | File Templates.
 */

public class StorkConditionsValidator extends ConditionsSpecValidator {

    private static final Logger LOG = LoggerFactory.getLogger(StorkConditionsValidator.class.getName());

    /**
     * Constructor
     */
    public StorkConditionsValidator() {

        super();
    }

    @Override
    public void validate(Conditions conditions) throws ValidationException {
        LOG.debug("conditions.getNotBefore() "+ conditions.getNotBefore());
        LOG.debug("conditions.getNotOnOrAfter() "+ conditions.getNotOnOrAfter());
        LOG.debug("dateTime.now() "+ DateTime.now());

        super.validate(conditions);

        if (conditions.getNotBefore() == null) {

            throw new ValidationException("NotBefore is required.");
        }

        if (conditions.getNotBefore().isAfterNow()) {
            throw new ValidationException("Current time is before NotBefore condition");
        }

        if (conditions.getNotOnOrAfter() == null) {

            throw new ValidationException("NotOnOrAfter is required.");
        }
        if (conditions.getNotOnOrAfter().isBeforeNow()) {

            throw new ValidationException("Current time is after NotOnOrAfter condition");
        }

        if (conditions.getAudienceRestrictions() == null || conditions.getAudienceRestrictions().isEmpty()) {

            throw new ValidationException("AudienceRestriction is required.");
        }

        if (conditions.getOneTimeUse() == null) {

            throw new ValidationException("OneTimeUse is required.");
        }

    }

}

