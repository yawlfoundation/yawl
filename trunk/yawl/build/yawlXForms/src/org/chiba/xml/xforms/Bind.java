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

import org.apache.commons.jxpath.Pointer;
import org.apache.log4j.Category;
import org.apache.xerces.dom.ElementImpl;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.events.XFormsEvent;
import org.chiba.xml.xforms.exception.XFormsBindingException;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.xpath.PathUtil;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;

import java.util.Iterator;

/**
 * Implementation of XForms Model Bind Element.
 *
 * @version $Id$
 */
public class Bind extends XFormsElement implements Binding {

    private static Category LOGGER = Category.getInstance(Bind.class);
    private String locationPath = null;

    /**
     * Creates a new Bind object.
     *
     * @param element the DOM Element annotated by this object
     * @param model   the parent Model object
     */
    public Bind(Element element, Model model) {
        super(element, model);

        // register with model
        getModel().addModelBinding(this);
    }

    // lifecycle methods

    /**
     * Performs element init.
     *
     * @throws XFormsException if any error occurred during init.
     */
    public void init() throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " init");
        }

        initializeLocationPath();
        initializeModelItems();
        Initializer.initializeBindElements(getModel(), getElement());
    }

    // implementation of 'org.chiba.xml.xforms.Binding'

    /**
     * Returns the binding expression.
     *
     * @return the binding expression.
     */
    public String getBindingExpression() {
        return this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, NODESET_ATTRIBUTE);
    }

    /**
     * Returns the id of the binding element.
     *
     * @return the id of the binding element.
     */
    public String getBindingId() {
        return this.id;
    }

    /**
     * Returns the enclosing element.
     *
     * @return the enclosing element.
     */
    public Binding getEnclosingBinding() {
        ElementImpl parentElement = (ElementImpl) this.element.getParentNode();

        if (parentElement.getLocalName().equals(XFormsConstants.MODEL)) {
            return null;
        }

        return (Binding) parentElement.getUserData();
    }

    /**
     * Returns the location path.
     *
     * @return the location path.
     */
    public String getLocationPath() {
        return this.locationPath;
    }

    /**
     * Returns the model id of the binding element.
     *
     * @return the model id of the binding element.
     */
    public String getModelId() {
        return this.model.getId();
    }

    // bind members

    /**
     * Returns the <code>calculate</code> attribute.
     *
     * @return the <code>calculate</code> attribute.
     */
    public String getCalculate() {
        return this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, CALCULATE_ATTRIBUTE);
    }

    /**
     * Returns the <code>constraint</code> attribute.
     *
     * @return the <code>constraint</code> attribute.
     */
    public String getConstraint() {
        return this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, CONSTRAINT_ATTRIBUTE);
    }

    /**
     * Returns the <code>type</code> attribute.
     *
     * @return the <code>type</code> attribute.
     */
    public String getDatatype() {
        return this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, TYPE_ATTRIBUTE);
    }

    /**
     * Returns the <code>p3ptype</code> attribute.
     *
     * @return the <code>p3ptype</code> attribute.
     */
    public String getP3PType() {
        return this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, P3PTYPE_ATTRIBUTE);
    }

    /**
     * Returns the <code>readonly</code> attribute.
     *
     * @return the <code>readonly</code> attribute.
     */
    public String getReadonly() {
        return this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, READONLY_ATTRIBUTE);
    }

    /**
     * Returns the <code>relevant</code> attribute.
     *
     * @return the <code>relevant</code> attribute.
     */
    public String getRelevant() {
        return this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, RELEVANT_ATTRIBUTE);
    }

    /**
     * Returns the <code>required</code> attribute.
     *
     * @return the <code>required</code> attribute.
     */
    public String getRequired() {
        return this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, REQUIRED_ATTRIBUTE);
    }

    /**
     * Checks wether this bind has a <code>calculate</code> attribute.
     *
     * @return <code>true</code> if this bind has a <code>calculate</code> attribute, otherwise <code>false</code>.
     */
    public boolean hasCalculate() {
        return this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, CALCULATE_ATTRIBUTE);
    }

    /**
     * Checks wether this bind has a <code>constraint</code> attribute.
     *
     * @return <code>true</code> if this bind has a <code>constraint</code> attribute, otherwise <code>false</code>.
     */
    public boolean hasConstraint() {
        return this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, CONSTRAINT_ATTRIBUTE);
    }

    /**
     * Checks wether this bind has a <code>type</code> attribute.
     *
     * @return <code>true</code> if this bind has a <code>type</code> attribute, otherwise <code>false</code>.
     */
    public boolean hasDatatype() {
        return this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, TYPE_ATTRIBUTE);
    }

    /**
     * Checks wether this bind has a <code>p3ptype</code> attribute.
     *
     * @return <code>true</code> if this bind has a <code>p3ptype</code> attribute, otherwise <code>false</code>.
     */
    public boolean hasP3PType() {
        return this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, P3PTYPE_ATTRIBUTE);
    }

    /**
     * Checks wether this bind has a <code>readonly</code> attribute.
     *
     * @return <code>true</code> if this bind has a <code>readonly</code> attribute, otherwise <code>false</code>.
     */
    public boolean hasReadonly() {
        return this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, READONLY_ATTRIBUTE);
    }

    /**
     * Checks wether this bind has a <code>relevant</code> attribute.
     *
     * @return <code>true</code> if this bind has a <code>relevant</code> attribute, otherwise <code>false</code>.
     */
    public boolean hasRelevant() {
        return this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, RELEVANT_ATTRIBUTE);
    }

    /**
     * Checks wether this bind has a <code>required</code> attribute.
     *
     * @return <code>true</code> if this bind has a <code>required</code> attribute, otherwise <code>false</code>.
     */
    public boolean hasRequired() {
        return this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, REQUIRED_ATTRIBUTE);
    }

    // event handling methods

    /**
     * Performs the default action for the given event.
     *
     * @param event the event for which default action is requested.
     */
    protected void performDefault(Event event) {
        if (event.getType().equals(EventFactory.BINDING_EXCEPTION)) {
            getLogger().error(this + " binding exception: " + ((XFormsEvent) event).getContextInfo());
            return;
        }
    }

    /**
     * Returns the logger object.
     *
     * @return the logger object.
     */
    protected Category getLogger() {
        return LOGGER;
    }

    // lifecycle template methods

    /**
     * Initializes the location path.
     */
    private void initializeLocationPath() {
        this.locationPath = this.container.getBindingResolver().resolve(this);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " init: resolved location path: " + this.locationPath);
        }
    }

    /**
     * Initializes all bound model items.
     *
     * @throws XFormsException if any error occured during model item init.
     */
    private void initializeModelItems() throws XFormsException {
        String locationPath = getLocationPath();
        Instance instance = getModel().getInstance(PathUtil.getInstanceId(this.model, locationPath));

        if (instance != null && instance.existsNode(locationPath)) {
            // initialize all bound model items
            Iterator iterator = instance.getPointerIterator(locationPath);
            while (iterator.hasNext()) {
                Pointer instancePointer = (Pointer) iterator.next();
                String path = instancePointer.asPath();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(this + " init: initializing model item for path '" + path + "'");
                }

                // 4.2.1 - 4.b applying model item properties to each node
                initializeModelItemProperties(instance.getModelItem(path));
            }
        }
    }

    /**
     * Initializes the model item properties of the specified model item.
     *
     * @param item the model item.
     * @throws XFormsException if any error occured during model item properties init.
     */
    public void initializeModelItemProperties(ModelItem item) throws XFormsException {
        if (hasDatatype()) {
            if (item.getDatatype() != null) {
                throw new XFormsBindingException("property 'type' already present at model item", this.target, this.id);
            }

            String datatype = getDatatype();
            if (!this.model.getValidator().isSupported(datatype)) {
                throw new XFormsBindingException("datatype '" + datatype + "' is not supported", this.target, this.id);
            }
            if (!this.model.getValidator().isKnown(datatype)) {
                throw new XFormsBindingException("datatype '" + datatype + "' is unknown", this.target, this.id);
            }

            item.setDatatype(datatype);
        }

        if (hasReadonly()) {
            if (item.getReadonly() != null) {
                throw new XFormsBindingException("property 'readonly' already present at model item", this.target, this.id);
            }

            item.setReadonly(getReadonly());
        }

        if (hasRequired()) {
            if (item.getRequired() != null) {
                throw new XFormsBindingException("property 'required' already present at model item", this.target, this.id);
            }

            item.setRequired(getRequired());
        }

        if (hasRelevant()) {
            if (item.getRelevant() != null) {
                throw new XFormsBindingException("property 'relevant' already present at model item", this.target, this.id);
            }

            item.setRelevant(getRelevant());
        }

        if (hasCalculate()) {
            if (item.getCalculate() != null) {
                throw new XFormsBindingException("property 'calculate' already present at model item", this.target, this.id);
            }

            item.setCalculate(getCalculate());
        }

        if (hasConstraint()) {
            if (item.getConstraint() != null) {
                throw new XFormsBindingException("property 'constraint' already present at model item", this.target, this.id);
            }

            item.setConstraint(getConstraint());
        }

        if (hasP3PType()) {
            if (item.getP3PType() != null) {
                throw new XFormsBindingException("property 'p3ptype' already present at model item", this.target, this.id);
            }

            item.setP3PType(getP3PType());
        }
    }

}

//end of class
