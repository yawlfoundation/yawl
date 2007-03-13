// Copyright 2006 Chibacon
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
package org.chiba.web.servlet;

import org.apache.commons.httpclient.Cookie;
import org.apache.log4j.Logger;
import org.chiba.adapter.ChibaAdapter;
import org.chiba.adapter.ui.UIGenerator;
import org.chiba.adapter.ui.XSLTGenerator;
import org.chiba.web.WebAdapter;
import org.chiba.web.flux.FluxAdapter;
import org.chiba.web.session.XFormsSession;
import org.chiba.web.session.XFormsSessionManager;
import org.chiba.web.session.impl.DefaultXFormsSessionManagerImpl;
import org.chiba.xml.events.XMLEvent;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.config.XFormsConfigException;
import org.chiba.xml.xforms.connector.http.AbstractHTTPConnector;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xslt.TransformerService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
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
 * @version $Id: ChibaServlet.java,v 1.21 2007/02/28 11:48:34 joernt Exp $
 */
public class ChibaServlet extends AbstractChibaServlet {
    //init-params
    private static final Logger LOGGER = Logger.getLogger(ChibaServlet.class);

    private static final String FORM_PARAM_NAME = "form";
    private static final String XSL_PARAM_NAME = "xslt";
    private static final String ACTIONURL_PARAM_NAME = "action_url";
    private static final String JAVASCRIPT_PARAM_NAME = "JavaScript";
    public static final String CHIBA_SUBMISSION_RESPONSE = "chiba.submission.response";

    /*
     * It is not thread safe to modify these variables once the
     * init(ServletConfig) method has been called
     */
    // the absolute path to the Chiba config-file
    protected String configPath = null;

    // the rootdir of this app; forms + documents fill be searched under this root
    protected String contextRoot = null;

    // where uploaded files are stored
    protected String uploadDir = null;

    protected String xsltPath = null;
    protected String xsltDefault = null;

    /**
     * path to javascript files as used by UIGenerator
     */
    protected String scriptPath = null;
    protected String sessionManager = null;
    protected String plain_html_agent;
    protected String ajax_agent;

    /**
     * path to core CSS file that holds vital XForms CSS rules
     */
    protected String cssPath = null;
    private String processorBase=null;
    private String formsDir=null;

    private static String YAWLID = new String(); // edited

