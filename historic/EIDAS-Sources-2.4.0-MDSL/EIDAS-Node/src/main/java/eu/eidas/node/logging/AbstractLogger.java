/*
 *
 *  Copyright (c) 2019 by European Commission
 *
 *  Licensed under the EUPL, Version 1.2 or - as soon they will be
 *  approved by the European Commission - subsequent versions of the
 *  EUPL (the "Licence");
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at:
 *  https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.
 *  See the Licence for the specific language governing permissions and
 *  limitations under the Licence
 *
 */

package eu.eidas.node.logging;

import eu.eidas.auth.commons.EidasDigestUtil;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.security.utils.Base64;

import java.util.Optional;

/**
 * Abstract class providing common functionalities for classes implementing the {@link IMessageLogger} interface
 * @since 2.4
 */
public abstract class AbstractLogger implements IMessageLogger {

     /** Defines NOT_APPLICABLE constant*/
    public final static String NOT_APPLICABLE = "N/A";

    /** Message end tag delimiter */
    public final static String END_TAG = ",";

    private static final int TAG_LENGTH = 14;
    private static final int BASE64_LENGTH = 88;

    /**
     * Cretaes a (key,value) pair entry in the tagsToLog map.
     * @param tag : The key used by the created entry
     * @param value : The value used to build the {@link Optional} that will be set in the map.
     * @param messageStringBuilder
     */
    protected final void setTagToLog(MessageLoggerTag tag, String value, StringBuilder messageStringBuilder) {
        messageStringBuilder
                .append("\n")
                .append(padTagWithSpaces(tag))
                .append(value)
                .append(END_TAG);
    }

    private String padTagWithSpaces (MessageLoggerTag tag) {
        return StringUtils.rightPad(tag.getValue(), TAG_LENGTH);
    }

    /**
     * Creates in the tagsToLog map an entry holding a generated hash
     * @param bytes : The bytes used for genrating the hash
     * @return the MsgHash Base 64 encoded
     */
    protected final String createMsgHashToLog(byte[] bytes) {
        byte[] msgHashBytes = EidasDigestUtil.hashPersonalToken(bytes);
        return Base64.encode(msgHashBytes, BASE64_LENGTH);
    }

    /**
     * Creates the BltHash from the token received in Base64.
     * @param tokenBase64 Base 64 encoded {@link String} used for creating the BltHash
     * @return the BltHash Base 64 encoded
     */
    protected final String createBltHashToLog(String tokenBase64) {
        final byte[] tokenBytes = EidasStringUtil.decodeBytesFromBase64(tokenBase64);
        if (ArrayUtils.isEmpty(tokenBytes)) {
            throw new InvalidParameterEIDASException(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
        }
        byte[] bltHashBytes = EidasDigestUtil.hashPersonalToken(tokenBytes);
        return Base64.encode(bltHashBytes, BASE64_LENGTH);
    }

}
