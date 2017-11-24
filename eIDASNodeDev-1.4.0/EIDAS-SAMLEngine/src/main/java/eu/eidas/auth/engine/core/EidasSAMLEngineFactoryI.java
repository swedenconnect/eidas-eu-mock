package eu.eidas.auth.engine.core;

import java.util.Properties;

import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngineI;

/**
 * provides SAMLEngine instances
 * @deprecated use {@link ProtocolEngine#getDefaultSamlEngine(String)} instead.
 */
@Deprecated
public interface EidasSAMLEngineFactoryI {

    /**
     * @param name the name of the engine
     * @param props additional properties used for initializing the engine
     * @return
     */
    ProtocolEngineI getEngine(String name, Properties props);

    /**
     * releases the provided engine
     *
     * @param engine
     */
    void releaseEngine(ProtocolEngineI engine);

    /**
     * @param name
     * @return the number of active engines with the given name (or total count if name is null)
     */
    int getActiveEngineCount(String name);
}
