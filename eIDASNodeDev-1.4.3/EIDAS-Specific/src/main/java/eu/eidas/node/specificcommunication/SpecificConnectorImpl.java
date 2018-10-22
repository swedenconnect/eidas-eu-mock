package eu.eidas.node.specificcommunication;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.tx.BinaryAuthenticationExchange;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.auth.specific.IAUConnector;
import eu.eidas.node.SpecificConnectorBean;
import eu.eidas.node.SpecificParameterNames;
import eu.eidas.node.SpecificViewNames;
import eu.eidas.node.specificcommunication.exception.SpecificException;
import eu.eidas.node.specificcommunication.protocol.IRequestCallbackHandler;

import static org.bouncycastle.util.encoders.Base64.decode;

/**
 * SpecificConnectorImpl: provides a sample implementation of the specific interface {@link ISpecificConnector}
 *
 * @since 1.1
 */
public class SpecificConnectorImpl implements ISpecificConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpecificConnectorImpl.class);

    private boolean signResponseAssertion;

    private SpecificConnectorBean specificConnectorBean;

    public boolean isSignResponseAssertion() {
        return signResponseAssertion;
    }

    public void setSignResponseAssertion(boolean signResponseAssertion) {
        this.signResponseAssertion = signResponseAssertion;
    }

    public SpecificConnectorBean getSpecificConnectorBean() {
        return specificConnectorBean;
    }

    public void setSpecificConnectorBean(SpecificConnectorBean specificConnectorBean) {
        this.specificConnectorBean = specificConnectorBean;
    }

    @Override
    public ILightRequest processRequest(@Nonnull HttpServletRequest httpServletRequest,
                                        @Nonnull HttpServletResponse httpServletResponse) throws SpecificException {

        // Obtains the parameters from httpRequest
        WebRequest webRequest = new IncomingRequest(httpServletRequest);

        String samlRequestFromSp = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_REQUEST);

        if (samlRequestFromSp == null) { throw new SpecificException("missing Specific Request"); }

        NormalParameterValidator.paramName(EidasParameterKeys.SAML_REQUEST)
                .paramValue(samlRequestFromSp)
                .eidasError(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML)
                .validate();

        IAUConnector specificNode = specificConnectorBean.getSpecificConnectorNode();

        return specificNode.processAuthenticationRequest(webRequest, decode(samlRequestFromSp));
    }

    @Override
    public void sendResponse(@Nonnull ILightResponse lightResponse,
                             @Nonnull HttpServletRequest httpServletRequest,
                             @Nonnull HttpServletResponse httpServletResponse) throws SpecificException {

        String inResponseTo = lightResponse.getInResponseToId();
        IAUConnector specificNode = specificConnectorBean.getSpecificConnectorNode();
        try {
            BinaryAuthenticationExchange binaryAuthenticationExchange
                     = specificNode.generateAuthenticationResponse(lightResponse, signResponseAssertion);

            StoredAuthenticationRequest storedAuthenticationRequest =
                    binaryAuthenticationExchange.getStoredRequest();

            String relayState = storedAuthenticationRequest.getRelayState();

            String spUrl = storedAuthenticationRequest.getRequest().getAssertionConsumerServiceURL();

            byte[] encodedSaml = Base64.encode(binaryAuthenticationExchange.getConnectorResponseMessage().getMessageBytes());

            String samlResponseToSp = EidasStringUtil.toString(encodedSaml);

            httpServletRequest.setAttribute(EidasParameterKeys.SAML_RESPONSE.toString(), samlResponseToSp);
            httpServletRequest.setAttribute(EidasParameterKeys.SP_URL.toString(), spUrl);
            httpServletRequest.setAttribute(SpecificParameterNames.RELAY_STATE.toString(), relayState);

            RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher(SpecificViewNames.COLLEAGUE_RESPONSE_REDIRECT
                                                                                           .toString());
            dispatcher.forward(httpServletRequest, httpServletResponse);

        } catch (ServletException | IOException e) {
            LOGGER.error("Error converting the LightResponse to the specific protocol");
            throw new SpecificException(e);

        } finally {
            //clean up
            specificNode.getSpecificSpRequestCorrelationMap().remove(inResponseTo);
            specificNode.getConnectorRequestCorrelationMap().remove(inResponseTo);
        }

    }

    @Override
    public void setRequestCallbackHandler(@Nonnull IRequestCallbackHandler requestCallbackHandler)
            throws SpecificException {
        throw new UnsupportedOperationException("Not implemented!");
    }
}
