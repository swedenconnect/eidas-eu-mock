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
package eu.eidas.node.auth.service;

import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.CitizenConsent;
import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.engine.ProtocolEngineI;

/**
 * This class is a service used by {@link AUSERVICE} to get, process citizen consent and to update attribute the
 * attribute list on session or citizen consent based.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com, luis.felix@multicert.com,
 *         hugo.magalhaes@multicert.com, paulo.ribeiro@multicert.com
 * @version $Revision: 1.7 $, $Date: 2010-11-18 23:17:50 $
 * @see ISERVICETranslatorService
 */
public final class AUSERVICECitizen implements ISERVICECitizenService {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AUSERVICECitizen.class.getName());

    private AUSERVICEUtil serviceUtil;

    /**
     * Service for SAML related operations.
     */
    private ISERVICESAMLService samlService;

    private ProtocolEngineI getProtocolEngine() {
        return samlService.getSamlEngine();
    }

    @Override
    @Nonnull
    public CitizenConsent getCitizenConsent(@Nonnull WebRequest webRequest, @Nonnull ImmutableAttributeMap attributes) {

        CitizenConsent consent = new CitizenConsent();

        LOG.debug("[getCitizenConsent] Constructing consent...");
        for (final AttributeDefinition definition : attributes.getDefinitions()) {
            String name = definition.getNameUri().toASCIIString();
            LOG.debug("[getCitizenConsent] checking " + name);
            if (webRequest.getEncodedLastParameterValue(name) != null) {
                if (definition.isRequired()) {
                    LOG.trace("[getCitizenConsent] adding " + name + " to mandatory attributes");
                    consent.setMandatoryAttribute(name);
                } else {
                    LOG.trace("[getCitizenConsent] adding " + name + " to optional attributes");
                    consent.setOptionalAttribute(name);
                }

            }
        }
        return consent;
    }

    @Override
    public void processCitizenConsent(CitizenConsent consent,
                                      @Nonnull StoredAuthenticationRequest storedRequest,
                                      String ipUserAddress) {

        for (final AttributeDefinition definition : storedRequest.getRequest()
                .getRequestedAttributes()
                .getDefinitions()) {
            String name = definition.getNameUri().toASCIIString();
            LOG.trace("Searching for " + name);
            if (definition.isRequired() && !consent.getMandatoryList().contains(name)) {
                LOG.trace("Attribute " + name + " not found");
                LOG.info("BUSINESS EXCEPTION : Required attribute is missing!");

                String errorMessage = EidasErrors.get(EidasErrorKey.CITIZEN_RESPONSE_MANDATORY.errorMessage());

                String errorCode = EidasErrors.get(EidasErrorKey.CITIZEN_RESPONSE_MANDATORY.errorCode());

                byte[] error = samlService.generateErrorAuthenticationResponse(storedRequest.getRequest(), EIDASStatusCode.REQUESTER_URI.toString(),
                                                                               EIDASSubStatusCode.REQUEST_DENIED_URI.toString(),
                                                                               errorCode, errorMessage, ipUserAddress, true);
                throw new ResponseCarryingServiceException(errorCode, errorMessage,
                                                           EidasStringUtil.encodeToBase64(error),
                                                           storedRequest.getRequest().getAssertionConsumerServiceURL(),
                                                           storedRequest.getRelayState());
            }
            LOG.debug(name + " found");
        }
    }

    @Override
    @Nonnull
    public IAuthenticationRequest updateConsentedAttributes(@Nonnull IAuthenticationRequest authnRequest,
                                                            @Nonnull ImmutableAttributeMap consentedAttributes) {
        return samlService.updateRequest(authnRequest, consentedAttributes);
    }

    @Override
    public void checkMandatoryAttributes(@Nonnull ImmutableAttributeMap attributes) throws EIDASServiceException {
        if (serviceUtil != null && !Boolean.parseBoolean(
                serviceUtil.getConfigs().getProperty(EIDASValues.DISABLE_CHECK_MANDATORY_ATTRIBUTES.toString())) &&
                !samlService.checkMandatoryAttributeSet(attributes)) {
            throw new EIDASServiceException(EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorCode()),
                                            EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorMessage()));
        }
    }

    @Override
    public void checkRepresentativeAttributes(@Nonnull ImmutableAttributeMap attributes) throws EIDASServiceException {
        if (serviceUtil != null && !Boolean.parseBoolean(
                serviceUtil.getConfigs().getProperty(EIDASValues.DISABLE_CHECK_REPRESENTATIVE_ATTRS.toString())) &&
                !samlService.checkRepresentativeAttributes(attributes)) {
            throw new EIDASServiceException(EidasErrors.get(EidasErrorKey.EIDAS_REPRESENTATIVE_ATTRIBUTES.errorCode()),
                    EidasErrors.get(EidasErrorKey.EIDAS_REPRESENTATIVE_ATTRIBUTES.errorMessage()));
        }
    }


    @Nonnull
    @Override
    public ImmutableAttributeMap filterConsentedAttributes(@Nonnull CitizenConsent citizenConsent,
                                                           @Nonnull ImmutableAttributeMap attributes) {

        ImmutableAttributeMap.Builder consentedAttributes = ImmutableAttributeMap.builder();

        boolean modified = false;

        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : attributes.getAttributeMap()
                .entrySet()) {

            AttributeDefinition<?> definition = entry.getKey();
            ImmutableSet<? extends AttributeValue<?>> values = entry.getValue();

            String name = definition.getNameUri().toASCIIString();
            if (definition.isRequired() || citizenConsent.getOptionalList().contains(name)) {
                consentedAttributes.put((AttributeDefinition) definition, (ImmutableSet) values);
            } else {
                LOG.trace("Removing " + name);
                modified = true;
            }
        }

        if (modified) {
            return consentedAttributes.build();
        }
        // no modification:
        return attributes;
    }

    public AUSERVICEUtil getServiceUtil() {
        return serviceUtil;
    }

    public void setServiceUtil(AUSERVICEUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    /**
     * Setter for samlService.
     *
     * @param theSamlService The samlService to set.
     * @see ISERVICESAMLService
     */
    public void setSamlService(ISERVICESAMLService theSamlService) {
        this.samlService = theSamlService;
    }

    /**
     * Getter for samlService.
     *
     * @return The samlService value.
     * @see ISERVICESAMLService
     */
    public ISERVICESAMLService getSamlService() {
        return samlService;
    }
}
