/*
 * Copyright (c) 2022 by European Commission
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

package eu.eidas.encryption.config;

import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.signature.XMLSignature;

import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class sets up the JCEName names used by OpenSaml based on the Security Providers order.
 * The first Security Provider JCE Name will be the one registered.
 */
public class EidasJCENameConfiguration {

    /**
     * If multiple providers disagree on the JCEName for a given algorithm
     * use the JCEName of the first Security Provider (configure in the java.security file)
     */
    public static void setJCENameBasedOnSecurityProviderOrder() {
        final Map<String, List<String>> possibleJCENamesPerAlgorithmUri = new HashMap<>();
        possibleJCENamesPerAlgorithmUri.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1,
                Arrays.asList("SHA256withRSASSA-PSS", "SHA256withRSAandMGF1")); // SunPKCS11 and BC respectively
        possibleJCENamesPerAlgorithmUri.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384_MGF1,
                Arrays.asList("SHA384withRSASSA-PSS", "SHA384withRSAandMGF1"));
        possibleJCENamesPerAlgorithmUri.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1,
                Arrays.asList("SHA512withRSASSA-PSS", "SHA512withRSAandMGF1"));

        final List<String> securityProviderAlgorithms = getAlgorithmsOfferedBySecurityProviders();
        registerJCENameForFirstSecurityProviderPerAlgorithmUri(possibleJCENamesPerAlgorithmUri, securityProviderAlgorithms);
    }

    private static List<String> getAlgorithmsOfferedBySecurityProviders() {
        return Arrays.stream(Security.getProviders())
                .map(Provider::getServices)
                .flatMap(Collection::stream)
                .map(Provider.Service::getAlgorithm)
                .collect(Collectors.toList());
    }

    private static void registerJCENameForFirstSecurityProviderPerAlgorithmUri(
            final Map<String, List<String>> jceNamesForAlgorithmUri,
            final List<String> securityProviderAlgorithms)
    {
        jceNamesForAlgorithmUri.forEach(
                (algorithmUri, jceNames) -> securityProviderAlgorithms
                        .stream()
                        .filter(offeredAlgorithm -> jceNames.stream().anyMatch(offeredAlgorithm::equalsIgnoreCase))
                        .findFirst()
                        .ifPresent(
                                (jceName) -> JCEMapper.register(algorithmUri, new JCEMapper.Algorithm("RSA", jceName, "Signature"))
                                )
        );
    }
}
