/*
 * Copyright (c) 2022 by European Commission
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
package eu.eidas.node.auth.service.tests;

import java.util.Arrays;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.eidas.node.auth.service.AUSERVICESAML;
import eu.eidas.node.auth.service.ISERVICESAMLService;

@Configuration
@EnableCaching(proxyTargetClass=true)
public class AUSERVICESAMLTestConfig {

	@Bean
	public ISERVICESAMLService springManagedAUSERVICESAML(){
		return new AUSERVICESAML();
	}
	
	@Bean
	public PropertiesFactoryBean propsFactory(){
		return new PropertiesFactoryBean();
	}
	
	@Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
          new ConcurrentMapCache("whitelist")));
        return cacheManager;
    }
	
}
