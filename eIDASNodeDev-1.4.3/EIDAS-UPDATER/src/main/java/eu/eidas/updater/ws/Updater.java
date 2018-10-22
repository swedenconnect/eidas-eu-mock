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
package eu.eidas.updater.ws;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.ContextLoader;

import eu.eidas.updater.vc.VersionControl;

/**
 * REST service for reloading the application context.
 *
 * @author hugo.magalhaes@multicert.com, ricardo.ferreira@multicert.com
 *
 * @version $Revision: $, $Date: $
 */
@Path("/updater")
public final class Updater {

  /**
   * Return MIME Type.
   */
  private static final String MIME_TYPE = "text/plain";

  /**
   * The refresh's return string.
   */
  private static final String RETURN_MSG = "Refresh operation launched";
    private static final String REFRESH_IN_PROGRESS = "A refresh operation is currently in progress";
    private static final String REFRESH_NOT_ALLOWED = "not allowed";
    private static final String LOCALHOST="127.0.0.1";//NOSONAR

  /**
   * Version Controller.
   */
  private VersionControl vc = null;
    private boolean refreshPending=false;

  /**
   * The Logger object.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Updater.class);
    Thread worker=null;

  /**
   * REST method that receives the request for restarting the application
   * context.
   *
   * @return A string indicating that the application context is restarting.
   */
  @GET
  @Path("refresh")
  @Produces(MIME_TYPE)
  public String refresh(@Context HttpServletRequest req) {
      if(!isRefreshPossible(req)){
          return REFRESH_NOT_ALLOWED;
      }
      synchronized(Updater.class) {
          if (refreshPending) {
              return REFRESH_IN_PROGRESS;
          }
          refreshPending=true;
      }


      worker = new Thread(new Runnable(){
          @Override
          public void run() {
              try {
                  restart();
                  if (vc != null) {
                      vc.updateVersion();
                  } else {
                      LOG.warn("Couldn't inject VersionControl!");
                  }
              }catch(Exception exc){
                  LOG.error("error during configuration reload: ", exc);
              }
              refreshPending=false;
              worker=null;
          }
      });
      worker.start();
      return RETURN_MSG;
  }

  /**
   * Reloads the spring application context.
   */
  private void restart() {
    LOG.debug("Restarting application context...");
    final ApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
    ((ConfigurableApplicationContext) ctx).close();
    ((ConfigurableApplicationContext) ctx).refresh();
  }

  /**
   * Sets the VersionControl wrapper.
   *
   * @param nVC The vc to set.
   */
  public void setVc(final VersionControl nVC) {
    this.vc = nVC;
  }

  /**
   * Gets the VersionControl wrapper.
   *
   * @return the vc value.
   */
  public VersionControl getVc() {
    return vc;
  }

    private boolean isRefreshPossible(HttpServletRequest req){
        String remoteAddress = req.getRemoteAddr();
        if(!LOCALHOST.equalsIgnoreCase(remoteAddress)){
            LOG.warn("trying to refresh the configuration from remote address "+remoteAddress );
            return false;
        }
        LOG.info("Allowing configuration reload");
        return true;
    }

}
