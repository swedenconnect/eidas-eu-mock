package eu.eidas.node.specificcommunication.protocol;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.node.specificcommunication.exception.SpecificException;

/**
 * Callback handler which can be registered by the specific module at the Connector side via {@link
 * eu.eidas.node.specificcommunication.ISpecificConnector#setRequestCallbackHandler(IRequestCallbackHandler)} to be able
 * call eIDAS back when needed.
 *
 * @see eu.eidas.node.specificcommunication.ISpecificConnector
 * @since 1.1
 */
public interface IRequestCallbackHandler {

    void callEidasBack(@Nonnull ILightRequest eidasRequest,
                       @Nonnull HttpServletRequest request,
                       @Nonnull HttpServletResponse response) throws SpecificException;
}
