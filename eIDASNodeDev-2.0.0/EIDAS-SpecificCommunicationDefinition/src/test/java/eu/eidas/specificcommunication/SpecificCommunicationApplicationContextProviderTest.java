/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.specificcommunication;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test class for {@link SpecificCommunicationApplicationContextProvider}.
 */
public class SpecificCommunicationApplicationContextProviderTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    /**
     * Test method for
     * {@link SpecificCommunicationApplicationContextProvider#getApplicationContext()}
     * <p/>
     * Must succeed.
     */
    @Test
    public void testGetApplicationContext() throws Exception {
        final ApplicationContext applicationContext = SpecificCommunicationApplicationContextProvider.getApplicationContext();
        Assert.assertNotNull(applicationContext);
    }

    /**
     * Test method for
     * {@link SpecificCommunicationApplicationContextProvider#setApplicationContext(ApplicationContext)}
     * <p/>
     * Must succeed.
     */
    @Test
    public void testSetApplicationContext() throws Exception {
        final ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("testApplicationContext.xml");
        new SpecificCommunicationApplicationContextProvider().setApplicationContext(classPathXmlApplicationContext);
        final ApplicationContext applicationContext = SpecificCommunicationApplicationContextProvider.getApplicationContext();
        final String helloBean = (String) applicationContext.getBean("helloBean");
        Assert.assertEquals("this is a test", helloBean);
    }

    /**
     * Test method for
     * {@link SpecificCommunicationApplicationContextProvider#setApplicationContext(ApplicationContext)}
     * when trying to get a bean that is not defined.
     * <p/>
     * Must fail.
     */
    @Test
    public void testSetApplicationContextNonExistingBean() throws Exception {
        thrown.expect(NoSuchBeanDefinitionException.class);
        thrown.expectMessage("No bean named 'specificCommunicationDefinitionConnectorConfigFile' is defined");

        final ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("testApplicationContext.xml");
        new SpecificCommunicationApplicationContextProvider().setApplicationContext(classPathXmlApplicationContext);
        final ApplicationContext applicationContext = SpecificCommunicationApplicationContextProvider.getApplicationContext();
        final String nonExistingbean = (String) applicationContext.getBean("specificCommunicationDefinitionConnectorConfigFile");

        Assert.assertNull(nonExistingbean);
    }
}