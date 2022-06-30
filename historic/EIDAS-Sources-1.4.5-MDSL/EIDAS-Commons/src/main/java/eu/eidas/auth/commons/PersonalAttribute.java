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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang.StringUtils;
import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;
import eu.eidas.util.Preconditions;

/**
 * Stores all the information relative to the PersonalAttribute.
 * <p/>
 * Note that every iterator obtained from this class MUST synchronize around its iteration loop.
 *
 * @deprecated use {@link eu.eidas.auth.model.attribute.ImmutablePersonalAttribute} instead.
 */
@Deprecated
@ThreadSafe
public final class PersonalAttribute {

    public enum Status {
        /**
         * Attribute is Available.
         */
        AVAILABLE("Available"),

        /**
         * Attribute is NotAvailable.
         */
        NOT_AVAILABLE("NotAvailable");

        @Nonnull
        private final transient String value;

        private static final EnumMapper<String, Status> MAPPER =
                new EnumMapper<String, Status>(new KeyAccessor<String, Status>() {

                    @Nonnull
                    @Override
                    public String getKey(@Nonnull Status stat) {
                        return stat.getValue();
                    }
                }, Canonicalizers.trimLowerCase(), values());

        Status(@Nonnull String val) {
            value = val;
        }

        @Nullable
        public static Status fromString(@Nonnull String val) {
            return MAPPER.fromKey(val);
        }

        public static EnumMapper<String, Status> mapper() {
            return MAPPER;
        }

        @Nonnull
        public String getValue() {
            return value;
        }

        @Nonnull
        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * Name of the personal attribute as a URI.
     * <p/>
     * This is the full name as in {@code http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName}.
     */
    private final String name;

    /**
     * Friendly name of this personal attribute.
     * <p/>
     * For example for {@code http://eidas.europa.eu/attributes/naturalperson/CurrentGivenName}, the friendly name is
     * {@code FirstName}.
     */
    private final String friendlyName;

    /**
     * Values of the personal attribute.
     */
    private List<String> value = Collections.synchronizedList(new ArrayList<String>());

    /**
     * Complex values of the personal attribute.
     */
    private Map<String, String> complexValue = Collections.synchronizedMap(new HashMap<String, String>());

    /**
     * Is the personal attribute mandatory?
     */
    private boolean required;

    /**
     * set to true when the attribute is an eIDAS natural person attribute
     */
    private boolean eidasNaturalPersonAttr;

    /**
     * set to true when the attribute is an eIDAS legal person attribute
     */
    private boolean eidasLegalPersonAttr;

    /**
     * Copy constructor which can change the name and copies all values.
     *
     * @param copy the instance to copy
     */
    public PersonalAttribute(@Nonnull PersonalAttribute copy,
                             @Nonnull String newName,
                             @Nonnull String newFriendlyName) {
        this(copy, newName, newFriendlyName, true);
    }

    /**
     * Copy constructor which keeps the same name and copies all values.
     *
     * @param copy the instance to copy
     */
    public PersonalAttribute(@Nonnull PersonalAttribute copy) {
        this(copy, copy.getName(), copy.getFriendlyName(), true);
    }

    /**
     * Copy constructor
     *
     * @param copy the instance to copy
     * @param copyAllValues {@code true} if all the values must also be copied.
     */
    private PersonalAttribute(@Nonnull PersonalAttribute copy,
                              @Nonnull String newName,
                              @Nonnull String newFriendlyName,
                              boolean copyAllValues) {
        Preconditions.checkNotNull(copy, "copy");
        Preconditions.checkNotNull(newName, "newName");
        Preconditions.checkNotNull(newFriendlyName, "newFriendlyName");
        name = newName;
        friendlyName = newFriendlyName;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (copy) {
            // lock obtention optimization // no deadlock possibility since this instance is not created yet
            //noinspection NestedSynchronizedStatement
            synchronized (this) {
                if (copyAllValues) {
                    setValue(copy.getValue());
                    setComplexValue(copy.getComplexValue());
                }
                setIsRequired(copy.isRequired());
                setEidasNaturalPersonAttr(copy.isEidasNaturalPersonAttr());
                setEidasLegalPersonAttr(copy.isEidasLegalPersonAttr());
            }
        }
    }

    /**
     * Default Constructor.
     */
    public PersonalAttribute(@Nonnull String name, @Nonnull String friendlyName) {
        String tmpFriendlyName = friendlyName;
        Preconditions.checkNotNull(name, "name");
        if (StringUtils.isBlank(tmpFriendlyName)) {
            tmpFriendlyName = extractFriendlyName(name);
        }
        this.name = name;
        this.friendlyName = friendlyName;
    }

