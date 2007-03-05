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
package org.chiba.xml.xforms.xpath;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.log4j.Category;
import org.chiba.xml.xforms.NamespaceCtx;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: InstanceFactory.java,v 1.2 2004/08/15 14:14:16 joernt Exp $
 */
public class InstanceFactory extends AbstractFactory {

    private static final Category LOGGER = Category.getInstance(InstanceFactory.class);

    private Element namespaceContext = null;

    /**
     * Creates a new instance factory.
     */
    public InstanceFactory() {
    }

    /**
     * Sets the namespace context element.
     * <p/>
     * All namespace declarations in scope for that element are copied
     * to any root element created by this factory.
     *
     * @param namespaceContext the namespace context element.
     */
    public void setNamespaceContext(Element namespaceContext) {
        this.namespaceContext = namespaceContext;
    }

    /**
     * Creates a new DOM element node.
     * <p/>
     * <em>Collection index handling is not supported by now !</em>
     *
     * @param context the current JXPath context.
     * @param pointer the pointer describing the node to be created.
     * @param parent  the parent object of the node to be created.
     * @param name    the name of the node to be created.
     * @param index   the collection index of the node to be created.
     * @return <code>true</code> if the node was successfully created,
     *         otherwise <code>false</code>.
     */
    public boolean createObject(JXPathContext context, Pointer pointer, Object parent, String name, int index) {
        if (parent instanceof Element) {
            return createChildElement((Element) parent, name, index);
        }

        if (parent instanceof Document) {
            return createRootElement((Document) parent, name, index);
        }

        LOGGER.error("wrong object model: " + parent.getClass().getName());
        return false;
    }

    /**
     * Declares a new variable.
     * <p/>
     * <em>Variable declaration is not supported by now !</em>
     *
     * @param context the current JXPath context.
     * @param name    the name of the variable.
     * @return <code>false<code>.
     */
    public boolean declareVariable(JXPathContext context, String name) {
        // todo ?
        return false;
    }

    /**
     * Creates the specified root element and attaches all namespace declarations.
     * <p/>
     * The root element will not be created if one of the following holds:
     * <ol>
     * <li>the index is not 0</li>
     * <li>the instance element is null</li>
     * <li>the namespace prefix is unknown</li>
     * </ol>
     *
     * @param parent the parent document.
     * @param name   the name of the root element.
     * @param index  the index of the root element.
     * @return <code>true</code> if the root element has been created successfully,
     *         otherwise <code>false</code>.
     * @see #setNamespaceContext(org.w3c.dom.Element)
     */
    private boolean createRootElement(Document parent, String name, int index) {
        if (index != 0) {
            LOGGER.error("exactly one root element allowed");
            return false;
        }

        if (this.namespaceContext == null) {
            LOGGER.error("no instance element specified");
            return false;
        }

        // check for root element namespace
        int nsSeparator = name.indexOf(':');
        String nsUri = null;

        if (nsSeparator > -1) {
            // resolve namespace uri
            String nsPrefix = name.substring(0, nsSeparator);
            nsUri = NamespaceCtx.getNamespaceURI(this.namespaceContext, nsPrefix);

            if (nsUri == null) {
                LOGGER.error("namespace prefix unknown: " + nsPrefix);
                return false;
            }
        }

        // create root element and apply namespace declarations
        Element root = parent.createElementNS(nsUri, name);
        NamespaceCtx.applyNamespaces(this.namespaceContext, root);
        parent.appendChild(root);

        return true;
    }

    /**
     * Creates the specified child element.
     * <p/>
     * The child element will not be created if one of the following holds:
     * <ol>
     * <li>the index is greater than the parent's children elements number</li>
     * <li>the namespace prefix is unknown</li>
     * </ol>
     *
     * @param parent the parent element.
     * @param name   the name of the child element.
     * @param index  the index of the child element.
     * @return <code>true</code> if the child element has been created successfully,
     *         otherwise <code>false</code>.
     */
    private boolean createChildElement(Element parent, String name, int index) {
        if (index > parent.getChildNodes().getLength()) {
            LOGGER.error("child position too big: " + (index + 1));
            return false;
        }

        // check for child element namespace
        int nsSeparator = name.indexOf(':');
        String nsUri = null;

        if (nsSeparator > -1) {
            // resolve namespace uri
            String nsPrefix = name.substring(0, nsSeparator);
            nsUri = NamespaceCtx.getNamespaceURI(parent, nsPrefix);

            if (nsUri == null) {
                LOGGER.error("namespace prefix unknown: " + nsPrefix);
                return false;
            }
        }

        // create child element
        Element child = parent.getOwnerDocument().createElementNS(nsUri, name);
        parent.appendChild(child);

        return true;
    }

}

// end of class
