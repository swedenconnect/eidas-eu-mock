/*
 * Copyright (c) 2023 by European Commission
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

package eu.eidas.node.auth.metadata;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.DefaultProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.core.eidas.spec.EidasSignatureConstants;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.node.auth.AUNODEUtil;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import eu.eidas.node.utils.EidasNodeMetadataGenerator;
import org.apache.xml.security.signature.XMLSignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

import javax.annotation.Nonnull;
import java.net.URI;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.isA;

public class EidasNodeMetadataGeneratorTest {

    private static final String PROXY_SERVICE_ENTITY_ID="http://localhost/EidasNode/ServiceMetadata";
    private static final String SAML_CONNECTOR_IDP = "METADATA";
    private static final String BINDING_LOCATION_URL = "http://localhost:8080/EidasNode/ServiceProvider";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUp(){
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        OpenSamlHelper.initialize();
    }

    @AfterClass
    public static void tearDown() {
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (bouncyCastleProvider != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the {@link eu.eidas.auth.engine.configuration.dom.SignatureKey#SIGNATURE_ALGORITHM_WHITE_LIST} is not defined
     * <p>
     * The metadata should publish the Default signature algorithm whitelist.
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithDefaultSigningAlgorithms() throws UnmarshallException, EIDASMetadataException {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();

        Properties nodeProps = createProperties();

        AUSERVICEUtil nodeUtil = new AUSERVICEUtil();
        nodeUtil.setConfigs(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorProxyService
                .generateProxyServiceMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        Assert.assertEquals(EidasSignatureConstants.DEFAULT_SIGNATURE_ALGORITHM_WHITE_LIST, getSigningAlgorithms(metadata));
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the {@link eu.eidas.auth.engine.configuration.dom.SignatureKey#SIGNATURE_ALGORITHM_WHITE_LIST} is defined
     * with RSA_SHA256, RSA_SHA256_MGF1, RSA_SHA384_MGF1, RSA_SHA512_MGF1 but RSA_SHA256 is not allowed according to specs.
     * <p>
     * The metadata should only publish the signature algorithms allowed in the specifications.
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithUnsupportedSigningAlgorithms() throws UnmarshallException, EIDASMetadataException {
        final EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();
        eidasNodeMetadataGeneratorProxyService.setProxyServiceEngine("ServiceWithAlgorithmWhitelist"); // SignModule_ServiceWithAlgorithmWhitelist.xml

        final Properties nodeProps = createProperties();
        final AUSERVICEUtil nodeUtil = new AUSERVICEUtil();
        nodeUtil.setConfigs(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        final ProtocolEngineI protocolEngine = ProtocolEngineFactory.getDefaultProtocolEngine("METADATA");
        final String metadata = eidasNodeMetadataGeneratorProxyService.generateProxyServiceMetadata(protocolEngine);

        final ImmutableSet<String> expectedListOfSigningAlgorithms = ImmutableSet.of(
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1,
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384_MGF1,
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1);
        Assert.assertEquals(expectedListOfSigningAlgorithms, getSigningAlgorithms(metadata));
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the {@link eu.eidas.auth.engine.configuration.dom.SignatureKey#SIGNATURE_ALGORITHM_WHITE_LIST} is defined
     * with RSA_SHA256, RSA_SHA256_MGF1, RSA_SHA384_MGF1, RSA_SHA512_MGF1 but RSA_SHA256 is not allowed according to specs.
     *
     * The metadata should only publish the signature algorithms allowed in the specifications.
     * <p>
     * Must fail.
     */
    @Test
    public void testGenerateMetadataWithAlgorithmNotInWhitelist() {
        final String notInWhitelistAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA256;
        exception.expect(IllegalStateException.class);
        exception.expectCause(isA(ProtocolEngineConfigurationException.class));
        exception.expectMessage("Metadata signing algorithm \""+ notInWhitelistAlgorithm +"\" is not allowed");

        final EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();
        eidasNodeMetadataGeneratorProxyService.setProxyServiceEngine("ServiceWithMetadataAlgorithmNotInAlgorithmWhitelist");

        final Properties nodeProps = createProperties();
        final AUSERVICEUtil nodeUtil = new AUSERVICEUtil();
        nodeUtil.setConfigs(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        final ProtocolEngineI protocolEngine = ProtocolEngineFactory.getDefaultProtocolEngine("METADATA");
        eidasNodeMetadataGeneratorProxyService.generateProxyServiceMetadata(protocolEngine);
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the {@link eu.eidas.auth.engine.configuration.dom.SignatureKey#SIGNATURE_ALGORITHM_WHITE_LIST} is empty
     * <p>
     * Must fail.
     */
    @Test
    public void testGenerateMetadataWithWhitelistEmpty() {
        final String parameterName = "signature.algorithm.whitelist";
        final String configurationName = "SignatureConf";
        final String instanceName = "ServiceWithoutAlgorithmWhitelist";

        exception.expect(IllegalArgumentException.class);
        exception.expectCause(isA(ProtocolEngineConfigurationException.class));
        exception.expectMessage("SAML engine configuration file contains parameter name \"" + parameterName
                + "\" with a blank value for configuration name \"" + configurationName
                + "\" in instance name \"" + instanceName + "\"");

        final EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();
        eidasNodeMetadataGeneratorProxyService.setProxyServiceEngine(instanceName);

        final Properties nodeProps = createProperties();
        final AUSERVICEUtil nodeUtil = new AUSERVICEUtil();
        nodeUtil.setConfigs(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        final ProtocolEngineI protocolEngine = ProtocolEngineFactory.getDefaultProtocolEngine("METADATA");
        eidasNodeMetadataGeneratorProxyService.generateProxyServiceMetadata(protocolEngine);
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the {@link EidasParameterKeys#UNSUPPORTED_ATTRIBUTES} is defined
     * <p>
     * The metadata should only publish the attributes which NamedURI are not present in the parameter
     * comma-separated value list.
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithOnlySupportedAttributes() {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();

        Properties nodeProps = createProperties();
        String unsupportedAttributes = getUnsupportedAttributesAsJoinedString(EidasSpec.Definitions.BIRTH_NAME, EidasSpec.Definitions.LEGAL_NAME);
        nodeProps.setProperty(EidasParameterKeys.UNSUPPORTED_ATTRIBUTES.toString(), unsupportedAttributes);

        AUSERVICEUtil nodeUtil = new AUSERVICEUtil();
        nodeUtil.setConfigs(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorProxyService
                .generateProxyServiceMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        Assert.assertFalse(isIDPMetadataDescriptorContaining(metadata, EidasSpec.Definitions.BIRTH_NAME));
        Assert.assertFalse(isIDPMetadataDescriptorContaining(metadata, EidasSpec.Definitions.LEGAL_NAME));
        Assert.assertTrue(isIDPMetadataDescriptorContaining(metadata, EidasSpec.Definitions.GENDER));
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the {@link EidasParameterKeys#UNSUPPORTED_ATTRIBUTES} is not defined
     * <p></p>
     * The metadata should publish all the eIDAS attributes
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithAllEidasAttributes() {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();

        Properties nodeProps = createProperties();

        AUSERVICEUtil nodeUtil = new AUSERVICEUtil();
        nodeUtil.setConfigs(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorProxyService
                .generateProxyServiceMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        for (AttributeDefinition<?> attributeDefinition: EidasSpec.REGISTRY.getAttributes()) {
            Assert.assertTrue(isIDPMetadataDescriptorContaining(metadata, attributeDefinition));
        }
    }


    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the RequesterIdFlag is set to true in Properties
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithRequesterIdFlagTrue() {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();

        Properties nodeProps = createProperties();
        setRequesterIdFlagPropertyTrue(nodeProps);
        AUNODEUtil nodeUtil = Mockito.mock(AUSERVICEUtil.class);
        Mockito.when(nodeUtil.getConfigs()).thenReturn(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorProxyService
                .generateProxyServiceMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        Assert.assertTrue(metadataContainsRequesterIdFlagAtEndOfEntityAttributesElement(metadata));
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the RequesterIdFlag is set to false in Properties
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithRequesterIdFlagFalse() {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();

        Properties nodeProps = createProperties();
        setRequesterIdFlagPropertyFalse(nodeProps);
        AUNODEUtil nodeUtil = Mockito.mock(AUSERVICEUtil.class);
        Mockito.when(nodeUtil.getConfigs()).thenReturn(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorProxyService
                .generateProxyServiceMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        Assert.assertFalse(metadataContainsRequesterIdFlagAtEndOfEntityAttributesElement(metadata));
    }

    @Nonnull
    private Properties createProperties() {
        final Properties nodeProps = new Properties();
        return nodeProps;
    }

    /**
     * Test method for
     * {@link EidasNodeMetadataGenerator#generateProxyServiceMetadata(ProtocolEngineI)}
     * when the RequesterIdFlag is set to false in Properties
     * <p>
     * Must succeed.
     */
    @Test
    public void testGenerateMetadataWithRequesterIdFlagNotSet() {
        EidasNodeMetadataGenerator eidasNodeMetadataGeneratorProxyService = buildEidasNodeMetadataGeneratorProxyService();

        Properties nodeProps = createProperties();
        AUNODEUtil nodeUtil = Mockito.mock(AUSERVICEUtil.class);
        Mockito.when(nodeUtil.getConfigs()).thenReturn(nodeProps);
        eidasNodeMetadataGeneratorProxyService.setNodeUtil(nodeUtil);

        String metadata = eidasNodeMetadataGeneratorProxyService
                .generateProxyServiceMetadata(ProtocolEngineFactory.getDefaultProtocolEngine("METADATA"));

        Assert.assertFalse(metadataContainsRequesterIdFlagAtEndOfEntityAttributesElement(metadata));
    }

    private void setRequesterIdFlagPropertyTrue(final Properties nodeProps) {
        setRequesterIdFlagValue(nodeProps, "true");
    }

    private void setRequesterIdFlagPropertyFalse(final Properties nodeProps) {
        setRequesterIdFlagValue(nodeProps, "false");
    }

    private void setRequesterIdFlagValue(final Properties nodeProps, final String value) {
        nodeProps.setProperty(EIDASValues.REQUESTER_ID_FLAG.toString(), value);
    }

    private boolean metadataContainsRequesterIdFlagAtEndOfEntityAttributesElement(String metadata) {
        final String requesterIdFlag =
                "<saml2:Attribute Name=\"http://macedir.org/entity-category\" NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\">" +
                        "<saml2:AttributeValue xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xsd:string\">" +
                            "http://eidas.europa.eu/entity-attributes/termsofaccess/requesterid" +
                        "</saml2:AttributeValue>" +
                "</saml2:Attribute>" +
        "</mdattr:EntityAttributes>";

        return metadata.contains(requesterIdFlag);
    }

    private boolean isIDPMetadataDescriptorContaining(String metadata, AttributeDefinition<?> attributeDefinition) {
        String attributeDefinitionInMetadata = "<saml2:Attribute xmlns:saml2=\"urn:oasis:names:tc:SAML:2.0:assertion\""
                + " Name=\"" + attributeDefinition.getNameUri().toASCIIString() + "\""
                + " NameFormat=\"urn:oasis:names:tc:SAML:2.0:attrname-format:uri\""
                + "/>";

        String idpSSODescriptor = metadata.substring(metadata.indexOf("<md:IDPSSODescriptor"), metadata.indexOf("</md:IDPSSODescriptor"));

        return idpSSODescriptor.contains(attributeDefinitionInMetadata);
    }

    private ImmutableSet<String> getSigningAlgorithms(String metadata) throws UnmarshallException, EIDASMetadataException {
        EntityDescriptor entityDescriptor = MetadataUtil.deserializeEntityDescriptor(metadata).getEntityDescriptors().get(0);
        EidasMetadataParametersI params = MetadataUtil.convertEntityDescriptor(entityDescriptor);
        ImmutableSet.Builder metadataSigningAlgorithms = ImmutableSet.builder();
        for (String signAlgorithm : params.getSigningMethods().split("[;,]")) {
            metadataSigningAlgorithms.add(signAlgorithm.trim());
        }
        return metadataSigningAlgorithms.build();
    }

    private String getUnsupportedAttributesAsJoinedString(AttributeDefinition<?> ... attributeDefinitions) {
        List<String> attributesNamedURI = Arrays.stream(attributeDefinitions)
                .map(AttributeDefinition::getNameUri)
                .map(URI::toASCIIString)
                .collect(Collectors.toList());
        return String.join(";", attributesNamedURI);
    }

    private EidasNodeMetadataGenerator buildEidasNodeMetadataGeneratorProxyService() {
        final EidasNodeMetadataGenerator eidasNodeMetadataGenerator = new EidasNodeMetadataGenerator();
        eidasNodeMetadataGenerator.setProxyServiceEngine(SAML_CONNECTOR_IDP);
        eidasNodeMetadataGenerator.setProxyServiceMetadataUrl(PROXY_SERVICE_ENTITY_ID);
        eidasNodeMetadataGenerator.setSingleSignOnServicePostLocation(BINDING_LOCATION_URL);
        eidasNodeMetadataGenerator.setSingleSignOnServiceRedirectLocation(BINDING_LOCATION_URL);

        eidasNodeMetadataGenerator.setNodeProtocolEngineFactory(DefaultProtocolEngineFactory.getInstance());

        return eidasNodeMetadataGenerator;
    }
}