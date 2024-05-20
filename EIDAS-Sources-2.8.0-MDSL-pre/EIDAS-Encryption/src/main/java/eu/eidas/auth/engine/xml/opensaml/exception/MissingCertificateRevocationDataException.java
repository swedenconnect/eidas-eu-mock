package eu.eidas.auth.engine.xml.opensaml.exception;

public class MissingCertificateRevocationDataException extends RuntimeException {
    public MissingCertificateRevocationDataException(final String message) {
        super(message);
    }
}
