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
package org.chiba.xml.xforms;

import org.apache.log4j.Category;
import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.config.XFormsConfigException;
import org.chiba.xml.xforms.connector.ConnectorFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.ui.AbstractFormControl;
import org.chiba.xml.xforms.ui.Upload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.HashMap;

/**
 * Chiba Facade Class.
 * <p/>
 * This class provides the interface to process W3C XForms 1.0 conformant documents.
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: ChibaBean.java,v 1.47 2004/12/27 22:42:05 joernt Exp $
 */
public class ChibaBean implements Serializable {
    private static Category LOGGER = Category.getInstance(ChibaBean.class);
    private static String APP_INFO = null;

    /**
     * The document container object model.
     */
    private Container container = null;


    /**
     * The base URI for resolution of relative URIs.
     */
    private String baseURI = null;

    /**
     * The context map which stores application-specific parameters.
     */
    private Map context = null;

    /**
     * signals if shutdown() has been called.
     */
    private boolean shutdown = false;

    /**
     * Creates a new ChibaBean object.
     */
    public ChibaBean() {
        LOGGER.info(getAppInfo());
    }

    /**
     * Returns Chiba version string.
     *
     * @return Chiba version string.
     */
    public static String getAppInfo() {
        synchronized (ChibaBean.class) {
            if (APP_INFO == null) {
                try {
                    BufferedInputStream stream = new BufferedInputStream(ChibaBean.class.getResourceAsStream("version.info"));
                    StringBuffer buffer = new StringBuffer("Chiba/");
                    int c;

                    while ((c = stream.read()) > -1) {
                        if (c != 10 && c != 13) {
                            buffer.append((char) c);
                        }
                    }

                    stream.close();

                    APP_INFO = buffer.toString();
                } catch (IOException e) {
                    APP_INFO = "Chiba";
                }
            }

            return APP_INFO;
        }
    }