    /**
     * Returns a short description of the servlet.
     *
     * @return - Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Servlet Controller for Chiba XForms Processor";
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

        LOGGER.debug("--------------- initing ChibaServlet... ---------------");
        contextRoot = getServletConfig().getServletContext().getRealPath("");
        if (contextRoot == null)
            contextRoot = getServletConfig().getServletContext().getRealPath(".");

        formsDir = getServletConfig().getServletContext().getInitParameter("chiba.forms");

        String path = getServletConfig().getInitParameter("chiba.config");
        if (path != null) {
            configPath = getServletConfig().getServletContext().getRealPath(path);
        }

        xsltPath = getServletConfig().getServletContext().getInitParameter("chiba.web.xslt.path");
        xsltDefault = getServletConfig().getServletContext().getInitParameter("chiba.web.xslt.default");

        uploadDir = getServletConfig().getServletContext().getInitParameter("chiba.upload");
        if (uploadDir != null) {
            if (uploadDir.toUpperCase().indexOf("WEB-INF") >= 0) {
                throw new ServletException("Chiba security constraint: uploadDir '" + uploadDir + "' not allowed");
            }
        }

        scriptPath = getServletConfig().getInitParameter("scriptPath");
        if (scriptPath == null) {
            LOGGER.warn("Script path not configured");
        }
        sessionManager = getServletConfig().getInitParameter("sessionManager");
        if (sessionManager == null) {
            LOGGER.warn("Session manager not configured - using default SessionManager");
        }

        cssPath = getServletConfig().getInitParameter("CSSPath");
        if (cssPath == null) {
            LOGGER.warn("CSS path not configured");
        }

        //user-agent mappings
        plain_html_agent = getServletConfig().getServletContext().getInitParameter("chiba.useragent.plainhtml.path");
        ajax_agent = getServletConfig().getServletContext().getInitParameter("chiba.useragent.ajax.path");

        String wipingInterval = getServletConfig().getInitParameter("XFormsSessionChecking");
        if(wipingInterval == null ) {
            wipingInterval = "0";
            LOGGER.warn("Wiping interval is set to '0' - XFormsSessions will not expire");
        }

        String xformsSessionTimeout = getServletConfig().getInitParameter("XFormsSessionTimeout");
        if(xformsSessionTimeout == null ) xformsSessionTimeout = "0";

        processorBase = getServletConfig().getInitParameter("defaultProcessorBase");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Context root: " + contextRoot);
            LOGGER.debug("Forms dir: " + formsDir);
            LOGGER.debug("Config path: " + configPath);
            LOGGER.debug("XSLT path: " + xsltPath);
            LOGGER.debug("Upload path: " + uploadDir);
            LOGGER.debug("Script path: " + scriptPath);
            LOGGER.debug("Session manager: " + sessionManager);
            LOGGER.debug("CSS path: " + cssPath);
            LOGGER.debug("Session wiping interval: " + wipingInterval);
            LOGGER.debug("Session timeout: " + xformsSessionTimeout);
            LOGGER.debug("Processor base: " + processorBase);
        }
        try {
            createXFormsSessionManager(Integer.parseInt(wipingInterval), Integer.parseInt(xformsSessionTimeout), sessionManager);
        } catch (XFormsConfigException e) {
            LOGGER.fatal(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Starts a new form-editing session.<br>
     * <p/>
     * The default value of a number of settings can be overridden as follows:
     * <p/>
     * 1. The uru of the xform to be displayed can be specified by using a param name of 'form' and a param value
     * of the location of the xform file as follows, which will attempt to load the current xforms file.
     * <p/>
     * http://localhost:8080/chiba-0.9.3/XFormsServlet?form=/forms/hello.xhtml
     * <p/>
     * 2. The uri of the XSLT file used to generate the form can be specified using a param name of 'xslt' as follows:
     * <p/>
     * http://localhost:8080/chiba-0.9.3/XFormsServlet?form=/forms/hello.xhtml&xslt=/chiba/my.xslt
     * <p/>
     * 3. Besides these special params arbitrary other params can be passed via the GET-string and will be available
     * in the context map of ChibaBean. This means they can be used as instance data (with the help of ContextResolver)
     * or to set params for URI resolution.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     * @see org.chiba.xml.xforms.connector.context.ContextResolver
     * @see org.chiba.xml.xforms.connector.ConnectorFactory
     */
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        WebAdapter adapter = null;
        HttpSession session = request.getSession(true);

        XFormsSessionManager sessionManager = getXFormsSessionManager();
        XFormsSession xFormsSession = sessionManager.createXFormsSession();

        YAWLID = request.getParameter("JSESSIONID");
        
