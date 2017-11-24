package eu.eidas.idp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableSet;

import eu.eidas.auth.commons.*;
import eu.eidas.auth.commons.attribute.*;
import eu.eidas.auth.engine.core.eidas.spec.RepresentativeLegalPersonSpec;
import eu.eidas.auth.engine.core.eidas.spec.RepresentativeNaturalPersonSpec;
import org.apache.log4j.Logger;

import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.idp.metadata.IDPCachingMetadataFetcher;

public class ProcessLogin {

    private static final Logger logger = Logger.getLogger(ProcessLogin.class.getName());

    private String samlToken;

    private String username;

    private String callback;

    private String eidasLoa;

    private boolean ipAddress;

    private Properties idpProperties;

    private static final IDPCachingMetadataFetcher idpMetadataFetcher = new IDPCachingMetadataFetcher();

    public ProcessLogin() throws IOException {
        idpProperties = IDPUtil.loadConfigs(Constants.IDP_PROPERTIES);
    }

    private Properties loadConfigs(String path) {
        try {
            return IDPUtil.loadConfigs(path);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new ApplicationSpecificIDPException("Could not load configuration file '"+path+"'", e);
        }
    }

    public static List<String> getValuesOfAttribute(String attrName, String value) {
        logger.trace("[processAuthentication] Setting: " + attrName + "=>" + value);
        ArrayList<String> tmp = new ArrayList<String>();
        tmp.add(value);
        if (AttributeValueTransliterator.needsTransliteration(value)) {
            String trValue = AttributeValueTransliterator.transliterate(value);
            tmp.add(trValue);
            logger.trace("[processAuthentication] Setting transliterated: " + attrName + "=>" + trValue);
        }
        return tmp;
    }

    protected ImmutableAttributeMap.Builder addAttributeValues(AttributeDefinition attr, Properties users, ImmutableAttributeMap.Builder mapBuilder) {
        String attrName = attr.getNameUri().toASCIIString();
        //lookup in properties file
        String key = username + "." + attrName.replaceFirst("[Hh][Tt][Tt][Pp]://", "");
        String value = users.getProperty(key);
        ArrayList<String> values = new ArrayList<String>();
        if (value != null && !value.isEmpty()) {
            values.addAll(getValuesOfAttribute(attrName, value));
        } else {
            String multivalues = users.getProperty(key + ".multivalue");
            if (null != multivalues && "true".equalsIgnoreCase(multivalues)) {
                for (int i = 1; null != users.getProperty(key + "." + i); i++) {
                    values.addAll(getValuesOfAttribute(attrName, users.getProperty(key + "." + i)));
                }
            }
        }
        if (!values.isEmpty()) {
            AttributeValueMarshaller<?> attributeValueMarshaller = attr.getAttributeValueMarshaller();
            ImmutableSet.Builder<AttributeValue<?>> builder = ImmutableSet.builder();
            for (final String val : values) {
                AttributeValue<?> attributeValue = null;
                try {
                    if (AttributeValueTransliterator.needsTransliteration(val)) {
                        attributeValue = attributeValueMarshaller.unmarshal(val, true);
                    } else {
                        attributeValue = attributeValueMarshaller.unmarshal(val, false);
                    }
                } catch (AttributeValueMarshallingException e) {
                    throw new IllegalStateException(e);
                }
                builder.add(attributeValue);
            }
            mapBuilder.put((AttributeDefinition) attr, (ImmutableSet) builder.build());
        }
        return mapBuilder;
    }

