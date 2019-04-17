package eu.eidas.auth.engine.core.eidas;

import org.opensaml.common.SAMLObject;

import javax.xml.namespace.QName;

//CAVEAT: this interface and the implmenting classes should be dropped when upgrading to opensaml 3

public interface DigestMethod extends SAMLObject {
    String ALG_SUPPORT_NS="urn:oasis:names:tc:SAML:metadata:algsupport";
    String ALG_SUPPORT_PREFIX="alg";
    /** The Constant DEFAULT_ELEMENT_LOCAL_NAME. */
    String DEF_LOCAL_NAME = "DigestMethod";

    /** The Constant DEFAULT_ELEMENT_NAME. */
    QName DEF_ELEMENT_NAME = new QName(ALG_SUPPORT_NS, DEF_LOCAL_NAME,ALG_SUPPORT_PREFIX);

    /** The Constant TYPE_LOCAL_NAME. */
    String TYPE_LOCAL_NAME = "DigestMethod";

    /** The Constant TYPE_NAME. */
    QName TYPE_NAME = new QName(ALG_SUPPORT_NS, TYPE_LOCAL_NAME,ALG_SUPPORT_PREFIX);

    /**
     * Gets the algorithm
     *
     * @return the algorithm
     */
    String getAlgorithm();

    /**
     * Sets the algorithm.
     *
     * @param algorithm the new algorithm
     */
    void setAlgorithm(String algorithm);
}
