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
 */

package eu.eidas.encryption.support;

import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.xmlsec.encryption.EncryptedData;
import org.opensaml.xmlsec.encryption.EncryptedKey;
import org.opensaml.xmlsec.encryption.support.AbstractEncryptedKeyResolver;
import org.opensaml.xmlsec.encryption.support.EncryptedKeyResolver;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class implementation for {@link EncryptedKeyResolver}.
 */
public class FirstInlineEncryptedKeyResolver extends AbstractEncryptedKeyResolver {

    private final int limit = 1;

    public FirstInlineEncryptedKeyResolver() {
    }

    /**
     * Constructor.
     *
     * @param recipients set of recipients
     */
    public FirstInlineEncryptedKeyResolver(@Nullable Set<String> recipients) {
        super(recipients);
    }

    /**
     * Constructor.
     *
     * @param recipient the recipient
     */
    public FirstInlineEncryptedKeyResolver(@Nullable String recipient) {
        super(recipient);
    }

    protected int getMaximumAmountOfEncryptedKeys() {
        return limit;
    }

    private boolean isMaximumOfEncryptedKeysReached(@Nonnull List<EncryptedKey> keys) {
        return getMaximumAmountOfEncryptedKeys() <= keys.size();
    }

    @Override
    @Nonnull
    public Iterable<EncryptedKey> resolve(@Nonnull EncryptedData encryptedData) {
        Constraint.isNotNull(encryptedData, "EncryptedData cannot be null");
        if (encryptedData.getKeyInfo() != null) {
            Iterator<EncryptedKey> encryptedKeysIterator = encryptedData.getKeyInfo().getEncryptedKeys().iterator();

            return getMatchingEncryptedKeys(encryptedKeysIterator);
        }
        return new ArrayList();
    }

    /**
     * Create a list of encrypted keys from the given encrypted keys iterator, the list contains only
     * encrypted keys that have a recipient matching with the configured ones and will not have more
     * element than the limit configured.
     * @param encryptedKeysIterator an iterator of encrypted keys.
     * @return the list of EncryptedKeys that match with the valid configured recipients
     *     with a maximum amount of encrypted keys matching the limit configured
     */
    private List<EncryptedKey> getMatchingEncryptedKeys(Iterator<EncryptedKey> encryptedKeysIterator) {
        List<EncryptedKey> encryptedKeys = new ArrayList();
        while(encryptedKeysIterator.hasNext() && !isMaximumOfEncryptedKeysReached(encryptedKeys)) {
            EncryptedKey encryptedKey = encryptedKeysIterator.next();
            if (this.matchRecipient(encryptedKey.getRecipient())) {
                encryptedKeys.add(encryptedKey);
            }
        }
        return encryptedKeys;
    }

}
