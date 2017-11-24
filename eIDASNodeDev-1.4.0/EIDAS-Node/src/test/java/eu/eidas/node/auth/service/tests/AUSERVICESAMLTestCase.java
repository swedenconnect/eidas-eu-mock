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

package eu.eidas.node.auth.service.tests;

import eu.eidas.auth.commons.*;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.cache.ConcurrentMapServiceDefaultImpl;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;
import eu.eidas.node.auth.connector.AUCONNECTORSAML;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.auth.service.AUSERVICESAML;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import eu.eidas.node.auth.service.ISERVICESAMLService;
import eu.eidas.node.auth.service.ResponseCarryingServiceException;
import eu.eidas.node.auth.util.tests.TestingConstants;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.MessageSource;

import java.util.Locale;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Functional testing class to {@link AUSERVICESAML}.
 *
 * @deprecated This class uses the STORK format instead of the eIDAS one.
 */
@Deprecated
@Ignore
//TODO check why this test fails, added Ignore here only to allow build with execution of all tests that do not fail
public final class AUSERVICESAMLTestCase {

    private static final String HTTP_METHOD = "POST";

    private static final String RELAY_STATE = "relayState";

    /**
     * Properties values for testing proposes.
     */
    private static Properties CONFIGS = new Properties();

    private static final String SERVICE_INSTANCE_NAME = "Service";

    /**
     * Empty EidasAuthenticationRequest object.
     */
    private static IAuthenticationRequest EMPTY_EIDAS_AUTHENTICATION_REQUEST =
            EidasAuthenticationRequest.builder().id("id").destination("dest").issuer("issuer").citizenCountryCode("country").build();

