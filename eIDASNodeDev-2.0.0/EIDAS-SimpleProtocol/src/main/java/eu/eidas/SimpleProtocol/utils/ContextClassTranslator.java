/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.SimpleProtocol.utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that provides the translations between context_class values of Simple Protocol and
 * level of assurance values of light request / light response.
 */
public class ContextClassTranslator {

    private final static Map<String, String> contextClassToLevelOfAssuranceMap;

    private final static Map<String, String> levelOfAssuranceTableToContextClassMap;

    private final static Map<String, String> levelOfAssuranceAliasMap;

    static {
        Map<String,String> contextClassMap = new HashMap<>();
        contextClassMap.put("A", "http://eidas.europa.eu/LoA/low");
        contextClassMap.put("B", "http://eidas.europa.eu/LoA/low");
        contextClassMap.put("C", "http://eidas.europa.eu/LoA/substantial");
        contextClassMap.put("D", "http://eidas.europa.eu/LoA/substantial");
        contextClassMap.put("E", "http://eidas.europa.eu/LoA/high");
        contextClassToLevelOfAssuranceMap = contextClassMap;

        Map<String,String> levelOfAssuranceMap = new HashMap<>();
        levelOfAssuranceMap.put("http://eidas.europa.eu/LoA/low", "B");
        levelOfAssuranceMap.put("http://eidas.europa.eu/LoA/substantial", "D");
        levelOfAssuranceMap.put("http://eidas.europa.eu/LoA/high", "E");
        levelOfAssuranceTableToContextClassMap = levelOfAssuranceMap;

        Map<String,String> levelOfAssuranceAliasMapReal = new HashMap<>();
        levelOfAssuranceAliasMapReal.put("http://eidas.europa.eu/LoA/low", "LOW");
        levelOfAssuranceAliasMapReal.put("http://eidas.europa.eu/LoA/substantial", "SUBSTANTIAL");
        levelOfAssuranceAliasMapReal.put("http://eidas.europa.eu/LoA/high", "HIGH");
        levelOfAssuranceAliasMap = levelOfAssuranceAliasMapReal;
    }

    private ContextClassTranslator(){}


    @Nullable
    public static String getLevelOfAssurance(@Nonnull String contextClass) {
        return contextClassToLevelOfAssuranceMap.get(contextClass);
    }

    @Nullable
    public static String getContextClass(@Nonnull String levelOfAssurance) {
        return levelOfAssuranceTableToContextClassMap.get(levelOfAssurance);
    }

    @Nullable
    public static String getLevelOfAssuranceAlias(@Nonnull String levelOfAssurance) {
        return levelOfAssuranceAliasMap.get(levelOfAssurance);
    }

}
