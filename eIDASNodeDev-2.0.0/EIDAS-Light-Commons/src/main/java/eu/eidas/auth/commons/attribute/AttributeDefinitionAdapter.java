/*

 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
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