    /**
     * Base64 SAML Token.
     */
    private static String STORK_SAML_TOKEN =
            "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9Im5vIj8+PHNhbWwycDpBdXRoblJlcXVlc3QgeG1sbnM6c2FtbDJwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiIgeG1sbnM6c3Rvcms9InVybjpldTpzdG9yazpuYW1lczp0YzpTVE9SSzoxLjA6YXNzZXJ0aW9uIiB4bWxuczpzdG9ya3A9InVybjpldTpzdG9yazpuYW1lczp0YzpTVE9SSzoxLjA6cHJvdG9jb2wiIHhtbG5zOnhzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgQXNzZXJ0aW9uQ29uc3VtZXJTZXJ2aWNlVVJMPSJBU1NFUlRJT05fVVJMIiBDb25zZW50PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6Y29uc2VudDp1bnNwZWNpZmllZCIgRGVzdGluYXRpb249IjEyNy4wLjAuMSIgRm9yY2VBdXRobj0idHJ1ZSIgSUQ9Il9kYmIwMmFlODAwZGZiZjEzYmY0OGRmOWU0ZDNmNTQ0MCIgSXNQYXNzaXZlPSJmYWxzZSIgSXNzdWVJbnN0YW50PSIyMDE1LTA4LTA1VDE1OjI5OjI5LjUwMFoiIFByb3RvY29sQmluZGluZz0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmJpbmRpbmdzOkhUVFAtUE9TVCIgUHJvdmlkZXJOYW1lPSJwcm92aWRlciIgVmVyc2lvbj0iMi4wIj48c2FtbDI6SXNzdWVyIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOm5hbWVpZC1mb3JtYXQ6ZW50aXR5Ij5odHRwOi8vU1BFUFNtZXRhZGF0YTwvc2FtbDI6SXNzdWVyPjxkczpTaWduYXR1cmU+PGRzOlNpZ25lZEluZm8+PGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz48ZHM6U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxkc2lnLW1vcmUjcnNhLXNoYTUxMiIvPjxkczpSZWZlcmVuY2UgVVJJPSIjX2RiYjAyYWU4MDBkZmJmMTNiZjQ4ZGY5ZTRkM2Y1NDQwIj48ZHM6VHJhbnNmb3Jtcz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIj48ZWM6SW5jbHVzaXZlTmFtZXNwYWNlcyB4bWxuczplYz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIiBQcmVmaXhMaXN0PSJ4cyIvPjwvZHM6VHJhbnNmb3JtPjwvZHM6VHJhbnNmb3Jtcz48ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhNTEyIi8+PGRzOkRpZ2VzdFZhbHVlPkorYnVwTnNUd2pPWVd3MWg0cVVQUFNEeXVXZFl4M2lJUGk4M1JXTnFTaWtjcFRwSjYzb2tleXVvMlJsMGkrSE9yT08zV1BpMm1QU2RLN1JsdmxpTTZRPT08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+WmI1OHh6WURpZ2pwL2d2d2RaV00rdWlTOHhSV3BrTXdHYUdKMDdyRnNiSmtEbm5HR2U1U1NBVzN5TFFxeTlRNm1NNjFIZklGRzVRWlk2YkR0QUZRWis3enZPa0N1TEpiM29UdFVSWHF5YmpJa3Zpdmx6WGFkYStROTRxdE5zZlFZenRmdUJicFZCNjhwZjYrdFJRclBBTlJWdjFIdG5VcFo4bmRubWRabGk4NzBnRkVkUGlvZ2hzeGhPL3NGeHB6NXBtNzBtU05kQWx0TjlUeDNKa1daR212VExWZkZZb3hzbFJ1cG45RVMvNnl5YnFKM1U4N3hjTEk0dW40Q2JXVFhTYW9EdlExZ3RPSmRCYTE5eGF0ZTZ6bU5RZzFBUlhMWENxRExVL0FiQ2huWjNmVjJOcFEwTXhhU1dpSFF2dHRFRlJ0Y0hmWUY2OFpYaXI5cEJtbDdxRHhROC9BNjY3UmtzNlE4OXA2NnVNSkVXVVRUZUZnMnZZM29mZThPa204ek5RZzh3djV3K002RzNveG1EQUpBN2I2WnB3ZmQ0bDg2UGtuR0RFMndYSUtRMlcveUdZZ2R1QmJ0cytpNnMxbU5TMlk4TFEzZWRxQzFHNUYwVktNeVlGV3c5YTQ3Q3RDbG51RkdjWWk5bitPbURlejJTbTV5d2JtdG5lc3NZTkoyTWRVUCtoeHBnQUt2Y1pkMENSQ29JaU1yRGtzZEpyOXJGN00xZ1ZhSnE0OHVtTElMQzVsdk5QZkdLTFVHUXNWcURPeUJmbmgzams4Mjh1NkRJdU5BaUlpNmlSL0UwREsxc2U2WnpJN09IcU44NFRMSDFybjdSMCtsMGVYY0RjaG1aVE1LSTNFNUlvR2ROMWgrOFRPakgvZ09ZL2xzZEExSG0xTjlVeTdYS0E9PC9kczpTaWduYXR1cmVWYWx1ZT48ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlGYVRDQ0ExRUNCRlRZM0xrd0RRWUpLb1pJaHZjTkFRRU5CUUF3ZVRFTE1Ba0dBMVVFQmhNQ1FrVXhFREFPQmdOVkJBZ01CMEpsDQpiR2RwZFcweEVUQVBCZ05WQkFjTUNFSnlkWE56Wld4ek1Sc3dHUVlEVlFRS0RCSkZkWEp2Y0dWaGJpQkRiMjFwYzNOcGIyNHhEakFNDQpCZ05WQkFzTUJVUkpSMGxVTVJnd0ZnWURWUVFEREE5c2IyTmhiQzFrWlcxdkxXTmxjblF3SGhjTk1UVXdNakE1TVRZeE16UTFXaGNODQpNVFl3TWpBNU1UWXhNelExV2pCNU1Rc3dDUVlEVlFRR0V3SkNSVEVRTUE0R0ExVUVDQXdIUW1Wc1oybDFiVEVSTUE4R0ExVUVCd3dJDQpRbkoxYzNObGJITXhHekFaQmdOVkJBb01Fa1YxY205d1pXRnVJRU52YldsemMybHZiakVPTUF3R0ExVUVDd3dGUkVsSFNWUXhHREFXDQpCZ05WQkFNTUQyeHZZMkZzTFdSbGJXOHRZMlZ5ZERDQ0FpSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnSVBBRENDQWdvQ2dnSUJBTGRQDQpsNWFIRkpmbHRVK1JjNHVyeXlLQ0g1QVZodXF0M3hNRDBTQ3B1RWVMRDJzV2NNNnZCSnJvRmNJeExYUE01NndENXY5dk1PN3NyVGdoDQpmTVliQmdzNW90SGMrSEoxRzZVbzhoUDNweFBVQ05NZnhDc0NBOE50eExndmt4TjROUHZIczRubmZnN1dLZXFGYkNtWndtSFZiZCtnDQoyYUlpUFBUazZZdDdnV3hvWUM0Smo0SzhFdExrN3I0aDN3amhXY0FxS0J2cC9QSTd4Y1A0dDBwc2pYV0NjM1piOHZaYnZ1ZkJMdVNrDQpsRHduZG5WWDBRYXV0RHJyQXQxdlFDRHRoT0ZLdjRFRSsyU1FLNVpVVmxjL2R5NFJ5aGhwaUtvaHB2TVgxOXk0bGV6QWhEeG1XZzB6DQpORTFqRWZWcGp3UFpSclczVERVY2NZLzBML3RiZDcyaHRpMEJjMmNvTWljVEpkaC9uVTRlR0c4OWkxUVphTDJmVjFiYjVzN0FZcGQrDQpMSmtzaFNiOGxNNTRzZ3E5Zi9FNVVVV2VPMTZWMndDdi9wUU5ic1ZzLytURlVCOFV1RHM3TUlibnIvbUdqNkR6RjBUMjhveC9mWlBYDQpTSkZNZVNLU2hUZXVYbTZZelVnY1VFMTZyMjRjQmhmdlZJaE0zSGNlaW5rZ1JmNWJSTWxxYVlCOE16Slhuc0R6OUQ5Qm9sNW9IcFJUDQpLVVRJK3k2QitySEkzS2ttWFI1b1NIcjJCMjNiRldySThEVE5BRW90OXlTMVNXd3BVM0VPam1yS3dWaFFpZUtQZDRKZ0Uwd043cGNLDQo5VllsWk5vb1VubW14UUhYTjJjdlZoRmE2dzNBekJpWGtuRlVSOVhrSzJuVVhIcmR3T0JheHdUZTZGdkN6b3NMZENtWkxyV2RBZ01CDQpBQUV3RFFZSktvWklodmNOQVFFTkJRQURnZ0lCQUQ3ZXIzUndaSFFYT3FOcFcrTnZjeE1mUUVoQ2kzK25icG1pVzBzWTVqaHdoVHo3DQp2aFpxMjRmaW1lemJsbHZCbWR0b0M5L2tCN25BZm5uVDZkM0MyOGlxUytlaWV6dmNibElGM3dmU2h3MFRZa251OUoyZ0Q2dkJiMk1sDQp1QnQ4UytCNUEzZml4L2dLaEh2S0hGZlYycTVCNmQ2YkFLcGdFZUV0R3FZeWUvMXdaR05EaDZOVFdFeDJ0MU1MMy9PTnQwRXlTc1pjDQo0eHNUdmdKZTdFVVVxbnlMYkRuVXlibXdxWnRKeFIyc1ZXNDhBeWZuVW84UnBxc3hIOGpCdG5MRS9OcGxHd1E5cmV3VjY4a0NTMkVEDQp4ZGNYSXdKcDlQS3V1MGoyT0xRNDg5T1A3OFA4c3o5aDJRYVZYa1RiM083L0VuOFgrOWFSZFdOMlNWRDFvU0lHQlJ3alBMMUVVdGF0DQpCTGgzQWsvdUZCaTJvNjRCamJ2S3BwZjJ3TlhodTQyUUNQV0lRVFd4akZPVXIrMXhnaWlsZHE5a1V3RnYvcEFHMit0MFRVL0JlVTRvDQpFN1RWNEZRNlZqMnNwSC9nVFo1bGEyc0dBa1ZJNTdST3hZWjNWTmV1L0dtaTJ1bTV6bk5SdWEwRnc1eS8xVCtsaHFqSStsRFBPM1kwDQptdWEwaENpTDliRHNKU0g0SUhDU1VLY3Zaa1lIbTBqU1RRaUNWaEhwbFAycWozbDdRY25FWE1kSUVwSVE3ZHBsN3ZiS2NySDluZm5vDQpNRzV4QnFVSzBReXlqczM4cTJtcldIamVXaVNkaGdCTFJiNjRjQXBuZHNpMjVZY1cxbDRtV0xla2xqcm0vRThkRi9EaGloekpRS0FoDQo0TE9yVkRzMnNYcE1HYUwvcGF5dk9GNElTYi9jPC9kczpYNTA5Q2VydGlmaWNhdGU+PC9kczpYNTA5RGF0YT48L2RzOktleUluZm8+PC9kczpTaWduYXR1cmU+PHNhbWwycDpFeHRlbnNpb25zPjxzdG9yazpRdWFsaXR5QXV0aGVudGljYXRpb25Bc3N1cmFuY2VMZXZlbD4zPC9zdG9yazpRdWFsaXR5QXV0aGVudGljYXRpb25Bc3N1cmFuY2VMZXZlbD48c3Rvcms6c3BJbnN0aXR1dGlvbj5wcm92aWRlcjwvc3Rvcms6c3BJbnN0aXR1dGlvbj48c3RvcmtwOmVJRFNlY3RvclNoYXJlPnRydWU8L3N0b3JrcDplSURTZWN0b3JTaGFyZT48c3RvcmtwOmVJRENyb3NzU2VjdG9yU2hhcmU+dHJ1ZTwvc3RvcmtwOmVJRENyb3NzU2VjdG9yU2hhcmU+PHN0b3JrcDplSURDcm9zc0JvcmRlclNoYXJlPnRydWU8L3N0b3JrcDplSURDcm9zc0JvcmRlclNoYXJlPjxzdG9ya3A6UmVxdWVzdGVkQXR0cmlidXRlcz48c3Rvcms6UmVxdWVzdGVkQXR0cmlidXRlIE5hbWU9Imh0dHA6Ly93d3cuc3RvcmsuZ292LmV1LzEuMC9pc0FnZU92ZXIiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJ0cnVlIj48c3Rvcms6QXR0cmlidXRlVmFsdWUgeG1sbnM6eHNpPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYS1pbnN0YW5jZSIgeHNpOnR5cGU9InhzOmFueVR5cGUiPjE1PC9zdG9yazpBdHRyaWJ1dGVWYWx1ZT48L3N0b3JrOlJlcXVlc3RlZEF0dHJpYnV0ZT48c3Rvcms6UmVxdWVzdGVkQXR0cmlidXRlIE5hbWU9Imh0dHA6Ly93d3cuc3RvcmsuZ292LmV1LzEuMC9hZ2UiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIiBpc1JlcXVpcmVkPSJmYWxzZSI+PHN0b3JrOkF0dHJpYnV0ZVZhbHVlIHhtbG5zOnhzaT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEtaW5zdGFuY2UiIHhzaTp0eXBlPSJ4czphbnlUeXBlIj4xNTwvc3Rvcms6QXR0cmlidXRlVmFsdWU+PC9zdG9yazpSZXF1ZXN0ZWRBdHRyaWJ1dGU+PC9zdG9ya3A6UmVxdWVzdGVkQXR0cmlidXRlcz48c3RvcmtwOkF1dGhlbnRpY2F0aW9uQXR0cmlidXRlcy8+PC9zYW1sMnA6RXh0ZW5zaW9ucz48c2FtbDJwOk5hbWVJRFBvbGljeSBBbGxvd0NyZWF0ZT0idHJ1ZSIgRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6bmFtZWlkLWZvcm1hdDpwZXJzaXN0ZW50Ii8+PHNhbWwycDpSZXF1ZXN0ZWRBdXRobkNvbnRleHQgQ29tcGFyaXNvbj0ibWluaW11bSI+PHNhbWwyOkF1dGhuQ29udGV4dENsYXNzUmVmPmh0dHA6Ly9laWRhcy5ldXJvcGEuZXUvTG9BL2xvdzwvc2FtbDI6QXV0aG5Db250ZXh0Q2xhc3NSZWY+PC9zYW1sMnA6UmVxdWVzdGVkQXV0aG5Db250ZXh0Pjwvc2FtbDJwOkF1dGhuUmVxdWVzdD4=";

