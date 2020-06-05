package eu.eidas.auth.engine.core.validator.stork;

/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 23/02/14
 * Time: 17:11
 * To change this template use File | Settings | File Templates.
 */

import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;

import eu.eidas.auth.engine.core.stork.SPApplication;


public class StorkSpApplicationValidator implements
        Validator<SPApplication> {

    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 100;

    public StorkSpApplicationValidator() {

    }

    public void validate(SPApplication spApplication) throws ValidationException {

        if (spApplication != null) {

            if (spApplication.getSPApplication() == null) {
                throw new ValidationException("spApplication has no value");
            }

            if (spApplication.getSPApplication().length() < MIN_SIZE || spApplication.getSPApplication().length() > MAX_SIZE) {
                throw new ValidationException("spApplication has wrong size: " + spApplication.getSPApplication().length());
            }

        }
    }

}

