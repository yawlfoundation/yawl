/*
 *
 *    Artistic License
 *
 *    Preamble
 *
 *    The intent of this document is to state the conditions under which a Package may be copied, such that
 *    the Copyright Holder maintains some semblance of artistic control over the development of the
 *    package, while giving the users of the package the right to use and distribute the Package in a
 *    more-or-less customary fashion, plus the right to make reasonable modifications.
 *
 *    Definitions:
 *
 *    "Package" refers to the collection of files distributed by the Copyright Holder, and derivatives
 *    of that collection of files created through textual modification.
 *
 *    "Standard Version" refers to such a Package if it has not been modified, or has been modified
 *    in accordance with the wishes of the Copyright Holder.
 *
 *    "Copyright Holder" is whoever is named in the copyright or copyrights for the package.
 *
 *    "You" is you, if you're thinking about copying or distributing this Package.
 *
 *    "Reasonable copying fee" is whatever you can justify on the basis of media cost, duplication
 *    charges, time of people involved, and so on. (You will not be required to justify it to the
 *    Copyright Holder, but only to the computing community at large as a market that must bear the
 *    fee.)
 *
 *    "Freely Available" means that no fee is charged for the item itself, though there may be fees
 *    involved in handling the item. It also means that recipients of the item may redistribute it under
 *    the same conditions they received it.
 *
 *    1. You may make and give away verbatim copies of the source form of the Standard Version of this
 *    Package without restriction, provided that you duplicate all of the original copyright notices and
 *    associated disclaimers.
 *
 *    2. You may apply bug fixes, portability fixes and other modifications derived from the Public Domain
 *    or from the Copyright Holder. A Package modified in such a way shall still be considered the
 *    Standard Version.
 *
 *    3. You may otherwise modify your copy of this Package in any way, provided that you insert a
 *    prominent notice in each changed file stating how and when you changed that file, and provided that
 *    you do at least ONE of the following:
 *
 *        a) place your modifications in the Public Domain or otherwise make them Freely
 *        Available, such as by posting said modifications to Usenet or an equivalent medium, or
 *        placing the modifications on a major archive site such as ftp.uu.net, or by allowing the
 *        Copyright Holder to include your modifications in the Standard Version of the Package.
 *
 *        b) use the modified Package only within your corporation or organization.
 *
 *        c) rename any non-standard executables so the names do not conflict with standard
 *        executables, which must also be provided, and provide a separate manual page for each
 *        non-standard executable that clearly documents how it differs from the Standard
 *        Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    4. You may distribute the programs of this Package in object code or executable form, provided that
 *    you do at least ONE of the following:
 *
 *        a) distribute a Standard Version of the executables and library files, together with
 *        instructions (in the manual page or equivalent) on where to get the Standard Version.
 *
 *        b) accompany the distribution with the machine-readable source of the Package with
 *        your modifications.
 *
 *        c) accompany any non-standard executables with their corresponding Standard Version
 *        executables, giving the non-standard executables non-standard names, and clearly
 *        documenting the differences in manual pages (or equivalent), together with instructions
 *        on where to get the Standard Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    5. You may charge a reasonable copying fee for any distribution of this Package. You may charge
 *    any fee you choose for support of this Package. You may not charge a fee for this Package itself.
 *    However, you may distribute this Package in aggregate with other (possibly commercial) programs as
 *    part of a larger (possibly commercial) software distribution provided that you do not advertise this
 *    Package as a product of your own.
 *
 *    6. The scripts and library files supplied as input to or produced as output from the programs of this
 *    Package do not automatically fall under the copyright of this Package, but belong to whomever
 *    generated them, and may be sold commercially, and may be aggregated with this Package.
 *
 *    7. C or perl subroutines supplied by you and linked into this Package shall not be considered part of
 *    this Package.
 *
 *    8. The name of the Copyright Holder may not be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 *    9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 *    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 *    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package org.chiba.adapter.web;

import org.apache.log4j.Category;
import org.chiba.adapter.ChibaAdapter;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.exception.XFormsException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The ChibaServlet handles all interactions between client and
 * form-processor (ChibaBean) for the whole lifetime of a form-filling session.
 * <br>
 * The Processor will be started through a Get-request from the client
 * pointing to the desired form-container. The Processor instance will
 * be stored in a Session-object.<br>
 * <br>
 * All further interaction will be handled through Post-requests.
 * Incoming request params will be mapped to data and action-handlers.
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @author William Boyd
 * @version $Version: $
 */