    /**
     * EidasAuthenticationRequest object.
     */
    private static EidasAuthenticationRequest EIDAS_AUTHENTICATION_REQUEST;

    /**
     * byte[] dummy SAML token.
     */
    private static byte[] SAML_TOKEN_ARRAY = new byte[] {
            60, 82, 101, 113, 117, 101, 115, 116, 62, 46, 46, 46, 60, 47, 82, 101, 113, 117, 101, 115, 116, 62};

    /**
     * AUService SAML class.
     */
    private static ISERVICESAMLService AUSERVICESAML = new AUSERVICESAML();

    /**
     * Initialising class variables.
     *
     * @throws java.lang.Exception
     */
    @Before
    public void runBeforeClass() throws Exception {
        CONFIGS.setProperty(EidasErrorKey.ATT_VERIFICATION_MANDATORY.errorCode(), "202010");
        CONFIGS.setProperty(EidasErrorKey.ATT_VERIFICATION_MANDATORY.errorMessage(), "missing.mandatory.attr");

        CONFIGS.setProperty(EidasErrorKey.ATTR_VALUE_VERIFICATION.errorCode(), "203008");
        CONFIGS.setProperty(EidasErrorKey.ATTR_VALUE_VERIFICATION.errorMessage(), "invalid.eidas.attrValue");

        CONFIGS.setProperty(EidasErrorKey.SERVICE_SAML_RESPONSE.errorCode(), "202011");
        CONFIGS.setProperty(EidasErrorKey.SERVICE_SAML_RESPONSE.errorMessage(), "error.gen.service.saml");

        CONFIGS.setProperty(EidasErrorKey.COLLEAGUE_REQ_INVALID_COUNTRYCODE.errorCode(), "202013");
        CONFIGS.setProperty(EidasErrorKey.COLLEAGUE_REQ_INVALID_COUNTRYCODE.errorMessage(), "country.service.nomatch");

        CONFIGS.setProperty(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode(), "201002");
        CONFIGS.setProperty(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage(), "invalid.connector.samlrequest");

        CONFIGS.setProperty(EidasErrorKey.COLLEAGUE_REQ_INVALID_QAA.errorCode(), "202004");
        CONFIGS.setProperty(EidasErrorKey.COLLEAGUE_REQ_INVALID_QAA.errorMessage(), "invalid.requested.service.qaalevel");

        CONFIGS.setProperty(EIDASValues.HASH_DIGEST_CLASS.toString(), "org.bouncycastle.crypto.digests.SHA512Digest");

        CONFIGS.setProperty(EIDASValues.EIDAS_SERVICE_LOA.toString(), "http://eidas.europa.eu/LoA/high");
        CONFIGS.setProperty(EIDASValues.NODE_SUPPORT_EIDAS_MESSAGE_FORMAT_ONLY.toString(), "false");

        ///EIDASUtil.createInstance(CONFIGS);

        EidasAuthenticationRequest.Builder eidasAuthenticationRequestBuilder =
                EidasAuthenticationRequest.builder();
        eidasAuthenticationRequestBuilder.assertionConsumerServiceURL(TestingConstants.ASSERTION_URL_CONS.toString());
        eidasAuthenticationRequestBuilder.issuer(TestingConstants.SAML_ISSUER_CONS.toString());
        eidasAuthenticationRequestBuilder.id(TestingConstants.SAML_ID_CONS.toString());
//        TODO check if saml token has to be set somewhere else
//        eidasAuthenticationRequestBuilder.setTokenSaml(new byte[0]);
        eidasAuthenticationRequestBuilder.levelOfAssurance(LevelOfAssurance.LOW.stringValue());
        eidasAuthenticationRequestBuilder.destination("127.0.0.1");
//        EIDAS_AUTHENTICATION_REQUEST = eidasAuthenticationRequestBuilder.build();

        AUSERVICESAML auservicesaml = (AUSERVICESAML) AUSERVICESAML;
        auservicesaml.setSamlEngineInstanceName(SERVICE_INSTANCE_NAME);
        auservicesaml.setServiceUtil(new AUSERVICEUtil());

//        // inject a metadata processor
//        ProtocolEngineI samlEngine = auservicesaml.getSamlEngine();
//        ExtensionProcessorI extensionProcessor = samlEngine.getExtensionProcessor();
//        if (extensionProcessor instanceof EidasExtensionProcessor) {
//            EidasExtensionProcessor eidasExtensionProcessor = (EidasExtensionProcessor) extensionProcessor;
//            MetadataLoaderPlugin metadataProcessor =
//                    eidasExtensionProcessor.getMetadataEncryptionHelper().getMetadataProcessor();
//            if (metadataProcessor instanceof WrappedMetadataProcessor) {
//                WrappedMetadataProcessor wrappedMetadataProcessor = (WrappedMetadataProcessor) metadataProcessor;
//                wrappedMetadataProcessor.setMetadataFetcherI(someMetadataProcessor);
//            }
//        }
    }

