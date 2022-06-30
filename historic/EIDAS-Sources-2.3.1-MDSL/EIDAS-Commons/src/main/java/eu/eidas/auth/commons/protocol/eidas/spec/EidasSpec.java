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
package eu.eidas.auth.commons.protocol.eidas.spec;

import org.joda.time.DateTime;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.protocol.eidas.impl.Gender;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddress;

/**
 * Both NaturalPerson and LegalPerson from the eIDAS Specification.
 * <p>
 * This class contains all the attribute definitions specified in eIDAS.
 *
 * @since 2016-05-0
 */
public final class EidasSpec {

    public static final class Definitions {

        // Natural

        public static final AttributeDefinition<String> PERSON_IDENTIFIER = NaturalPersonSpec.Definitions.PERSON_IDENTIFIER;

        public static final AttributeDefinition<String> CURRENT_FAMILY_NAME = NaturalPersonSpec.Definitions.CURRENT_FAMILY_NAME;

        public static final AttributeDefinition<String> CURRENT_GIVEN_NAME = NaturalPersonSpec.Definitions.CURRENT_GIVEN_NAME;

        public static final AttributeDefinition<DateTime> DATE_OF_BIRTH = NaturalPersonSpec.Definitions.DATE_OF_BIRTH;

        public static final AttributeDefinition<String> BIRTH_NAME = NaturalPersonSpec.Definitions.BIRTH_NAME;

        public static final AttributeDefinition<String> PLACE_OF_BIRTH = NaturalPersonSpec.Definitions.PLACE_OF_BIRTH;

        public static final AttributeDefinition<PostalAddress> CURRENT_ADDRESS = NaturalPersonSpec.Definitions.CURRENT_ADDRESS;

        public static final AttributeDefinition<Gender> GENDER = NaturalPersonSpec.Definitions.GENDER;

        // Legal

        public static final AttributeDefinition<String> LEGAL_PERSON_IDENTIFIER =
                LegalPersonSpec.Definitions.LEGAL_PERSON_IDENTIFIER;

        public static final AttributeDefinition<String> LEGAL_NAME = LegalPersonSpec.Definitions.LEGAL_NAME;

        public static final AttributeDefinition<PostalAddress> LEGAL_PERSON_ADDRESS = LegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS;

        public static final AttributeDefinition<String> VAT_REGISTRATION_NUMBER = LegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER;

        public static final AttributeDefinition<String> TAX_REFERENCE = LegalPersonSpec.Definitions.TAX_REFERENCE;

        public static final AttributeDefinition<String> D_2012_17_EU_IDENTIFIER =
                LegalPersonSpec.Definitions.D_2012_17_EU_IDENTIFIER;

        public static final AttributeDefinition<String> LEI = LegalPersonSpec.Definitions.LEI;

        public static final AttributeDefinition<String> EORI = LegalPersonSpec.Definitions.EORI;

        public static final AttributeDefinition<String> SEED = LegalPersonSpec.Definitions.SEED;

        public static final AttributeDefinition<String> SIC = LegalPersonSpec.Definitions.SIC;

        // Representative Natural

        public static final AttributeDefinition<String> REPV_PERSON_IDENTIFIER = RepresentativeNaturalPersonSpec.Definitions.PERSON_IDENTIFIER;

        public static final AttributeDefinition<String> REPV_CURRENT_FAMILY_NAME = RepresentativeNaturalPersonSpec.Definitions.CURRENT_FAMILY_NAME;

        public static final AttributeDefinition<String> REPV_CURRENT_GIVEN_NAME = RepresentativeNaturalPersonSpec.Definitions.CURRENT_GIVEN_NAME;

        public static final AttributeDefinition<DateTime> REPV_DATE_OF_BIRTH = RepresentativeNaturalPersonSpec.Definitions.DATE_OF_BIRTH;

        public static final AttributeDefinition<String> REPV_BIRTH_NAME = RepresentativeNaturalPersonSpec.Definitions.BIRTH_NAME;

        public static final AttributeDefinition<String> REPV_PLACE_OF_BIRTH = RepresentativeNaturalPersonSpec.Definitions.PLACE_OF_BIRTH;

        public static final AttributeDefinition<PostalAddress> REPV_CURRENT_ADDRESS = RepresentativeNaturalPersonSpec.Definitions.CURRENT_ADDRESS;

        public static final AttributeDefinition<Gender> REPV_GENDER = RepresentativeNaturalPersonSpec.Definitions.GENDER;

        // Representative Legal

        public static final AttributeDefinition<String> REPV_LEGAL_PERSON_IDENTIFIER =
                LegalPersonSpec.Definitions.LEGAL_PERSON_IDENTIFIER;

        public static final AttributeDefinition<String> REPV_LEGAL_NAME = RepresentativeLegalPersonSpec.Definitions.LEGAL_NAME;

        public static final AttributeDefinition<PostalAddress> REPV_LEGAL_PERSON_ADDRESS = RepresentativeLegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS;

        public static final AttributeDefinition<String> REPV_VAT_REGISTRATION_NUMBER = RepresentativeLegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER;

        public static final AttributeDefinition<String> REPV_TAX_REFERENCE = RepresentativeLegalPersonSpec.Definitions.TAX_REFERENCE;

        public static final AttributeDefinition<String> REPV_D_2012_17_EU_IDENTIFIER =
                RepresentativeLegalPersonSpec.Definitions.D_2012_17_EU_IDENTIFIER;

        public static final AttributeDefinition<String> REPV_LEI = RepresentativeLegalPersonSpec.Definitions.LEI;

        public static final AttributeDefinition<String> REPV_EORI = RepresentativeLegalPersonSpec.Definitions.EORI;

        public static final AttributeDefinition<String> REPV_SEED = RepresentativeLegalPersonSpec.Definitions.SEED;

        public static final AttributeDefinition<String> REPV_SIC = RepresentativeLegalPersonSpec.Definitions.SIC;


        private Definitions() {
        }
    }

    public static final AttributeRegistry REGISTRY =
            AttributeRegistries.copyOf(NaturalPersonSpec.REGISTRY, LegalPersonSpec.REGISTRY, RepresentativeNaturalPersonSpec.REGISTRY, RepresentativeLegalPersonSpec.REGISTRY);

    private EidasSpec() {
    }
}
