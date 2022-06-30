package eu.eidas.auth.engine.core;

/**
 * Marker interface for the encrypt and decrypt interfaces.
 *
 * @since 1.1
 */
public interface ProtocolCipherI {

    /**
     * Returns {@code true} when the validity period of an X.509 certificate must be verified when performing
     * cryptographic operations, returns {@code false} otherwise.
     *
     * @return {@code true} when the validity period of an X.509 certificate must be verified when performing
     * cryptographic operations, returns {@code false} otherwise.
     */
    boolean isCheckedValidityPeriod();

    /**
     * Returns {@code true} when using self-signed X.509 certificate is not allowed, returns {@code false} when it is
     * allowed.
     *
     * @return {@code true} when using self-signed X.509 certificate is not allowed, returns {@code false} when it is
     * allowed.
     */
    boolean isDisallowedSelfSignedCertificate();

    /**
     * Returns whether encryption is mandatory regardless of the country.
     * <p>
     * When this flag is {@code true}, the {@link #isEncryptionEnabled(String)} method must always return {@code true}.
     *
     * @return whether encryption is mandatory regardless of the country.
     */
    boolean isResponseEncryptionMandatory();
}
