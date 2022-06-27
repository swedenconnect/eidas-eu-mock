/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package eu.eidas.auth.engine.core.eidas.impl;

import eu.eidas.auth.engine.core.eidas.RequestedAttribute;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSBooleanValue;
import org.opensaml.core.xml.util.AttributeMap;
import org.opensaml.core.xml.util.XMLObjectChildrenList;
import org.opensaml.saml.common.AbstractSAMLObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The Class RequestedAttributeImpl.
 *
 */
public class RequestedAttributeImpl extends AbstractSAMLObject implements
	RequestedAttribute {

    /**
     * The attribute values.
     */
    private final XMLObjectChildrenList<XMLObject> attributeValues;

    /**
     * The friendly name.
     *
     */
    private String friendlyName;

    /**
     * The is required.
    */
    private String isRequired;

    /**
     * The name.
     *
     */
    private String name;

    /**
     * The name format.
     *
     */
    private String nameFormat;

    /**
     * The unknown attributes.
     *
     */
    private AttributeMap unknownAttributes;

    /**
     * Instantiates a new requested attribute impl.
     *
     * @param namespaceURI the namespace uri
     * @param elementLocalName the element local name
     * @param namespacePrefix the namespace prefix
     */
    protected RequestedAttributeImpl(final String namespaceURI,
	    final String elementLocalName, final String namespacePrefix) {
	super(namespaceURI, elementLocalName, namespacePrefix);
	unknownAttributes = new AttributeMap(this);
	attributeValues = new XMLObjectChildrenList<XMLObject>(this);
    }


    /**
     * Gets the attribute values.
     *
     * @return the attribute values
     */
    public final List<XMLObject> getAttributeValues() {
	return attributeValues;
    }

    /**
     * Gets the friendly name.
     *
     * @return the friendly name.
     */
    public final String getFriendlyName() {
	return friendlyName;
    }


    /**
     * Gets the checks if is required.
     *
     * @return the boolean if it's required.
     *//*
    public final String isRequired() {
	return isRequired;
    }*/


    /**
     * Gets the is required xs boolean.
     *
     * @return the XSBoolean if it's required.
     */
    public final String getIsRequiredXSBoolean() {
	return isRequired;
    }


    /**
     * Gets the name.
     *
     * @return the name
     */
    public final String getName() {
	return name;
    }


    /**
     * Gets the name format.
     *
     * @return the name format.
     */
    public final String getNameFormat() {
	return nameFormat;
    }

    /**
     * Gets the ordered children.
     *
     * @return the list of XMLObject.
     */
    public final List<XMLObject> getOrderedChildren() {
	final List<XMLObject> children = new ArrayList<XMLObject>();
	children.addAll(attributeValues);
	return Collections.unmodifiableList(children);
    }

    /**
     * Gets the unknown attributes.
     *
     * @return the attribute map
     */
    public final AttributeMap getUnknownAttributes() {
	return unknownAttributes;
    }

    /**
     * Sets the friendly name.
     *
     * @param newFriendlyName the new friendly name format
     */
    public final void setFriendlyName(final String newFriendlyName) {
	this.friendlyName = prepareForAssignment(this.friendlyName,
		newFriendlyName);
    }

    /**
     * Set new boolean value isRequired.
     * @param newIsRequired then new value
     */
    public final void setIsRequired(final String newIsRequired) {
	isRequired = prepareForAssignment(this.isRequired, newIsRequired);

    }

    /**
     * Sets the name.
     *
     * @param newName the new name
     */
    public final void setName(final String newName) {
	this.name = prepareForAssignment(this.name, newName);
    }

    /**
     * Sets the name format.
     *
     * @param newNameFormat the new name format
     */
    public final void setNameFormat(final String newNameFormat) {
	this.nameFormat = prepareForAssignment(this.nameFormat, newNameFormat);
    }

    /**
     * Sets the unknown attributes.
     *
     * @param newUnknownAttr the new unknown attributes
     */
    public final void setUnknownAttributes(final AttributeMap newUnknownAttr) {
	this.unknownAttributes = newUnknownAttr;
    }

    @Override
    public XSBooleanValue isRequiredXSBoolean() {
        return XSBooleanValue.valueOf(isRequired);
    }

    @Override
    public void setIsRequired(@Nullable  Boolean aBoolean) {
        this.isRequired = String.valueOf(aBoolean);
    }


    @Override
    public void setIsRequired(@ Nullable XSBooleanValue xsBooleanValue) {
        this.isRequired = String.valueOf(xsBooleanValue);
    }

    @Override
    public Boolean isRequired() {
        return Boolean.parseBoolean(isRequired);
    }
}