public class ChibaServlet extends HttpServlet {
    //init-params
    private static Category cat = Category.getInstance(ChibaServlet.class);

    private static final String FORM_PARAM_NAME= "form";
    private static final String XSL_PARAM_NAME = "xslt";
    private static final String CSS_PARAM_NAME = "css";
    private static final String ACTIONURL_PARAM_NAME = "action_url";
    private static String YAWLID = new String(); // edited
    
    /*
     * It is not thread safe to modify these variables once the
     * init(ServletConfig) method has been called
     */
    // the absolute path to the Chiba config-file
    private String configPath = null;

    // the rootdir of this app; forms + documents fill be searched under this root
    private String contextRoot = null;

    // where uploaded files are stored
    private String uploadDir = null;

    private String stylesPath = null;

    /**
     * Returns a short description of the servlet.
     *
     * @return - Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Servlet Controller for YAWL XForms Processor";
    }

    /**
     * Destroys the servlet.
     */
    public void destroy() {
    }

    /**
     * Initializes the servlet.
     *
     * @param config - the ServletConfig object
     * @throws javax.servlet.ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        cat.info("--------------- initing ChibaServlet... ---------------");
        //read some params from web-inf
        contextRoot = getServletConfig().getServletContext().getRealPath("");
        if (contextRoot == null)
            contextRoot = getServletConfig().getServletContext().getRealPath(".");

        //get the relative path to the chiba config-file
        String path = getServletConfig().getInitParameter("chiba.config");

        //get the real path for the config-file
        if (path != null) {
            configPath = getServletConfig().getServletContext().getRealPath(path);
        }

        //get the path for the stylesheets
        path = getServletConfig().getServletContext().getInitParameter("chiba.xforms.stylesPath");

        //get the real path for the stylesheets and configure a new StylesheetLoader with it
        if (path != null) {
            stylesPath = getServletConfig().getServletContext().getRealPath(path);
            cat.info("stylesPath: " + stylesPath);
        }

        //uploadDir = contextRoot	+ "/" + getServletConfig().getServletContext().getInitParameter("chiba.upload");
        uploadDir = getServletConfig().getServletContext().getInitParameter("chiba.upload");

        //Security constraint
        if (uploadDir != null) {
            if (uploadDir.toUpperCase().indexOf("WEB-INF") >= 0) {
                throw new ServletException("Chiba security constraint: uploadDir '" + uploadDir + "' not allowed");
            }
        }
    }

    /**
     * Starts a new form-editing session.<br>
     * <p/>
     * The default value of a number of settings can be overridden as follows:
     * <p/>
     * 1. The uri of the xform to be displayed can be specified by using a param name of 'form' and a param value
     * of the location of the xform file as follows, which will attempt to load the current xforms file.
     * <p/>
     * http://localhost:8080/chiba-0.9.3/XFormsServlet?form=/forms/hello.xhtml
     * <p/>
     * 2. The uri of the CSS file used to style the form can be specified using a param name of 'css' as follows:
     * <p/>
     * http://localhost:8080/chiba-0.9.3/XFormsServlet?form=/forms/hello.xhtml&css=/chiba/my.css
     * <p/>
     * 3. The uri of the XSLT file used to generate the form can be specified using a param name of 'xslt' as follows:
     * <p/>
     * http://localhost:8080/chiba-0.9.3/XFormsServlet?form=/forms/hello.xhtml&xslt=/chiba/my.xslt
     * <p/>
     * 4. Besides these special params arbitrary other params can be passed via the GET-string and will be available
     * in the context map of ChibaBean. This means they can be used as instance data (with the help of ContextResolver)
     * or to set params for URI resolution.
     *
     * @see org.chiba.xml.xforms.connector.context.ContextResolver
     * @see org.chiba.xml.xforms.connector.ConnectorFactory
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        ServletAdapter servletAdapter = null;
        HttpSession session = request.getSession(true);
        
        //cat.info("request.parameter(session) "+request.getParameter("sessionHandle"));
        //cat.info("request.parameter(JSESSIONID) "+request.getParameter("JSESSIONID"));
        
        YAWLID = request.getParameter("JSESSIONID");
        
        cat.info("--------------- new XForms session ---------------");
        try {
            response.setContentType("text/html");
            java.io.PrintWriter out = response.getWriter();

            // determine Form to load
            String formPath = request.getParameter(FORM_PARAM_NAME);
            if (formPath == null) {
                throw new IOException("File: " + formPath + " not found");
            }

            String xslFile = request.getParameter(XSL_PARAM_NAME);
            String css = request.getParameter(CSS_PARAM_NAME);

            // build actionURL where forms are submitted to
            String actionURL = getActionURL(request, response);

            servletAdapter = setupServletAdapter(actionURL, session, formPath, xslFile, css);
            updateContext(servletAdapter, request, session);

            //add all request params that are not used by this servlet to the context map in ChibaBean
            //cat.info("CS: storeContextParams");
            storeContextParams(request, servletAdapter);
            servletAdapter.init();
            servletAdapter.executeHandler();
            servletAdapter.buildUI(out);
            session.setAttribute("chiba.adapter", servletAdapter);
            out.close();
        } catch (Exception e) {
            // attempt to shutdown processor
            if (servletAdapter != null && servletAdapter.getChibaBean() != null) {
                try {
                    servletAdapter.getChibaBean().shutdown();
                } catch (XFormsException xfe) {
                    xfe.printStackTrace();
                }
            }

            // store exception
            session.setAttribute("chiba.exception", e);

            // redirect to error page (after encoding session id if required)
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/" +
                    request.getSession().getServletContext().getInitParameter("error.page")));
        }
    }

    /**
     * this method is responsible for passing all context information needed by the Adapter and Processor from
     * ServletRequest to ChibaContext.
     *
     * @param servletAdapter the ChibaAdapter to use
     * @param request        the ServletRequest
     * @param session        the ServletSession
     */
    protected void updateContext(ServletAdapter servletAdapter, HttpServletRequest request, HttpSession session) {
        servletAdapter.setContextProperty(ServletAdapter.USERAGENT, request.getHeader("User-Agent"));
        servletAdapter.setContextProperty(ServletAdapter.HTTP_SERVLET_REQUEST, request);
        servletAdapter.setContextProperty(ServletAdapter.HTTP_SESSION_OBJECT, session);
    }

    
    /**
     * handles all interaction with the user during a form-session.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        ServletAdapter servletAdapter = null;

        try {
            servletAdapter = (ServletAdapter) session.getAttribute("chiba.adapter");
            if (servletAdapter == null) {
                throw new ServletException(Config.getInstance().getErrorMessage("session-invalid"));
            }
            updateContext(servletAdapter, request, session);
            servletAdapter.executeHandler();

            // handle setRedirect <xforms:load show='replace'/>
            // and redirect from submission as well
            // NOTE - this needs to be checked *before* the this.getForwardMap()
            // as a submission handler may force a redirect
            if (servletAdapter.getRedirectUri() != null) {
            	//cat.info("doPost getRedirectURI");
                String redirectTo = servletAdapter.getRedirectUri();
                // todo: remove from session ?
                // shutdown processor
                servletAdapter.getChibaBean().shutdown();

                // send redirect (after encoding session id if required)
                response.sendRedirect(response.encodeRedirectURL(redirectTo));

                // remove redirect uri and terminate
                servletAdapter.setRedirect(null);
                return;
            }

            //servletAdapter.getChibaBean().getContext().put("JSESSIONID", YAWLID); // edited
            
            // handle forward <xforms:submission replace='all'/>
            Map forwardMap = servletAdapter.getForwardMap();
            forwardMap.put("Set-Cookie", "JSESSIONID="+YAWLID); // YAWL edit to overwrite the session cookie
            InputStream forwardStream = (InputStream) forwardMap.get(ChibaAdapter.SUBMISSION_RESPONSE_STREAM);
            if (forwardStream != null) {
            	//cat.info("doPost forward");
                // todo: remove from session ?
                // shutdown processor
                servletAdapter.getChibaBean().shutdown();
               
                // forward submission response
                forwardResponse(forwardMap, response);

                // remove forward response and terminate
                servletAdapter.forward(null);
                return;
            }

            // set content type
            response.setContentType("text/html");

            // render result to output
            servletAdapter.buildUI(response.getWriter());
            response.getWriter().close();
        } catch (Exception e) {
            // attempt to shutdown processor
            if (servletAdapter != null && servletAdapter.getChibaBean() != null) {
                try {
                    servletAdapter.getChibaBean().shutdown();
                } catch (XFormsException xfe) {
                    xfe.printStackTrace();
                }
            }

            // store exception
            session.setAttribute("chiba.exception", e);

            // redirect to error page (after encoding session id if required)
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/" +
                    request.getSession().getServletContext().getInitParameter("error.page")));
        }
    }

    /**
     * creates and configures the ServletAdapter which does the actual request processing.
     *
     * @param actionURL - the URL to submit to
     * @param session   - the Servlet session
     * @param formPath  - the relative location where forms are stored
     * @param xslFile   - the xsl file to use for transform
     * @param cssFile   - the CSS file to use for styling the output
     * @return ServletAdapter
     */
    private ServletAdapter setupServletAdapter(String actionURL,
                                               HttpSession session,
                                               String formPath,
                                               String xslFile,
                                               String cssFile)
            {
        //setup and configure the adapter
        ServletAdapter aAdapter = new ServletAdapter();
        aAdapter.setContextRoot(contextRoot);
        if ((configPath != null) && !(configPath.equals(""))) {
            aAdapter.setConfigPath(configPath);
        }
        aAdapter.setFormPath(formPath);
        aAdapter.setStylesheetPath(stylesPath);
        aAdapter.setActionUrl(actionURL);
        aAdapter.setUploadDir(uploadDir);

        if (xslFile != null) {
            aAdapter.setStylesheet(xslFile);
            if (cat.isDebugEnabled()) {
                cat.debug("using xsl stylesheet: " + xslFile);
            }
        }
        if (cssFile != null) {
            aAdapter.setCSS(cssFile);
            if (cat.isDebugEnabled()) {
                cat.debug("using css stylesheet: " + cssFile);
            }
        }

        Map servletMap = new HashMap();
        servletMap.put(ChibaAdapter.SESSION_ID, YAWLID); // edited
        //servletMap.put(ChibaAdapter.SESSION_ID, session.getId()); // replaced session handle servlet parameter
        aAdapter.setContextProperty(ChibaAdapter.SUBMISSION_RESPONSE, servletMap);

        return aAdapter;
    }

