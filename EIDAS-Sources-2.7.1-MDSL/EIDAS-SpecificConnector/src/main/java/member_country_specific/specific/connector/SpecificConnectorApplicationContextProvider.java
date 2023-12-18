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
package member_country_specific.specific.connector;

import member_country_specific.specific.connector.communication.SpecificConnector;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Application context provider.
 *
 * @since 2.0
 */
public class SpecificConnectorApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext applicationContext = new ClassPathXmlApplicationContext("specificConnectorApplicationContext.xml");

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    //SpecificConnector sc = applicationContext.getBean(SpecificConnector.class);

    public void setApplicationContext(ApplicationContext ctx) {
        SpecificConnectorApplicationContextProvider.setGlobalAppContext(ctx);
    }

    private static void setGlobalAppContext(ApplicationContext ctx){
        applicationContext = ctx;
    }
}
