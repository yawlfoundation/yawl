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

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.log4j.Category;
import org.apache.xerces.dom.NodeImpl;
import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.exception.XFormsLinkException;
import org.chiba.xml.xforms.xpath.InstanceFactory;
import org.chiba.xml.xforms.xpath.PathUtil;
import org.chiba.xml.xforms.xpath.XPathExtensionFunctions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Abstraction of XForms instance Element.
 *
 * @version $Id$
 */
public class Instance extends XFormsElement implements org.apache.commons.jxpath.Container, InstanceData {
    private static final Category LOGGER = Category.getInstance(Instance.class);
    private Document instanceDocument = null;
    private Element initialInstance = null;
    private JXPathContext instanceContext = null;
    private JXPathContext originalContext = null;
    private Map finalMembers = null;

//    private int nodeCounter = 0;
//    private String prefix = "I-";
//    private Map instanceDataItems = null;

    /**
     * Creates a new Instance object.
     *
     * @param element the DOM Element of this instance
     * @param model   the owning Model of this instance
     *                <p/>
     *                todo: check: param model might be unnecessary todo: check: param element might be unnecessary
     */
    public Instance(Element element, Model model) {
        super(element, model);
    }

    /**
     * Performs element init.
     *
     * @throws XFormsException if any error occurred during init.
     */
    public void init() throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " init");
        }

        // create instance context
        InstanceFactory factory = new InstanceFactory();
        factory.setNamespaceContext(this.element);

        XPathExtensionFunctions functions = new XPathExtensionFunctions();
        functions.setNamespaceContext(this.element);

        // load initial instance
        this.initialInstance = getInitiallInstance();

        // create instance document
        this.instanceDocument = createInstanceDocument();

        this.instanceContext = JXPathContext.newContext(this);
        this.instanceContext.setFactory(factory);
        this.instanceContext.setFunctions(functions);

        Map namespaces = NamespaceCtx.getAllNamespaces(this.element);
        Iterator iterator = namespaces.keySet().iterator();
        while (iterator.hasNext()) {
            String prefix = (String) iterator.next();
            String uri = (String) namespaces.get(prefix);

            this.instanceContext.registerNamespace(prefix, uri);
        }

        //id handling
//        this.instanceDataItems = new HashMap();
//        createIds();
    }

/*
    private void createIds() {
        //note that this is a bit redundant to Container generateId. seems like we need some
        //id manager.
        DocumentTraversal parentDoc = (DocumentTraversal)getInstanceDocument();
        Node root = getInstanceDocument().getDocumentElement();

        NodeIterator mainWalker = parentDoc.createNodeIterator(root, NodeFilter.SHOW_ELEMENT,
                                                               new NodeFilter() {
                public short acceptNode(Node n) {
                    if ((n.getNodeType() == Node.ELEMENT_NODE) ) {
                        return FILTER_ACCEPT;
                    } else{
                        return FILTER_SKIP;
                    }
                }
            }, false);

        while (true) {
            Element node = (Element)mainWalker.nextNode();
            if (node == null) {
                break;
            }
            String id= ((Element) node).getAttributeNS("","id");
            if (id == null) {
                node.setAttributeNS("","id","prefix" + (createId()));
            }
        }

    }
*/

