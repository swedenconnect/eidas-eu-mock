/*
 * Copyright (c) 2022.  Agency for Digital Government (DIGG)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.swedenconnect.eidas.cef.confbuilder.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;

import org.springframework.core.io.Resource;

/**
 * Utility methods for working with X.509 certificates and CRL:s.
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public class X509Utils {

  /** Factory for creating certificates. */
  private static CertificateFactory factory = null;

  static {
    try {
      factory = CertificateFactory.getInstance("X.509");
    }
    catch (CertificateException e) {
      throw new SecurityException(e);
    }
  }

  /**
   * Decodes a {@link X509Certificate} from its encoding.
   *
   * @param encoding the certificate encoding
   * @return a X509Certificate object
   * @throws CertificateException for decoding errors
   */
  public static X509Certificate decodeCertificate(final byte[] encoding) throws CertificateException {
    try (final ByteArrayInputStream bis = new ByteArrayInputStream(encoding)) {
      return decodeCertificate(bis);
    }
    catch (final IOException e) {
      throw new CertificateException("IO error", e);
    }
  }

  /**
   * Decodes a {@link X509Certificate} from an input stream.
   * <p>
   * The method does not close the input stream.
   * </p>
   *
   * @param stream the stream to read
   * @return a X509Certificate object
   * @throws CertificateException for decoding errors
   */
  public static X509Certificate decodeCertificate(final InputStream stream) throws CertificateException {
    return (X509Certificate) factory.generateCertificate(stream);
  }

  /**
   * Decodes a {@link X509Certificate} from a resource.
   *
   * @param resource the resource to read
   * @return a X509Certificate object
   * @throws CertificateException for decoding errors
   */
  public static X509Certificate decodeCertificate(final Resource resource) throws CertificateException {
    try (final InputStream is = resource.getInputStream()) {
      return decodeCertificate(is);
    }
    catch (final IOException e) {
      throw new CertificateException("Failed to read certificate resource", e);
    }
  }

  /**
   * Decodes a {@link X509CRL} from its encoding.
   *
   * @param encoding the CRL encoding
   * @return a X509CRL object
   * @throws CRLException for decoding errors
   */
  public static X509CRL decodeCrl(final byte[] encoding) throws CRLException {
    try (final ByteArrayInputStream bis = new ByteArrayInputStream(encoding)) {
      return decodeCrl(bis);
    }
    catch (final IOException e) {
      throw new CRLException("IO error", e);
    }
  }

  /**
   * Decodes a {@link X509CRL} from an input stream.
   * <p>
   * The method does not close the input stream.
   * </p>
   *
   * @param stream the stream to read
   * @return a X509CRL object
   * @throws CRLException for decoding errors
   */
  public static X509CRL decodeCrl(final InputStream stream) throws CRLException {
    return (X509CRL) factory.generateCRL(stream);
  }

  /**
   * Decodes a {@link X509CRL} from a resource.
   *
   * @param resource the resource to read
   * @return a X509CRL object
   * @throws CRLException for decoding errors
   */
  public static X509CRL decodeCrl(final Resource resource) throws CRLException {
    try (final InputStream is = resource.getInputStream()) {
      return decodeCrl(is);
    }
    catch (final IOException e) {
      throw new CRLException("Failed to read CRL resource", e);
    }
  }

  /**
   * The {@link X509Certificate#toString()} prints way too much for a normal log entry. This method displays the
   * subject, issuer and serial number.
   *
   * @param certificate the certificate to log
   * @return a log string
   */
  public static String toLogString(final X509Certificate certificate) {
    if (certificate == null) {
      return "null";
    }
    StringBuffer sb = new StringBuffer();
    sb.append("subject='").append(certificate.getSubjectX500Principal().getName()).append("',");
    sb.append("issuer='").append(certificate.getIssuerX500Principal().getName()).append("',");
    sb.append("serial-number='").append(certificate.getSerialNumber()).append("'");
    return sb.toString();
  }

  // Hidden
  private X509Utils() {
  }
}