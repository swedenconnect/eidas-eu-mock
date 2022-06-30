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

import java.io.IOException;
import java.io.OutputStream;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Specific encoder used to provide identifies the complete object handled.
 * The SHA-256 hash is over the functional data
 * @author vanegdi
 */
public class HashPatternLayoutEncoder extends PatternLayoutEncoder {
    /**
     * Replacing default outputStream
     * @param outputStream the outputstream
     * @throws java.io.IOException
     */
    @Override
    public void init(OutputStream outputStream) throws IOException {
        super.init(new HashAndCounterGenerator(outputStream, true, "SHA-256"));
    }

    @Override
    public void doEncode(ILoggingEvent event) throws IOException {
        super.doEncode(event);
    }
}