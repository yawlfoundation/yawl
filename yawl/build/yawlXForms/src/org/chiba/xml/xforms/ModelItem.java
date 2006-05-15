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

import org.apache.xerces.dom.NodeImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements XForms model item properties. <P> Annotates the additional state information required for XForms
 * processing to simple DOM nodes. Each bound DOM node in the instance should have an model item object attached. States
 * are computed based on local values provided by calculator / validator results, thus implementing the rules stated in
 * http://www.w3.org/TR/2003/REC-xforms-20031014/index-all.html#model-xformsconstraints .
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public class ModelItem implements ModelItemProperties, LocalValue {

    // the lexical property values
    private String datatype;
    private String readonly;
    private String required;
    private String relevant;
    private String calculate;
    private String constraint;
    private String p3ptype;

    // the local property values
    private boolean localReadonly;
    private boolean localRequired;
    private boolean localRelevant;
    private boolean constraintValid;
    private boolean datatypeValid;
    private boolean requiredValid;

    // the computed property changes
    private boolean wasEnabled;
    private boolean wasReadonly;
    private boolean wasRequired;
    private boolean wasValid;

    private ModelItemProperties parent;
    private NodeImpl node;
    private List listeners;

    /**
     * Creates a new ModelItem object.
     */
    public ModelItem() {
        // default settings
        this.localReadonly = false;
        this.localRequired = false;
        this.localRelevant = true;
        this.constraintValid = true;
        this.datatypeValid = true;
        this.requiredValid = true;
        this.wasEnabled = true;
        this.wasReadonly = false;
        this.wasRequired = false;
        this.wasValid = true;
    }

    /**
     * Creates a new ModelItem object.
     *
     * @param node the DOM Node
     */
    public ModelItem(NodeImpl node) {
        this();
        this.node = node;
    }

    /**
     * Returns the node.
     *
     * @return the node.
     */
    public Object getNode() {
        return this.node;
    }

    /**
     * Stes the node.
     *
     * @param node the node.
     */
    public void setNode(NodeImpl node) {
        this.node = node;
    }

    /**
     * Returns the parent.
     *
     * @return the parent.
     */
    public ModelItemProperties getParent() {
        if (this.parent != null) {
            return this.parent;
        }

        if (this.node == null) {
            return null;
        }

        NodeImpl parentNode;
        if (this.node instanceof Attr) {
            parentNode = (NodeImpl) ((Attr) this.node).getOwnerElement();
        } else {
            parentNode = (NodeImpl) this.node.getParentNode();
        }

        if (parentNode == null) {
            return null;
        }

        this.parent = (ModelItemProperties) parentNode.getUserData();
        return this.parent;
    }

    /**
     * Sets the parent.
     *
     * @param parent the parent.
     */
    public void setParent(ModelItemProperties parent) {
        this.parent = parent;
    }

    /**
     * Sets the <code>type</code> model item property.
     *
     * @param datatype the <code>type</code> model item property.
     */
    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    /**
     * Sets the <code>readonly</code> model item property.
     *
     * @param readonly the <code>readonly</code> model item property.
     */
    public void setReadonly(String readonly) {
        this.readonly = readonly;
    }

    /**
     * Sets the <code>required</code> model item property.
     *
     * @param required the <code>required</code> model item property.
     */
    public void setRequired(String required) {
        this.required = required;
    }

    /**
     * Sets the <code>relevant</code> model item property.
     *
     * @param relevant the <code>relevant</code> model item property.
     */
    public void setRelevant(String relevant) {
        this.relevant = relevant;
    }

    /**
     * Sets the <code>calculate</code> model item property.
     *
     * @param calculate the <code>calculate</code> model item property.
     */
    public void setCalculate(String calculate) {
        this.calculate = calculate;
    }

    /**
     * Sets the <code>constraint</code> model item property.
     *
     * @param constraint the <code>constraint</code> model item property.
     */
    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    /**
     * Sets the <code>p3pType</code> model item property.
     *
     * @param p3ptype the <code>p3pType</code> model item property.
     */
    public void setP3PType(String p3ptype) {
        this.p3ptype = p3ptype;
    }

    // implementation of 'org.chiba.xml.xforms.ModelItemProperties'

    /**
     * Returns the <code>type</code> model item property.
     *
     * @return the <code>type</code> model item property.
     */
    public String getDatatype() {
        return this.datatype;
    }

    /**
     * Returns the <code>readonly</code> model item property.
     *
     * @return the <code>readonly</code> model item property.
     */
    public String getReadonly() {
        return this.readonly;
    }

    /**
     * Returns the <code>required</code> model item property.
     *
     * @return the <code>required</code> model item property.
     */
    public String getRequired() {
        return this.required;
    }

    /**
     * Returns the <code>relevant</code> model item property.
     *
     * @return the <code>relevant</code> model item property.
     */
    public String getRelevant() {
        return this.relevant;
    }

    /**
     * Returns the <code>calculate</code> model item property.
     *
     * @return the <code>calculate</code> model item property.
     */
    public String getCalculate() {
        return this.calculate;
    }

    /**
     * Returns the <code>constraint</code> model item property.
     *
     * @return the <code>constraint</code> model item property.
     */
    public String getConstraint() {
        return this.constraint;
    }

    /**
     * Returns the <code>p3ptype</code> model item property.
     *
     * @return the <code>p3ptype</code> model item property.
     */
    public String getP3PType() {
        return this.p3ptype;
    }

    /**
     * Returns the computed <code>valid</code> state. <P> When a model item property is required, it is valid when it
     * has a value *and* the constraint property evaluates to <code>true</code> as well as the type property is
     * satisfied. <P> When a model item property is not required, it is valid when it has no value *or* the constraint
     * property evaluates to <code>true</code> as well as the type property is satisfied.
     *
     * @return the computed <code>valid</code> state.
     */
    public boolean isValid() {
        return isLocalRequired()
                ? isRequiredValid() && isConstraintValid() && isDatatypeValid()
                : (!isRequiredValid()) || (isConstraintValid() && isDatatypeValid());
    }

    /**
     * Returns the computed <code>readonly</code> state. <P> A model item is readonly when its readonly property
     * evaluates to <code>true</code> or its parent item is readonly.
     *
     * @return the computed <code>readonly</code> state.
     */
    public boolean isReadonly() {
        ModelItemProperties parentItem = getParent();

        if (parentItem != null) {
            return isLocalReadonly() || parentItem.isReadonly();
        }

        return isLocalReadonly();
    }

    /**
     * Returns the computed <code>required</code> state. <P> A model item is required when its required property
     * evaluates to <code>true</code>.
     *
     * @return the computed <code>required</code> state.
     */
    public boolean isRequired() {
        return isLocalRequired();
    }

    /**
     * Returns the computed <code>enabled</code> state. <P> A model item is enabled when its relevant property evaluates
     * to <code>true</code> and its parent item is enabled.
     *
     * @return the computed <code>enabled</code> state.
     */
    public boolean isEnabled() {
        ModelItemProperties parentItem = getParent();

        if (parentItem != null) {
            return isLocalRelevant() && parentItem.isEnabled();
        }

        return isLocalRelevant();
    }

    /**
     * Sets the value of the associated model item.
     *
     * @param value the value of the associated model item.
     */
    public void setValue(String value) {
        // from: 8.1.1 Implementation Requirements Common to All Form Controls
        // Form controls that write simpleContent to instance data must do so exactly as
        // defined by the XForms Action 10.1.9 The setvalue Element

        // from: 10.1.9 The setvalue Element
        // All strings are inserted into the instance data as follows:
        // * Element nodes: If the element has any child text nodes, the first text node
        //                  is replaced with one corresponding to the new value. If no child
        //                  text nodes are present, a text node is created, corresponding to
        //                  the new value, and appended as the first child node.
        if (Node.ELEMENT_NODE == this.node.getNodeType()) {
            NodeList children = this.node.getChildNodes();
            for (int index = 0; index < children.getLength(); index++) {
                Node child = children.item(index);
                if (Node.TEXT_NODE == child.getNodeType()) {
                    child.setNodeValue(value);
                    return;
                }
            }

            this.node.insertBefore(this.node.getOwnerDocument().createTextNode(value), this.node.getFirstChild());
        }

        // * Attribute nodes: The string-value of the attribute is replaced with a string corresponding
        //                    to the new value.
        if (Node.ATTRIBUTE_NODE == this.node.getNodeType()) {
            this.node.setNodeValue(value);
        }

        // * Text nodes: The text node is replaced with a new one corresponding to the new value.
        if (Node.TEXT_NODE == this.node.getNodeType()) {
            this.node.setNodeValue(value);
        }

        // * Namespace, processing instruction, comment, and the XPath root node: behavior is undefined.
    }

    /**
     * Returns the value of the associated model item.
     *
     * @return the value of the associated model item.
     */
    public String getValue() {
        // from: 8.1.1 Implementation Requirements Common to All Form Controls
        // All form controls that read simpleContent instance data must do so as follows:

        // * Element nodes: if text child nodes are present, returns the string-value of the
        //                  first text child node. Otherwise, returns "" (the empty string)
        if (Node.ELEMENT_NODE == this.node.getNodeType()) {
            NodeList children = this.node.getChildNodes();
            for (int index = 0; index < children.getLength(); index++) {
                Node child = children.item(index);
                if (Node.TEXT_NODE == child.getNodeType()) {
                    return child.getNodeValue();
                }
            }

            return "";
        }

        // * Attribute nodes: returns the string-value of the node.
        if (Node.ATTRIBUTE_NODE == this.node.getNodeType()) {
            return this.node.getNodeValue();
        }

        // * Text nodes: returns the string-value of the node.
        if (Node.TEXT_NODE == this.node.getNodeType()) {
            return this.node.getNodeValue();
        }

        // * Namespace, processing instruction, comment, and the XPath root node: behavior is undefined.
        return null;
    }

    /**
     * Stores the current model item state.
     */
    public void synchronizeChangeState() {
        // set change flags
        this.wasEnabled = isEnabled();
        this.wasReadonly = isReadonly();
        this.wasRequired = isRequired();
        this.wasValid = isValid();
    }

    /**
     * Registers the specified listener with the associated model item.
     *
     * @param listener the model item listener.
     */
    public void register(ModelItemListener listener) {
        if (this.listeners == null) {
            this.listeners = new ArrayList();
        }

        this.listeners.add(listener);
    }

    /**
     * Deregisters the specified listener with the associated model item.
     *
     * @param listener the model item listener.
     */
    public void deregister(ModelItemListener listener) {
        if (this.listeners != null) {
            this.listeners.remove(listener);
        }
    }

    /**
     * Notifies all registered model item listeners about any computed model item property changes.
     */
    public void notifyListeners() {
        // compute for changes
        boolean enabledChanged = this.wasEnabled != isEnabled();
        boolean readonlyChanged = this.wasReadonly != isReadonly();
        boolean requiredChanged = this.wasRequired != isRequired();
        boolean validChanged = this.wasValid != isValid();

        // synchronize change state
        synchronizeChangeState();

        if (this.listeners != null && this.listeners.size() > 0) {
            if (enabledChanged || readonlyChanged || requiredChanged || validChanged) {
                ModelItemListener listener;
                for (int index = 0; index < this.listeners.size(); index++) {
                    listener = (ModelItemListener) this.listeners.get(index);

                    if (enabledChanged) {
                        listener.enabledChanged(this.wasEnabled);
                    }
                    if (readonlyChanged) {
                        listener.readonlyChanged(this.wasReadonly);
                    }
                    if (requiredChanged) {
                        listener.requiredChanged(this.wasRequired);
                    }
                    if (validChanged) {
                        listener.validChanged(this.wasValid);
                    }
                }
            }
        }
    }


    // implementation of 'org.chiba.xml.xforms.LocalValue'

    /**
     * Sets the local readonly state of the associated model item.
     *
     * @param localReadonly the local readonly state of the associated model item.
     */
    public void setLocalReadonly(boolean localReadonly) {
        this.localReadonly = localReadonly;
    }

    /**
     * Returns the local readonly state of the associated model item.
     *
     * @return the local readonly state of the associated model item.
     */
    public boolean isLocalReadonly() {
        return this.localReadonly;
    }

    /**
     * Sets the local relevant state of the associated model item.
     *
     * @param localRelevant the local relevant state of the associated model item.
     */
    public void setLocalRelevant(boolean localRelevant) {
        this.localRelevant = localRelevant;
    }

    /**
     * Returns the local relevant state of the associated model item.
     *
     * @return the local relevant state of the associated model item.
     */
    public boolean isLocalRelevant() {
        return this.localRelevant;
    }

    /**
     * Sets the local required state of the associated model item.
     *
     * @param localRequired the local required state of the associated model item.
     */
    public void setLocalRequired(boolean localRequired) {
        this.localRequired = localRequired;
    }

    /**
     * Returns the local required state of the associated model item.
     *
     * @return the local required state of the associated model item.
     */
    public boolean isLocalRequired() {
        return this.localRequired;
    }

    /**
     * Sets the constraint valid state of the associated model item.
     *
     * @param constraintValid the constraint valid state of the associated model item.
     */
    public void setConstraintValid(boolean constraintValid) {
        this.constraintValid = constraintValid;
    }

    /**
     * Returns the constraint valid state of the associated model item.
     *
     * @return the constraint valid state of the associated model item.
     */
    public boolean isConstraintValid() {
        return this.constraintValid;
    }

    /**
     * Sets the datatype valid state of the associated model item.
     *
     * @param datatypeValid the datatype valid state of the associated model item.
     */
    public void setDatatypeValid(boolean datatypeValid) {
        this.datatypeValid = datatypeValid;
    }

    /**
     * Returns the datatype valid state of the associated model item.
     *
     * @return the datatype valid state of the associated model item.
     */
    public boolean isDatatypeValid() {
        return this.datatypeValid;
    }

    /**
     * Sets the required valid state of the associated model item.
     *
     * @param requiredValid the required valid state of the associated model item.
     */
    public void setRequiredValid(boolean requiredValid) {
        this.requiredValid = requiredValid;
    }

    /**
     * Returns the required valid state of the associated model item.
     *
     * @return the required valid state of the associated model item.
     */
    public boolean isRequiredValid() {
        return this.requiredValid;
    }

    // file upload fixes, the following methods are needed for form-data
    // serialization ... 
    private String filename = null;
    private String mediatype = null;

    public void setFileName(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setMediatype(String mediatype) {
        this.mediatype = mediatype;
    }

    public String getMediatype() {
        return this.mediatype;
    }
}

// end of class
