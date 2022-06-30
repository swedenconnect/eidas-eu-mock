package eu.eidas.idp;

import eu.eidas.auth.commons.EIDASUtil;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.ProtocolEngineConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public final class IDPUtil {

    private static final Logger LOG = LoggerFactory.getLogger(IDPUtil.class);

    private static final Properties idpProperties = loadIDPConfigs();

    static ProtocolEngineConfigurationFactory protocolEngineConfigurationFactory = null;
    static ProtocolEngineFactory defaultProtocolEngineFactory = null;

    public static Properties loadConfigs(String fileName) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(getConfigFilePath() + fileName));
        return properties;
    }

    public static Properties loadIDPConfigs() throws ApplicationSpecificIDPException {
        try {
            return IDPUtil.loadConfigs(Constants.IDP_PROPERTIES);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            throw new ApplicationSpecificIDPException("Could not load configuration file", e);
        }
    }


    /**
     * @return metadata directory
     */
    public static String getMetadataRepositoryPath() {
        return idpProperties.getProperty(Constants.IDP_METADATA_REPOPATH);
    }

    /**
     * @return true when the metadata support should be active
     */
    public static boolean isMetadataHttpFetchEnabled() {
        return idpProperties.getProperty(Constants.IDP_METADATA_HTTPFETCH) == null || Boolean.parseBoolean(
                idpProperties.getProperty(Constants.IDP_METADATA_HTTPFETCH));
    }

    /**
     * @return true metadata signature must be validated for those not in trusted list
     */
    public static boolean isValidateEntityDescriptorSignatureEnabled() {
        Properties properties = IDPUtil.loadIDPConfigs();
        return properties.getProperty(Constants.IDP_METADATA_VALIDATESIGN) == null || Boolean.parseBoolean(
                properties.getProperty(Constants.IDP_METADATA_VALIDATESIGN));
    }

    public static String getTrustedEntityDescriptors() {
        Properties properties = IDPUtil.loadIDPConfigs();
        return properties.getProperty(Constants.IDP_METADATA_TRUSTEDDS, "");
    }

    public static String getConfigFilePath() {
        /*String envLocation = System.getenv().get(Constants.IDP_CONFIG_REPOSITORY);
        String configLocation = System.getProperty(Constants.IDP_CONFIG_REPOSITORY, envLocation);
        return configLocation;*/
        return (String)ApplicationContextProvider.getApplicationContext().getBean(Constants.IDP_REPO_BEAN_NAME);
    }

    public static synchronized ProtocolEngineI getProtocolEngine() {
        if (defaultProtocolEngineFactory == null) {
            protocolEngineConfigurationFactory = new ProtocolEngineConfigurationFactory(Constants.IDP_SAMLENGINE_FILE, null, IDPUtil.getConfigFilePath());
            try {
                defaultProtocolEngineFactory = new ProtocolEngineFactory(protocolEngineConfigurationFactory);
            } catch (SamlEngineConfigurationException e) {
                LOG.error("Error creating protocol engine factory : ", e);
            }
        }
        return defaultProtocolEngineFactory.getProtocolEngine(Constants.SAMLENGINE_NAME);
    }

    private IDPUtil() {
    }
}
