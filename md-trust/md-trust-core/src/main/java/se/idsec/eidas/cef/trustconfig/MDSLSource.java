package se.idsec.eidas.cef.trustconfig;

import eu.europa.eidas.metadata.servicelist.MetadataServiceListDocument;
import se.idsec.eidas.cef.trustconfig.xml.SigVerifyResult;
import se.idsec.eidas.cef.trustconfig.xml.XMLSignatureVerifier;
import se.idsec.eidas.cef.trustconfig.xml.XmlUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Aggregator for MDSL data. This aggregator operates according to the following process:
 *
 * <ul>
 *   <li>MDSL is downloaded when an object of this class is constructed</li>
 *   <li>If cached MDSL has not expired, the cached MDSL is always returned upon request</li>
 *   <li>If the cached MDSL has expired, a new MDSL is downloaded upon request</li>
 *   <li>On all requests, the cahced MDSL is recached on all requests if the recache time has passed. Such recache is done in the background in a daemon thread</li>
 * </ul>
 *
 * This strategy ensures that a cached MDSL is allways used without delay except if the service has been idle for as long as it takes for the MDSL to expire.
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public class MDSLSource {

  protected final Logger LOG = Logger.getLogger(MDSLDownloader.class.getName());
  /** URL location of the MDSL */
  private final URL mdslUrl;
  /** Certificate used to verify the signature on the MDSL */
  private final X509Certificate mdslCert;
  /** The max age of the MDSL where no recache is attempted */
  private long cacheDuration;
  /** The time when the MDSL was cached most recently */
  private long lastUpdate = 0;
  /** The parsed data object holding the MDSL data */
  protected MetadataServiceListDocument cachedMdsl;
  /** The daemon thread used to download and recache MDSL data */
  private Thread downloadThread;

  /**
   * Constructor
   * @param location URL location of the MDSL
   * @param mdslCert Certificate used to validate the signature on the MDSL
   * @throws MalformedURLException if the MDSL location URL is invalid
   */
  public MDSLSource(String location, X509Certificate mdslCert) throws MalformedURLException {
    this.mdslUrl = new URL(location);
    this.mdslCert = mdslCert;
    /** Set default recache time to 10 minutes between caches */
    this.cacheDuration = 1000 * 60 * 10;
    MDSLDownloader downloader = new MDSLDownloader(mdslUrl, mdslCert);
    cachedMdsl = downloader.downloadAndVerifyMdslDoc();
    lastUpdate = System.currentTimeMillis();
  }

  /**
   * Setter for custom cache duration. Default is 10 minutes
   * @param cacheDuration custom cache duration in milliseconds
   */
  public void setCacheDuration(long cacheDuration) {
    this.cacheDuration = cacheDuration;
  }

  /**
   * Returns a current non expired MDSL if available.
   * @return current non expired MDSL
   */
  public MetadataServiceListDocument getMdsl(){
    MetadataServiceListDocument currentNonExpiredMdsl = getNonExpiredCachedMdsl();

    if (currentNonExpiredMdsl != null && System.currentTimeMillis() < lastUpdate + cacheDuration){
      // we have not yet reached the cache duration. Don't recache. Just return cached version
      return currentNonExpiredMdsl;
    }
    if (currentNonExpiredMdsl != null){
      // We still have a valid mdsl. Return that but perform a recache for next time.
      recache();
      return currentNonExpiredMdsl;
    }
    // The MDSL has expired. Force update MDSL.
    recache();
    try {
      downloadThread.join();
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    return getNonExpiredCachedMdsl();
  }

  /**
   * Returns the current cached MDSL only if its next update is after current time
   * Note that the recache only replaces the current cached MDSL if download and signature validation succeeds.
   * It is therefore important to verify that the current cached MDSL has not expired before using it.
   * @return
   */
  private MetadataServiceListDocument getNonExpiredCachedMdsl() {
    // Check that the current cached MDSL is not to old
    try {
      if (cachedMdsl.getMetadataServiceList().getNextUpdate().before(Calendar.getInstance())){
        LOG.log(Level.WARNING, "MDSL is expired from URL: " + mdslUrl.toExternalForm());
        return null;
      }
      return cachedMdsl;
    } catch (Exception ex){
      LOG.log(Level.WARNING, "No current valid unexpired MDSL can be found for URL: " + mdslUrl.toExternalForm(), ex);
      return null;
    }
  }

  /**
   * Starts the recache daemon process if no such process is already in action
   */
  private synchronized void recache() {
    if (downloadThread != null && downloadThread.isAlive()){
      return;
    }
    MDSLDownloader downloader = new MDSLDownloader(mdslUrl, mdslCert);
    downloadThread = new Thread(downloader);
    downloadThread.setDaemon(true);
    downloadThread.start();
  }

  /**
   * Runnable MDSL downloader class
   */
  public class MDSLDownloader implements Runnable {

    /** MDSL location URL */
    private URL url;
    /** MDSL validation certificate */
    private X509Certificate certificate;

    /**
     * Constructor
     * @param url MDSL location url
     * @param certificate MDSL validation certificate
     */
    public MDSLDownloader(URL url, X509Certificate certificate) {
      this.url = url;
      this.certificate = certificate;
    }

    /**
     * Starts download process in the runnable class
     */
    @Override public void run() {
      MetadataServiceListDocument downloadAndVerifyMdslDoc = downloadAndVerifyMdslDoc();
      lastUpdate = System.currentTimeMillis();
      if (downloadAndVerifyMdslDoc != null){
        cachedMdsl = downloadAndVerifyMdslDoc;
      }
    }

    /**
     * Performs the download and signature validation of the target MDSL
     * @return Parsed MDSL document {@link MetadataServiceListDocument}
     */
    public MetadataServiceListDocument downloadAndVerifyMdslDoc(){
      try {
        MetadataServiceListDocument mdslDoc = MetadataServiceListDocument.Factory.parse(url);
        SigVerifyResult sigVerifyResult = XMLSignatureVerifier.verifySignature(XmlUtils.getCanonicalBytes(mdslDoc));
        if (sigVerifyResult.isValid()){
          if (sigVerifyResult.getCert().equals(certificate)){
            return mdslDoc;
          } else {
            LOG.log(Level.WARNING, "Invalid MDSL Certificate in MDSL from: " + url.toExternalForm());
          }
        } else {
          LOG.log(Level.WARNING, "Invalid MDSL Certificate in MDSL from: " + url.toExternalForm());
        }
      }
      catch (Exception e) {
        LOG.log(Level.WARNING, "Unable to parse and verify MDSL doc from: " + url.toExternalForm(), e);
      }
      return null;
    }
  }

}