    public PersonalAttribute(@Nonnull String name, @Nonnull String friendlyName, boolean isRequired) {
        this(name,friendlyName);
        setIsRequired(isRequired);
    }

    /**
     * PersonalAttribute Constructor for complex values.
     *
     * @param attrName The attribute name.
     * @param attrIsRequired The attribute type value.
     * @param simpleValue The attribute's value.
     */
    public PersonalAttribute(@Nonnull String attrName,
                             @Nonnull String friendlyName,
                             final boolean attrIsRequired,
                             @Nonnull List<String> simpleValue) {
        this(attrName, friendlyName);
        // lock obtention optimization
        synchronized (this) {
            this.setIsRequired(attrIsRequired);
            this.setValue(simpleValue);
        }
    }

    /**
     * PersonalAttribute Constructor for complex values.
     *
     * @param attrName The attribute name.
     * @param attrIsRequired The attribute type value.
     * @param attrComplexValue The attribute's complex value.
     */
    @Deprecated
    public PersonalAttribute(@Nonnull String attrName,
                             @Nonnull String friendlyName,
                             final boolean attrIsRequired,
                             @Nonnull Map<String, String> attrComplexValue) {
        this(attrName, friendlyName);
        // lock obtention optimization
        synchronized (this) {
            this.setIsRequired(attrIsRequired);
            this.setComplexValue(attrComplexValue);
        }
    }

    /**
     * Static factory to create a new copy of the given instance, including all values.
     *
     * @param copy the instance to copy
     * @return a new copy
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static PersonalAttribute copyOf(@Nullable PersonalAttribute copy) {
        if (null == copy) {
            return null;
        }
        return new PersonalAttribute(copy, copy.getName(), copy.getFriendlyName(), true);
    }

    /**
     * Static factory to create a new copy of the given instance, excluding all values.
     *
     * @param copy the instance to copy without any value
     * @return a new copy without any value
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static PersonalAttribute copyOfWithoutValues(@Nullable PersonalAttribute copy) {
        if (null == copy) {
            return null;
        }
        return new PersonalAttribute(copy, copy.getName(), copy.getFriendlyName(), false);
    }

    /**
     * Static factory to create a new copy of the given instance, including all values and which can change the name.
     *
     * @param copy the instance to copy
     * @return a new copy
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static PersonalAttribute copyOfWithNewName(@Nullable PersonalAttribute copy,
                                                      @Nonnull String newName,
                                                      @Nonnull String newFriendlyName) {
        if (null == copy) {
            return null;
        }
        return new PersonalAttribute(copy, newName, newFriendlyName, true);
    }

    /**
     * Static factory to create a new copy of the given instance, excluding all values and which can change the name.
     *
     * @param copy the instance to copy without any value
     * @return a new copy without any value
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static PersonalAttribute copyOfWithNewNameWithoutValues(@Nullable PersonalAttribute copy,
                                                                   @Nonnull String newName,
                                                                   @Nonnull String newFriendlyName) {
        if (null == copy) {
            return null;
        }
        return new PersonalAttribute(copy, newName, newFriendlyName, false);
    }

    /**
     * Guesses the friendly name from the given full attribute name URI.
     *
     * @param name the attribute name URI
     * @return the guessed friendly name
     */
    public static String extractFriendlyName(@Nonnull String name) {
        Preconditions.checkNotNull(name, "name");
        if (StringUtils.isBlank(name)) {
            return StringUtils.EMPTY;
        }
        int lastIndexOf = name.lastIndexOf('/');
        if (lastIndexOf == -1 || lastIndexOf == name.length() - 1) {
            return name;
        }
        return name.substring(lastIndexOf + 1);
    }

    /**
     * Getter for the required value.
     *
     * @return The required value.
     */
    public synchronized boolean isRequired() {
        return required;
    }

    /**
     * Setter for the required value.
     *
     * @param attrIsRequired this attribute?
     */
    public synchronized void setIsRequired(final boolean attrIsRequired) {
        this.required = attrIsRequired;
    }

    /**
     * Returns the name of this personal attribute as a URI.
     * <p/>
     * This is the full name as in {@code http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName}.
     *
     * @return The name value.
     */
    public String getName() {
        return name;
    }

    /**
     * /** Friendly Name of this personal attribute.
     * <p/>
     * For example, for {@code http://eidas.europa.eu/attributes/naturalperson/CurrentFamilyName}, the friendly name is
     * {@code CurrentFamilyName}.
     *
     * @return The friendly name value.
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Getter for the value.
     *
     * @return The list of values.
     */
    @Nonnull
    public synchronized List<String> getValue() {
        return value;
    }