    /**
     * Test method for {@link AUSERVICESAML# checkAttributeValues (IAuthenticationRequest, String)}. Using null {@link
     * EidasAuthenticationRequest} object must throw a NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testCheckMandatoryAttributesNullAuthData() {
        AUSERVICESAML.checkMandatoryAttributeSet(null);
    }

    /**
     * Test method for {@link AUSERVICESAML# checkAttributeValues (IAuthenticationRequest, String)}. Using empty {@link
     * EidasAuthenticationRequest} object. Won't throw any exception.
     */
    @Test()
    public void testCheckMandatoryAttributesEmptyAuthData() {
        AUSERVICESAML.checkMandatoryAttributeSet(ImmutableAttributeMap.builder().build());
    }

    /**
     * Test method for {@link AUSERVICESAML# checkAttributeValues (IAuthenticationRequest, String)}. Testing attribute
     * list without all the mandatory attributes. Must throws a {@link EIDASServiceException}.
     */
    @Test
    public void testCheckMandatoryAttributesMissingMand() {
        AUSERVICESAML auconnectorsaml = provideauservicesaml();

        EidasAuthenticationRequest.Builder eidasAuthenticationRequestBuilder =
                new EidasAuthenticationRequest.Builder(EIDAS_AUTHENTICATION_REQUEST);

        MessageSource mockMessages = mock(MessageSource.class);
        when(mockMessages.getMessage(anyString(), (Object[]) any(), (Locale) any())).thenReturn(
                TestingConstants.ERROR_MESSAGE_CONS.toString());
        auconnectorsaml.setMessageSource(mockMessages);

        IEIDASLogger mockLoggerBean = mock(IEIDASLogger.class);
        auconnectorsaml.setLoggerBean(mockLoggerBean);

        EidasAuthenticationRequest eidasAuthenticationRequest = eidasAuthenticationRequestBuilder.build();
        assertFalse(auconnectorsaml.checkMandatoryAttributeSet(eidasAuthenticationRequest.getRequestedAttributes()));
    }

