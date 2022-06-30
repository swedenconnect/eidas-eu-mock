package eu.eidas.auth.engine.core.stork;

import org.opensaml.Configuration;

import eu.eidas.auth.engine.core.stork.impl.AuthenticationAttributesBuilder;
import eu.eidas.auth.engine.core.stork.impl.AuthenticationAttributesMarshaller;
import eu.eidas.auth.engine.core.stork.impl.AuthenticationAttributesUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.CitizenCountryCodeBuilder;
import eu.eidas.auth.engine.core.stork.impl.CitizenCountryCodeMarshaller;
import eu.eidas.auth.engine.core.stork.impl.CitizenCountryCodeUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.EIDCrossBorderShareBuilder;
import eu.eidas.auth.engine.core.stork.impl.EIDCrossBorderShareMarshaller;
import eu.eidas.auth.engine.core.stork.impl.EIDCrossBorderShareUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.EIDCrossSectorShareBuilder;
import eu.eidas.auth.engine.core.stork.impl.EIDCrossSectorShareMarshaller;
import eu.eidas.auth.engine.core.stork.impl.EIDCrossSectorShareUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.EIDSectorShareBuilder;
import eu.eidas.auth.engine.core.stork.impl.EIDSectorShareMarshaller;
import eu.eidas.auth.engine.core.stork.impl.EIDSectorShareUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.QAAAttributeBuilder;
import eu.eidas.auth.engine.core.stork.impl.QAAAttributeMarshaller;
import eu.eidas.auth.engine.core.stork.impl.QAAAttributeUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.RequestedAttributeBuilder;
import eu.eidas.auth.engine.core.stork.impl.RequestedAttributeMarshaller;
import eu.eidas.auth.engine.core.stork.impl.RequestedAttributeUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.RequestedAttributesBuilder;
import eu.eidas.auth.engine.core.stork.impl.RequestedAttributesMarshaller;
import eu.eidas.auth.engine.core.stork.impl.RequestedAttributesUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.SPApplicationBuilder;
import eu.eidas.auth.engine.core.stork.impl.SPApplicationMarshaller;
import eu.eidas.auth.engine.core.stork.impl.SPApplicationUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.SPCountryBuilder;
import eu.eidas.auth.engine.core.stork.impl.SPCountryMarshaller;
import eu.eidas.auth.engine.core.stork.impl.SPCountryUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.SPIDBuilder;
import eu.eidas.auth.engine.core.stork.impl.SPIDMarshaller;
import eu.eidas.auth.engine.core.stork.impl.SPIDUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.SPInformationBuilder;
import eu.eidas.auth.engine.core.stork.impl.SPInformationMarshaller;
import eu.eidas.auth.engine.core.stork.impl.SPInformationUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.SPInstitutionBuilder;
import eu.eidas.auth.engine.core.stork.impl.SPInstitutionMarshaller;
import eu.eidas.auth.engine.core.stork.impl.SPInstitutionUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.SPSectorBuilder;
import eu.eidas.auth.engine.core.stork.impl.SPSectorMarshaller;
import eu.eidas.auth.engine.core.stork.impl.SPSectorUnmarshaller;
import eu.eidas.auth.engine.core.stork.impl.VIDPAuthenticationAttributesBuilder;
import eu.eidas.auth.engine.core.stork.impl.VIDPAuthenticationAttributesMarshaller;
import eu.eidas.auth.engine.core.stork.impl.VIDPAuthenticationAttributesUnmarshaller;

public final class StorkExtensionConfiguration {

    private StorkExtensionConfiguration() {}

    public static void configureExtension() {
        Configuration.registerObjectProvider(QAAAttribute.DEF_ELEMENT_NAME,
                new QAAAttributeBuilder(), new QAAAttributeMarshaller(),
                new QAAAttributeUnmarshaller());

        Configuration.registerObjectProvider(EIDSectorShare.DEF_ELEMENT_NAME,
                new EIDSectorShareBuilder(), new EIDSectorShareMarshaller(),
                new EIDSectorShareUnmarshaller());

        Configuration.registerObjectProvider(
                EIDCrossSectorShare.DEF_ELEMENT_NAME,
                new EIDCrossSectorShareBuilder(),
                new EIDCrossSectorShareMarshaller(),
                new EIDCrossSectorShareUnmarshaller());

        Configuration.registerObjectProvider(
                EIDCrossBorderShare.DEF_ELEMENT_NAME,
                new EIDCrossBorderShareBuilder(),
                new EIDCrossBorderShareMarshaller(),
                new EIDCrossBorderShareUnmarshaller());

        Configuration.registerObjectProvider(SPSector.DEF_ELEMENT_NAME,
                new SPSectorBuilder(), new SPSectorMarshaller(),
                new SPSectorUnmarshaller());

        Configuration.registerObjectProvider(SPInstitution.DEF_ELEMENT_NAME,
                new SPInstitutionBuilder(), new SPInstitutionMarshaller(),
                new SPInstitutionUnmarshaller());

        Configuration.registerObjectProvider(SPApplication.DEF_ELEMENT_NAME,
                new SPApplicationBuilder(), new SPApplicationMarshaller(),
                new SPApplicationUnmarshaller());

        Configuration.registerObjectProvider(SPCountry.DEF_ELEMENT_NAME,
                new SPCountryBuilder(), new SPCountryMarshaller(),
                new SPCountryUnmarshaller());
        Configuration.registerObjectProvider(
                RequestedAttribute.DEF_ELEMENT_NAME,
                new RequestedAttributeBuilder(),
                new RequestedAttributeMarshaller(),
                new RequestedAttributeUnmarshaller());

        Configuration.registerObjectProvider(
                RequestedAttributes.DEF_ELEMENT_NAME,
                new RequestedAttributesBuilder(),
                new RequestedAttributesMarshaller(),
                new RequestedAttributesUnmarshaller());

        Configuration.registerObjectProvider(
                AuthenticationAttributes.DEF_ELEMENT_NAME,
                new AuthenticationAttributesBuilder(),
                new AuthenticationAttributesMarshaller(),
                new AuthenticationAttributesUnmarshaller());

        Configuration.registerObjectProvider(
                VIDPAuthenticationAttributes.DEF_ELEMENT_NAME,
                new VIDPAuthenticationAttributesBuilder(),
                new VIDPAuthenticationAttributesMarshaller(),
                new VIDPAuthenticationAttributesUnmarshaller());

        Configuration.registerObjectProvider(
                CitizenCountryCode.DEF_ELEMENT_NAME,
                new CitizenCountryCodeBuilder(),
                new CitizenCountryCodeMarshaller(),
                new CitizenCountryCodeUnmarshaller());

        Configuration.registerObjectProvider(
                SPID.DEF_ELEMENT_NAME,
                new SPIDBuilder(),
                new SPIDMarshaller(),
                new SPIDUnmarshaller());

        Configuration.registerObjectProvider(
                SPInformation.DEF_ELEMENT_NAME,
                new SPInformationBuilder(),
                new SPInformationMarshaller(),
                new SPInformationUnmarshaller());


    }
}