    @Nonnull
    public synchronized String getDisplayValue() {
        if (value.size() == 1) {
            return value.get(0);
        } else {
            return value.toString();
        }
    }

    /**
     * Setter for the list of values.
     *
     * @param attrValue The personal attribute value.
     */
    public synchronized void setValue(@Nullable List<String> attrValue) {
        // no defensive copy needed when there is no reference update
        if (value == attrValue) {
            return;
        }
        if (null == attrValue || attrValue.isEmpty()) {
            value.clear();
            return;
        }
        ArrayList<String> defensiveCopy = new ArrayList<String>();
        defensiveCopy.addAll(attrValue);
        value = Collections.synchronizedList(defensiveCopy);
    }

    /**
     * Getter for the status.
     *
     * @return The status.
     */
    public Status getStatus() {
        if (isEmpty()) {
            return Status.NOT_AVAILABLE;
        }
        return Status.AVAILABLE;
    }

    /**
     * Returns {@code true} if this attribute has no simple value and no complex value, {@code false} otherwise.
     *
     * @return {@code true} if this attribute has no simple value and no complex value, {@code false} otherwise.
     */
    public synchronized boolean isEmpty() {
        // atomically check both the list and the map
        return isEmptyValue() && isEmptyComplexValue();
    }

    /**
     * Getter for the complex value.
     *
     * @return The complex value.
     */
    @Nonnull
    public synchronized Map<String, String> getComplexValue() {
        return complexValue;
    }

    /**
     * Setter for the complex value.
     *
     * @param complexVal The personal attribute Complex value.
     */
    public synchronized void setComplexValue(@Nullable Map<String, String> complexVal) {
        // no defensive copy needed when there is no reference update
        if (this.complexValue == complexVal) {
            return;
        }
        if (null == complexVal || complexVal.isEmpty()) {
            this.complexValue.clear();
            return;
        }
        this.complexValue = Collections.synchronizedMap(new HashMap<String, String>(complexVal));
    }

    /**
     * Return true the value is empty.
     *
     * @return True if the value is empty "[]";
     */
    public synchronized boolean isEmptyValue() {
        return value.isEmpty() || (value.size() == 1 && StringUtils.isEmpty(value.get(0)));
    }

    /**
     * Returns true if the Complex Value is empty.
     *
     * @return True if the Complex Value is empty;
     */
    public synchronized boolean isEmptyComplexValue() {
        return complexValue.isEmpty();
    }

    public synchronized boolean isEidasNaturalPersonAttr() {
        return eidasNaturalPersonAttr;
    }

    public synchronized void setEidasNaturalPersonAttr(boolean eidasNaturalPersonAttrArg) {
        this.eidasNaturalPersonAttr = eidasNaturalPersonAttrArg;
    }

    public synchronized boolean isEidasLegalPersonAttr() {
        return eidasLegalPersonAttr;
    }

    public synchronized void setEidasLegalPersonAttr(boolean eidasLegalPersonAttrArg) {
        this.eidasLegalPersonAttr = eidasLegalPersonAttrArg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PersonalAttribute that = (PersonalAttribute) o;

        // Open call for final fields:
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (friendlyName != null ? !friendlyName.equals(that.friendlyName) : that.friendlyName != null) {
            return false;
        }

        synchronized (this) {
            // A deadlock is possible here if 2 threads try to equals() each other instances...
            synchronized (that) {
                if (required != that.required) {
                    return false;
                }
                if (eidasNaturalPersonAttr != that.eidasNaturalPersonAttr) {
                    return false;
                }
                if (eidasLegalPersonAttr != that.eidasLegalPersonAttr) {
                    return false;
                }
                if (value != null ? !value.equals(that.value) : that.value != null) {
                    return false;
                }
                return complexValue != null ? complexValue.equals(that.complexValue) : that.complexValue == null;
            }
        }
    }

    @Override
    public synchronized int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (friendlyName != null ? friendlyName.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (complexValue != null ? complexValue.hashCode() : 0);
        result = 31 * result + (required ? 1 : 0);
        result = 31 * result + (eidasNaturalPersonAttr ? 1 : 0);
        result = 31 * result + (eidasLegalPersonAttr ? 1 : 0);
        return result;
    }

    /**
     * Prints the PersonalAttribute in the following format. name:required:[v,a,l,u,e,s]|[v=a,l=u,e=s]:status;
     *
     * @return The PersonalAttribute as a string.
     */
    @Override
    public synchronized String toString() {
        return PersonalAttributeString.toString(this);
    }
}