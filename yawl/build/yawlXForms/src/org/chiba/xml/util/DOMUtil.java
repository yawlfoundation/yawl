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
 * */
package org.chiba.xml.util;

import org.chiba.xml.xforms.xpath.PathUtil;
import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeFilter;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * some DOM utility methods.
 *
 * @author joern turner
 * @author vrg
 * @version $Id: DOMUtil.java,v 1.16 2004/12/20 00:13:23 joernt Exp $
 */
public class DOMUtil {

    /**
     * __UNDOCUMENTED__
     *
     * @param start __UNDOCUMENTED__
     * @param name  __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public static Element getChildElement(Node start, String name) {
        //        NodeList nl=start.getChildNodes();
        NodeList nl = null;

        if (start.getNodeType() == Node.DOCUMENT_NODE) {
            nl = ((Document) start).getDocumentElement().getChildNodes();
        } else {
            nl = start.getChildNodes();
        }

        int len = nl.getLength();
        Node n = null;

        for (int i = 0; i < len; i++) {
            n = nl.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (n.getNodeName().equals(name)) {
                    return (Element) n;
                }
            }
        }

        return null;
    }

    /**
     * returns the xpath from the start-element up to the document-root
     *
     * @return - returns the xpath from-element up to the document-root.
     * The start element is included.
     */

    //    public static String getPath(Node start) {
    //        String path=null;
    //
    //        if(start.getNodeType()==Node.ELEMENT_NODE || start.getNodeType()==Node.ATTRIBUTE_NODE) {
    //
    ////            path=start.getNodeName();
    //
    //            Node n=start;
    //            while(n.getParentNode()!=null) {
    //                path=n.getNodeName() + "/" + path;
    //            }
    //
    //        }
    //        return path;
    //    }

    /**
     * returns a java.util.List of Elements which are children of the start Element.
     */
    public static List getChildElements(Node start) {
        List l = new ArrayList();
        NodeList nl = start.getChildNodes();
        int len = nl.getLength();
        Node n = null;

        for (int i = 0; i < len; i++) {
            n = nl.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                l.add(n);
            }
        }

