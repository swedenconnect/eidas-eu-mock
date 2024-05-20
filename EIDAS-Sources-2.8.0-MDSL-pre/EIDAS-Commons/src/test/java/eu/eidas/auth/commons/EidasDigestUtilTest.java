/*
 * Copyright (c) 2021 by European Commission
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
package eu.eidas.auth.commons;

import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Random;

import static org.hamcrest.Matchers.isA;

/**
 * Test class for {@link EidasDigestUtil}
 */
public class EidasDigestUtilTest {

    private byte[] bytesToHash;
    private byte[] hashedBytes;
    private final Random random = new Random();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        bytesToHash = new byte[20];
        random.nextBytes(bytesToHash);
    }

    @After
    public void tearDown() {
        hashedBytes = null;
    }

    /**
     * Test method for
     * {@link EidasDigestUtil#hash(byte[])}
     * <p>
     * Must succeed.
     */
    @Test
    public void testHashBytes() {
        hashedBytes = EidasDigestUtil.hash(bytesToHash);

        Assert.assertNotNull(hashedBytes);
        Assert.assertNotEquals(bytesToHash, hashedBytes);
    }

    /**
     * Test method for
     * {@link EidasDigestUtil#hash(byte[], String, String)}
     * when provider string is null
     * <p>
     * Must succeed.
     */
    @Test
    public void testHashNullProvider() {
        hashedBytes = EidasDigestUtil.hash(bytesToHash, "SHA-512", null);

        Assert.assertNotNull(hashedBytes);
        Assert.assertNotEquals(bytesToHash, hashedBytes);
    }

    /**
     * Test method for
     * {@link EidasDigestUtil#hash(byte[], String, String)}
     * when provider does not exist
     * <p>
     * Must fail.
     */
    @Test
    public void testHashNoSuchProvider() {
        expectedException.expect(InternalErrorEIDASException.class);
        expectedException.expectCause(isA(NoSuchProviderException.class));

        EidasDigestUtil.hash(bytesToHash, "SHA-512", "NonExistant");
    }

    /**
     * Test method for
     * {@link EidasDigestUtil#hash(byte[], String, String)}
     * when algorithm is not valid
     * <p>
     * Must fail.
     */
    @Test
    public void testHashNoSuchAlgorithm() {
        expectedException.expect(InternalErrorEIDASException.class);
        expectedException.expectCause(isA(NoSuchAlgorithmException.class));

        EidasDigestUtil.hash(bytesToHash, "NonExistant", null);
    }

    /**
     * Test method for
     * {@link EidasDigestUtil#hashPersonalToken(byte[])}
     * when provider string is null
     * <p>
     * Must succeed.
     */
    @Test
    public void testHashPersonalToken() {
        hashedBytes = EidasDigestUtil.hashPersonalToken(bytesToHash);

        Assert.assertNotNull(hashedBytes);
        Assert.assertNotEquals(bytesToHash, hashedBytes);
    }
}