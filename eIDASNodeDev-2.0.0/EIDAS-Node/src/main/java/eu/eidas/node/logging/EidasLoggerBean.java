/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,  WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and    limitations under the License.
 */
package eu.eidas.node.logging;

import eu.eidas.auth.commons.IEIDASLogger;
import eu.eidas.auth.commons.EIDASValues;

import org.apache.xml.security.utils.Base64;


/**
 * Specific class that implements the {@link IEIDASLogger}.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com,
 *         luis.felix@multicert.com, hugo.magalhaes@multicert.com
 */
public final class EidasLoggerBean implements IEIDASLogger {
    /**
     * The origin.
     */
    private String origin;

    /**
     * The qaa level.
     */
    private int qaaLevel;

    /**
     * The timestamp.
     */
    private String timestamp;

    /**
     * In Response to.
     */
    private String inResponseTo;

    /**
     * In Response to SP Request.
     */
    private String inResponseToSPReq;

    /**
     * Sp Application.
     */
    private String spApplication;

    /**
     * The Provider Name.
     */
    private String providerName;

    /**
     * Operation type.
     */
    private String opType;

    /**
     * The destination.
     */
    private String destination;

    /**
     * The message.
     */
    private String message;

    /**
     * The country.
     */
    private String country;

    /**
     * Encrypted SAML token.
     */
    private byte[] samlHash;

    /**
     * Id of the originator message.
     */
    private String msgId;

    /**
     * SP Id of the originator message.
     */
    private String sPMsgId;

    /**
     * {@inheritDoc}
     */
    public String getOpType() {
        return opType;
    }

    /**
     * {@inheritDoc}
     */
    public void setOpType(final String opType) {
        this.opType = opType;
    }

    /**
     * {@inheritDoc}
     */
    public void setDestination(final String dest) {
        this.destination = dest;
    }

    /**
     * {@inheritDoc}
     */
    public String getDestination() {
        return destination;
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage() {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    public void setMessage(final String msg) {
        this.message = msg;
    }

    /**
     * {@inheritDoc}
     */
    public String getCountry() {
        return country;
    }

    /**
     * {@inheritDoc}
     */
    public void setCountry(final String countryName) {
        this.country = countryName;
    }

    /**
     * {@inheritDoc}
     */
    public byte[] getSamlHash() {
        if (samlHash != null) {
            return samlHash.clone();
        }

        return new byte[0];
    }

    /**
     * {@inheritDoc}
     */
    public void setSamlHash(final byte[] samlHash) {
        this.samlHash = samlHash.clone();
    }

    /**
     * {@inheritDoc}
     */
    public void setMsgId(final String theMsgId) {
        this.msgId = theMsgId;
    }

    /**
     * {@inheritDoc}
     */
    public String getMsgId() {
        return msgId;
    }

    /**
     * {@inheritDoc}
     */
    public void setSPMsgId(final String theSPMsgId) {
        this.sPMsgId = theSPMsgId;
    }

    /**
     * {@inheritDoc}
     */
    public String getSPMsgId() {
        return sPMsgId;
    }

    /**
     * {@inheritDoc}
     */
    public void setSpApplication(final String spApplication) {
        this.spApplication = spApplication;
    }

    /**
     * {@inheritDoc}
     */
    public String getSpApplication() {
        return spApplication;
    }

    /**
     * {@inheritDoc}
     */
    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    /**
     * {@inheritDoc}
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * {@inheritDoc}
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * {@inheritDoc}
     */
    public void setOrigin(final String origin) {
        this.origin = origin;
    }

    /**
     * {@inheritDoc}
     */
    public int getQaaLevel() {
        return qaaLevel;
    }

    /**
     * {@inheritDoc}
     */
    public void setQaaLevel(final int nQaaLevel) {
        this.qaaLevel = nQaaLevel;
    }

    /**
     * {@inheritDoc}
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * {@inheritDoc}
     */
    public void setTimestamp(final String nTimetamp) {
        this.timestamp = nTimetamp;
    }

    /**
     * {@inheritDoc}
     */
    public String getInResponseTo() {
        return inResponseTo;
    }

    /**
     * {@inheritDoc}
     */
    public void setInResponseTo(final String inResponseTo) {
        this.inResponseTo = inResponseTo;
    }

    /**
     * {@inheritDoc}
     */
    public String getInResponseToSPReq() {
        return inResponseToSPReq;
    }

    /**
     * {@inheritDoc}
     */
    public void setInResponseToSPReq(final String inResponseToSPReq) {
        this.inResponseToSPReq = inResponseToSPReq;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Timestamp     ").append(timestamp).append(",\n");
        stringBuilder.append("OpType        ").append(opType).append(",\n");

        if (EIDASValues.EIDAS_CONNECTOR_REQUEST.toString().equals(opType) ||
                EIDASValues.EIDAS_SERVICE_REQUEST.toString().equals(opType) ||
                EIDASValues.SP_REQUEST.toString().equals(opType)) {
            stringBuilder.append("Origin        ").append(origin).append(",\n");
            stringBuilder.append("destination   ").append(destination)
                         .append(",\n");
            stringBuilder.append("spApplication ").append(spApplication)
                         .append(",\n");
            stringBuilder.append("providerName  ").append(providerName)
                         .append(",\n");
            stringBuilder.append("country       ").append(country).append(",\n");
            stringBuilder.append("QaaLevel      ").append(qaaLevel).append(",\n");
        } else {
            stringBuilder.append(inResponseTo);

            if (EIDASValues.EIDAS_CONNECTOR_RESPONSE.toString().equals(opType)) {
                stringBuilder.append("inResponseToSPReq ")
                             .append(inResponseToSPReq).append(",\n");
            }

            stringBuilder.append("message       ").append(message).append(",\n");
        }

        stringBuilder.append("samlHash      ").append(Base64.encode(samlHash))
                     .append(",\n");

        if (EIDASValues.EIDAS_CONNECTOR_REQUEST.toString().equals(opType)) {
            stringBuilder.append("sPMsgId       ").append(sPMsgId).append(",\n");
        }

        stringBuilder.append("msgId       ").append(msgId).append(",\n");

        return stringBuilder.toString();
    }
}
