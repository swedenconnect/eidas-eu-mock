package member_country_specific.sp;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext applicationContext = null;
    public void setApplicationContext(ApplicationContext ctx) {
        ApplicationContextProvider.setGlobalAppCtxt(ctx);
    }
    public static ApplicationContext getApplicationContext(){
        return applicationContext;
    }
    private static void setGlobalAppCtxt(ApplicationContext ctx){
        applicationContext=ctx;
    }
}
