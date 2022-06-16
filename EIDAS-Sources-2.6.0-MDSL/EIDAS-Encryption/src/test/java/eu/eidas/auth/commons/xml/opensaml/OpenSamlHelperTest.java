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

package eu.eidas.auth.commons.xml.opensaml;

import eu.eidas.encryption.exception.UnmarshallException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.core.config.Configuration;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;

/**
 * Test class for {@link OpenSamlHelper}
 */
@RunWith(MockitoJUnitRunner.class)
public class OpenSamlHelperTest {

    private Configuration defaultConfiguration;
    private Configuration spyConfiguration;

    @Mock
    private Document mockDocument;

    @Mock
    private Element mockElement;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        OpenSamlHelper.getSchema();

        final Method getConfiguration = ConfigurationService.class.getDeclaredMethod("getConfiguration");
        getConfiguration.setAccessible(true);

        defaultConfiguration = (Configuration) getConfiguration.invoke(null);
        spyConfiguration = Mockito.spy(defaultConfiguration);

        ConfigurationService.setConfiguration(spyConfiguration);
    }

    @After
    public void setConfiguration() {
        if (null != defaultConfiguration) {
            ConfigurationService.setConfiguration(defaultConfiguration);
        }
    }

    /**
     * Test method for
     * {@link OpenSamlHelper#unmarshallFromDom(Document)}
     * when DOM Document is null
     * <p>
     * Must fail.
     */
    @Test
    public void testUnmarshallFromDomWithDocumentNull() throws UnmarshallException {
        expectedException.expect(UnmarshallException.class);

        OpenSamlHelper.unmarshallFromDom(null);
    }

    /**
     * Test method for
     * {@link OpenSamlHelper#unmarshallFromDom(Document)}
     * when root element is null
     * <p>
     * Must fail.
     */
    @Test
    public void testUnmarshallFromDomWithRootNull() throws UnmarshallException {
        expectedException.expect(UnmarshallException.class);

        Mockito.when(mockDocument.getDocumentElement()).thenReturn(null);

        OpenSamlHelper.unmarshallFromDom(mockDocument);
    }

    /**
     * Test method for
     * {@link OpenSamlHelper#unmarshallFromDom(Document)}
     * when UnmarshallerFactory is null
     * <p>
     * Must fail.
     */
    @Test
    public void testUnmarshallFromDomWithUnmarshallerFactoryNull() throws UnmarshallException {
        expectedException.expect(UnmarshallException.class);

        final XMLObjectProviderRegistry spyXMLObjectProviderRegistry = getXmlObjectProviderRegistry();

        Mockito.when(mockDocument.getDocumentElement()).thenReturn(mockElement);
        Mockito.when(spyXMLObjectProviderRegistry.getUnmarshallerFactory()).thenReturn(null);
        Mockito.when(spyConfiguration.get(XMLObjectProviderRegistry.class, "default")).thenReturn(spyXMLObjectProviderRegistry);

        OpenSamlHelper.unmarshallFromDom(mockDocument);
    }

    /**
     * Test method for
     * {@link OpenSamlHelper#unmarshallFromDom(Document)}
     * when Unmarshaller is null
     * <p>
     * Must fail.
     */
    @Test
    public void testUnmarshallFromDomWithUnmarshallerNull() throws UnmarshallException {
        expectedException.expect(UnmarshallException.class);

        final UnmarshallerFactory mockUnmarshallerFactory = Mockito.mock(UnmarshallerFactory.class);
        final XMLObjectProviderRegistry spyXMLObjectProviderRegistry = getXmlObjectProviderRegistry();

        Mockito.when(mockDocument.getDocumentElement()).thenReturn(mockElement);
        Mockito.when(spyXMLObjectProviderRegistry.getUnmarshallerFactory()).thenReturn(mockUnmarshallerFactory);
        Mockito.when(spyConfiguration.get(XMLObjectProviderRegistry.class, "default")).thenReturn(spyXMLObjectProviderRegistry);
        Mockito.when(mockUnmarshallerFactory.getUnmarshaller(any(Element.class))).thenReturn(null);

        OpenSamlHelper.unmarshallFromDom(mockDocument);
    }

    /**
     * Test method for
     * {@link OpenSamlHelper#unmarshallFromDom(Document)}
     * when {@link Unmarshaller#unmarshall(Element)} throws {@link UnmarshallException}
     * <p>
     * Must fail.
     */
    @Test
    public void testUnmarshallFromDomWhenExceptionIsThrown() throws UnmarshallException, UnmarshallingException {
        expectedException.expect(UnmarshallException.class);

        final Unmarshaller mockUnmarshaller = Mockito.mock(Unmarshaller.class);
        final UnmarshallerFactory mockUnmarshallerFactory = Mockito.mock(UnmarshallerFactory.class);
        final XMLObjectProviderRegistry spyXMLObjectProviderRegistry = getXmlObjectProviderRegistry();

        Mockito.when(mockDocument.getDocumentElement()).thenReturn(mockElement);
        Mockito.when(spyXMLObjectProviderRegistry.getUnmarshallerFactory()).thenReturn(mockUnmarshallerFactory);
        Mockito.when(spyConfiguration.get(XMLObjectProviderRegistry.class, "default")).thenReturn(spyXMLObjectProviderRegistry);
        Mockito.when(mockUnmarshallerFactory.getUnmarshaller(any(Element.class))).thenReturn(mockUnmarshaller);
        Mockito.when(mockUnmarshaller.unmarshall(any())).thenThrow(UnmarshallingException.class);

        OpenSamlHelper.unmarshallFromDom(mockDocument);
    }

    private XMLObjectProviderRegistry getXmlObjectProviderRegistry() {
        final XMLObjectProviderRegistry xmlObjectProviderRegistry = defaultConfiguration.get(XMLObjectProviderRegistry.class, "default");

        final XMLObjectProviderRegistry spyXMLObjectProviderRegistry = Mockito.spy(xmlObjectProviderRegistry);

        return spyXMLObjectProviderRegistry;
    }

}