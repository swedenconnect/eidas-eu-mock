/*
 * Copyright (c) 2016 by European Commission
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

package eu.eidas.idp;

public interface Constants {

    public static String SAMLENGINE_NAME="IdP";

    public static final String IDP_SAMLENGINE_FILE = "IdPSamlEngine.xml";
    public static final String IDP_CONFIG_REPOSITORY = "IDP_CONFIG_REPOSITORY";
    public static final String IDP_REPO_BEAN_NAME = "idpConfigRepository";


    public static String IDP_PROPERTIES="idp.properties";
    public static String IDP_METADATA_URL="idp.metadata.url";
    public static String IDP_COUNTRY="idp.country";
    public static final String IDP_METADATA_HTTPFETCH = "idp.metadata.httpfetch";
    public static final String IDP_METADATA_REPOPATH = "idp.metadata.repository.path";

    public static final String IDP_METADATA_VALIDATESIGN = "idp.metadata.validatesignature";
    public static final String IDP_METADATA_TRUSTEDDS = "idp.metadata.trusteddescriptors";

    public static String SSOS_POST_LOCATION_URL = "idp.ssos.post.location";
    public static String SSOS_REDIRECT_LOCATION_URL = "idp.ssos.redirect.location";

}
