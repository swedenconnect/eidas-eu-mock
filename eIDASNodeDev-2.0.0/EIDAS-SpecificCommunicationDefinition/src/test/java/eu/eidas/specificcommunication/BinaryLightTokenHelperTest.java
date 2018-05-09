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

package eu.eidas.specificcommunication;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.auth.commons.light.ILightToken;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.auth.commons.tx.LightTokenEncoder;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link BinaryLightTokenHelper}.
 */
public class BinaryLightTokenHelperTest {

    private final String VALID_BINARY_LIGHT_TOKEN_REQUEST_BASE64 = "c3BlY2lmaWNDb21tdW5pY2F0aW9uRGVmaW5pdGlvbkNvbm5lY3RvclJlcXVlc3R8ODUyYTY0YzAtOGFjMS00NDVmLWIwZTEtOTkyYWRhNDkzMDMzfDIwMTctMTItMTEgMTQ6MTI6MDUgMTQ4fDdNOHArdVA4Q0tYdU1pMklxU2RhMXRnNDUyV2xSdmNPU3d1MGRjaXNTWUU9";

    private final String LIGHTTOKEN_CONNECTOR_REQUEST_SECRET = "mySecretConnectorRequest";

    private final String LIGHTTOKEN_CONNECTOR_REQUEST_ALGORITHM = "SHA-256";

    private final String LIGHTTOKEN_CONNECTOR_REQUEST_ISSUER_NAME = "specificCommunicationDefinitionConnectorRequest";

    private final String LIGHTTOKEN_CONNECTOR_RESPONSE_SECRET = "mySecretConnectorResponse";

    private final String INVALID_BINARY_LIGHT_TOKEN = "notAValidToken";

    private final String INVALID_BASE64_TOKEN = "{}notAToken";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test method for
     * {@link BinaryLightTokenHelper#getBinaryLightTokenId(String, String, String)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetBinaryLightTokenId() throws Exception {
        final String binaryLightTokenIdActual = BinaryLightTokenHelper
                .getBinaryLightTokenId(
                        VALID_BINARY_LIGHT_TOKEN_REQUEST_BASE64,
                        LIGHTTOKEN_CONNECTOR_REQUEST_SECRET,
                        LIGHTTOKEN_CONNECTOR_REQUEST_ALGORITHM);

        final String binaryLightTokenIdExpected = "852a64c0-8ac1-445f-b0e1-992ada493033";
        Assert.assertEquals(binaryLightTokenIdExpected, binaryLightTokenIdActual);
    }

    /**
     * Test method for
     * {@link BinaryLightTokenHelper#getBinaryLightTokenId(String, String, String)}
     * when using an invalid {@link BinaryLightToken} in Base64.
     * <p>
     * Must fail.
     */
    @Test
    public void testGetBinaryLightTokenIdInvalidToken() throws Exception {
        thrown.expect(SecurityEIDASException.class);
        thrown.expectMessage("LightToken parse error");

        BinaryLightTokenHelper.getBinaryLightTokenId(
                INVALID_BINARY_LIGHT_TOKEN,
                LIGHTTOKEN_CONNECTOR_REQUEST_SECRET,
                LIGHTTOKEN_CONNECTOR_REQUEST_ALGORITHM);
    }

    /**
     * Test method for
     * {@link BinaryLightTokenHelper#getBinaryLightTokenId(String, String, String)}
     * when using an invalid {@link BinaryLightToken} with not valid Base64 characters.
     * <p>
     * Must fail.
     */
    @Test
    public void testGetBinaryLightTokenIdInvalidBase64Token() throws Exception {
        thrown.expect(SecurityEIDASException.class);
        thrown.expectMessage("LightToken parse error");

        BinaryLightTokenHelper.getBinaryLightTokenId(
                INVALID_BASE64_TOKEN,
                LIGHTTOKEN_CONNECTOR_REQUEST_SECRET,
                LIGHTTOKEN_CONNECTOR_REQUEST_ALGORITHM);
    }

    /**
     * Test method for
     * {@link BinaryLightTokenHelper#getBinaryLightTokenId(String, String, String)}
     * when using a valid {@link BinaryLightToken} but using the wrong secret.
     * <p>
     * Must fail.
     */
    @Test
    public void testGetBinaryLightTokenIdWrongSecret() throws Exception {
        thrown.expect(SecurityEIDASException.class);
        thrown.expectMessage("LightToken digest failure");

        BinaryLightTokenHelper.getBinaryLightTokenId(
                VALID_BINARY_LIGHT_TOKEN_REQUEST_BASE64,
                LIGHTTOKEN_CONNECTOR_RESPONSE_SECRET,
                LIGHTTOKEN_CONNECTOR_REQUEST_ALGORITHM);
    }

    /**
     * Test method for
     * {@link BinaryLightTokenHelper#getBinaryLightTokenId(String, String, String)}
     * when using a valid {@link BinaryLightToken} but using an invalid algorithm.
     * <p>
     * Must fail.
     */
    @Test
    public void testGetBinaryLightTokenIdInvalidAlgorithm() throws Exception {
        thrown.expect(SpecificCommunicationException.class);
        thrown.expectCause(IsInstanceOf.<Throwable>instanceOf(NoSuchAlgorithmException.class));
        thrown.expectMessage("invalidAlgorithm MessageDigest not available");

        BinaryLightTokenHelper.getBinaryLightTokenId(
                VALID_BINARY_LIGHT_TOKEN_REQUEST_BASE64,
                LIGHTTOKEN_CONNECTOR_REQUEST_SECRET,
                "invalidAlgorithm");
    }

