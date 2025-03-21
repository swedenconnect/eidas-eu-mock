/*
 * Copyright (c) 2024 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package eu.eidas.auth.commons.attribute;

import eu.eidas.auth.commons.collections.PrintSortedProperties;
import eu.eidas.auth.commons.io.PropertiesConverter;
import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Converts a Properties into a Set of AttributeDefinition.
 * <p>
 * The Properties format is defined as in the following XML Properties example:
 * <pre>
 * &lt;!DOCTYPE properties SYSTEM &quot;http://java.sun.com/dtd/properties.dtd&quot;&gt;
 * &lt;properties&gt;
 * &lt;comment&gt;eIDAS attributes&lt;/comment&gt;
 * &lt;entry key=&quot;1.Name&quot;&gt;&quot;http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier&lt;/entry&gt;
 * &lt;entry key=&quot;1.FriendlyName&quot;&gt;PersonIdentifier&lt;/entry&gt;
 * &lt;entry key=&quot;1.PersonType&quot;&gt;NaturalPerson&lt;/entry&gt;
 * &lt;entry key=&quot;1.Required&quot;&gt;true&lt;/entry&gt;
 * &lt;entry key=&quot;1.UniqueIdentifier&quot;&gt;true&lt;/entry&gt;
 * &lt;entry key=&quot;1.XmlType.NamespaceUri&quot;&gt;http://eidas.europa.eu/attributes/naturalperson&lt;/entry&gt;
 * &lt;entry key=&quot;1.XmlType.LocalPart&quot;&gt;PersonIdentifierType&lt;/entry&gt;
 * &lt;entry key=&quot;1.XmlType.NamespacePrefix&quot;&gt;eidas-natural&lt;/entry&gt;
 * &lt;entry key=&quot;1.AttributeValueMarshaller&quot;&gt;eu.eidas.auth.commons.attribute.impl.UnrestrictedStringAttributeValue&lt;/entry&gt;
 *
 * &lt;entry key=&quot;2.Name&quot;&gt;http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName&lt;/entry&gt;
 * &lt;entry key=&quot;2.FriendlyName&quot;&gt;FamilyName&lt;/entry&gt;
 * &lt;entry key=&quot;2.PersonType&quot;&gt;NaturalPerson&lt;/entry&gt;
 * &lt;entry key=&quot;2.Required&quot;&gt;true&lt;/entry&gt;
 * &lt;entry key=&quot;2.TransliterationMandatory&quot;&gt;true&lt;/entry&gt;
 * &lt;entry key=&quot;2.XmlType.NamespaceUri&quot;&gt;http://eidas.europa.eu/attributes/naturalperson&lt;/entry&gt;
 * &lt;entry key=&quot;2.XmlType.LocalPart&quot;&gt;CurrentFamilyNameType&lt;/entry&gt;
 * &lt;entry key=&quot;2.XmlType.NamespacePrefix&quot;&gt;eidas-natural&lt;/entry&gt;
 * &lt;entry key=&quot;2.AttributeValueMarshaller&quot;&gt;eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller&lt;/entry&gt;
 *
 * &lt;entry key=&quot;3.Name&quot;&gt;http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName&lt;/entry&gt;
 * &lt;entry key=&quot;3.FriendlyName&quot;&gt;FirstName&lt;/entry&gt;
 * &lt;entry key=&quot;3.PersonType&quot;&gt;NaturalPerson&lt;/entry&gt;
 * &lt;entry key=&quot;3.Required&quot;&gt;true&lt;/entry&gt;
 * &lt;entry key=&quot;3.TransliterationMandatory&quot;&gt;true&lt;/entry&gt;
 * &lt;entry key=&quot;3.XmlType.NamespaceUri&quot;&gt;http://eidas.europa.eu/attributes/naturalperson&lt;/entry&gt;
 * &lt;entry key=&quot;3.XmlType.LocalPart&quot;&gt;CurrentGivenNameType&lt;/entry&gt;
 * &lt;entry key=&quot;3.XmlType.NamespacePrefix&quot;&gt;eidas-natural&lt;/entry&gt;
 * &lt;entry key=&quot;3.AttributeValueMarshaller&quot;&gt;eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller&lt;/entry&gt;
 * &lt;/properties&gt;
 * </pre>
 *
 * @since 1.1
 */
public enum AttributeSetPropertiesConverter implements PropertiesConverter<SortedSet<AttributeDefinition<?>>> {

    INSTANCE;

    private enum Suffix {

        NAME_URI(".NameUri"),

        FRIENDLY_NAME(".FriendlyName"),

        PERSON_TYPE(".PersonType"),

        REQUIRED(".Required"),

