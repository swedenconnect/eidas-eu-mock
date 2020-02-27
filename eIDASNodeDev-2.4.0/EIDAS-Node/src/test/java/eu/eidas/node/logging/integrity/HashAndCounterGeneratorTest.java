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
 * limitations under the Licence.
 *
 */

package eu.eidas.node.logging.integrity;

import eu.eidas.auth.commons.EidasStringUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Test class for {@link HashAndCounterGenerator}
 * Single test cases for HASH256 output log.
 */
public class HashAndCounterGeneratorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for {@link HashAndCounterGenerator#getModifiedLoggedBytes(byte[])}
     * Test a single log generation
     * <br>
     * Must succeed.
     */
    @Test
    public void testSingleLog() {
        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(true, "SHA-256");

        byte[] originalLogLineBytes = EidasStringUtil.getBytes("TestA\n");
        String result = EidasStringUtil.toString(hashAndCounterGenerator.getModifiedLoggedBytes(originalLogLineBytes));

        assertEquals("TestA #1# [kQYTvzcedONYovA2OkEpDihUOl1PyapPioklXt04RgE=]\n", result);
    }

    /**
     * Test method for {@link HashAndCounterGenerator#getModifiedLoggedBytes(byte[])}
     * Test a single log generation, without counter.
     * <br>
     * Must succeed.
     */
    @Test
    public void testSingleLogWithoutcounter() {
        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(false, "SHA-256");

        byte[] originalLogLineBytes = EidasStringUtil.getBytes("TestA\n");
        String result = EidasStringUtil.toString(hashAndCounterGenerator.getModifiedLoggedBytes(originalLogLineBytes));

        assertEquals("TestA [qsKaWzzzT1lv5mgcj7ySG9tN9/4S9w0/qf7meMhJ7i4=]\n", result);
    }

    /**
     * Test method for {@link HashAndCounterGenerator#getModifiedLoggedBytes(byte[])}
     * Test a single log with coherent logging line.
     * <br>
     * Must succeed.
     */
    @Test
    public void testSingleLogComplex() {
        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(true, "SHA-256");

        byte[] originalLogLineBytes = EidasStringUtil.getBytes("2015-03-26; 09:55:38.848 [main] INFO  eu.stork.peps.auth.speps.AUSPEPS  - - -== SESSION : AUSPEPS.getAuthenticationRequest Called, size is 0\n");
        String result = EidasStringUtil.toString(hashAndCounterGenerator.getModifiedLoggedBytes(originalLogLineBytes));

        assertEquals("2015-03-26; 09:55:38.848 [main] INFO  eu.stork.peps.auth.speps.AUSPEPS  - - -== SESSION : AUSPEPS.getAuthenticationRequest Called, size is 0 #1# [zddAiurv157ma1imuTAQ/l8OgA/X+hSNsS+ESqOz6Zw=]\n", result);
    }

    /**
     * Test method for {@link HashAndCounterGenerator#getModifiedLoggedBytes(byte[])}
     * Test a double line log generation with CR-LF instead of single LF.
     * <br>
     * Must succeed.
     */
    @Test
    public void testDoubleLogwithCR() {
        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(true, "SHA-256");

        byte[] originalLogLineBytes = EidasStringUtil.getBytes("TestA\r\nTestB\r\n");
        String result = EidasStringUtil.toString(hashAndCounterGenerator.getModifiedLoggedBytes(originalLogLineBytes));

        assertEquals("TestA #1# [kQYTvzcedONYovA2OkEpDihUOl1PyapPioklXt04RgE=]\nTestB #2# [oGtSUycjJdFkXuOrSRV/5Hy2kE1US8SCLBMKjRv9mw8=]\n", result);
    }

