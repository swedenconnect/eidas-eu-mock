/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
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

package eu.eidas.node.logging.integrity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;

/**
 * This file checker states the latest part of the log file is composed by [xxx]
 * where xxx is the hash value of the rest of the line salted with the previous line hash
 * @author vanegdi
 */
public class HashFileChecker {

    private static final Logger LOG = LoggerFactory.getLogger(HashFileChecker.class.getName());

    private HashFileChecker(){
    }
    /**
     * Check a log file to control if the hash are consistent.
     * @param is the input stream
     * @param hashAlgorithm a ash algorithm
     * @return true if the hashes are correct
     * @throws IllegalStateException in case of wrong format line
     * @throws IOException in cas of error
     */
    public static boolean check(InputStream is, final String hashAlgorithm) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String line = bufferedReader.readLine();
        String previousHash=null;
        HashAndCounterGenerator hashAndCounterGenerator;
        long cpt = 0;
        while (line != null) {
            if (line.endsWith("]")) {
                cpt++;
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                //int posBeginHash =
                int posBeginHashSeparator = line.lastIndexOf('[');
                int posEndHashSeparator = line.lastIndexOf(']');
                if (posBeginHashSeparator<1){
                    throw new IllegalStateException("The log line doesn't contain any text");
                }
                if (posEndHashSeparator<posBeginHashSeparator){
                    throw new IllegalStateException("Malformed hash");
                }
                String hashFromFile = line.substring(posBeginHashSeparator + 1, posEndHashSeparator);
                if (hashFromFile == null || hashFromFile.length()<1){
                    throw new IllegalStateException("Empty hash");
                }
                String lineWithoutHash = line.substring(0,posBeginHashSeparator-1);
                // Init the hash with previous hash salting
                if (previousHash != null) {
                    hashAndCounterGenerator = new HashAndCounterGenerator(byteArrayOutputStream, previousHash, false, hashAlgorithm);
                } else {
                    hashAndCounterGenerator = new HashAndCounterGenerator(byteArrayOutputStream, false, hashAlgorithm);
                }

                hashAndCounterGenerator.write(lineWithoutHash.getBytes(Charset.forName("UTF-8")));
                hashAndCounterGenerator.write(HashAndCounterGenerator.NEWLINE); // Triggers the computation of the hash
                hashAndCounterGenerator.flush();
                if (!hashAndCounterGenerator.getLastDigest().equals(hashFromFile)) {
                    LOG.info("ERROR : Error while validating file hashes at log entry {}, get [{}], expecting [{}]", cpt, hashFromFile, hashAndCounterGenerator.getLastDigest());
                    return false;
                }
                previousHash = hashAndCounterGenerator.getLastDigest();
                line = bufferedReader.readLine();
            } else {
                    throw new IllegalStateException("Log line is not finished with as hash value");
            }
        }
        return true;
    }
}