//    private int createId(){
//        return ++nodeCounter;
//    }

    // instance specific methods

    /**
     * this method lets you access the state of individual instance data nodes. each node has an associated ModelItem
     * object which holds the current status of readonly, required, validity, relevant. it also annotates the DOM node
     * with type information.
     * <p/>
     * When an ModelItem does not exist already it will be created and attached as useroject to its corresponding node
     * in the instancedata.
     *
     * @param locationPath - an absolute location path pointing to some node in this instance
     * @return the ModelItemProperties for the specified node
     */
    public ModelItem getModelItem(String locationPath) {
        // ensure that node exists
        if (!existsNode(locationPath)) {
            return null;
        }

        // canonicalize path
        String canonicalPath = this.instanceContext.getPointer(locationPath).asPath();

        // lookup node
        NodeImpl node = (NodeImpl) this.instanceContext.getPointer(canonicalPath).getNode();

        // lookup item
        ModelItem item = (ModelItem) node.getUserData();

        if (item == null) {
            // create item
            item = new ModelItem(node);
            node.setUserData(item);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(this + " get model item: created item for path '" + canonicalPath + "'");
            }
        }

        return item;
    }

    /**
     * Returns the instance context. This may also be used for non-default instances, cause it knows how to switch the
     * context correctly with help of xforms:instance() function.
     *
     * @return the instance context.
     */
    public JXPathContext getInstanceContext() {
        return this.instanceContext;
    }

    /**
     * Sets the instance document.
     *
     * @param document the instance document.
     */
    void setInstanceDocument(Document document) {
        this.instanceDocument = document;
    }

    /**
     * Returns the instance document.
     *
     * @return the instance document.
     */
    public Document getInstanceDocument() {
        return this.instanceDocument;
    }

    /**
     * Sets the value of the specified node.
     *
     * @param path  the path pointing to a node.
     * @param value the value to be set.
     */
    public void setNodeValue(String path, String value) throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " set node value: " + path + "=" + value);
        }

        ModelItemProperties item = getModelItem(path);
        if (item == null) {
            throw new XFormsException("model item for path '" + path + "' does not exist");
        }

        if (!item.isReadonly()) {
            item.setValue(value);
            this.model.addChanged((NodeImpl) item.getNode());
        } else {
            getLogger().warn(this + " set node value: attempt to set readonly value");
        }
    }

    /**
     * Returns the value of the specified node.
     *
     * @param path the path pointing to a node.
     * @return the value of the specified node.
     */
    public String getNodeValue(String path) throws XFormsException {
        ModelItemProperties item = getModelItem(path);
        if (item == null) {
            throw new XFormsException("model item for path '" + path + "' does not exist");
        }

        return item.getValue();
    }

    /**
     * Returns a pointer for the specified node.
     *
     * @param path the path pointing to a node.
     * @return a pointer for the specified node.
     */
    public Pointer getPointer(String path) {
        return this.instanceContext.getPointer(path);
    }

    /**
     * Returns a pointer iterator for the specified nodeset.
     *
     * @param path the path pointing to a nodeset.
     * @return a pointer for the specified nodeset.
     */
    public Iterator getPointerIterator(String path) {
        return this.instanceContext.iteratePointers(path);
    }

    /**
     * Modifies the value contained by this container.
     *
     * @param value the new value.
     */
    public void setValue(Object value) {
        if (value instanceof Document) {
            setInstanceDocument((Document) value);
        }
    }

    // implementation of 'org.apache.commons.jxpath.Container'

    /**
     * Returns the contained value.
     *
     * @return the contained value.
     */
    public Object getValue() {
        return getInstanceDocument();
    }

    /**
     * Counts the number of nodes in the specified nodeset.
     *
     * @param path the path pointing to a nodeset.
     * @return the number of nodes in the specified nodeset.
     */
    public int countNodeset(String path) {
        //this runs extremely slow - todo: optimize
        return ((Integer) this.instanceContext.getValue("count(" + path + ")", Integer.class)).intValue();
    }

    /**
     * Creates the specified node.
     *
     * @param path the path pointing to a node.
     */
    public void createNode(String path) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " create node: " + path);
        }

        if (this.instanceDocument.getDocumentElement() == null) {
            String qName = PathUtil.localStepName(PathUtil.getFirstStep(path));
            createRootElement(this.instanceDocument, qName);
        }

        this.instanceContext.createPath(path);
    }

    /**
     * Deletes the specified node.
     *
     * @param path     the path pointing to a nodeset.
     * @param position the nodeset position.
     */
    public void deleteNode(String path, int position) throws XFormsException {
        String deletePath = path + "[" + position + "]";
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " delete node: " + deletePath);
            getLogger().debug(this + " delete node: instance data before manipulation" + toString(this.instanceDocument));
        }

        // [1] check and canonicalize delete path
        try {
            deletePath = this.instanceContext.getPointer(deletePath).asPath();
        } catch (JXPathException e) {
            throw new XFormsException("invalid delete path '" + deletePath + "'");
        }

        // [2] remove specified path
        this.instanceContext.removePath(deletePath);

        // [3] dispatch chiba event
        this.container.dispatch(this.target, EventFactory.NODE_DELETED, deletePath);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " delete node: instance data after manipulation" + toString(this.instanceDocument));
        }
    }

    /**
     * Checks if the specified node exists.
     *
     * @param path the path pointing to a node.
     * @return <code>true</code> if the specified node exists, otherwise <code>false</code>.
     */
    public boolean existsNode(String path) {
        return countNodeset(path) > 0;
    }

    /**
     * Checks wether the specified <code>instance</code> element has an initial instance.
     * <p/>
     * The specified <code>instance</code> element is considered to have an initial instance if it has either a
     * <code>src</code> attribute or at least one element child.
     *
     * @return <code>true</code> if the <code>instance</code> element has an initial instance, otherwise
     *         <code>false</code>.
     */
    public boolean hasInitialInstance() {
        return this.initialInstance != null;
    }

    // lifecycle methods

    /**
     * Inserts the specified node.
     *
     * @param path     the path pointing to a nodeset.
     * @param position the nodeset position.
     */
    public void insertNode(String path, int position) throws XFormsException {
        String insertPath = path + "[" + position + "]";
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " insert node: " + insertPath);
            getLogger().debug(this + " insert node: instance data before manipulation" + toString(this.instanceDocument));
        }

        // [1] check insert position
        if (position < 1 || position > countNodeset(path) + 1) {
            throw new XFormsException("invalid insert path '" + insertPath + "'");
        }

        // [2] lookup required parent node
        Node parentNode = getInsertParent(path);

        // [3] lookup optional reference node
        Node refNode = getInsertReference(path, position);

        // [4] lookup required collection member
        Node finalMember = getFinalCollectionMember(path);

        // [5] clone final collection member
        Node newNode = this.instanceDocument.importNode(finalMember, true);

        // [6] insert new node at specified position
        parentNode.insertBefore(newNode, refNode);

        // [7] canonicalize insert path and dispatch chiba event
        insertPath = this.instanceContext.getPointer(insertPath).asPath();
        this.container.dispatch(this.target, EventFactory.NODE_INSERTED, insertPath);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " insert node: instance data after manipulation" + toString(this.instanceDocument));
        }
    }

    /**
     * Performs element reset.
     *
     * @throws XFormsException if any error occurred during reset.
     */
    public void reset() throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " reset");
        }

        // recreate instance document
        this.instanceDocument = createInstanceDocument();
    }

    /**
     * Stores the current instance data as initial instance if no original instance exists.
     * <p/>
     * This is needed for resetting an instance to its initial state when no initial instance exists.
     */
    public void storeInitialInstance() {
        if (this.initialInstance == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " store initial instance");
            }

            this.initialInstance = (Element) this.instanceDocument.getDocumentElement().cloneNode(true);
        }
    }

    // ********************************* private parts *************************

    private Node getInsertParent(String path) {
        // build parent path and lookup node
        String parentPath = path.substring(0, path.lastIndexOf('/'));
        Node parentNode = (Node) this.instanceContext.getPointer(parentPath).getNode();

        return parentNode;
    }

    private Node getInsertReference(String path, int position) {
        // build reference path and check node
        String refPath = path + "[" + position + "]";
        if (existsNode(refPath)) {
            // reference node found
            return (Node) getPointer(refPath).getNode();
        }

        // build parent path
        String parentPath = path.substring(0, path.lastIndexOf('/'));
        String currentPath;

        // lookup siblings in the _original_ instance
        Node finalMember = getFinalCollectionMember(path);
        Node finalSibling = DOMUtil.getNextSiblingElement(finalMember);

        while (finalSibling != null) {
            // build reference path and check node
            currentPath = parentPath + "/" + finalSibling.getNodeName();
            if (existsNode(currentPath)) {
                // reference node found
                return (Node) getPointer(currentPath).getNode();
            }

            finalSibling = DOMUtil.getNextSiblingElement(finalSibling);
        }

        return null;
    }

    private Node getFinalCollectionMember(String path) {
        // instantiate cache lazily
        if (this.finalMembers == null) {
            this.finalMembers = new HashMap();
        }

        // lookup final collection member
        Node finalMember = (Node) this.finalMembers.get(path);
        if (finalMember != null) {
            // cache hit
            return finalMember;
        }

        // search final collection member
        Pointer finalPointer = getLastPointer(getOriginalContext(), path);

        if (finalPointer == null) {
            String genericPath = PathUtil.removePredicates(path);
            finalPointer = getLastPointer(getOriginalContext(), genericPath);

            // todo: if finalPointer == null throw exception
        }

        // obtain and cache final collection member
        finalMember = (Node) finalPointer.getNode();
        this.finalMembers.put(path, finalMember);

        return finalMember;
    }

    private Pointer getLastPointer(JXPathContext jxPathContext, String xpath) {
        // this is ugly and expensive, but needed as long as
        // jxpath has problems with last()
        Pointer finalPointer = null;
        Iterator members = jxPathContext.iteratePointers(xpath);
        while (members.hasNext()) {
            finalPointer = (Pointer) members.next();
        }

        return finalPointer;
    }

    private JXPathContext getOriginalContext() {
        if (this.originalContext == null) {
            try {
                this.originalContext = JXPathContext.newContext(createInstanceDocument());
            } catch (XFormsException e) {
                // todo: error handling
                e.printStackTrace();
            }
        }

        return this.originalContext;
    }

    /**
     * Returns the logger object.
     *
     * @return the logger object.
     */
    protected Category getLogger() {
        return LOGGER;
    }

    /**
     * Creates the root element of the instance data.
     *
     * @param qname the qualified name of the root element.
     */
    private void createRootElement(Document document, String qname) {
        int separator = qname.indexOf(':');
        Element root;

        if (separator > -1) {
            String prefix = qname.substring(0, separator);
            String uri = NamespaceCtx.getNamespaceURI(this.element, prefix);
            root = document.createElementNS(uri, qname);
        } else {
            root = document.createElement(qname);
        }

        NamespaceCtx.applyNamespaces(this.element, root);
        document.appendChild(root);
    }

    // initialization helper methods

    /**
     * Returns the original instance.
     * <p/>
     * If this instance has a <code>src</code> attribute, it will be resolved and the resulting document element is
     * used. Otherwise the first child element of this instance is used.
     *
     * @return the original instance.
     */
    private Element getInitiallInstance() throws XFormsLinkException {
        if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, SRC_ATTRIBUTE)) {
            String src = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, SRC_ATTRIBUTE);