    private void storeContextParams(HttpServletRequest request, ServletAdapter servletAdapter) {
        Enumeration params = request.getParameterNames();
        String s;
        while (params.hasMoreElements()) {
            s = (String) params.nextElement();
            //store all request-params we don't use in the context map of ChibaBean
            if(!(s.equals(FORM_PARAM_NAME) || s.equals(XSL_PARAM_NAME) || s.equals(CSS_PARAM_NAME) || s.equals(ACTIONURL_PARAM_NAME))){
                String value = request.getParameter(s);
                servletAdapter.setContextProperty(s,value);

                //cat.info("CS :: added request param '" + s + "' to context: "+value);
            }
        }
    }

    private String getActionURL(HttpServletRequest request, HttpServletResponse response) {
        String defaultActionURL =
                request.getScheme()
                + "://"
                + request.getServerName()
                + ":"
                + request.getServerPort()
                + request.getContextPath()
                + request.getServletPath();
        String encodedDefaultActionURL = response.encodeURL(defaultActionURL);
        cat.info("encodedDefaultActionURL: "+encodedDefaultActionURL);
        int sessIdx = encodedDefaultActionURL.indexOf(";jsession");
        String sessionId = null;
        if (sessIdx > -1) {
            sessionId = encodedDefaultActionURL.substring(sessIdx);
            //cat.info("YAWLID = "+YAWLID);
            //sessionId = YAWLID;
            //cat.info("sessionId = "+sessionId);
        }
        String actionURL = request.getParameter(ACTIONURL_PARAM_NAME);
        	
        //cat.info("ACTIONURL_PARAM_NAME: "+request.getParameter(ACTIONURL_PARAM_NAME));
        if (null == actionURL) {
            actionURL = encodedDefaultActionURL;
            //cat.info("in null == actionURL: "+actionURL);
        } else if (null != sessionId) {
            actionURL += sessionId;
            //cat.info("in null == sessionId");
        }

        //cat.info("actionURL: " + actionURL);
        // encode the URL to allow for session id rewriting
        actionURL = response.encodeURL(actionURL);
        return actionURL;
    }

    private void forwardResponse(Map forwardMap, HttpServletResponse response) throws IOException {
        // fetch response stream
        InputStream responseStream = (InputStream) forwardMap.remove(ChibaAdapter.SUBMISSION_RESPONSE_STREAM);

        // copy header information
        Iterator iterator = forwardMap.keySet().iterator();
        while (iterator.hasNext()) {
            String name = iterator.next().toString();
            String value = forwardMap.get(name).toString();
            response.setHeader(name, value);
            cat.info("RESPONSE HEADER SET: "+name+", "+value);
        }

        // copy stream content
        OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
        for (int b = responseStream.read();
             b > -1;
             b = responseStream.read()) {
            outputStream.write(b);
        }

        // close streams
        responseStream.close();
        outputStream.close();
    }

}

// end of class
