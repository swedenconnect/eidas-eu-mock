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

package eu.eidas.auth.commons.protocol.eidas.impl;

import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LevelOfAssuranceTestConstants
 * Class holding static values for reuse in tests concerning {@link NotifiedLevelOfAssurance} , {@link LevelOfAssurance} and {@link LevelOfAssuranceComparison}
 */
public class LevelOfAssuranceTestConstants {

    static final String LEVEL_OF_ASSURANCE_NOTIFIED_LOW = "http://eidas.europa.eu/LoA/low";
    static final String LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL = "http://eidas.europa.eu/LoA/substantial";
    static final String LEVEL_OF_ASSURANCE_NOTIFIED_HIGH = "http://eidas.europa.eu/LoA/high";

    static final String NON_NOTIFIED_LOA_ALPHA = "http://service.memberstate.ms/NotNotified/LoA/low";
    static final String NON_NOTIFIED_LOA_BETA = "http://non.eidas.eu/NotNotified/LoA/1";
    static final String NON_NOTIFIED_LOA_HIGH = "http://abc.europa.eu/LoA/high";

    static final NotifiedLevelOfAssurance EIDAS_PROTOCOL_NOTIFIED_LOA_LOW = NotifiedLevelOfAssurance.LOW;
    static final NotifiedLevelOfAssurance EIDAS_PROTOCOL_NOTIFIED_LOA_SUBSTANTIAL = NotifiedLevelOfAssurance.SUBSTANTIAL;
    static final NotifiedLevelOfAssurance EIDAS_PROTOCOL_NOTIFIED_LOA_HIGH = NotifiedLevelOfAssurance.HIGH;

    static final LevelOfAssurance LIGHT_IMPL_NOTIFIED_LOA_LOW = LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_LOW);
    static final LevelOfAssurance LIGHT_IMPL_NOTIFIED_LOA_SUBSTANTIAL = LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_SUBSTANTIAL);
    static final LevelOfAssurance LIGHT_IMPL_NOTIFIED_LOA_HIGH = LevelOfAssurance.build(LEVEL_OF_ASSURANCE_NOTIFIED_HIGH);

    static final String LEVEL_OF_ASSURANCE_COMPARISON_EXACT_STRING = LevelOfAssuranceComparison.EXACT.stringValue();
    static final String LEVEL_OF_ASSURANCE_COMPARISON_MINIMUM_STRING = LevelOfAssuranceComparison.MINIMUM.stringValue();

    static final List<LevelOfAssurance> NON_NOTIFIED_LEVELS_OF_ASSURANCE = Arrays.asList(
            LevelOfAssurance.build(NON_NOTIFIED_LOA_ALPHA),
            LevelOfAssurance.build(NON_NOTIFIED_LOA_BETA)
    );

    static final List<LevelOfAssurance> NON_NOTIFIED_LEVELS_OF_ASSURANCE_HIGH = Arrays.asList(
            LevelOfAssurance.build(NON_NOTIFIED_LOA_HIGH),
            LevelOfAssurance.build(NON_NOTIFIED_LOA_ALPHA)
    );

    static final Map<String, List<Object>> lookup = new HashMap<>();

    static {
        lookup.put("levelOfAssurance", Arrays.asList(
                EIDAS_PROTOCOL_NOTIFIED_LOA_LOW,
                EIDAS_PROTOCOL_NOTIFIED_LOA_SUBSTANTIAL,
                EIDAS_PROTOCOL_NOTIFIED_LOA_HIGH
        ));
        lookup.put("nonNotifiedLevelsOfAssurance", Arrays.asList(
                NON_NOTIFIED_LEVELS_OF_ASSURANCE,
                NON_NOTIFIED_LEVELS_OF_ASSURANCE_HIGH
        ));
        lookup.put("levelOfAssuranceComparison", Arrays.asList(
                LevelOfAssuranceComparison.EXACT,
                LevelOfAssuranceComparison.MINIMUM
        ));
    }
}
