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
 *
 */
package org.chiba.xml.xforms;

import org.apache.commons.jxpath.Pointer;
import org.apache.log4j.Category;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.dom.NodeImpl;
import org.apache.xerces.xs.*;
import org.chiba.xml.xforms.action.OutermostActionHandler;
import org.chiba.xml.xforms.config.Config;
import org.chiba.xml.xforms.config.XFormsConfigException;
import org.chiba.xml.xforms.connector.ConnectorFactory;
import org.chiba.xml.xforms.constraints.MainDependencyGraph;
import org.chiba.xml.xforms.constraints.SubGraph;
import org.chiba.xml.xforms.constraints.Validator;
import org.chiba.xml.xforms.constraints.Vertex;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.events.XFormsEvent;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.exception.XFormsLinkException;
import org.chiba.xml.xforms.exception.XFormsComputeException;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.ls.LSInput;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

/**
 * encapsulates the model-part of a XForm: the schema, instance, submission and Bindings and gives access to the
 * individual parts.
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @author Mark Dimon
 * @author Sophie Ramel
 * @version $Id$
 */
public class Model extends XFormsElement implements XFormsModelElement {
    private static Category LOGGER = Category.getInstance(Model.class);

    private List instances;
    private List modelBindings;
    private MainDependencyGraph mainGraph;
    private Validator validator;
    private Vector changed = new Vector();
    private OutermostActionHandler outermost;
    private List schemas;

    /**
     * Creates a new Model object.
     *
     * @param element the DOM Element representing this Model
     */
    public Model (Element element) {
        super(element);
    }

    /**
     * returns the list of changed Vertices
     *
     * @return the list of changed Vertices
     */
    public Vector getChanged () {
        return this.changed;
    }

    // children of this model can get acess to the container
    public Container getContainer () {
        return this.container;
    }

    /**
     * returns the default instance of this model. this is always the first in document order regardless of its
     * id-attribute.
     *
     * @return the default instance of this model
     */
    public Instance getDefaultInstance () {
        if (this.instances.size() > 0) {
            return (Instance) this.instances.get(0);
        }

        return null;
    }

    /**
     * returns the instance-object for given id.
     *
     * @param id the identifier for instance
     * @return the instance-object for given id.
     */
    public Instance getInstance (String id) {
        if ((id == null) || "".equals(id)) {
            return getDefaultInstance();
        }

        for (int index = 0; index < this.instances.size(); index++) {
            Instance instance = (Instance) this.instances.get(index);

            if (id.equals(instance.getId())) {
                return instance;
            }
        }

        return null;
    }

    /**
     * Return the w3c Document that allows external manipulation of the instance DOM
     */
    public Document getInstanceDocument (String instanceId) throws DOMException {
        return getInstance(instanceId).getInstanceDocument();
    }

    /**
     * returns the Main-Calculation-Graph
     *
     * @return the Main-Calculation-Graph
     */
    public MainDependencyGraph getMainGraph () {
        return this.mainGraph;
    }

    /**
     * returns this Model object
     *
     * @return this Model object
     */
    public Model getModel () {
        return this;
    }

    /**
     * Returns the validator.
     *
     * @return the validator.
     */
    public Validator getValidator () {
        if (this.validator == null) {
            this.validator = new Validator();
            this.validator.setModel(this);
        }

        return this.validator;
    }

    /**
     * Returns a list of Schemas associated with this Model. <P> The list is loaded at xforms-model-construct time and
     * is ordered as follows: <ol> <li>The XForms Datatypes Schema is always on top of the list.</li> <li>All linked
     * Schemas of this Model in order of their occurrence in the <tt>xforms:schema</tt> attribute.</li> <li>All inline
     * Schemas of this Model in document order.</li> </ol>
     *
     * @return a list of Schemas associated with this Model.
     */
    public List getSchemas () {
        return this.schemas;
    }

