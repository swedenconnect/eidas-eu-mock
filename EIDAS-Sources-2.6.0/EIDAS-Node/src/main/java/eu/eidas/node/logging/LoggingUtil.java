/*
 * Copyright (c) 2021 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package eu.eidas.node.logging;

import eu.eidas.auth.commons.EidasDigestUtil;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.node.utils.PropertiesUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Base64;

/**
 * Central class used to associate logging marker to the functionality.
 */
public final class LoggingUtil {

    private LoggingUtil(){}

    public static void logServletCall(HttpServletRequest request, final String className, final Logger logger){
        String sessionId = LoggingConstants.NOT_APPLICABLE;
        HttpSession session = request.getSession(false);
        if (null != session) {
            sessionId = session.getId();
            MDC.put(LoggingMarkerMDC.MDC_SESSIONID, sessionId);
        }
        if (!StringUtils.isEmpty(request.getRemoteHost())) {
            MDC.put(LoggingMarkerMDC.MDC_REMOTE_HOST, request.getRemoteHost());
        }
        logger.info(LoggingMarkerMDC.WEB_EVENT, "**** CALL to servlet " + className
                + " FROM " + request.getRemoteAddr()
                + " HTTP " + request.getMethod()
                + " SESSIONID " + sessionId + "****");
    }

    /**
     * Creates a generated hash of the Message
     * @param bytes : The bytes used for generating the hash
     * @return the MsgHash Base 64 encoded
     */
    public static final String createMsgHash(byte[] bytes) {
        byte[] msgHashBytes = EidasDigestUtil.hashPersonalToken(bytes);
        return new String(Base64.getEncoder().encode(msgHashBytes));
    }

    /**
     * Creates the BltHash from the token received in Base64.
     * @param tokenBase64 Base 64 encoded {@link String} used for creating the BltHash
     * @return the BltHash Base 64 encoded
     */
    public static final String createBltHash(String tokenBase64) {
        final byte[] tokenBytes = EidasStringUtil.decodeBytesFromBase64(tokenBase64);
        if (ArrayUtils.isEmpty(tokenBytes)) {
            throw new InvalidParameterEIDASException(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
        }
        String digestAlgorithm = PropertiesUtil.getProperty(EidasParameterKeys.LOGGING_HASH_DIGEST_ALGORITHM.toString());
        String digestProvider = PropertiesUtil.getProperty(EidasParameterKeys.LOGGING_HASH_DIGEST_PROVIDER.toString());
        byte[] bltHashBytes = EidasDigestUtil.hash(tokenBytes, digestAlgorithm, digestProvider);
        return new String(Base64.getEncoder().encode(bltHashBytes));
    }

}
