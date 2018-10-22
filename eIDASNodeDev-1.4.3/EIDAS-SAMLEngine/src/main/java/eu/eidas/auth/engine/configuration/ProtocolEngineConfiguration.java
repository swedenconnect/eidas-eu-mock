package eu.eidas.auth.engine.configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.engine.SamlEngineClock;
import eu.eidas.auth.engine.core.ProtocolCipherI;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.util.Preconditions;

/**
 * This constitutes the configuration of the {@link eu.eidas.auth.engine.ProtocolEngineI}.
 *
 * The ProtocolEngineConfiguration is composed of
 * <ol>
 * <li>A unique name (e.g. "MyEngineName" )</li>
 * <li>Core properties</li>
 * <li>A Signer</li>
 * <li>An (optional) Cipher</li>
 * <li>A ProtocolProcessor</li>
 * <li>A Clock</li>
 * </ol>
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
@NotThreadSafe
public final class ProtocolEngineConfiguration {

    /**
     * Builder pattern for the {@link ProtocolEngineConfiguration} class.
     * <p/>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p/>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p/>
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder {

        @Nonnull
        private String instanceName;

        @Nonnull
        private SamlEngineCoreProperties coreProperties;

        @Nonnull
        private ProtocolSignerI signer;

        @Nullable
        private ProtocolCipherI cipher;

        @Nonnull
        private ProtocolProcessorI protocolProcessor;

        @Nonnull
        private SamlEngineClock clock;

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            Preconditions.checkNotNull(copy, "copy");
            instanceName = copy.instanceName;
            coreProperties = copy.coreProperties;
            signer = copy.signer;
            cipher = copy.cipher;
            protocolProcessor = copy.protocolProcessor;
            clock = copy.clock;
        }

        public Builder(@Nonnull ProtocolEngineConfiguration copy) {
            Preconditions.checkNotNull(copy, "copy");
            instanceName = copy.instanceName;
            coreProperties = copy.coreProperties;
            signer = copy.signer;
            cipher = copy.cipher;
            protocolProcessor = copy.protocolProcessor;
            clock = copy.clock;
        }

        @Nonnull
        public Builder instanceName(final String instanceName) {
            this.instanceName = instanceName;
            return this;
        }

        @Nonnull
        public Builder coreProperties(final SamlEngineCoreProperties coreProperties) {
            this.coreProperties = coreProperties;
            return this;
        }

        @Nonnull
        public Builder signer(final ProtocolSignerI signer) {
            this.signer = signer;
            return this;
        }

        @Nonnull
        public Builder cipher(final ProtocolCipherI cipher) {
            this.cipher = cipher;
            return this;
        }

        @Nonnull
        public Builder protocolProcessor(final ProtocolProcessorI protocolProcessor) {
            this.protocolProcessor = protocolProcessor;
            return this;
        }

        @Nonnull
        public Builder clock(final SamlEngineClock clock) {
            this.clock = clock;
            return this;
        }

        private void validate() throws IllegalArgumentException {
            Preconditions.checkNotBlank(instanceName, "instanceName");
            Preconditions.checkNotNull(coreProperties, "coreProperties");
            Preconditions.checkNotNull(signer, "signer");
            Preconditions.checkNotNull(protocolProcessor, "protocolProcessor");
            Preconditions.checkNotNull(clock, "clock");
        }

        @Nonnull
        public ProtocolEngineConfiguration build() {
            validate();
            return new ProtocolEngineConfiguration(this);
        }

    }

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull ProtocolEngineConfiguration copy) {
        return new Builder(copy);
    }

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProtocolEngineConfiguration.class);

    @Nonnull
    private final String instanceName;

    @Nonnull
    private final SamlEngineCoreProperties coreProperties;

    @Nonnull
    private final ProtocolSignerI signer;

    @Nullable
    private final ProtocolCipherI cipher;

    @Nonnull
    private final ProtocolProcessorI protocolProcessor;

    @Nonnull
    private final SamlEngineClock clock;

    private ProtocolEngineConfiguration(@Nonnull Builder builder) {
        instanceName = builder.instanceName;
        coreProperties = builder.coreProperties;
        signer = builder.signer;
        cipher = builder.cipher;
        protocolProcessor = builder.protocolProcessor;
        clock = builder.clock;
    }

    @Nullable
    public ProtocolCipherI getCipher() {
        return cipher;
    }

    @Nonnull
    public SamlEngineClock getClock() {
        return clock;
    }

    @Nonnull
    public SamlEngineCoreProperties getCoreProperties() {
        return coreProperties;
    }

    @Nonnull
    public ProtocolProcessorI getProtocolProcessor() {
        return protocolProcessor;
    }

    @Nonnull
    public String getInstanceName() {
        return instanceName;
    }

    @Nonnull
    public ProtocolSignerI getSigner() {
        return signer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProtocolEngineConfiguration that = (ProtocolEngineConfiguration) o;

        if (!instanceName.equals(that.instanceName)) {
            return false;
        }
        if (!coreProperties.equals(that.coreProperties)) {
            return false;
        }
        if (!signer.equals(that.signer)) {
            return false;
        }
        if (cipher != null ? !cipher.equals(that.cipher) : that.cipher != null) {
            return false;
        }
        if (!protocolProcessor.equals(that.protocolProcessor)) {
            return false;
        }
        return clock.equals(that.clock);

    }

    @Override
    public int hashCode() {
        int result = instanceName.hashCode();
        result = 31 * result + coreProperties.hashCode();
        result = 31 * result + signer.hashCode();
        result = 31 * result + (cipher != null ? cipher.hashCode() : 0);
        result = 31 * result + protocolProcessor.hashCode();
        result = 31 * result + clock.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SamlEngineConfiguration{" +
                "instanceName='" + instanceName + '\'' +
                ", coreProperties=" + coreProperties +
                ", signer=" + signer +
                ", cipher=" + cipher +
                ", protocolProcessor=" + protocolProcessor +
                ", clock=" + clock +
                '}';
    }
}
