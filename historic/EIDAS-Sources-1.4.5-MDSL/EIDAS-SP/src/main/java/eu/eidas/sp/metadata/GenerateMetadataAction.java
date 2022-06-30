/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.sp.metadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.engine.configuration.dom.EncryptionKey;
import eu.eidas.auth.engine.configuration.dom.SignatureKey;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.sp.SpProtocolEngineFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.metadata.MetadataConfigParams;
import eu.eidas.auth.engine.metadata.EidasMetadata;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.sp.Constants;
import eu.eidas.sp.SPUtil;

import static eu.eidas.sp.Constants.SP_CONF;

/**
 * This Action returns an xml containing SP metadata
 *
 */
public class GenerateMetadataAction extends ActionSupport implements ServletRequestAware, ServletResponseAware {

	static final Logger logger = LoggerFactory.getLogger(GenerateMetadataAction.class.getName());
	private static final long serialVersionUID = -3995903150829760796L;
	private transient InputStream dataStream;
	Properties configs = SPUtil.loadSPConfigs();

	public String generateMetadata(){
		String metadata="invalid metadata";
		if(SPUtil.isMetadataEnabled()) {
			try {
				EidasMetadata.Generator generator = EidasMetadata.generator();
				MetadataConfigParams.Builder mcp = MetadataConfigParams.builder();
				mcp.spEngine(SpProtocolEngineFactory.getSpProtocolEngine(SP_CONF));
				mcp.entityID(configs.getProperty(Constants.SP_METADATA_URL));
				String returnUrl = configs.getProperty(Constants.SP_RETURN);
				mcp.assertionConsumerUrl(returnUrl);
				mcp.technicalContact(MetadataUtil.createTechnicalContact(configs));
				mcp.supportContact(MetadataUtil.createSupportContact(configs));
				mcp.organization(MetadataUtil.createOrganization(configs));
				mcp.signingMethods(configs == null ? null : configs.getProperty(SignatureKey.SIGNATURE_ALGORITHM_WHITE_LIST.getKey()));
				mcp.digestMethods(configs == null ? null : configs.getProperty(SignatureKey.SIGNATURE_ALGORITHM_WHITE_LIST.getKey()));
				mcp.encryptionAlgorithms(configs == null ? null : configs.getProperty(EncryptionKey.ENCRYPTION_ALGORITHM_WHITE_LIST.getKey()));
				String spType =  configs.getProperty(Constants.SP_TYPE, null);
				mcp.spType(StringUtils.isBlank(spType) ? null : spType);
				mcp.eidasProtocolVersion(configs == null ? null : configs.getProperty(EIDASValues.EIDAS_PROTOCOL_VERSION.toString()));
				mcp.eidasApplicationIdentifier(configs == null ? null : configs.getProperty(EIDASValues.EIDAS_APPLICATION_IDENTIFIER.toString()));
				generator.configParams(mcp.build());
				metadata = generator.build().getMetadata();
			}catch(EIDASSAMLEngineException see){
				logger.error("error generating metadata {}", see);
			}
		}
		dataStream = new ByteArrayInputStream(EidasStringUtil.getBytes(metadata));
		return Action.SUCCESS;
	}

	@Override
	public void setServletRequest(HttpServletRequest request) {
	}

	@Override
	public void setServletResponse(HttpServletResponse response) {
	}

	public InputStream getInputStream(){return dataStream;}
	public void setInputStream(InputStream inputStream){dataStream=inputStream;}

}