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
package eu.eidas.updater.vc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper to Eidas Version Control.
 *
 */
public final class VersionControl {

  /**
   * Flag to check if update version is enabled.
   */
  private boolean updateVersion = false;

  /**
   * The Logger Object.
   */
  private static final Logger LOG = LoggerFactory.getLogger(VersionControl.class);

  /**
   * Updates the version file.
   */
  public void updateVersion() {
    LOG.debug("Update is on: " + updateVersion);
  }

  /**
   * @param nUpdateVersion the updateVersion to set
   */
  public void setUpdateVersion(final boolean nUpdateVersion) {
    this.updateVersion = nUpdateVersion;
  }

  /**
   * @return the updateVersion
   */
  public boolean isUpdateVersion() {
    return updateVersion;
  }

}
