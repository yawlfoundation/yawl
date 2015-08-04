package org.chiba.web.servlet;

import org.apache.log4j.Logger;
import org.chiba.adapter.ui.UIGenerator;
import org.chiba.web.WebAdapter;
import org.chiba.web.session.XFormsSession;
import org.chiba.web.session.XFormsSessionManager;
import org.chiba.xml.xforms.config.Config;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * This servlet follows the PFG pattern (Post, forward, get) and provides the GET part. A seperate GET request at the end
 * of the request processing is used to render the output. This avoids problems with cached POST requests and the user
 * using the back button of the browser. Cause the GET request does not trigger such a warning the user can smoothly step
 * backwards.
 *
 * @author Joern Turner
 * @version $Version: $
 * @see PlainHtmlServlet
 */
public class ViewServlet extends AbstractChibaServlet {
    private static final Logger LOGGER = Logger.getLogger(ViewServlet.class);

    /**
     * Returns a short description of the servlet.
     *
     * @return - Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "responsible for showing the views to the user in Chiba XForms applications";
    }

    /**
     * Destroys the servlet.
     */
    public void destroy() {
    }

    /**
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        WebAdapter webAdapter = null;

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control","private, no-store, no-cache, must-revalidate");
        response.setHeader("Pragma","no-cache");
        response.setHeader("Expires","-1");

        String key = request.getParameter("sessionKey");
        String referer = request.getParameter("referer");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("handling session: " + key);
            LOGGER.debug("referer: " + referer);
        }
        try {
            XFormsSessionManager manager = (XFormsSessionManager) session.getAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER);
            XFormsSession xFormsSession = manager.getXFormsSession(key);
            if (xFormsSession == null) {
                LOGGER.info("session does not exist: " + key + " - creating new one");
                response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/XFormsServlet?" + referer));

            } else {
                webAdapter = xFormsSession.getAdapter();
                if (webAdapter == null) {
                    throw new ServletException(Config.getInstance().getErrorMessage("session-invalid"));
                }
                response.setContentType(HTML_CONTENT_TYPE);

                UIGenerator uiGenerator = (UIGenerator) xFormsSession.getProperty(XFormsSession.UIGENERATOR);
                uiGenerator.setInput(webAdapter.getXForms());
                uiGenerator.setOutput(response.getOutputStream());
                uiGenerator.generate();

                response.getOutputStream().close();
            }
        } catch (Exception e) {
            shutdown(webAdapter, session, e, response, request, key);
        }
    }
}
