package member_country_specific.idp;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext applicationContext = null;

    public void setApplicationContext(ApplicationContext ctx) {
        ApplicationContextProvider.setGlobalAppContext(ctx);
    }
    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }
    private static void setGlobalAppContext(ApplicationContext ctx){
        applicationContext = ctx;
    }
}
