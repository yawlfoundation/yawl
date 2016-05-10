package org.yawlfoundation.yawl.resourcing.jsf;

import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginFilter implements Filter {

	private final String loginPage = "/faces/Login.jsp";

	public void init(FilterConfig filterConfig) throws ServletException { }

	public void destroy() {	}


	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse)) {

			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			if (!isInvalidSession(httpRequest)) {
				if (!(isLoginPageRequest(httpRequest) || isRSSFormRequest(httpRequest))) {
					if (!isLoggedIn(httpRequest)) {
						httpResponse.sendRedirect(httpRequest.getContextPath() + loginPage);
						return;
					}
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

	private boolean isLoggedIn(HttpServletRequest httpServletRequest) {
        SessionBean sb = getSessionBean(httpServletRequest);
		return sb != null && sb.isLoggedIn();
	}

	protected SessionBean getSessionBean(HttpServletRequest request) {
		return (SessionBean) (request).getSession().getAttribute("SessionBean");
	}

	private boolean isInvalidSession(HttpServletRequest httpServletRequest) {
		return (httpServletRequest.getRequestedSessionId() != null) &&
                !httpServletRequest.isRequestedSessionIdValid();
	}

}
