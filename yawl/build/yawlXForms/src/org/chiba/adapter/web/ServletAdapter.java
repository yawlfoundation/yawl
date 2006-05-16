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
import org.chiba.adapter.InteractionHandler;
import org.chiba.tools.xslt.StylesheetLoader;
import org.chiba.tools.xslt.UIGenerator;
import org.chiba.tools.xslt.XSLTGenerator;
import org.chiba.xml.xforms.ChibaBean;
import org.chiba.xml.xforms.XFormsConstants;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Node;

import java.io.File;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * integrates XForms Processor into Web-applications and handles request processing. This is the default
 * implementation of ChibaAdapter and besides handling the interaction it also
 * manages a UIGenerator to build the rendered output for the browser.
 *
 * @author joern turner
 * @version $Id: ServletAdapter.java,v 1.37 2004/12/06 23:24:14 joernt Exp $
 */
public class ServletAdapter implements ChibaAdapter{

    private static final Category LOGGER = Category.getInstance(ServletAdapter.class);
    public static final String HTTP_SERVLET_REQUEST = "chiba.web.request";
    public static final String HTTP_SESSION_OBJECT = "chiba.web.session";
    public static final String HTTP_UPLOAD_DIR = "chiba.web.uploadDir";

    private ChibaBean chibaBean = null;
    private String configPath = null;
    private String formPath = null;
    private URI formURI = null;
    private Node formNode = null;
    private String actionUrl = null;
    private String CSSFile = null;
    private String stylesheet = null;
    private String contextRoot = null;
    private UIGenerator generator = null;
    private String stylesheetPath = null;
    private static HashMap context = null;
    public static final String USERAGENT = "chiba.useragent";
    private InteractionHandler handler;
    
    /**
     * Creates a new ServletAdapter object.
     */
    public ServletAdapter() {
        this.context = new HashMap();
    }

    /**
     * creates an instance of ChibaBean, configures it and creates a generator instance
     *
     * @throws XFormsException If an error occurs
     */
    public void init() throws XFormsException {
        // create bean
        this.chibaBean = new ChibaBean();

        // use custom config-file if this param is existent
        if ((this.configPath != null) && !(this.configPath.equals(""))) {
            this.chibaBean.setConfig(this.configPath);
        }
        this.chibaBean.setContext(this.context);

        if (this.formPath != null) {
            File f = new File(locateFile(this.formPath));
            this.formURI = f.toURI();
//            chibaBean.setBaseURI(f.getParentFile().toURI().toString());
        }
        if (this.formURI != null) {
            this.chibaBean.setBaseURI(this.formURI.toString());
            this.chibaBean.setXMLContainer(this.formURI);
        }
        else {
            // todo: base uri
            // this.chibaBean.setBaseURI(...);
            this.chibaBean.setXMLContainer(this.formNode);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(this.toString());
            LOGGER.debug("Formpath: " + formPath);
            LOGGER.debug("CSS-File: " + CSSFile);
            LOGGER.debug("XSLT stylesheet: " + stylesheet);
            LOGGER.debug("action URL: " + actionUrl);
        }

        generator = createUIGenerator();

        this.chibaBean.init();
        this.handler = getNewInteractionHandler();
    }

    /**
     * return a new InteractionHandler.
     *
     * This method returns a new HttpRequestHandler.
     *
     * @return returns a new 
     */
    protected InteractionHandler getNewInteractionHandler() 
	throws XFormsException
    {
	return new HttpRequestHandler(this.chibaBean);
    }

    /**
     * call the InteractionHandler
     *
     * @throws XFormsException
     */
    public void executeHandler() throws XFormsException {
        this.handler.execute();
    }

    /**
     * terminates the XForms processing. right place to do cleanup of resources.
     *
     * @throws org.chiba.xml.xforms.exception.XFormsException
     *
     */
    public void shutdown() throws XFormsException {
        //todo: cleanup and generate response in case something went fatally wrong or session is terminated
    }

    /**
     * Instructs the application environment to forward the given response.
     *
     * @param response a map containing at least a response stream and optional
     *                 header information.
     */
    public void forward(Map response) {
        this.chibaBean.getContext().put(SUBMISSION_RESPONSE, response);
    }

    /**
     * returns a Map object containing a forward uri. this is used by the 'load' action
     *
     * @return a Map object containing a forward uri
     */
    public Map getForwardMap() {
        return (Map) chibaBean.getContext().get(SUBMISSION_RESPONSE);
    }

    /**
     * generates the user interface.
     *
     * This method generates the user interface.
     *
     * @throws XFormsException
     */
    public final void buildUI() throws XFormsException {
        String dataPrefix = Config.getInstance().getProperty("chiba.web.dataPrefix");
        String triggerPrefix = Config.getInstance().getProperty("chiba.web.triggerPrefix");
        String userAgent = (String) getContextProperty(ServletAdapter.USERAGENT);

        generator.setParameter("data-prefix", dataPrefix);
        generator.setParameter("trigger-prefix", triggerPrefix);
        generator.setParameter("user-agent", userAgent);
        if (CSSFile != null) {
            generator.setParameter("css-file", CSSFile);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(">>> setting UI generator params...");
            LOGGER.debug("data-prefix=" + dataPrefix);
            LOGGER.debug("trigger-prefix=" + triggerPrefix);
            LOGGER.debug("user-agent=" + userAgent);
            if (CSSFile != null) {
                LOGGER.debug("css-file=" + CSSFile);
            }
            LOGGER.debug(">>> setting UI generator params...end");
        }

        generator.setInputNode(this.chibaBean.getXMLContainer());
        generator.generate();
    }

