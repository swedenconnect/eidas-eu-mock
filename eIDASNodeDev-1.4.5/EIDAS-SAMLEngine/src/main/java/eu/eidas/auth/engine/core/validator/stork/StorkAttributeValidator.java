package eu.eidas.auth.engine.core.validator.stork;

import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.validator.AttributeSchemaValidator;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.util.AttributeMap;
import org.opensaml.xml.validation.ValidationException;

/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 4/03/14
 * Time: 11:44
 * To change this template use File | Settings | File Templates.
 */

public class StorkAttributeValidator extends AttributeSchemaValidator {

    private static final String PATTERN_EIDENTIFIER = "^[A-Z]{2}/[A-Z]{2}/[A-Za-z0-9+/=\r\n]+$";
    private static final String PATTERN_GENDER = "^[MF]{1}$";
    private static final String PATTERN_COUNTRYCODEOFBIRTH = "^[A-Z]{2}|[A-Z]{4}$";
    private static final String PATTERN_COUNTRYCODE = "^[A-Z]{2}$";
    private static final String PATTERN_MARTIALSTATUS = "^[SMPDW]{1}$";
    private static final String PATTERN_EMAIL = "^[-+.\\w]{1,64}@[-.\\w]{1,64}\\.[-.\\w]{2,6}$";
    private static final String PATTERN_AGE = "^[0-9]{1,3}$";
    private static final int MAX_AGE = 120;
    private static final String PATTERN_ISAGEOVER = PATTERN_AGE;
    private static final String PATTERN_CITIZENQAALEVEL = "^[1-4]{1}$";
    public static final String STORK_ATTRIBUTE_STATUS_ATTTRIB_NAME = "AttributeStatus";

    public static final QName DEFAULT_STORK_ATTRIBUTE_QNAME = new QName(STORKAttributes.STORK10_NS, STORK_ATTRIBUTE_STATUS_ATTTRIB_NAME, STORKAttributes.STORK10_PREFIX);
    public static final String ALLOWED_ATTRIBUTE_STATUS_AVAIL = "Available";
    public static final String ALLOWED_ATTRIBUTE_STATUS_NOT_AVAIL = "NotAvailable";
    public static final String ALLOWED_ATTRIBUTE_STATUS_WITHHELD = "Withheld";


    /**
     * Constructor
     */
    public StorkAttributeValidator() {

        super();
    }

    @Override
    public void validate(Attribute attr) throws ValidationException {

        super.validate(attr);

        if (attr.getName() == null) {

            throw new ValidationException("Name is required.");
        }

        if (attr.getNameFormat() == null) {

            throw new ValidationException("NameFormat is required.");
        }


        if (attr.getUnknownAttributes() != null) {
            AttributeMap map = attr.getUnknownAttributes();
            String value = map.get(DEFAULT_STORK_ATTRIBUTE_QNAME);
            if (value != null && !ALLOWED_ATTRIBUTE_STATUS_AVAIL.equals(value) && !ALLOWED_ATTRIBUTE_STATUS_NOT_AVAIL.equals(value) && !ALLOWED_ATTRIBUTE_STATUS_WITHHELD.equals(value) ){
                throw new ValidationException("AttributeStatus is invalid.");
            }
        }

        if (!attr.getAttributeValues().isEmpty()) {
            //validate individual attributes if present
            XMLObject attrValueObject = attr.getAttributeValues().get(0);

            if (!(attrValueObject instanceof XSString)) {
                //Only validate String attributes
                return;
            }

            String value = ((XSString) attr.getAttributeValues().get(0)).getValue();
            String attrName = attr.getName();

            //only isAgeOver can be empty if provided
            if (value == null) {
                //only isAgeOver can be empty if provided
                if (attrName.equals(STORKAttributes.STORK_ATTRIBUTE_ISAGEOVER)) {
                    return;
                } else {
                    throw new ValidationException("Provided AttributeValue is empty");
                }
            }

            //validate eIdentifier
            validateAttributeValueFormat(value, attrName, STORKAttributes.STORK_ATTRIBUTE_EIDENTIFIER, PATTERN_EIDENTIFIER);

            //validate gender
            validateAttributeValueFormat(value, attrName, STORKAttributes.STORK_ATTRIBUTE_GENDER, PATTERN_GENDER);

            //validate dateOfBirth
            if (attrName.equals(STORKAttributes.STORK_ATTRIBUTE_DATEOFBIRTH)) {
                verifyDate(value);
            }

            //validate countryCode of birth
            validateAttributeValueFormat(value, attrName, STORKAttributes.STORK_ATTRIBUTE_COUNTRYCODEOFBIRTH, PATTERN_COUNTRYCODEOFBIRTH);

            //validate countryCode
            validateAttributeValueFormat(value, attrName, STORKAttributes.STORK_ATTRIBUTE_NATIONALITYCODE, PATTERN_COUNTRYCODE);

            //validate martialStatus
            validateAttributeValueFormat(value, attrName, STORKAttributes.STORK_ATTRIBUTE_MARTIALSTATUS, PATTERN_MARTIALSTATUS);

            //validate email
            validateAttributeValueFormat(value, attrName, STORKAttributes.STORK_ATTRIBUTE_EMAIL, PATTERN_EMAIL);

            //validate age and isAgeOver
            validateAttributeValueFormat(value, attrName, STORKAttributes.STORK_ATTRIBUTE_AGE, PATTERN_AGE);
            validateAttributeValueFormat(value, attrName, STORKAttributes.STORK_ATTRIBUTE_ISAGEOVER, PATTERN_ISAGEOVER);
            if ((attr.getName().equals(STORKAttributes.STORK_ATTRIBUTE_AGE) || attr.getName().equals(STORKAttributes.STORK_ATTRIBUTE_ISAGEOVER)) &&
                Integer.parseInt(((XSString) attr.getAttributeValues().get(0)).getValue()) > MAX_AGE) {
                    throw new ValidationException("Maximum age reached");
            }

            validateAttributeValueFormat(value, attrName, STORKAttributes.STORK_ATTRIBUTE_CITIZENQAALEVEL, PATTERN_CITIZENQAALEVEL);
        }

    }

    private void validateAttributeValueFormat(String value, String currentAttrName, String attrNameToTest, String pattern) throws ValidationException {
        if (currentAttrName.equals(attrNameToTest) && !Pattern.matches(pattern, value)) {
            throw new ValidationException(attrNameToTest + " has incorrect format.");
        }

    }

    private static void verifyDate(String pepsDate) throws ValidationException {
        DateTimeFormatter fmt;

        switch (pepsDate.length()) {
            case 4:
                fmt = DateTimeFormat.forPattern("yyyy");
                break;
            case 6:
                fmt = DateTimeFormat.forPattern("yyyyMM");
                break;
            case 8:
                fmt = DateTimeFormat.forPattern("yyyyMMdd");
                break;
            default:
                throw new ValidationException("Date has wrong format");
        }

        try {
            fmt.parseDateTime(pepsDate);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Date has wrong format  {}", e);
        }


    }


}