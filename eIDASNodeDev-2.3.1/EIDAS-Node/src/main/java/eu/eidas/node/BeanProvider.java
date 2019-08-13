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
package eu.eidas.node;

import org.springframework.context.ApplicationContext;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;


public class BeanProvider implements ApplicationListener<ContextRefreshedEvent> {

    private static volatile ApplicationContext CONTEXT = null;

    public static <T> T getBean(Class<T> type) {
        return type.cast(CONTEXT.getBean(type));
    }

    public static <T> T getBean(Class<T> type, String name) {
        return type.cast(CONTEXT.getBean(name));
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        BeanProvider.CONTEXT = contextRefreshedEvent.getApplicationContext();
    }
}
