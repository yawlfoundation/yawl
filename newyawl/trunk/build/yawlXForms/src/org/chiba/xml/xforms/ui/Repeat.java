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

import org.apache.log4j.Category;
import org.apache.xerces.dom.ElementImpl;
import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.*;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.events.XFormsEvent;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.xpath.PathUtil;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of XForms Repeat element.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: Repeat.java,v 1.48 2004/12/17 21:00:45 unl Exp $
 */
public class Repeat extends BoundElement implements EventListener {
    private static final Category LOGGER = Category.getInstance(Repeat.class);

    private int index;
    private String canonicalPath;
    private Element prototype;
    private List items;

    /**
     * Creates a new Repeat object.
     *
     * @param element the DOM Element
     * @param model the Model this repeat belongs to
     */
    public Repeat(Element element, Model model) {
        super(element, model);
    }

    // bound element methods

    /**
     * Checks wether this control is a leaf control.
     *
     * @return <code>false</code>.
     */
    public boolean isLeaf() {
        return false;
    }

    // event handling methods

    /**
     * This method is called whenever an event occurs of the type for which the
     * <code>EventListener</code> interface was registered.
     *
     * @param event The <code>Event</code> contains contextual information about
     * the event. It also contains the <code>stopPropagation</code> and
     * <code>preventDefault</code> methods which are used in determining the
     * event's flow and default action.
     */
    public void handleEvent(Event event) {
        String eventType = event.getType();
        if (EventFactory.NODE_INSERTED.equals(eventType) || EventFactory.NODE_DELETED.equals(eventType)) {
            try {
                // get event context
                String context = ((XFormsEvent) event).getContextInfo().toString();
                String path = PathUtil.stripLastPredicate(context);

                if (path.equals(getCanonicalPath())) {
                    int position = PathUtil.stepIndex(PathUtil.lastStep(context));
                    if (EventFactory.NODE_INSERTED.equals(eventType)) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug(this + " insert: position " + position);
                        }

                        // insert repeat item
                        this.items.add(position - 1, initializeRepeatItem(position));

                        // update position of following items
                        for (int index = position; index < this.items.size(); index++) {
                            ((RepeatItem) this.items.get(index)).setPosition(index + 1);
                        }

                        // set index to inserted item
                        setIndex(position);
                    }
                    else {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug(this + " delete: position " + position);
                        }

                        // delete repeat item
                        disposeRepeatItem((RepeatItem) this.items.remove(position - 1));

                        // update position of following items
                        for (int index = position - 1; index < this.items.size(); index++) {
                            ((RepeatItem) this.items.get(index)).setPosition(index + 1);
                        }

                        // set index only if it was pointing to the deleted item
                        // and the deleted item was the last collection member
                        int size = getContextSize();
                        if (getIndex() > size) {
                            setIndex(size);
                        }
                    }
                }
            }
            catch (Exception e) {
                // handle exception, prevent default action and stop event propagation
                handleException(e);
                event.preventDefault();
                event.stopPropagation();
            }
        }
    }

    // repeat specific methods

    /**
     * Returns the repeat index.
     *
     * @return the repeat index.
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Sets the index of this <code>repeat</code>.
     *
     * @param index the index of this <code>repeat</code>.
     */
    public void setIndex(int index) throws XFormsException {
        if (this.index != index) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " set index: " + index);
            }

            this.index = index;

            // todo: really needed ?
            this.element.setAttributeNS(NamespaceCtx.CHIBA_NS, NamespaceCtx.CHIBA_PREFIX + ":index", String.valueOf(index));

            // dispatch chiba event
            this.container.dispatch(this.target, EventFactory.INDEX_CHANGED, String.valueOf(this.index));
        }

        if (isRepeated()) {
            // set enclosing index
            RepeatItem repeatItem = (RepeatItem) this.container.lookup(getRepeatItemId());
            repeatItem.getRepeat().setIndex(repeatItem.getPosition());
        }
        else {
            // register repeat item under original id
            registerRepeatItem(index);
        }
    }

    /**
     * Returns the context size of this repeat.
     * <p/>
     * The context size is the size of the bound nodeset.
     *
     * @return the context size of this repeat.
     */
    public int getContextSize() {
        if (isBound()) {
            return this.model.getInstance(getInstanceId()).countNodeset(getLocationPath());
        }

        return 0;
    }

    /**
     * Returns the location path in canonical form.
     *
     * @return the location path in canonical form.
     */
    public String getCanonicalPath() {
        if (isRepeated()) {
            return computeCanonicalPath();
        }

        if (this.canonicalPath == null) {
            this.canonicalPath = computeCanonicalPath();
        }

        return this.canonicalPath;
    }

    /**
     * Returns the specified repeat item.
     *
     * @param position the repeat item position.
     * @return the specified repeat item or <code>null</code> if there is no
     * such position.
     */
    public RepeatItem getRepeatItem(int position) {
        if (position > 0 && position <= this.items.size()) {
            return (RepeatItem) this.items.get(position - 1);
        }

        return null;
    }

    // binding related methods

    /**
     * Checks wether this element has a model binding.
     * <p/>
     * This element has a model binding if it has a <code>bind</code> or a
     * <code>repeat-bind</code> attribute.
     *
     * @return <code>true</code> if this element has a model binding, otherwise
     * <code>false</code>.
     */
    public boolean hasModelBinding() {
        return super.hasModelBinding() || hasRepeatBindAttribute();
    }

    /**
     * Checks wether this element has an ui binding.
     * <p/>
     * This element has an ui binding if it has a <code>nodeset</code> or a
     * <code>repeat-nodeset</code> attribute.
     *
     * @return <code>true</code> if this element has an ui binding, otherwise
     * <code>false</code>.
     */
    public boolean hasUIBinding() {
        return hasNodesetAttribute() || hasRepeatNodesetAttribute();
    }

    /**
     * Returns the model binding of this element.
     *
     * @return the model binding of this element.
     */
    public Bind getModelBinding() {
        if (hasRepeatBindAttribute()) {
            String bindId = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.REPEAT_BIND_ATTRIBUTE);

            return (Bind) this.container.lookup(bindId);
        }

        return super.getModelBinding();
    }

    /**
     * Returns the binding expression.
     *
     * @return the binding expression.
     */
    public String getBindingExpression() {
        if (hasNodesetAttribute()) {
            return this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.NODESET_ATTRIBUTE);
        }

        if (hasRepeatNodesetAttribute()) {
            return this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.REPEAT_NODESET_ATTRIBUTE);
        }

        return null;
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
        initializePrototype();
        initializeDataElement();
        initializeRepeat();
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

        updateRepeat();
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
        disposeRepeat();
        disposeSelf();
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
     * Initializes this repeat.
     * <p/>
     * The repeat prototype is cloned and removed from the document.
     */
    protected void initializePrototype() throws XFormsException {
        // create prototype element
        Document document = this.element.getOwnerDocument();
        this.prototype = document.createElementNS(NamespaceCtx.XFORMS_NS, this.xformsPrefix + ":" + XFormsConstants.GROUP);
        this.prototype.setAttributeNS(null, "id", this.container.generateId());
        this.prototype.setAttributeNS(NamespaceCtx.CHIBA_NS, NamespaceCtx.CHIBA_PREFIX + ":transient", String.valueOf(true));
        this.prototype.setAttributeNS(NamespaceCtx.CHIBA_NS, NamespaceCtx.CHIBA_PREFIX + ":position", String.valueOf(0));

        // clone repeat prototype
        NodeList children = this.element.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            this.prototype.appendChild(children.item(index).cloneNode(true));
        }

        // remove repeat prototype
        DOMUtil.removeAllChildren(this.element);
    }

    /**
     * Initializes this repeat.
     * <p/>
     * The repeat is registered with the instance as event listener. For each
     * node in the bound nodeset repeat items are created and initialized. The
     * repeat index is set to 1, unless the bound nodeset is empty. The repeat
     * is registered with the instance as event listener.
     */
    protected final void initializeRepeat() throws XFormsException {
        // add prototype to data element
        this.dataElement.getElement().appendChild(this.prototype);

        // register repeat as event listener *before* items are initialized
        Instance instance = this.model.getInstance(getInstanceId());
        instance.getTarget().addEventListener(EventFactory.NODE_INSERTED, this, false);
        instance.getTarget().addEventListener(EventFactory.NODE_DELETED, this, false);

        // initialize repeat items
        int count = getContextSize();
        this.items = new ArrayList(count);

        // pretend index during item initialization, since it's needed to detect
        // the item selection state (todo: should be refactored some day)
        this.index = 1;

        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " init: initializing " + count + " repeat item(s)");
        }
        for (int position = 1; position < count + 1; position++) {
            this.items.add(initializeRepeatItem(position));
        }

        // remove pretended index and set index *after* item initialization
        this.index = 0;
        setIndex(count > 0 ? 1 : 0);
    }

    /**
     * Updates this repeat.
     * <p/>
     * Repeat items are initialized or disposed according to the bound
     * nodeset.
     */
    protected final void updateRepeat() throws XFormsException {
        int size = getContextSize();
        int items = this.element.getChildNodes().getLength() - 1;

        if (size < items) {
            // remove obsolete repeat items
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " update: disposing " + (items - size) + " repeat item(s)");
            }
            for (int position = items; position > size; position--) {
                disposeRepeatItem((RepeatItem) this.items.remove(position - 1));
            }

            if (getIndex() > size) {
                // set index to last
                setIndex(size);
            }
        }

        if (size > items) {
            // add missing repeat items
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " update: initializing " + (size - items) + " repeat item(s)");
            }
            for (int position = items + 1; position <= size; position++) {
                this.items.add(initializeRepeatItem(position));
            }

            if (getIndex() == 0) {
                // set index to first
                setIndex(1);
            }
        }
    }

    /**
     * Disposes this repeat.
     * <p/>
     * The repeat is deregistered as event listener. The list of repeat items
     * and the repeat prototype are freed.
     */
    protected final void disposeRepeat() throws XFormsException {
        // deregister repeat as event listener
        Instance instance = this.model.getInstance(getInstanceId());
        instance.getTarget().removeEventListener(EventFactory.NODE_INSERTED, this, false);
        instance.getTarget().removeEventListener(EventFactory.NODE_DELETED, this, false);

        // free repeat items and prototype
        this.items.clear();
        this.items = null;
        this.prototype = null;
    }

    // helper methods

    private RepeatItem initializeRepeatItem(int position) throws XFormsException {
        // detect reference node
        Node before = DOMUtil.findNthChildNS(this.element, NamespaceCtx.XFORMS_NS, XFormsConstants.GROUP, position);
        if (before == null) {
            before = DOMUtil.findFirstChildNS(this.element, NamespaceCtx.CHIBA_NS, "data");
        }

        // create repeat item
        Element group = (Element) this.prototype.cloneNode(true);
        this.element.insertBefore(group, before);

        // dispatch chiba event
        String info = this.originalId != null ? this.originalId : this.id;
        this.container.dispatch(this.target, EventFactory.PROTOTYPE_CLONED, info);

        // initialize repeat item
        RepeatItem repeatItem = (RepeatItem) this.container.getElementFactory().createXFormsElement(group, getModel());
        repeatItem.setRepeat(this);
        repeatItem.setPosition(position);
        repeatItem.setGeneratedId(this.container.generateId());
        repeatItem.register();
        repeatItem.init();

        // dispatch chiba event
        this.container.dispatch(this.target, EventFactory.ITEM_INSERTED, String.valueOf(position));
        return repeatItem;
    }

    private void disposeRepeatItem(RepeatItem repeatItem) throws XFormsException {
        // dispose repeat item
        Element element = repeatItem.getElement();
        int position = repeatItem.getPosition();
        repeatItem.dispose();
        this.element.removeChild(element);

        // dispatch chiba event
        this.container.dispatch(this.target, EventFactory.ITEM_DELETED, String.valueOf(position));
    }

    private void registerRepeatItem(int position) {
        RepeatItem repeatItem = getRepeatItem(position);
        if (repeatItem != null) {
            repeatItem.register();
            registerChildren(repeatItem.getElement());
        }
    }

    private void registerChildren(Node parent) {
        NodeList childNodes = parent.getChildNodes();

        for (int index = 0; index < childNodes.getLength(); index++) {
            Node node = childNodes.item(index);

            if (node instanceof ElementImpl) {
                ElementImpl elementImpl = (ElementImpl) node;
                XFormsElement xFormsElement = (XFormsElement) elementImpl.getUserData();

                if (xFormsElement != null) {
                    // register current (action or ui) element
                    xFormsElement.register();

                    if (xFormsElement instanceof Repeat) {
                        // register *selected* repeat item only, if any
                        Repeat repeat = (Repeat) xFormsElement;
                        RepeatItem repeatItem = repeat.getRepeatItem(repeat.getIndex());

                        if (repeatItem != null) {
                            repeatItem.register();
                            registerChildren(repeatItem.getElement());
                        }
                    }
                    else {
                        // register *all* children
                        registerChildren(xFormsElement.getElement());
                    }
                }
            }
        }
    }

    private String computeCanonicalPath() {
        Instance instance = this.model.getInstance(getInstanceId());
        String locationPath = getLocationPath();

        if (instance.existsNode(locationPath)) {
            return PathUtil.stripLastPredicate(instance.getPointer(locationPath).asPath());
        }

        int index = locationPath.lastIndexOf('/');
        String parent = locationPath.substring(0, index);
        String child = locationPath.substring(index);

        return instance.getPointer(parent).asPath() + child;
    }

    /**
     * Checks wether this element has a <code>nodeset</code> attribute.
     *
     * @return <code>true</code> if this element has a <code>nodeset</code>
     *         attribute, otherwise <code>false</code>.
     */
    private boolean hasNodesetAttribute() {
        return this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.NODESET_ATTRIBUTE);
    }

    /**
     * Checks wether this element has a <code>repeat-bind</code> attribute.
     *
     * @return <code>true</code> if this element has a <code>repeat-bind</code>
     *         attribute, otherwise <code>false</code>.
     */
    private boolean hasRepeatBindAttribute() {
        return this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.REPEAT_BIND_ATTRIBUTE);
    }

    /**
     * Checks wether this element has a <code>repeat-nodeset</code> attribute.
     *
     * @return <code>true</code> if this element has a <code>repeat-nodeset</code>
     *         attribute, otherwise <code>false</code>.
     */
    private boolean hasRepeatNodesetAttribute() {
        return this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.REPEAT_NODESET_ATTRIBUTE);
    }

}

// end of class
