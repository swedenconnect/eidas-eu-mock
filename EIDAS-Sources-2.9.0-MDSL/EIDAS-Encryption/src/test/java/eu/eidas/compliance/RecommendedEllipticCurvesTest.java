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

package eu.eidas.compliance;

import eu.eidas.RecommendedSecurityProviders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Collection;

/**
 * eIDAS Cryptographic Requirements v1.4
 * <a href="https://ec.europa.eu/digital-building-blocks/sites/display/DIGITAL/eIDAS+eID+Profile?preview=/467109280/704841745/eIDAS%20Cryptographic%20Requirement%20v.1.4_final.pdf">...</a>
 *
 * 3.4 Elliptic Curves,
 * MUST be only named curves
 * RECOMMENDED to support the following named curves:
 */
@RunWith(value = Parameterized.class)
public class RecommendedEllipticCurvesTest {

    @Parameterized.Parameter(value = 0)
    public String curveName;

    @Parameterized.Parameter(value = 1)
    public String curveAlias;

    @Before
    public void setUp() throws Exception {
        RecommendedSecurityProviders.setupRecommendedSecurityProviders();
    }

    @Parameterized.Parameters(name = " with curve {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                //  Name                JCA Alias
                {"BrainpoolP256r1",     "brainpoolP256r1"   },
                {"BrainpoolP384r1",     "brainpoolP384r1"   },
                {"BrainpoolP512r1",     "brainpoolP512r1"   },
                {"NIST Curve P-256",    "secp256r1"         },
                {"NIST Curve P-384",    "secp384r1"         },
                {"NIST Curve P-512",    "secp521r1"         },

        });
    }
    @Test
    public void fetchCurve() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator.getInstance("EC").initialize(new ECGenParameterSpec(curveAlias), new SecureRandom());
    }
}
