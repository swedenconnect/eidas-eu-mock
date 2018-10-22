package eu.eidas.samlengineconfig;

import org.junit.Test;

import eu.eidas.samlengineconfig.impl.marshaller.EngineInstanceUnmarshallerImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestEngineInstanceUnmarshaller {
    String TEST_REAL="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<instances>\n" +
            "\n" +
            "\t<!-- ******************** Service ******************** -->\n" +
            "\t<!-- Configuration name -->\n" +
            "\t<instance name=\"Service\">\n" +
            "\t\t<!-- Configurations parameters SamlEngine -->\n" +
            "\t\t<configuration name=\"SamlEngineConf\">\n" +
            "\t\t\t<parameter name=\"fileConfiguration\" value=\"SamlEngine_Service.xml\" />\n" +
            "\t\t</configuration>\n" +
            "\n" +
            "\t\t<!-- Settings module signature -->\n" +
            "\t\t<configuration name=\"SignatureConf\">\n" +
            "\t\t\t<!-- Specific signature module -->\n" +
            "\t\t\t<parameter name=\"class\"\n" +
            "\t\t\t\tvalue=\"eu.eidas.auth.engine.core.impl.SignSW\" />\n" +
            "\t\t\t<!-- Settings specific module -->\n" +
            "\t\t\t<parameter name=\"fileConfiguration\" value=\"SignModule_Service.xml\" />\n" +
            "\t\t</configuration>\n" +
            "\n" +
            "        <!-- Settings module encryption -->\n" +
            "        <configuration name=\"EncryptionConf\">\n" +
            "            <!-- Specific signature module -->\n" +
            "            <parameter name=\"class\"\n" +
            "                       value=\"eu.eidas.auth.engine.core.impl.EncryptionSW\" />\n" +
            "            <!-- Settings specific module\n" +
            "                 responseTo/FromPointAlias & requestTo/FromPointAlias parameters will be added -->\n" +
            "            <parameter name=\"fileConfiguration\" value=\"EncryptModule_Service-CB.xml\" />\n" +
            "            <!-- Settings for activation of the encryption. If file not found then no encryption applies-->\n" +
            "            <parameter name=\"fileActivationConfiguration\"\n" +
            "                       value=\"c:\\PGM\\projects\\configEidas\\encryptionConf.xml\" />\n" +
            "        </configuration>\n" +
            "\t</instance>" +
            "</instances>";

    private static final String TEST_IBM_JVM="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<instances>\n" +
            "\n" +
            "\t<!-- ******************** Service ******************** -->\n" +
            "\t<!-- Configuration name -->\n" +
            "\t<instance name=\"Service\">\n" +
            "\t\t<!-- Configurations parameters SamlEngine -->\n" +
            "\t\t<configuration name=\"SamlEngineConf\">\n" +
            "\t\t\t<parameter name=\"fileConfiguration\" value=\"SamlEngine_Service.xml\" />\n" +
            "\t\t</configuration>\n" +
            "\n" +
            "\t\t<!-- Settings module signature -->\n" +
            "\t\t<configuration name=\"SignatureConf\">\n" +
            "\t\t\t<!-- Specific signature module -->\n" +
            "\t\t\t<parameter name=\"class\"\n" +
            "\t\t\t\tvalue=\"eu.eidas.auth.engine.core.impl.SignSW\" />\n" +
            "\t\t\t<!-- Settings specific module -->\n" +
            "\t\t\t<parameter name=\"fileConfiguration\" value=\"SignModule_Service.xml\" />\n" +
            "\t\t</configuration>\n" +
            "\n" +
            "        <!-- Settings module encryption -->\n" +
            "        <configuration name=\"EncryptionConf\">\n" +
            "            <!-- Specific signature module -->\n" +
            "            <parameter name=\"class\"\n" +
            "                       value=\"eu.service.auth.engine.core.impl.EncryptionSW\" />\n" +
            "            <!-- Settings specific module\n" +
            "                 responseTo/FromPointAlias & requestTo/FromPointAlias parameters will be added -->\n" +
            "            <parameter name=\"fileConfiguration\" value=\"EncryptModule_Service-CB.xml\" />\n" +
            "            <!-- Settings for activation of the encryption. If file not found then no encryption applies-->\n" +
            "            <parameter name=\"fileActivationConfiguration\"\n" +
            "                       value=\"c:\\PGM\\projects\\configEidas\\encryptionConf.xml\" />\n" +
            "        </configuration>\n" +
            "\t</instance>\n" +
            "\n" +
            "\t<!-- ******************** SP-Specific ******************** -->\n" +
            "\n" +
            "\t<instance name=\"SP-Specific\">\n" +
            "\t\t<configuration name=\"SamlEngineConf\">\n" +
            "\t\t\t<parameter name=\"fileConfiguration\" value=\"SamlEngine_SP-Specific.xml\" />\n" +
            "\t\t</configuration>\n" +
            "\n" +
            "\t\t<configuration name=\"SignatureConf\">\n" +
            "\t\t\t<parameter name=\"class\"\n" +
            "\t\t\t\tvalue=\"eu.eidas.auth.engine.core.impl.SignSW\" />\n" +
            "\t\t\t<parameter name=\"fileConfiguration\" value=\"SignModule_SP-Specific.xml\" />\n" +
            "\t\t</configuration>\n" +
            "\n" +
            "        <configuration name=\"EncryptionConf\">\n" +
            "            <!-- Specific signature module -->\n" +
            "            <parameter name=\"class\"\n" +
            "                       value=\"eu.eidas.auth.engine.core.impl.EncryptionSW\" />\n" +
            "            <!-- Settings specific module\n" +
            "                 responseTo/FromPointAlias & requestTo/FromPointAlias parameters will be added -->\n" +
            "            <parameter name=\"fileConfiguration\" value=\"EncryptModule_SP-Connector-CB.xml\" />\n" +
            "            <!-- Settings for activation of the encryption. If file not found then no encryption applies-->\n" +
            "            <parameter name=\"fileActivationConfiguration\"\n" +
            "                       value=\"c:\\PGM\\projects\\configEidas\\encryptionConf.xml\" />\n" +
            "        </configuration>\n" +
            "\t</instance>\n" +
            "\n" +
            "\n" +
            "\t<!-- ******************** Connector-Service ******************** -->\n" +
            "\n" +
            "\t<instance name=\"Connector-Service\">\n" +
            "\t\t<configuration name=\"SamlEngineConf\">\n" +
            "\t\t\t<parameter name=\"fileConfiguration\" value=\"SamlEngine_Connector-Service.xml\" />\n" +
            "\t\t</configuration>\n" +
            "\n" +
            "\t\t<configuration name=\"SignatureConf\">\n" +
            "\t\t\t<parameter name=\"class\"\n" +
            "\t\t\t\tvalue=\"eu.eidas.auth.engine.core.impl.SignSW\" />\n" +
            "\t\t\t<parameter name=\"fileConfiguration\" value=\"SignModule_Connector-Service.xml\" />\n" +
            "\t\t</configuration>\n" +
            "\n" +
            "        <configuration name=\"EncryptionConf\">\n" +
            "            <!-- Specific signature module -->\n" +
            "            <parameter name=\"class\"\n" +
            "                       value=\"eu.eidas.auth.engine.core.impl.EncryptionSW\" />\n" +
            "            <!-- Settings specific module\n" +
            "                 responseTo/FromPointAlias & requestTo/FromPointAlias parameters will be added -->\n" +
            "            <parameter name=\"fileConfiguration\" value=\"EncryptModule_Connector-Service-CB.xml\" />\n" +
            "            <!-- Settings for activation of the encryption. If file not found then no encryption applies-->\n" +
            "            <parameter name=\"fileActivationConfiguration\"\n" +
            "                       value=\"c:\\PGM\\projects\\configEidas\\encryptionConf.xml\" />\n" +
            "        </configuration>\n" +
            "\t</instance>\n" +
            "\n" +
            "\t<!-- ******************** Specific-IdP ******************** -->\n" +
            "\t<!-- Configuration name -->\n" +
            "\t<instance name=\"Specific-IdP\">\n" +
            "\t\t<!-- Configurations parameters SamlEngine -->\n" +
            "\t\t<configuration name=\"SamlEngineConf\">\n" +
            "\t\t\t<parameter name=\"fileConfiguration\" value=\"SamlEngine_Specific-IdP.xml\" />\n" +
            "\t\t</configuration>\n" +
            "\n" +
            "\t\t<!-- Settings module signature -->\n" +
            "\t\t<configuration name=\"SignatureConf\">\n" +
            "\t\t\t<!-- Specific signature module -->\n" +
            "\t\t\t<parameter name=\"class\"\n" +
            "\t\t\t\tvalue=\"eu.eidas.auth.engine.core.impl.SignSW\" />\n" +
            "\t\t\t<!-- Settings specific module -->\n" +
            "\t\t\t<parameter name=\"fileConfiguration\" value=\"SignModule_Specific-IdP.xml\" />\n" +
            "\t\t</configuration>\n" +
            "\n" +
            "        <configuration name=\"EncryptionConf\">\n" +
            "            <!-- Specific signature module -->\n" +
            "            <parameter name=\"class\"\n" +
            "                       value=\"eu.eidas.auth.engine.core.impl.EncryptionSW\" />\n" +
            "            <!-- Settings specific module\n" +
            "                 responseTo/FromPointAlias & requestTo/FromPointAlias parameters will be added -->\n" +
            "            <parameter name=\"fileConfiguration\" value=\"EncryptModule_Specific-CB.xml\" />\n" +
            "            <!-- Settings for activation of the encryption. If file not found then no encryption applies-->\n" +
            "            <parameter name=\"fileActivationConfiguration\"\n" +
            "                       value=\"c:\\PGM\\projects\\configEidas\\encryptionConf_specific.xml\" />\n" +
            "        </configuration>\n" +
            "\t</instance>\n" +
            "\n" +
            "</instances>";
    private static final String TEST_SIMPLE="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<instances name=\"oo\">\n" +
            "    <instance name=\"engineinstance1\" >\n" +
            "        <configuration name=\"name11\">\n" +
            "            <parameter name=\"sp-name111\" value=\"sp-value111\"></parameter>\n" +
            "        </configuration>\n" +
            "    </instance>\n" +
            "    <instance name=\"engineinstance2\" >\n" +
            "        <configuration name=\"name21\">\n" +
            "            <parameter name=\"sp-name211\" value=\"sp-value211\"></parameter>\n" +
            "        </configuration>\n" +
            "    </instance>\n" +
            "</instances>";

    @Test
    public void testDeserialize(){
        EngineInstanceUnmarshallerImpl eiui=new EngineInstanceUnmarshallerImpl();
        SamlEngineConfiguration ec = eiui.readEngineInstanceFromString(TEST_SIMPLE);
        assertNotNull(ec);
        assertEquals(ec.getInstances().size(), 2);
        assertEquals(ec.getInstances().get(0).getConfigurations().size(), 1);
        assertNotNull(ec.getInstances().get(0).getConfigurations().get(0).getName());
        assertNotNull(ec.getInstances().get(0).getConfigurations().get(0).getParameters());
        assertFalse(ec.getInstances().get(0).getConfigurations().get(0).getParameters().isEmpty());
    }
    @Test
    public void testDeserializeIBM_JVMtest(){
        EngineInstanceUnmarshallerImpl eiui=new EngineInstanceUnmarshallerImpl();
        SamlEngineConfiguration ec = eiui.readEngineInstanceFromString(TEST_IBM_JVM);
        assertNotNull(ec);
        assertEquals(ec.getInstances().size(), 4);
    }
}
