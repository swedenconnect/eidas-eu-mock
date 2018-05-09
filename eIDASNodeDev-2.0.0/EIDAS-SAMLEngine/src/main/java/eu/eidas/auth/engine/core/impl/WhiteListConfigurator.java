/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.auth.engine.core.impl;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * WhiteListConfigurator
 *
 * @since 1.1
 */
public final class WhiteListConfigurator {

    private static final Pattern WHITE_LIST_SPLITTER = Pattern.compile("[;,]");

    @Nonnull
    public static ImmutableSet<String> getAllowedAlgorithms(@Nonnull ImmutableSet<String> defaultWhiteList,
                                                            @Nonnull ImmutableSet<String> allowedValues,
                                                            @Nullable String algorithmWhiteListValue) {
        if (StringUtils.isBlank(algorithmWhiteListValue)) {
            return defaultWhiteList;
        }
        String[] wlAlgorithms = WHITE_LIST_SPLITTER.split(algorithmWhiteListValue);
        if (null != wlAlgorithms && wlAlgorithms.length > 0) {
            return getAllowedAlgorithms(defaultWhiteList, allowedValues, ImmutableSet.<String>copyOf(wlAlgorithms));
        }
        return defaultWhiteList;
    }

    @Nonnull
    public static ImmutableSet<String> getAllowedAlgorithms(@Nonnull ImmutableSet<String> defaultWhiteList,
                                                            @Nonnull ImmutableSet<String> allowedValues,
                                                            @Nullable ImmutableSet<String> candidateValues) {
        if (CollectionUtils.isEmpty(candidateValues)) {
            return defaultWhiteList;
        }
        ImmutableSet.Builder<String> allowed = ImmutableSet.builder();
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
        ImmutableSet<String> set = allowed.build();
        if (set.isEmpty()) {
            return defaultWhiteList;
        }
        return set;
    }

    @Nonnull
    public static ImmutableSet<String> getAllowedAlgorithms(@Nonnull ImmutableSet<String> defaultWhiteList,
                                                            @Nullable String algorithmWhiteListValue) {
        if (StringUtils.isBlank(algorithmWhiteListValue)) {
            return defaultWhiteList;
        }
        ImmutableSet.Builder<String> allowed = ImmutableSet.builder();
        String[] wlAlgorithms = WHITE_LIST_SPLITTER.split(algorithmWhiteListValue);

        for (String candidateValue : wlAlgorithms) {
            String candidateAlgorithm = StringUtils.trimToNull(candidateValue);
            if (StringUtils.isNotBlank(candidateAlgorithm)) {
                candidateAlgorithm = StringUtils.lowerCase(candidateAlgorithm, Locale.ENGLISH);
                allowed.add(candidateAlgorithm);
            }
        }

        ImmutableSet<String> set = allowed.build();
        if (set.isEmpty()) { return defaultWhiteList; }
        return set;
    }

    private WhiteListConfigurator() {
    }
}
