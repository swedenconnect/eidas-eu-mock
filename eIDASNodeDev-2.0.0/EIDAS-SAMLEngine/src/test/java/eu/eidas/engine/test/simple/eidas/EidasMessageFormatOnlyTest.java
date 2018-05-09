package eu.eidas.engine.test.simple.eidas;

import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.NaturalPersonSpec;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class EidasMessageFormatOnlyTest {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasMessageFormatOnlyTest.class.getName());

    private ProtocolEngineI getEngine(String conf) {
        ProtocolEngineI engine = null;
            engine = ProtocolEngineFactory.getDefaultProtocolEngine(conf);
        return engine;
    }

    private ProtocolEngineI getEngine() {
        return getEngine("EIDASONLY");
    }

    /**
     * The destination.
     */
    private String destination;

    /**
     * The service provider name.
     */
    private String spName;

    /**
     * The service provider sector.
     */
    private String spSector;

    /**
     * The service provider institution.
     */
    private String spInstitution;

    /**
     * The service provider application.
     */
    private String spApplication;

    /**
     * The service provider country.
     */
    private String spCountry;

    /**
     * The service provider id.
     */
    private String spId;

    /**
     * The quality authentication assurance level.
     */
    private static final int QAAL = 3;

    /**
     * The Map of Attributes.
     */
    private ImmutableAttributeMap immutableAttributeMap;

    /**
     * The assertion consumer URL.
     */
    private String assertConsumerUrl;

    @Before
    public void init() {

        immutableAttributeMap =
                new ImmutableAttributeMap.Builder().put(NaturalPersonSpec.Definitions.BIRTH_NAME).build();

        destination = "http://proxyservice.gov.xx/EidasNode/ColleagueRequest";
        assertConsumerUrl = "http://connector.gov.xx/EidasNode/ColleagueResponse";

        spName = "University of Oxford";
        spSector = "EDU001";
        spInstitution = "OXF001";
        spApplication = "APP001";
        spCountry = "EN";

        spId = "EDU001-OXF001-APP001";

    }

    @Test
    public void testMessageFormatForEidasOnly() {
        ProtocolEngineI engine = getEngine("CONF2");
        assertNotNull(engine);
        byte[] request = null;
        try {
            request = generateEidasRequest();
        } catch (EIDASSAMLEngineException ee) {
            fail("error during the generation of eidas request: " + ee);
        }
        try {
            getEngine("CONF2").unmarshallRequestAndValidate(request, "EN");
        } catch (EIDASSAMLEngineException ee1) {
            fail("cannot validate eidas request on multi processor engine");
        }
    }

    private byte[] generateEidasRequest() throws EIDASSAMLEngineException {

        IEidasAuthenticationRequest request = EidasAuthenticationRequest.builder()
                .id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
                .issuer("http://localhost:7001/SP/metadata")
                .destination(destination)
                .providerName(spName)
                .requestedAttributes(immutableAttributeMap)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .citizenCountryCode("ES")
                .levelOfAssurance(LevelOfAssurance.SUBSTANTIAL)
                .build();

        return getEngine("CONF2").generateRequestMessage(request, null).getMessageBytes();
    }

}
