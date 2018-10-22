package eu.eidas.auth.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;

import eu.eidas.util.Preconditions;

/**
 * Converts {@link PersonalAttribute} objects to their String representations and vice versa.
 * <p/>
 * The PersonalAttribute format is as follows:
 * <p/>
 * {@code name:required:[v,a,l,u,e,s]|[v=a,l=u,e=s]:status;}
 * <p/>
 *
 * @since 1.1
 */
public final class PersonalAttributeString {

    public static final String HTTP_PREFIX = "http://";

    //////////////////

    /**
     * Converts the attribute tuple (attrName:attrType...) to the new format.
     *
     * @param tuples The attribute tuples to convert.
     * @return The attribute tuples in the new format.
     */
    @Nonnull
    private static String[] convertFormat(@Nullable String[] tuples) {
        final String[] newFormatTuples = new String[AttributeConstants.NUMBER_TUPLES.intValue()];
        if (null != tuples) {
            System.arraycopy(tuples, 0, newFormatTuples, 0, tuples.length);

            for (int i = tuples.length; i < newFormatTuples.length; i++) {
                if (i == AttributeConstants.ATTR_VALUE_INDEX.intValue()) {
                    newFormatTuples[i] = "[]";
                } else {
                    newFormatTuples[i] = "";
                }
            }
        }
        return newFormatTuples;
    }

    /**
     * Validates and creates the attribute's complex values.
     *
     * @param values The complex values.
     * @return The {@link Map} with the complex values.
     * @see Map
     */
    @Nonnull
    private static Map<String, String> createComplexValue(@Nonnull String[] values) {
        final Map<String, String> complexValue = new HashMap<String, String>();
        for (final String val : values) {
            final String[] tVal = val.split("=");
            if (StringUtils.isNotEmpty(val) && tVal.length == 2) {
                complexValue.put(tVal[0], AttributeUtil.unescape(tVal[1]));
            }
        }
        return complexValue;
    }

    /**
     * Validates and creates the attribute values.
     *
     * @param vals The attribute values.
     * @return The {@link List} with the attribute values.
     * @see List
     */
    @Nonnull
    private static List<String> createValues(@Nonnull String[] vals) {
        final List<String> values = new ArrayList<String>();
        for (final String val : vals) {
            if (StringUtils.isNotEmpty(val)) {
                values.add(AttributeUtil.unescape(val));
            }
        }
        return values;
    }

    /**
     * Converts a string representation of the Personal Attribute into an instance of the {@link PersonalAttribute}.
     *
     * @param attributeString the string representation of the Personal Attribute
     * @return an instance of the {@link PersonalAttribute}.
     */
    @Nonnull
    public static PersonalAttribute fromString(@Nonnull String attributeString) {
        String[] tuples;
        // TODO  its not nice
        if (attributeString.startsWith(HTTP_PREFIX)) {
            tuples = attributeString.substring(HTTP_PREFIX.length()).split(EIDASValues.ATTRIBUTE_TUPLE_SEP.toString(),
                    AttributeConstants.NUMBER_TUPLES.intValue());
            tuples[0] = HTTP_PREFIX + tuples[0];
        } else {
            tuples = attributeString.split(EIDASValues.ATTRIBUTE_TUPLE_SEP.toString(),
                    AttributeConstants.NUMBER_TUPLES.intValue());
        }

        // Convert to the new format if needed!
        tuples = convertFormat(tuples);

        if (!AttributeUtil.hasValidTuples(tuples)) {
            throw new IllegalArgumentException("Invalid personal attribute list tuples: " + Arrays.toString(tuples));
        }
        final int attrValueIndex = AttributeConstants.ATTR_VALUE_INDEX.intValue();
        final String tmpAttrValue = tuples[attrValueIndex].substring(1, tuples[attrValueIndex].length() - 1);
        final String[] vals = tmpAttrValue.split(EIDASValues.ATTRIBUTE_VALUE_SEP.toString());
        String name = tuples[AttributeConstants.ATTR_NAME_INDEX.intValue()];
        String friendlyName = PersonalAttribute.extractFriendlyName(name);
        final PersonalAttribute persAttr = new PersonalAttribute(name, friendlyName);
        persAttr.setIsRequired(Boolean.valueOf(tuples[AttributeConstants.ATTR_TYPE_INDEX.intValue()]).booleanValue());
        // check if it is a complex value
        if (friendlyName.equals(
                EidasParameterKeys.COMPLEX_ADDRESS_VALUE.toString())) {
            persAttr.setComplexValue(createComplexValue(vals));
        } else {
            persAttr.setValue(createValues(vals));
        }

        if (tuples.length == AttributeConstants.NUMBER_TUPLES.intValue()) {
            String status = tuples[AttributeConstants.ATTR_STATUS_INDEX.intValue()];
            if (StringUtils.isNotEmpty(status)) {
                PersonalAttribute.Status statusConstant = PersonalAttribute.Status.fromString(status);
                if (null == statusConstant) {
                    throw new IllegalArgumentException(
                            "Illegal status: \"" + status + "\" in attributeString \"" + attributeString + "\"");
                }
                PersonalAttribute.Status actual = persAttr.getStatus();
                if (statusConstant != actual) {
                    throw new IllegalArgumentException(
                            "Inconsistent status: \"" + status + "\" in attributeString \"" + attributeString
                                    + "\", it should be \"" +
                                    actual + "\"");
                }
            }
        }
        return persAttr;
    }