    /**
     * Sets the config path.
     * <p/>
     * Checks existence of the config path and creates a config instance.
     *
     * @param path the absolute path to the config file.
     */
    public void setConfig(String path) throws XFormsException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("set config: " + path);
        }

        if ((path == null) || (new File(path).exists() == false)) {
            throw new XFormsConfigException("path not found: " + path);
        }

        Config.getInstance(path);
    }

    /**
     * Sets the base URI.
     * <p/>
     * The base URI is used for resolution of relative URIs occurring in the document, e.g. instance sources or
     * submission actions.
     *
     * @param uri the base URI.
     */
    public void setBaseURI(String uri) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("set base uri: " + uri);
        }

        this.baseURI = uri;
    }

    /**
     * Returns the base URI.
     *
     * @return the base URI.
     * @see #setBaseURI(String)
     */
    public String getBaseURI() {
        return this.baseURI;
    }

    /**
     * Allows to set a context map for storing application-specific parameters.
     *
     * @param context the context map to use.
     */
    public void setContext(Map context) {
        this.context = context;
    }

    /**
     * Returns the context map which stores application-specific parameters.
     *
     * @return the context map which stores application-specific parameters.
     */
    public Map getContext() {
        if(this.context == null ){
            this.context = new HashMap();
        }
        return this.context;
    }

    /**
     * Returns the document container associated with this processor.
     *
     * @return the document container associated with this processor.
     */
    public Container getContainer() throws XFormsException {
        // todo: sessions don't matter here, move this check to adapter
        if (shutdown) {
//            throw new XFormsException("The form session has been terminated.");
            throw new XFormsException(Config.getInstance().getErrorMessage("session-invalid"),
                                      null, "session-invalid");
        }
        return this.container;
    }

    /**
     * Sets the containing document.
     * <p/>
     * A new document container is created.
     *
     * @param file the absolute file name of the containing document.
     * @throws XFormsException if the document container could not be created.
     * @see #setXMLContainer(java.net.URI)
     * @deprecated
     */
    public void setXMLContainer(String file) throws XFormsException {
        ensureContainerNotInitialized();

        Document xFormsDocument;
        try {
            xFormsDocument = getXFormsDocumentBuilder().parse(new File(file));
        } catch (Exception e) {
            throw new XFormsException("could not create document container", e);
        }

        createContainer().setDocument(xFormsDocument);
    }

    /**
     * Sets the containing document.
     * <p/>
     * A new document container is created.
     *
     * @param node
     *            Either the containing document as DOM Document or the 
     *            root of a DOM (sub)tree as DOM Element.
     * @throws XFormsException if the document container could not be created.
     */
    public void setXMLContainer(Node node) throws XFormsException {
        ensureContainerNotInitialized();

        Document xFormsDocument = toXFormsDocument(node);
        createContainer().setDocument(xFormsDocument);
    }

    /**
     * Sets the containing document.
     * <p/>
     * A new document container is created.
     *
     * @param uri the containing document URI.
     * @throws XFormsException if the document container could not be created.
     */
    public void setXMLContainer(URI uri) throws XFormsException {
        ensureContainerNotInitialized();

        // todo: refactor / fix uri resolution in connector factory to work without an init'd processor
        String absoluteURI = resolve(uri);
        ConnectorFactory connectorFactory = ConnectorFactory.getFactory();
        connectorFactory.setContext(getContext());
        Node node = (Node) connectorFactory.createURIResolver(absoluteURI, null).resolve();

        Document xFormsDocument = toXFormsDocument(node);
        createContainer().setDocument(xFormsDocument);
    }

    /**
     * Sets the containing document.
     * <p/>
     * A new document container is created.
     *
     * @param stream the containing document as input stream.
     * @throws XFormsException if the document container could not be created.
     */
    public void setXMLContainer(InputStream stream) throws XFormsException {
        ensureContainerNotInitialized();

        Document xFormsDocument;
        try {
            xFormsDocument = getXFormsDocumentBuilder().parse(stream);
        } catch (Exception e) {
            throw new XFormsException("could not create document container", e);
        }

        createContainer().setDocument(xFormsDocument);
    }

    /**
     * Sets the containing document.
     * <p/>
     * A new document container is created.
     *
     * @param source the containing document as input source.
     * @throws XFormsException if the document container could not be created.
     */
    public void setXMLContainer(InputSource source) throws XFormsException {
        ensureContainerNotInitialized();

        Document xFormsDocument;
        try {

            xFormsDocument = getXFormsDocumentBuilder().parse(source);
        } catch (Exception e) {
            throw new XFormsException("could not create document container", e);
        }

        createContainer().setDocument(xFormsDocument);
    }

    /**
     * Returns the containing document as DOM.
     * <p/>
     * This returns the live DOM processed by Chiba internally. Changes will affect internal state and may cause
     * malfunction. Should we better be more restrictive and return a clone to prevent this ?
     *
     * @return the containing document.
     * @throws XFormsException if no document container is present.
     */
    public Document getXMLContainer() throws XFormsException {
        ensureContainerPresent();

        return this.container.getDocument();
    }

    /**
     * Convenience method to import instance data prior to initializing the processor (calling its init-method). This
     * inlines the passed instance data into the containing document and makes them available for editing. They'll be
     * handled exactly like inline preset data.
     *
     * @param id      the id of the target instance element.
     * @param element the imported element.
     * @throws XFormsException if no document container is present or an error occurred during instance import.
     * @see #setInstanceElement(java.lang.String, org.w3c.dom.Element)
     * @deprecated
     */
    public void importInstance(String id, Element element) throws XFormsException {
        setInstanceElement(id, element);
    }

    /**
     * Allows to set or overwrite instance data.
     * <p/>
     * Convenience method to import instance data prior to initializing the processor. This inlines the passed instance
     * data and makes it available for editing. It'll be handled exactly like inline preset data.
     *
     * @param id      the id of the instance.
     * @param element the instance element.
     * @throws XFormsException if no document container is present or the specified instance does not exist.
     */
    public void setInstanceElement(String id, Element element) throws XFormsException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("set instance element: instance: " + id + ", element: " + element);
        }

        ensureContainerPresent();
        ensureContainerNotInitialized();

        this.container.importInstance(id, element);
    }

    /**
     * Allows to set or overwrite a instance's src URI.
     * <p/>
     * This method can be used to provide a parametrized URI to the URI Resolver which handles the instance's src URI.
     * <p/>
     * If no id or an empty id is specified, the default instance is selected. The default instance is the first
     * instance in document order.
     *
     * @param id     the id of the instance.
     * @param srcURI the source URI.
     * @throws XFormsException if no document container is present or the specified instance does not exist.
     * @see org.chiba.xml.xforms.connector.URIResolver
     */
    public void setInstanceURI(String id, String srcURI) throws XFormsException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("set instance uri: instance: " + id + ", uri: " + srcURI);
        }

        ensureContainerPresent();
        ensureContainerNotInitialized();

        this.container.setInstanceURI(id, srcURI);
    }

    /**
     * Allows to set or overwrite a submission's action URI.
     * <p/>
     * This method can be used to provide a parametrized URI to the Submission Driver which handles the submission's
     * action URI.
     * <p/>
     * If no id or an empty id is specified, the default submission is selected. The default submission is the first
     * submission in document order.
     *
     * @param id        the id of the submission.
     * @param actionURI the action URI.
     * @throws XFormsException if no document container is present or the specified submission does not exist.
     * @see org.chiba.xml.xforms.connector.SubmissionHandler
     */
    public void setSubmissionURI(String id, String actionURI) throws XFormsException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("set submission uri: submission: " + id + ", uri: " + actionURI);
        }

        ensureContainerPresent();
        ensureContainerNotInitialized();

        this.container.setSubmissionURI(id, actionURI);
    }

    /**
     * Bootstraps processor initialization.
     * <p/>
     * Use this method after setXMLContainer() and (optionally) setInstanceData() have been called to actually start the
     * processing.
     *
     * @throws XFormsException if no document container is present or an error occurred during init.
     */
    public void init() throws XFormsException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("init");
        }

        ensureContainerPresent();
        ensureContainerNotInitialized();

        this.container.init();
    }

    /**
     * Dispatches an event of the specified type to the given event targetId.
     *
     * @param targetId the id of the event targetId.
     * @param event  the event type.
     * @throws XFormsException if no document container is present or an error occurred during dispatch.
     */
    public boolean dispatch(String targetId, String event) throws XFormsException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("dispatch: targetId: " + targetId + ", event: " + event);
        }

        ensureContainerPresent();
        ensureContainerInitialized();

        return this.container.dispatch(targetId, event);
    }

    /**
     * This method emulates the setting of a new value through an UI control.
     *
     * The value will only be changed if there was a change to the data. This
     * method mainly exists to allow the separation of the actual UI handling.
     * Applications have to call this method to propagate their UI value changes
     * to the Chiba processor.
     * 
     * <p>
     * This method is a conveniance method and is equivalent to
     * <code>updateControlValue(id, null, newValue, null)</code>.
     * 
     * @param id
     *            the id of the control
     * @param newValue
     *            the new value for the control
     * @throws XFormsException
     */
    public final void updateControlValue(String id, String newValue) throws XFormsException {
        updateControlValue(id, null, newValue, null);
    }

    /**
     * This method emulates the setting of a new value through an UI control.
     * 
     * The value will only be changed if there was a change to the data. This
     * method mainly exists to allow the separation of the actual UI handling.
     * Applications have to call this method to propagate their UI value changes
     * to the Chiba processor.
     * 
     * @param id
     *            the id of the control
     * @param contentType
     *            the content-type for the uploaded resource. Set this value to
     *            null, unless the controll is an upload-controll.
     * @param newValue
     *            the newValue for the control. If the control is an upload
     *            control, this is the name the uploaded data should be saved
     *            under.
     * @param data
     *            the uploaded data as byte array. Set this value to null,
     *            unless the control is an upload control.
     * @throws XFormsException
     */
    public void updateControlValue(String id, String contentType,
                                   String newValue, byte[] data) throws XFormsException {
        ensureContainerPresent();
        ensureContainerInitialized();

        // check old control value
        AbstractFormControl control = (AbstractFormControl) getContainer().lookup(id);
        if (control == null) {
            LOGGER.warn("Control with id '" + id + "' does not exist. Updating cancelled.");
            return;
        }
        if(data != null && !(control instanceof Upload)) {
            throw new XFormsException("Only Update control can be updated with file data");
        }
        if(newValue == null && control instanceof Upload){
            throw new XFormsException("No name specified for Upload data - Param 'newValue' is null");            
        }
        String oldValue = control.getValue();


        if (newValue.equals(oldValue)) {
            // NOP
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("data '" + id + "' has no changes");
            }
        } else {
            // update control value
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("data '" + id + "' changes to '" + newValue + "'");
            }
            if (!(control instanceof Upload)) {
            control.setValue(newValue);
            } else {
                Upload uploadControl = (Upload) control;
                uploadControl.setValue(contentType, newValue, data);
            }
        }

        // quick hack
        setVisited(control);
    }

    private void setVisited(AbstractFormControl control) {
        Element element = control.getElement();
        Element data = DOMUtil.findFirstChildNS(element, NamespaceCtx.CHIBA_NS, "data");
        if (data.getAttributeNS(NamespaceCtx.CHIBA_NS, "enabled").equals("true")) {
            data.setAttributeNS(NamespaceCtx.CHIBA_NS, NamespaceCtx.CHIBA_PREFIX + ":visited", String.valueOf(true));
        }
    }

    /**
     * Tests if an 'upload' control is bound to 'id' and has a datatype of 'xsd:anyUri'.
     * <br/><br/>
     * Upload controls are special and need special support to integrate with the environment. If the upload is
     * bound to a data-item of datatype 'xsd:anyUri' the data will be stored in an external file and the data-item
     * will take an URI pointing to the newly created resource.
     *
     * If the upload is bound to the other allowed types 'hexBinary' and 'base64', the data will be stored as part of
     * the instance data in their respective encoding.
     *
     * This method allows an application to distinguish these cases.
     *
     * todo: review the option to create a more generic method 'getDatatypeForControl(id)'
     *  
     * @param id the id of the control
     * @return true, if the control is an upload control and is bound to a data-item with datatype 'xsd:anyUri'.
     */
    public boolean storesExternalData(String id) throws XFormsException {
        ensureContainerPresent();
        ensureContainerInitialized();

        AbstractFormControl control = (AbstractFormControl) getContainer().lookup(id);
        
        if (!(control instanceof Upload)) {
            // only upload controls have data
            return false;
        }
        return ((Upload) control).hasAnyUriType();
    }

    /**
     * Finishes processor operation.
     *
     * @throws XFormsException if no document container is present or an error occurred during shutdown.
     */
    public void shutdown() throws XFormsException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("shutdown");
        }

        ensureContainerPresent();
        ensureContainerInitialized();

        this.shutdown = true;
        this.container.shutdown();
        this.container = null;
    }

    private Container createContainer() {
        this.container = new Container(this);
        return this.container;
    }

    private Document toXFormsDocument(Node node) {
        if (node instanceof XFormsDocument) {
            return (Document)node;
        }

        Document xFormsDocument = new XFormsDocument();
        if (node instanceof Document) {
            node = ((Document) node).getDocumentElement();
        }
        xFormsDocument.appendChild(xFormsDocument.importNode(node, true));

        return xFormsDocument;
    }

    private DocumentBuilder getXFormsDocumentBuilder() throws XFormsException {
        try {
            // start WLS 8.1
            String oldProperty = System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
            System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
                    "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
            // end WLS 8.1.

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setAttribute("http://apache.org/xml/properties/dom/document-class-name",
                    "org.chiba.xml.xforms.XFormsDocument");
            DocumentBuilder builder = factory.newDocumentBuilder();

            // start WLS 8.1
            if (oldProperty == null || oldProperty.length() == 0) {
                System.getProperties().remove("javax.xml.parsers.DocumentBuilderFactory");
            }
            else {
                System.setProperty("javax.xml.parsers.DocumentBuilderFactory", oldProperty);
            }
            // end WLS 8.1.

            return builder;
        } catch (Exception e) {
            throw new XFormsException(e);
        }
    }

    private void ensureContainerPresent() throws XFormsException {
        if (this.container == null) {
            throw new XFormsException("document container not present");
        }
    }

    private void ensureContainerInitialized() throws XFormsException {
        if (this.container == null || !this.container.isModelConstructDone()) {
            throw new XFormsException("document container not initialized");
        }
    }

    private void ensureContainerNotInitialized() throws XFormsException {
        if (this.container != null && this.container.isModelConstructDone()) {
            throw new XFormsException("document container already initialized");
        }
    }

    // todo: move this code away
    private String resolve(URI relative) throws XFormsException {
        if (relative.isAbsolute() || relative.isOpaque()) {
            return relative.toString();
        }

        if (this.baseURI == null) {
            throw new XFormsException("base uri not present");
        }

        try {
            return new URI(this.baseURI).resolve(relative).toString();
        } catch (URISyntaxException e) {
            throw new XFormsException(e);
        }
    }

}

// end of class
