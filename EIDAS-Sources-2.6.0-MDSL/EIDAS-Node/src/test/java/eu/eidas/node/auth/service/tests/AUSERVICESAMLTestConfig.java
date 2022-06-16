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
