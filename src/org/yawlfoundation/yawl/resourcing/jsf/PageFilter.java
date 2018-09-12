package org.yawlfoundation.yawl.resourcing.jsf;

import org.yawlfoundation.yawl.resourcing.resource.UserPrivileges;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This filter prevents users from accessing unauthorised (admin) pages by directly
 * typing the admin page's url into the address bar of a browser.
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
        return !isAdminPage(request) || isAdminSession(request) ||
				isPrivilegedSession(request);
    }


    private boolean isAdminSession(HttpServletRequest request) {
        SessionBean sb = getSessionBean(request);
   		return sb != null && sb.isAdminSession();
   	}


   	private boolean isPrivilegedSession(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
        if (requestURI != null && requestURI.endsWith("caseMgt.jsp")) {
			SessionBean sb = getSessionBean(request);
	        if (sb != null) {
				UserPrivileges up = sb.getSessionPrivileges();
				return up != null && up.canManageCases();
			}
		}
		return false;
	}


   	protected SessionBean getSessionBean(HttpServletRequest request) {
   		return (SessionBean) request.getSession().getAttribute("SessionBean");
   	}


    private boolean isAdminPage(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return requestURI != null &&
      				(requestURI.endsWith("orgDataMgt.jsp") ||
                     requestURI.endsWith("caseMgt.jsp") ||
					 requestURI.endsWith("adminQueues.jsp") ||
                     requestURI.endsWith("participantData.jsp") ||
                     requestURI.endsWith("nonHumanMgt.jsp") ||
                     requestURI.endsWith("customServices.jsp") ||
                     requestURI.endsWith("calendarMgt.jsp") ||
                     requestURI.endsWith("externalClients.jsp"));
    }

}