    public boolean processAuthentication(HttpServletRequest request, HttpServletResponse response) {

        //TODO vargata : check if parameters or error to be loaded here

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String samlToken = request.getParameter("samlToken");
        ipAddress = "on".equalsIgnoreCase(request.getParameter("ipAddress"));
        eidasLoa = request.getParameter("eidasloa");

        IAuthenticationRequest authnRequest = validateRequest(samlToken);
        this.callback = authnRequest.getAssertionConsumerServiceURL();

        if (username == null || password == null) {
            sendErrorRedirect(authnRequest, request, EIDASSubStatusCode.AUTHN_FAILED_URI,
                              EidasErrorKey.AUTHENTICATION_FAILED_ERROR.toString());
            return false;
        }

        Properties users = null;
        String pass = null;
        try {
            users = loadConfigs("user.properties");
            pass = users.getProperty(username);
        } catch (SecurityEIDASException e) {
            logger.error(e);
            sendErrorRedirect(authnRequest, request, EIDASSubStatusCode.AUTHN_FAILED_URI,
                    EidasErrorKey.AUTHENTICATION_FAILED_ERROR.toString());
        }

        if (pass == null || (!pass.equals(password))) {
            sendErrorRedirect(authnRequest, request, EIDASSubStatusCode.AUTHN_FAILED_URI,
                              EidasErrorKey.AUTHENTICATION_FAILED_ERROR.toString());
            return false;
        }

        this.username = username;

        ImmutableAttributeMap recvAttrMap = authnRequest.getRequestedAttributes();
        ImmutableAttributeMap sendAttrMap;
        ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();

        for (AttributeDefinition<?> attr : recvAttrMap.getDefinitions()) {
            addAttributeValues(attr, users, mapBuilder);
        }
        // in case of representative attributes, explicit check on user file as if they have been asked in the request
        for (AttributeDefinition<?> attr : RepresentativeLegalPersonSpec.REGISTRY.getAttributes()) {
            addAttributeValues(attr, users, mapBuilder);
        }
        for (AttributeDefinition<?> attr : RepresentativeNaturalPersonSpec.REGISTRY.getAttributes()) {
            addAttributeValues(attr, users, mapBuilder);
        }

        sendAttrMap = mapBuilder.build();
        sendRedirect(authnRequest, sendAttrMap, request);
        return true;
    }

