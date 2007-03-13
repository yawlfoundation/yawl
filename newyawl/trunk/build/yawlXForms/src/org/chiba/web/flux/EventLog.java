// Copyright 2005 Chibacon
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
package org.chiba.web.flux;

import org.chiba.xml.dom.DOMUtil;
import org.chiba.xml.events.ChibaEventNames;
import org.chiba.xml.events.XMLEvent;
import org.chiba.xml.xforms.XFormsConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * EventLog logs all events happening in XForms processor and build a DOM
 * document which represent those events.
 *
 * @author Joern Turner
 * @version $Id: EventLog.java,v 1.1 2006/09/10 19:50:45 joernt Exp $
 */
public class EventLog {
    private static List HELPER_ELEMENTS = Arrays.asList(new String[]{XFormsConstants.LABEL, XFormsConstants.HELP, XFormsConstants.HINT, XFormsConstants.ALERT, XFormsConstants.VALUE});
    private static List SELECTOR_ELEMENTS = Arrays.asList(new String[]{XFormsConstants.SELECT1, XFormsConstants.SELECT});

    private Document doc;
    private Element root;
    private Element selector;

    public EventLog() {
        this.doc = DOMUtil.newDocument(false, false);
        this.root = this.doc.createElement("eventlog");
        this.root.setAttribute("id", "eventlog");
        this.doc.appendChild(this.root);
    }

    public Element getLog() {
        return (Element) this.root.cloneNode(true);
    }

    public void add(XMLEvent event) {
        // get target properties
        String type = event.getType();
        Element target = (Element) event.getTarget();
        String targetId = target.getAttributeNS(null, "id");
        String targetName = target.getLocalName();

        // create event element
        Element element;

        if (ChibaEventNames.STATE_CHANGED.equals(type) && SELECTOR_ELEMENTS.contains(targetName)) {
            // selector events are always appended to the end of the log
            // to ensure their items' labels and values are updated before
            element = insert(null, type, targetId, targetName);
            if (this.selector == null) {
                this.selector = element;
            }
        }
        else {
            // all other events are inserted before any selector events
            element = insert(this.selector, type, targetId, targetName);
        }

        if (ChibaEventNames.STATE_CHANGED.equals(type) && HELPER_ELEMENTS.contains(targetName)) {
            // parent id is needed for updating all helper elements cause they
            // are identified by '<parentId>-label' etc. rather than their own id
            String parentId = ((Element) target.getParentNode()).getAttributeNS(null, "id");
            addProperty(element, "parentId", parentId);
        }

        // add event params
        Iterator iterator = event.getPropertyNames().iterator();
        String name;
        Object context;
        while (iterator.hasNext()) {
            name = (String) iterator.next();
            context = event.getContextInfo(name);
            addProperty(element, name, context != null ? context.toString() : null);
        }
    }

    public Element add(String type, String targetId, String targetName){
        return insert(this.selector, type, targetId, targetName);
    }

    public Element addProperty(Element element, String name, String value) {
        Element property = this.doc.createElement("property");
        property.setAttribute("name", name);
        if (value != null) {
            property.appendChild(this.doc.createTextNode(value));
        }
        element.appendChild(property);

        return element;
    }

    public Element addProperty(Element element, String name, Element value) {
        Element property = this.doc.createElement("property");
        property.setAttribute("name", name);
        property.appendChild(value);
        element.appendChild(property);
        return element;
    }


    private Element insert(Element ref, String type, String targetId, String targetName){
        // create event element
        Element element = this.doc.createElement("event");
        this.root.insertBefore(element, ref);

        // add target properties
        element.setAttribute("type", type);
        element.setAttribute("targetId", targetId);
        element.setAttribute("targetName", targetName);
        return element;
    }


    // clean the log
    public void flush() {
        DOMUtil.removeAllChildren(this.root);
        this.selector = null;
    }
}