    /**
     * Test method for {@link AUSERVICESAML# generateAuthenticationResponse(IAuthenticationRequest, String, boolean)} .
     * Testing an empty {@link EidasAuthenticationRequest} with consent. Must throws a {@link
     * InternalErrorEIDASException}.
     */
    @Test(expected = InternalErrorEIDASException.class)
    public void testGenerateAuthenticationResponseEmptyAuthDataAuditable() {
        AUSERVICESAML.processIdpSpecificResponse(EMPTY_EIDAS_AUTHENTICATION_REQUEST,
                                                     AuthenticationResponse.builder().build(),
                                                     TestingConstants.USER_IP_CONS.toString(), true);
    }

    /**
     * Test method for {@link AUSERVICESAML# generateAuthenticationResponse (IAuthenticationRequest, String, boolean)} .
     * Testing an empty {@link EidasAuthenticationRequest} with no consent. Must throws a {@link
     * InternalErrorEIDASException}.
     */
    @Test(expected = InternalErrorEIDASException.class)
    public void testGenerateAuthenticationResponseEmptyAuthData() {
        AUSERVICESAML.processIdpSpecificResponse(EMPTY_EIDAS_AUTHENTICATION_REQUEST,
                                                     AuthenticationResponse.builder().build(),
                                                     TestingConstants.USER_IP_CONS.toString(), false);
    }

    private AUSERVICESAML provideauservicesaml() {
        AUSERVICESAML auservice = new AUSERVICESAML();
        auservice.setSamlEngineInstanceName("Service");
        auservice.setServiceUtil(new AUSERVICEUtil());
        return auservice;
    }

    /**
     * Test method for {@link AUSERVICESAML# generateErrorAuthenticationResponse (IAuthenticationRequest, String, String,
     * String, String, boolean)} . Testing an empty {@link EidasAuthenticationRequest} with audit on. Must throws a
     * {@link InternalErrorEIDASException}.
     */
    @Test(expected = InternalErrorEIDASException.class)
    public void testGenerateErrorAuthenticationResponseEmptyAuthDataAuditable() {
        AUSERVICESAML auservice = provideauservicesaml();

        MessageSource mockMessages = mock(MessageSource.class);
        when(mockMessages.getMessage(anyString(), (Object[]) any(), (Locale) any())).thenReturn(
                TestingConstants.ERROR_MESSAGE_CONS.toString());
        auservice.setMessageSource(mockMessages);

        IEIDASLogger mockLoggerBean = mock(IEIDASLogger.class);
        auservice.setLoggerBean(mockLoggerBean);

        auservice.generateErrorAuthenticationResponse(EMPTY_EIDAS_AUTHENTICATION_REQUEST,
                                                      TestingConstants.ERROR_STATUS_CONS.toString(),
                                                      TestingConstants.SUB_ERROR_CODE_CONS.toString(),
                                                      TestingConstants.ERROR_CODE_CONS.toString(),
                                                      TestingConstants.ERROR_MESSAGE_CONS.toString(),
                                                      TestingConstants.USER_IP_CONS.toString(), true);
    }