    private IAuthenticationRequest validateRequest(String samlToken) {
        IAuthenticationRequest authnRequest;
        try {
            ProtocolEngineI engine = getSamlEngineInstance();
            authnRequest =
                    engine.unmarshallRequestAndValidate(EidasStringUtil.decodeBytesFromBase64(samlToken), getCountry());
        } catch (Exception e) {
            logger.error(e);
            throw new InvalidParameterEIDASException(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
        }
        return authnRequest;
    }

    private String getCountry() {
        return idpProperties == null ? null : idpProperties.getProperty(Constants.IDP_COUNTRY);
    }

    private void sendRedirect(IAuthenticationRequest authnRequest,
                              ImmutableAttributeMap attrMap,
                              HttpServletRequest request) {
        try {
            String remoteAddress = request.getRemoteAddr();
            if (request.getHeader(EidasParameterKeys.HTTP_X_FORWARDED_FOR.toString()) != null) {
                remoteAddress = request.getHeader(EidasParameterKeys.HTTP_X_FORWARDED_FOR.toString());
            } else {
                if (request.getHeader(EidasParameterKeys.X_FORWARDED_FOR.toString()) != null) {
                    remoteAddress = request.getHeader(EidasParameterKeys.X_FORWARDED_FOR.toString());
                }
            }

            ProtocolEngineI engine = getSamlEngineInstance();
            AuthenticationResponse.Builder responseAuthReq = new AuthenticationResponse.Builder();

            responseAuthReq.attributes(attrMap);
            responseAuthReq.inResponseTo(authnRequest.getId());
            IAuthenticationRequest processedAuthnRequest = processRequestCallback(authnRequest, engine);
            String metadataUrl = idpProperties == null ? null : idpProperties.getProperty(Constants.IDP_METADATA_URL);
            if (metadataUrl != null && !metadataUrl.isEmpty()) {
                responseAuthReq.issuer(metadataUrl);
            }
            responseAuthReq.levelOfAssurance(eidasLoa);

            responseAuthReq.id(SAMLEngineUtils.generateNCName());
            responseAuthReq.statusCode(EIDASStatusCode.SUCCESS_URI.toString());

            AuthenticationResponse response = responseAuthReq.build();

            IResponseMessage responseMessage;
            boolean signAssertion = engine.getSigner().isResponseSignAssertions();
            try {
                responseMessage = engine.generateResponseMessage(processedAuthnRequest, response, signAssertion, ipAddress ? remoteAddress : null);
                samlToken = EidasStringUtil.encodeToBase64(responseMessage.getMessageBytes());

            } catch (EIDASSAMLEngineException se) {
                if (se.getErrorDetail().startsWith("Unique Identifier not found:") || se.getErrorDetail()
                        .startsWith("No attribute values in response.")) {
                    // special IdP case when subject cannot be constructed due to missing unique identifier
                    sendErrorRedirect(processedAuthnRequest, request, EIDASSubStatusCode.INVALID_ATTR_NAME_VALUE_URI,
                                      EidasErrorKey.ATT_VERIFICATION_MANDATORY.toString());
                } else {
                    throw se;
                }
            }
        } catch (Exception ex) {
            throw new InternalErrorEIDASException("0", "Error generating SAMLToken", ex);
        }
    }

    private ProtocolEngineI getSamlEngineInstance() throws EIDASSAMLEngineException {
        return IDPUtil.getProtocolEngine();
    }

    private void sendErrorRedirect(IAuthenticationRequest authnRequest,
                                   HttpServletRequest request,
                                   EIDASSubStatusCode subStatusCode,
                                   String message) {
        byte[] failureBytes;
        try {
            AuthenticationResponse.Builder samlTokenFail = new AuthenticationResponse.Builder();
            samlTokenFail.statusCode(EIDASStatusCode.RESPONDER_URI.toString());
            samlTokenFail.subStatusCode(subStatusCode.toString());
            samlTokenFail.statusMessage(message);
            ProtocolEngineI engine = getSamlEngineInstance();
            samlTokenFail.id(SAMLEngineUtils.generateNCName());
            samlTokenFail.inResponseTo(authnRequest.getId());
            String metadataUrl = idpProperties == null ? null : idpProperties.getProperty(Constants.IDP_METADATA_URL);
            if (metadataUrl != null && !metadataUrl.isEmpty()) {
                samlTokenFail.issuer(metadataUrl);
            }
            IAuthenticationRequest processedAuthnRequest = processRequestCallback(authnRequest, engine);
            samlTokenFail.levelOfAssurance(eidasLoa);
            AuthenticationResponse token = samlTokenFail.build();
            IResponseMessage responseMessage =
                    engine.generateResponseErrorMessage(processedAuthnRequest, token, request.getRemoteAddr());
            failureBytes = responseMessage.getMessageBytes();
        } catch (Exception ex) {
            throw new InternalErrorEIDASException("0", "Error generating SAMLToken", ex);
        }
        this.samlToken = EidasStringUtil.encodeToBase64(failureBytes);
    }

    private IAuthenticationRequest processRequestCallback(IAuthenticationRequest authnRequest, ProtocolEngineI engine)
            throws EIDASSAMLEngineException {
        IAuthenticationRequest newAuthnRequest = authnRequest;
        if (callback == null) {
            EidasAuthenticationRequest.Builder builder =
                    EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) authnRequest);
            callback = MetadataUtil.getAssertionConsumerUrlFromMetadata(idpMetadataFetcher,
                                                                        (MetadataSignerI) engine.getSigner(),
                                                                        authnRequest);

            builder.assertionConsumerServiceURL(callback);
            newAuthnRequest = builder.build();
        }
        return newAuthnRequest;
    }

    /**
     * @param samlToken the samlToken to set
     */
    public void setSamlToken(String samlToken) {
        this.samlToken = samlToken;
    }

    /**
     * @return the samlToken
     */
    public String getSamlToken() {
        return samlToken;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param callback the callback to set
     */
    public void setCallback(String callback) {
        this.callback = callback;
    }

    /**
     * @return the callback
     */
    public String getCallback() {
        return callback;
    }

}
