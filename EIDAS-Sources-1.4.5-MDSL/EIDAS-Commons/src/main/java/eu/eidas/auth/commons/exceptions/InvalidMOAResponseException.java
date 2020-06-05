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
package eu.eidas.auth.commons.exceptions;

/**
 * Invalid MOA Exception class.
 */
public class InvalidMOAResponseException extends AbstractEIDASException {

    /**
     * Unique identifier.
     */
    private static final long serialVersionUID = 1328100834655923156L;

    /**
     * Exception Constructor with two Strings representing the errorCode and
     * errorMessage as parameters.
     *
     * @param errorCode    The error code value.
     * @param errorMessage The error message value.
     */
    public InvalidMOAResponseException(final String errorCode,
                                       final String errorMessage) {

        super(errorCode, errorMessage);
    }

    /**
     * {@inheritDoc}
     */
    public final String getMessage() {

        return "InvalidMOAResponseException Error (" + this.getErrorCode() + ") processing request : "
                + this.getErrorMessage();
    }
}