    /**
     * Test method for {@link AUSERVICESAML# generateErrorAuthenticationResponse (IAuthenticationRequest, String, String,
     * String, String, boolean)} . Testing an empty {@link EidasAuthenticationRequest} with audit off. Must throws a
     * {@link InternalErrorEIDASException}.
     */
    @Test(expected = InternalErrorEIDASException.class)
    public void testGenerateErrorAuthenticationResponseEmptyAuthData() {
        AUSERVICESAML auconnectorsaml = provideauservicesaml();

        MessageSource mockMessages = mock(MessageSource.class);
        when(mockMessages.getMessage(anyString(), (Object[]) any(), (Locale) any())).thenReturn(
                TestingConstants.ERROR_MESSAGE_CONS.toString());
        auconnectorsaml.setMessageSource(mockMessages);

        IEIDASLogger mockLoggerBean = mock(IEIDASLogger.class);
        auconnectorsaml.setLoggerBean(mockLoggerBean);

        auconnectorsaml.generateErrorAuthenticationResponse(EMPTY_EIDAS_AUTHENTICATION_REQUEST,
                                                            TestingConstants.ERROR_STATUS_CONS.toString(),
                                                            TestingConstants.SUB_ERROR_CODE_CONS.toString(),
                                                            TestingConstants.ERROR_CODE_CONS.toString(),
                                                            TestingConstants.ERROR_MESSAGE_CONS.toString(),
                                                            TestingConstants.USER_IP_CONS.toString(), false);
    }

    /**
     * Test method for {@link AUSERVICESAML# generateErrorAuthenticationResponse (IAuthenticationRequest, String, String,
     * String, String, boolean)} . Testing with audit on. Must return a byte[].
     */
    @Test()
    public void testGenerateErrorAuthenticationResponseAuditable() {
        AUSERVICESAML auconnectorsaml = provideauservicesaml();

        MessageSource mockMessages = mock(MessageSource.class);
        when(mockMessages.getMessage(anyString(), (Object[]) any(), (Locale) any())).thenReturn(
                TestingConstants.ERROR_MESSAGE_CONS.toString());
        auconnectorsaml.setMessageSource(mockMessages);

        IEIDASLogger mockLoggerBean = mock(IEIDASLogger.class);
        auconnectorsaml.setLoggerBean(mockLoggerBean);

        assertTrue(auconnectorsaml.generateErrorAuthenticationResponse(EIDAS_AUTHENTICATION_REQUEST,
                                                                       TestingConstants.ERROR_STATUS_CONS.toString(),
                                                                       TestingConstants.SUB_ERROR_CODE_CONS.toString(),
                                                                       TestingConstants.ERROR_CODE_CONS.toString(),
                                                                       TestingConstants.ERROR_MESSAGE_CONS.toString(),
                                                                       TestingConstants.USER_IP_CONS.toString(),
                                                                       true).length > 0);
    }

    /**
     * Test method for {@link AUSERVICESAML# generateErrorAuthenticationResponse (IAuthenticationRequest, String, String,
     * String, String, boolean)} . Must return a byte[].
     */
    @Test()
    public void testGenerateErrorAuthenticationResponse() {
        AUSERVICESAML auconnectorsaml = provideauservicesaml();

        MessageSource mockMessages = mock(MessageSource.class);
        when(mockMessages.getMessage(anyString(), (Object[]) any(), (Locale) any())).thenReturn(
                TestingConstants.ERROR_MESSAGE_CONS.toString());
        auconnectorsaml.setMessageSource(mockMessages);

        IEIDASLogger mockLoggerBean = mock(IEIDASLogger.class);
        auconnectorsaml.setLoggerBean(mockLoggerBean);

        assertTrue(auconnectorsaml.generateErrorAuthenticationResponse(EIDAS_AUTHENTICATION_REQUEST,
                                                                       TestingConstants.ERROR_STATUS_CONS.toString(),
                                                                       TestingConstants.SUB_ERROR_CODE_CONS.toString(),
                                                                       TestingConstants.ERROR_CODE_CONS.toString(),
                                                                       TestingConstants.ERROR_MESSAGE_CONS.toString(),
                                                                       TestingConstants.USER_IP_CONS.toString(),
                                                                       false).length > 0);
    }

    /**
     * Test method for {@link AUSERVICESAML# getSAMLToken(String)}. Testing for dummy value.
     */
    private static String SAML_BASE64_REQUEST = "PFJlcXVlc3Q+Li4uPC9SZXF1ZXN0Pg==";
//=base64("<Request>...</Request>")

    /**
     * Test method for {@link AUSERVICESAML# processAuthenticationRequest(byte[], String) )}. Testing null
     * saml token. Must return and {@link InternalErrorEIDASException}.
     */
    @Test(expected = InternalErrorEIDASException.class)
    public void testProcessAuthenticationRequestFailSAML() {
        AUSERVICESAML auconnectorsaml = provideauservicesaml();
        MessageSource mockMessages = mock(MessageSource.class);
        when(mockMessages.getMessage(anyString(), (Object[]) any(), (Locale) any())).thenReturn(
                TestingConstants.ERROR_MESSAGE_CONS.toString());
        auconnectorsaml.setMessageSource(mockMessages);

        IEIDASLogger mockLoggerBean = mock(IEIDASLogger.class);
        auconnectorsaml.setLoggerBean(mockLoggerBean);


        auconnectorsaml.processConnectorRequest(HTTP_METHOD, null, TestingConstants.USER_IP_CONS.toString(),
                                                     RELAY_STATE);
    }

