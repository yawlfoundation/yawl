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
import org.w3c.dom.*;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Preprocesses the input XForms document to customize some of the XML representations e.g. adjust attributes to always
 * use a prefix.
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: PreProcessor.java,v 1.21 2004/08/15 14:14:18 joernt Exp $
 */
public class PreProcessor implements XFormsConstants {
    private static Category LOGGER = Category.getInstance(PreProcessor.class);

    private static final String ALERT_PROPERTY = "chiba.ui.defaultAlertText";
    private static final String ALERT_DEFAULT = "The specified value is invalid";
    private static List ALERTABLE = Arrays.asList(new String[]{
        INPUT,
        SECRET,
        TEXTAREA,
        UPLOAD,
        RANGE,
        TRIGGER,
        SUBMIT,
        SELECT,
        SELECT1,
        GROUP});

    private Container container;
    private Document document;

    /**
     * Creates a new PreProcessor object.
     */
    public PreProcessor() {
    }

    /**
     * Returns the container.
     *
     * @return the container.
     */
    public Container getContainer() {
        return this.container;
    }

    /**
     * Sets the container.
     *
     * @param container the container.
     */
    public void setContainer(Container container) {
        this.container = container;
    }

    /**
     * Returns the document.
     *
     * @return the document.
     */
    public Document getDocument() {
        return this.document;
    }

    /**
     * Sets the document.
     *
     * @param document the document.
     */
    public void setDocument(Document document) {
        this.document = document;
    }

    // preprocessor interface

    /**
     * Triggers preprocessing.
     */
    public void process() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("process: start");
        }

        importChibaNamespace();
        processXformsElements();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("process: finish");
        }
    }

    // processing methods

    /**
     * Imports the chiba namespace.
     */
    private void importChibaNamespace() {
        this.document.getDocumentElement().setAttributeNS(NamespaceCtx.XMLNS_NS,
                "xmlns:" + NamespaceCtx.CHIBA_PREFIX,
                NamespaceCtx.CHIBA_NS);
    }

    /*
     * Processes all elements in the xforms namespace.
     */
    private void processXformsElements() {
        DocumentTraversal parentDoc = (DocumentTraversal) this.document;
        Node root = this.document.getDocumentElement();

        NodeIterator mainWalker = parentDoc.createNodeIterator(root, NodeFilter.SHOW_ELEMENT,
                new NodeFilter() {
                    public short acceptNode(Node n) {
                        if ((n.getNodeType() == Node.ELEMENT_NODE) &&
                                NamespaceCtx.XFORMS_NS.equals(n.getNamespaceURI())) {
                            return FILTER_ACCEPT;
                        } else {
                            return FILTER_SKIP;
                        }
                    }
                }, false);

        while (true) {
            Element element = (Element) mainWalker.nextNode();

            if (element == null) {
                break;
            }

            String xformsPrefix = NamespaceCtx.getPrefix(element, NamespaceCtx.XFORMS_NS);
            if (xformsPrefix == null || xformsPrefix.length() == 0) {
                // declare xforms namespace locally
                xformsPrefix = NamespaceCtx.XFORMS_PREFIX;
                element.setAttributeNS(NamespaceCtx.XMLNS_NS,
                        "xmlns:" + xformsPrefix,
                        NamespaceCtx.XFORMS_NS);
            }

            ensureIdAttribute(element);
            ensureAttributeNamespaces(element, xformsPrefix);
            ensureDefaultAlert(element, xformsPrefix);
            patchInstanceExpressions(element);
        }
    }

    private void ensureIdAttribute(Element element) {
        // check for id
        if (element.hasAttributeNS(null, "id")) {
            return;
        }

        // for backwards compatibility
        if (element.hasAttributeNS(NamespaceCtx.XFORMS_NS, "id")) {
            element.setAttributeNS(null, "id", element.getAttributeNS(NamespaceCtx.XFORMS_NS, "id"));
            element.removeAttributeNS(NamespaceCtx.XFORMS_NS, "id");
            return;
        }

        String id = this.container.generateId();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("ensuring id: id='" + id + "'");
        }
        element.setAttributeNS(null, "id", id);
    }

    private void ensureAttributeNamespaces(Element element, String xformsPrefix) {
        NamedNodeMap attrs = element.getAttributes();
        ArrayList list = new ArrayList();

        // get list of all attributes that need to be changed
        for (int c = 0; c < attrs.getLength(); c++) {
            Node attr = attrs.item(c);
            String name = attr.getNodeName();

            if (attr.getNamespaceURI() == null
                    && !name.equals("id")
                    && !name.equals("class")) {
                list.add(name);
            }
        }

        // change them
        for (int c = 0; c < list.size(); c++) {
            String name = (String) list.get(c);
            String value = element.getAttribute(name);
            element.removeAttribute(name);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("ensuring namespace: " + xformsPrefix + ":" + name + "='" + value + "'");
            }
            element.setAttributeNS(NamespaceCtx.XFORMS_NS, xformsPrefix + ":" + name, value);
        }
    }

    private void ensureDefaultAlert(Element element, String xformsPrefix) {
        if (BindingResolver.hasModelBinding(element) || BindingResolver.hasUIBinding(element)) {
            String localName = element.getLocalName();
            if (ALERTABLE.contains(localName) && DOMUtil.findFirstChildNS(element, NamespaceCtx.XFORMS_NS, ALERT) == null) {
                String id = this.container.generateId();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("ensuring alert: id='" + id + "'");
                }
                Document document = element.getOwnerDocument();
                Element alertElement = document.createElementNS(NamespaceCtx.XFORMS_NS, xformsPrefix + ":" + ALERT);
                alertElement.setAttributeNS(null, "id", id);
                alertElement.appendChild(document.createTextNode(PreProcessor.defaultAlertText()));
                element.appendChild(alertElement);
            }
        }
    }

    private void patchInstanceExpressions(Element element) {
        NamedNodeMap attributes = element.getAttributes();

        for (int index = 0; index < attributes.getLength(); index++) {
            Attr attr = (Attr) attributes.item(index);

            if (NamespaceCtx.XFORMS_NS.equals(attr.getNamespaceURI())) {
                attr.setValue(PreProcessor.patchInstanceExpression(attr.getValue()));
            }
        }
    }

    // context free static methods (public for testing)

    /**
     * Replaces any occurrence of <code>instance('&lt;instance-id&gt;')</code> with
     * <code>instance('&lt;instance-id&gt;')/.</code>. This is needed for correct JXPath operation.
     *
     * @param value the value of an attribute to be fixed.
     * @return the fixed value.
     */
    public static String patchInstanceExpression(final String value) {
        int start = 0;
        int end;
        int index = value.indexOf("instance('");
        StringBuffer buffer = new StringBuffer();

        while (index > -1) {
            end = value.indexOf("')", index) + 2;
            buffer.append(value.substring(start, end)).append("/.");
            start = end;
            index = value.indexOf("instance('", start);
        }

        if (start < value.length()) {
            buffer.append(value.substring(start));
        }

        return buffer.toString();
    }

    public static String defaultAlertText() {
        try {
            return Config.getInstance().getProperty(ALERT_PROPERTY, ALERT_DEFAULT);
        } catch (Exception e) {
            return ALERT_DEFAULT;
        }
    }

}

//end of class
