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
 * Security eIDAS Node Exception class.
 * 
 * @see AbstractEIDASException
 */
public final class SecurityEIDASException extends AbstractEIDASException {
  
  /**
   * Unique identifier.
   */
  private static final long serialVersionUID = 5605743302478554967L;
  
  /**
   * Exception Constructor with two Strings representing the errorCode and
   * errorMessage as parameters.
   * 
   * @param errorCode The error code value.
   * @param errorMsg The error message value.
   */
  public SecurityEIDASException(final String errorCode, final String errorMsg) {
    super(errorCode, errorMsg);
  }
  
  /**
   * Exception Constructor with two Strings representing the errorCode and
   * errorMessage as parameters and the Throwable cause.
   * 
   * @param errorCode The error code value.
   * @param errorMessage The error message value.
   * @param cause The throwable object.
   */
  public SecurityEIDASException(final String errorCode,
    final String errorMessage, final Throwable cause) {
    
    super(errorCode, errorMessage, cause);
  }
  
  /**
   * Exception Constructor with one String representing the encoded samlToken.
   * 
   * @param samlTokenFail The error SAML Token.
   */
  public SecurityEIDASException(final String samlTokenFail) {
    super(samlTokenFail);
  }
  
}
