/*
 * Copyright (c) 2025 by European Commission
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
package eu.eidas.node.auth.service.tests;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.engine.DefaultProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.core.impl.AbstractProtocolSigner;
import eu.eidas.auth.engine.core.impl.CertificateValidator;
import eu.eidas.auth.engine.xml.opensaml.CertificateVerifierParams;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.auth.service.AUSERVICESAML;
import eu.eidas.node.auth.util.tests.TestingConstants;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.utils.ReflectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opensaml.security.x509.X509Credential;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.lang.reflect.Field;
import java.security.cert.X509Certificate;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Verifies {@link ProtocolEngine} behavior in scenarios involving self-signed certificates.
 */
public class AUSERVICESAMLCertificateTest {

    private static String SAML_TOKEN = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9Im5vIj8+CjxzYW1sMnA6QXV0aG5SZXF1ZXN0CiAgICB4bWxuczpzYW1sMnA9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpwcm90b2NvbCIKICAgIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIgogICAgeG1sbnM6c2FtbDI9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iCiAgICB4bWxuczpzdG9yaz0idXJuOmV1OnN0b3JrOm5hbWVzOnRjOlNUT1JLOjEuMDphc3NlcnRpb24iCiAgICB4bWxuczpzdG9ya3A9InVybjpldTpzdG9yazpuYW1lczp0YzpTVE9SSzoxLjA6cHJvdG9jb2wiIEFzc2VydGlvbkNvbnN1bWVyU2VydmljZVVSTD0iaHR0cDovL3NwLmxvY2FsOjkwOTAvU1AvUmV0dXJuUGFnZSIgQ29uc2VudD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmNvbnNlbnQ6dW5zcGVjaWZpZWQiIERlc3RpbmF0aW9uPSJodHRwOi8vcGVwcy5sb2NhbDo5MDkwL1BFUFMvU2VydmljZVByb3ZpZGVyIiBGb3JjZUF1dGhuPSJ0cnVlIiBJRD0iX2QwNDhjYjMxNzgxMzg0NWIzMmE3YTJiNzVmM2JhZDU5IiBJc1Bhc3NpdmU9ImZhbHNlIiBJc3N1ZUluc3RhbnQ9IjIwMTEtMTEtMzBUMTY6MDk6NTcuODI0WiIgUHJvdG9jb2xCaW5kaW5nPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YmluZGluZ3M6SFRUUC1QT1NUIiBQcm92aWRlck5hbWU9IkRFTU8tU1AiIFZlcnNpb249IjIuMCI+CiAgICA8c2FtbDI6SXNzdWVyIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5Ij5odHRwOi8vUy1QRVBTLmdvdi54eDwvc2FtbDI6SXNzdWVyPgogICAgPGRzOlNpZ25hdHVyZT4KICAgICAgICA8ZHM6U2lnbmVkSW5mbz4KICAgICAgICAgICAgPGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KICAgICAgICAgICAgPGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDcvMDUveG1sZHNpZy1tb3JlI3NoYTI1Ni1yc2EtTUdGMSIvPgogICAgICAgICAgICA8ZHM6UmVmZXJlbmNlIFVSST0iI19kMDQ4Y2IzMTc4MTM4NDViMzJhN2EyYjc1ZjNiYWQ1OSI+CiAgICAgICAgICAgICAgICA8ZHM6VHJhbnNmb3Jtcz4KICAgICAgICAgICAgICAgICAgICA8ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz4KICAgICAgICAgICAgICAgICAgICA8ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+CiAgICAgICAgICAgICAgICA8L2RzOlRyYW5zZm9ybXM+CiAgICAgICAgICAgICAgICA8ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhMjU2Ii8+CiAgICAgICAgICAgICAgICA8ZHM6RGlnZXN0VmFsdWU+S3hoR05ybXVTdzBsWlVwYlB6ZXZvRDlJNFdzPTwvZHM6RGlnZXN0VmFsdWU+CiAgICAgICAgICAgIDwvZHM6UmVmZXJlbmNlPgogICAgICAgIDwvZHM6U2lnbmVkSW5mbz4KICAgICAgICA8ZHM6U2lnbmF0dXJlVmFsdWU+SDRGeDRCRFoxckVBbC9LT1g5SkgzV0dwK2F5ZUZyL0ZMa0s4TmRTaHJwTUk1clcvaW9MVjhmVXlRcWcyVjkvS2JCQXlxWFpMMU0zSWhaMUIwM25pdmFHcVdRV2JSSU5PazIxK28zNktwMjhrclFDZmp2SGFVU2wrQUc1dWlkYUlPQVJlR09CRG5DVUpGR3poRS93NzVlc1oybkpyYmtHY1ViSVFqaUlscGowTG9udnJRanRvOC95amQwUk01bTVJSkNEK0JvRVRna1k2TGZqNHRReDNiNWRHK2pvUnJTeDhHZmJDVjVNdnBxNGd0eDJ3RzFIdURyWUNZQTY1SHFKSElsTnFNZEx4bUFFcTN2Q2EyN0xBT0VFZTRmTENzaHJVbTJJRlZIZWFZZVBXY041SFJFK1VEa3kxWWVDWlJtaWxraCtpQXBLdS9kTGpDN3FxektpS3VnPT08L2RzOlNpZ25hdHVyZVZhbHVlPgogICAgICAgIDxkczpLZXlJbmZvPgogICAgICAgICAgICA8ZHM6WDUwOURhdGE+CiAgICAgICAgICAgICAgICA8ZHM6WDUwOUNlcnRpZmljYXRlPk1JSURKekNDQWc4Q0JFdW9uYkl3RFFZSktvWklodmNOQVFFRkJRQXdXREVMTUFrR0ExVUVCaE1DUlZNeERqQU1CZ05WQkFnTUJWTncKWVdsdU1ROHdEUVlEVlFRSERBWk5ZV1J5YVdReERqQU1CZ05WQkFvTUJVbHVaSEpoTVJnd0ZnWURWUVFEREE5c2IyTmhiQzFrWlcxdgpMV05sY25Rd0hoY05NVEF3TXpJek1UQTFNek00V2hjTk1UQXdOakF4TVRBMU16TTRXakJZTVFzd0NRWURWUVFHRXdKRlV6RU9NQXdHCkExVUVDQXdGVTNCaGFXNHhEekFOQmdOVkJBY01CazFoWkhKcFpERU9NQXdHQTFVRUNnd0ZTVzVrY21FeEdEQVdCZ05WQkFNTUQyeHYKWTJGc0xXUmxiVzh0WTJWeWREQ0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQQURDQ0FRb0NnZ0VCQUpmZFExL21rTTlzcXFhYgpyaThxcXFYNHMwSjZVRUVYeUYrMEFqTFU4UkM0V01lWUZTWjV0ZndueUxzb1hZMC85YlpMWG5CeFNpYlFZYWY0U25wWkpobFZBNHNVCjBkOHF5RVdBMk92WEdEQ203dXNwR0hHM0NSUWE3ZnBIc21IbWZJQXFobzlYRGx3cHdKZHg1Z0s0RWRjNmFBcmNNUWZqcU1oeTFzMW4KNk9mMWkxbE1HR3NHckVSSVJZN2JpSVF1LzhuSVRiSEgxOHNVQVpNRzF1L1EwQXZpOU8zTFd5c3dIWFVvdVo4MThmV3dHNnhydTJONQp5NnZ0Vk8vU0wzZG9SU2RxWWtFbXM5M1RneFR0YUtnOFhOZTI0emhOVHRlNm52V0xhS2VzaTJLelpHQzU3SFU0N0hCRkVzOE5Xazd6CjlCRGY4dUx5UHo5VkRhaHdWS3pNdG84Q0F3RUFBVEFOQmdrcWhraUc5dzBCQVFVRkFBT0NBUUVBZ3B1UmVYUTdEc2Zkb2U1enp5MkYKNmtuMXFxdE1rUkdzQlBLbkRGQ0kvR2tJWnBCaXFsZndkaDZzaXBuRktXZktFRGxQS04xa0VoYmtEUjJHQTFDcDRGNFpTTC9IdW1aTQpwV2FiUmhEeGhUUHZISUZiYlNoUERKWTkzK2p3L3lRZWFXZ011aHczV3pkSDlUclZvUlpIMFE3c0cxcElKbzUvNklvZ0lad0Z2SkhUCi9ORHV0S211clU2THc4Vm5kZThQZm5RZCtGVEVoejBTRUd5S2tXakF1aEdiSmZzZUJ5L3ozTCtNSnExcmRTUTlQRjd3WER2V056SnEKeWFOQlVXV0JWMVRza3ZrTlpYY2FndXJvVlB5MlhoQTFhaXhsQWFqV0VYTGs2VXVqNVVZcWFsclYvcmVOWXZEdlkwQlYyQ0luNTFyNwpQcG04SUZWVGs4bVlmWDgvanc9PTwvZHM6WDUwOUNlcnRpZmljYXRlPgogICAgICAgICAgICA8L2RzOlg1MDlEYXRhPgogICAgICAgIDwvZHM6S2V5SW5mbz4KICAgIDwvZHM6U2lnbmF0dXJlPgogICAgPHNhbWwycDpFeHRlbnNpb25zPgogICAgICAgIDxzdG9yazpRdWFsaXR5QXV0aGVudGljYXRpb25Bc3N1cmFuY2VMZXZlbD4zPC9zdG9yazpRdWFsaXR5QXV0aGVudGljYXRpb25Bc3N1cmFuY2VMZXZlbD4KICAgICAgICA8c3Rvcms6c3BTZWN0b3I+REVNTy1TUC1TRUNUT1I8L3N0b3JrOnNwU2VjdG9yPgogICAgICAgIDxzdG9yazpzcEluc3RpdHV0aW9uPkRFTU8tU1A8L3N0b3JrOnNwSW5zdGl0dXRpb24+CiAgICAgICAgPHN0b3JrOnNwQXBwbGljYXRpb24+REVNTy1TUC1BUFBMSUNBVElPTjwvc3Rvcms6c3BBcHBsaWNhdGlvbj4KICAgICAgICA8c3Rvcms6c3BDb3VudHJ5PlBUPC9zdG9yazpzcENvdW50cnk+CiAgICAgICAgPHN0b3JrcDplSURTZWN0b3JTaGFyZT50cnVlPC9zdG9ya3A6ZUlEU2VjdG9yU2hhcmU+CiAgICAgICAgPHN0b3JrcDplSURDcm9zc1NlY3RvclNoYXJlPnRydWU8L3N0b3JrcDplSURDcm9zc1NlY3RvclNoYXJlPgogICAgICAgIDxzdG9ya3A6ZUlEQ3Jvc3NCb3JkZXJTaGFyZT50cnVlPC9zdG9ya3A6ZUlEQ3Jvc3NCb3JkZXJTaGFyZT4KICAgICAgICA8c3RvcmtwOlJlcXVlc3RlZEF0dHJpYnV0ZXM+CiAgICAgICAgICAgIDxzdG9yazpSZXF1ZXN0ZWRBdHRyaWJ1dGUgTmFtZT0iaHR0cDovL3d3dy5zdG9yay5nb3YuZXUvMS4wL2dpdmVuTmFtZSIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9InRydWUiLz4KICAgICAgICAgICAgPHN0b3JrOlJlcXVlc3RlZEF0dHJpYnV0ZSBOYW1lPSJodHRwOi8vd3d3LnN0b3JrLmdvdi5ldS8xLjAvZUlkZW50aWZpZXIiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJ0cnVlIi8+CiAgICAgICAgPC9zdG9ya3A6UmVxdWVzdGVkQXR0cmlidXRlcz4KICAgICAgICA8c3RvcmtwOkF1dGhlbnRpY2F0aW9uQXR0cmlidXRlcz4KICAgICAgICAgICAgPHN0b3JrcDpWSURQQXV0aGVudGljYXRpb25BdHRyaWJ1dGVzPgogICAgICAgICAgICAgICAgPHN0b3JrcDpDaXRpemVuQ291bnRyeUNvZGU+TE88L3N0b3JrcDpDaXRpemVuQ291bnRyeUNvZGU+CiAgICAgICAgICAgICAgICA8c3RvcmtwOlNQSW5mb3JtYXRpb24+CiAgICAgICAgICAgICAgICAgICAgPHN0b3JrcDpTUElEPkRFTU8tU1A8L3N0b3JrcDpTUElEPgogICAgICAgICAgICAgICAgPC9zdG9ya3A6U1BJbmZvcm1hdGlvbj4KICAgICAgICAgICAgPC9zdG9ya3A6VklEUEF1dGhlbnRpY2F0aW9uQXR0cmlidXRlcz4KICAgICAgICA8L3N0b3JrcDpBdXRoZW50aWNhdGlvbkF0dHJpYnV0ZXM+CiAgICA8L3NhbWwycDpFeHRlbnNpb25zPgo8L3NhbWwycDpBdXRoblJlcXVlc3Q+";
    private final byte[] saml = EidasStringUtil.decodeBytesFromBase64(SAML_TOKEN);

