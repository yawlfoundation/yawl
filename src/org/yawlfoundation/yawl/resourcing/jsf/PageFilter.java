package org.yawlfoundation.yawl.resourcing.jsf;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The jsf ThemeServlet class makes all files on the classpath available via
 * the 'theme' uri. This filter prevents certain files from being accessed via
 * that method.
 */
public class PageFilter implements Filter {

	public void init(FilterConfig filterConfig) throws ServletException { }

	public void destroy() {	}


	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse)) {

			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			if (!isAuthorised(httpRequest)) {
                httpRequest.getSession();       // stops potential IllegalStateException
				httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		}

		filterChain.doFilter(request, response);
	}


    private boolean isAuthorised(HttpServletRequest request) {
        return !isAdminPage(request) || isAdminSession(request);
    }


    private boolean isAdminSession(HttpServletRequest httpServletRequest) {
        SessionBean sb = getSessionBean(httpServletRequest);
   		return sb != null && sb.isAdminSession();
   	}


   	protected SessionBean getSessionBean(HttpServletRequest request) {
   		return (SessionBean) (request).getSession().getAttribute("SessionBean");
   	}


    private boolean isAdminPage(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return requestURI != null &&
      				(requestURI.endsWith("orgDataMgt.jsp") ||
                     requestURI.endsWith("caseMgt.jsp") ||
                     requestURI.endsWith("nonHumanMgt.jsp") ||
                     requestURI.endsWith("customServices.jsp") ||
                     requestURI.endsWith("calendarMgt.jsp") ||
                     requestURI.endsWith("externalClients.jsp"));
    }

}