        return l;
    }

    /**
     * returns an element's position in the given NodeList
     *
     * @param refNode the element to get the index for
     * @param list    the NodeList to search
     * @return the position starting with 1, or -1 if refNode was null
     */
    public static int getCurrentListPosition(Node refNode, NodeList list) {
        if (refNode == null) {
            return -1;
        }

        int counter = 1;

        for (int n = 0; n < list.getLength(); n++, counter++) {
            if (list.item(n) == refNode) {
                return counter;
            }
        }

        return -1;
    }

    /**
     * returns an element's position in the list of its siblings.
     *
     * @param refNode the element to get the index for
     * @return the position starting with 1, or -1 if refNode was null
     */
    public static int getCurrentPosition(Node refNode) {
        if (refNode == null) {
            return -1;
        }

        int counter = 0;
        Node current = refNode;

        while (current != null) {
            if (current.getNodeType() == Node.ELEMENT_NODE) {
                counter++;
            }

            current = current.getPreviousSibling();
        }

        return counter;
    }

    /**
     * equivalent to the XPath expression './/tagName[@attrName='attrValue']'
     */
    public static Element getElementByAttributeValue(Node start, String tagName, String attrName,
                                                     String attrValue) {
        NodeList nl = ((Element) start).getElementsByTagName(tagName);
        int l = nl.getLength();

        if (l == 0) {
            return null;
        }

        Element e = null;
        String compareValue = null;

        for (int i = 0; i < l; i++) {
            e = (Element) nl.item(i);

            if (e.getNodeType() == Node.ELEMENT_NODE) {
                compareValue = e.getAttribute(attrName);

                if (compareValue.equals(attrValue)) {
                    return e;
                }
            }
        }

        return null;
    }

    /**
     * equivalent to the XPath expression './/tnuri:tagName[@anuri:attrName='attrValue']'
     */
    public static Element getElementByAttributeValueNS(Node start, String tnuri, String tagName,
                                                       String anuri, String attrName, String attrValue) {
        NodeList nl = ((Element) start).getElementsByTagNameNS(tnuri, tagName);

        if (nl != null) {
            int l = nl.getLength();

            if (l == 0) {
                return null;
            }

            Element e = null;
            String compareValue = null;

            for (int i = 0; i < l; i++) {
                e = (Element) nl.item(i);

                if (e.getNodeType() == Node.ELEMENT_NODE) {
                    compareValue = e.getAttributeNS(anuri, attrName);

                    if (compareValue.equals(attrValue)) {
                        return e;
                    }
                }
            }
        }

        return null;
    }

    /**
     * returns the first child of the contextnode which has the specified tagname regardless of the depth in the tree.
     *
     * @param contextNode where to start the search
     * @param tag         the name of the wanted child
     * @return the first child found under the contextnode
     */
    public static Node getFirstChildByTagName(Node contextNode, String tag) {
        Node n = null;

        if (contextNode.getNodeType() == Node.DOCUMENT_NODE) {
            n = ((Document) contextNode).getDocumentElement();

            if (!n.getNodeName().equals(tag)) {
                n = null;
            }
        } else {
            NodeList nodes = ((Element) contextNode).getElementsByTagName(tag);

            if (nodes != null) {
                n = nodes.item(0);
            }
        }

        return n;
    }

    /**
     * returns the first child of the contextnode which has the specified tagname and namespace uri regardless of the
     * depth in the tree.
     *
     * @param contextNode where to start the search
     * @param nsuri       the namespace uri
     * @param tag         the local name part of the wanted child
     * @return the first child found under the contextnode
     */
    public static Node getFirstChildByTagNameNS(Node contextNode, String nsuri, String tag) {
        Node n = null;

        if (contextNode.getNodeType() == Node.DOCUMENT_NODE) {
            n = ((Document) contextNode).getDocumentElement();

            if (!(n.getNamespaceURI().equals(nsuri) && n.getNodeName().equals(tag))) {
                n = null;
            }
        } else {
            NodeList nodes = ((Element) contextNode).getElementsByTagNameNS(nsuri, tag);

            if (nodes != null) {
                n = nodes.item(0);
            }
        }

        return n;
    }

    /**
     * gets the first child of a node which is an element. This avoids the whitespace problems when using
     * org.w3c.dom.node.getFirstChild(). Whitespace-nodes may also appear as children, but normally are not what you're
     * looking for.
     */
    public static Element getFirstChildElement(Node start) {
        Node n = null;
        NodeList nl = start.getChildNodes();
        int len = nl.getLength();

        if (len == 0) {
            return null;
        }

        for (int i = 0; i < len; i++) {
            n = nl.item(i);

            if (n.getNodeType() == Node.ELEMENT_NODE) {
                return ((Element) n);
            }
        }

        return null;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param start __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public static Element getLastChildElement(Node start) {
        NodeList children = start.getChildNodes();

        if (children != null) {
            int len = children.getLength();
            Node n = null;

            for (int i = len - 1; i >= 0; i--) {
                n = children.item(i);

                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    return ((Element) n);
                }
            }
        }

        return null;
    }

    /**
     * Returns the next sibling element of the specified node.
     * <p/>
     * If there is no such element, this method returns <code>null</code>.
     *
     * @param node the node to process.
     * @return the next sibling element of the specified node.
     */
    public static Element getNextSiblingElement(Node node) {
        Node sibling = node.getNextSibling();

        if ((sibling == null) || (sibling.getNodeType() == Node.ELEMENT_NODE)) {
            return (Element) sibling;
        }

        return getNextSiblingElement(sibling);
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param nodeToCompare __UNDOCUMENTED__
     * @param nsuri         __UNDOCUMENTED__
     * @param tagName       __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public static boolean isNodeInNS(Node nodeToCompare, String nsuri, String tagName) {
        String ntcnsuri = nodeToCompare.getNamespaceURI();

        if ((ntcnsuri != null) && (ntcnsuri.length() > 0)) {
            return (tagName.equals(nodeToCompare.getLocalName()) && ntcnsuri.equals(nsuri));
        } else {
            return (tagName.equals(nodeToCompare.getNodeName()));
        }
    }

    /**
     * Returns the previous sibling element of the specified node.
     * <p/>
     * If there is no such element, this method returns <code>null</code>.
     *
     * @param node the node to process.
     * @return the previous sibling element of the specified node.
     */
    public static Element getPreviousSiblingElement(Node node) {
        Node sibling = node.getPreviousSibling();

        if ((sibling == null) || (sibling.getNodeType() == Node.ELEMENT_NODE)) {
            return (Element) sibling;
        }

        return getPreviousSiblingElement(sibling);
    }

    /**
     * gets the first child of a node which is a text or cdata node.
     */
    public static Node getTextNode(Node start) {
        Node n = null;

        start.normalize();

        NodeList nl;
        if (start.getNodeType() == Node.DOCUMENT_NODE) {
            nl = ((Document) start).getDocumentElement().getChildNodes();
        } else {
            nl = start.getChildNodes();
        }

        int len = nl.getLength();

        if (len == 0) {
            return null;
        }

        for (int i = 0; i < len; i++) {
            n = nl.item(i);

            if (n.getNodeType() == Node.TEXT_NODE) {
                return n;
            } else if (n.getNodeType() == Node.CDATA_SECTION_NODE) {
                return n;
            }
        }

        return null;
    }

    /**
     * returns the Text-Node child of Node 'start' as String. If no TextNode exists, an empty string is returned.
     */
    public static String getTextNodeAsString(Node start) {
        Node txt = getTextNode(start);

        if (txt != null) {
            return txt.getNodeValue();
        }

        return "";
    }

    /**
     * copies all attributes from one Element to another
     *
     * @param from   - the Element which the source attributes
     * @param to     - the target Element for the Attributes
     * @param filter - a NodeFilter to apply during copy
     */
    public static void copyAttributes(Element from, Element to, NodeFilter filter) {
        if ((from != null) && (to != null)) {
            NamedNodeMap map = from.getAttributes();

            /* if filter is null use our own default filter, which accepts
               everything (this saves us from always check if filter is
               null */
            if (filter == null) {
                filter = new NodeFilter() {
                    public short acceptNode(Node n) {
                        return NodeFilter.FILTER_ACCEPT;
                    }
                };
            }

            if (map != null) {
                int len = map.getLength();

                for (int i = 0; i < len; i++) {
                    Node attr = map.item(i);

                    if (attr.getNodeType() == Node.ATTRIBUTE_NODE) {
                        if (filter.acceptNode(attr) == NodeFilter.FILTER_ACCEPT) {
                            to.setAttributeNS(attr.getNamespaceURI(), attr.getNodeName(), attr.getNodeValue());
                        }
                    }
                }
            }
        }
    }

    // return the count of child elements
    public static int countChildElements(Node node) {
        NodeList nl = node.getChildNodes();
        int count = 0;

        for (int n = 0; n < nl.getLength(); n++) {
            if (nl.item(n).getNodeType() == Node.ELEMENT_NODE) {
                count++;
            }
        }

        return count;
    }

    /**
     * find the first child in a parent for a tagname part (only one the child level)
     *
     * @param parent  the parent to search the child in
     * @param tagName the local name part of the child node
     * @return the found child casted to Element or null if no such child was found.
     */
    public static Element findFirstChild(Node parent, String tagName) {
        if (tagName == null) {
            return null;
        }

        NodeList children = parent.getChildNodes();

        if (children != null) {
            int len = children.getLength();
            Node n = null;

            for (int i = 0; i < len; i++) {
                n = children.item(i);

                //System.out.println("child="+n.getNodeName());
                if ((n.getNodeType() == Node.ELEMENT_NODE) && tagName.equals(n.getNodeName())) {
                    return ((Element) n);
                }
            }
        }

        return null;
    }

    /**
     * find the first child in a parent for a namespace uri and local name part (equals "/tagName[1]" in xpath)
     *
     * @param parent  the parent to search the child in
     * @param nsuri   the namespace uri of the child node
     * @param tagName the local name part of the child node
     * @return the found child casted to Element or null if no such child was found.
     */
    public static Element findFirstChildNS(Node parent, String nsuri, String tagName) {
        if (tagName == null) {
            return null;
        }

        NodeList children = parent.getChildNodes();

        if (children != null) {
            int len = children.getLength();
            Node n = null;

            for (int i = 0; i < len; i++) {
                n = children.item(i);

                if ((n.getNodeType() == Node.ELEMENT_NODE) && isNodeInNS(n, nsuri, tagName)) {
                    return ((Element) n);
                }
            }
        }

        return null;
    }

    /**
     * find the last child in a parent for a tagname part
     *
     * @param parent  the parent to search the child in
     * @param tagName the local name part of the child node
     * @return the found child casted to Element or null if no such child was found.
     */
    public static Element findLastChild(Node parent, String tagName) {
        if (tagName == null) {
            return null;
        }

        NodeList children = parent.getChildNodes();

        if (children != null) {
            int len = children.getLength();
            Node n = null;

            for (int i = len - 1; i >= 0; i--) {
                n = children.item(i);

                if ((n.getNodeType() == Node.ELEMENT_NODE) && tagName.equals(n.getNodeName())) {
                    return ((Element) n);
                }
            }
        }

        return null;
    }

    /**
     * find the last child in a parent for a namespace uri and local name part
     *
     * @param parent  the parent to search the child in
     * @param nsuri   the namespace uri of the child node
     * @param tagName the local name part of the child node
     * @return the found child casted to Element or null if no such child was found.
     */
    public static Element findLastChildNS(Node parent, String nsuri, String tagName) {
        if (tagName == null) {
            return null;
        }

        NodeList children = parent.getChildNodes();

        if (children != null) {
            int len = children.getLength();
            Node n = null;

            for (int i = len - 1; i >= 0; i--) {
                n = children.item(i);

                if ((n.getNodeType() == Node.ELEMENT_NODE) && isNodeInNS(n, nsuri, tagName)) {
                    return ((Element) n);
                }

                /*                    tagName.equals(n.getLocalName())) {
                   if (nsuri == n.getNamespaceURI())
                       return ((Element) n);
                   if (nsuri != null &&
                       nsuri.equals(n.getNamespaceURI()))
                       return ((Element) n);
                   }*/
            }
        }

        return null;
    }

    /**
     * find the nth child in a parent for a namespace uri and local name part (equals "/tagName[idx]" in xpath)
     *
     * @param contextNode the parent to search the child in
     * @param nsuri       the namespace uri of the child node
     * @param tag         the local name part of the child node
     * @param idx         the index to use (starting at one)
     * @return the found child casted to Element or null if no such child was found.
     */
    public static Node findNthChildNS(Node contextNode, String nsuri, String tag, int idx) {
        if (tag == null) {
            return null;
        }

        NodeList children = contextNode.getChildNodes();

        if (children != null) {
            int len = children.getLength();
            Node n = null;

            //            int childcount = 0; // to count the found childs
            //
            //            idx --;                 // since index starts at one
            int childcount = 1;

            for (int i = 0; i < len; i++) {
                n = children.item(i);

                if ((n.getNodeType() == Node.ELEMENT_NODE) && isNodeInNS(n, nsuri, tag)) {
                    if (childcount == idx) {
                        return ((Element) n);
                    } else if (childcount > idx) {
                        return null;
                    }

                    childcount++;
                }
            }
        }

        return null;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param start __UNDOCUMENTED__
     * @param name  __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public static boolean hasChild(Element start, String name) {
        NodeList nl = start.getChildNodes();
        int len = nl.getLength();

        Node n = null;

        for (int i = 0; i < len; i++) {
            n = nl.item(i);

            if (n.getNodeName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * just the same as hasNonWhitespaceChildren, but seen from a different perspective ;)
     *
     * @param element
     * @return true, if any Element nodes are found, otherwise false
     */
    public static boolean hasElementChildren(Element element) {
        return hasNonWhitespaceChildren(element);
    }

    /**
     * check, if the passed element node has non-whitespace children.
     *
     * @return true, if any Element nodes are found, otherwise false
     */
    public static boolean hasNonWhitespaceChildren(Element element) {
        if (element.hasChildNodes()) {
            NodeList children = element.getChildNodes();
            int len = children.getLength();
            Node n = null;

            for (int i = 0; i < len; i++) {
                n = children.item(i);

                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    /**
     * This is a workaround for very strange behaviour of xerces-1.4.2 DOM importNode.
     */
    public static Node importNode(Document document, Node toImport) {
        if (toImport != null) {
            Node root = toImport.cloneNode(false); // no deep cloning!

            root = document.importNode(root, false);

            for (Node n = toImport.getFirstChild(); n != null; n = n.getNextSibling()) {
                root.appendChild(document.importNode(n, true));
            }

            return root;
        }

        return null;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param newChild __UNDOCUMENTED__
     * @param refChild __UNDOCUMENTED__
     * @throws DOMException __UNDOCUMENTED__
     */
    public static void insertAfter(Node newChild, Node refChild)
            throws DOMException {
        if (refChild == null) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, "refChild == null");
        }

        Node nextSibling = refChild.getNextSibling();

        if (nextSibling == null) {
            refChild.getParentNode().appendChild(newChild);
        } else {
            refChild.getParentNode().insertBefore(newChild, nextSibling);
        }
    }

    /**
     * Moves a child the given index in a nodelist for a given number of steps.
     *
     * @param nodelist the nodelist to work on. if the nodelist is empty, nothing is done
     * @param index    index pointing to the child to move.  if the index is not in the list range nothing is done.
     * @param step     the amount of slots to move the child.  if step is negative the child is moved up (towards the list
     *                 start), if it is positive it is moved down (towards the list end). if the step is zero nothing is done.
     */
    public static void moveChild(NodeList nodelist, int index, int step) {
        if ((nodelist == null) || (nodelist.getLength() == 0)) {
            return;
        }

        if ((index >= nodelist.getLength()) || (index < 0)) {
            return;
        }

        if (step == 0) {
            return;
        }

        Node parent = nodelist.item(0).getParentNode();
        Node deletedElt = parent.removeChild(nodelist.item(index));

        if ((index + step) == (nodelist.getLength() - 1)) {
            parent.appendChild(deletedElt);
        } else {
            // SURE? it seems that after a removeChild the indices of the nodes
            // in the nodelist seem not to change.  Checking the DOM spec the
            // nodelist is live, but this seems not to be true for index changes
            // is this a bug, or correct?
            // Due to this behaviour the following seperation betweem step forward
            // and backward is necessary.
            if (step < 0) {
                parent.insertBefore(deletedElt, nodelist.item(index + step));
            } else {
                parent.insertBefore(deletedElt, nodelist.item(index + step + 1));
            }
        }
    }

    /**
     * Removes all children of the specified node.
     *
     * @param node the node.
     */
    public static void removeAllChildren(Node node) {
        Node child;
        while ((child = node.getFirstChild()) != null) {
            node.removeChild(child);
        }
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param isNamespaceAware __UNDOCUMENTED__
     * @param isValidating     __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     */
    public static Document newDocument(boolean isNamespaceAware, boolean isValidating) {
        // !!! workaround to enable Chiba to run within WebLogic Server
        // Force JAXP to use xerces as the default JAXP parser doesn't work with Chiba
        //
        //        String oldFactory = System.getProperty("javax.xml.parsers.DocumentBuilderFactory");
        //        System.setProperty("javax.xml.parsers.DocumentBuilderFactory","org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // restore to original factory
        //
        //        System.setProperty("javax.xml.parsers.DocumentBuilderFactory",oldFactory);
        // !!! end workaround
        factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(isNamespaceAware);
        factory.setValidating(isValidating);

        try {
            // Create builder.
            DocumentBuilder builder = factory.newDocumentBuilder();

            return builder.newDocument();
        } catch (ParserConfigurationException pce) {
            System.err.println(pce.toString());
        }

        return null;
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param in         __UNDOCUMENTED__
     * @param namespaces __UNDOCUMENTED__
     * @param validating __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     * @throws ParserConfigurationException __UNDOCUMENTED__
     * @throws SAXException                 __UNDOCUMENTED__
     * @throws IOException                  __UNDOCUMENTED__
     */
    public static Document parseInputStream(InputStream in, boolean namespaces, boolean validating)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = createDocumentBuilder(namespaces, validating);

        return builder.parse(in);
    }

    /**
     * parses a Xml-File on disk and returns the parsed DOM Document.
     *
     * @param fileName - must be an absolute file-path pointing to the file
     */
    public static Document parseXmlFile(String fileName, boolean namespaces, boolean validating)
            throws ParserConfigurationException, SAXException, IOException {
        return DOMUtil.parseXmlFile(new File(fileName), namespaces, validating);
    }

    /**
     * __UNDOCUMENTED__
     *
     * @param file       __UNDOCUMENTED__
     * @param namespaces __UNDOCUMENTED__
     * @param validating __UNDOCUMENTED__
     * @return __UNDOCUMENTED__
     * @throws ParserConfigurationException __UNDOCUMENTED__
     * @throws SAXException                 __UNDOCUMENTED__
     * @throws IOException                  __UNDOCUMENTED__
     */
    public static Document parseXmlFile(File file, boolean namespaces, boolean validating)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder builder = createDocumentBuilder(namespaces, validating);

        return builder.parse(file);
    }

    /**
     * Serializes the specified node to stdout.
     *
     * @param node the node to serialize
     */
    public static void prettyPrintDOM(Node node) {
        try {
            prettyPrintDOM(node, System.out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Serializes the specified node to the given stream. Serialization is achieved by an identity transform.
     *
     * @param node   the node to serialize
     * @param stream the stream to serialize to.
     * @throws TransformerException if any error ccurred during the identity transform.
     */
    public static void prettyPrintDOM(Node node, OutputStream stream) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.transform(new DOMSource(node), new StreamResult(stream));
    }

    /**
     * Selects a nodelist for a path.  the path is a simple slash seperated path expression pointing to a node.<p>
     * <p/>
     * for a tree:
     * <pre><![CDATA[
     * <a>
     *   <b>
     *     <c>
     *       <d/>
     *       <d/>
     *     </c>
     *     <e/>
     *   </b>
     * </a>
     * ]]></pre>
     * <p/>
     * the path /a/b/c/d returns a list with the two &lt;d&gt; elements. the path /a/b/c returns a list with one path
     * element.<p>
     * <p/>
     * the path is resolved always against a context node, and therefor may or may not begin with a slash.
     */
    public static List selectNodesByPath(Node context, String nsuri, String bindingExpr) {
        Vector steps = PathUtil.getSteps(bindingExpr);

        if (steps.size() == 0) {
            return null;
        }

        Node current = context;
        int sc = 0;
        String step;

        for (sc = 0; sc < (steps.size() - 1); sc++) {
            step = (String) steps.get(sc);

            int pos = step.indexOf("[");

            if (pos == -1) {
                // simple step. search the element from the current node
                // and change current node to it.
                Node n = DOMUtil.findFirstChildNS(current, nsuri, step);
                current = n;
            } else {
                // indexed step.  search the index element from the current
                // node and change current node to it.
                String stepName = step.substring(0, pos);
                String stepIdx = step.substring(pos + 1, step.length() - 1);
                int idx = Integer.parseInt(stepIdx);
                Node n = DOMUtil.findNthChildNS(current, nsuri, stepName, idx);
                current = n;
            }

            if (current == null) {
                // if current becomes null the bindingExpr points to a non
                // exisitng dom branch
                return null;
            }
        }

        /* now get the last element as node list from the current node.  If
           the last node has an index, return only this one node (in a
           list), otherwise return all sibling with that name */
        if (sc < steps.size()) {
            ArrayList list = new ArrayList();

            step = (String) steps.get(sc);

            int pos = step.indexOf('[');

            if (pos != -1) {
                String stepName = step.substring(0, pos);
                String stepIdx = step.substring(pos + 1, step.length() - 1);
                int idx = Integer.parseInt(stepIdx);
                Node nn = DOMUtil.findNthChildNS(current, nsuri, stepName, idx);

                if (nn != null) {
                    list.add(nn);
                }
            } else {
                NodeList nl = current.getChildNodes();

                for (int i = 0; i < nl.getLength(); i++) {
                    Node nn = nl.item(i);

                    if ((nn.getNodeType() == Node.ELEMENT_NODE) && isNodeInNS(nn, nsuri, step)) {
                        list.add(nn);
                    }
                }
            }

            return ((list.size() > 0)
                    ? list
                    : null);
        }

        // otherwise return null = nothing found
        return null;
    }

    private static DocumentBuilder createDocumentBuilder(boolean namespaces, boolean validating)
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(namespaces);
        factory.setValidating(validating);

        //        factory.setAttribute("http://xml.org/sax/features/namespace-prefixes)",new Boolean(true));
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder;
    }

}

// end of class
