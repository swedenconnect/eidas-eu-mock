/*
 * Copyright (c) 2019 by European Commission
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
 *
 */
package eu.eidas.auth.engine.metadata.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import eu.eidas.auth.engine.metadata.MetadataFetcherI;

@Configuration
@PropertySource("classpath:whitelisting-fetcher.properties")
public class JavaConfigurationWhitelistingMetadataFetcherTestConfiguration {

	@Bean 
	public String whitelist(){
		return "";
	}

	@Bean 
	public Boolean doWhitelist(){
		return false;
	}
	
	@Bean
	@Scope("prototype")
	public WhitelistingMetadataFetcher fetcher(String list, Boolean doWhitelisting) {
		return new WhitelistingMetadataFetcher(list, doWhitelisting) {
			@Override
			protected MetadataFetcherI getMetadataFetcher() {
				return null;
			}
		};
	}
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
