/*
 * Copyright (c) 2017 by European Commission
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
package eu.eidas.node.auth.service;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.engine.ProtocolEngineI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * This class is a service used by {@link AUSERVICE} and to check mandatory and representative attributes.
 *
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
