package eu.eidas.auth.engine.core.validator.stork;

/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 23/02/14
 * Time: 17:17
 * To change this template use File | Settings | File Templates.
 */

import java.util.regex.Pattern;

import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;

import eu.eidas.auth.engine.core.stork.SPCountry;


public class StorkSpCountryValidator implements Validator<SPCountry> {

    public static final String REGEX_PATTERN = "^[A-Z]{2}$";

    public StorkSpCountryValidator() {

    }

    public void validate(SPCountry spCountry) throws ValidationException {

        if (spCountry != null) {

            if (spCountry.getSPCountry() == null) {
                throw new ValidationException("spCountry has no value");
            }

            if (!Pattern.matches(REGEX_PATTERN, spCountry.getSPCountry())) {
                throw new ValidationException("spCountry not valid: " + spCountry.getSPCountry());
            }
        }
    }

}

