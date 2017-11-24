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

package eu.eidas.node.auth.util.tests.logback_integrity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.node.logging.integrity.HashAndCounterGenerator;

/**
 * Single test cases for HASH256 output log.
 * @author vanegdi
 */
public class SHA256OutputStreamTest {
    /**
     * Test a single log generation
     */
    @Test
    public void testSingleLog() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(byteArrayOutputStream, true, "SHA-256");
        hashAndCounterGenerator.write(EidasStringUtil.getBytes("TestA\n"));
        hashAndCounterGenerator.flush();
        String result = EidasStringUtil.toString(byteArrayOutputStream.toByteArray());
        Assert.assertEquals("TestA #1# [kQYTvzcedONYovA2OkEpDihUOl1PyapPioklXt04RgE=]\n",result);
    }

    /**
     * Test a single log generation
     */
    @Test
    public void testSingleLogWithoutcounter() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(byteArrayOutputStream, false, "SHA-256");
        hashAndCounterGenerator.write(EidasStringUtil.getBytes("TestA\n"));
        hashAndCounterGenerator.flush();
        String result = EidasStringUtil.toString(byteArrayOutputStream.toByteArray());
        Assert.assertEquals("TestA [qsKaWzzzT1lv5mgcj7ySG9tN9/4S9w0/qf7meMhJ7i4=]\n",result);
    }

    /**
     * Test a single log with coherent logging line
     */
    @Test
    public void testSingleLogComplex() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(byteArrayOutputStream, true, "SHA-256");
        hashAndCounterGenerator.write(EidasStringUtil.getBytes("2015-03-26; 09:55:38.848 [main] INFO  eu.stork.peps.auth.speps.AUSPEPS  - - -== SESSION : AUSPEPS.getAuthenticationRequest Called, size is 0\n"));
        hashAndCounterGenerator.flush();
        String result = EidasStringUtil.toString(byteArrayOutputStream.toByteArray());
        Assert.assertEquals("2015-03-26; 09:55:38.848 [main] INFO  eu.stork.peps.auth.speps.AUSPEPS  - - -== SESSION : AUSPEPS.getAuthenticationRequest Called, size is 0 #1# [zddAiurv157ma1imuTAQ/l8OgA/X+hSNsS+ESqOz6Zw=]\n",result);
    }

    /**
     * Test a double line log generation with CR-LF instead of single LF
     */
    @Test
    public void testDoubleLogwithCR() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(byteArrayOutputStream, true, "SHA-256");
        hashAndCounterGenerator.write(EidasStringUtil.getBytes("TestA\r\nTestB\r\n"));
        hashAndCounterGenerator.flush();
        String result = EidasStringUtil.toString(byteArrayOutputStream.toByteArray());
        Assert.assertEquals(
                "TestA #1# [kQYTvzcedONYovA2OkEpDihUOl1PyapPioklXt04RgE=]\nTestB #2# [oGtSUycjJdFkXuOrSRV/5Hy2kE1US8SCLBMKjRv9mw8=]\n",
                result);
    }

    /**
     * Test a double line log generation with 2 different line content
     */
    @Test
    public void testDoubleLogComplex() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(byteArrayOutputStream, true, "SHA-256");
        hashAndCounterGenerator.write(EidasStringUtil.getBytes("2015-03-26; 09:55:38.848 [main] INFO  eu.stork.peps.auth.speps.AUSPEPS  - - -== SESSION : AUSPEPS.getAuthenticationRequest Called, size is 0\n" +
                "2015-03-26; 09:55:38.855 [main] INFO  eu.stork.peps.auth.speps.AUSPEPS  - - -== SESSION : AUSPEPS.getAuthenticationRequest Called, size is 0\n"));
        hashAndCounterGenerator.flush();
        String result = EidasStringUtil.toString(byteArrayOutputStream.toByteArray());
        Assert.assertEquals("2015-03-26; 09:55:38.848 [main] INFO  eu.stork.peps.auth.speps.AUSPEPS  - - -== SESSION : AUSPEPS.getAuthenticationRequest Called, size is 0 #1# [zddAiurv157ma1imuTAQ/l8OgA/X+hSNsS+ESqOz6Zw=]\n" +
                        "2015-03-26; 09:55:38.855 [main] INFO  eu.stork.peps.auth.speps.AUSPEPS  - - -== SESSION : AUSPEPS.getAuthenticationRequest Called, size is 0 #2# [4xvKn/cq10GRwnVc0CWjZ6sp+c+jlTxNtdgQhP1BMnY=]\n",
                result);
    }


    @Test
    public void testMultipleLog() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        HashAndCounterGenerator hashAndCounterGenerator = new HashAndCounterGenerator(byteArrayOutputStream, true, "SHA-256");
        hashAndCounterGenerator.write(EidasStringUtil.getBytes("TestA\nTestB\nTestC\n"));
        hashAndCounterGenerator.flush();
        String result = EidasStringUtil.toString(byteArrayOutputStream.toByteArray());
        Assert.assertEquals(
                "TestA #1# [kQYTvzcedONYovA2OkEpDihUOl1PyapPioklXt04RgE=]\nTestB #2# [oGtSUycjJdFkXuOrSRV/5Hy2kE1US8SCLBMKjRv9mw8=]\nTestC #3# [+PUueHVsyR46ZU6CbNmMfClsVSwD+0Jjw9z++hqWzao=]\n",
                result);
    }
}
