/*
 * Copyright (c) 2024 by European Commission
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

package eu.eidas;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.annotation.Nonnull;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is the list of recommended Security providers for eIDAS 2.9 Encryption (Connector and Proxy)
 * Note that we have moved SunEC to last place as it does not fulfill the eIDAS Curve requirements in JDK17
 * see RecommendedEllipticCurvesTest
 */
public class RecommendedSecurityProviders {

    protected static final String[] RECOMMENDED_PROVIDERS = {
            "SUN",
            "SunRsaSign",
            "SunJSSE",
            "SunJCE",
            "SunJGSS",
            "SunSASL",
            "XMLDSig",
            "SunPCSC",
            "JdkLDAP",
            "JdkSASL",
            "SunMSCAPI",
            "BC",
            "SunEC", // to exclude, modify JSSE reliance on curve "named group: x25519"
    };

    static final List<Provider> preferredProviders = collectJavaSecurityProviderInstancesForRecommendedProviders();

    public static void setupRecommendedSecurityProviders() {
        replaceJavaSecurityProvidersIfNeeded(preferredProviders);
    }

    /**
     * Retrieves security providers from JCA according to string list of providers
     */
    private static @Nonnull List<Provider> collectJavaSecurityProviderInstancesForRecommendedProviders() {
        return Stream.of(RECOMMENDED_PROVIDERS)
                .map(providerName -> {
                    Provider provider = Security.getProvider(providerName);
                    if ("BC".equals(providerName) && provider == null) {
                        return new BouncyCastleProvider();
                    }
                    return provider;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static void replaceJavaSecurityProvidersIfNeeded(List<Provider> preferredProviders) {
        Provider[] providers = Security.getProviders();
        for (int i = 0; i < providers.length; i++) {
            if (!providers[i].equals(preferredProviders.get(i))) {
                updateSecurityProviders(preferredProviders, providers);
                break;
            }
        }
    }

    private static void updateSecurityProviders(List<Provider> preferredProviders, Provider[] providers) {
        Arrays.stream(providers).map(Provider::getName).forEach(Security::removeProvider);
        preferredProviders.forEach(Security::addProvider);
    }
}
