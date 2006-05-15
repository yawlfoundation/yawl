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
import org.apache.xerces.dom.ElementImpl;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.exception.XFormsErrorIndication;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;

/**
 * Superclass for all XForms elements. This includes either all elements from the
 * XForms namespace and all bound elements which may be from foreign namespaces but wear
 * XForms binding attributes.
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public abstract class XFormsElement implements XFormsConstants {
    /**
     * the Container object holding the whole DOM Document of this form
     */
    protected Container container = null;

    /**
     * the annotated DOM Element
     */
    protected Element element = null;

    /**
     * the DOM EventTarget
     */
    protected EventTarget target = null;

    /**
     * the Model object of this XFormsElement
     */
    protected Model model = null;

    /**
     * the id of this Element
     */
    protected String id;

    /**
     * the original id of this Element (when repeated)
     */
    protected String originalId;

    /**
     * the xforms prefix used in this Document
     */
    protected String xformsPrefix = null;


    /**
     * Creates a new XFormsElement object.
     *
     * @param element the DOM Element annotated by this object
     */
    public XFormsElement(Element element) {
        this.element = element;
        this.target = (EventTarget) element;

        this.container = getContainerObject();
        this.xformsPrefix = NamespaceCtx.getPrefix(this.element, NamespaceCtx.XFORMS_NS);
        this.id = this.element.getAttributeNS(null, "id");

        // todo: call in builder explicitely
        // register with container
        register();
    }

    /**
     * Creates a new XFormsElement object.
     *
     * @param element the DOM Element annotated by this object
     * @param model   the Model object of this XFormsElement
     */
    public XFormsElement(Element element, Model model) {
        this(element);
        this.model = model;
    }

    /**
     * Performs element init.
     *
     * @throws XFormsException if any error occurred during init.
     */
    public abstract void init() throws XFormsException;

    /**
     * returns the Container object of this Element.
     *
     * @return Container object of this Element
     */
    public Container getContainerObject() {
        return (Container) ((ElementImpl) this.element.getOwnerDocument().getDocumentElement()).getUserData();
    }

    /**
     * Returns the DOM element of this element.
     *
     * @return the DOM element of this element.
     */
    public Element getElement() {
        return this.element;
    }

    // member access methods

    /**
     * Returns the global id of this element.
     *
     * @return the global id of this element.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the context model of this element.
     *
     * @return the context model of this element.
     */
    public Model getModel() {
        return this.model;
    }

    /**
     * returns the parent XFormsElement object of the DOM parent Node if any or null otherwise.
     *
     * @return the parent XFormsElement object of the DOM parent Node if any or null otherwise.
     */
    protected XFormsElement getParentObject() {
        return (XFormsElement) ((ElementImpl) this.element.getParentNode()).getUserData();
    }

    /**
     * Returns the DOM event target of this element.
     *
     * @return the DOM event target of this element.
     */
    public EventTarget getTarget() {
        return this.target;
    }

    // id handling

    /**
     * Registers this element with the container.
     */
    public void register() {
        this.container.register(this);

        if (this.originalId != null) {
            String tmpId = this.id;
            this.id = this.originalId;
            this.container.register(this);
            this.id = tmpId;
        }
    }

    /**
     * Deregisters this element from the container.
     */
    public void deregister() {
        this.container.deregister(this);
    }

    /**
     * Stores the original id and sets the generated id as new id.
     *
     * @param generatedId the generated id.
     */
    public void setGeneratedId(String generatedId) throws XFormsException {
        this.originalId = this.id;
        this.id = generatedId;
        this.element.setAttributeNS(null, "id", this.id);
        this.container.dispatch(this.target, EventFactory.ID_GENERATED, this.originalId);
    }

    // standard methods

    /**
     * Check wether this object and the specified object are equal.
     *
     * @param object the object in question.
     * @return <code>true</code> if this object and the specified
     *         object are equal, <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (object == this) {
            return true;
        }

        if (!(object instanceof XFormsElement)) {
            return false;
        }

        return ((XFormsElement) object).getId().equals(getId());
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object.
     */
    public String toString() {
        return "[" + getElement().getNodeName() + " id='" + getId() + "']";
    }

    /**
     * Returns the logger object.
     *
     * @return the logger object.
     */
    protected abstract Category getLogger();

    /**
     * Performs the default action for the given event.
     *
     * @param event the event for which default action is requested.
     */
    protected void performDefault(Event event) {
//template method
    }

    /**
     * returns true if the current events' default processing is cancelled by some listener on the
     * element itself e.g.
     * <pre>
     *   &lt;model ev:event="xforms-revalidate" ev:defaultAction="cancel"&gt;
     * </pre>
     * would cancel all revalidation (the default action for this event) for this model.
     *
     * @param event the event to investigate
     * @return true if the current events' default processing is cancelled
     */
    protected boolean isCancelled(Event event) {
        if (event.getCancelable()) {

            //todo: check parents with phase='capture' for this event

            if (element.hasAttributeNS(NamespaceCtx.XMLEVENTS_NS, "event")) {
                String s = element.getAttributeNS(NamespaceCtx.XMLEVENTS_NS, "event");

                if (s.equals(event.getType())) {
                    if (element.getAttributeNS(NamespaceCtx.XMLEVENTS_NS, "defaultAction").equals("cancel")) {
                        return true; //performDefault gets cancelled by cancelling listener on this model
                    }
                }
            }
        }
        return false;
    }

    /**
     * Handles the given exception during event flow.
     * <p/>
     * The exception is logged. If it is an error indication, the
     * corresponding event is dispatched and the exception is stored
     * in the container for later rethrow.
     *
     * @param exception the exception to be handled.
     */
    protected final void handleException(Exception exception) {
        getLogger().error(this + " the following error occurred", exception);

        if (exception instanceof XFormsErrorIndication) {
            XFormsErrorIndication indication = (XFormsErrorIndication) exception;

            if (!indication.isHandled()) {
                // dispatch error indication event
                try {
                    this.container.dispatch(indication.getEventTarget(),
                            indication.getEventType(),
                            indication.getContextInfo());
                } catch (XFormsException e) {
                    getLogger().error("uh oh - exception during error indication event", e);
                }

                // set error indication handled
                indication.setHandled();
            }
        }

        // notify container
        this.container.setEventException(exception);
    }

}

//end of class
