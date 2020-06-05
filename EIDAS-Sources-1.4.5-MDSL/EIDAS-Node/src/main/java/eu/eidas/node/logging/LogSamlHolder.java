/*
 * Copyright (c) 2018 by European Commission
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

import java.io.Serializable;

/**
 * Holder for values necessary for Logging Saml messages.
 */
public class LogSamlHolder implements Serializable {

    private String redirectUrl;

    private final String samlToken;

    private String samlTokenFail;

    private String relayState;

    private String issuer;

    private String samlResponseOriginatingMsgId;

    public LogSamlHolder(String redirectUrl, String samlToken, String samlTokenFail, String relayState, String issuer, String samlResponseOriginatingMsgId) {
        this.redirectUrl = redirectUrl;
        this.samlToken = samlToken;
        this.samlTokenFail = samlTokenFail;
        this.relayState = relayState;
        this.issuer = issuer;
        this.samlResponseOriginatingMsgId = samlResponseOriginatingMsgId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getSamlTokenSuccess() {
        return samlToken;
    }

    public String getSamlTokenFail() {
        return samlTokenFail;
    }

    public String getRelayState() {
        return relayState;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getSamlResponseOriginatingMsgId() {
        return samlResponseOriginatingMsgId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogSamlHolder that = (LogSamlHolder) o;

        if (redirectUrl != null ? !redirectUrl.equals(that.redirectUrl) : that.redirectUrl != null) return false;
        if (samlToken != null ? !samlToken.equals(that.samlToken) : that.samlToken != null) return false;
        if (samlTokenFail != null ? !samlTokenFail.equals(that.samlTokenFail) : that.samlTokenFail != null) return false;
        if (relayState != null ? !relayState.equals(that.relayState) : that.relayState != null) return false;
        if (issuer != null ? !issuer.equals(that.issuer) : that.issuer != null) return false;

        return samlResponseOriginatingMsgId != null ? samlResponseOriginatingMsgId.equals(that.samlResponseOriginatingMsgId) : that.samlResponseOriginatingMsgId == null;
    }

    @Override
    public int hashCode() {
        int result = redirectUrl != null ? redirectUrl.hashCode() : 0;
        result = 31 * result + (samlToken != null ? samlToken.hashCode() : 0);
        result = 31 * result + (samlTokenFail != null ? samlTokenFail.hashCode() : 0);
        result = 31 * result + (relayState != null ? relayState.hashCode() : 0);
        result = 31 * result + (issuer != null ? issuer.hashCode() : 0);
        result = 31 * result + (samlResponseOriginatingMsgId != null ? samlResponseOriginatingMsgId.hashCode() : 0);
        return result;
    }

}
