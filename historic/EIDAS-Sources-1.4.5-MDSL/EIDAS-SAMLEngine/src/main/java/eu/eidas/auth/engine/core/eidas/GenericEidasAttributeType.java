package eu.eidas.auth.engine.core.eidas;

import java.util.Map;

import javax.xml.namespace.QName;

import org.opensaml.common.SAMLObject;

import eu.eidas.auth.engine.core.SAMLCore;

@Deprecated
public interface GenericEidasAttributeType extends SAMLObject {
    String TYPE_SUPPORT_NS = SAMLCore.EIDAS10_RESPONSESAML_NS.getValue();
    String TYPE_SUPPORT_PREFIX = SAMLCore.EIDAS10_SAML_PREFIX.getValue();

    String getDefLocalName(String attrName);
    QName getDefElementName(String attrName);

    /**
     *
     * @param attrName
     * @return the variable type name
     */
    String getTypeLocalName(String attrName);
    /**
     *
     * @param attrName
     * @return the variable type name
     */
    QName getTypeName(String attrName);

    /**
     *
     * @return the attribute value, as a String
     */
    String getValue();

    /**
     * set the attribute value
     * @param value
     */
    void setValue(String value);

    /**
     * get the map of attribute an attribute to the element
     */
    Map<String, String> getAttributeMap();
}
