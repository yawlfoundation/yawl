/*
 *
 *    Artistic License
 *
 *    Preamble
 *
 *    The intent of this document is to state the conditions under which a
 *    Package may be copied, such that the Copyright Holder maintains some
 *    semblance of artistic control over the development of the package,
 *    while giving the users of the package the right to use and distribute
 *    the Package in a more-or-less customary fashion, plus the right to make
 *    reasonable modifications.
 *
 *    Definitions:
 *
 *    "Package" refers to the collection of files distributed by the
 *    Copyright Holder, and derivatives of that collection of files created
 *    through textual modification.
 *
 *    "Standard Version" refers to such a Package if it has not been
 *    modified, or has been modified in accordance with the wishes of the
 *    Copyright Holder.
 *
 *    "Copyright Holder" is whoever is named in the copyright or copyrights
 *    for the package.
 *
 *    "You" is you, if you're thinking about copying or distributing this Package.
 *
 *    "Reasonable copying fee" is whatever you can justify on the basis of
 *    media cost, duplication charges, time of people involved, and so
 *    on. (You will not be required to justify it to the Copyright Holder,
 *    but only to the computing community at large as a market that must bear
 *    the fee.)
 *
 *    "Freely Available" means that no fee is charged for the item itself,
 *    though there may be fees involved in handling the item. It also means
 *    that recipients of the item may redistribute it under the same
 *    conditions they received it.
 *
 *    1. You may make and give away verbatim copies of the source form of the
 *    Standard Version of this Package without restriction, provided that you
 *    duplicate all of the original copyright notices and associated
 *    disclaimers.
 *
 *    2. You may apply bug fixes, portability fixes and other modifications
 *    derived from the Public Domain or from the Copyright Holder. A Package
 *    modified in such a way shall still be considered the Standard Version.
 *
 *    3. You may otherwise modify your copy of this Package in any way,
 *    provided that you insert a prominent notice in each changed file
 *    stating how and when you changed that file, and provided that you do at
 *    least ONE of the following:
 *
 *        a) place your modifications in the Public Domain or otherwise make
 *        them Freely Available, such as by posting said modifications to
 *        Usenet or an equivalent medium, or placing the modifications on a
 *        major archive site such as ftp.uu.net, or by allowing the Copyright
 *        Holder to include your modifications in the Standard Version of the
 *        Package.
 *
 *        b) use the modified Package only within your corporation or
 *        organization.
 *
 *        c) rename any non-standard executables so the names do not conflict
 *        with standard executables, which must also be provided, and provide
 *        a separate manual page for each non-standard executable that
 *        clearly documents how it differs from the Standard Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    4. You may distribute the programs of this Package in object code or
 *    executable form, provided that you do at least ONE of the following:
 *
 *        a) distribute a Standard Version of the executables and library
 *        files, together with instructions (in the manual page or
 *        equivalent) on where to get the Standard Version.
 *
 *        b) accompany the distribution with the machine-readable source of
 *        the Package with your modifications.
 *
 *        c) accompany any non-standard executables with their corresponding
 *        Standard Version executables, giving the non-standard executables
 *        non-standard names, and clearly documenting the differences in
 *        manual pages (or equivalent), together with instructions on where
 *        to get the Standard Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    5. You may charge a reasonable copying fee for any distribution of this
 *    Package. You may charge any fee you choose for support of this
 *    Package. You may not charge a fee for this Package itself.  However,
 *    you may distribute this Package in aggregate with other (possibly
 *    commercial) programs as part of a larger (possibly commercial) software
 *    distribution provided that you do not advertise this Package as a
 *    product of your own.
 *
 *    6. The scripts and library files supplied as input to or produced as
 *    output from the programs of this Package do not automatically fall
 *    under the copyright of this Package, but belong to whomever generated
 *    them, and may be sold commercially, and may be aggregated with this
 *    Package.
 *
 *    7. C or perl subroutines supplied by you and linked into this Package
 *    shall not be considered part of this Package.
 *
 *    8. The name of the Copyright Holder may not be used to endorse or
 *    promote products derived from this software without specific prior
 *    written permission.
 *
 *    9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 *    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 *    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package org.chiba.xml.xforms;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.log4j.Category;
import org.apache.xerces.dom.ElementImpl;
import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.config.XFormsConfigException;
import org.chiba.xml.xforms.connector.ConnectorFactory;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.exception.XFormsErrorIndication;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a complete XForms document. It encapsulates the DOM document.
 * <p/>
 * The XForms document may consist of pure XForms markup or may contain mixed markup from other namespaces (e.g. HTML,
 * SVG, WAP).
 * <p/>
 * Responsibilities are: <ul> <li>model creation and initialization</li> <li>event creation, dispatching and error
 * handling</li> <li>element registry</li> </ul>
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @author Eduardo Millan <emillan@users.sourceforge.net>
 * @version $Id$
 */
