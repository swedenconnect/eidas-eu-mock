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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.eidas.auth.commons.attribute.impl.AbstractAttributeValue;

@XmlType
@XmlSeeAlso(AbstractAttributeValue.class)
public class AttributeMapType {
	@XmlType
	private static final class AttributeMapEntryType {
		@XmlElement
		@XmlJavaTypeAdapter(AttributeDefinitionAdapter.class)
		AttributeDefinition<?> definition;

		@XmlElement
		Set<String> value = new HashSet<>();

		void addValue(String value) {
			this.value.add(value);
		}
	}

	@XmlElement
	private List<AttributeMapEntryType> attribute = new ArrayList<AttributeMapEntryType>();

	private AttributeMapEntryType newEntry(AttributeDefinition<?> definition) {
		AttributeMapEntryType entry = new AttributeMapEntryType();
		entry.definition = definition;
		attribute.add(entry);
		return entry;
	}

	public static class ImmutableAttributeMapAdapter extends XmlAdapter<AttributeMapType, ImmutableAttributeMap> {

		@Override
		public ImmutableAttributeMap unmarshal(AttributeMapType input) throws Exception {
			ImmutableAttributeMap.Builder builder = ImmutableAttributeMap.builder();
			for (AttributeMapEntryType entry : input.attribute) {
				AttributeDefinition<?> definition = entry.definition;
				List<AttributeValue<?>> values = new ArrayList<>();
				for (String valueStr : entry.value) {
					values.add(definition.unmarshal(valueStr, true));
				}
				AttributeValue[] valuesArray = values.toArray(new AttributeValue[] {});
				builder.put(definition, valuesArray);
			}
			ImmutableAttributeMap result = builder.build();
			return result;
		}

		@Override
		public AttributeMapType marshal(ImmutableAttributeMap input) throws Exception {
			AttributeMapType result = new AttributeMapType();
			for (AttributeDefinition<?> definition : input.getDefinitions()) {
				AttributeMapType.AttributeMapEntryType entry = result.newEntry(definition);
				for (AttributeValue value : input.getAttributeValues(definition)) {
					String valueStr = definition.marshal(value);
					entry.addValue(valueStr);
				}
			}
			return result;
		}

	}
}
