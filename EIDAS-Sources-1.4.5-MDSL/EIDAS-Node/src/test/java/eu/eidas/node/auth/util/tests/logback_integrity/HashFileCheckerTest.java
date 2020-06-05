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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.node.logging.integrity.HashFileChecker;

/**
 * @author vanegdi
 */
public class HashFileCheckerTest {
    @Test
    public void testValidEntry() throws Exception{
        InputStream is = new ByteArrayInputStream(
                EidasStringUtil.getBytes("2015-03-26; 09:55:38.848 [main] INFO  eu.test.appli  - - -== SESSION : Test.getAuthenticationRequest Called, size is 0 #1# [+7ojMjYzoLMYIl8lzT7mgrI2SMSs4KLUWwcVBMquKlM=]\n"));
        Assert.assertTrue(HashFileChecker.check(is, "SHA-256"));
    } @Test
    public void testLongerValidFile() throws Exception{
        InputStream is = new ByteArrayInputStream(EidasStringUtil.getBytes("2015-03-26; 09:55:38.848 [main] INFO  eu.test.appli  - - -== SESSION : Test.getAuthenticationRequest Called, size is 0 #1# [+7ojMjYzoLMYIl8lzT7mgrI2SMSs4KLUWwcVBMquKlM=]\n" +
        "2015-03-26; 09:55:38.855 [main] INFO  eu.test.appli  - - -== SESSION : Test.getAuthenticationRequest Called, size is 0 #2# [EXCwHb/cO2R0XahMUctJVu2JMc5kKhEBK36xACWl85g=]\n" +
        "2015-03-26; 09:55:38.857 [main] WARN  eu.test.appli  - - -Session is missing or invalid #3# [1l137/ppIbm0MhasxVYD0nwAY+ZKIZ1AnQMAA4kJFd0=]\n" +
        "2015-03-26; 09:55:38.861 [main] INFO  eu.test.appli  - - -== SESSION : Test.getAuthenticationRequest Called, size is 0 #4# [7Q5VvImVlLOUfEGd2qRHUVQMs4Iv9Zce1BkM3w1q2Uo=]\n" +
        "2015-03-26; 09:55:38.864 [main] INFO  eu.test.appli  - - -== SESSION : Test.getAuthenticationRequest Called, size is 0 #5# [KG73aXnqYmxJg1/8QYjVglwg4p/WWGLzXDxG9mGLIfA=]\n"));
        Assert.assertTrue(HashFileChecker.check(is, "SHA-256"));
    }
    /**
     * The number of log entry is not consistent.
     */
    @Test()
    public void testInvalidText() throws Exception{
        InputStream is = new ByteArrayInputStream(EidasStringUtil.getBytes("2015-03-26; 09:55:38.848 [main] INFO  eu.test.appli  - - -== SESSION : Test.getAuthenticationRequest Called, size is 0 #2# [+7ojMjYzoLMYIl8lzT7mgrI2SMSs4KLUWwcVBMquKlM=]\n"));
        Assert.assertFalse(HashFileChecker.check(is, "SHA-256"));
    }
    /**
     * The hash is not consistent
     */
    @Test
    public void testInvalidHash() throws Exception{
        InputStream is = new ByteArrayInputStream(EidasStringUtil.getBytes("2015-03-26; 09:55:38.848 [main] INFO  eu.test.appli  - - -== SESSION : Test.getAuthenticationRequest Called, size is 0 #1# [+7ojMjYzM=]\n"));
        Assert.assertFalse(HashFileChecker.check(is, "SHA-256"));
    }

    @Test(expected=IllegalStateException.class)
    public void testMissingText() throws Exception{
        InputStream is = new ByteArrayInputStream(EidasStringUtil.getBytes("[+7ojMjYzM=]\n"));
        HashFileChecker.check(is, "SHA-256");
    }
    @Test(expected=IllegalStateException.class)
    public void testMissingHash() throws Exception{
        InputStream is = new ByteArrayInputStream(EidasStringUtil.getBytes("2015-03-26; 09:55:38.848 [main] INFO  eu.test.appli  - - -== SESSION : Test.getAuthenticationRequest Called, size is 0 #1#\n"));
        HashFileChecker.check(is, "SHA-256");
    }
    @Test(expected=IllegalStateException.class)
    public void testMissingTrailingHash() throws Exception{
        InputStream is = new ByteArrayInputStream(EidasStringUtil.getBytes("2015-03-26; 09:55:38.848 [main] INFO  eu.test.appli  - - -== SESSION : Test.getAuthenticationRequest Called, size is 0 #1# []\n"));
        HashFileChecker.check(is, "SHA-256");
    }
    /**
     * Takes the longerValidFile without the line #3#.
     */
    @Test
    public void testMissingEntry() throws Exception{
        InputStream is = new ByteArrayInputStream(EidasStringUtil.getBytes("2015-03-26; 09:55:38.848 [main] INFO  eu.test.appli  - - -== SESSION : Test.getAuthenticationRequest Called, size is 0 #1# [+7ojMjYzoLMYIl8lzT7mgrI2SMSs4KLUWwcVBMquKlM=]\n" +
                "2015-03-26; 09:55:38.855 [main] INFO  eu.test.appli  - - -== SESSION : Test.getAuthenticationRequest Called, size is 0 #2# [EXCwHb/cO2R0XahMUctJVu2JMc5kKhEBK36xACWl85g=]\n" +
                "2015-03-26; 09:55:38.861 [main] INFO  eu.test.appli  - - -== SESSION : Test.getAuthenticationRequest Called, size is 0 #4# [7Q5VvImVlLOUfEGd2qRHUVQMs4Iv9Zce1BkM3w1q2Uo=]\n" +
                "2015-03-26; 09:55:38.864 [main] INFO  eu.test.appli  - - -== SESSION : Test.getAuthenticationRequest Called, size is 0 #5# [KG73aXnqYmxJg1/8QYjVglwg4p/WWGLzXDxG9mGLIfA=]\n"));
        Assert.assertFalse(HashFileChecker.check(is, "SHA-256"));
    }
}