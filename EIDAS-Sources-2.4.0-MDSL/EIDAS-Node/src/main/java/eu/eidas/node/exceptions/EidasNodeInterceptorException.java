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

package eu.eidas.node.exceptions;

/**
 * EidasNodeInterceptorException bean.
 * 
 * @see RuntimeException
 */
public final class EidasNodeInterceptorException extends RuntimeException {
  
  /**
   * Unique identifier.
   */
  private static final long serialVersionUID = 8126567583461952110L;
  
  /**
   * Error code.
   */
  private final String errorCode;
  
  /**
   * Error message.
   */
  private final String errorMessage;
  
  /**
   * Class constructor for EidasNodeInterceptorException.
   * 
   * @param code The code of the error.
   * @param message The description of the error.
   */
  public EidasNodeInterceptorException(final String code, final String message) {
    super(message);
    this.errorCode = code;
    this.errorMessage = message;
  }
  
  /**
   * {@inheritDoc}
   */
  public String getMessage() {
    return errorMessage;
  }
  
  /**
   * Getter for errorCode.
   * 
   * @return The errorCode value.
   */
  public String getErrorCode() {
    return this.errorCode;
  }

  
  /**
   * Getter for errorMessage.
   * 
   * @return The errorMessage value.
   */
  public String getErrorMessage() {
    return this.errorMessage;
  }
}
