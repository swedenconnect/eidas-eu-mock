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

package eu.eidas.node.logging.messages;

import eu.eidas.node.logging.LoggingConstants;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Abstract Model class for logging incoming and outgoing requests/responses from/to
 * of the Eidas Proxy Service and Eidas Connector.
 *
 * @since 2.5
 */
public abstract class MessageLog {

    /** Defines MessageLog constants used for formatting */
    protected static final String NEW_LINE = "\n";

    private final Tag timestamp = new Tag("Timestamp");
    private final Tag opType =  new Tag("OpType");
    private final Tag nodeId = new Tag("NodeId");
    private final Tag origin = new Tag("Origin");
    private final Tag destination = new Tag("Destination");
    private final Tag flowId = new Tag("flowId");
    private final Tag msgId = new Tag("msgId");
    private final Tag msgHash = new Tag("msgHash");

    /**
     * MessageLog should only be build through the builder
     */
    protected MessageLog() {}

    /**
     * Method that return the list of tags common to all MessageLog.
     * The common fields are: timestamp, opType, nodeId, origin, destination,
     *  flowId, msgId, msgHash.
     * If there are other tags that should be present in the message log,
     * this tags should be put in the list of the {@link MessageLog#getSpecificTags()} methods.
     *
     * @return the list of common tags.
     */
    protected final List<Tag> getCommonTags() {
        List<Tag> commonTags = new ArrayList<>();
        commonTags.add(timestamp);
        commonTags.add(opType);
        commonTags.add(nodeId);
        commonTags.add(origin);
        commonTags.add(destination);
        commonTags.add(flowId);
        commonTags.add(msgId);
        commonTags.add(msgHash);
        return commonTags;
    }

    /**
     * Method that return the list of tags specifics to this MessageLog type.
     * The list cannot be null.
     * @return the list of specific tags.
     */
    @Nonnull
    protected abstract List<Tag> getSpecificTags();

    private List<Tag> getAllTags() {
        List<Tag> allTags = new ArrayList<>();
        allTags.addAll(getCommonTags());
        allTags.addAll(getSpecificTags());
        return allTags;
    }

    /**
     * Formats the MessageLog output.
     * Print all the tags information of the MessageLog.
     * Each tag is display on a different line.
     *
     * For the tags to be displayed:
     * @see #getCommonTags
     * @see #getSpecificTags
     * @return the formatted MessageLog output.
     */
    @Override
    public String toString() {
        String messageLog = "";
        for (Tag tag: getAllTags()) {
            messageLog += NEW_LINE + tag;
        }
        return messageLog;
    }

    /**
     * Model for a tag composing the message.
     */
    protected static class Tag {
        protected static final int TITLE_SIZE = 14;
        protected static final String TAG_FORMAT = "%-" + TITLE_SIZE + "s%s,";

        private String title;
        private String value;

        public Tag(String title) {
            this.title = title;
            this.value = LoggingConstants.UNDEFINED;
        }

        public void setValue(String value) {
            if (null != value) {
                this.value = value;
            }
        }

        /**
         * Method to format a tag output.
         * @return the formatted tag as String.
         */
        @Override
        public String toString() {
            return String.format(TAG_FORMAT, title, value);
        }
    }

    /**
     * Abstract builder to be use to create the MessageLog
     * @param <T> This describe the type of MessageLog.
     */
    public static abstract class Builder<T extends MessageLog> {
        // MessageLog Instance supplier
        private Supplier<T> supplier;

        private String timestamp;
        private String opType;
        private String nodeId;
        private String origin;
        private String destination;
        private String flowId;
        private String msgId;
        private String msgHash;

        /**
         * Initialize a MessageLogBuilder.
         * @param messageLogSupplier the MessageLog constructor
         */
        public Builder(Supplier<T> messageLogSupplier) {
            this.supplier = messageLogSupplier;
            this.timestamp = OffsetDateTime.now(ZoneOffset.UTC).toString();
        }

        public Builder setOpType(String opType) {
            this.opType = opType;
            return this;
        }

        public Builder setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder setOrigin(String origin) {
            this.origin = origin;
            return this;
        }

        public Builder setDestination(String destination) {
            this.destination = destination;
            return this;
        }

        public Builder setFlowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public Builder setMsgId(String msgId) {
            this.msgId = msgId;
            return this;
        }

        public Builder setMsgHash(String msgHash) {
            this.msgHash = msgHash;
            return this;
        }

        /**
         * Method that set the value of the common tags of the MessageLog to build.
         * @param messageLog the messageLog to be updated.
         */
        protected final void buildCommonTags(MessageLog messageLog) {
            messageLog.timestamp.setValue(this.timestamp);
            messageLog.opType.setValue(this.opType);
            messageLog.nodeId.setValue(this.nodeId);
            messageLog.origin.setValue(this.origin);
            messageLog.destination.setValue(this.destination);
            messageLog.flowId.setValue(this.flowId);
            messageLog.msgId.setValue(this.msgId);
            messageLog.msgHash.setValue(this.msgHash);
        }

        /**
         * Method that set the value of the tags specifics to the type of MessageLog to build.
         * @param messageLog the messageLog to be updated.
         */
        protected abstract void buildSpecificTags(T messageLog);

        /**
         * Build a MessageLog instance with the value set in the builder.
         * @return the MessageLog.
         */
        public final String build() {
            T messageLog = supplier.get();
            buildCommonTags(messageLog);
            buildSpecificTags(messageLog);
            return messageLog.toString();
        }
    }
}
