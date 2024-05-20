/*
 * Copyright (c) 2023 by European Commission
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

package eu.eidas.logging.utils.logger;

import eu.eidas.logging.IMessageLogger;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import org.slf4j.Logger;

import javax.cache.Cache;

public abstract class LightMessageLoggerBuilder<B extends  LightMessageLoggerBuilder, L extends IMessageLogger> {

    private Cache<String, String> flowIdCache;
    private SpecificCommunicationLoggingService specificCommunicationLoggingService;
    private MessageLoggerUtils messageLoggerUtils;
    private Logger fullLogger;

    public B withFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
        return getThis();
    }

    public B forFlowIdCache(Mockable <Cache<String, String>> mocking) {
        mocking.run(this.flowIdCache);
        return getThis();
    }

    public B withSpecificCommunicationLoggingService(SpecificCommunicationLoggingService specificCommunicationLoggingService ) {
        this.specificCommunicationLoggingService = specificCommunicationLoggingService;
        return getThis();
    }

    public B forSpecificCommunicationLoggingService(MockableWithError <SpecificCommunicationLoggingService> mocking) throws Exception {
        mocking.run(this.specificCommunicationLoggingService);
        return getThis();
    }

    public B withMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
        return getThis();
    }

    public B forMessageLoggerUtils(Mockable <MessageLoggerUtils> mocking) {
        mocking.run(this.messageLoggerUtils);
        return getThis();
    }

    public B withFullLogger(Logger fullLogger) {
        this.fullLogger = fullLogger;
        return getThis();
    }

    public abstract L build() throws IllegalAccessException;

    public interface Mockable<T> {
        public abstract void run(T mock);
    }

    public interface MockableWithError<T> {
        public abstract void run(T mock) throws Exception;
    }

    public Cache<String, String> getFlowIdCache() {
        return flowIdCache;
    }

    public SpecificCommunicationLoggingService getSpecificCommunicationLoggingService() {
        return specificCommunicationLoggingService;
    }

    public MessageLoggerUtils getMessageLoggerUtils() {
        return messageLoggerUtils;
    }

    public Logger getFullLogger() {
        return fullLogger;
    }

    protected abstract B getThis();
}

