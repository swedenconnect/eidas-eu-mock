/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package member_country_specific.idp;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class IDPUtil {

    private IDPUtil() {
    }

    public static Properties loadConfigs(String fileName) throws IOException {
        Properties properties = new Properties();

        File file = new File(getConfigFilePath() + fileName);

        properties.load(Files.newReader(file, StandardCharsets.UTF_8));
        return properties;
    }

    public static String getConfigFilePath() {

        String filePath = (String) ApplicationContextProvider.getApplicationContext().getBean(Constants.IDP_REPO_BEAN_NAME);
        String pathSeparator = File.separator;
        return filePath.endsWith(pathSeparator) ? filePath : filePath + pathSeparator;
    }
}
