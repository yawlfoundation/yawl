/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The jsf ThemeServlet class makes all files on the classpath available via
 * the 'theme' uri. This filter prevents certain files from being accessed via
 * that method.
 */
public class ThemeFilter implements Filter {

	public void init(FilterConfig filterConfig) throws ServletException { }

	public void destroy() {	}


	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {

		if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse)) {

			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpServletResponse httpResponse = (HttpServletResponse) response;

			if (isBlackListed(httpRequest)) {
                httpRequest.getSession();       // stops potential IllegalStateException
				httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		}

		filterChain.doFilter(request, response);
	}


	private boolean isBlackListed(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		return requestURI != null &&
				(requestURI.endsWith(".properties") || requestURI.endsWith(".xml") ||
                 requestURI.endsWith("/") || requestURI.endsWith(".class") ||
                 !requestURI.contains("."));
	}

}
