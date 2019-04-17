
package eu.eidas.auth.engine.core.validator.stork;

import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;

import eu.eidas.auth.engine.core.stork.SPID;


public class StorkSPIDValidator implements Validator<SPID> {

    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 40;

    public StorkSPIDValidator() {

    }

    public void validate(SPID spid) throws ValidationException {

        // Only for VIDP

        if (spid != null) {

            if (spid.getSPID() == null) {
                throw new ValidationException("SPID has no value");
            }

            if (spid.getSPID().length() <= MIN_SIZE || spid.getSPID().length() > MAX_SIZE) {
                throw new ValidationException("SPID has wrong size: " + spid.getSPID().length());
            }
        }


    }

}
