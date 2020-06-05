/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.eidas.engine.test.simple;

import org.bouncycastle.asn1.x500.X500Name;
import org.junit.Test;

import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.X500PrincipalUtil;
import eu.eidas.auth.engine.xml.opensaml.BuilderFactoryUtil;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import junit.framework.Assert;
import junit.framework.TestCase;

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
    @Test
    public final void testX509PrincipalsUtils() {
        System.out.println("*********************************************");
        X500Name test1 = new X500Name("C=AU,ST=Victoria");
        X500Name test2 = new X500Name("CN=Thawte Timestamping CA, OU=Thawte Certification, O=Thawte, L=Durbanville, ST=Western Cape, C=ZA");
        Assert.assertTrue(X500PrincipalUtil.principalNotNullEquals(test2, test2));
        Assert.assertFalse(X500PrincipalUtil.principalNotNullEquals(null, null));
        Assert.assertFalse(X500PrincipalUtil.principalNotNullEquals(test2, null));
        Assert.assertFalse(X500PrincipalUtil.principalNotNullEquals(null, test2));
        Assert.assertFalse(X500PrincipalUtil.principalNotNullEquals(test1, test2));
    }
}
