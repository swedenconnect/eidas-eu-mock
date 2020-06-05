package eu.eidas.node.specificcommunication.protocol;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.node.specificcommunication.exception.SpecificException;

/**
 * Callback handler which can be registered by the specific module at the Proxy-Service side via {@link
 * eu.eidas.node.specificcommunication.ISpecificProxyService#setResponseCallbackHandler(IResponseCallbackHandler)} to be
 * able call eIDAS back when needed.
 *
 * @see eu.eidas.node.specificcommunication.ISpecificProxyService
 * @since 1.1
 */
public interface IResponseCallbackHandler {

    void callEidasBack(@Nonnull ILightResponse eidasResponse,
                       @Nonnull HttpServletRequest request,
                       @Nonnull HttpServletResponse response) throws SpecificException;
}
