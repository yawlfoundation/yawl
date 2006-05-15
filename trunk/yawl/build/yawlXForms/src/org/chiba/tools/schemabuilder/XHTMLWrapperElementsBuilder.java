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
package org.chiba.tools.schemabuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * XHTML implementation of WrapperElementsBuilder: allows to wrap the XForm document in XHTML tags.
 *
 * @author Sophie Ramel
 */
public class XHTMLWrapperElementsBuilder implements WrapperElementsBuilder {

    private final static String XHTML_NS = "http://www.w3.org/2002/06/xhtml2";
    private final static String XHTML_PREFIX = "html";

    private String title;
    private Vector links;
    private Vector meta;
    private Hashtable namespaces;

    /**
     * Creates a new instance of XHTMLWrapperElementsBuilder
     */
    public XHTMLWrapperElementsBuilder() {
        meta = new Vector();
        links = new Vector();
        namespaces = new Hashtable();
    }

    /**
     * add a tag "title" in the header of the HTML document
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * add a tag "link" in the header of the HTML document
     *
     * @param href the "href" parameter of the "link" tag
     * @param type the "type" parameter of the "link" tag
     * @param rel  the "rel" parameter of the "link" tag
     */
    public void addLink(String href, String type, String rel) {
        String[] l = new String[3];
        l[0] = href;
        l[1] = type;
        l[2] = rel;
        links.add(l);
    }

    /**
     * add a tag "meta" in the header of the HTML document
     *
     * @param http_equiv the "http-equiv" parameter of the "META" tag
     * @param name       the "name" parameter of the "META" tag
     * @param content    the "content" parameter of the "META" tag
     * @param scheme     the "scheme" parameter of the "META" tag
     */
    public void addMeta(String http_equiv,
                        String name,
                        String content,
                        String scheme) {
        String[] s = new String[4];
        s[0] = http_equiv;
        s[1] = name;
        s[2] = content;
        s[3] = scheme;
        meta.add(s);
    }

    public void addNamespaceDeclaration(String prefix, String url) {
        namespaces.put(prefix, url);
    }

    /**
     * create the wrapper element of the different controls
     *
     * @param controlElement the control element (input, select, repeat, group, ...)
     * @return the wrapper element, already containing the control element
     */
    public Element createControlsWrapper(Element controlElement) {
        /*Document doc=controlElement.getOwnerDocument();
           Element tr=doc.createElement("tr");
           Element td=doc.createElement("td");
           tr.appendChild(td);
           td.appendChild(controlElement);
           return tr;*/
        return controlElement;
    }

    /**
     * creates the global enveloppe of the resulting document, and puts it in the document
     *
     * @return the enveloppe
     */
    public Element createEnvelope(Document doc) {
        Element html = doc.createElementNS(XHTML_NS, XHTML_PREFIX + ":html");
        //set namespace attribute
        html.setAttributeNS(SchemaFormBuilder.XMLNS_NAMESPACE_URI,
                "xmlns:" + XHTML_PREFIX,
                XHTMLWrapperElementsBuilder.XHTML_NS);
        doc.appendChild(html);

        //other namespaces
        Enumeration enumeration = namespaces.keys();
        while (enumeration.hasMoreElements()) {
            String prefix = (String) enumeration.nextElement();
            String ns = (String) namespaces.get(prefix);
            html.setAttributeNS(SchemaFormBuilder.XMLNS_NAMESPACE_URI,
                    "xmlns:" + prefix,
                    ns);

        }

        return html;
    }

    /**
     * create the element that will contain the content of the group (or repeat) element
     *
     * @param groupElement the group or repeat element
     * @return the wrapper element
     */
    public Element createGroupContentWrapper(Element groupElement) {
        /*Document doc=groupElement.getOwnerDocument();
           Element table=doc.createElement("table");
           groupElement.appendChild(table);
           return table;*/
        return groupElement;
    }

    /**
     * create the wrapper element of the form
     *
     * @param enveloppeElement the form element (chiba:form or other)
     * @return the wrapper element
     */

    public Element createFormWrapper(Element enveloppeElement) {
        Document doc = enveloppeElement.getOwnerDocument();
        Element body = doc.createElementNS(XHTML_NS, XHTML_PREFIX + ":body");
        //body.appendChild(formElement);
        enveloppeElement.appendChild(body);
        return body;
    }

    /**
     * create the wrapper element of the xforms:model element
     *
     * @param modelElement the xforms:model element
     * @return the wrapper element, already containing the model
     */
    public Element createModelWrapper(Element modelElement) {
        Document doc = modelElement.getOwnerDocument();
        Element head = doc.createElementNS(XHTML_NS, XHTML_PREFIX + ":head");
        head.appendChild(modelElement);

        //eventually add other info
        if ((title != null) && !title.equals("")) {
            Element title_el = doc.createElementNS(XHTML_NS, XHTML_PREFIX + ":title");
            Text title_text = doc.createTextNode(title);
            title_el.appendChild(title_text);
            head.appendChild(title_el);
        }

        if ((meta != null) && !meta.isEmpty()) {
            Iterator it = meta.iterator();

            while (it.hasNext()) {
                String[] m = (String[]) it.next();
                String http_equiv = m[0];
                String name = m[1];
                String content = m[2];
                String scheme = m[3];

                Element meta_el = doc.createElementNS(XHTML_NS, XHTML_PREFIX + ":META");
                head.appendChild(meta_el);

                //attributes
                if ((http_equiv != null) && !http_equiv.equals("")) {
                    meta_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":http-equiv", http_equiv);
                }

                if ((name != null) && !name.equals("")) {
                    meta_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":name", name);
                }

                if ((content != null) && !content.equals("")) {
                    meta_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":content", content);
                }

                if ((scheme != null) && !scheme.equals("")) {
                    meta_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":scheme", scheme);
                }
            }
        }

        if ((links != null) && !links.isEmpty()) {
            Iterator it = links.iterator();

            while (it.hasNext()) {
                String[] l = (String[]) it.next();
                String href = l[0];
                String type = l[1];
                String rel = l[2];

                Element link_el = doc.createElementNS(XHTML_NS, XHTML_PREFIX + ":LINK");
                head.appendChild(link_el);

                //attributes
                if ((href != null) && !href.equals("")) {
                    link_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":href", href);
                }

                if ((type != null) && !type.equals("")) {
                    link_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":type", type);
                }

                if ((rel != null) && !rel.equals("")) {
                    link_el.setAttributeNS(XHTML_NS, XHTML_PREFIX + ":rel", rel);
                }
            }
        }

        return head;
    }
}
