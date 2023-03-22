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

package eu.eidas.auth.engine.xml.opensaml;

import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.saml.saml2.metadata.Organization;

import javax.xml.namespace.QName;

/**
 * Test class for {@link BuilderFactoryUtil}.
 */
public class BuilderFactoryUtilTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setup() throws InitializationException {
        if (null == ConfigurationService.get(XMLObjectProviderRegistry.class)) {
            InitializationService.initialize();
        }
    }

    /**
     * Test method for
     * {@link BuilderFactoryUtil#buildXmlObject(QName)}
     * when builder is null
     * <p>
     * Must fail.
     */
    @Test
    public void testBuildXmlObjectWithBuilderNull() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        QName qName = new QName("namespaceURI", "localPart");
        BuilderFactoryUtil.buildXmlObject(qName);
    }

    /**
     * Test method for
     * {@link BuilderFactoryUtil#buildXmlObject(Class)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testBuildXmlObjectWithClassParameter() throws IllegalAccessException, NoSuchFieldException {
        BuilderFactoryUtil.buildXmlObject(Organization.class);
    }

    /**
     * Test method for
     * {@link BuilderFactoryUtil#buildXmlObject(QName, QName)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testBuildXmlObjectWithTwoQnameParameters() {
        final QName qName = new QName("urn:oasis:names:tc:SAML:2.0:metadata", "AttributeProfile");
        final QName qName1 = new QName("urn:oasis:names:tc:SAML:2.0:protocol", "IDPEntry");

        BuilderFactoryUtil.buildXmlObject(qName, qName1);
    }
}