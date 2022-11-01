/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.idsec.eidas.cef.trustconfig.xml;

import org.w3c.dom.Node;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Carries the result of XML signature validation
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public class SigVerifyResult {

    /** The signing certificate of the first valid signature */
    private X509Certificate cert = null;
    /** Status indicator */
    private String status = "";
    /** Boolean indicator of valid signature. True if at least one signature is valid */
    private boolean valid = false;
    /** Validation result of all present signatures */
    private List<IndivdualSignatureResult> resultList = new ArrayList<IndivdualSignatureResult>();
    /** The number of present signatures */
    private int sigCnt;
    /** The number of valid signatures */
    private int validSignatures;

    /**
     * Constructor
     */
    public SigVerifyResult() {
    }

    /**
     * Sets the certificate and the status to valid
     *
     * @param cert
     */
    public SigVerifyResult(X509Certificate cert) {
        this.status = "ok";
        this.cert = cert;
        this.valid = true;
    }

    /**
     * Sets the values of the signature verification result
     *
     * @param cert Certificate
     * @param status Status
     * @param valid true if the signature is valid.
     */
    public SigVerifyResult(X509Certificate cert, String status, boolean valid) {
        this.status = status;
        this.cert = cert;
        this.valid = valid;
    }

    /**
     * Sets the status comment for failed validation
     *
     * @param status Status
     */
    public SigVerifyResult(String status) {
        this.status = status;
        this.cert = null;
        this.valid = false;
    }

    /**
     * Analyzes the result of individual signatures and generates the summary
     * of the signature validation results.
     */
    public void consolidateResults() {
        if (resultList.isEmpty()) {
            valid = false;
            status = "No signature covers this document";
            sigCnt = 0;
            validSignatures = 0;
            return;
        }
        sigCnt = resultList.size();
        int firstValid = -1, idx = 0, validCnt = 0;
        for (IndivdualSignatureResult result : resultList) {
            if (result.thisValid) {
                validCnt++;
            }
            if (firstValid < 0 && result.thisValid) {
                firstValid = idx;
            }
            idx++;
        }
        validSignatures = validCnt;

        //Get the most relevant signature result
        if (firstValid < 0) {
            valid = false;
            status = resultList.get(0).thisStatus;
        } else {
            IndivdualSignatureResult result = resultList.get(firstValid);
            valid = result.thisValid;
            status = result.thisStatus;
            cert = result.thisCert;
        }
    }

    /**
     * Creates and adds a new signature result for individual signature
     * @return the created and added signature result object
     */
    public IndivdualSignatureResult addNewIndividualSignatureResult() {
        IndivdualSignatureResult result = new IndivdualSignatureResult();
        resultList.add(result);
        return result;
    }

    /**
     * Getter for certificate of the first valid signature
     * @return {@link X509Certificate}
     */
    public X509Certificate getCert() {
        return cert;
    }

    /**
     * Getter for status
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Getter for valid status. Indication of true if at least one signature is valid.
     * @return valid status
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the list of individual signature results
     * @return individual signature results
     */
    public List<IndivdualSignatureResult> getResultList() {
        return resultList;
    }

    /**
     * Getter for the number of signatures
     * @return number of signatures
     */
    public int getSigCnt() {
        return sigCnt;
    }

    /**
     * Getter for the number of valid signatures
     * @return the number of valid signatures
     */
    public int getValidSignatures() {
        return validSignatures;
    }

    /**
     * Data class for validation result for an individual signature
     */
    public static class IndivdualSignatureResult {

        /** The signature certificate */
        public X509Certificate thisCert = null;
        /** Status indication */
        public String thisStatus = "";
        /** Boolean indication if the signature is valid */
        public boolean thisValid = false;
        /** The signature node of this signature */
        public Node thisSignatureNode = null;

        /**
         * Constructor
         */
        public IndivdualSignatureResult() {
        }
    }
}