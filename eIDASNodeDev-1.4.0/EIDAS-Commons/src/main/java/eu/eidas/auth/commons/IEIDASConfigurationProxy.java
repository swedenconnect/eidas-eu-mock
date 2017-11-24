package eu.eidas.auth.commons;

/**
 * provides access to values of eidas node configuration parameters
 */
public interface IEIDASConfigurationProxy {
    /**
     *
     * @param parameterName the name of the eidas Node parameter
     * @return the parameter value (or null if the parameter is missing from the config)
     */
    String getEidasParameterValue(String parameterName);
}
