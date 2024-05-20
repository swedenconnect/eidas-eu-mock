/*
 * Copyright (c) 2024 by European Commission
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
package eu.eidas.auth.engine.core.impl;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.configuration.dom.EncryptionConfiguration;
import eu.eidas.auth.engine.core.ProtocolCipherI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.Security;

/**
 * The base abstract class for implementations of {@link ProtocolCipherI}.
 *
 * @since 1.1
 */
public abstract class AbstractProtocolCipher implements ProtocolCipherI {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractProtocolCipher.class);
    private static final String BOUNCY_CASTLE_PROVIDER = "BC";

    private final EncryptionConfiguration encryptionConfiguration;

    protected AbstractProtocolCipher(@Nonnull EncryptionConfiguration encryptionConfiguration) {
        this.encryptionConfiguration = encryptionConfiguration;
    }

    @Nonnull
    protected ImmutableSet<String> getEncryptionAlgorithmWhiteList() {
        return this.encryptionConfiguration.getEncryptionAlgorithmWhitelist();
    }

    @Nonnull
    protected String getJcaProviderNameWithDefault(@Nullable String jcaProviderName) {
        if (StringUtils.isBlank(jcaProviderName)) {
            if (Security.getProvider(BOUNCY_CASTLE_PROVIDER) != null) {
                jcaProviderName = BOUNCY_CASTLE_PROVIDER;
            }
        }
        return jcaProviderName;
    }

    @Override
    public EncryptionConfiguration getEncryptionConfiguration() {
        return null;
    }

    @Override
    public boolean isCheckedValidityPeriod() {
        return this.encryptionConfiguration.isCheckedValidityPeriod();
    }

    @Override
    public boolean isDisallowedSelfSignedCertificate() {
        return this.encryptionConfiguration.isDisallowedSelfSignedCertificate();
    }

    @Override
    public boolean isResponseEncryptionMandatory() {
        return this.encryptionConfiguration.isResponseEncryptionMandatory();
    }

    @Override
    public boolean isAssertionEncryptWithKey() {
        return this.encryptionConfiguration.isAssertionEncryptWithKey();
    }

    protected void validateEncryptionAlgorithm(@Nonnull String encryptionAlgorithm) throws EIDASSAMLEngineException {
        if (StringUtils.isBlank(encryptionAlgorithm) || !getEncryptionAlgorithmWhiteList().contains(
                encryptionAlgorithm.trim())) {
            LOG.error("Invalid encryption algorithm: \"" + encryptionAlgorithm
                              + "\" (allowed algorithms in white list are: " + getEncryptionAlgorithmWhiteList() + ")");
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_ENCRYPTION_ALGORITHM,
                    "Invalid encryption algorithm: \"" + encryptionAlgorithm
                    + "\" (allowed algorithms in white list are: " + getEncryptionAlgorithmWhiteList() + ")");
        }
    }
}
