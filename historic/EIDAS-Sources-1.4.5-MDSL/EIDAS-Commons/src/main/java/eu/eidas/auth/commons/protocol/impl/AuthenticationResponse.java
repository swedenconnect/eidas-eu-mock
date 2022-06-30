/**
 * This work is Open Source and licensed by the European Commission under the conditions of the European Public License
 * v1.1
 * <p/>
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 * <p/>
 * any use of this file implies acceptance of the conditions of this license. Unless required by applicable law or
 * agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package eu.eidas.auth.commons.protocol.impl;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import eu.eidas.auth.commons.protocol.IAuthenticationResponse;

/**
 * Concrete implementation of the {@link IAuthenticationResponse} interface.
 */
@Immutable
@ThreadSafe
public final class AuthenticationResponse extends AbstractAuthenticationResponse implements Serializable {

    /**
     * Builder pattern for the {@link AuthenticationResponse} class.
     * <p/>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p/>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p/>
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder
            extends AbstractAuthenticationResponse.AbstractBuilder<Builder, AuthenticationResponse> {

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            super(copy);
        }

        public Builder(@Nonnull IAuthenticationResponse copy) {
            super(copy);
        }

        @Override
        protected void validate() throws IllegalArgumentException {
        }

        @Nonnull
        @Override
        protected AuthenticationResponse newInstance() {
            return new AuthenticationResponse(this);
        }
    }

    private static final long serialVersionUID = 2095012420974837449L;

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull IAuthenticationResponse copy) {
        return new Builder(copy);
    }

    private AuthenticationResponse(@Nonnull Builder builder) {
        super(builder);
    }

    /**
     * Defensive serialization ensuring that the validation rules defined in the Builder are always used.
     * <p/>
     * Used upon de-serialization, not serialization.
     * <p/>
     * The state of this class is transformed back into the class it represents.
     */
    private Object readResolve() throws ObjectStreamException {
        return new Builder(this).build();
    }
}