        TRANSLITERATION_MANDATORY(".TransliterationMandatory"),

        UNIQUE_IDENTIFIER(".UniqueIdentifier"),

        XML_TYPE_NAMESPACE_URI(".XmlType.NamespaceUri"),

        XML_TYPE_LOCAL_PART(".XmlType.LocalPart"),

        XML_TYPE_NAMESPACE_PREFIX(".XmlType.NamespacePrefix"),

        ATTRIBUTE_VALUE_MARSHALLER(".AttributeValueMarshaller");

        private static final EnumMapper<String, Suffix> MAPPER =
                new EnumMapper<String, Suffix>(new KeyAccessor<String, Suffix>() {

                    @Nonnull
                    @Override
                    public String getKey(@Nonnull Suffix suffix) {
                        return suffix.getValue();
                    }
                }, Canonicalizers.trimLowerCase(), values());

        @Nullable
        public static Suffix fromString(@Nonnull String val) {
            return MAPPER.fromKey(val);
        }

        public static EnumMapper<String, Suffix> mapper() {
            return MAPPER;
        }

        @Nonnull
        private final transient String value;

        private final transient int length;

        Suffix(@Nonnull String val) {
            value = val;
            length = value.length();
        }

        @Nonnull
        public String computeEntry(@Nonnull String key) {
            Preconditions.checkNotBlank(key, "key");
            return key + value;
        }

        @Nonnull
        public String extractKeyPrefix(@Nonnull String entry) {
            if (!matches(entry)) {
                throw new IllegalArgumentException("entry \"" + entry + "\" does not match suffix \"" + value + "\"");
            }
            return entry.substring(0, entry.length() - length);
        }

        public int getLength() {
            return length;
        }

        @Nonnull
        public String getValue() {
            return value;
        }

        public boolean matches(@Nullable String entry) {
            return null != entry && entry.endsWith(value);
        }

        @Override
        public String toString() {
            return getValue();
        }
    }

