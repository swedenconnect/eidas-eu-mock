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
package eu.eidas.tests;

import eu.eidas.auth.commons.CitizenConsent;
import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;

public class CitizenConsentTestCase {
    private static final String CITIZEN_CONSENT_TEST = "Mandatory attributes: mandatoryAttr1;mandatoryAttr2;mandatoryAttr3; Optional attributes: optionalAttr1;optionalAttr1;";
    @Test
    public void testCitizenConsent() {
        CitizenConsent citizenConsent = new CitizenConsent();

        ArrayList<String> mandatoryList = new ArrayList<String>();
        mandatoryList.add("mandatoryAttr1");
        mandatoryList.add("mandatoryAttr2");
        citizenConsent.setMandatoryList(mandatoryList);
        citizenConsent.setMandatoryAttribute("mandatoryAttr3");

        ArrayList<String> optionalList = new ArrayList<String>();
        optionalList.add("optionalAttr1");
        citizenConsent.setOptionalList(optionalList);
        citizenConsent.setOptionalAttribute("optionalAttr1");

        Assert.assertEquals(mandatoryList, citizenConsent.getMandatoryList());
        Assert.assertEquals(optionalList, citizenConsent.getOptionalList());

        Assert.assertEquals(citizenConsent.toString(), CITIZEN_CONSENT_TEST);
    }
}
