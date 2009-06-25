/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
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
                if (isInvalidSession(httpRequest)) {
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

    private boolean isInvalidSession(HttpServletRequest httpServletRequest) {
        return (httpServletRequest.getRequestedSessionId() != null) &&
               !httpServletRequest.isRequestedSessionIdValid();
    }

}


