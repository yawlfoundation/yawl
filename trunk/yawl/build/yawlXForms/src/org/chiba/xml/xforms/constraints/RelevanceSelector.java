/*
 *
 *	Artistic License
 *
 *	Preamble
 *
 *	The intent of this document is to state the conditions under which a
 *	Package may be copied, such that the Copyright Holder maintains some
 *	semblance of artistic control over the development of the package,
 *	while giving the users of the package the right to use and distribute
 *	the Package in a more-or-less customary fashion, plus the right to make
 *	reasonable modifications.
 *
 *	Definitions:
 *
 *	"Package" refers to the collection of files distributed by the
 *	Copyright Holder, and derivatives of that collection of files created
 *	through textual modification.
 *
 *	"Standard Version" refers to such a Package if it has not been
 *	modified, or has been modified in accordance with the wishes of the
 *	Copyright Holder.
 *
 *	"Copyright Holder" is whoever is named in the copyright or copyrights
 *	for the package.
 *
 *	"You" is you, if you're thinking about copying or distributing this Package.
 *
 *	"Reasonable copying fee" is whatever you can justify on the basis of
 *	media cost, duplication charges, time of people involved, and so
 *	on. (You will not be required to justify it to the Copyright Holder,
 *	but only to the computing community at large as a market that must bear
 *	the fee.)
 *
 *	"Freely Available" means that no fee is charged for the item itself,
 *	though there may be fees involved in handling the item. It also means
 *	that recipients of the item may redistribute it under the same
 *	conditions they received it.
 *
 *	1. You may make and give away verbatim copies of the source form of the
 *	Standard Version of this Package without restriction, provided that you
 *	duplicate all of the original copyright notices and associated
 *	disclaimers.
 *
 *	2. You may apply bug fixes, portability fixes and other modifications
 *	derived from the Public Domain or from the Copyright Holder. A Package
 *	modified in such a way shall still be considered the Standard Version.
 *
 *	3. You may otherwise modify your copy of this Package in any way,
 *	provided that you insert a prominent notice in each changed file
 *	stating how and when you changed that file, and provided that you do at
 *	least ONE of the following:
 *
 *		a) place your modifications in the Public Domain or otherwise make
 *		them Freely Available, such as by posting said modifications to
 *		Usenet or an equivalent medium, or placing the modifications on a
 *		major archive site such as ftp.uu.net, or by allowing the Copyright
 *		Holder to include your modifications in the Standard Version of the
 *		Package.
 *
 *		b) use the modified Package only within your corporation or
 *		organization.
 *
 *		c) rename any non-standard executables so the names do not conflict
 *		with standard executables, which must also be provided, and provide
 *		a separate manual page for each non-standard executable that
 *		clearly documents how it differs from the Standard Version.
 *
 *		d) make other distribution arrangements with the Copyright Holder.
 *
 *	4. You may distribute the programs of this Package in object code or
 *	executable form, provided that you do at least ONE of the following:
 *
 *		a) distribute a Standard Version of the executables and library
 *		files, together with instructions (in the manual page or
 *		equivalent) on where to get the Standard Version.
 *
 *		b) accompany the distribution with the machine-readable source of
 *		the Package with your modifications.
 *
 *		c) accompany any non-standard executables with their corresponding
 *		Standard Version executables, giving the non-standard executables
 *		non-standard names, and clearly documenting the differences in
 *		manual pages (or equivalent), together with instructions on where
 *		to get the Standard Version.
 *
 *		d) make other distribution arrangements with the Copyright Holder.
 *
 *	5. You may charge a reasonable copying fee for any distribution of this
 *	Package. You may charge any fee you choose for support of this
 *	Package. You may not charge a fee for this Package itself.  However,
 *	you may distribute this Package in aggregate with other (possibly
 *	commercial) programs as part of a larger (possibly commercial) software
 *	distribution provided that you do not advertise this Package as a
 *	product of your own.
 *
 *	6. The scripts and library files supplied as input to or produced as
 *	output from the programs of this Package do not automatically fall
 *	under the copyright of this Package, but belong to whomever generated
 *	them, and may be sold commercially, and may be aggregated with this
 *	Package.
 *
 *	7. C or perl subroutines supplied by you and linked into this Package
 *	shall not be considered part of this Package.
 *
 *	8. The name of the Copyright Holder may not be used to endorse or
 *	promote products derived from this software without specific prior
 *	written permission.
 *
 *	9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 *	WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 *	MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package org.chiba.xml.xforms.constraints;

import org.apache.log4j.Category;
import org.apache.xerces.dom.NodeImpl;
import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.Instance;
import org.chiba.xml.xforms.ModelItemProperties;
import org.chiba.xml.xforms.NamespaceCtx;
import org.w3c.dom.*;

/**
 * Selects relevant instance data for submission.
 *
 * @author <a href="mailto:unl@users.sourceforge.net">Ulrich Nicolas Liss&eacute;</a>
 * @version $Id: RelevanceSelector.java,v 1.8 2004/08/15 14:14:13 joernt Exp $
 */
public class RelevanceSelector {

    private static Category LOGGER = Category.getInstance(RelevanceSelector.class);

