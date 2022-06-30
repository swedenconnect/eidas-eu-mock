/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.eidas.node.auth.service;

import eu.eidas.auth.commons.CitizenConsent;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Interface that supplies methods for processing citizen-related matters.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com,
 *         luis.felix@multicert.com, hugo.magalhaes@multicert.com,
 *         paulo.ribeiro@multicert.com
 * @version $Revision: 1.28 $, $Date: 2010-11-18 23:17:50 $
 */
public interface ISERVICECitizenService {

    /**
     * Checks if the citizen consent has all the required mandatory attributes.
     *
     * @param consent            The citizen supplied consent.
     * @param ipUserAddress      The citizen's IP address.
     * @see CitizenConsent
     * @see EidasAuthenticationRequest
     * @see ISERVICESAMLService
     */
    void processCitizenConsent(CitizenConsent consent,
                               @Nonnull StoredAuthenticationRequest storedRequest,
                               String ipUserAddress);

    /**
     * Constructs the Citizen Consent based on the checked boxes from consent-type
     * form.
     *
     * @return CitizenConsent containing the mandatory and optional attributes
     * that the Node has permission to request.
     * @see CitizenConsent
     * @see Map
     */
    @Nonnull
    CitizenConsent getCitizenConsent(@Nonnull WebRequest webRequest, @Nonnull ImmutableAttributeMap attributes);

    /**
     * Eliminates attributes without consent, and updates the Attributes.
     *
     * @param citizenConsent The attributes the citizen gives permission to be
     *                       accessed.
     * @param attributes     The attributes to update.
     * @return The updated attributes.
     * @see CitizenConsent
     * @see ImmutableAttributeMap
     */
    @Nonnull
    ImmutableAttributeMap filterConsentedAttributes(@Nonnull CitizenConsent citizenConsent,
                                                    @Nonnull ImmutableAttributeMap attributes);

    /**
     * Replaces the attribute list in session with the one provided.
     *
     * @return The updated Personal Attribute List.
     */
    @Nonnull
    IAuthenticationRequest updateConsentedAttributes(@Nonnull IAuthenticationRequest authnRequest,
                                                     @Nonnull ImmutableAttributeMap consentedAttributes);

    /**
     * Checks that all mandatory values are present in the given attributes otherwise throws an Exception.
     *
     * @throws EIDASServiceException if the attributes are not missing a mandatory attribute
     */
    void checkMandatoryAttributes(@Nonnull ImmutableAttributeMap attributes) throws EIDASServiceException;

    /**
     * Checks whether the attribute map satifisfies the rule of representation, otherwise thorows an Exception.
     *
     * @throws EIDASServiceException if the attributes are not missing a mandatory attribute
     */
    void checkRepresentativeAttributes(@Nonnull ImmutableAttributeMap attributes) throws EIDASServiceException;
}
