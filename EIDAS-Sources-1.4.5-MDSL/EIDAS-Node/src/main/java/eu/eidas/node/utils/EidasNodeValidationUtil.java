/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.node.utils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.stork.IStorkAuthenticationRequest;
import eu.eidas.node.auth.service.AUSERVICEUtil;

/**
 * @author vanegdi on 14/08/2015. Remark : moved from eidasUtil
 */
public class EidasNodeValidationUtil {

    private EidasNodeValidationUtil() {
        // Private default constructor for utility class.
    }

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasNodeValidationUtil.class.getName());

    /**
     *  Validates message destination property against URI accessible from configuration. Now it is using parameters form eidas.xml
     *  same, as for metadata, but later this function to be refactored into samlengine, because destination check is a saml2
     *  standard requirement. Also, TODO do not check web binding, check message binding instead.
     *
     * @param authnRequest
     * @param serviceUtil
     * @param httpMethod
     * @param reportedErr
     */
    public static void validateServiceDestination(IAuthenticationRequest authnRequest,
                                                  AUSERVICEUtil serviceUtil,
                                                  String httpMethod,
                                                  EidasErrorKey reportedErr) {
        if ("POST".equals(httpMethod) &&
                !serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_POST_URIDEST.toString()).equalsIgnoreCase(authnRequest.getDestination())) {
            LOG.info("Expected auth request destination {} but got {}", serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_POST_URIDEST.toString()), authnRequest.getDestination());
            throw new EidasNodeException(
                    EidasErrors.get(reportedErr.errorCode()),
                    EidasErrors.get(reportedErr.errorMessage()),
                    new InternalErrorEIDASException(
                            EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL.errorCode()),
                            EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL.errorMessage())
                    )
            );
        } else {
            if (!serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_REDIRECT_URIDEST.toString()).equalsIgnoreCase(authnRequest.getDestination())) {
                LOG.info("Expected auth request destinaion {} but got {}", serviceUtil.getProperty(EIDASValues.EIDAS_SERVICE_REDIRECT_URIDEST.toString()), authnRequest.getDestination());
                throw new InternalErrorEIDASException(
                        EidasErrors.get(reportedErr.errorCode()),
                        EidasErrors.get(reportedErr.errorMessage()),
                        new InternalErrorEIDASException(
                                EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL.errorCode()),
                                EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL.errorMessage())
                        )
                );
            }
        }
    }

    /**
     *  Validates assertion consumer URL against Connector metadata.
     *
     * @param authnRequest
     * @param metadataAssertationConsumerURL
     * @param reportedErr
     */
    public static void validateAssertionConsumerURL(IAuthenticationRequest authnRequest,
                                                    String metadataAssertationConsumerURL,
                                                    EidasErrorKey reportedErr) {
        if (!authnRequest.getAssertionConsumerServiceURL().equals(metadataAssertationConsumerURL)) {
            LOG.info("Expected auth request assertion consumer url {} but got {}", metadataAssertationConsumerURL, authnRequest.getAssertionConsumerServiceURL());
            throw new EidasNodeException(
                    EidasErrors.get(reportedErr.errorCode()),
                    EidasErrors.get(reportedErr.errorMessage()),
                    new InternalErrorEIDASException(
                            EidasErrors.get(reportedErr.errorCode()),
                            EidasErrors.get(reportedErr.errorMessage())
                    )
            );
        }
    }

    /**
     * validates the current binding with that configured in the SAMLRequest
     *
     * @param authRequest
     * @param method
     */
    public static void validateBinding(IAuthenticationRequest authRequest,
                                       IncomingRequest.Method method,
                                       EidasErrorKey reportedErr) {

        if (authRequest.getBinding() != null && !authRequest.getBinding().equalsIgnoreCase(method.getValue())
                || authRequest.getBinding() == null && authRequest instanceof IStorkAuthenticationRequest) {
            LOG.info("Expected auth request protocol binding {} but got {}", method, authRequest.getBinding());
            throw new EidasNodeException(EidasErrors.get(reportedErr.errorCode()),
                                                  EidasErrors.get(reportedErr.errorMessage()),
                                                  new InternalErrorEIDASException(EidasErrors.get(
                                                          EidasErrorKey.INVALID_PROTOCOL_BINDING.errorCode()),
                                                                                  EidasErrors.get(
                                                                                          EidasErrorKey.INVALID_PROTOCOL_BINDING
                                                                                                  .errorMessage())));
        }
    }

    /**
     * Check if the Level of assurance is valid
     *
     * @param authnRequest
     * @param stringMaxLoA - max LoA value of the responder
     * @return true when the LoA value in the request exists and is inferior (or equal) to that of the responder
     */
    public static boolean isRequestLoAValid(IAuthenticationRequest authnRequest, String stringMaxLoA) {
        if (authnRequest instanceof IEidasAuthenticationRequest) {
            IEidasAuthenticationRequest request = (IEidasAuthenticationRequest) authnRequest;
            boolean invalidLoa = StringUtils.isEmpty(stringMaxLoA) || LevelOfAssurance.getLevel(stringMaxLoA) == null ||
                    request == null || request.getLevelOfAssurance() == null
                    || LevelOfAssurance.getLevel(request.getLevelOfAssurance()) == null;
            if (!invalidLoa) {
                return isLoAValid(request.getLevelOfAssuranceComparison(), request.getLevelOfAssurance(), stringMaxLoA);
            }

            return !invalidLoa;
        }
        return true;
    }

    /**
     * Check if the Level of assurance is valid compared to a given max value
     *
     * @param compareType
     * @param requestLoA
     * @param stringMaxLoA - max LoA value of the responder
     * @return true when the LoA compare type and value exist and the value is inferior (or equal) to that of the
     * responder
     */
    public static boolean isLoAValid(final LevelOfAssuranceComparison compareType, final String requestLoA, String stringMaxLoA) {
        boolean invalidLoa = StringUtils.isEmpty(stringMaxLoA) || LevelOfAssurance.getLevel(stringMaxLoA) == null ||
                requestLoA == null || LevelOfAssurance.getLevel(requestLoA) == null;
        if (!invalidLoa) {
            if (null != compareType) {
                invalidLoa = LevelOfAssurance.getLevel(requestLoA).numericValue() > LevelOfAssurance.getLevel(stringMaxLoA)
                        .numericValue();
            } else {
                invalidLoa = true;
            }
        }

        return !invalidLoa;
    }
}