    /**
     * Test method for {@link HashAndCounterGenerator#getModifiedLoggedBytes(byte[])}
     * Test a double line log generation with 2 different line content.
     * <br>
     * Must succeed.
     */
    @Test
    public void testDoubleLogComplex() {
        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(true, "SHA-256");

        byte[] originalLogLineBytes = EidasStringUtil.getBytes("2015-03-26; 09:55:38.848 [main] INFO  eu.stork.peps.auth.speps.AUSPEPS  - - -== SESSION : AUSPEPS.getAuthenticationRequest Called, size is 0\n" +
                "2015-03-26; 09:55:38.855 [main] INFO  eu.stork.peps.auth.speps.AUSPEPS  - - -== SESSION : AUSPEPS.getAuthenticationRequest Called, size is 0\n");
        String result = EidasStringUtil.toString(hashAndCounterGenerator.getModifiedLoggedBytes(originalLogLineBytes));

        assertEquals("2015-03-26; 09:55:38.848 [main] INFO  eu.stork.peps.auth.speps.AUSPEPS  - - -== SESSION : AUSPEPS.getAuthenticationRequest Called, size is 0 #1# [zddAiurv157ma1imuTAQ/l8OgA/X+hSNsS+ESqOz6Zw=]\n" +
                "2015-03-26; 09:55:38.855 [main] INFO  eu.stork.peps.auth.speps.AUSPEPS  - - -== SESSION : AUSPEPS.getAuthenticationRequest Called, size is 0 #2# [4xvKn/cq10GRwnVc0CWjZ6sp+c+jlTxNtdgQhP1BMnY=]\n", result);
    }

    /**
     * Test method for {@link HashAndCounterGenerator#getModifiedLoggedBytes(byte[])}
     * Test a multiple line log.
     * <br>
     * Must succeed.
     */
    @Test
    public void testMultipleLog() {
        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(true, "SHA-256");

        byte[] originalLoggedBytes = EidasStringUtil.getBytes("TestA\nTestB\nTestC\n");
        String result = EidasStringUtil.toString(hashAndCounterGenerator.getModifiedLoggedBytes(originalLoggedBytes));

        assertEquals("TestA #1# [kQYTvzcedONYovA2OkEpDihUOl1PyapPioklXt04RgE=]\nTestB #2# [oGtSUycjJdFkXuOrSRV/5Hy2kE1US8SCLBMKjRv9mw8=]\nTestC #3# [+PUueHVsyR46ZU6CbNmMfClsVSwD+0Jjw9z++hqWzao=]\n", result);
    }

    /**
     * Test method for {@link HashAndCounterGenerator}
     * Test an exception is (re)thrown when an attempt is made to use an unknown algorithm for the MessageDigest.
     * <br>
     * Must fail and throw {@link RuntimeException}
     */
    @Test
    public void testExceptionThrownOnUnknownAlgorithm() {
        expectedException.expect(RuntimeException.class);
        new HashAndCounterGenerator(true, "unknown");
    }

    /**
     * Test method for {@link HashAndCounterGenerator}
     * Test an exception is (re)thrown when an internal IOException occurs.
     * <br>
     * Must fail and throw {@link RuntimeException}
     */
    @Test
    public void testExceptionThrownOnIOException() throws IOException {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Message to check");

        final byte NEWLINE = '\n';

        ByteArrayOutputStream byteArrayOutputStreamMock = Mockito.mock(ByteArrayOutputStream.class);

        doThrow(new IOException("Message to check")).when(byteArrayOutputStreamMock).write(any());

        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(true,
                "SHA-256", byteArrayOutputStreamMock);

        hashAndCounterGenerator.getModifiedLoggedBytes(new byte[]{NEWLINE, 'y'});
    }

    /**
     * Test method for {@link HashAndCounterGenerator#getModifiedLoggedBytes(byte[])}
     * Test the outputstream is always reset.
     * <br>
     * Must succeed.
     */
    @Test
    public void testOutputStreamReset() {

        ByteArrayOutputStream byteArrayOutputStreamMock = Mockito.mock(ByteArrayOutputStream.class);

        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(true,
                "SHA-256", byteArrayOutputStreamMock);
        hashAndCounterGenerator.getModifiedLoggedBytes(new byte[]{'x', 'y'});

        verify(byteArrayOutputStreamMock).reset();
    }

}