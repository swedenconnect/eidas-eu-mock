/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */

package eu.eidas.node.utils;

import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link EidasNodeValidationUtil}
 */
public class EidasNodeValidationUtilTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test method for
     * {@link EidasNodeValidationUtil#validateConnectorDestination(IAuthenticationRequest, AUCONNECTORUtil, String, EidasErrorKey)}
     * when the httpMethod is redirect one of the accepted HTTP methods
     * when the uri matches the accepted ones for redirect
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidateConnectorDestination() {
        final String httpMethod = BindingMethod.GET.getValue();
        String uriToBeValidated = "uri";
        String key = EIDASValues.EIDAS_CONNECTOR_REDIRECT_URI_DESTINATION_WHITELIST.toString();

        IAuthenticationRequest mockedIAuthenticationRequest = mock(IAuthenticationRequest.class);
        when(mockedIAuthenticationRequest.getDestination()).thenReturn(uriToBeValidated);

        Properties configProperties = mock(Properties.class);
        when(configProperties.getProperty(key)).thenReturn(uriToBeValidated);

        AUCONNECTORUtil mockedAuconnectorUtil = mock(AUCONNECTORUtil.class);
        when(mockedAuconnectorUtil.getConfigs()).thenReturn(configProperties);

        EidasErrorKey mockedEidasErrorKey = EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL;
        EidasNodeValidationUtil.validateConnectorDestination(mockedIAuthenticationRequest, mockedAuconnectorUtil, httpMethod, mockedEidasErrorKey);
    }

    /**
     * Test method for
     * {@link EidasNodeValidationUtil#validateConnectorDestination(IAuthenticationRequest, AUCONNECTORUtil, String, EidasErrorKey)}
     * when the httpMethod is redirect, one of the accepted HTTP methods
     * when the uri does not match the accepted ones for redirect
     * <p>
     * Must fail.
     */
    @Test
    public void testValidateConnectorDestinationWhenUriIsNotValid() {
        thrown.expect(EidasNodeException.class);
        thrown.expectMessage("Security Error (203002) processing request : invalid.service.destUrl");

        final String httpMethod = BindingMethod.GET.getValue();
        String uriToBeValidated = "uri";
        String anotherUri = "anotherUri";
        EIDASValues eidasConnectorRedirectUridest = EIDASValues.EIDAS_CONNECTOR_REDIRECT_URI_DESTINATION_WHITELIST;

        IAuthenticationRequest mockedIAuthenticationRequest = mock(IAuthenticationRequest.class);
        when(mockedIAuthenticationRequest.getDestination()).thenReturn(uriToBeValidated);

        Properties configProperties = mock(Properties.class);
        when(configProperties.getProperty(eidasConnectorRedirectUridest.toString())).thenReturn(anotherUri);

        AUCONNECTORUtil mockedAuconnectorUtil = mock(AUCONNECTORUtil.class);
        when(mockedAuconnectorUtil.getConfigs()).thenReturn(configProperties);

        EidasErrorKey mockedEidasErrorKey = EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL;
        EidasNodeValidationUtil.validateConnectorDestination(mockedIAuthenticationRequest, mockedAuconnectorUtil, httpMethod, mockedEidasErrorKey);
    }

    /**
     * Test method for
     * {@link EidasNodeValidationUtil#validateConnectorDestination(IAuthenticationRequest, AUCONNECTORUtil, String, EidasErrorKey)}
     * when the httpMethod is POST, one of the accepted HTTP methods
     * when the uri matches the accepted ones for POST
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidateConnectorDestinationHttpMethodPost() {
        final String httpMethod = BindingMethod.POST.getValue();
        String uriToBeValidated = "uri";
        String key = EIDASValues.EIDAS_CONNECTOR_POST_URI_DESTINATION_WHITELIST.toString();

        IAuthenticationRequest mockedIAuthenticationRequest = mock(IAuthenticationRequest.class);
        when(mockedIAuthenticationRequest.getDestination()).thenReturn(uriToBeValidated);

        Properties configProperties = mock(Properties.class);
        when(configProperties.getProperty(key)).thenReturn(uriToBeValidated);

        AUCONNECTORUtil mockedAuconnectorUtil = mock(AUCONNECTORUtil.class);
        when(mockedAuconnectorUtil.getConfigs()).thenReturn(configProperties);

        EidasErrorKey mockedEidasErrorKey = EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL;
        EidasNodeValidationUtil.validateConnectorDestination(mockedIAuthenticationRequest, mockedAuconnectorUtil, httpMethod, mockedEidasErrorKey);
    }

    /**
     * Test method for
     * {@link EidasNodeValidationUtil#validateConnectorDestination(IAuthenticationRequest, AUCONNECTORUtil, String, EidasErrorKey)}
     * when the httpMethod is POST, one of the accepted HTTP methods
     * when the uri does not match the accepted ones for POST
     * <p>
     * Must fail.
     */
    @Test
    public void testValidateConnectorDestinationWhenUriIsNotValidHttpMethodPost() {
        thrown.expect(EidasNodeException.class);
        thrown.expectMessage("Security Error (203002) processing request : invalid.service.destUrl");

        final String httpMethod = BindingMethod.POST.getValue();
        String uriToBeValidated = "uri";
        String anotherUri = "anotherUri";
        EIDASValues eidasConnectorPostUridest = EIDASValues.EIDAS_CONNECTOR_POST_URI_DESTINATION_WHITELIST;

        IAuthenticationRequest mockedIAuthenticationRequest = mock(IAuthenticationRequest.class);
        when(mockedIAuthenticationRequest.getDestination()).thenReturn(uriToBeValidated);

        Properties configProperties = mock(Properties.class);
        when(configProperties.getProperty(eidasConnectorPostUridest.toString())).thenReturn(anotherUri);

        AUCONNECTORUtil mockedAuconnectorUtil = mock(AUCONNECTORUtil.class);
        when(mockedAuconnectorUtil.getConfigs()).thenReturn(configProperties);

        EidasErrorKey mockedEidasErrorKey = EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL;
        EidasNodeValidationUtil.validateConnectorDestination(mockedIAuthenticationRequest, mockedAuconnectorUtil, httpMethod, mockedEidasErrorKey);
    }

    /**
     * Test method for
     * {@link EidasNodeValidationUtil#validateConnectorDestination(IAuthenticationRequest, AUCONNECTORUtil, String, EidasErrorKey)}
     * when the httpMethod is REDIRECT one of the accepted http methods
     * when the uri matches the accepted ones for POST.
     * when accepted uris list for REDIRECT is null.
     * <p>
     * Must succeed.
     */
    @Test
    public void testValidateConnectorDestinationWhenRedirectMethodAndUriIsValidForPostMethodsAndRedirectAllowedListNull() {
        final String httpMethod = BindingMethod.GET.getValue();
        String uriToBeValidated = "uri";
        EIDASValues eidasConnectorPostUridest = EIDASValues.EIDAS_CONNECTOR_POST_URI_DESTINATION_WHITELIST;

        IAuthenticationRequest mockedIAuthenticationRequest = mock(IAuthenticationRequest.class);
        when(mockedIAuthenticationRequest.getDestination()).thenReturn(uriToBeValidated);

        Properties configProperties = mock(Properties.class);
        when(configProperties.getProperty(eidasConnectorPostUridest.toString())).thenReturn(uriToBeValidated);

        AUCONNECTORUtil mockedAuconnectorUtil = mock(AUCONNECTORUtil.class);
        when(mockedAuconnectorUtil.getConfigs()).thenReturn(configProperties);

        EidasErrorKey mockedEidasErrorKey = EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL;
        EidasNodeValidationUtil.validateConnectorDestination(mockedIAuthenticationRequest, mockedAuconnectorUtil, httpMethod, mockedEidasErrorKey);
    }

    /**
     * Test method for
     * {@link EidasNodeValidationUtil#validateConnectorDestination(IAuthenticationRequest, AUCONNECTORUtil, String, EidasErrorKey)}
     * when the httpMethod is REDIRECT one of the accepted http methods
     * when the uri matches the accepted ones for POST.
     * when accepted uris list for REDIRECT is not null.
     * when the uri does not match the accepted ones for REDIRECT.
     * <p>
     * Must fail.
     */
    @Test
    public void testValidateConnectorDestinationWhenPostMethodAndUriIsValidForRedirectMethods() {
        thrown.expect(EidasNodeException.class);
        thrown.expectMessage("Security Error (203002) processing request : invalid.service.destUrl");

        final String httpMethod = BindingMethod.GET.getValue();
        String uriToBeValidated = "uri";
        String anotherUri = "anotherUri";

        EIDASValues eidasConnectorPostUridest = EIDASValues.EIDAS_CONNECTOR_POST_URI_DESTINATION_WHITELIST;
        EIDASValues eidasConnectorRedirectUridest = EIDASValues.EIDAS_CONNECTOR_REDIRECT_URI_DESTINATION_WHITELIST;

        IAuthenticationRequest mockedIAuthenticationRequest = mock(IAuthenticationRequest.class);
        when(mockedIAuthenticationRequest.getDestination()).thenReturn(uriToBeValidated);

        Properties configProperties = mock(Properties.class);
        when(configProperties.getProperty(eidasConnectorPostUridest.toString())).thenReturn(uriToBeValidated);
        when(configProperties.getProperty(eidasConnectorRedirectUridest.toString())).thenReturn(anotherUri);

        AUCONNECTORUtil mockedAuconnectorUtil = mock(AUCONNECTORUtil.class);
        when(mockedAuconnectorUtil.getConfigs()).thenReturn(configProperties);

        EidasErrorKey mockedEidasErrorKey = EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL;
        EidasNodeValidationUtil.validateConnectorDestination(mockedIAuthenticationRequest, mockedAuconnectorUtil, httpMethod, mockedEidasErrorKey);
    }


    /**
     * Test method for
     * {@link EidasNodeValidationUtil#validateConnectorDestination(IAuthenticationRequest, AUCONNECTORUtil, String, EidasErrorKey)}
     * when the httpMethod is not one of the accepted http methods
     * <p>
     * Must fail.
     */
    @Test
    public void testValidateConnectorDestinationWhenHttpMethodNotValid() {
        thrown.expect(EidasNodeException.class);
        thrown.expectMessage("Security Error (203002) processing request : invalid.service.destUrl");

        final String httpMethod = "PUT";
        String uriToBeValidated = "uri";

        EIDASValues eidasConnectorPostUridest = EIDASValues.EIDAS_CONNECTOR_POST_URI_DESTINATION_WHITELIST;

        IAuthenticationRequest mockedIAuthenticationRequest = mock(IAuthenticationRequest.class);
        when(mockedIAuthenticationRequest.getDestination()).thenReturn(uriToBeValidated);

        Properties configProperties = mock(Properties.class);
        when(configProperties.getProperty(eidasConnectorPostUridest.toString())).thenReturn(uriToBeValidated);

        AUCONNECTORUtil mockedAuconnectorUtil = mock(AUCONNECTORUtil.class);
        when(mockedAuconnectorUtil.getConfigs()).thenReturn(configProperties);

        EidasErrorKey mockedEidasErrorKey = EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL;
        EidasNodeValidationUtil.validateConnectorDestination(mockedIAuthenticationRequest, mockedAuconnectorUtil, httpMethod, mockedEidasErrorKey);
    }

}