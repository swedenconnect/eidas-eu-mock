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
package eu.eidas.auth.commons.attribute;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;

import eu.eidas.auth.commons.attribute.impl.LiteralStringAttributeValueMarshaller;

public final class AttributeDefinitionAdapter extends XmlAdapter<String, AttributeDefinition> {

	final static AttributeValueMarshaller<?> MARSHALLER = new LiteralStringAttributeValueMarshaller();

	@Override
	public AttributeDefinition unmarshal(String s) throws Exception {
		AttributeDefinition definition = AttributeDefinition.builder()
				.attributeValueMarshaller((AttributeValueMarshaller<Object>) MARSHALLER)
				.friendlyName("not set").nameUri(s).personType(PersonType.NATURAL_PERSON).required(false)
				.transliterationMandatory(false).uniqueIdentifier(false)
				.xmlType(new QName(s, "eidas-natural", "not set"))
				.build();
		return definition;
	}

	@Override
	public String marshal(AttributeDefinition d) throws Exception {
		return d.getNameUri().toString();
	}
}