        /*
        the XFormsSessionManager is kept in the http-session though it is accessible as singleton. Subsequent
        servlets should access the manager through the http-session attribute as below to ensure the http-session
        is refreshed.
        */
        session.setAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER,sessionManager);

        if (LOGGER.isDebugEnabled()) {
            printSessionKeys(session);
            LOGGER.debug("created XFormsSession with key: " + xFormsSession.getKey());
        }

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control","private, no-store,  no-cache, must-revalidate");
        response.setHeader("Pragma","no-cache");
        response.setDateHeader("Expires",-1);

        try {
            // determine Form to load
            String formURI = getRequestURI(request) + request.getParameter(FORM_PARAM_NAME);
            if (formURI == null) {
                throw new IOException("Resource not found: " + formURI + " not found");
            }

            String xslFile = request.getParameter(XSL_PARAM_NAME);
            String javascriptPresent = request.getParameter(JAVASCRIPT_PARAM_NAME);

            String actionURL = null;
            if (javascriptPresent != null) {
                //do AJAX
                adapter = new FluxAdapter();
                actionURL = getActionURL(request, response, true);
            } else {
                //do standard browser support without scripting
                adapter = new ServletAdapter();
                actionURL = getActionURL(request, response, false);
            }
            adapter.setXFormsSession(xFormsSession);

            //setup Adapter
            adapter = setupAdapter(adapter, xFormsSession.getKey(), formURI);
            setContextParams(request, adapter);
            storeCookies(request, adapter);
            storeAcceptLanguage(request, adapter);
            adapter.init();
            XMLEvent exitEvent = adapter.checkForExitEvent();

            if (exitEvent != null) {
                handleExit(exitEvent, xFormsSession, session,  request, response);
            } else {
                //todo: review: the following may be substituted with the ViewServlet
                response.setContentType(HTML_CONTENT_TYPE);
                UIGenerator uiGenerator = createUIGenerator(request, xFormsSession, actionURL, xslFile == null ? xsltDefault : xslFile, javascriptPresent);
                uiGenerator.setInput(adapter.getXForms());
                uiGenerator.setOutput(response.getOutputStream());
                uiGenerator.generate();

                //store WebAdapter in XFormsSession
                xFormsSession.setAdapter(adapter);
                //store UIGenerator in XFormsSession as property
                xFormsSession.setProperty(XFormsSession.UIGENERATOR, uiGenerator);
                //store queryString as 'referer' in XFormsSession
                xFormsSession.setProperty(XFormsSession.REFERER,request.getQueryString());
                //actually add the XFormsSession ot the manager
                sessionManager.addXFormsSession(xFormsSession);
            }

            printSessionKeys(session);

        }
        catch (Exception e) {
            shutdown(adapter, session, e, response, request, xFormsSession.getKey());
        }
    }

    /**
     * factory method to create and setup an XFormsSessionManager. Overwrite this to provide your own implementation.
     *
     * @param wipingInterval defines the time in millis between checks for expired XFormsSessions
     * @param timeout a timeout in millis for the XFormsSession
     * @param className the fully qualified classname of the XFormsSessionManager to use
     * @throws org.chiba.xml.xforms.config.XFormsConfigException thrown if no usable XFormsSessionManager implementation around
     */
    protected void createXFormsSessionManager(int wipingInterval, int timeout, String className) throws XFormsConfigException {
        XFormsSessionManager manager=null;
        if(className==null) {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Default XFormsSessionManager choosen" );
            }
            manager = getXFormsSessionManager();

        } else {
            try {
                if(LOGGER.isDebugEnabled()) {
                    LOGGER.debug("XFormsSessionManager: " + className  );
                }
                Class clazz = Class.forName(className);
                manager  = (XFormsSessionManager)clazz.getMethod("getInstance", (Class[])null).invoke(clazz,(Object[])null);
            }
            catch (ClassCastException cce) {
                throw new XFormsConfigException(cce);
            } catch (IllegalAccessException e) {
                throw new XFormsConfigException(e);
            } catch (ClassNotFoundException e) {
                throw new XFormsConfigException(e);
            } catch (NoSuchMethodException e) {
                LOGGER.fatal("Class '" + className +"' has no getInstance method") ;
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                LOGGER.fatal("Error invoking Class '" + className +"'") ;
                e.printStackTrace();
            }
        }

        /*
        Chiba Web comes with a default web.xml containing a default value of 120 Secs between expiry checks.
        If the value is not present in web.xml 0 is assumed meaning no expiry checking at all.
         */
        if(wipingInterval != 0){
            manager.setInterval(wipingInterval);
        }else{
            manager.setInterval(0);// no XFormsSession expiry takes place
        }

        if (timeout != 0){
            manager.setTimeout(timeout);
        }else{
            manager.setTimeout(1000 * 60); // 1 minute session lifetime (expressed in milliseconds)
        }

        //start running the session cleanup

        manager.init();

    }

    /**
     * returns a specific implementation of XFormsSessionManager. Plugin your own implementations here if needed.
     *
     * @return a specific implementation of XFormsSessionManager (defaults to DefaultXFormsSessionManagerImpl)
     */
    protected XFormsSessionManager getXFormsSessionManager(){
       return DefaultXFormsSessionManagerImpl.getInstance();
    }

    /**
     * configures the an Adapter for interacting with the XForms processor (ChibaBean). The Adapter itself
     * will create the XFormsProcessor (ChibaBean) and configure it for processing.
     * <p/>
     * If you'd like to use a different source of XForms documents e.g. DOM you should extend this class and
     * overwrite this method. Please take care to also set the baseURI of the processor to a reasonable value
     * cause this will be the fundament for all URI resolutions taking place.
     *
     * @param adapter  the WebAdapter implementation to setup
     * @param formPath - the relative location where forms are stored
     * @return ServletAdapter
     * @throws java.net.URISyntaxException in case the passed formPath cannot be converted to an URI
     * @param sessionKey a generated sessionKey for representing the new XFormsSession
     * @throws org.chiba.xml.xforms.exception.XFormsException in case Adapter cannot be created
     */
    protected WebAdapter setupAdapter(WebAdapter adapter,
                                      String sessionKey,
                                      String formPath
    ) throws XFormsException, URISyntaxException {

        if ((configPath != null) && !(configPath.equals(""))) {
            adapter.setConfigPath(configPath);
        }
        adapter.setXForms(new URI(formPath));
        String baseURI;
        if (processorBase == null || processorBase.equalsIgnoreCase("remote") ) {
            baseURI = formPath;
        }
        else {
            baseURI = new File(contextRoot, formsDir).toURI().toString();
        }
        adapter.setBaseURI(baseURI);
        adapter.setUploadDestination(new File(contextRoot, uploadDir).getAbsolutePath());

        Map servletMap = new HashMap();
        servletMap.put(WebAdapter.SESSION_ID, sessionKey);
        adapter.setContextParam(ChibaAdapter.SUBMISSION_RESPONSE, servletMap);

        return adapter;
    }

    /**
     * stores cookies that may exist in request and passes them on to processor for usage in
     * HTTPConnectors. Instance loading and submission then uses these cookies. Important for
     * applications using auth.
     *
     * @param request the servlet request
     * @param adapter the WebAdapter instance
     */
    protected void storeCookies(HttpServletRequest request, WebAdapter adapter) {
        javax.servlet.http.Cookie[] cookiesIn = request.getCookies();
        if (cookiesIn != null) {
            Cookie[] commonsCookies = new org.apache.commons.httpclient.Cookie[cookiesIn.length];
            for (int i = 0; i < cookiesIn.length; i += 1) {
                javax.servlet.http.Cookie c = cookiesIn[i];
            
	            if (c.getName().compareTo("JSESSIONID") == 0){
	                commonsCookies[i] = new Cookie(c.getDomain(),
	                                              c.getName(),
	                                              YAWLID,
	                                              c.getPath(),
	                                              c.getMaxAge(),
	                                              c.getSecure());
		        }
		        else{
		        	commonsCookies[i] = new Cookie(c.getDomain(),
		                      c.getName(),
		                      c.getValue(),
		                      c.getPath(),
		                      c.getMaxAge(),
		                      c.getSecure());
		        }
            }
            adapter.setContextParam(AbstractHTTPConnector.REQUEST_COOKIE, commonsCookies);
        }
    }

    protected void storeAcceptLanguage(HttpServletRequest request, WebAdapter adapter) {
        String acceptLanguage = request.getHeader("accept-language");
        if ((acceptLanguage != null) && (acceptLanguage.length() > 0)){
            adapter.setContextParam(AbstractHTTPConnector.ACCEPT_LANGUAGE, acceptLanguage);
        }
    }

    protected UIGenerator createUIGenerator(HttpServletRequest request, XFormsSession session, String actionURL, String xslFile, String js) throws URISyntaxException, XFormsConfigException {
        TransformerService transformerService = (TransformerService) getServletContext().getAttribute(TransformerService.class.getName());
        URI uri = new File(getServletContext().getRealPath(xsltPath)).toURI().resolve(new URI(xslFile));

        XSLTGenerator generator = new XSLTGenerator();
        generator.setTransformerService(transformerService);
        generator.setStylesheetURI(uri);

        // todo: unify and extract parameter names
        generator.setParameter("contextroot", request.getContextPath());
        generator.setParameter("sessionKey", session.getKey());
        if(session.getProperty(XFormsSession.KEEPALIVE_PULSE) != null){
            generator.setParameter("keepalive-pulse",session.getProperty(XFormsSession.KEEPALIVE_PULSE));
        }
        generator.setParameter("action-url", actionURL);
        generator.setParameter("debug-enabled", String.valueOf(LOGGER.isDebugEnabled()));
        String selectorPrefix = Config.getInstance().getProperty(HttpRequestHandler.SELECTOR_PREFIX_PROPERTY,
                HttpRequestHandler.SELECTOR_PREFIX_DEFAULT);
        generator.setParameter("selector-prefix", selectorPrefix);
        String removeUploadPrefix = Config.getInstance().getProperty(HttpRequestHandler.REMOVE_UPLOAD_PREFIX_PROPERTY,
                HttpRequestHandler.REMOVE_UPLOAD_PREFIX_DEFAULT);
        generator.setParameter("remove-upload-prefix", removeUploadPrefix);
        String dataPrefix = Config.getInstance().getProperty("chiba.web.dataPrefix");
        generator.setParameter("data-prefix", dataPrefix);

        String triggerPrefix = Config.getInstance().getProperty("chiba.web.triggerPrefix");
        generator.setParameter("trigger-prefix", triggerPrefix);

        generator.setParameter("user-agent", request.getHeader("User-Agent"));

        generator.setParameter("scripted", String.valueOf(js != null));
        if (scriptPath != null) {
            generator.setParameter("scriptPath", scriptPath);
        }
        if (cssPath != null) {
            generator.setParameter("CSSPath", cssPath);
        }

        return generator;
    }

    /**
     * this method is responsible for passing all context information needed by the Adapter and Processor from
     * ServletRequest to ChibaContext. Will be called only once when the form-session is inited (GET).
     *
     * @param request    the ServletRequest
     * @param webAdapter the ChibaAdapter to use
     */
    protected void setContextParams(HttpServletRequest request, WebAdapter webAdapter) {

        //[1] pass user-agent to Adapter for UI-building
        webAdapter.setContextParam(WebAdapter.USERAGENT, request.getHeader("User-Agent"));
        webAdapter.setContextParam(WebAdapter.REQUEST_URI,getRequestURI(request));

        //[2] read any request params that are *not* Chiba params and pass them into the context map
        Enumeration params = request.getParameterNames();
        String s;
        while (params.hasMoreElements()) {
            s = (String) params.nextElement();
            //store all request-params we don't use in the context map of ChibaBean
            if (!(s.equals(FORM_PARAM_NAME) ||
                    s.equals(XSL_PARAM_NAME) ||
                    s.equals(ACTIONURL_PARAM_NAME) ||
                    s.equals(JAVASCRIPT_PARAM_NAME))) {
                String value = request.getParameter(s);
                webAdapter.setContextParam(s, value);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("added request param '" + s + "' added to context");
                }
            }
        }
    }

    private String getActionURL(HttpServletRequest request, HttpServletResponse response, boolean scripted) {

        String defaultActionURL = null;
        if (scripted) {

            defaultActionURL = getRequestURI(request) + ajax_agent;
        } else {
            defaultActionURL = getRequestURI(request) + plain_html_agent;
        }
        String encodedDefaultActionURL = response.encodeURL(defaultActionURL);
        int sessIdx = encodedDefaultActionURL.indexOf(";jsession");
        String sessionId = null;
        if (sessIdx > -1) {
            sessionId = encodedDefaultActionURL.substring(sessIdx);
        }
        String actionURL = request.getParameter(ACTIONURL_PARAM_NAME);
        if (null == actionURL) {
            actionURL = encodedDefaultActionURL;
        } else if (null != sessionId) {
            actionURL += sessionId;
        }

        LOGGER.debug("actionURL: " + actionURL);
        // encode the URL to allow for session id rewriting
        actionURL = response.encodeURL(actionURL);
        return actionURL;
    }

    private String getRequestURI(HttpServletRequest request) {
        StringBuffer buffer = new StringBuffer(request.getScheme());
        buffer.append("://");
        buffer.append(request.getServerName());
        buffer.append(":");
        buffer.append(request.getServerPort());
        buffer.append(request.getContextPath());
        return buffer.toString();
    }

    private void printSessionKeys(HttpSession session) {
        LOGGER.debug("--------------- session dump ---------------");
        Enumeration keys = session.getAttributeNames();
        if (keys.hasMoreElements()) {
            LOGGER.debug("--- existing keys in session --- ");
            while (keys.hasMoreElements()) {
                String s = (String) keys.nextElement();
                LOGGER.debug("existing sessionkey: " + s + ":" + session.getAttribute(s));
            }
        } else {
            LOGGER.debug("--- no keys present in session ---");
        }
    }


}

// end of class
