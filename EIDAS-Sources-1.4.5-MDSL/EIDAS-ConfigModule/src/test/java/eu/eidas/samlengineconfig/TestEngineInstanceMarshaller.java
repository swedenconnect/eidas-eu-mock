package eu.eidas.samlengineconfig;

import eu.eidas.samlengineconfig.EngineInstance;
import eu.eidas.samlengineconfig.SamlEngineConfiguration;
import eu.eidas.samlengineconfig.StringParameter;
import eu.eidas.samlengineconfig.impl.EngineInstanceImpl;
import eu.eidas.samlengineconfig.impl.InstanceConfigurationImpl;
import eu.eidas.samlengineconfig.impl.SamlEngineConfigurationImpl;
import eu.eidas.samlengineconfig.impl.marshaller.EngineInstanceMarshallerImpl;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

public class TestEngineInstanceMarshaller {
    @Test
    public void testSerialize(){
        EngineInstanceMarshallerImpl eimi=new EngineInstanceMarshallerImpl();
        SamlEngineConfiguration config=new SamlEngineConfigurationImpl();
        EngineInstance instance=new EngineInstanceImpl();
        StringParameter sp=new StringParameter();
        sp.setName("sp-name");
        sp.setValue("sp-value");
        InstanceConfigurationImpl ic=new InstanceConfigurationImpl("name",null);
        ic.getParameters().add(sp);
        instance.addConfiguration(ic);
        instance.setName("engineinstance");
        config.addInstance(instance);
        String s=eimi.serializeEngineInstance(config);
        assertNotNull(s);
        assertNotNull(s);
    }
}
