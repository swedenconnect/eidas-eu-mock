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

package eu.eidas.config;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.eidas.config.impl.CategoryListImpl;
import eu.eidas.config.impl.EIDASNodeMetaconfigHolderImpl;
import eu.eidas.config.impl.EIDASNodeMetaconfigListImpl;
import eu.eidas.config.impl.EIDASNodeMetaconfigProviderImpl;
import eu.eidas.config.impl.marshaller.EIDASMetadataUnmarshallerImpl;
import eu.eidas.config.node.EIDASNodeMetaconfigProvider;
import eu.eidas.config.node.EIDASNodeParameterMeta;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/testcontext.xml")
@FixMethodOrder(MethodSorters.JVM)
public class TestEidasNodeMetadata {
    String TEST_CONTENT="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<NODEMetadata>\n" +
    "    <categories>\n" +
    "        <category name=\"service\">ProxyService parameters</category>\n" +
    "        <category name=\"connector\">Connector parameters</category>\n" +
    "    </categories>\n" +
    "    <parameters>\n" +
    "    </parameters>\n" +
    "</NODEMetadata>";
    final static String TEST_CATEGORY="parameter.category.label.administer.service";


    @Test
    public void testDeserialize(){
        EIDASMetadataUnmarshallerImpl eiui=new EIDASMetadataUnmarshallerImpl();
        EIDASNodeMetaconfigHolderImpl holder = eiui.readNodeMetadataFromString(TEST_CONTENT);
        assertNotNull(holder);
        CategoryListImpl categories = holder.getCategoryList();
        EIDASNodeMetaconfigListImpl metadataList=holder.getNodeMetadataList();
        assertNotNull(categories);
        assertNotNull(categories.getCategories());
        assertFalse(categories.getCategories().isEmpty());
        assertTrue(categories.getCategories().size() == 2);
        assertNotNull(metadataList);
    }

    @Test
    public void testNodeMetadataProvider(){
        EIDASNodeMetaconfigProviderImpl provider = new EIDASNodeMetaconfigProviderImpl();
        assertNotNull(provider.getCategories());
        assertFalse(provider.getCategories().isEmpty());
        assertFalse(provider.getCategorizedParameters().isEmpty());
        assertTrue(provider.getCategoryParameter(TEST_CATEGORY).size() == 8);
    }

    @Autowired
    private EIDASNodeMetaconfigProvider metadataProvider = null;

    @Test
    public void testNodeMetadataProviderByString(){
        assertNotNull(metadataProvider);
        assertNotNull(metadataProvider.getCategories());
        assertFalse(metadataProvider.getCategories().isEmpty());
        assertFalse(metadataProvider.getCategorizedParameters().isEmpty());
    }

}
