package eu.eidas.auth.engine.xml.opensaml.exception;

public class CertificateRevokedException extends RuntimeException {

    public CertificateRevokedException(final String message) {
        super(message);
    }

}
