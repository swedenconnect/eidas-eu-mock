/*
 * Copyright (c) 2020 by European Commission
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
package eu.eidas.node.auth.connector;

import eu.eidas.auth.commons.Country;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used by {@link AUCONNECTOR} to create the Country Selector and to check the selected Country.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com, luis.felix@multicert.com,
 *         hugo.magalhaes@multicert.com, paulo.ribeiro@multicert.com
 * @version $Revision: 1.7 $, $Date: 2011-02-18 07:04:16 $
 * @see ICONNECTORCountrySelectorService
 */
@Deprecated
public final class AUCONNECTORCountrySelector implements ICONNECTORCountrySelectorService {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AUCONNECTORCountrySelector.class.getName());

    /**
     * Connector authentication URL.
     */
    private String destination;

    /**
     * Connector's Util class.
     */
    private AUCONNECTORUtil connectorUtil;

    /**
     * {@inheritDoc}
     */
    public List<Country> createCountrySelector() {

        LOG.trace("Loading number of ServiceProxies");
        final int nService = Integer.parseInt(connectorUtil.loadConfig(EidasParameterKeys.EIDAS_NUMBER.toString()));
        LOG.debug("Number of ServiceProxies: " + nService);

        final List<Country> countries = new ArrayList<Country>(nService);

        for (int i = 1; i <= nService; i++) {

            final String countryId = connectorUtil.loadConfig(EIDASValues.EIDAS_SERVICE_PREFIX.index(i));
            final String countryName = connectorUtil.loadConfig(EIDASValues.EIDAS_SERVICE_PREFIX.name(i));
            if (StringUtils.isNotEmpty(countryId) && StringUtils.isNotEmpty(countryName)) {

                final Country countryInfo = new Country(countryId, countryName);

                LOG.trace("Index [" + i + "] has " + countryInfo.getCountryId() + "[ServiceProxy ID] and " + countryInfo
                        .getCountryName() + " [ServiceProxy NAME].");
                countries.add(countryInfo);
            }
        }
        return countries;
    }

    /**
     * Setter for destination.
     *
     * @param nDestination The destination to set.
     */
    public void setDestination(final String nDestination) {
        this.destination = nDestination;
    }

    /**
     * Getter for destination.
     *
     * @return The destination value.
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Setter for connectorUtil.
     *
     * @param connectorUtil The connectorUtil to set.
     */
    public void setConnectorUtil(final AUCONNECTORUtil connectorUtil) {
        this.connectorUtil = connectorUtil;
    }

    /**
     * Getter for connectorUtil.
     *
     * @return The connectorUtil value.
     */
    public AUCONNECTORUtil getConnectorUtil() {
        return connectorUtil;
    }

}
