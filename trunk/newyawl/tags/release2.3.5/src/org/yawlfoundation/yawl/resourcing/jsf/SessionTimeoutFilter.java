/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Redirects a user action to the login page after a session timeout
 *
 * Author: Michael Adams
 * Date: 14/03/2008
 *
 * Based on code sourced from:
 * http://techieexchange.blogspot.com/2008/02/jsf-session-expiry-timeout-solution.html
 */

public class SessionTimeoutFilter implements Filter {

    private String _timeoutPage = "/sessiontimeout.html";
    private Logger _log = Logger.getLogger(SessionTimeoutFilter.class);



    // Implemented interface methods //

    public void init(FilterConfig filterConfig) throws ServletException { }

    public void destroy() { }

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {

        if ((request instanceof HttpServletRequest) &&
            (response instanceof HttpServletResponse)) {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // avoid infinite loop from login page back to timeout page
            if (! isLoginPageRequest(httpRequest)) {
                if (isInvalidSession(httpRequest) && (! isRSSFormRequest(httpRequest))) {
                    _log.warn("User session has expired");
                    String url = httpRequest.getContextPath() + _timeoutPage;
                    httpResponse.sendRedirect(url);
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }


    private boolean isLoginPageRequest(HttpServletRequest request) {
        return StringUtils.contains(request.getRequestURI(), "Login");
    }

    private boolean isRSSFormRequest(HttpServletRequest request) {
        return StringUtils.contains(request.getRequestURI(), "rssFormViewer");
    }

    private boolean isInvalidSession(HttpServletRequest httpServletRequest) {
        return (httpServletRequest.getRequestedSessionId() != null) &&
               !httpServletRequest.isRequestedSessionIdValid();
    }

}


