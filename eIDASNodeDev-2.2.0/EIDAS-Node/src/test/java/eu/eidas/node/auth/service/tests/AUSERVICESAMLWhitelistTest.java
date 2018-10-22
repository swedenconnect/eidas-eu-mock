package eu.eidas.node.auth.service.tests;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.eidas.node.auth.service.AUSERVICESAML;
import eu.eidas.node.auth.service.ISERVICESAMLService;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=AUSERVICESAMLTestConfig.class)
public class AUSERVICESAMLWhitelistTest {
	
	@Autowired
	ISERVICESAMLService springManagedAUSERVICESAML;
	
	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	PropertiesFactoryBean propFactory;
	
	@Before
	public void setup(){
		assertNotNull(springManagedAUSERVICESAML);
		assertNotNull(applicationContext);
		assertNotNull(propFactory);
	}

	@Test
	public void testBuildCacheOnce() {
		assertNotNull(springManagedAUSERVICESAML);
		AUSERVICESAML bean =spy((AUSERVICESAML)springManagedAUSERVICESAML);
		
		//first call builds cache
		bean.metadataWhitelist();
		
		//subsequent calls use cache but internally will not engage the PropertiesFactoryBean
		//to read the property in order to build the cache
		propFactory=spy(propFactory);
		
		bean.metadataWhitelist();
		bean.metadataWhitelist();
		bean.metadataWhitelist();
		springManagedAUSERVICESAML.whitelist("connector.metadata.location.whitelist");
		springManagedAUSERVICESAML.whitelist("connector.metadata.location.whitelist");
		
		verifyNoMoreInteractions(propFactory);
	}

}
