/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package member_country_specific.specific.proxyservice.servlet;

import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import member_country_specific.specific.proxyservice.SpecificProxyServiceApplicationContextProvider;

import javax.servlet.http.HttpServlet;

/**
 * Specific Proxy Service servlet ancestor.
 *
 * @since 2.0
 */
public abstract class AbstractSpecificProxyServiceServlet extends HttpServlet {

    /**
     * Abstract logging impl.
     * @return the concrete logger of implementing servlet
     */
	protected abstract Logger getLogger();

    /**
     * Obtaining the application context
     * @return Node applicationContext
     */
    protected final ApplicationContext getApplicationContext() {
        return SpecificProxyServiceApplicationContextProvider.getApplicationContext();
    }

}
