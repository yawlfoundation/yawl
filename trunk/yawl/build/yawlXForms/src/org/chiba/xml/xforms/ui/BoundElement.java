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
package org.chiba.xml.xforms.ui;

import org.chiba.xml.xforms.*;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.events.XFormsEvent;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.xpath.PathUtil;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;

/**
 * Bound elements are all elements that may have XForms binding expressions.
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public abstract class BoundElement extends AbstractUIElement implements Binding {
    /**
     * the data proxy
     */
    protected DataElement dataElement = null;

    /**
     * the id of the instance this element is bound to
     */
    protected String instanceId = null;

    /**
     * the resolved xpath locationpath pointing to the target node in the
     * instance
     */
    protected String locationPath = null;

    /**
     * Creates a new BoundElement object.
     *
     * @param element the DOM Element
     * @param model the Model to which this element is bound
     */
    public BoundElement(Element element, Model model) {
        super(element, model);
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

        initializeInstanceNode();
        initializeDataElement();
        initializeChildren();
    }

    /**
     * Performs element update.
     *
     * @throws XFormsException if any error occurred during update.
     */
    public void update() throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " update");
        }

        updateDataElement();
        updateChildren();
    }

    /**
     * Performs element disposal.
     *
     * @throws XFormsException if any error occurred during disposal.
     */
    public void dispose() throws XFormsException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " dispose");
        }

        disposeChildren();
        disposeDataElement();
        disposeSelf();
    }

    // implementation of 'org.chiba.xml.xforms.Binding'

    /**
     * Returns the binding expression.
     *
     * @return the binding expression.
     */
    public String getBindingExpression() {
        return this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, REF_ATTRIBUTE);
    }

    /**
     * Returns the id of the binding element.
     *
     * @return the id of the binding element.
     */
    public String getBindingId() {
        if (hasModelBinding()) {
            return getModelBinding().getId();
        }

        return this.id;
    }

    /**
     * Returns the enclosing element.
     *
     * @return the enclosing element.
     */
    public Binding getEnclosingBinding() {
        return BindingResolver.getEnclosingBinding(this);
    }

    /**
     * Returns the location path.
     *
     * @return the location path.
     */
    public String getLocationPath() {
        if (!isBound()) {
            return null;
        }

        if (isRepeated()) {
            // don't cache location path for repeated items
            return BindingResolver.getExpressionPath(this, getRepeatItemId());
        }

        if (this.locationPath == null) {
            // cache location path for non-repeated items
            this.locationPath = BindingResolver.getExpressionPath(this, null);
        }

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

    // bound element methods

    /**
     * Checks wether this element is bound to a model item.
     * <p/>
     * This element is considered bound if it has either a model binding or an ui binding.
     *
     * @return <code>true</code> if this element is bound to a model item, otherwise <code>false</code>.
     */
    public boolean isBound() {
        return hasModelBinding() || hasUIBinding();
    }

    /**
     * Returns the id of the instance this element is bound to.
     * <p/>
     * The instance id is determined as follows: <ol> <li>If the location path starts with a
     * <code>xforms:instance()</code> function, the instance id is obtained from the argument of that fuction.</li>
     * <li>If there is a default instance in the corresponding model, the instance id is obtained from the default
     * instance.</li> <li>The instance id is set empty, which maps to the default instance.</li> </ol>
     *
     * @return the id of the instance this element is bound to.
     */
    public String getInstanceId() {
        // lazy initialization
        if (this.instanceId == null) {
            String path = getLocationPath();
            this.instanceId = PathUtil.getInstanceId(this.model, path);
        }

        return this.instanceId;
    }

    /**
     * Returns the value of the instance node this element is bound to.
     *
     * @return the value of the instance node this element is bound to.
     */
    public String getInstanceValue() {
        Instance instance = this.model.getInstance(getInstanceId());

        return (String) instance.getInstanceContext().getValue(getLocationPath());
    }

    /**
     * Checks wether this control is a leaf control.
     * <p/>
     * A control is a leaf control if it cannot contain other controls.
     *
     * @return <code>true</code> if this control is a leaf control, <code>false</code> otherwise.
     */
    public boolean isLeaf() {
        return true;
    }

    /**
     * Checks wether this element has an ui binding.
     * <p/>
     * This element has an ui binding if it has a <code>ref</code> attribute.
     *
     * @return <code>true</code> if this element has an ui binding, otherwise <code>false</code>.
     */
    protected boolean hasUIBinding() {
        return this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, REF_ATTRIBUTE);
    }

    /**
     * Checks wether this element has a model binding.
     * <p/>
     * This element has a model binding if it has a <code>bind</code> attribute.
     *
     * @return <code>true</code> if this element has a model binding, otherwise <code>false</code>.
     */
    protected boolean hasModelBinding() {
        return this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, BIND_ATTRIBUTE);
    }

    /**
     * Returns the model binding of this element.
     *
     * @return the model binding of this element.
     */
    protected Bind getModelBinding() {
        if (hasModelBinding()) {
            String bindId = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, BIND_ATTRIBUTE);

            return (Bind) this.container.lookup(bindId);
        }

        return null;
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

    // lifecycle template methods

    /**
     * Initializes the bound instance node.
     * <p/>
     * This methods implements the behaviour defined in [4.2.2 The xforms-model-construct-done Event].
     *
     * @throws XFormsException if an error occurred during init.
     */
    protected final void initializeInstanceNode() throws XFormsException {
        if (isBound()) {
            // 4.2.2 The xforms-model-construct-done Event
            // The default action for this event happens once, no matter how many XForms
            // Models are present in the containing document, and results in the following,
            // for each form control:
            // Processing can proceed in one of two different ways depending on whether an
            // instance in a model exists when the first form control is processed.
            // If the instance referenced on the form control existed when the first form
            // control was processed:
            // 1. The binding expression is evaluated to ensure that it points to a node
            //    that exists. If this is not the case then the form control should behave
            //    in the same manner as if it had bound to a model item with the relevant
            //    model item property resolved to false.
            // If the instance referenced on the form control did not exist when the first
            // form control for the same instance was processed:
            // 1. For the first reference to an instance a default instance is created by
            //    following the rules described below.
            //    a. A root instanceData element is created.
            //    b. An instance data element node will be created using the binding expression
            //       from the user interface control as the name. If the name is not a valid QName,
            //       processing halts with an exception (4.5.1 The xforms-binding-exception Event).
            // 2. For the second and subsequent references to an instance which was automatically
            //    created the following processing is performed:
            //    a. If a matching instance data node is found, the user interface control will be
            //       connected to that element.
            //    b. If a matching instance data node is not found, an instance data node will be
            //       created using the binding expression from the user interface control as the name.
            //       If the name is not a valid QName, processing halts with an exception (4.5.1 The
            //       xforms-binding-exception Event).
            String instanceId = getInstanceId();
            Instance instance = getModel().getInstance(instanceId);

            if (instance == null) {
                // create instance
                instance = this.model.addInstance(instanceId);
            }

            String locationPath = getLocationPath();

            if (!instance.existsNode(locationPath)) {
                if (instance.hasInitialInstance()) {
                    // disabling is handled by data element
                }
                else {
                    if (locationPath.startsWith(BindingResolver.OUTERMOST_CONTEXT)) {
                        // patch generic location path to start with 'instanceData' root
                        locationPath = locationPath.substring(BindingResolver.OUTERMOST_CONTEXT.length());
                        locationPath = "/instanceData" + locationPath;
                    }

                    // create instance node
                    instance.createNode(locationPath);
                }
            }
        }
    }

    /**
     * Initializes the chiba data proxy.
     */
    protected final void initializeDataElement() {
        if (isBound()) {
            // create and initialize chiba data element
            this.dataElement = new DataElement(this);
            this.dataElement.setHandleProperties(true);
            this.dataElement.setHandlePath(true);
            this.dataElement.setHandleTypes(isLeaf());
            this.dataElement.setHandleValue(isLeaf());
            this.dataElement.init();
        }
    }

    /**
     * Updates the chiba data proxy.
     */
    protected final void updateDataElement() {
        if (isBound()) {
            this.dataElement.update();
        }
    }

    /**
     * Disposes the chiba data proxy.
     */
    protected final void disposeDataElement() {
        if (isBound()) {
            this.dataElement.dispose();
            this.dataElement = null;
        }
    }


    // helper methods

    /**
     * this dispatches the 'Value Change with Focus Change' [XForms 1.0:4.6.7] event sequence
     *
     * @throws XFormsException
     */
    protected void dispatchValueChangeSequence() throws XFormsException {
        this.model.getContainer().dispatch(this.model.getTarget(), EventFactory.RECALCULATE, null);
        this.model.getContainer().dispatch(this.model.getTarget(), EventFactory.REVALIDATE, null);
        // dispatch of valid/invalid, enabled/disabled, optional/required,
        // readonly/readwrite is handled by chiba data proxy
        this.model.getContainer().dispatch(this.target, EventFactory.VALUE_CHANGED, null);
        this.model.getContainer().dispatch(this.target, EventFactory.DOM_FOCUS_OUT, null);
        this.model.getContainer().dispatch(this.target, EventFactory.DOM_FOCUS_IN, null);
        this.model.getContainer().dispatch(this.model.getTarget(), EventFactory.REFRESH, null);
    }

}
