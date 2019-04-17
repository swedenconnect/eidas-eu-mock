package eu.eidas.auth.engine.core.validator.stork;

/**
 * Created with IntelliJ IDEA.
 * User: s228576
 * Date: 25/02/14
 * Time: 12:06
 * To change this template use File | Settings | File Templates.
 */

import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.validator.SubjectConfirmationSchemaValidator;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.signature.X509Data;
import org.opensaml.xml.validation.ValidationException;

public class StorkSubjectConfirmationValidator extends
        SubjectConfirmationSchemaValidator {

    private static final String ALLOWED_METHOD_1 = "urn:oasis:names:tc:SAML:2.0:cm:bearer";
    private static final String ALLOWED_METHOD_2 = "oasis:names:tc:SAML:2.0:cm:holder-of-key";

    /**
     * Constructor
     */
    public StorkSubjectConfirmationValidator() {

        super();
    }

    @Override
    public void validate(SubjectConfirmation subjectConfirmation)
            throws ValidationException {

        super.validate(subjectConfirmation);

        String method = subjectConfirmation.getMethod();

        if (!(method.equals(ALLOWED_METHOD_1) || method.equals(ALLOWED_METHOD_2))) {
            throw new ValidationException("Method is invalid.");
        }

        if (subjectConfirmation.getSubjectConfirmationData() == null) {
            throw new ValidationException("SubjectConfirmationData required.");

        }

        SubjectConfirmationData confData = subjectConfirmation.getSubjectConfirmationData();


        if (method.equals(ALLOWED_METHOD_1) && confData.getNotBefore() != null) {
                throw new ValidationException("NotBefore in SubjectConfirmationData not allowed if confirmation method is \"bearer\".");
        }

        if (confData.getNotOnOrAfter() == null) {
            throw new ValidationException("NotOnOrAfter is required.");
        }

        if (confData.getRecipient() == null) {
            throw new ValidationException("Recipient is required.");
        }

        if (confData.getInResponseTo() == null) {
            throw new ValidationException("InResponseTo is required.");
        }

        if (method.equals(ALLOWED_METHOD_2)) {
            List<XMLObject> childrenKeyInfo = confData.getUnknownXMLObjects(new QName("KeyInfo"));

            if (childrenKeyInfo.isEmpty()) {
                throw new ValidationException("KeyInfo is required.");
            }

            List<XMLObject> childrenKeyData = confData.getUnknownXMLObjects(new QName("X509Data"));

            if (childrenKeyData.size() != 1) {
                throw new ValidationException("Invalid number of X509Data elements.");
            } else {
                X509Data data = (X509Data) childrenKeyData.get(0);

                if (data.getX509Certificates() == null || data.getX509Certificates().isEmpty()) {
                    throw new ValidationException("X509Certificate is required.");
                }

            }

        }


    }


}

