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

package eu.eidas.engine.test.simple;

import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.xml.opensaml.BuilderFactoryUtil;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * The Class SimpleBaseTest. Defines a set of test the initialization of the
 * SAML engine.
 */
public class SimpleBaseTest extends TestCase {

    /**
     * Test SAML engine correct configuration name.
     */
    @Test
    public final void testSamlEngineCorrectInit() {
            Assert.assertNotNull(ProtocolEngineFactory.getDefaultProtocolEngine("CONF1"));
    }

    /**
     * Test SAML engine error configuration name.
     */
    @Test
    public final void testSamlEngineErrorNameConf() {
            Assert.assertNull(ProtocolEngineFactory.getDefaultProtocolEngine("CONF_ERROR"));
    }

    /**
     * Test SAML engine error name null.
     */
    @Test
    public final void testSamlEngineErrorNameNull() throws Exception {
        try {
            ProtocolEngineFactory.getDefaultProtocolEngine(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected){
            assertEquals("instanceName cannot be null, empty or blank", expected.getMessage());
        }
    }

    /**
     * Test SAML engine correct name configuration with spaces.
     */
    @Test
    public final void testSamlEngineErrorNameSpaces() {
            Assert.assertNotNull(ProtocolEngineFactory.getDefaultProtocolEngine("   CONF1    "));
    }

    @Test
    public final void testSamlEngineUtils() throws EIDASSAMLEngineException{
        Assert.assertNotNull(SAMLEngineUtils.encode("TEST", SAMLEngineUtils.SHA_512));
        Assert.assertNotNull(BuilderFactoryUtil.generateKeyInfo());
        Assert.assertNotNull(BuilderFactoryUtil.generateNameID());

    }
}
