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

package eu.eidas.auth.engine.core.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * WhiteListConfigurator
 *
 * @since 1.1
 */
public final class WhiteListConfigurator {

    private static final Pattern WHITE_LIST_SPLITTER = Pattern.compile("[;,]");

    @Nonnull
    public static Set<String> getAllowedAlgorithms(@Nonnull Set<String> defaultWhiteList,
                                                            @Nonnull Set<String> allowedValues,
                                                            @Nullable String algorithmWhiteListValue) {
        if (StringUtils.isBlank(algorithmWhiteListValue)) {
            return defaultWhiteList;
        }
        String[] wlAlgorithms = WHITE_LIST_SPLITTER.split(algorithmWhiteListValue);
        if (null != wlAlgorithms && wlAlgorithms.length > 0) {
            Set<String> wlAlgorithmSet = new LinkedHashSet<>(Arrays.asList(wlAlgorithms));
            return getAllowedAlgorithms(defaultWhiteList, allowedValues, Collections.unmodifiableSet(wlAlgorithmSet));
        }
        return defaultWhiteList;
    }

    @Nonnull
    public static Set<String> getAllowedAlgorithms(@Nonnull Set<String> defaultWhiteList,
                                                   @Nonnull Set<String> allowedValues,
                                                   @Nullable Set<String> candidateValues) {
        if (CollectionUtils.isEmpty(candidateValues)) {
            return defaultWhiteList;
        }
        Set<String> allowed = new LinkedHashSet<>();
        boolean modified = false;
        for (String candidateValue : candidateValues) {
            String candidateAlgorithm = StringUtils.trimToNull(candidateValue);
            if (StringUtils.isNotBlank(candidateAlgorithm)) {
                if (allowedValues.contains(candidateAlgorithm)) {
                    allowed.add(candidateAlgorithm);
                    if (!modified && !candidateAlgorithm.equals(candidateValue)) {
                        modified = true;
                    }
                } else {
                    modified = true;
                }
            }
        }
        if (!modified) {
            return candidateValues;
        }
        if (allowed.isEmpty()) {
            return defaultWhiteList;
        }
        return Collections.unmodifiableSet(allowed);
    }

    @Nonnull
    public static Set<String> getAllowedAlgorithms(@Nonnull Set<String> defaultWhiteList,
                                                            @Nullable String algorithmWhiteListValue) {
        if (StringUtils.isBlank(algorithmWhiteListValue)) {
            return defaultWhiteList;
        }
        Set<String> allowed = new LinkedHashSet<>();
        String[] wlAlgorithms = WHITE_LIST_SPLITTER.split(algorithmWhiteListValue);

        for (String candidateValue : wlAlgorithms) {
            String candidateAlgorithm = StringUtils.trimToNull(candidateValue);
            if (StringUtils.isNotBlank(candidateAlgorithm)) {
                candidateAlgorithm = StringUtils.lowerCase(candidateAlgorithm, Locale.ENGLISH);
                allowed.add(candidateAlgorithm);
            }
        }

        if (allowed.isEmpty()) {
            return defaultWhiteList;
        }
        return Collections.unmodifiableSet(allowed);
    }

    private WhiteListConfigurator() {
    }
}
