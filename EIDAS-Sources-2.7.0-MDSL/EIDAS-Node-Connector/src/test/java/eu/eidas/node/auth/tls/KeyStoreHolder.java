/*
 * Copyright (c) 2021 by European Commission
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

package eu.eidas.node.auth.tls;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.FileSystems;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class KeyStoreHolder implements Supplier<KeyStore> {

    /* Generated keystore file */
    private File keyStoreFile;

    private KeyStore keystore;

    private KeyStoreHolder(File keyStoreFile, KeyStore keyStore) {
        this.keyStoreFile = keyStoreFile;
        this.keystore = keyStore;
    }

    public static KeyStoreHolder build(String keyAlg, int keySize) throws Exception {
        return new KeyStoreBuilder()
                .setKeyAlg(keyAlg)
                .setKeysize(keySize)
                .setKeyStoreType(KeyStore.getDefaultType())
                .build();
    }

    public KeyStore get() {
        return this.keystore;
    }

    public String getKeyStorePath() {
        if (this.keyStoreFile != null) {
            return this.keyStoreFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * Builder class for creating keystore using keytool command.
     * The keystore will hold one single entry with a private key and a corresponding certificate.
     */
    public static class KeyStoreBuilder {

        // Password used for the key store
        private final static String KEYSTORE_PASSWORD = "changeit";

        // The Test host DNS name is used by the tested URLs and it MUST be used in the CN name
        // of the generated certificate. This is needed to avoid a missing alias exception
        private final static String DEFAULT_CN = "localhost";

        /* Key algorithm used while generating the keystore */
        private String keyAlg;

        /* Key size used while generating the keystore */
        private int keysize;

        /*Used keystore type*/
        private String keyStoreType;

        KeyStoreBuilder setKeyAlg(String keyAlg) {
            this.keyAlg = keyAlg;
            return this;
        }

        KeyStoreBuilder setKeysize(int keysize) {
            this.keysize = keysize;
            return this;
        }

        KeyStoreBuilder setKeyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
            return this;
        }

        private KeyStoreHolder build () throws Exception {
            String separator = FileSystems.getDefault().getSeparator();

            String keyStoreDir = System.getProperty("user.dir") + separator + "target";
            String keyStoreFileName = "apikeystore-" + keyAlg + "_" + keysize + '.' + keyStoreType;
            String keyStorePath = keyStoreDir + separator + keyStoreFileName;

            List<String> processArgs = Arrays.asList("keytool",
                    "-genkeypair",
                    "-keystore", keyStorePath,
                    "-deststoretype", keyStoreType,
                    "-dname", "CN=" + DEFAULT_CN,
                    "-keypass", KEYSTORE_PASSWORD,
                    "-storepass", KEYSTORE_PASSWORD,
                    "-keyalg", keyAlg,
                    "-keysize", "" + keysize,
                    "-validity", "1",
                    "-alias", DEFAULT_CN
            );

            // Launch keystore generation command and wait for its completion
            ProcessBuilder keytool = new ProcessBuilder(processArgs);
            keytool.directory(new File(keyStoreDir));
            keytool.start().waitFor();

            // Loading Keystore
            File keyStoreFile = new File(keyStorePath);
            KeyStore ks = KeyStore.getInstance(keyStoreType);
            try (FileInputStream fis = new FileInputStream(keyStoreFile.getPath())) {
                ks.load(fis, KEYSTORE_PASSWORD.toCharArray());
            }

            return new KeyStoreHolder(keyStoreFile, ks);
        }
    }
}