    private AUSERVICESAML auServiceSaml;
    private ApplicationContext oldContext = null;
    private final ApplicationContext mockApplicationContext = Mockito.mock(ApplicationContext.class);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        auServiceSaml = new AUSERVICESAML();
        auServiceSaml.setNodeProtocolEngineFactory(DefaultProtocolEngineFactory.getInstance());

        overrideStaticApplicationContext();
        mockMessageSource();
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * Verifies that an {@link EIDASSAMLEngineException} is thrown when processing a SAML request signed with a self-signed certificate.
     * <p>
     * The test expects a {@link ProxyServiceError} to be thrown with an {@link EIDASSAMLEngineException} as cause.
     * The property disallow.self.signed.certificate is set to true in the SignModule_Service_DisallowSelfSigned.xml test configuration file.
     * <p>
     * Must fail.
     */
    @Test
    public void testProcessConnectorRequestWithSelfSignedCertificateThrowsException() {
        expectedException.expect(ProxyServiceError.class);
        expectedException.expectMessage(EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
        expectedException.expectCause(allOf(
                isA(EIDASSAMLEngineException.class),
                hasProperty("additionalInformation", allOf(
                        containsString("ERROR : The certificate with reference"),
                        containsString("failed check (selfsigned)")
                ))
        ));

        // set the SAML engine instance defined in SignModule_Service_DisallowSelfSigned.xml
        auServiceSaml.setSamlEngineInstanceName("Service_DisallowSelfSigned");

        auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * Verifies that no {@link EIDASSAMLEngineException} is thrown when processing a SAML request signed with a self-signed certificate.
     * <p>
     * The test expects the {@link CertificateValidator#checkCertificateIssuer(X509Certificate)} pass successfully when
     * the property disallow.self.signed.certificate is set to false in the SignModule_Service.xml test configuration file.
     * <p>
     * Note: The test fails due to {@link AbstractProtocolSigner#checkValidTrust(List, X509Credential, CertificateVerifierParams)}.
     * This does not affect the purpose of this test.
     * <p>
     * Must fail.
     */
    @Test
    public void testProcessConnectorRequestWithSelfSignedCertificateSucceedsWhenAllowed() {
        expectedException.expect(ProxyServiceError.class);
        expectedException.expectMessage(EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
        expectedException.expectCause(allOf(
                isA(EIDASSAMLEngineException.class),
                hasProperty("additionalInformation", allOf(
                        containsString("Invalid chain of trust")
                ))
        ));

        // set the SAML engine instance defined in SignModule_Service.xml
        auServiceSaml.setSamlEngineInstanceName("Service");

        auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");
    }

    /**
     * Mocks the {@code sysadminMessageSource} bean in the application context.
     * <p>
     * Configures a {@link ResourceBundleMessageSource} mock to return a generic
     * "pretty error message" for any message lookup. This allows tests to run
     * without relying on actual message resource files.
     */
    private void mockMessageSource() {
        final ResourceBundleMessageSource resourceBundleMessageSource = mock(ResourceBundleMessageSource.class);
        Mockito.when(resourceBundleMessageSource.getMessage(anyString(), any(), any())).thenReturn("pretty error message");
        Mockito.when(mockApplicationContext.getBean("sysadminMessageSource")).thenReturn(resourceBundleMessageSource);
    }

    /**
     * Overrides the static {@code CONTEXT} field in {@link BeanProvider} with a mock {@link ApplicationContext}.
     * <p>
     * Stores the original context in {@code oldContext} and replaces it via reflection using {@link ReflectionUtils}.
     * This allows unit tests to control Spring bean resolution behavior without a real application context.
     *
     * @throws NoSuchFieldException   if the {@code CONTEXT} field does not exist
     * @throws IllegalAccessException if the field access or modification fails
     */
    private void overrideStaticApplicationContext() throws NoSuchFieldException, IllegalAccessException {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
    }

    @After
    public void tearDown() {
        if (oldContext != null) {
            ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", oldContext);
            oldContext = null;
        }
    }
}