// resolve uri
            Object result;

            try {
                result = this.container.getConnectorFactory().createURIResolver(src, this.element).resolve();
            } catch (Exception e) {
                throw new XFormsLinkException("uri resolution failed", e, this.model.getTarget(), src);
            }

            
            if (result instanceof Document) {
                return ((Document) result).getDocumentElement();
            }

            if (result instanceof Element) {
                return (Element) result;
            }

            throw new XFormsLinkException("object model not supported", this.model.getTarget(), src);
        }

        return DOMUtil.getFirstChildElement(this.element);
    }

    /**
     * Returns a new created instance document.
     * <p/>
     * If this instance has an original instance, it will be imported into this new document. Otherwise the new document
     * is left empty.
     *
     * @return a new created instance document.
     */
    private Document createInstanceDocument() throws XFormsException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            Document document = factory.newDocumentBuilder().newDocument();

            if (this.initialInstance != null) {
                document.appendChild(document.importNode(this.initialInstance.cloneNode(true), true));

                if (!this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, SRC_ATTRIBUTE)) {
                    // apply namespaces
                    NamespaceCtx.applyNamespaces(this.element, document.getDocumentElement());
                }
            }

            return document;
        } catch (Exception e) {
            throw new XFormsException(e);
        }
    }

    private String toString(Node node) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            DOMUtil.prettyPrintDOM(node, stream);
        } catch (TransformerException e) {
            // nop
        }
        return System.getProperty("line.separator") + stream;
    }


}

// end of class
