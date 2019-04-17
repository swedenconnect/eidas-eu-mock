/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
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

package eu.eidas.sp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.opensymphony.xwork2.ActionSupport;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.BinaryRequestMessage;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

import static eu.eidas.sp.Constants.SP_CONF;

/**
 * This Action resign the current saml request
 */
@SuppressWarnings("squid:S1948") //TODO get rid of Struts
public class ResignAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

    static final Logger logger = LoggerFactory.getLogger(ResignAction.class.getName());

    private static final long serialVersionUID = 8366221022770773467L;

    HttpServletRequest request;

    private String citizenCountryCode;

    private String samlRequestXML;

    private String samlRequestBinding;

    private String samlRequestLocation;

    private transient InputStream dataStream;

    public String resignAsValidRequest() {
        byte[] result;
        byte[] messageBytes = EidasStringUtil.getBytes(samlRequestXML);
        try {
            SpProtocolEngineI defaultSamlEngine = SpProtocolEngineFactory.getSpProtocolEngine(SP_CONF);
            IAuthenticationRequest authenticationRequest =
                    defaultSamlEngine.unmarshallRequestAndValidate(messageBytes, citizenCountryCode,null);
            EidasAuthenticationRequest.Builder reqBuilder =
                    new EidasAuthenticationRequest.Builder((IEidasAuthenticationRequest) authenticationRequest);
            reqBuilder.binding(samlRequestBinding);
            reqBuilder.destination(samlRequestLocation);
            EidasAuthenticationRequest authnRequest = reqBuilder.build();
            IRequestMessage requestMessage =
                    defaultSamlEngine.resignEIDASAuthnRequest(new BinaryRequestMessage(authnRequest, messageBytes),
                                                              true);
            result = requestMessage.getMessageBytes();
        } catch (EIDASSAMLEngineException ssee) {
            logger.info("Error during resigning with validation", ssee);
            result = messageBytes;
        }
        dataStream = new ByteArrayInputStream(result);
        return SUCCESS;
    }

    public String reSign() {

        byte[] reSigned = new byte[] {};

        try {
            reSigned = SpProtocolEngineFactory.getSpProtocolEngine(SP_CONF).reSignRequest(EidasStringUtil.getBytes(samlRequestXML));
        } catch (EIDASSAMLEngineException ssee) {
            logger.error("Error during resigning ", ssee);
        }

        dataStream = new ByteArrayInputStream(reSigned);
        return SUCCESS;

    }

    public InputStream getInputStream() {
        return dataStream;
    }

    public void setInputStream(InputStream inputStream) {
        dataStream = inputStream;
    }

    public String getSamlRequestXML() {
        return samlRequestXML;
    }

    public void setSamlRequestXML(String samlRequestXML) {
        this.samlRequestXML = samlRequestXML;
    }

    public String getSamlRequestBinding() {
        return samlRequestBinding;
    }

    public void setSamlRequestBinding(String binding) {
        this.samlRequestBinding = binding;
    }

    @Override
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    @SuppressWarnings("squid:S1186")
    public void setServletResponse(HttpServletResponse response) {
    }

    public String getSamlRequestLocation() {
        return samlRequestLocation;
    }

    public void setSamlRequestLocation(String samlRequestLocation) {
        this.samlRequestLocation = samlRequestLocation;
    }

    public String getCitizenCountryCode() {
        return citizenCountryCode;
    }

    public void setCitizenCountryCode(String citizenCountryCode) {
        this.citizenCountryCode = citizenCountryCode;
    }
}
