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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.util.Preconditions;

/**
 * Static helper methods.
 *
 * @deprecated This class has more than one responsibility and relies on a mutable static state with is setup in an
 * awkward way.
 */
@SuppressWarnings("ConstantConditions")
@Deprecated
public class EIDASUtil {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EIDASUtil.class);

    private static final Pattern SEMI_COLON_SEPARATOR_PATTERN = Pattern.compile(";");

    @SuppressWarnings("CollectionDeclaredAsConcreteClass")
    @Nonnull
    static ImmutableMap<String, String> immutableMap(@Nullable Properties properties) {
        if (null == properties || properties.isEmpty()) {
            return ImmutableMap.of();
        }
        return Maps.fromProperties(properties);
    }

    @Nonnull
    static Properties toProperties(@Nonnull ImmutableMap<String, String> immutableMap) {
        Properties properties = new Properties();
        //noinspection UseOfPropertiesAsHashtable
        properties.putAll(immutableMap);
        return properties;
    }

    /**
     * Gets the Eidas error code in the error message if exists!
     *
     * @param errorMessage The message to get the error code if exists;
     * @return the error code if exists. Returns null otherwise.
     */
    public static String getEidasErrorCode(final String errorMessage) {
        if (StringUtils.isNotBlank(errorMessage)
                && errorMessage.indexOf(EIDASValues.ERROR_MESSAGE_SEP.toString()) >= 0) {
            final String[] msgSplitted = errorMessage.split(EIDASValues.ERROR_MESSAGE_SEP.toString());
            if (msgSplitted.length == 2 && StringUtils.isNumeric(msgSplitted[0])) {
                return msgSplitted[0];
            }
        }
        return null;
    }

    /**
     * Gets the Eidas error message in the saml message if exists!
     *
     * @param errorMessage The message to get in the saml message if exists;
     * @return the error message if exists. Returns the original message otherwise.
     */
    public static String getEidasErrorMessage(final String errorMessage) {
        if (StringUtils.isNotBlank(errorMessage)
                && errorMessage.indexOf(EIDASValues.ERROR_MESSAGE_SEP.toString()) >= 0) {
            final String[] msgSplitted = errorMessage.split(EIDASValues.ERROR_MESSAGE_SEP.toString());
            if (msgSplitted.length == 2 && StringUtils.isNumeric(msgSplitted[0])) {
                return msgSplitted[1];
            }
        }
        return errorMessage;
    }

    /**
     * @param values a string containing several chunks separated by ;
     * @return a set of chunks extracted from values
     */
    @Nonnull
    public static Set<String> parseSemicolonSeparatedList(@Nullable String values) {
        Set<String> result = new HashSet<String>();
        if (!StringUtils.isEmpty(values)) {
            String[] valuesArr = SEMI_COLON_SEPARATOR_PATTERN.split(values);
            if (valuesArr != null) {
                for (String value : valuesArr) {
                    value = value.trim();
                    if (!StringUtils.isEmpty(value)) {
                        result.add(value);
                    }
                }
            }
        }
        return result;
    }
}
