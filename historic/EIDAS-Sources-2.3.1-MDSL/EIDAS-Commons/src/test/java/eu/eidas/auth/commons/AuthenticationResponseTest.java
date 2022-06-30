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
package eu.eidas.auth.commons;

import java.util.Collections;

import org.joda.time.DateTime;
import org.junit.Test;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import junit.framework.Assert;

/**
 * AuthenticationResponseTest
 *
 * @since 1.1
 */
public class AuthenticationResponseTest {
    private static final AttributeDefinition<String> PERSON_IDENTIFIER =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier")
                    .friendlyName("PersonIdentifier")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .uniqueIdentifier(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "PersonIdentifierType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    @Test
    public void testAuthnResponse() throws CloneNotSupportedException {
        int QAAL = 3;
        AuthenticationResponse.Builder eidasAuthnResponse = new AuthenticationResponse.Builder();
        eidasAuthnResponse.country("UK");
        eidasAuthnResponse.id("QDS2QFD");
        eidasAuthnResponse.audienceRestriction("PUBLIC");
        eidasAuthnResponse.inResponseTo("6E97069A1754ED");
        eidasAuthnResponse.failure(false);
        eidasAuthnResponse.statusCode(EIDASStatusCode.REQUESTER_URI.toString());
        eidasAuthnResponse.subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString());
        eidasAuthnResponse.statusMessage("TEST");
        eidasAuthnResponse.notBefore(new DateTime());
        eidasAuthnResponse.notOnOrAfter(new DateTime());
        eidasAuthnResponse.ipAddress("123.123.123.123");

        eidasAuthnResponse.issuer("issuer");
        eidasAuthnResponse.subject("subject");
        eidasAuthnResponse.subjectNameIdFormat("subjectNameIdFormat");

        eidasAuthnResponse.levelOfAssurance("assuranceLevel");

        ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();
        mapBuilder.put(PERSON_IDENTIFIER, Collections.<AttributeValue<String>>emptySet());
        eidasAuthnResponse.attributes(mapBuilder.build());

        IAuthenticationResponse response = eidasAuthnResponse.build();

        Assert.assertNotNull(response.getCountry());
        Assert.assertNotNull(response.getId());
        Assert.assertNotNull(response.getAudienceRestriction());
        Assert.assertNotNull(response.getInResponseToId());
        Assert.assertNotNull(response.isFailure());
        Assert.assertNotNull(response.getStatusCode());
        Assert.assertNotNull(response.getSubStatusCode());
        Assert.assertNotNull(response.getNotBefore());
        Assert.assertNotNull(response.getNotOnOrAfter());
        Assert.assertNotNull(response.getAttributes());
        Assert.assertNotNull(response.getStatusMessage());
    }
}