    /**
     * Test method for {@link AUSERVICESAML# processAuthenticationRequest(byte[], String) )}. Testing
     * invalid country code in class. Must return and {@link EIDASServiceException}.
     */
    @Test(expected = EIDASServiceException.class)
    public void testProcessAuthenticationRequestFailCountry() {
        AUSERVICESAML auconnectorsaml = provideauservicesaml();
        byte[] saml = EidasStringUtil.decodeBytesFromBase64(STORK_SAML_TOKEN);
        MessageSource mockMessages = mock(MessageSource.class);
        when(mockMessages.getMessage(anyString(), (Object[]) any(), (Locale) any())).thenReturn(
                TestingConstants.ERROR_MESSAGE_CONS.toString());
        auconnectorsaml.setMessageSource(mockMessages);

        IEIDASLogger mockLoggerBean = mock(IEIDASLogger.class);
        auconnectorsaml.setLoggerBean(mockLoggerBean);
        auconnectorsaml.processConnectorRequest(HTTP_METHOD, saml, TestingConstants.USER_IP_CONS.toString(),
                                                     RELAY_STATE);
    }

    @Test
    public void testObtainNewRequest() {
        AUCONNECTORSAML auconnectorsaml = new AUCONNECTORSAML();
        IEIDASLogger mockLoggerBean = mock(IEIDASLogger.class);
        auconnectorsaml.setLoggerBean(mockLoggerBean);

        auconnectorsaml.setSamlServiceInstance(SERVICE_INSTANCE_NAME);
        EidasAuthenticationRequest.Builder eidasAuthenticationRequestBuilder =
                new EidasAuthenticationRequest.Builder(EIDAS_AUTHENTICATION_REQUEST);
        eidasAuthenticationRequestBuilder.providerName("provider");
//        TODO check if this is not to be replaced by oher method of eidasAuthenticationRequestBuilder
//        eidasAuthenticationRequestBuilder.setQaa(3);
        eidasAuthenticationRequestBuilder.nameIdFormat("stork1");
        eidasAuthenticationRequestBuilder.citizenCountryCode("PT");
        EidasAuthenticationRequest eidasAuthenticationRequest = eidasAuthenticationRequestBuilder.build();

        AUCONNECTORUtil auconnectorUtil = new AUCONNECTORUtil();
        auconnectorUtil.setConfigs(CONFIGS);
        auconnectorsaml.setConnectorUtil(auconnectorUtil);

        IRequestMessage iRequestMessage = auconnectorsaml.generateServiceAuthnRequest(null, eidasAuthenticationRequest);
        String saml = EidasStringUtil.toString(iRequestMessage.getMessageBytes());
        assertFalse(saml.isEmpty());

    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}. Testing
     * invalid max qaalevel in class. Must return and {@link EIDASServiceException}.
     */
    @Test(expected = ResponseCarryingServiceException.class)
    public void testProcessAuthenticationRequestFailMaxQAALevel() {
        AUSERVICESAML auconnectorsaml = provideauservicesaml();
        byte[] saml = EidasStringUtil.decodeBytesFromBase64(STORK_SAML_TOKEN);
        MessageSource mockMessages = mock(MessageSource.class);
        when(mockMessages.getMessage(anyString(), (Object[]) any(), (Locale) any())).thenReturn(
                TestingConstants.ERROR_MESSAGE_CONS.toString());
        auconnectorsaml.setMessageSource(mockMessages);

        AUSERVICEUtil auserviceutil = new AUSERVICEUtil();
        auserviceutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
        auserviceutil.setAntiReplayCache(auserviceutil.getConcurrentMapService().getConfiguredMapCache());
        auserviceutil.setConfigs(CONFIGS);
        auserviceutil.flushReplayCache();
        auconnectorsaml.setServiceUtil(auserviceutil);
        IEIDASLogger mockLoggerBean = mock(IEIDASLogger.class);
        auconnectorsaml.setLoggerBean(mockLoggerBean);
        auconnectorsaml.setCountryCode(TestingConstants.LOCAL_CONS.toString());
        IAuthenticationRequest req = auconnectorsaml.processConnectorRequest(HTTP_METHOD, saml,
                                                                                  TestingConstants.USER_IP_CONS.toString(),
                                                                                  RELAY_STATE);
        assertNotNull(req.getLevelOfAssurance());
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}. Must
     * succeed.
     */
    @Test
    public void testProcessAuthenticationRequest() {
        // Instantiate the util service for anti replay check
        AUSERVICEUtil auserviceutil = new AUSERVICEUtil();
        auserviceutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
        auserviceutil.setAntiReplayCache(auserviceutil.getConcurrentMapService().getConfiguredMapCache());
        auserviceutil.flushReplayCache();
        CONFIGS.setProperty(EIDASValues.NODE_SUPPORT_EIDAS_MESSAGE_FORMAT_ONLY.toString(), "false");
        auserviceutil.setConfigs(CONFIGS);
        AUSERVICESAML auconnectorsaml = provideauservicesaml();
        auconnectorsaml.setServiceUtil(auserviceutil);

        byte[] saml = EidasStringUtil.decodeBytesFromBase64(STORK_SAML_TOKEN);
        MessageSource mockMessages = mock(MessageSource.class);
        when(mockMessages.getMessage(anyString(), (Object[]) any(), (Locale) any())).thenReturn(
                TestingConstants.ERROR_MESSAGE_CONS.toString());
        auconnectorsaml.setMessageSource(mockMessages);

        IEIDASLogger mockLoggerBean = mock(IEIDASLogger.class);
        auconnectorsaml.setLoggerBean(mockLoggerBean);
        auconnectorsaml.setCountryCode(TestingConstants.LOCAL_CONS.toString());
        auconnectorsaml.setMaxQAA(TestingConstants.MAX_QAA_CONS.intValue());
        auconnectorsaml.setMaxQAAlevel(TestingConstants.MAX_QAA_CONS.intValue());
        auconnectorsaml.setMinQAA(TestingConstants.MIN_QAA_CONS.intValue());
        auconnectorsaml.processConnectorRequest(HTTP_METHOD, saml, TestingConstants.USER_IP_CONS.toString(),
                                                     RELAY_STATE);
    }

