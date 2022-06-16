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

package eu.eidas.encryption.support;


import org.junit.Assert;
import org.junit.Test;
import org.opensaml.xmlsec.encryption.EncryptedData;
import org.opensaml.xmlsec.encryption.EncryptedKey;
import org.opensaml.xmlsec.encryption.impl.EncryptedDataBuilder;
import org.opensaml.xmlsec.encryption.impl.EncryptedKeyBuilder;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.impl.KeyInfoBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 *  Test class for {@link FirstInlineEncryptedKeyResolver}
 */
public class FirstInlineEncryptedKeyResolverTest {

    /**
     * Test for  {@link FirstInlineEncryptedKeyResolver#resolve(EncryptedData)}
     * when EncryptedData contains multiples EncryptedKey
     * Should only get first one.
     * <p>
     * Must succeed.
     */
    @Test
    public void testResolve() {
        EncryptedDataBuilder encryptedDataBuilder = new EncryptedDataBuilder();
        EncryptedData encryptedData = encryptedDataBuilder.buildObject();

        EncryptedKeyBuilder encryptedKeyBuilder = new EncryptedKeyBuilder();
        EncryptedKey firstEncryptedKey = encryptedKeyBuilder.buildObject();
        EncryptedKey secondEncryptedKey = encryptedKeyBuilder.buildObject();

        KeyInfoBuilder keyInfoBuilder = new KeyInfoBuilder();
        KeyInfo keyInfo = keyInfoBuilder.buildObject();
        keyInfo.getEncryptedKeys().add(firstEncryptedKey);
        keyInfo.getEncryptedKeys().add(secondEncryptedKey);
        encryptedData.setKeyInfo(keyInfo);

        FirstInlineEncryptedKeyResolver resolver = new FirstInlineEncryptedKeyResolver();
        List<EncryptedKey> encryptedKeys = new ArrayList<>();

        resolver.resolve(encryptedData).forEach(encKey -> encryptedKeys.add(encKey));

        Assert.assertEquals(1, encryptedKeys.size());
        EncryptedKey resultKey = encryptedKeys.get(0);
        Assert.assertEquals(firstEncryptedKey, resultKey);
        Assert.assertNotEquals(secondEncryptedKey, resultKey);
    }
}
