package eu.eidas.auth.engine.core.validator.stork;

/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 23/02/14
 * Time: 17:42
 * To change this template use File | Settings | File Templates.
 */

import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;

import eu.eidas.auth.engine.core.stork.SPSector;


public class StorkSpSectorValidator implements
        Validator<SPSector> {

    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 20;

    public StorkSpSectorValidator() {

    }

    public void validate(SPSector spSector) throws ValidationException {

        if (spSector != null) {

            if (spSector.getSPSector() == null) {
                throw new ValidationException("spSector has no value");
            }

            if (spSector.getSPSector().length() < MIN_SIZE || spSector.getSPSector().length() > MAX_SIZE) {
                throw new ValidationException("spApplication has wrong size: " + spSector.getSPSector().length());
            }

        }
    }

}