    /**
     * Returns a document containing only the relevant model items
     * of the specified instance data.
     *
     * @param instance the instance data.
     * @return a document containing only the relevant model items
     *         of the specified instance data.
     */
    public static Document selectRelevant(Instance instance) {
        return selectRelevant(instance, "/");
    }

    /**
     * Returns a document containing only the relevant model items
     * of the specified instance data.
     *
     * @param instance the instance.
     * @param path     the path denoting an instance subtree.
     * @return a document containing only the relevant model items
     *         of the specified instance data.
     */
    public static Document selectRelevant(Instance instance, String path) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("select relevant: processing " + path);
        }

        Document relevantDocument = DOMUtil.newDocument(true, false);
        NodeImpl instanceRoot = (NodeImpl) instance.getInstanceDocument().getDocumentElement();
        NodeImpl instanceNode = (NodeImpl) instance.getPointer(path).getNode();

        if (instanceNode.getNodeType() == Node.DOCUMENT_NODE) {
            if (isEnabled(instanceRoot)) {
                // process document tree
                addElement(relevantDocument, instanceRoot);
            }
        }

        if (instanceNode.getNodeType() == Node.ELEMENT_NODE) {
            if (isEnabled(instanceNode)) {
                // process element subtree
                addElement(relevantDocument, instanceNode);

                // apply namespaces
                NamespaceCtx.applyNamespaces((Element) instanceNode, relevantDocument.getDocumentElement());
            }
        }

        return relevantDocument;
    }

    private static void addElement(Document relevantDocument, NodeImpl instanceNode) {
        Element relevantElement;

        if (instanceNode.getNamespaceURI() == null) {
            relevantElement = relevantDocument.createElement(instanceNode.getNodeName());
        } else {
            relevantElement = relevantDocument.createElementNS(instanceNode.getNamespaceURI(),
                    instanceNode.getNodeName());
        }

        relevantDocument.appendChild(relevantElement);
        addAttributes(relevantElement, instanceNode);
        addChildren(relevantElement, instanceNode);
    }

    private static void addElement(Element relevantParent, NodeImpl instanceNode) {
        Document relevantDocument = relevantParent.getOwnerDocument();
        Element relevantElement;

        if (instanceNode.getNamespaceURI() == null) {
            relevantElement = relevantDocument.createElement(instanceNode.getNodeName());
        } else {
            relevantElement = relevantDocument.createElementNS(instanceNode.getNamespaceURI(),
                    instanceNode.getNodeName());
        }

        // needed in instance serializer ...
        ((NodeImpl) relevantElement).setUserData(instanceNode.getUserData());

        relevantParent.appendChild(relevantElement);
        addAttributes(relevantElement, instanceNode);
        addChildren(relevantElement, instanceNode);
    }

    private static void addAttributes(Element relevantElement, NodeImpl instanceNode) {
        NamedNodeMap instanceAttributes = instanceNode.getAttributes();

        for (int index = 0; index < instanceAttributes.getLength(); index++) {
            NodeImpl instanceAttr = (NodeImpl) instanceAttributes.item(index);

            if (isEnabled(instanceAttr)) {
                if (instanceAttr.getNamespaceURI() == null) {
                    relevantElement.setAttribute(instanceAttr.getNodeName(),
                            instanceAttr.getNodeValue());
                } else {
                    relevantElement.setAttributeNS(instanceAttr.getNamespaceURI(),
                            instanceAttr.getNodeName(),
                            instanceAttr.getNodeValue());
                }
            }
        }
    }

    private static void addChildren(Element relevantElement, NodeImpl instanceNode) {
        Document ownerDocument = relevantElement.getOwnerDocument();
        NodeList instanceChildren = instanceNode.getChildNodes();

        for (int index = 0; index < instanceChildren.getLength(); index++) {
            NodeImpl instanceChild = (NodeImpl) instanceChildren.item(index);

            if (isEnabled(instanceChild)) {
                switch (instanceChild.getNodeType()) {
                    case Node.TEXT_NODE:
                        /* rather not, otherwise we cannot follow specs when
                         * serializing to multipart/form-data for example
                         *
                        // denormalize text for better whitespace handling during serialization
                        List list = DOMWhitespace.denormalizeText(instanceChild.getNodeValue());
                        for (int item = 0; item < list.size(); item++) {
                            relevantElement.appendChild(ownerDocument.createTextNode(list.get(item).toString()));
                        }
			*/
                        relevantElement.appendChild(ownerDocument.createTextNode(instanceChild.getNodeValue()));
                        break;
                    case Node.CDATA_SECTION_NODE:
                        relevantElement.appendChild(ownerDocument.createCDATASection(instanceChild.getNodeValue()));
                        break;
                    case Node.ELEMENT_NODE:
                        addElement(relevantElement, instanceChild);
                        break;
                    default:
                        // ignore
                        break;
                }
            }
        }
    }

    private static boolean isEnabled(NodeImpl nodeImpl) {
        ModelItemProperties item = (ModelItemProperties) nodeImpl.getUserData();
        return item == null || item.isEnabled();
    }

}
