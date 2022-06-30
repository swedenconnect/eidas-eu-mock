/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.eidas.config.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * country eIDASService parametrization in eidas.xml
 */
public class EIDASNodeCountry {
    private static final Logger LOG = LoggerFactory.getLogger(EIDASNodeCountry.class.getName());
    private String code;
    private String name;
    private String serviceUrl;
    private String serviceMetadataUrl;
    private int skewTime;
    private boolean encryptionTo=false;
    private boolean encryptionFrom=true;


    public EIDASNodeCountry(){
    }
    public EIDASNodeCountry(String code, String name, String url, String skewTime){
        this();
        setCode(code);
        setServiceUrl(url);
        setName(name);
        int skewTimeSet=0;
        try {
            skewTimeSet=Integer.parseInt(skewTime);
        }catch (NumberFormatException nfe){
            LOG.info("ERROR : invalid skewtime value {}", nfe.getMessage());
        }
        setSkewTime(skewTimeSet);
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public int getSkewTime() {
        return skewTime;
    }

    public void setSkewTime(int skewTime) {
        this.skewTime = skewTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEncryptionTo() {
        return encryptionTo;
    }

    public void setEncryptionTo(boolean encryptionTo) {
        this.encryptionTo = encryptionTo;
    }

    public boolean isEncryptionFrom() {
        return encryptionFrom;
    }

    public void setEncryptionFrom(boolean encryptionFrom) {
        this.encryptionFrom = encryptionFrom;
    }

    public String getServiceMetadataUrl() {
        return serviceMetadataUrl;
    }

    public void setServiceMetadataUrl(String serviceMetadataUrl) {
        this.serviceMetadataUrl = serviceMetadataUrl;
    }
}