    @Nonnull
    private static String getMandatoryString(@Nonnull Map<?, ?> properties,
                                             @Nonnull Suffix suffix,
                                             @Nonnull String id) {
        String key = suffix.computeEntry(id);
        String value = (String) properties.get(key);
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Entry \"" + key + "\" has an empty value");
        }
        return value;
    }

    private static boolean getOptionalFlag(@Nonnull Map<?, ?> properties, @Nonnull Suffix suffix, @Nonnull String id) {
        String key = suffix.computeEntry(id);
        String value = (String) properties.get(key);
        String trimmedValue = StringUtils.trim(value);
        if (StringUtils.isNotBlank(trimmedValue)) {
            boolean flag = "true".equalsIgnoreCase(trimmedValue);
            if (!flag && !"false".equalsIgnoreCase(trimmedValue)) {
                throw new IllegalArgumentException("Entry \"" + key + "\" has an illegal value \"" + value + "\"");
            }
            return flag;
        }
        return false;
    }

    /**
     * Converts the given properties into an immutable sorted set of <code>AttributeDefinition</code>s.
     *
     * @param properties the properties
     * @return an immutable sorted set of <code>AttributeDefinition</code>s.
     */
    @SuppressWarnings("CollectionDeclaredAsConcreteClass")
    @Nonnull
    public static SortedSet<AttributeDefinition<?>> toAttributeSet(@Nonnull Properties properties) {
        return toAttributeSetFromProperties(properties);
    }

    /**
     * Converts the given Map into an immutable sorted set of <code>AttributeDefinition</code>s.
     *
     * @param properties the Map
     * @return an immutable sorted set of <code>AttributeDefinition</code>s.
     */
    @Nonnull
    public static SortedSet<AttributeDefinition<?>> toAttributeSet(@Nonnull Map<String, String> properties) {
        return toAttributeSetFromProperties(properties);
    }

    @Nonnull
    private static SortedSet<AttributeDefinition<?>> toAttributeSetFromProperties(@Nonnull Map<?, ?> properties) {
        SortedSet<AttributeDefinition<?>> attributeDefinitions = new TreeSet<>();
        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            if (Suffix.NAME_URI.matches(key)) {
                String id = Suffix.NAME_URI.extractKeyPrefix(key);
                String fullName = getMandatoryString(properties, Suffix.NAME_URI, id);
                String friendlyName = getMandatoryString(properties, Suffix.FRIENDLY_NAME, id);
                boolean required = getOptionalFlag(properties, Suffix.REQUIRED, id);
                boolean transliterationMandatory = getOptionalFlag(properties, Suffix.TRANSLITERATION_MANDATORY, id);
                boolean uniqueIdentifier = getOptionalFlag(properties, Suffix.UNIQUE_IDENTIFIER, id);
                String typeStr = getMandatoryString(properties, Suffix.PERSON_TYPE, id);
                PersonType personType = PersonType.fromString(typeStr);
                if (null == personType) {
                    throw new IllegalArgumentException(
                            "Entry \"" + Suffix.PERSON_TYPE.computeEntry(id) + "\" has an illegal value \"" + typeStr
                                    + "\"");
                }
                String xmlNamespaceUri = getMandatoryString(properties, Suffix.XML_TYPE_NAMESPACE_URI, id);
                String localPart = getMandatoryString(properties, Suffix.XML_TYPE_LOCAL_PART, id);
                String xmlNamespacePrefix = getMandatoryString(properties, Suffix.XML_TYPE_NAMESPACE_PREFIX, id);
                String attributeValueMarshaller = getMandatoryString(properties, Suffix.ATTRIBUTE_VALUE_MARSHALLER, id);

                AttributeDefinition.Builder<?> builder = AttributeDefinition.builder();
                builder.nameUri(fullName);
                builder.friendlyName(friendlyName);
                builder.personType(personType);
                builder.required(required);
                builder.transliterationMandatory(transliterationMandatory);
                builder.uniqueIdentifier(uniqueIdentifier);
                builder.xmlType(xmlNamespaceUri, localPart, xmlNamespacePrefix);
                builder.attributeValueMarshaller(attributeValueMarshaller);
                attributeDefinitions.add(builder.build());
            }
        }
        return Collections.unmodifiableSortedSet(attributeDefinitions);
    }

    /**
     * Converts the given Iterable of <code>AttributeDefinition</code>s to an immutable sorted Map of Strings.
     *
     * @param attributes the Iterable of <code>AttributeDefinition</code>s
     * @return an immutable sorted Map of Strings.
     */
    @Nonnull
    public static SortedMap<String, String> toMap(@Nonnull Iterable<AttributeDefinition<?>> attributes) {
        TreeMap<String, String> map = new TreeMap<>();
        int i = 0;
        for (AttributeDefinition<?> attributeDefinition : attributes) {
            String id = String.valueOf(++i);
            map.put(Suffix.NAME_URI.computeEntry(id), attributeDefinition.getNameUri().toASCIIString());
            map.put(Suffix.FRIENDLY_NAME.computeEntry(id), attributeDefinition.getFriendlyName());
            map.put(Suffix.PERSON_TYPE.computeEntry(id), attributeDefinition.getPersonType().getValue());
            map.put(Suffix.REQUIRED.computeEntry(id), String.valueOf(attributeDefinition.isRequired()));
            if (attributeDefinition.isTransliterationMandatory()) {
                map.put(Suffix.TRANSLITERATION_MANDATORY.computeEntry(id), Boolean.TRUE.toString());
            }
            if (attributeDefinition.isUniqueIdentifier()) {
                map.put(Suffix.UNIQUE_IDENTIFIER.computeEntry(id), Boolean.TRUE.toString());
            }
            QName xmlType = attributeDefinition.getXmlType();
            map.put(Suffix.XML_TYPE_NAMESPACE_URI.computeEntry(id), String.valueOf(xmlType.getNamespaceURI()));
            map.put(Suffix.XML_TYPE_LOCAL_PART.computeEntry(id), String.valueOf(xmlType.getLocalPart()));
            map.put(Suffix.XML_TYPE_NAMESPACE_PREFIX.computeEntry(id), String.valueOf(xmlType.getPrefix()));
            map.put(Suffix.ATTRIBUTE_VALUE_MARSHALLER.computeEntry(id),
                    attributeDefinition.getAttributeValueMarshaller().getClass().getName());
        }
        return map;
    }

    /**
     * Converts the given Iterable of <code>AttributeDefinition</code>s to a Properties.
     *
     * @param attributes the Iterable of <code>AttributeDefinition</code>s
     * @return a Properties.
     */
    @Nonnull
    public static Properties toProperties(@Nonnull Iterable<AttributeDefinition<?>> attributes) {
        SortedMap<String, String> sortedMap = toMap(attributes);
        Properties properties = new PrintSortedProperties();
        //noinspection UseOfPropertiesAsHashtable
        properties.putAll(sortedMap);
        return properties;
    }

    @Nonnull
    @Override
    public Properties marshal(@Nonnull SortedSet<AttributeDefinition<?>> value) {
        return toProperties(value);
    }

    @Nonnull
    @Override
    public SortedSet<AttributeDefinition<?>> unmarshal(@Nonnull Properties properties) {
        return toAttributeSet(properties);
    }
}