    /**
     * generates the user interface.
     * 
     * This conveniance method generates the user interface 
     * using a java.io.Writer.
     *
     * @param responseWriter the Writer to use for the result stream
     * @throws XFormsException
     */
    public void buildUI(Writer responseWriter) throws XFormsException {
        generator.setOutput(responseWriter);
        this.buildUI();
    }


    /**
     * factory method for creating UIGenerator instances.
     *
     * @return the created UIGenerator instance
     * @throws XFormsException
     */
    public UIGenerator createUIGenerator() throws XFormsException {
        //create and configure StylesheetLoader
        StylesheetLoader stylesLoader = new StylesheetLoader(stylesheetPath);

        //if there's a stylesheet specified in the request
        if (stylesheet != null) {
            stylesLoader.setStylesheetFile(stylesheet);
        }

        if (generator == null) {
            generator = getNewUIGenerator(stylesLoader);
        }
        //todo: move these params to buildUI too
        generator.setParameter("action-url", actionUrl);
        generator.setParameter("debug-enabled", String.valueOf(LOGGER.isDebugEnabled()));
        String selectorPrefix = Config.getInstance().getProperty(HttpRequestHandler.SELECTOR_PREFIX_PROPERTY,
                                                                 HttpRequestHandler.SELECTOR_PREFIX_DEFAULT);
        generator.setParameter("selector-prefix", selectorPrefix);
        String removeUploadPrefix = Config.getInstance().getProperty(HttpRequestHandler.REMOVE_UPLOAD_PREFIX_PROPERTY,
                                                                     HttpRequestHandler.REMOVE_UPLOAD_PREFIX_DEFAULT);
        generator.setParameter("remove-upload-prefix", removeUploadPrefix);
        if (CSSFile != null) {
            generator.setParameter("css-file", CSSFile);
        }
        return generator;
    }

    /**
     * return a new UIGenerator.
     *
     * This method returns a new XSLTGenerator.
     *
     * @param stylesLoader
     * @return returns a new UIGenerator object
     */
    protected UIGenerator getNewUIGenerator(StylesheetLoader stylesLoader) 
        throws XFormsException
    {
        return new XSLTGenerator(stylesLoader);
    }

    /**
     * Instructs the application environment to setRedirect to the given URI.
     *
     * @param uri an absolute URI.
     */
    public void setRedirect(String uri) {
        chibaBean.getContext().put(LOAD_URI, uri);
    }

    /**
     * returns the redirect Uri
     *
     * @return the redirect Uri
     */
    public String getRedirectUri() {
        return (String) chibaBean.getContext().get(LOAD_URI);
    }

    // ************************* ACCESSORS ********************************************

    /**
     * returns the ChibaBean instance used with this servletAdapter
     *
     * @return the ChibaBean instance used with this servletAdapter
     */
    public ChibaBean getChibaBean() {
        return chibaBean;
    }

    /**
     * sets the Url for the action target
     *
     * @param actionUrl the Url for the action target
     */
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    /**
     * sets the context root for the webapp. This is used to build the correct pathes of relative path-statements
     *
     * @param contextRoot the root of the webapp
     */
    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    /**
     * set the path to the config file
     *
     * @param configPath the path to the config file
     */
    public void setConfigPath(String configPath) {
        this.configPath = configPath;
    }

    /**
     * sets the path where to find XForms documents.
     *
     * @param formPath the path where to find XForms documents
     */
    public void setFormPath(String formPath) {
        this.formPath = formPath;
        this.formURI = null;
        this.formNode = null;
    }

    public void setFormURI(URI formURI) {
        this.formURI = formURI;
        this.formPath = null;
        this.formNode = null;
    }

    public void setFormDocument(Node formNode) {
        this.formNode = formNode;
        this.formURI = null;
        this.formPath = null;
    }

    /**
     * gets a context property from Chiba's context hashmap.
     *
     * @param key
     * @return a context property from Chiba's context hashmap.
     */
    public static Object getContextProperty(String key) {
        Object returnKey = context.get(key);
        context.remove(key);
    	return returnKey;
    }

    /**
     * stores a context property into Chiba's context hashmap.
     *
     * @param key the key to associate with val
     * @param val the value object to store
     */
    public void setContextProperty(String key, Object val) {
        context.put(key, val);
    }

    public String getUploadDir() {
        return (String) getContextProperty(HTTP_UPLOAD_DIR);
    }

    /**
     * sets the directory where uploaded files are stored.
     *
     * @param uploadDir the directory where uploaded files are stored
     */
    public void setUploadDir(String uploadDir) {
        setContextProperty(HTTP_UPLOAD_DIR, uploadDir);
    }

    /**
     * sets the path where to find the xslt stylesheets
     *
     * @param stylesPath the path where to find the xslt stylesheets
     */
    public void setStylesheetPath(String stylesPath) {
        this.stylesheetPath = stylesPath;
    }

    /**
     * set the CSS file to use for styling the user interface
     *
     * @param css the CSS file to use for styling the user interface
     */
    public void setCSS(String css) {
        this.CSSFile = css;
    }

    /**
     * sets the name of the xslt stylesheet to use for building the UI
     *
     * @param stylesheetFile the name of the xslt stylesheet to use for building the UI
     */
    public void setStylesheet(String stylesheetFile) {
        this.stylesheet = stylesheetFile;
    }

    /**
     * build the absolute path to the requested file and test its
     * existence. <br><br>
     *
     * @param uri - the relative uri of the file
     * @return returns the absolute path to the file
     */
    private String locateFile(String uri) throws XFormsException {
        if (uri == null) {
            throw new XFormsException("No form file specified");
        }

        //construct absolute path to file and check existence
        String filePath = contextRoot + uri;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("requested file: " + filePath);
        }

        if (!(new File(filePath).exists())) {
            throw new XFormsException("File does not exist: " + filePath);
        }

        return filePath;
    }

}

//end of class

