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

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * This class resolves the namespace information used by a given host document.
 * It contains utility methods for resolving both namespace uris and prefixes
 * as well as for resolving all namespaces in scope.
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public class NamespaceCtx {

    // namespace constants

    /**
     * The Chiba namespace (will be declared automatically).
     */
    public static final String CHIBA_NS = "http://chiba.sourceforge.net/2003/08/xforms";

    /**
     * The Chiba namespace prefix.
     */
    public static final String CHIBA_PREFIX = "chiba";

    /**
     * The XForms namespace.
     */
    public static final String XFORMS_NS = "http://www.w3.org/2002/xforms";

    /**
     * The XForms namespace prefix.
     */
    public static final String XFORMS_PREFIX = "xf";

    /**
     * The XHTML namespace (default namespace for generated and transformed documents).
     */
    public static final String XHTML_NS = "http://www.w3.org/2002/06/xhtml2";

    /**
     * The XHTML namespace prefix.
     */
    public static final String XHTML_PREFIX = "xhtml";

    /**
     * The XML namespace.
     */
    public static final String XML_NS = "http://www.w3.org/XML/1998/namespace";

    /**
     * The XML namespace prefix.
     */
    public static final String XML_PREFIX = "xml";

    /**
     * The XMLNS namespace.
     */
    public static final String XMLNS_NS = "http://www.w3.org/2000/xmlns/";

    /**
     * The XMLNS namespace prefix.
     */
    public static final String XMLNS_PREFIX = "xmlns";

    /**
     * The XML Events namespace.
     */
    public static final String XMLEVENTS_NS = "http://www.w3.org/2001/xml-events";

    /**
     * The XML Events namespace prefix.
     */
    public static final String XMLEVENTS_PREFIX = "ev";

    /**
     * The XML Schema namespace.
     */
    public static final String XMLSCHEMA_NS = "http://www.w3.org/2001/XMLSchema";

    /**
     * The XML Schema namespace prefix.
     */
    public static final String XMLSCHEMA_PREFIX = "xs";

    /**
     * The XML Schema Instance namespace.
     */
    public static final String XMLSCHEMA_INSTANCE_NS = "http://www.w3.org/2001/XMLSchema-instance";

    /**
     * The XML Schema Instance namespace prefix.
     */
    public static final String XMLSCHEMA_INSTANCE_PREFIX = "xsi";


    /**
     * Returns a map of all namespace declarations in scope for the specified context
     * element.
     * <p/>
     * Note that if multiple default namespace declarations are found on the way up
     * to the root of the document, only the first one found is used.
     *
     * @param context the context element.
     * @return a map of all namespace declarations in scope for the specified context
     *         element.
     */
    public static Map getAllNamespaces(Element context) {
        Map map = new HashMap();
        NamespaceCtx.getAllNamespaces(context, map);
        return map;
    }

    /**
     * Returns the namespace uri for the specified namespace prefix.
     * <p/>
     * When the specified prefix is <code>null</code> or empty, this method
     * returns the default namespace uri, if any. If the prefix is not bound
     * to a namespace uri, <code>null</code> is returned.
     *
     * @param context the context element.
     * @param prefix  the namespace prefix.
     * @return the namespace uri for the specified namespace prefix.
     */
    public static String getNamespaceURI(Element context, String prefix) {
        NamedNodeMap attrs = context.getAttributes();
        for (int c = 0; c < attrs.getLength(); c++) {
            Node attr = attrs.item(c);

            if (NamespaceCtx.XMLNS_NS.equals(attr.getNamespaceURI())) {
                // check for default namespace declaration
                if (attr.getPrefix() == null && (prefix == null || prefix.length() == 0)) {
                    return attr.getNodeValue();
                }
                // check for prefixed namespace declaration
                if (NamespaceCtx.XMLNS_PREFIX.equals(attr.getPrefix()) && attr.getLocalName().equals(prefix)) {
                    return attr.getNodeValue();
                }
            }
        }

        Node n = context.getParentNode();
        if (n != null && n.getNodeType() == Node.ELEMENT_NODE) {
            return NamespaceCtx.getNamespaceURI((Element) n, prefix);
        }

        return null;
    }

    /**
     * Returns the namespace prefix for the specified namespace uri.
     * <p/>
     * When the specified namespace uri matches the default namespace uri,
     * an empty prefix is returned. If no prefix is not bound to the specified
     * namespace uri, <code>null</code> is returned.
     *
     * @param context the context element.
     * @param uri     the namespace uri.
     * @return the namespace prefix for the specified namespace uri.
     */
    public static String getPrefix(Element context, String uri) {
        NamedNodeMap attrs = context.getAttributes();
        for (int c = 0; c < attrs.getLength(); c++) {
            Node attr = attrs.item(c);

            if (NamespaceCtx.XMLNS_NS.equals(attr.getNamespaceURI())) {
                // check for default namespace declaration
                if (attr.getPrefix() == null && attr.getNodeValue().equals(uri)) {
                    return "";
                }
                // check for prefixed namespace declaration
                if (NamespaceCtx.XMLNS_PREFIX.equals(attr.getPrefix()) && attr.getNodeValue().equals(uri)) {
                    return attr.getLocalName();
                }
            }
        }

        Node n = context.getParentNode();
        if (n != null && n.getNodeType() == Node.ELEMENT_NODE) {
            return NamespaceCtx.getPrefix((Element) n, uri);
        }

        return null;
    }

    /**
     * Applies all namespace declarations in scope for the specified context
     * element to the specified candidate element.
     * <p/>
     * Any namespace declarations occurring on the candidate element are preserved.
     *
     * @param context   the context element.
     * @param candidate the candidate element.
     */
    public static void applyNamespaces(Element context, Element candidate) {
        NamedNodeMap attrs = context.getAttributes();
        for (int c = 0; c < attrs.getLength(); c++) {
            Node attr = attrs.item(c);

            // check for namespace declarations unknown by candidate
            if (NamespaceCtx.XMLNS_NS.equals(attr.getNamespaceURI()) &&
                    !candidate.hasAttributeNS(NamespaceCtx.XMLNS_NS, attr.getLocalName())) {
                // copy namespace declaration
                candidate.setAttributeNS(NamespaceCtx.XMLNS_NS, attr.getNodeName(), attr.getNodeValue());
            }
        }

        Node n = context.getParentNode();
        if (n != null && n.getNodeType() == Node.ELEMENT_NODE) {
            NamespaceCtx.applyNamespaces((Element) n, candidate);
        }
    }

    private static void getAllNamespaces(Element context, Map namespaces) {
        String prefix;
        Node attr;
        NamedNodeMap attrs = context.getAttributes();
        for (int c = 0; c < attrs.getLength(); c++) {
            attr = attrs.item(c);

            if (NamespaceCtx.XMLNS_NS.equals(attr.getNamespaceURI())) {
                prefix = attr.getPrefix() == null ? "" : attr.getLocalName();

                // check for overridden namespace declaration
                if (namespaces.get(prefix) == null) {
                    namespaces.put(prefix, attr.getNodeValue());
                }
            }
        }

        Node n = context.getParentNode();
        if (n != null && n.getNodeType() == Node.ELEMENT_NODE) {
            NamespaceCtx.getAllNamespaces((Element) n, namespaces);
        }
    }

}

//end of class