    /**
     * adds a changed Vertex to the changelist. this happens every time a nodes value has changed.
     * <p/>
     * this method has to be called after each change to the instance-data!
     *
     * @param changedNode - the Node whose value has changed
     */
    public void addChanged (NodeImpl changedNode) {
        if (this.mainGraph != null) {
            if (this.changed == null) {
                this.changed = new Vector();
            }

            Vertex vertex = this.mainGraph.getVertex(changedNode, Vertex.CALCULATE_VERTEX);

            if (vertex != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(this + " add changed: adding calculate vertex for " + changedNode);
                }

                this.changed.add(vertex);
            }
        }
    }

    /**
     * adds a new instance to this model.
     *
     * @param id the optional instance id.
     * @return the new instance.
     */
    public Instance addInstance (String id) throws XFormsException {
        // create instance node
        Element instanceNode = this.element.getOwnerDocument()
                .createElementNS(NamespaceCtx.XFORMS_NS, xformsPrefix + ":" + INSTANCE);

        // ensure id
        String realId = id;
        if (realId == null || realId.length() == 0) {
            realId = this.container.generateId();
        }

        instanceNode.setAttributeNS(null, "id", realId);
        instanceNode.setAttributeNS(NamespaceCtx.XMLNS_NS, "xmlns", "");
        this.element.appendChild(instanceNode);

        // create and initialize instance object
        createInstanceObject(instanceNode);
        return getInstance(id);
    }

    /**
     * adds a Bind object to this Model
     *
     * @param bind the Bind object to add
     */
    public void addModelBinding (Bind bind) {
        if (this.modelBindings == null) {
            this.modelBindings = new ArrayList();
        }

        this.modelBindings.add(bind);
    }

    /**
     * Returns the outermost action handler.
     *
     * @return the outermost action handler.
     */
    public OutermostActionHandler getOutermostActionHandler () {
        if (this.outermost == null) {
            this.outermost = new OutermostActionHandler(this);
        }
        return this.outermost;
    }

    /**
     * this method checks for the existence of all functions listed on model and throws
     * a XFormsComputeException if one is not found (see 7.12 Extensions Functions, XForms 1.0 Rec.)
     * <br/><br/>
     * Note: this method only checks if the passed functions can be found in Chiba configuration file but doesn't
     * try to invoke them. It may still happen that runtime exceptions with these functions occur in case
     * the functions class does contain the function in question or wrong parameter are used.
     *
     * @throws XFormsComputeException in case one of the listed functions cannot be found or loaded
     */
    public void checkExtensionFunctions() throws XFormsComputeException {
        String functions = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS,XFormsConstants.FUNCTIONS);
        if(functions!=null && !functions.equals("")){
            //check for availability of extension functions...
            StringTokenizer tokenizer = new StringTokenizer(functions);
            while (tokenizer.hasMoreTokens()) {
                String qname = tokenizer.nextToken();
                String prefix = qname.substring(0,qname.indexOf(":"));
                String localName = qname.substring(qname.indexOf(":") +1);
                String[] functionInfo={""};
                if (functionInfo != null) {
                    try {
                        String uri = NamespaceCtx.getNamespaceURI(this.element, prefix);
                        functionInfo= Config.getInstance().getExtensionFunction(uri, localName);
                        if(functionInfo != null){
                            Class.forName(functionInfo[0]);
                        }else{
                            throw new XFormsComputeException("Function '" + localName + "' cannot be found in Namespace '" + uri + "'",(EventTarget) this.target,null);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new XFormsComputeException("Class containing Function cannot be found ",(EventTarget) this.target,null);
                    } catch (XFormsConfigException e) {
                        throw new XFormsComputeException("Configuration Problem - check default.xml ", this.target,null);
                    }
                }
            }

        }
    }
    /**
     * Performs element init.
     *
     * @throws XFormsException if any error occurred during init.
     */
    public void init () throws XFormsException {
        // todo: initialize model actions *before* any model event is fired ?
        // note that the following does not work for bound actions,
        // since they cannot be initialized without an initialized model
//        Initializer.initializeActionElements(this, this.element);
    }

    // implementation of XFormsModelElement

    /**
     * 7.2.2 The rebuild() Method.<br> This method signals the XForms processor to rebuild any internal data structures
     * used to track computational dependencies within this XForms Model. This method takes no parameters and raises no
     * exceptions.
     * <p/>
     * this method rebuilds the MainDependencyGraph, re-inits Bind elements (which themselves build the Vertices) and
     * triggers a recalculate.
     */
    public void rebuild () {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " rebuild");
        }

        if (this.modelBindings != null && this.modelBindings.size() > 0) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " rebuild: creating main dependency graph for " +
                        this.modelBindings.size() + " bind(s)");
            }

            this.mainGraph = new MainDependencyGraph();

            for (int index = 0; index < this.modelBindings.size(); index++) {
                Bind bind = (Bind) this.modelBindings.get(index);
                this.mainGraph.buildBindGraph(bind, this);
            }

            this.changed = (Vector) this.mainGraph.getVertices().clone();
        }
    }

    /**
     * does a full Model recalculation. all dynamic properties will be evaluated.
     */
    public void recalculate () {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " recalculate");
        }

        if (this.changed != null && this.changed.size() > 0) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " recalculate: creating sub dependency graph for " +
                        this.changed.size() + " node(s)");
            }

            SubGraph sGraph = new SubGraph();
            sGraph.constructSubDependencyGraph(null, changed, mainGraph);
            sGraph.recalculate();
            this.changed.clear();
        }
    }

    /**
     * 7.2.5 The refresh() Method.<br> This method signals the XForms processor to perform a full refresh of form
     * controls bound to instance nodes within this XForms Model. This method takes no parameters and raises no
     * exceptions.
     */
    public void refresh () throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " refresh");
        }

        Initializer.updateUIElements(this.container.getDocument().getDocumentElement());
    }

    /**
     * run the different validations which may be attached to this model. There may be Schema and XForms constraints to
     * be validated.
     */
    public void revalidate () {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " revalidate");
        }

        revalidateInstances();
    }

    /**
     * Revalidates all instances of this model.
     *
     * @return <code>true</code> if all instances of this model are valid, otherwise <code>false</code>.
     */
    public boolean revalidateInstances () {
        boolean result = true;

        if (this.instances != null && this.instances.size() > 0) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " revalidate: revalidating " + this.instances.size() +
                        " instance(s)");
            }

            for (int index = 0; index < this.instances.size(); index++) {
                result &= getValidator().validate((Instance) this.instances.get(index));
            }
        }

        return result;
    }

    /**
     * Returns the logger object.
     *
     * @return the logger object.
     */
    protected Category getLogger () {
        return LOGGER;
    }

    /**
     * Performs the default action for the given event.
     *
     * @param event the event for which default action is requested.
     */
    protected void performDefault (Event event) {
        try {
            if (event.getType().equals(EventFactory.MODEL_CONSTRUCT)) {
                modelConstruct();
                return;
            }
            if (event.getType().equals(EventFactory.MODEL_CONSTRUCT_DONE)) {
                modelConstructDone();
                return;
            }
            if (event.getType().equals(EventFactory.READY)) {
                ready();
                return;
            }
            if (event.getType().equals(EventFactory.REFRESH)) {
                if (isCancelled(event)) {
                    getLogger().debug(this + event.getType() + " cancelled");
                    return;
                }

                refresh();
                return;
            }
            if (event.getType().equals(EventFactory.REVALIDATE)) {
                if (isCancelled(event)) {
                    getLogger().debug(this + event.getType() + " cancelled");
                    return;
                }
                revalidate();
                return;
            }
            if (event.getType().equals(EventFactory.RECALCULATE)) {
                if (isCancelled(event)) {
                    getLogger().debug(this + event.getType() + " cancelled");
                    return;
                }
                recalculate();
                return;
            }
            if (event.getType().equals(EventFactory.REBUILD)) {
                if (isCancelled(event)) {
                    getLogger().debug(this + event.getType() + " cancelled");
                    return;
                }
                rebuild();
                return;
            }
            if (event.getType().equals(EventFactory.RESET)) {
                if (isCancelled(event)) {
                    getLogger().debug(this + event.getType() + " cancelled");
                    return;
                }
                reset();
                return;
            }
            if (event.getType().equals(EventFactory.SUBMIT_ERROR)) {
                getLogger().warn(this + " submit error: " + ((XFormsEvent) event).getContextInfo());
                return;
            }
            if (event.getType().equals(EventFactory.BINDING_EXCEPTION)) {
                getLogger().error(this + " binding exception: " + ((XFormsEvent) event).getContextInfo());
                return;
            }
            if (event.getType().equals(EventFactory.LINK_EXCEPTION)) {
                getLogger().error(this + " link exception: " + ((XFormsEvent) event).getContextInfo());
                return;
            }
            if (event.getType().equals(EventFactory.LINK_ERROR)) {
                getLogger().warn(this + " link error: " + ((XFormsEvent) event).getContextInfo());
                return;
            }
            if (event.getType().equals(EventFactory.COMPUTE_EXCEPTION)) {
                getLogger().error(this + " compute exception: " + ((XFormsEvent) event).getContextInfo());
                return;
            }
        }
        catch (Exception e) {
            // handle exception and stop event propagation
            handleException(e);
            event.stopPropagation();
        }
    }

    /**
     * Implements <code>xforms-model-construct</code> default action.
     */
    private void modelConstruct () throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " model construct");
        }

        // load schemas
        this.schemas = new ArrayList();
        loadDefaultSchema(this.schemas);
        loadLinkedSchemas(this.schemas);
        loadInlineSchemas(this.schemas);

        // set datatypes for validation
        getValidator().setDatatypes(getNamedDatatypes(this.schemas));

        // build instances
        this.instances = new ArrayList();

        // todo: move to static method in initializer
        NodeList nl = getElement().getElementsByTagNameNS(NamespaceCtx.XFORMS_NS, INSTANCE);
        int count = nl.getLength();

        for (int index = 0; index < count; index++) {
            Element xformsInstance = (Element) nl.item(index);
            createInstanceObject(xformsInstance);
        }

        // todo: initialize p3p ?
        // initialize binds and submissions (actions should be initialized already)
        Initializer.initializeBindElements(this, this.element);
        Initializer.initializeSubmissionElements(this, this.element);
        Initializer.initializeActionElements(this, this.element);

        this.container.dispatch(this.target, EventFactory.REBUILD, null);
        this.container.dispatch(this.target, EventFactory.RECALCULATE, null);
        this.container.dispatch(this.target, EventFactory.REVALIDATE, null);
    }

    private void createInstanceObject (Element xformsInstance) throws XFormsException {
        Instance instance = (Instance) this.container.getElementFactory().createXFormsElement(xformsInstance, this);
        instance.init();
        this.instances.add(instance);
    }


    /**
     * Implements <code>xforms-model-construct-done</code> default action.
     */
    private void modelConstructDone () throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " model construct done");
        }

        if (getContainer().isModelConstructDone()) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " model construct done: already performed");
            }

            // process only once for all models
        }
        else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " model construct done: starting ui initialization");
            }

            // initialize ui elements
            Initializer.initializeUIElements(this.container.getDocument().getDocumentElement());
        }
    }

    /**
     * Implements <code>xforms-ready</code> default action.
     */
    private void ready () {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " ready");
        }

        if (this.instances != null) {
            for (int index = 0; index < this.instances.size(); index++) {
                ((Instance) this.instances.get(index)).storeInitialInstance();
            }
        }
    }

    /**
     * Implements <code>xforms-reset</code> default action.
     */
    private void reset () throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " reset");
        }

        if (this.instances != null && this.instances.size() > 0) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " reset: resetting " + this.instances.size() + " instance(s)");
            }

            for (int index = 0; index < this.instances.size(); index++) {
                Instance instance = (Instance) this.instances.get(index);
                instance.reset();
            }
        }

        // dispatch xforms-rebuild, xforms-recalculate, xforms-revalidate
        // and xforms-refresh to model
        this.container.dispatch(this.target, EventFactory.REBUILD, null);
        this.container.dispatch(this.target, EventFactory.RECALCULATE, null);
        this.container.dispatch(this.target, EventFactory.REVALIDATE, null);
        this.container.dispatch(this.target, EventFactory.REFRESH, null);
    }

    private void loadDefaultSchema (List list) throws XFormsException {
        try {
            // todo: still a hack
            InputStream stream = Config.class.getResourceAsStream("XFormsDatatypes.xsd");
            XSModel schema = loadSchema(stream);

            if (schema == null) {
                throw new NullPointerException("resource not found");
            }
            list.add(schema);
        }
        catch (Exception e) {
            throw new XFormsLinkException("could not load default schema", this.target, null);
        }
    }

    private void loadLinkedSchemas (List list) throws XFormsException {
        String schemaURI = null;
        try {
            String schemaLocations = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, "schema");
            StringTokenizer tokenizer = new StringTokenizer(schemaLocations, " ");
            XSModel schema = null;

            while (tokenizer.hasMoreTokens()) {
                schemaURI = tokenizer.nextToken();

                if (schemaURI.startsWith("#")) {
                    // lookup fragment
                    String id = schemaURI.substring(1);
                    Pointer pointer = this.container.getRootContext().getPointer("//*[@id='" + id + "']");
                    Element element = (Element) pointer.getNode();
                    schema = loadSchema(element);
                }
                else {
                    // resolve URI
                    schema = loadSchema(schemaURI);
                }

                if (schema == null) {
                    throw new NullPointerException("resource not found");
                }
                list.add(schema);
            }
        }
        catch (Exception e) {
            throw new XFormsLinkException("could not load linked schema", this.target, schemaURI);
        }
    }

    private void loadInlineSchemas (List list) throws XFormsException {
        String schemaId = null;
        try {
            NodeList children = this.element.getChildNodes();

            for (int index = 0; index < children.getLength(); index++) {
                Node child = children.item(index);

                if (Node.ELEMENT_NODE == child.getNodeType() &&
                        NamespaceCtx.XMLSCHEMA_NS.equals(child.getNamespaceURI())) {
                    Element element = (Element) child;
                    schemaId = element.hasAttributeNS(null, "id")
                            ? element.getAttributeNS(null, "id")
                            : element.getNodeName();

                    XSModel schema = loadSchema(element);

                    if (schema == null) {
                        throw new NullPointerException("resource not found");
                    }
                    list.add(schema);
                }
            }
        }
        catch (Exception e) {
            throw new XFormsLinkException("could not load inline schema", this.target, schemaId);
        }
    }

    // todo: move to schema helper component
    public Map getNamedDatatypes (List schemas) {
        Map datatypes = new HashMap();

        // iterate schemas
        Iterator schemaIterator = schemas.iterator();
        while (schemaIterator.hasNext()) {
            XSModel schema = (XSModel) schemaIterator.next();
            XSNamedMap definitions = schema.getComponents(XSConstants.TYPE_DEFINITION);

            for (int index = 0; index < definitions.getLength(); index++) {
                XSTypeDefinition type = (XSTypeDefinition) definitions.item(index);

                // process named simple types being supported by XForms
                if (type.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE &&
                        !type.getAnonymous() &&
                        getValidator().isSupported(type.getName())) {
                    String name = type.getName();

                    // extract local name
                    int separator = name.indexOf(':');
                    String localName = separator > -1 ? name.substring(separator + 1) : name;

                    // build expanded name
                    String namespaceURI = type.getNamespace();
                    String expandedName = namespaceURI != null ? "{" + namespaceURI + "}" + localName : localName;

                    if (NamespaceCtx.XFORMS_NS.equals(namespaceURI) || NamespaceCtx.XMLSCHEMA_NS.equals(namespaceURI)) {
                        // register default xforms and schema datatypes without namespace for convenience
                        datatypes.put(localName, type);
                    }

                    // register uniquely named type
                    datatypes.put(expandedName, type);
                }
            }
        }

        return datatypes;
    }

    public String getTargetNamespace (XSModel xsModel) {
        String namespace = xsModel.getComponents(XSConstants.TYPE_DEFINITION).item(0).getNamespace();
        return namespace;
    }

    private XSModel loadSchema (String uri) throws XFormsException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        ConnectorFactory connectorFactory = this.container.getConnectorFactory();
        URI absoluteURI = connectorFactory.getAbsoluteURI(uri, this.element);

        return getSchemaLoader().loadURI(absoluteURI.toString());
    }

    private XSModel loadSchema (InputStream stream) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        LSInput input = new DOMInputImpl();
        input.setByteStream(stream);

        return getSchemaLoader().load(input);
    }

    private XSModel loadSchema (Element element) throws TransformerException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        Element copy = (Element) element.cloneNode(true);
        NamespaceCtx.applyNamespaces(element, copy);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.transform(new DOMSource(copy), new StreamResult(stream));
        byte[] array = stream.toByteArray();

        return loadSchema(new ByteArrayInputStream(array));
    }

    private XSLoader getSchemaLoader () throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        System.setProperty(DOMImplementationRegistry.PROPERTY, "org.apache.xerces.dom.DOMXSImplementationSourceImpl");
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        XSImplementation implementation = (XSImplementation) registry.getDOMImplementation("XS-Loader");
        XSLoader loader = implementation.createXSLoader(null);

        return loader;
    }

}

// end of class
