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
package eu.eidas.auth.engine.metadata;

import eu.eidas.engine.exceptions.EIDASMetadataException;

import javax.annotation.Nonnull;

/**
 * Retrieves SAML2 metadata {code EntityDescriptor}s associated with a SAML Service Provider (SP) and/or Identity
 * Provider (IDP).
 *
 * @since 1.1
 */
public interface MetadataFetcherI {

    /**
     * Returns the metadata EntityDescriptor instance fetched from the given URL.
     *
     * @param url the url of the metadata file
     * @param metadataSigner the metadataSigner which can be used to verify the digital signature of the retrieved
     * EntityDescriptor via the ({@link MetadataSignerI#validateMetadataSignature(SignableXMLObject)} method.
     * @param metadataClock used to validate
     * @return the entity descriptor associated with the given url.
     * @throws EIDASMetadataException in case of errors
     */
    @Nonnull
    EidasMetadataParametersI getEidasMetadata(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner, MetadataClockI metadataClock)
            throws EIDASMetadataException;
}
