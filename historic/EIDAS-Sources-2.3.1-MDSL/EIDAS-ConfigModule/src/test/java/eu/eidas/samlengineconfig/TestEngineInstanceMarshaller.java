/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
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
