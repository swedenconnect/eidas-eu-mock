/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.eidas.auth.commons;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

/**
 * This class holds static helper methods.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com,
 *         luis.felix@multicert.com, hugo.magalhaes@multicert.com,
 *         paulo.ribeiro@multicert.com
 * @version $Revision: 1.5 $, $Date: 2010-12-15 23:19:59 $
 */
public final class AttributeUtil {

    /**
     * Private constructor. Prevents the class from being instantiated.
     */
    private AttributeUtil() {
        // empty constructor
    }

    /**
     * Safe escape any given string.
     *
     * @param value The HTTP Value to escaped.
     * @return The value escaped value.
     */
    public static String escape(final String value) {

        final String attrSep = EIDASValues.ATTRIBUTE_SEP.toString();
        final String attrTupleSep = EIDASValues.ATTRIBUTE_TUPLE_SEP.toString();
        final String attrValueSep = EIDASValues.ATTRIBUTE_VALUE_SEP.toString();

        final String escAttrSep = "%" + (int) attrSep.charAt(0);
        final String escAttrTupleSep = "%" + (int) attrTupleSep.charAt(0);
        final String escAttrValueSep = "%" + (int) attrValueSep.charAt(0);

        return value.replace(attrSep, escAttrSep)
                .replace(attrTupleSep, escAttrTupleSep)
                .replace(attrValueSep, escAttrValueSep);
    }

    /**
     * Unescape any given string.
     *
     * @param value The HTTP Value to be unescaped.
     * @return The value unescaped value.
     */
    public static String unescape(final String value) {
        final String attrSep = EIDASValues.ATTRIBUTE_SEP.toString();
        final String attrTupleSep = EIDASValues.ATTRIBUTE_TUPLE_SEP.toString();
        final String attrValueSep = EIDASValues.ATTRIBUTE_VALUE_SEP.toString();

        final String escAttrSep = "%" + (int) attrSep.charAt(0);
        final String escAttrTupleSep = "%" + (int) attrTupleSep.charAt(0);
        final String escAttrValueSep = "%" + (int) attrValueSep.charAt(0);

        return value.replace(escAttrSep, attrSep)
                .replace(escAttrTupleSep, attrTupleSep)
                .replace(escAttrValueSep, attrValueSep);
    }

    /**
     * Appends the string representation of an object to a StringBuilder.
     *
     * @param strBuilder The StringBuilder to append to.
     * @param val        The string representation of an object.
     */
    public static void appendIfNotNull(final StringBuilder strBuilder,
                                       final Object val) {

        if (val != null) {
            strBuilder.append(val);
        }
    }

    /**
     * Given a separator and a list of strings, joins the list, as a string,
     * separated by the separator string.
     *
     * @param list      The list of strings to join.
     * @param separator The separator string.
     * @return the list, as a string, separated by the separator string.
     */
    public static String listToString(final List<String> list,
                                      final String separator) {

        final StringBuilder strBuilder = new StringBuilder();
        for (final String s : list) {
            if (!StringUtils.isEmpty(s)) {
                strBuilder.append(AttributeUtil.escape(s) + separator);
            }
        }
        return strBuilder.toString();
    }

    /**
     * Given a separator and a map of strings to strings, joins the map, as a
     * string, separated by the separator string with the pair key/value
     * concatenated with a '='.
     *
     * @param map       The map of strings to join.
     * @param separator The separator string.
     * @return the map of strings, as a string, separated by the separator string
     * with the pair key/value concatenated with a '='.
     */
    public static String mapToString(final Map<String, String> map,
                                     final String separator) {

        final StringBuilder strBuilder = new StringBuilder();
        final Iterator<String> orderedKeys=new TreeSet<String>(map.keySet()).iterator();
        while (orderedKeys.hasNext()) {
            final String key=orderedKeys.next();
            final String entryValue = map.get(key);
            strBuilder.append(key);
            strBuilder.append('=');
            strBuilder.append(AttributeUtil.escape(entryValue));
            strBuilder.append(separator);
        }
        return strBuilder.toString();
    }

    /**
     * Validates the attribute value format.
     *
     * @param value The attribute value to validate.
     * @return true if value has a valid format.
     */
    public static boolean isValidValue(final String value) {
        boolean retVal = false;
        if (value != null && value.charAt(0) == '[' && value.endsWith("]")) {
            final String tmpAttrValue = value.substring(1, value.length() - 1);
            final String[] vals =
                    tmpAttrValue.split(EIDASValues.ATTRIBUTE_VALUE_SEP.toString());

            if (tmpAttrValue.length() >= 0
                    || (vals.length > 0 && vals[0].length() > 0)) {
                retVal = true;
            }
        }
        return retVal;
    }

    /**
     * Validates the attribute type value. It's case insensitive. E.g. return true
     * value to: a) "true", "TRUE", "True", ... b) "false", "FALSE", "False", ...
     *
     * @param type The attribute type value.
     * @return true if type has a true or false (case insensitive) value.
     */
    public static boolean isValidType(final String type) {
        return StringUtils.isNotEmpty(type) && (EIDASValues.TRUE.toString().equalsIgnoreCase(type) || EIDASValues.FALSE.toString().equalsIgnoreCase(type));
    }

    /**
     * Validates the Personal attribute tuple. E.g. name:type:[value]:status
     *
     * @param tuples The Personal attribute's tuple.
     * @return true if the tuples' format is valid.
     * @see EIDASUtil#validateParameter(String, String, String)
     * @see String#equalsIgnoreCase(String)
     */
    public static boolean hasValidTuples(final String[] tuples) {
        boolean retVal = false;

        final int numberTuples = AttributeConstants.NUMBER_TUPLES.intValue();
        if (tuples != null && tuples.length == numberTuples) {
            // validate attrName
            final int attrNameIndex = AttributeConstants.ATTR_NAME_INDEX.intValue();
            final int attrTypeIndex = AttributeConstants.ATTR_TYPE_INDEX.intValue();
            final int attrValueIndex = AttributeConstants.ATTR_VALUE_INDEX.intValue();

            retVal = StringUtils.isNotEmpty(tuples[attrNameIndex]) &&
                    StringUtils.isNotEmpty(tuples[attrTypeIndex]) &&
                    StringUtils.isNotEmpty(tuples[attrValueIndex]);
            retVal = retVal && AttributeUtil.isValidType(tuples[attrTypeIndex]) &&
                    AttributeUtil.isValidValue(tuples[attrValueIndex]);
        }
        return retVal;
    }

    /**
     * Check if all mandatory attributes have values.
     *
     * @param personalAttrList The Personal Attributes List.
     * @return true if all mandatory attributes have values, false if at least one
     * attribute doesn't have value.
     */
    public static boolean checkMandatoryAttributes(final IPersonalAttributeList personalAttrList) {
        for (final PersonalAttribute personalAttribute : personalAttrList) {
            if (personalAttribute.isRequired() && personalAttribute.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Obtain the list of missing mandatory attributes.
     *
     * @param personalAttrList The Personal Attributes List.
     * @return the comma separated list of mandatory attributes that doesn't have value.
     */
    public static String getMissingMandatoryAttributes(final IPersonalAttributeList personalAttrList) {
        StringBuilder listOfMissingAttributes = new StringBuilder();
        for (final PersonalAttribute personalAttribute : personalAttrList) {
            if (personalAttribute.isRequired() && personalAttribute.isEmpty()) {
                if (listOfMissingAttributes.length() > 0) {
                    listOfMissingAttributes.append(", ");
                }
                listOfMissingAttributes.append(personalAttribute.getName());
            }
        }
        return listOfMissingAttributes.toString();
    }
}