public class Container {
    private static Category LOGGER = Category.getInstance(Container.class);
    private BindingResolver bindingResolver = null;
    private ChibaBean processor = null;
    private ConnectorFactory connectorFactory = null;
    private Document document = null;
    private Element root = null;
    private EventFactory eventFactory = null;
    private JXPathContext rootContext = null;
    private List models = null;
    private Map xFormsElements = null;
    private XFormsElementFactory elementFactory = null;
    private boolean modelConstructDone = false;
    private int idCounter = 0;
    private Exception eventException;

    /**
     * associates DocumentContainer with Processor.
     *
     * @param processor the Processor object
     */
    public Container(ChibaBean processor) {
        this.processor = processor;
    }

    /**
     * returns the processor for this container
     *
     * @return the processor for this container
     */
    public ChibaBean getProcessor() {
        return processor;
    }

    /**
     * Returns the binding resolver.
     *
     * @return the binding resolver.
     */
    public BindingResolver getBindingResolver() {
        if (this.bindingResolver == null) {
            this.bindingResolver = new BindingResolver();
        }

        return this.bindingResolver;
    }

    /**
     * Returns the connector factory.
     *
     * @return the connector factory.
     */
    public ConnectorFactory getConnectorFactory() {
        if (this.connectorFactory == null) {
            try {
                this.connectorFactory = ConnectorFactory.getFactory();
                this.connectorFactory.setContext(this.processor.getContext());
            } catch (XFormsConfigException xce) {
                throw new RuntimeException(xce);
            }
        }

        return this.connectorFactory;
    }

    /**
     * Returns the XForms element factory which is responsible for creating objects for all XForms elements in the input
     * document.
     *
     * @return the XForms element factory
     * @see XFormsElementFactory
     */
    public XFormsElementFactory getElementFactory() {
        if (this.elementFactory == null) {
            this.elementFactory = new XFormsElementFactory();
        }

        return this.elementFactory;
    }

    /**
     * passes the XML container document which contains XForms markup. This method must be called before init() and
     * already builds up the NamespaceCtx and RootContext objects.
     *
     * @param document a DOM Document
     */
    public void setDocument(Document document) {
        this.document = document;
        this.root = this.document.getDocumentElement();
        
        // preprocess document to adjust ids, namespaces, ...
        PreProcessor preProcessor = new PreProcessor();
        preProcessor.setContainer(this);
        preProcessor.setDocument(this.document);
        preProcessor.process();

        this.rootContext = JXPathContext.newContext(root);
    }

    /**
     * Returns the container as a dom tree representing an (external) XML representation of the xforms container.  The
     * return value is live, that means changes to the return tree affects the internal container representation.
     *
     * @return the container as a document.
     */
    public Document getDocument() {
        return this.document;
    }

    /**
     * returns the root JXPathContext for the whole Document
     *
     * @return the root JXPathContext for the whole Document
     */
    public JXPathContext getRootContext() {
        return this.rootContext;
    }