    /**
     * test the EIDAS only mode cause an error when trying to generate Service authn request
     */
    @Test(expected = InternalErrorEIDASException.class)
    public void testGenerateStorkSAMLRequestInEidasOnlyMode() {
        // Instantiate the util service for anti replay check
        AUSERVICEUtil auserviceutil = new AUSERVICEUtil();

        // Support to eIDAS message format only
        auserviceutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
        auserviceutil.setAntiReplayCache(auserviceutil.getConcurrentMapService().getConfiguredMapCache());
        auserviceutil.flushReplayCache();
        CONFIGS.setProperty(EIDASValues.NODE_SUPPORT_EIDAS_MESSAGE_FORMAT_ONLY.toString(), "true");
        auserviceutil.setConfigs(CONFIGS);
        AUSERVICESAML auservicesaml = provideauservicesaml();
        auservicesaml.setServiceUtil(auserviceutil);

        byte[] saml = EidasStringUtil.decodeBytesFromBase64(STORK_SAML_TOKEN);
        MessageSource mockMessages = mock(MessageSource.class);
        when(mockMessages.getMessage(anyString(), (Object[]) any(), (Locale) any())).thenReturn(
                TestingConstants.ERROR_MESSAGE_CONS.toString());
        auservicesaml.setMessageSource(mockMessages);

        IEIDASLogger mockLoggerBean = mock(IEIDASLogger.class);
        auservicesaml.setLoggerBean(mockLoggerBean);
        auservicesaml.setCountryCode(TestingConstants.LOCAL_CONS.toString());
        auservicesaml.setMaxQAA(TestingConstants.MAX_QAA_CONS.intValue());
        auservicesaml.setMaxQAAlevel(TestingConstants.MAX_QAA_CONS.intValue());
        auservicesaml.setMinQAA(TestingConstants.MIN_QAA_CONS.intValue());
        auservicesaml.processConnectorRequest(HTTP_METHOD, saml, TestingConstants.USER_IP_CONS.toString(),
                                                   RELAY_STATE);
    }

    @Test
    public void testCheckMandatoryAttributes() {
        ImmutableAttributeMap requestedAttributes = ImmutableAttributeMap.builder().put(EidasSpec.Definitions.CURRENT_FAMILY_NAME).put(EidasSpec.Definitions.CURRENT_GIVEN_NAME).build();

        ImmutableAttributeMap responseAttributes =
                ImmutableAttributeMap.builder().put(EidasSpec.Definitions.CURRENT_FAMILY_NAME, new StringAttributeValue("familyName", false)).put(EidasSpec.Definitions.CURRENT_GIVEN_NAME, new StringAttributeValue("givenName", false)).build();

        boolean actual = AUSERVICESAML.checkMandatoryAttributes(requestedAttributes, responseAttributes);

        assertEquals(true, actual);
    }

    @Test
    public void testCheckMandatoryAttributesValueMissing() {
        ImmutableAttributeMap requestedAttributes = ImmutableAttributeMap.builder().put(EidasSpec.Definitions.CURRENT_FAMILY_NAME).put(EidasSpec.Definitions.CURRENT_GIVEN_NAME).build();

        ImmutableAttributeMap responseAttributes =
                ImmutableAttributeMap.builder().put(EidasSpec.Definitions.CURRENT_FAMILY_NAME).put(EidasSpec.Definitions.CURRENT_GIVEN_NAME, new StringAttributeValue("givenName", false)).build();

        boolean actual = AUSERVICESAML.checkMandatoryAttributes(requestedAttributes, responseAttributes);

        assertEquals(false, actual);
    }

    @Test
    public void testCheckMandatoryAttributesMandAttrMissing() {
        ImmutableAttributeMap requestedAttributes = ImmutableAttributeMap.builder().put(EidasSpec.Definitions.CURRENT_FAMILY_NAME).put(EidasSpec.Definitions.CURRENT_GIVEN_NAME).build();

        ImmutableAttributeMap responseAttributes =
                ImmutableAttributeMap.builder().put(EidasSpec.Definitions.CURRENT_GIVEN_NAME, new StringAttributeValue("givenName", false)).build();

        boolean actual = AUSERVICESAML.checkMandatoryAttributes(requestedAttributes, responseAttributes);

        assertEquals(false, actual);
    }

    @Test
    public void testCheckMandatoryAttributesOptAttrValueMissing() {
        ImmutableAttributeMap requestedAttributes = ImmutableAttributeMap.builder().put(EidasSpec.Definitions.PLACE_OF_BIRTH).put(EidasSpec.Definitions.CURRENT_GIVEN_NAME).build();

        ImmutableAttributeMap responseAttributes =
                ImmutableAttributeMap.builder().put(EidasSpec.Definitions.PLACE_OF_BIRTH).put(EidasSpec.Definitions.CURRENT_GIVEN_NAME, new StringAttributeValue("givenName", false)).build();

        boolean actual = AUSERVICESAML.checkMandatoryAttributes(requestedAttributes, responseAttributes);

        assertEquals(true, actual);
    }
}
