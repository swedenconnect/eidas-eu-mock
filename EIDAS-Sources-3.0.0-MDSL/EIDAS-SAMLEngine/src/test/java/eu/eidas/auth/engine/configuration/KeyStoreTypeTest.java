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

package eu.eidas.auth.engine.configuration;

import org.junit.Assert;
import org.junit.Test;

public class KeyStoreTypeTest {

    @Test
    public void isEqualTo() {
        Assert.assertTrue(KeyStoreType.valueOf("JKS").isEqualTo("JKS"));
    }

    @Test
    public void values() {
        final KeyStoreType [] expected = {KeyStoreType.PKCS12, KeyStoreType.PKCS11, KeyStoreType.JKS};
        final KeyStoreType [] arrayOfKeyStoreTypes = KeyStoreType.values();
        Assert.assertArrayEquals(expected, arrayOfKeyStoreTypes);
    }

    @Test
    public void valueOf() {
        Assert.assertEquals(KeyStoreType.JKS, KeyStoreType.valueOf("JKS"));
    }
}