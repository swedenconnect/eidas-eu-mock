/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 *  EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the
 * "NOTICE" text file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution.
 * Any derivative works that you distribute must include a readable
 * copy of the "NOTICE" text file.
 */
package eu.eidas.auth.commons.tx;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.auth.commons.light.impl.AbstractLightToken;
import eu.eidas.auth.commons.light.impl.LightToken;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.Assert.*;


public class LightTokenEncoderTest {

    private static final String ALGORITHM = "SHA-256";

    private static final String ID = "f5e7e0f5-b9b8-4256-a7d0-4090141b326d";

    private static final String ISSUER = "MYSPECIFIC";

    private static DateTime TIMESTAMP = AbstractLightToken.LIGHTTOKEN_DATE_FORMAT.parseDateTime("1956-10-23 10:52:01 698");

    private static String SECRET = "MYSECRET";

    private static String TOKEN = new StringBuilder()
            .append("MYSPECIFIC")
            .append(AbstractLightToken.SEPARATOR)
            .append("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
            .append(AbstractLightToken.SEPARATOR)
            .append("1956-10-23 10:52:01 698")
            .append(AbstractLightToken.SEPARATOR)
            .append("1040jxHfWeTgn98YczCpon3m+zOHdVXgYkdOjyKVaJA=")
            .toString();

    private static String TOKEN_BADDATE = new StringBuilder()
            .append("MYSPECIFIC")
            .append(AbstractLightToken.SEPARATOR)
            .append("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
            .append(AbstractLightToken.SEPARATOR)
            .append("1956-10-23 29:52:01 698")
            .append(AbstractLightToken.SEPARATOR)
            .append("1040jxHfWeTgn98YczCpon3m+zOHdVXgYkdOjyKVaJA=")
            .toString();


    private static String TOKEN_MANYPARTS = new StringBuilder()
            .append("MYSPECIFIC")
            .append(AbstractLightToken.SEPARATOR)
            .append("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
            .append(AbstractLightToken.SEPARATOR)
            .append("1956-10-23 29:52:01 698")
            .append(AbstractLightToken.SEPARATOR)
            .append(AbstractLightToken.SEPARATOR)
            .append("*")
            .append(AbstractLightToken.SEPARATOR)
            .append("*")
            .append(AbstractLightToken.SEPARATOR)
            .append(AbstractLightToken.SEPARATOR)
            .append("ayvs5qu1I+s+aNGRN5I")
            .append(AbstractLightToken.SEPARATOR)
            .append("TNhCZGh9hBnXUCsnqCrPXlU=")
            .toString();


    @SuppressWarnings({"PublicField"})
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testEncode() throws NoSuchAlgorithmException {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        BinaryLightToken binaryToken = LightTokenEncoder.encode(lightToken, SECRET, ALGORITHM);

        assertNotNull(binaryToken);
        assertEquals(binaryToken.getToken(), lightToken);
        assertArrayEquals(binaryToken.getTokenBytes(), TOKEN.getBytes());
    }

    @Test
    public void testDecode() throws NoSuchAlgorithmException {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        BinaryLightToken binaryToken = LightTokenEncoder.decode(TOKEN.getBytes(), SECRET, ALGORITHM);

        assertNotNull(binaryToken);
        assertArrayEquals(binaryToken.getTokenBytes(), TOKEN.getBytes());
        assertTrue(binaryToken.getToken().equals(lightToken));
    }

    @Test
    public void testDecodeBase64() throws NoSuchAlgorithmException {
        LightToken lightToken = new LightToken.Builder().id(ID)
                .issuer(ISSUER)
                .createdOn(TIMESTAMP)
                .build();
        BinaryLightToken binaryToken = LightTokenEncoder.decodeBase64(EidasStringUtil.encodeToBase64(TOKEN).getBytes(), SECRET, ALGORITHM);

        assertNotNull(binaryToken);
        assertArrayEquals(binaryToken.getTokenBytes(), TOKEN.getBytes());
        assertTrue(binaryToken.getToken().equals(lightToken));
    }

    @Test
    public void testDecodeFailsOnBadSecret() throws NoSuchAlgorithmException {
        thrown.expect(SecurityEIDASException.class);
        thrown.expectMessage("LightToken digest failure");
        BinaryLightToken binaryToken = LightTokenEncoder.decode(TOKEN.getBytes(), SECRET + "2", ALGORITHM);
    }

    @Test
    public void testDecodeFailsOnLongToken() throws NoSuchAlgorithmException {
        thrown.expect(SecurityEIDASException.class);
        thrown.expectMessage("Error parsing LightToken, size exceeds " + LightTokenEncoder.MAX_TOKEN_SIZE);
        byte[] longToken = new byte[LightTokenEncoder.MAX_TOKEN_SIZE+1];
        Arrays.fill(longToken, (byte) 'A');
        BinaryLightToken binaryToken = LightTokenEncoder.decodeBase64(longToken, SECRET, ALGORITHM);
    }

    @Test
    public void testDecodeFailsDateProblem() throws NoSuchAlgorithmException {
        thrown.expect(SecurityEIDASException.class);
        thrown.expectMessage("LightToken createdOn timestamp parse failure");
        BinaryLightToken binaryToken = LightTokenEncoder.decode(TOKEN_BADDATE.getBytes(), SECRET, ALGORITHM);
    }

    @Test
    public void testDecodeFailsGeneral() throws NoSuchAlgorithmException {
        thrown.expect(SecurityEIDASException.class);
        thrown.expectMessage("LightToken parse error");
        BinaryLightToken binaryToken = LightTokenEncoder.decode(TOKEN_MANYPARTS.getBytes(), SECRET, ALGORITHM);
    }

}
