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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * This enum class contains the SAML Token Status Code.
 */
public enum EIDASStatusCode {

    /**
     * URI for Requester status code.
     */
    REQUESTER_URI("urn:oasis:names:tc:SAML:2.0:status:Requester"),

    /**
     * URI for Responder status code.
     */
    RESPONDER_URI("urn:oasis:names:tc:SAML:2.0:status:Responder"),

    /**
     * URI for Success status code.
     */
    SUCCESS_URI("urn:oasis:names:tc:SAML:2.0:status:Success"),

    // put the ; on a separate line to make merges easier
    ;

    /**
     * Represents the constant's value.
     */
    @Nonnull
    private final transient String value;

    private static final EnumMapper<String, EIDASStatusCode> MAPPER =
            new EnumMapper<String, EIDASStatusCode>(new KeyAccessor<String, EIDASStatusCode>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull EIDASStatusCode eidasStatusCode) {
                    return eidasStatusCode.getValue();
                }
            }, Canonicalizers.trimLowerCase(), values());

    /**
     * Solo Constructor.
     *
     * @param val The Constant value.
     */
    EIDASStatusCode(@Nonnull String val) {
        value = val;
    }

    @Nullable
    public static EIDASStatusCode fromString(@Nonnull String val) {
        return MAPPER.fromKey(val);
    }

    public static EnumMapper<String, EIDASStatusCode> mapper() {
        return MAPPER;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    /**
     * Return the Constant Value.
     *
     * @return The constant value.
     */
    @Nonnull
    @Override
    public String toString() {
        return value;
    }
}