    /**
     * imports the passed input Element into the Containers' DOM Document by appending them as a child of the specified
     * instance Element.
     *
     * @param id the instance id where to place the new instancedata
     * @param e  the input DOM Element to be imported
     * @throws XFormsException
     */
    public void importInstance(String id, Element e) throws XFormsException {
        Element instanceElement = findElement(XFormsConstants.INSTANCE, id);

        if (instanceElement == null) {
            throw new XFormsException("instance element '" + id + "' not found");
        }

        Element firstChild = DOMUtil.getFirstChildElement(instanceElement);
        if (firstChild != null) {
            instanceElement.removeChild(firstChild);
        }

        Element newElement = (Element) getDocument().importNode(e, true);
        instanceElement.appendChild(newElement);
    }

    /**
     * Allows to set or overwrite a instance's src URI.
     * <p/>
     * This method can be used to provide a parametrized URI to the URI resolver which handles the instance's src URI.
     *
     * @param id     the id of the instance.
     * @param srcURI the source URI.
     * @throws XFormsException if no document is present or the specified instance does not exist.
     * @see org.chiba.xml.xforms.connector.URIResolver
     */
    public void setInstanceURI(String id, String srcURI) throws XFormsException {
        Element instanceElement = findElement(XFormsConstants.INSTANCE, id);

        if (instanceElement == null) {
            throw new XFormsException("instance element '" + id + "' not found");
        }

        String xformsPrefix = NamespaceCtx.getPrefix(instanceElement, NamespaceCtx.XFORMS_NS);
        instanceElement.setAttributeNS(NamespaceCtx.XFORMS_NS, xformsPrefix + ":src", srcURI);
    }

    /**
     * Allows to set or overwrite a submission's action URI.
     * <p/>
     * This method can be used to provide a parametrized URI to the Submission Driver which handles the submission's
     * action URI.
     *
     * @param id        the id of the submission.
     * @param actionURI the action URI.
     * @throws XFormsException if no document is present or the specified submission does not exist.
     * @see org.chiba.xml.xforms.connector.SubmissionHandler
     */
    public void setSubmissionURI(String id, String actionURI) throws XFormsException {
        Element submissionElement = findElement(XFormsConstants.SUBMISSION, id);

        if (submissionElement == null) {
            throw new XFormsException("submission element '" + id + "' not found");
        }

        String xformsPrefix = NamespaceCtx.getPrefix(submissionElement, NamespaceCtx.XFORMS_NS);
        submissionElement.setAttributeNS(NamespaceCtx.XFORMS_NS, xformsPrefix + ":action", actionURI);
    }

    /**
     * stores this container as userobject in document element and triggers model initialization
     *
     * @throws XFormsException
     */
    public void init() throws XFormsException {
        ((ElementImpl) this.root).setUserData(this);
        
        // trigger model initialization
        initModels();
    }

    /**
     * Triggers model destruction.
     *
     * @throws XFormsException
     */
    public void shutdown() throws XFormsException {
        // todo: release resources ...
        if (this.models != null) {
            Model model;
            for (int index = 0; index < this.models.size(); index++) {
                model = (Model) this.models.get(index);
                dispatch(model.getTarget(), EventFactory.MODEL_DESTRUCT, null);
            }
        }
    }

    /**
     * adds a DOM EventListener to a target Node.
     *
     * @param targetId      the target Node for this Event
     * @param eventType     the type of Event
     * @param eventListener the EventListener
     * @param useCapture    true, if capture should be used for this Eventtype
     * @throws XFormsException throws XFormsException if target Node cannot be found
     */
    public void addEventListener(String targetId, String eventType, EventListener eventListener,
                                 boolean useCapture) throws XFormsException {
        if (this.rootContext != null) {
            Pointer pointer = this.rootContext.getPointer("//*[@id='" + targetId + "']");

            if (pointer != null) {
                EventTarget eventTarget = (EventTarget) pointer.getNode();
                eventTarget.addEventListener(eventType, eventListener, useCapture);

                return;
            }
        }

        throw new XFormsException("event target '" + targetId + "' not found");
    }

