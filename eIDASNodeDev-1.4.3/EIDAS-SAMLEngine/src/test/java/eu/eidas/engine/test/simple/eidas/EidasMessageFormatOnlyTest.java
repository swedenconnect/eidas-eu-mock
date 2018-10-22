package eu.eidas.engine.test.simple.eidas;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.stork.IStorkAuthenticationRequest;
import eu.eidas.auth.commons.protocol.stork.impl.StorkAuthenticationRequest;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.SAMLCore;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;

public class EidasMessageFormatOnlyTest {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasMessageFormatOnlyTest.class.getName());

    private static AttributeDefinition<String> newStorkAttributeDefinition(String friendlyName, boolean required) {
        return new AttributeDefinition.Builder<String>().nameUri(SAMLCore.STORK10_BASE_URI.getValue() + friendlyName)
                .friendlyName(friendlyName)
                .required(required)
                .personType(PersonType.NATURAL_PERSON)
                .xmlType("http://www.w3.org/2001/XMLSchema", "string", "eidas-natural")
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();
    }

    private ProtocolEngineI getEngine(String conf) {
        ProtocolEngineI engine = null;
            engine = ProtocolEngineFactory.getDefaultProtocolEngine(conf);
        return engine;
    }

    private ProtocolEngineI getEngine() {
        return getEngine("EIDASONLY");
    }

    private ProtocolEngineI getStorkEngine() {
        return getEngine("CONF2");
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

    public EidasMessageFormatOnlyTest() {
        final AttributeDefinition<String> isAgeOver = newStorkAttributeDefinition("isAgeOver", true);

        final AttributeDefinition<String> dateOfBirth = newStorkAttributeDefinition("dateOfBirth", false);

        final AttributeDefinition<String> eIDNumber = newStorkAttributeDefinition("eIdentifier", true);

        immutableAttributeMap =
                new ImmutableAttributeMap.Builder().put(isAgeOver, new StringAttributeValue("16", false), new StringAttributeValue("18", false)).put(dateOfBirth).put(eIDNumber).build();

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
        ProtocolEngineI engine = getEngine();
        assertNotNull(engine);
        byte[] request = null;
        try {
            request = generateStorkRequest();
        } catch (EIDASSAMLEngineException ee) {
            fail("error during the generation of stork request: " + ee);
        }
        try {
            engine.unmarshallRequestAndValidate(request, "EN",Arrays.asList(REQUEST_ISSUER));
            fail("can validate stork request on eidas only processor");
        } catch (EIDASSAMLEngineException ee) {
            try {
                getStorkEngine().unmarshallRequestAndValidate(request, "EN",Arrays.asList(REQUEST_ISSUER));
            } catch (EIDASSAMLEngineException ee1) {
                fail("cannot validate stork request on multi processor engine");
            }
        }

    }

    private static final String REQUEST_ISSUER = "http://localhost:7001/SP/metadata".toLowerCase();
    private byte[] generateStorkRequest() throws EIDASSAMLEngineException {

		IStorkAuthenticationRequest request = StorkAuthenticationRequest.builder()
                .id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
                .issuer(REQUEST_ISSUER)
                .destination(destination)
                .providerName(spName)
                .qaa(QAAL)
                .requestedAttributes(immutableAttributeMap)
                .assertionConsumerServiceURL(assertConsumerUrl)
                .spSector(spSector)
                .spInstitution(spInstitution)
                .spApplication(spApplication)
                .serviceProviderCountryCode(spCountry)
                .spId(spId)
                .citizenCountryCode("ES")
                .levelOfAssurance("High")
                .build();

        return getStorkEngine().generateRequestMessage(request, request.getIssuer()).getMessageBytes();
    }

}
