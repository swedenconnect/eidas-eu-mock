/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.eidas.auth.engine.core.uumds;

import java.security.cert.X509Certificate;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.engine.core.ExtensionProcessorI;
import eu.eidas.auth.engine.core.stork.RequestedAttribute;
import eu.eidas.auth.engine.core.stork.RequestedAttributes;
import eu.eidas.auth.engine.core.stork.StorkExtensionProcessor;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

public final class UumdsExtensionProcessor extends StorkExtensionProcessor implements ExtensionProcessorI {

	/**
	 * The Constant LOG.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(UumdsExtensionProcessor.class.getName());

	public UumdsExtensionProcessor(AttributeRegistry storkAttributeRegistry,
			AttributeRegistry additionalAttributeRegistry) {
		super(storkAttributeRegistry, additionalAttributeRegistry);
	}

	public UumdsExtensionProcessor(String storkAttributesFileName, String additionalAttributesFileName, String defaultPath) {
		super(storkAttributesFileName, additionalAttributesFileName, defaultPath);
	}

	@Override
	protected void fillRequestedAttributes(IAuthenticationRequest request, RequestedAttributes reqAttributes)
			throws EIDASSAMLEngineException {

		LOG.trace("SAML Engine configuration properties load.");
		for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends eu.eidas.auth.commons.attribute.AttributeValue<?>>> entry : request
          .getRequestedAttributes()
          .getAttributeMap()
          .entrySet()) {
      AttributeDefinition<?> attributeDefinition = entry.getKey();

            AttributeDefinition<?> requestedAttribute = getRequestedAttribute(attributeDefinition);

			// Verify if the attribute name exists.
			if (null == requestedAttribute) {
				LOG.trace("Attribute name: {} was not found.", attributeDefinition.getNameUri());

				requestedAttribute = attributeDefinition;
			} else {
				LOG.trace("Generate requested attribute: " + requestedAttribute);
			}

			AttributeValueMarshaller<?> attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();
   ImmutableSet.Builder<String> builder = ImmutableSet.builder();
   for (final eu.eidas.auth.commons.attribute.AttributeValue<?> attributeValue : entry.getValue()) {
       try {
           String marshalledValue = attributeValueMarshaller.marshal((eu.eidas.auth.commons.attribute.AttributeValue)attributeValue);
           builder.add(marshalledValue);
       } catch (AttributeValueMarshallingException e) {
           LOG.error("Illegal attribute value: " + e, e);
           throw new EIDASSAMLEngineException(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
				   EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode(),
				   e);
       }
   }

            RequestedAttribute requestedAttr = generateReqAuthnAttributeSimple(requestedAttribute, builder.build());

			// Add requested attribute.
			reqAttributes.getAttributes().add(requestedAttr);
		}
		//addUumdsVersion
	}
	// private void addUumdsVersion(EidasAuthenticationRequest request, AuthnRequest
	// authnRequestAux)
	// throws EIDASSAMLEngineException {
	// if (request == null || StringUtils.isEmpty(request.getEidasLoA())) {
	// return;
	// }
	// if (LevelOfAssurance.getLevel(request.getEidasLoA()) == null) {
	// throw new
	// EIDASSAMLEngineException(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.errorCode(),
	// EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.errorMessage());
	// }
	// RequestedAuthnContext authnContext =
	// (RequestedAuthnContext)
	// SAMLEngineUtils.createSamlObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
	// if (authnContext == null) {
	// throw new EIDASSAMLEngineException("Unable to create SAML Object
	// DEFAULT_ELEMENT_NAME");
	// }
	// authnContext.setComparison(AuthnContextComparisonTypeEnumeration.MINIMUM);
	// AuthnContextClassRef authnContextClassRef =
	// (AuthnContextClassRef)
	// SAMLEngineUtils.createSamlObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
	// authnContextClassRef.setAuthnContextClassRef(request.getEidasLoA());
	// authnContext.getAuthnContextClassRefs().add(authnContextClassRef);
	// authnRequestAux.setRequestedAuthnContext(authnContext);
	//
	// }

	@Nullable
	@Override
	public X509Certificate getEncryptionCertificate(@Nullable String requestIssuer) throws EIDASSAMLEngineException {
		return null;
	}

	@Nullable
	@Override
	public X509Certificate getRequestSignatureCertificate(@Nonnull String issuer) throws EIDASSAMLEngineException {
		return null;
	}

	@Nullable
 @Override
 public X509Certificate getResponseSignatureCertificate(@Nonnull String issuer) throws EIDASSAMLEngineException {
     return null;
 }

	@Override
	public boolean isAcceptableHttpRequest(IAuthenticationRequest authnRequest, String httpMethod)
			throws EIDASSAMLEngineException {
		return true;
	}
}