    /**
     * Creates a Personal Attribute List from a String representing an Attribute List.
     *
     * @param attrList String representing the attribute list.
     * @throws IllegalArgumentException if the string representation using tuples is invalid
     */
    @Nonnull
    public static PersonalAttributeList fromStringList(@Nonnull String attrList) {
        PersonalAttributeList pal = new PersonalAttributeList();
        final StringTokenizer strToken = new StringTokenizer(attrList, EIDASValues.ATTRIBUTE_SEP.toString());

        while (strToken.hasMoreTokens()) {
            PersonalAttribute persAttr = fromString(strToken.nextToken());
            pal.add(persAttr);
        }
        return pal;
    }

    /**
     * Prints the PersonalAttribute in the following format. name:required:[v,a,l,u,e,s]|[v=a,l=u,e=s]:status;
     *
     * @return The PersonalAttribute as a string.
     */
    @Nonnull
    public static String toString(@Nonnull PersonalAttribute personalAttribute) {
        Preconditions.checkNotNull(personalAttribute, "personalAttribute");
        final PersonalAttribute pal = personalAttribute;
        final StringBuilder strBuild = new StringBuilder();
        synchronized (pal) {

            String name = pal.getName();
            boolean required = pal.isRequired();
            AttributeUtil.appendIfNotNull(strBuild, name);
            strBuild.append(EIDASValues.ATTRIBUTE_TUPLE_SEP.toString());
            AttributeUtil.appendIfNotNull(strBuild, String.valueOf(required));
            strBuild.append(EIDASValues.ATTRIBUTE_TUPLE_SEP.toString());
            strBuild.append('[');

            if (pal.isEmptyValue()) {
                if (!pal.isEmptyComplexValue()) {
                    AttributeUtil.appendIfNotNull(strBuild, AttributeUtil.mapToString(pal.getComplexValue(),
                                                                                      EIDASValues.ATTRIBUTE_VALUE_SEP.toString()));
                }
            } else {
                AttributeUtil.appendIfNotNull(strBuild, AttributeUtil.listToString(pal.getValue(),
                                                                                   EIDASValues.ATTRIBUTE_VALUE_SEP.toString()));
            }

            strBuild.append(']');
            strBuild.append(EIDASValues.ATTRIBUTE_TUPLE_SEP.toString());
            AttributeUtil.appendIfNotNull(strBuild, pal.getStatus());
            strBuild.append(EIDASValues.ATTRIBUTE_SEP.toString());

        }
        return strBuild.toString();
    }

    /**
     * Creates a string in the following format.
     * <p/>
     * attrName:attrType:[attrValue1,attrValue2=attrComplexValue]:attrStatus;
     *
     * @return {@inheritDoc}
     */
    @Nonnull
    public static String toStringList(@Nonnull PersonalAttributeList personalAttributeList) {
        final StringBuilder strBuilder = new StringBuilder();
        for (PersonalAttribute personalAttribute : personalAttributeList) {
            if (null != personalAttribute) {
                strBuilder.append(toString(personalAttribute));
            }
        }
        return strBuilder.toString();
    }

    private PersonalAttributeString() {
    }
}
