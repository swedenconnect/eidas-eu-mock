package eu.eidas.auth.engine.core.validator.stork;

import java.util.List;

import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;

import eu.eidas.auth.engine.core.stork.RequestedAttribute;
import eu.eidas.auth.engine.core.stork.RequestedAttributes;


/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 4/03/14
 * Time: 10:51
 * To change this template use File | Settings | File Templates.
 */


public class StorkRequestedAttributesValidator implements
        Validator<RequestedAttributes> {

    public StorkRequestedAttributesValidator() {

    }

    public void validate(RequestedAttributes attrs) throws ValidationException {
        StorkRequestedAttributeValidator valRequestedAttribute = new StorkRequestedAttributeValidator();

        List<RequestedAttribute> requestedAttributeList = attrs.getAttributes();

        for (RequestedAttribute storkAttribute : requestedAttributeList) {
            valRequestedAttribute.validate(storkAttribute);
        }
    }
}
