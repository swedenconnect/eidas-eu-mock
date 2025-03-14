/*
 * Copyright (c) 2024 by European Commission
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

package eu.eidas.node.service;

import eu.eidas.auth.cache.ConcurrentMapJcacheServiceDefaultImpl;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.auth.commons.tx.FlowIdCache;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.ProxyBeanNames;
import eu.eidas.node.auth.service.ISERVICEService;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.cache.Cache;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link ColleagueRequestServlet}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ColleagueRequestServletTest {

    private ColleagueRequestServlet colleagueRequestServlet;

    @Mock
    private ApplicationContext mockApplicationContext;

    @Mock
    private ISERVICEService mockServiceService;

    Cache<String, StoredAuthenticationRequest> mockRequestCorrelationCache;

    BinaryLightToken binaryLightToken;

    SpecificCommunicationService mockSpecificProxyserviceCommunicationService;

    IAuthenticationRequest mockStoredAuthRequest;

    RequestDispatcher mockDispatcher;

    private static final String spCountryCode = "CX";

    private static final String citizenCountryCode = "CY";

    private ApplicationContext oldContext = null;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setupContext() throws Exception {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);


        colleagueRequestServlet = new ColleagueRequestServlet();

        mockServiceControllerService();
        mockIAuthenticationRequest();
        mockFlowIdCache();
        mockSpecificProxyserviceCommunicationService();

        ServletContext mockServletContext = mockServletContext();

        mockDispatcher = mock(RequestDispatcher.class);
        when(mockServletContext.getRequestDispatcher(any())).thenReturn(mockDispatcher);
    }

    @After
    public void tearDown() {
        if (oldContext != null) {
            ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", oldContext);
            oldContext = null;
        }
    }

    /**
     * Test method for
     * {@link ColleagueRequestServlet#buildLightRequest(HttpServletRequest, HttpServletResponse, IAuthenticationRequest)}
     * <p>
     * to check if spCountryCode has the correct value.
     * <p>
     * Must succeed.
     */
    @Test
    public void testSetSpCountryCode() throws Exception {
        Mockito.when(mockStoredAuthRequest.getServiceProviderCountryCode()).thenReturn(spCountryCode);
        Mockito.when(mockStoredAuthRequest.getCitizenCountryCode()).thenReturn(citizenCountryCode);

        LevelOfAssurance.Builder levelOfAssuranceBuilder = LevelOfAssurance.builder();
        levelOfAssuranceBuilder.value(ILevelOfAssurance.EIDAS_LOA_LOW);
        LevelOfAssurance levelOfAssurance = levelOfAssuranceBuilder.build();
        ArrayList<ILevelOfAssurance> iLevelOfAssurances = new ArrayList<>();
        iLevelOfAssurances.add(levelOfAssurance);
        Mockito.when(mockStoredAuthRequest.getLevelsOfAssurance()).thenReturn(iLevelOfAssurances);

        HttpServletRequest mockRequest = createMockRequest();
        colleagueRequestServlet.doPost(mockRequest, new MockHttpServletResponse());

        ArgumentCaptor<ILightRequest> argument = ArgumentCaptor.forClass(ILightRequest.class);
        Mockito.verify(mockSpecificProxyserviceCommunicationService).putRequest(argument.capture());

        String actualSpCountryCode = argument.getValue().getSpCountryCode();
        String expectedSpCountryCodeCX = this.spCountryCode;
        assertEquals(expectedSpCountryCodeCX, actualSpCountryCode);

        String actualCitizenCountryCode = argument.getValue().getCitizenCountryCode();
        String expectedCitizenCountryCodeCY = this.citizenCountryCode;
        assertEquals(expectedCitizenCountryCodeCY, actualCitizenCountryCode);

        verify(mockDispatcher).forward(isA(ServletRequest.class), isA(ServletResponse.class));
    }

    /**
     * Test method for
     * {@link ColleagueRequestServlet#buildLightRequest(HttpServletRequest, HttpServletResponse, IAuthenticationRequest)}
     * When IAuthenticationRequest does not have a correct level of assurance
     * <p>
     * Must fail.
     */
    @Test
    public void testBuildLightRequestIllegalArguments() throws ServletException, IOException {
        expectedException.expect(ProxyServiceError.class);

        Mockito.when(mockStoredAuthRequest.getServiceProviderCountryCode()).thenReturn(spCountryCode);
        Mockito.when(mockStoredAuthRequest.getCitizenCountryCode()).thenReturn(citizenCountryCode);
        Mockito.when(mockStoredAuthRequest.getLevelsOfAssurance()).thenReturn(null);

        HttpServletRequest mockRequest = createMockRequest();
        colleagueRequestServlet.doPost(mockRequest, new MockHttpServletResponse());
    }

    private void mockIAuthenticationRequest() {
        mockStoredAuthRequest = Mockito.mock(IAuthenticationRequest.class);
        Mockito.when(mockStoredAuthRequest.getIssuer()).thenReturn("mockIssuer");
        Mockito.when(mockServiceService
                        .processAuthenticationRequest(any(), any(), any(), any()))
                .thenReturn(mockStoredAuthRequest);
    }

    private void mockServiceControllerService() {
        ServiceControllerService mockServiceControllerService = Mockito.mock(ServiceControllerService.class);
        Mockito.when(mockApplicationContext.getBean(ProxyBeanNames.EIDAS_SERVICE_CONTROLLER.toString()))
                .thenReturn(mockServiceControllerService);
        Mockito.when(mockServiceControllerService.getProxyService()).thenReturn(mockServiceService);

        ConcurrentMapJcacheServiceDefaultImpl concurrentMapJcacheServiceDefault = new ConcurrentMapJcacheServiceDefaultImpl();
        mockRequestCorrelationCache = concurrentMapJcacheServiceDefault.getConfiguredCache();


        Mockito.when(mockApplicationContext.getBean(ProxyBeanNames.SPECIFIC_PROXYSERVICE_DEPLOYED_JAR.toString()))
                .thenReturn(Boolean.TRUE);

        Mockito.when(mockServiceControllerService.getProxyServiceRequestCorrelationCache()).thenReturn(mockRequestCorrelationCache);
    }

    private void mockFlowIdCache() {
        FlowIdCache mockFlowIdCache = Mockito.mock(FlowIdCache.class);
        Mockito.when(mockApplicationContext.getBean(ProxyBeanNames.EIDAS_PROXYSERVICE_FLOWID_CACHE.toString()))
                .thenReturn(mockFlowIdCache);
    }

    private void mockSpecificProxyserviceCommunicationService() throws SpecificCommunicationException {
        mockSpecificProxyserviceCommunicationService = Mockito.mock(SpecificCommunicationService.class);

        binaryLightToken =
                BinaryLightTokenHelper.createBinaryLightToken("CA", "test", "SHA-256");

        Mockito.when(mockSpecificProxyserviceCommunicationService.putRequest(any())).thenReturn(binaryLightToken);

        Mockito.when(mockApplicationContext.getBean(ProxyBeanNames.SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE.toString()))
                .thenReturn(mockSpecificProxyserviceCommunicationService);
    }

    private ServletContext mockServletContext() throws Exception {
        ServletContext servletContext = Mockito.mock(ServletContext.class);
        ServletConfig mockServletConfig = Mockito.mock(ServletConfig.class);
        Mockito.when(mockServletConfig.getServletContext()).thenReturn(servletContext);
        colleagueRequestServlet.init(mockServletConfig);

        return servletContext;
    }

    private HttpServletRequest createMockRequest() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest(HttpMethod.POST.toString(), null);
        mockRequest.setParameter(EidasParameterKeys.SAML_REQUEST.toString(), samlRequestBase64);
        mockRequest.setParameter(EidasParameterKeys.RELAY_STATE.toString(), "");
        return mockRequest;
    }

    final String samlRequestBase64 = "PHNhbWwycDpBdXRoblJlcXVlc3QgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiIHhtbG5zOmVpZGFzPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L3NhbWwtZXh0ZW5zaW9ucyIgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIHhtbG5zOnNhbWwycD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOnByb3RvY29sIiBDb25zZW50PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6Y29uc2VudDp1bnNwZWNpZmllZCIgRGVzdGluYXRpb249Imh0dHA6Ly9jZWYtZWlkLWJ1aWxkLTE6ODA4MC9FaWRhc05vZGUvQ29sbGVhZ3VlUmVxdWVzdCIgRm9yY2VBdXRobj0idHJ1ZSIgSUQ9Il9sVks1eXowZi5sSWtRUnNDMmI1eURLMURNSGh6N3Y3VGhJSDJCMDZFWXdGZmlIMl9QbkRHMkpZNTlPUGZoSk0iIElzUGFzc2l2ZT0iZmFsc2UiIElzc3VlSW5zdGFudD0iMjAxOS0xMS0wN1QxMjoxODoyMy45MjRaIiBQcm92aWRlck5hbWU9IkRFTU8tU1AtQ0EiIFZlcnNpb249IjIuMCI+PHNhbWwyOklzc3VlciBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpuYW1laWQtZm9ybWF0OmVudGl0eSI+aHR0cDovL2NlZi1laWQtYnVpbGQtMTo4MDgwL0VpZGFzTm9kZS9Db25uZWN0b3JNZXRhZGF0YTwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmU+CjxkczpTaWduZWRJbmZvPgo8ZHM6Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPgo8ZHM6U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxkc2lnLW1vcmUjcnNhLXNoYTUxMiIvPgo8ZHM6UmVmZXJlbmNlIFVSST0iI19sVks1eXowZi5sSWtRUnNDMmI1eURLMURNSGh6N3Y3VGhJSDJCMDZFWXdGZmlIMl9QbkRHMkpZNTlPUGZoSk0iPgo8ZHM6VHJhbnNmb3Jtcz4KPGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIi8+CjxkczpUcmFuc2Zvcm0gQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KPC9kczpUcmFuc2Zvcm1zPgo8ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhNTEyIi8+CjxkczpEaWdlc3RWYWx1ZT5tOWtCWGVnZzR3bDI2T01TYWgzOGlPaUp2SGJLd0QybUhaWHZORlhSbE0wMlpkVjhjU2F1MXlQcTBSeGhiTklYaWd4cTBBUTYwcm93JiMxMzsKWi9jenEwamlFZz09PC9kczpEaWdlc3RWYWx1ZT4KPC9kczpSZWZlcmVuY2U+CjwvZHM6U2lnbmVkSW5mbz4KPGRzOlNpZ25hdHVyZVZhbHVlPgpOU2syNldPQ2NzUEhMVEJXOHFib0k1NGVXdUdLM0YzV25QUDFLL3VTaWhUbWFkYmNhMjJuaXZ6cVZFWndoc0hlb0s3Zk93ZjZSeVplJiMxMzsKQ25NcVhSTFhtd3hQamVmSkxGaVVZQWNCdHNSTEhFQi9DWEQxNEtYc0lsT09sdXpiaHdoVDZXYzk3SVFxNUxKT3ZnNUlnQ1VMV3hOTCYjMTM7CmNPeitkeWprOEk5RG5WY0V5VjhJSWpiUzlyb3RiRkZrUXNSUSthby9tY0ZYdHRaYVdpTHBzb0tNOWs0THVFa2JNMGEzRWdRampOVTQmIzEzOwpZN1VLQVFta2ptWVI4Q2M1VHpNa2RsN3ZMVWtLclpJSnkzMTFwYTB0SERmWWlIK2EyMWlFOFlBeStDdENNYTJEUTZESmwyb0xweDlpJiMxMzsKZkQzc1gzNU4rTm8zUnFYdStpdGFHc2RNOFNHaUxPek5aQTJINU5VbnZMRnJTcVYreXhCTjZvK0kxTjFLZjFrSEJFUW5US1MwZ3FIUyYjMTM7Cm00dzhxRWQvMnlGSys2ZDJZc2VXVDRwR1FEUTN2VTVrSUNJTTNvMHVYcy9QbmVjQTZGaFhEN2JrM1FoOWQ1U1VlbitwSW10K204V0kmIzEzOwpaK2FTYlcySldCUERJdUluaVVuY2Q0V1c4R2U5cHhkLzdja0cwbGxTTEFHTE5leW9FR3R4TDU3cnByMlZJaDBtajdUTW4zYktzRTg2JiMxMzsKL2hRdmd5SlRPNG96S2l1OElmZDkyZXp1cUhwMTY3U2Q0K1MzQmFXWCs1QzkwSUFlRERjNkp6Y3pzNDJmeE5RaTQwT01CQXFJQVZRNyYjMTM7CjdLQTVDNzlQTG93R0Y2R2RDcVBsWGZQMWQyOVp1ekZWZFpsMXkycUZrM3o1cm4ydGV3K29WMzNJdm94bzltYm5KblNaTmlZS2M3Zz0KPC9kczpTaWduYXR1cmVWYWx1ZT4KPGRzOktleUluZm8+PGRzOlg1MDlEYXRhPjxkczpYNTA5Q2VydGlmaWNhdGU+TUlJRlRUQ0NBelVDQkZUSS9JZ3dEUVlKS29aSWh2Y05BUUVOQlFBd2F6RUxNQWtHQTFVRUJoTUNRMEV4Q3pBSkJnTlZCQWdNQWtWVgpNUXN3Q1FZRFZRUUhEQUpGVlRFT01Bd0dBMVVFQ2d3RlUxQkZVRk14RGpBTUJnTlZCQXNNQlZOVVQxSkxNU0l3SUFZRFZRUUREQmx6CmNHVndjeTFqWVMxa1pXMXZMV05sY25ScFptbGpZWFJsTUI0WERURTFNREV5T0RFMU1UTXhNbG9YRFRFMk1ERXlPREUxTVRNeE1sb3cKYXpFTE1Ba0dBMVVFQmhNQ1EwRXhDekFKQmdOVkJBZ01Ba1ZWTVFzd0NRWURWUVFIREFKRlZURU9NQXdHQTFVRUNnd0ZVMUJGVUZNeApEakFNQmdOVkJBc01CVk5VVDFKTE1TSXdJQVlEVlFRRERCbHpjR1Z3Y3kxallTMWtaVzF2TFdObGNuUnBabWxqWVhSbE1JSUNJakFOCkJna3Foa2lHOXcwQkFRRUZBQU9DQWc4QU1JSUNDZ0tDQWdFQWhGWEMvR0RLakhBMGFYd1UreEl2KzJHeWZTalEyTDFaUzFhc0M1QVkKT0xhMVBDMlRXaWxqYk9qMXZSWEdsTlhHNHVlOTFuUWtwTUJOZzNuTW1adGRoUDJ2ZkV0VCs5VjJJTlFSKzFhUThwbFBHV05JT1gxTwo1TlovRjJNL2RhQVoySzNZK1dTS0xpbjdGQ2RCTDZCdzBJR0ZtUVVlYmxJcnVtTW9lUnBRaFhCZnJESHlGVy9vek5lQjZNOXgyYlRECm1rL2hqSk84bC9hdXhzUGpZelUyK3JFTlMra0VLcjEwSFJ5OE1UcXRmY3FWZnQ1YmxDQVVPZURjdHZzZi81czM3SksyTUJHamdYNlgKbWNoTFF5bE9wVVpyQm83Y05UZmM3N1Jqb2JkZ041cldVeVo4V2VFWUh0ZnlVbytuRjdESFAyZXVpdWh1ZUZXN0dpbmQrTXJjQXVkSQpKSTVyMjd4NGpobmV5dWwxYTVPV2JHVVA4NGxPb0lwekNrdkUwdzdIYnBJdER4Ym51WDA5S0JtbThsS1VpbmdCNUNhcUc1RCtBUENXClQzZW1vMUhJa0xYM0pxOE9MWE51N2M5alBCMXhHazRiNlpDMmZoQ0x3dnYzN2JBYWhXWjgxV2txeldMYVpweUFLWit2UkNhN1dNdHQKSGZoVkVKbUVVL3dDZ2Z2djJsMW12Q3o2MUg2QVFXekVkOFpMd2labmxqZ0RFdHhxOHNSTlpIUVZQdkJuQzlUS3l6eTZEMS8rc1o3Ugo4cDRBeEZIWDVVeEcrcUV0RWl6NXFYRnN3WFU4Z0JzS3BETzljRzZuN0M4Qmc4dWhzeDFUZldKZW9lMW10SnBLVEF2SmJxRS9PMmFrClFNSHUwQzJEK2ZvdTNqeldqb0Rmck1KaSs5Q1Zxc3lJQ1JjQ0F3RUFBVEFOQmdrcWhraUc5dzBCQVEwRkFBT0NBZ0VBYnl3dW0rcFkKdHJYblJtdEkyenJ1bjdKdW03VXB4U2xWalcxTVV2dDB2WkhOZ3VuQWtEaXZGMWVPWHNYUVIxYm9YZGpzdXZSdzNFUk1oZHBpYm43SQptdkZBbU1yWU1lejVQTkV1c3FRK3B6SXoyT1BhN0pJamVteDZlbVpUb1VLaEo1aXFqRGxMK0o1TzdvdjYrRnJqamxSamZQc01XSmRDCnp1M05rMFRCWDFwTTJQV0RzMk5vSVRDT2pNSThqditVcjlncUg4bW9wWWYxU0Y5NFJqTktxaVNReEFlWHIwQk02YVdiWnplVFhQNXoKTXdsYm0waWNxQUYyNllNWmdFeXBDUjNVaEptbGhnRlZkL2ZQdWFvNmxhV1M1dHNLb1loR1h6N2xsaGVZMWdQTlZwMjhwRTZQa0QzUwplZ2JadTJSTTlpcnkwRyt2NDZPK2d3WURJdVhDeUE5QlJhZ2lTZWs0Tk45Tm9IRnJMRXRiTjVQbHhjVjdVYXZJeFZjK3FsL0RUcS85CjJkL0VOemF5L01WMWtGMlpISnorVGVPRU9XWGUwbUVyTjJJRHVEckoxb1hYQUpaTXNWM2NuL0VVbjJOWjFkOG1oUUIxRHBQSXpXY2sKOFBLenpvOCtFRVdNeFlyTDN2anJIOVNQRTBRVzJUbk1vdlN2Y29tZzlacXlkNzlOQ3BKa0tDdDJGS3NCemlLQmNJY2pvRmo5OCs5QgprWGV4aUlpb3R5UUtIS0pTU2NEeTZydWhpZFU1QzdqaW53cldEaFRpRWhrcHljS3ZpcVVtNDBQM1pDRWJGaHkwSWFzalYrcE44ZWRkCmdmNFRCZXgva0Y1VGV1UkQwZkJaYVFpam96ZGVWbHg0bGVqMVNpaVVaZytiT05Hdm1ySnJrTExscnJqZnQvN05kUDQ9PC9kczpYNTA5Q2VydGlmaWNhdGU+PC9kczpYNTA5RGF0YT48L2RzOktleUluZm8+PC9kczpTaWduYXR1cmU+PHNhbWwycDpFeHRlbnNpb25zPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGVzPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJELTIwMTItMTctRVVJZGVudGlmaWVyIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vRC0yMDEyLTE3LUVVSWRlbnRpZmllciIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9ImZhbHNlIi8+PGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IkVPUkkiIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9sZWdhbHBlcnNvbi9FT1JJIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iTEVJIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vTEVJIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iTGVnYWxOYW1lIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vTGVnYWxOYW1lIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0idHJ1ZSIvPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJMZWdhbEFkZHJlc3MiIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9sZWdhbHBlcnNvbi9MZWdhbFBlcnNvbkFkZHJlc3MiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJmYWxzZSIvPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJMZWdhbFBlcnNvbklkZW50aWZpZXIiIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9sZWdhbHBlcnNvbi9MZWdhbFBlcnNvbklkZW50aWZpZXIiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJ0cnVlIi8+PGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IlNFRUQiIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9sZWdhbHBlcnNvbi9TRUVEIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iU0lDIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vU0lDIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iVGF4UmVmZXJlbmNlIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vVGF4UmVmZXJlbmNlIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iVkFUUmVnaXN0cmF0aW9uIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbGVnYWxwZXJzb24vVkFUUmVnaXN0cmF0aW9uTnVtYmVyIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iQmlydGhOYW1lIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbmF0dXJhbHBlcnNvbi9CaXJ0aE5hbWUiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJmYWxzZSIvPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJDdXJyZW50QWRkcmVzcyIgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vQ3VycmVudEFkZHJlc3MiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJmYWxzZSIvPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJGYW1pbHlOYW1lIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbmF0dXJhbHBlcnNvbi9DdXJyZW50RmFtaWx5TmFtZSIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9InRydWUiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iRmlyc3ROYW1lIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbmF0dXJhbHBlcnNvbi9DdXJyZW50R2l2ZW5OYW1lIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0idHJ1ZSIvPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJEYXRlT2ZCaXJ0aCIgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vRGF0ZU9mQmlydGgiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJ0cnVlIi8+PGVpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZSBGcmllbmRseU5hbWU9IkdlbmRlciIgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vR2VuZGVyIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iUGVyc29uSWRlbnRpZmllciIgTmFtZT0iaHR0cDovL2VpZGFzLmV1cm9wYS5ldS9hdHRyaWJ1dGVzL25hdHVyYWxwZXJzb24vUGVyc29uSWRlbnRpZmllciIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9InRydWUiLz48ZWlkYXM6UmVxdWVzdGVkQXR0cmlidXRlIEZyaWVuZGx5TmFtZT0iUGxhY2VPZkJpcnRoIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbmF0dXJhbHBlcnNvbi9QbGFjZU9mQmlydGgiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJmYWxzZSIvPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJMZWdhbEFkZGl0aW9uYWxBdHRyaWJ1dGUiIE5hbWU9Imh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvYXR0cmlidXRlcy9sZWdhbHBlcnNvbi9MZWdhbEFkZGl0aW9uYWxBdHRyaWJ1dGUiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJmYWxzZSIvPjxlaWRhczpSZXF1ZXN0ZWRBdHRyaWJ1dGUgRnJpZW5kbHlOYW1lPSJBZGRpdGlvbmFsQXR0cmlidXRlIiBOYW1lPSJodHRwOi8vZWlkYXMuZXVyb3BhLmV1L2F0dHJpYnV0ZXMvbmF0dXJhbHBlcnNvbi9BZGRpdGlvbmFsQXR0cmlidXRlIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSIgaXNSZXF1aXJlZD0iZmFsc2UiLz48L2VpZGFzOlJlcXVlc3RlZEF0dHJpYnV0ZXM+PC9zYW1sMnA6RXh0ZW5zaW9ucz48c2FtbDJwOk5hbWVJRFBvbGljeSBBbGxvd0NyZWF0ZT0idHJ1ZSIgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoxLjE6bmFtZWlkLWZvcm1hdDp1bnNwZWNpZmllZCIvPjxzYW1sMnA6UmVxdWVzdGVkQXV0aG5Db250ZXh0IENvbXBhcmlzb249Im1pbmltdW0iPjxzYW1sMjpBdXRobkNvbnRleHRDbGFzc1JlZj5odHRwOi8vZWlkYXMuZXVyb3BhLmV1L0xvQS9sb3c8L3NhbWwyOkF1dGhuQ29udGV4dENsYXNzUmVmPjwvc2FtbDJwOlJlcXVlc3RlZEF1dGhuQ29udGV4dD48L3NhbWwycDpBdXRoblJlcXVlc3Q+";
}