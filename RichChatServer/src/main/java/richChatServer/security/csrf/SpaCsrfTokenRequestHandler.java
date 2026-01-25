/*
 * Copyright (C) 2025 IUT Laval - Le Mans Université
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package richChatServer.security.csrf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

/**
 *
 * @author Remi Venant
 */
public class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    public final static String CSRF_DEV_HEADER_NAME = "X-XSRF-TOKEN-DEV";

    private static final Log LOG = LogFactory.getLog(SpaCsrfTokenRequestHandler.class);

    private final CsrfTokenRequestHandler xorDelegate = new XorCsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestHandler plainDelegate = new CsrfTokenRequestAttributeHandler();

    private boolean useTokenForDev = false;

    public void setUseTokenForDev() {
        LOG.info("WILL INJECT CSRF TOKEN TO HEADER FOR ALL REQUEST! Use only for dev!!!");
        this.useTokenForDev = true;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> deferredCsrfToken) {
        /*
         * Always use XorCsrfTokenRequestAttributeHandler to provide BREACH protection of
         * the CsrfToken when it is rendered in the response body.
         */
        this.xorDelegate.handle(request, response, deferredCsrfToken);

        /*
         * Render the token value to a cookie by causing the deferred token to be loaded.
         */
        CsrfToken tk = deferredCsrfToken.get();

        // Inject le token dans l'en-tête de réponse pour le dev UNIQUEMENT
        if (this.useTokenForDev) {
            response.setHeader(CSRF_DEV_HEADER_NAME, tk.getToken());
        }
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        /*
         * If the request contains the dev csrf header, and handling it is enabled,
         * use the XorCsrfTokenRequestAttributeHandler with the proper csrf provider
         */
        if (this.useTokenForDev) {
            String headerDevTk = request.getHeader(CSRF_DEV_HEADER_NAME);
            if (StringUtils.hasText(headerDevTk)) {
                LOG.debug("RESOLVE TOKEN WITH DEV HEADER");
                return headerDevTk;
            }
        }
        /*
        * If the request contains a request header, use CsrfTokenRequestAttributeHandler
        * to resolve the CsrfToken. This applies when a single-page application includes
        * the header value automatically, which was obtained via a cookie containing the
        * raw CsrfToken.
         */
        if (StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()))) {
            return this.plainDelegate.resolveCsrfTokenValue(request, csrfToken);
        }
        /*
        * In all other cases (e.g. if the request contains a request parameter), use
        * XorCsrfTokenRequestAttributeHandler to resolve the CsrfToken. This applies
        * when a server-side rendered form includes the _csrf request parameter as a
        * hidden input, or when it is provided in a STOMP header....
         */
        return this.xorDelegate.resolveCsrfTokenValue(request, csrfToken);
    }

}