    /**
     * removes an EventListener
     *
     * @param targetId      the event target
     * @param eventType     the type of Event
     * @param eventListener the listener
     * @param useCapture    true, if capture should be used for this Event
     * @throws XFormsException if eventtarget cannot be found
     */
    public void removeEventListener(String targetId, String eventType, EventListener eventListener,
                                    boolean useCapture)
            throws XFormsException {
        if (this.rootContext != null) {
            Pointer pointer = this.rootContext.getPointer("//*[@id='" + targetId + "']");

            if (pointer != null) {
                EventTarget eventTarget = (EventTarget) pointer.getNode();
                eventTarget.removeEventListener(eventType, eventListener, useCapture);

                return;
            }
        }

        throw new XFormsException("event target '" + targetId + "' not found");
    }

    /**
     * Dispatches an Event of the specified type to the given eventType targetId.
     *
     * @param targetId  the id of the eventType targetId.
     * @param eventType the eventType type.
     */
    public boolean dispatch(String targetId, String eventType) throws XFormsException {
        return dispatch(targetId, eventType, null);
    }

    /**
     * dispatches an DOM Event
     *
     * @param targetId  the target Node for this Event
     * @param eventType the type of Event
     * @param info      an additional info object
     * @return true, if Event was cancelled by an Listener
     * @throws XFormsException if target Node cannot be found
     */
    public boolean dispatch(String targetId, String eventType, Object info) throws XFormsException {
        if (this.rootContext != null) {
            Pointer pointer = this.rootContext.getPointer("//*[@id='" + targetId + "']");

            if (pointer != null) {
                EventTarget eventTarget = (EventTarget) pointer.getNode();

                return dispatch(eventTarget, eventType, info);
            }
        }

        throw new XFormsException("event target '" + targetId + "' not found");
    }

    /**
     * Dispatches an Event of the specified type to the given eventType targetId.
     *
     * @param eventTarget the target Node for this Event
     * @param eventType   the type of Event
     * @param info        an additional info object
     * @return true, if Event was cancelled by an Listener
     */
    public boolean dispatch(EventTarget eventTarget, String eventType, Object info) throws XFormsException {
        if (this.eventFactory == null) {
            this.eventFactory = new EventFactory();
        }

        Event event = EventFactory.createEvent(eventType, info);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("dispatching '" + event.getType() + "' to " + eventTarget);
        }

        boolean result = eventTarget.dispatchEvent(event);

        // exception during event flow ?
        if (this.eventException != null) {
            XFormsException xfe = null;

            if (this.eventException instanceof XFormsErrorIndication) {
                if (((XFormsErrorIndication) this.eventException).isFatal()) {
                    // downcast fatal error for rethrowal
                    xfe = (XFormsException) this.eventException;
                }
            } else {
                // wrap exception for rethrowal
                xfe = new XFormsException(this.eventException);
            }

            // remove exception in either case
            this.eventException = null;

            if (xfe != null) {
                throw xfe;
            }
        }

