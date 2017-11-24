package eu.eidas.auth.engine.core.validator.stork;

import java.util.regex.Pattern;

import org.opensaml.saml2.metadata.validator.RequestedAttributeSchemaValidator;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSAny;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.validation.ValidationException;

import eu.eidas.auth.engine.core.stork.RequestedAttribute;


/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 4/03/14
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */


public class StorkRequestedAttributeValidator extends
        RequestedAttributeSchemaValidator {

    private static final String PATTERN_ISAGEOVER = "^[0-9]{1,3}$";
    private static final String STORK_ATTRIBUTE_ISAGEOVER = "isAgeOver";

    public StorkRequestedAttributeValidator() {

        super();
    }

    public void validate(RequestedAttribute attr) throws ValidationException {


        if (!STORKAttributes.ATTRIBUTES_SET.contains(attr.getName()) && attr.isRequired()) {
            throw new ValidationException("Mandatory RequestedAttribute \"" + attr.getName() + "\" is not valid");
        }

        if (attr.getName() == null) {

            throw new ValidationException("Name is required.");
        }

        if (attr.getNameFormat() == null) {

            throw new ValidationException("NameFormat is required.");
        }

        if (!STORKAttributes.ATTRIBUTES_SET.contains(attr.getName()) && attr.isRequired()) {
            throw new ValidationException("Mandatory RequestedAttribute \"" + attr.getName() + "\" is not valid");
        }

        if (attr.getName().equals(STORK_ATTRIBUTE_ISAGEOVER)) {
            if (attr.getAttributeValues().isEmpty()) {
                throw new ValidationException("isAgeOver requires attribute value");
            }

            XMLObject attrValueObject = attr.getAttributeValues().get(0);

            if (attrValueObject instanceof XSString) {
                if (!Pattern.matches(PATTERN_ISAGEOVER, ((XSString) attr.getAttributeValues().get(0)).getValue())) {
                    throw new ValidationException("Value for isAgeOver has incorrect format.");
                }
            } else if (attrValueObject instanceof XSAny) {
                if (!Pattern.matches(PATTERN_ISAGEOVER, ((XSAny) attrValueObject).getTextContent())) {
                    throw new ValidationException("Value for isAgeOver has incorrect format.");
                }

            } else {
                throw new ValidationException("Value for isAgeOver has incorrect format.");
            }

        }

    }

}
