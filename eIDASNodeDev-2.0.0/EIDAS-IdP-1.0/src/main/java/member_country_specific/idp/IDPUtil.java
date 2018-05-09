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
