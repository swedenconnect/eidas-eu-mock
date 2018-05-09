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

package member_country_specific.sp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class SPUtil {

    //SPUtil() {};

    static final Logger LOG = LoggerFactory.getLogger(SPUtil.class);

    public static String getConfigFilePath() {
        String filePath = (String) ApplicationContextProvider.getApplicationContext().getBean(Constants.SP_REPO_BEAN_NAME);
        String pathSeparator = File.separator;
        return filePath.endsWith(pathSeparator) ? filePath : filePath + pathSeparator;
    }

    private static Properties loadConfigs(String fileName) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(SPUtil.getConfigFilePath()+fileName));
        return properties;
    }

    public static Properties loadSPConfigs() {
        try {
            return SPUtil.loadConfigs(Constants.SP_PROPERTIES);
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.error("", e);
            throw new ApplicationSpecificServiceException("Could not load configuration file", e.getMessage());
        }
    }
}
