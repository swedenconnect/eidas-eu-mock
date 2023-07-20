/*
 * Copyright (c) 2022 by European Commission
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

package eu.eidas.node.security;

import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static eu.eidas.auth.commons.EidasParameterKeys.TOKEN;

/**
 * Test class for {@link AbstractSecurityRequest}
 */
public class AbstractSecurityRequestTest {

    private final AbstractSecurityRequest request = new AbstractSecurityRequest() {
        @Override
        public int hashCode() {
            return super.hashCode();
        }
    };

    private final String knownAddress = "knownAddress";
    private final String pathInvoked = "ColleagueRequestServlet";
    private final String knownDomain = "known";

    private ConcurrentHashMap<String, List<Long>> ipList = new ConcurrentHashMap<>();
    private List<Long> timesList = new ArrayList<>();

    private final ConfigurationSecurityBean mockConfigurationSecurityBean = Mockito.mock(ConfigurationSecurityBean.class);
    private final ConfigurationSecurityBean oldSecurityBean = request.getConfigurationSecurityBean();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        timesList.add(System.currentTimeMillis() - 2000L);
        ipList.put(knownAddress, timesList);

        Field configurationSecurityBeanField = getConfigurationSecurityBeanField();
        configurationSecurityBeanField.set(request, mockConfigurationSecurityBean);
    }

    @After
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        timesList = new ArrayList<>();
        ipList = new ConcurrentHashMap<>();

        Field configurationSecurityBeanField = getConfigurationSecurityBeanField();
        configurationSecurityBeanField.set(request, oldSecurityBean);
    }

    /**
     * Test method for
     * {@link AbstractSecurityRequest#checkRequest(String, int, int, String, ConcurrentHashMap)}
     * when number of requests is below threshold
     * <p>
     * Must succeed.
     */
    @Test
    public void testCheckRequestKnownAddressBelowThreshold() {
        request.checkRequest(knownAddress, 1, 1, pathInvoked, ipList);
    }

    /**
     * Test method for
     * {@link AbstractSecurityRequest#checkRequest(String, int, int, String, ConcurrentHashMap)}
     * when number of requests exceeds threshold
     * <p>
     * Must fail.
     */
    @Test
    public void testCheckRequestKnownAddressOverThreshold() {
        expectedException.expect(ProxyServiceError.class);

        timesList.add(System.currentTimeMillis() + 1);
        ipList.replace(knownAddress, timesList);
        request.checkRequest(knownAddress, 1, 1, pathInvoked, ipList);
    }

    /**
     * Test method for
     * {@link AbstractSecurityRequest#checkRequest(String, int, int, String, ConcurrentHashMap)}
     * when address is not yet known
     * <p>
     * Must succeed.
     */
    @Test
    public void testCheckRequestNewAddress() {
        String newAddress = "newAddress";
        request.checkRequest(newAddress, 1, 1, pathInvoked, ipList);
    }

    /**
     * Test method for
     * {@link AbstractSecurityRequest#checkRequest(String, int, int, String, ConcurrentHashMap)}
     * when address is already known
     * <p>
     * Must fail.
     */
    @Test
    public void testCheckRequestKnownAddress() {
        expectedException.expect(ProxyServiceError.class);

        String newAddress = "knownAddress";
        request.checkRequest(newAddress, 3, 1, pathInvoked, ipList);
    }
    /**
     * Test method for
     * {@link AbstractSecurityRequest#checkDomain(String, String, HttpServletRequest)}
     * when domain is known
     * <p>
     * Must succeed.
     */
    @Test
    public void testCheckDomainKnown() {
        HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockConfigurationSecurityBean.getTrustedDomains()).thenReturn(knownDomain);
        String tokenBase64 = "token";
        Mockito.when(mockHttpServletRequest.getAttribute(TOKEN.toString())).thenReturn(tokenBase64);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());

        request.checkDomain(knownDomain, pathInvoked, mockHttpServletRequest);
    }

    /**
     * Test method for
     * {@link AbstractSecurityRequest#checkDomain(String, String, HttpServletRequest)}
     * when domain is unknown
     * <p>
     * Must fail.
     */
    @Test
    public void testCheckDomainUnknown() {
        expectedException.expect(ProxyServiceError.class);

        HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockConfigurationSecurityBean.getTrustedDomains()).thenReturn(knownDomain);

        String unknownDomain = "unknown";
        request.checkDomain(unknownDomain, pathInvoked, mockHttpServletRequest);
    }

    /**
     * Test method for
     * {@link AbstractSecurityRequest#checkDomain(String, String, HttpServletRequest)}
     * when spUrl in domain is incorrect
     * <p>
     * Must fail.
     */
    @Test
    public void testCheckDomainBadSpUrl() {
        expectedException.expect(ProxyServiceError.class);

        Map<String, String[]> parameterMap = new HashMap<>();
        String[] parameterValues = {"BadValue"};
        parameterMap.put("spUrl", parameterValues);

        HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockConfigurationSecurityBean.getTrustedDomains()).thenReturn(knownDomain);
        String tokenBase64 = "token";
        Mockito.when(mockHttpServletRequest.getAttribute(TOKEN.toString())).thenReturn(tokenBase64);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(BindingMethod.POST.getValue());
        Mockito.when(mockHttpServletRequest.getParameterMap()).thenReturn(parameterMap);

        request.checkDomain(knownDomain, pathInvoked, mockHttpServletRequest);
    }

    private Field getConfigurationSecurityBeanField() throws NoSuchFieldException {
        Field configurationSecurityBeanField = AbstractSecurityRequest.class.getDeclaredField("configurationSecurityBean");
        configurationSecurityBeanField.setAccessible(true);
        return configurationSecurityBeanField;
    }
}