    /**
     * Test method for
     * {@link BinaryLightTokenHelper#getBinaryToken(HttpServletRequest, String)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetBinaryToken() throws Exception {
        final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        final String param = "binaryLightToken";
        when(mockHttpServletRequest.getParameter(param)).thenReturn(VALID_BINARY_LIGHT_TOKEN_REQUEST_BASE64);
        final String binaryTokenActual = BinaryLightTokenHelper.getBinaryToken(mockHttpServletRequest, param);

        Assert.assertSame(VALID_BINARY_LIGHT_TOKEN_REQUEST_BASE64, binaryTokenActual);
    }

    /**
     * Test method for
     * {@link BinaryLightTokenHelper#getBinaryToken(HttpServletRequest, String)}
     * when using the wrong parameter key
     * <p>
     * Must fail.
     */
    @Test
    public void testGetBinaryTokenInvalidParam() throws Exception {
        final HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        final String param = "binaryLightToken";
        when(mockHttpServletRequest.getParameter(param)).thenReturn(VALID_BINARY_LIGHT_TOKEN_REQUEST_BASE64);
        final String binaryTokenActual = BinaryLightTokenHelper.getBinaryToken(mockHttpServletRequest, "wrongParameterKey");

        Assert.assertNull(binaryTokenActual);
    }

    /**
     * Test method for
     * {@link BinaryLightTokenHelper#encodeBinaryLightTokenBase64(BinaryLightToken)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testEncodeBinaryLightTokenBase64() throws Exception {
        final BinaryLightToken binaryLightTokenExpected = BinaryLightTokenHelper
                .createBinaryLightToken(LIGHTTOKEN_CONNECTOR_REQUEST_ISSUER_NAME, LIGHTTOKEN_CONNECTOR_REQUEST_SECRET, LIGHTTOKEN_CONNECTOR_REQUEST_ALGORITHM);
        final ILightToken iLightTokenExpected = binaryLightTokenExpected.getToken();

        final String tokenBase64 = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightTokenExpected);

        final String binaryLightTokenString = EidasStringUtil.decodeStringFromBase64(tokenBase64);
        BinaryLightToken binaryLightTokenActual = LightTokenEncoder
                .decode(binaryLightTokenString.getBytes(), LIGHTTOKEN_CONNECTOR_REQUEST_SECRET, LIGHTTOKEN_CONNECTOR_REQUEST_ALGORITHM);
        final ILightToken iLightTokenActual = binaryLightTokenActual.getToken();

        Assert.assertEquals(iLightTokenExpected, iLightTokenActual);
    }

    /**
     * Test method for
     * {@link BinaryLightTokenHelper#encodeBinaryLightTokenBase64(BinaryLightToken)}
     * when using a null binaryLightToken parameter.
     * <p>
     * Must fail.
     */
    @Test
    public void testEncodeBinaryLightTokenBase64NullBinaryLightToken() throws Exception {
        thrown.expect(NullPointerException.class);

        BinaryLightTokenHelper.encodeBinaryLightTokenBase64(null);
    }

    /**
     * Test method for
     * {@link BinaryLightTokenHelper#createBinaryLightToken(String, String, String)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testCreateBinaryLightToken() throws Exception {
        final BinaryLightToken binaryLightToken = BinaryLightTokenHelper
                .createBinaryLightToken(LIGHTTOKEN_CONNECTOR_REQUEST_ISSUER_NAME, LIGHTTOKEN_CONNECTOR_REQUEST_SECRET, LIGHTTOKEN_CONNECTOR_REQUEST_ALGORITHM);

        Assert.assertNotNull(binaryLightToken);
        Assert.assertEquals(LIGHTTOKEN_CONNECTOR_REQUEST_ISSUER_NAME, binaryLightToken.getToken().getIssuer());
    }

    /**
     * Test method for
     * {@link BinaryLightTokenHelper#createBinaryLightToken(String, String, String)}
     * when issuerName is null.
     * <p>
     * Must fail.
     */
    @Test
    public void testCreateBinaryLightTokenNullIssuer() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("issuer cannot be null, empty or blank");

        BinaryLightTokenHelper.createBinaryLightToken(null, LIGHTTOKEN_CONNECTOR_REQUEST_SECRET, LIGHTTOKEN_CONNECTOR_REQUEST_ALGORITHM);
    }

    /**
     * Test method for
     * {@link BinaryLightTokenHelper#createBinaryLightToken(String, String, String)}
     * when algorithm is invalid.
     * <p>
     * Must fail.
     */
    @Test
    public void testCreateBinaryLightTokenInvalidAlgorithm() throws Exception {
        thrown.expect(SpecificCommunicationException.class);
        thrown.expectCause(IsInstanceOf.<Throwable>instanceOf(NoSuchAlgorithmException.class));
        thrown.expectMessage("java.security.NoSuchAlgorithmException: invalidAlgorithm MessageDigest not available");

        BinaryLightTokenHelper.createBinaryLightToken(LIGHTTOKEN_CONNECTOR_REQUEST_ISSUER_NAME, LIGHTTOKEN_CONNECTOR_REQUEST_SECRET, "invalidAlgorithm");
    }
}