        return result;
    }

    /**
     * Stores an exception until the currently ongoing event flow has finished.
     *
     * @param eventException an exception occurring during event flow.
     */
    public void setEventException(Exception eventException) {
        this.eventException = eventException;
    }

    /**
     * Returns the specified XForms element.
     *
     * @param id the id of the XForms element.
     * @return the specified XForms element or <code>null</code> if the id is unknown.
     */
    public XFormsElement lookup(String id) {
        if (this.xFormsElements != null) {
            return (XFormsElement) this.xFormsElements.get(id);
        }

        return null;
    }

    /**
     * Generates an unique identifier.
     *
     * @return an unique identifier.
     */
    public String generateId() {
        // todo: build external is service
        String id = "C" + (++this.idCounter);

        while (lookup(id) != null) {
            id = "C" + (++this.idCounter);
        }

        return id;
    }

    /**
     * Registers the specified XForms element with this <code>container</code>.
     * <p/>
     * Attaches Container as listener for XForms exception events.
     *
     * @param element the XForms element to be registered.
     */
    public void register(XFormsElement element) {
        if (this.xFormsElements == null) {
            this.xFormsElements = new HashMap();
        }

        this.xFormsElements.put(element.getId(), element);
    }

    /**
     * Deregisters the specified XForms element with this <code>container</code>.
     *
     * @param element the XForms element to be deregistered.
     */
    public void deregister(XFormsElement element) {
        if (this.xFormsElements != null) {
            this.xFormsElements.remove(element.getId());
        }
    }

    /**
     * convenience method to return default model without knowing its id.
     *
     * @return returns the first model in document order
     */
    public Model getDefaultModel() throws XFormsException {
        return getModel(null);
    }

    /**
     * return a model object by its id. If id is null or an empty string, the default model (first found in document
     * order) is returned.
     */
    public Model getModel(String id) throws XFormsException {
        if ((id == null) || (id.length() == 0)) {
            if (this.models != null && this.models.size() > 0) {
                return (Model) this.models.get(0);
            }

            throw new XFormsException("default model not found");
        }

        if (this.models != null) {
            Model model;
            for (int index = 0; index < this.models.size(); index++) {
                model = (Model) this.models.get(index);

                if (model.getId().equals(id)) {
                    return model;
                }
            }
        }

        throw new XFormsException("model '" + id + "' not found");
    }

    /**
     * Returns all models.
     *
     * @return all models.
     */
    public List getModels() {
        return this.models;
    }

    /**
     * returns true, if the default-processing for xforms-model-construct-done Event has been executed already.
     *
     * @return true, if the default-processing for xforms-model-construct-done Event has been executed already.
     */
    public boolean isModelConstructDone() {
        return this.modelConstructDone;
    }

    /**
     * create Model-objects which simply hold their Model-element node (formerly named XForm-element).
     * <p/>
     * The first Model-element found in the container is the default-model and if it has no model-id it is stored with a
     * key of 'default' in the models hashtable. Otherwise the provided id is used.
     * <p/>
     * The subsequent model-elements are stored with their id as the key. If no id exists an exception is thrown (as
     * defined by Spec).
     */
    private void initModels() throws XFormsException {
        this.models = new ArrayList();
        NodeList nl = root.getElementsByTagNameNS(NamespaceCtx.XFORMS_NS, XFormsConstants.MODEL);
        Model model;
        Element modelElement;
        
        // create all models and dispatch xforms-model-construct to all models
        for (int i = 0; i < nl.getLength(); i++) {
            modelElement = (Element) nl.item(i);

            model = (Model) getElementFactory().createXFormsElement(modelElement, null);
            model.checkExtensionFunctions();
            model.init();

            this.models.add(model);
            dispatch(model.getTarget(), EventFactory.MODEL_CONSTRUCT, null);
        }

        for (int i = 0; i < nl.getLength(); i++) {
            model = (Model) this.models.get(i);
            dispatch(model.getTarget(), EventFactory.MODEL_CONSTRUCT_DONE, null);
            
            // set flag to signal that construction has been performed
            this.modelConstructDone = true;
        }

        for (int i = 0; i < nl.getLength(); i++) {
            model = (Model) this.models.get(i);
            dispatch(model.getTarget(), EventFactory.READY, null);
        }
    }

    private Element findElement(String name, String id) {
        if (this.document == null) {
            return null;
        }

        NodeList list = this.document.getElementsByTagNameNS(NamespaceCtx.XFORMS_NS, name);

        if (id == null || id.length() == 0) {
            if (list.getLength() > 0) {
                return (Element) list.item(0);
            }
        }

        for (int index = 0; index < list.getLength(); index++) {
            Element element = (Element) list.item(index);
            if (element.getAttribute("id").equals(id)) {
                return element;
            }
        }

        return null;
    }

}
