/*
 * Copyright (c) 2022 by European Commission
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

package eu.eidas.encryption.config;

import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.signature.XMLSignature;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link EidasJCENameConfiguration}
 */
public class EidasJCENameConfigurationTest {

    /**
     * Test method for
     * {@link EidasJCENameConfiguration#setJCENameConfiguration()}
     * Configuration is set
     * <p>
     * Must succeed.
     */
    @Test
    public void setJCENameConfiguration() {
        EidasJCENameConfiguration.setJCENameConfiguration();

        Assert.assertEquals("RSASSA-PSS", JCEMapper.translateURItoJCEID(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1));
        Assert.assertEquals("RSASSA-PSS", JCEMapper.translateURItoJCEID(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384_MGF1));
        Assert.assertEquals("RSASSA-PSS", JCEMapper.translateURItoJCEID(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1));
    }
}