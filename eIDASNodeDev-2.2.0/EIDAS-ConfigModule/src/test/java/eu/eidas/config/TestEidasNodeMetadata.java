
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
