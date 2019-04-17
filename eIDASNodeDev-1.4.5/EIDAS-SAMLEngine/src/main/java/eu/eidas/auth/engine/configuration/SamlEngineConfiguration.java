package eu.eidas.auth.engine.configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.engine.SamlEngineClock;
import eu.eidas.auth.engine.core.ExtensionProcessorI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.auth.engine.core.SamlEngineEncryptionI;
import eu.eidas.util.Preconditions;

/**
 * This constitutes the configuration of the SAML engine.
 * <p>
 * Remove this class in 1.2.
 *
 * @since 1.1
 * @deprecated since 1.1, use {@link ProtocolEngineConfiguration} instead.
 */
@SuppressWarnings("ConstantConditions")
@Deprecated
public final class SamlEngineConfiguration {

    /**
     * Builder pattern for the {@link SamlEngineConfiguration} class.
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
        private SamlEngineEncryptionI cipher;

        @Nonnull
        private ExtensionProcessorI extensionProcessor;

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
            extensionProcessor = copy.extensionProcessor;
            clock = copy.clock;
        }

        public Builder(@Nonnull SamlEngineConfiguration copy) {
            Preconditions.checkNotNull(copy, "copy");
            instanceName = copy.instanceName;
            coreProperties = copy.coreProperties;
            signer = copy.signer;
            cipher = copy.cipher;
            extensionProcessor = copy.extensionProcessor;
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
        public Builder cipher(final SamlEngineEncryptionI cipher) {
            this.cipher = cipher;
            return this;
        }

        @Nonnull
        public Builder extensionProcessor(final ExtensionProcessorI extensionProcessor) {
            this.extensionProcessor = extensionProcessor;
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
            Preconditions.checkNotNull(extensionProcessor, "extensionProcessor");
            Preconditions.checkNotNull(clock, "clock");
        }

        @Nonnull
        public SamlEngineConfiguration build() {
            validate();
            return new SamlEngineConfiguration(this);
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
    public static Builder builder(@Nonnull SamlEngineConfiguration copy) {
        return new Builder(copy);
    }

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SamlEngineConfiguration.class);

    @Nonnull
    private final String instanceName;

    @Nonnull
    private final SamlEngineCoreProperties coreProperties;

    @Nonnull
    private final ProtocolSignerI signer;

    @Nullable
    private final SamlEngineEncryptionI cipher;

    @Nonnull
    private final ExtensionProcessorI extensionProcessor;

    @Nonnull
    private final SamlEngineClock clock;

    private SamlEngineConfiguration(@Nonnull Builder builder) {
        instanceName = builder.instanceName;
        coreProperties = builder.coreProperties;
        signer = builder.signer;
        cipher = builder.cipher;
        extensionProcessor = builder.extensionProcessor;
        clock = builder.clock;
    }

    @Nullable
    public SamlEngineEncryptionI getCipher() {
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
    public ExtensionProcessorI getExtensionProcessor() {
        return extensionProcessor;
    }

    @Nonnull
    public String getInstanceName() {
        return instanceName;
    }

    @Nonnull
    public ProtocolSignerI getSigner() {
        return signer;
    }

    public boolean isResponseEncryptionMandatory() {
        return cipher != null && cipher.isResponseEncryptionMandatory();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SamlEngineConfiguration that = (SamlEngineConfiguration) o;

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
        if (!extensionProcessor.equals(that.extensionProcessor)) {
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
        result = 31 * result + extensionProcessor.hashCode();
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
                ", extensionProcessor=" + extensionProcessor +
                ", clock=" + clock +
                '}';
    }
}
