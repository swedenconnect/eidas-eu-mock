/*
 * Copyright (c) 2020 by European Commission
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

package member_country_specific.idp.actions;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import member_country_specific.idp.Constants;
import member_country_specific.idp.IDPUtil;
import member_country_specific.idp.LevelOfAssurance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class AuthenticateCitizenAction extends ActionSupport{

	public static final String INITIALIZE = "initialize";
	private static final long serialVersionUID = -7243683543548722148L;
    private static final String NON_NOTIFIED_LOA = "nonNotifiedLoA";

    private Properties idpProperties;
	private List<LevelOfAssurance> nonNotifiedLoAs;
	private List<String> nameIDFormats;

    public String initialize() throws IOException {
        idpProperties = IDPUtil.loadConfigs(Constants.IDP_PROPERTIES);
        nonNotifiedLoAs = getConfiguredNonNotifiedLoAs();
        nameIDFormats = createNameIDFormatsList();
        return INITIALIZE;
    }

    @Override
    public String execute() {
        return Action.SUCCESS;
    }

    private List<LevelOfAssurance> getConfiguredNonNotifiedLoAs() {
        List<LevelOfAssurance> configuredNonNotifiedLoAs = new ArrayList<>();
        int i = 1;
        LevelOfAssurance nonNotifiedLoA;
        do {
            nonNotifiedLoA = null;
            String name = idpProperties.getProperty(NON_NOTIFIED_LOA + i + ".name");
            String value = idpProperties.getProperty(NON_NOTIFIED_LOA + i++ + ".value");
            if (value != null && !value.isEmpty()) {
                nonNotifiedLoA = new LevelOfAssurance(name, value);
                configuredNonNotifiedLoAs.add(nonNotifiedLoA);
            }
        } while (nonNotifiedLoA != null);
        return configuredNonNotifiedLoAs;
    }

    private List<String> createNameIDFormatsList() {
        return Arrays.stream(SamlNameIdFormat.values())
                .map((s) -> s.getNameIdFormat().split(":"))
                .map((array) -> array[array.length-1])
                .collect(Collectors.toList());
    }


    public List<LevelOfAssurance> getNonNotifiedLoAs() {
        return nonNotifiedLoAs == null ? new ArrayList<>() : nonNotifiedLoAs;
    }

    public List<String> getNameIDFormats() {
        return nameIDFormats;
    }

}
