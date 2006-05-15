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
import org.chiba.xml.util.DOMUtil;
import org.chiba.xml.xforms.Model;
import org.chiba.xml.xforms.NamespaceCtx;
import org.chiba.xml.xforms.XFormsConstants;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of <b>9.3.3 The itemset Element</b>.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public class Itemset extends BoundElement {
    private static final Category LOGGER = Category.getInstance(Itemset.class);

    private Element prototype;
    private List items;

    /**
     * Creates a new Itemset object.
     *
     * @param element the DOM Element
     * @param model the Model this itemset belongs to
     */
    public Itemset(Element element, Model model) {
        super(element, model);
    }

    // itemset specific methods

    /**
     * Returns the context size of this itemset.
     * <p/>
     * The context size is the size of the bound nodeset.
     *
     * @return the context size of this itemset.
     */
    public int getContextSize() {
        if (isBound()) {
            return this.model.getInstance(getInstanceId()).countNodeset(getLocationPath());
        }

        return 0;
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
        initializeItemset();
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

        updateItemset();
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
        disposeItemset();
        disposeSelf();
    }

    /**
     * Checks wether this control is a leaf control.
     * <p/>
     * A control is a leaf control if it cannot contain other controls.
     *
     * @return <code>true</code> if this control is a leaf control,
     * <code>false</code> otherwise.
     */
    public boolean isLeaf() {
        return false;
    }

    // binding related methods

    /**
     * Checks wether this element has an ui binding.
     * <p/>
     * This element has an ui binding if it has a <code>nodeset</code>
     * attribute.
     *
     * @return <code>true</code> if this element has an ui binding, otherwise
     *         <code>false</code>.
     */
    public boolean hasUIBinding() {
        return this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.NODESET_ATTRIBUTE);
    }

    /**
     * Returns the binding expression.
     *
     * @return the binding expression.
     */
    public String getBindingExpression() {
        return this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.NODESET_ATTRIBUTE);
    }

    // lifecycle template methods

    /**
     * Initializes the prototype of this itemset.
     * <p/>
     * The itemset prototype is cloned and removed from the document.
     */
    protected void initializePrototype() {
        // create prototype element
        Document document = this.element.getOwnerDocument();
        this.prototype = document.createElementNS(NamespaceCtx.XFORMS_NS, this.xformsPrefix + ":" + XFormsConstants.ITEM);
        this.prototype.setAttributeNS(null, "id", this.container.generateId());

        // clone itemset prototype
        NodeList children = this.element.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            this.prototype.appendChild(children.item(index).cloneNode(true));
        }

        // remove itemset prototype
        DOMUtil.removeAllChildren(this.element);
    }

    /**
     * Initializes this itemset.
     * <p/>
     * For each node in the bound nodeset selector items are created and
     * initialized.
     */
    protected void initializeItemset() throws XFormsException {
        // add prototype to data element
        this.dataElement.getElement().appendChild(this.prototype);

        // initialize positional items
        int count = getContextSize();
        this.items = new ArrayList(count);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " init: initializing " + count + " selector item(s)");
        }
        for (int position = 1; position < count + 1; position++) {
            this.items.add(initializeSelectorItem(position));
        }
    }

    /**
     * Updates this itemset.
     * <p/>
     * Selector items are initialized or disposed according to the bound
     * nodeset.
     */
    protected void updateItemset() throws XFormsException {
        // check nodeset count
        int count = getContextSize();
        int size = this.items.size();

        if (count < size) {
            // remove obsolete selector items
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " update: disposing " + (size - count) + " selector item(s)");
            }
            for (int position = size; position > count; position--) {
                disposeSelectorItem((Item) this.items.remove(position - 1));
            }
        }

        if (count > size) {
            // add missing selector items
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " update: initializing " + (count - size) + " selector item(s)");
            }
            for (int position = size + 1; position <= count; position++) {
                this.items.add(initializeSelectorItem(position));
            }
        }
    }

    /**
     * Disposes this itemset.
     * <p/>
     * The list of selector items and the itemset prototype are freed.
     */
    protected void disposeItemset() {
        // free selector items and prototype
        this.items.clear();
        this.items = null;
        this.prototype = null;
    }

    /**
     * Returns the logger object.
     *
     * @return the logger object.
     */
    protected Category getLogger() {
        return LOGGER;
    }

    // helper

    private Item initializeSelectorItem(int position) throws XFormsException {
        // detect reference node
        Node before = DOMUtil.findNthChildNS(this.element, NamespaceCtx.XFORMS_NS, XFormsConstants.ITEM, position);
        if (before == null) {
            before = DOMUtil.findFirstChildNS(this.element, NamespaceCtx.CHIBA_NS, "data");
        }

        // create selector item
        Element element = (Element) this.prototype.cloneNode(true);
        this.element.insertBefore(element, before);

        // dispatch chiba event
        String info = this.originalId != null ? this.originalId : this.id;
        this.container.dispatch(this.target, EventFactory.PROTOTYPE_CLONED, info);

        // initialize selector item
        Item item = (Item) this.container.getElementFactory().createXFormsElement(element, this.model);
        item.setItemset(this);
        item.setPosition(position);
        item.setGeneratedId(this.container.generateId());
        item.register();
        item.init();

        // dispatch chiba event
        this.container.dispatch(this.target, EventFactory.ITEM_INSERTED, String.valueOf(position));
        return item;
    }

    private void disposeSelectorItem(Item item) throws XFormsException {
        // dispose selector item
        Element element = item.getElement();
        int position = item.getPosition();
        item.dispose();
        this.element.removeChild(element);

        // dispatch chiba event
        this.container.dispatch(this.target, EventFactory.ITEM_DELETED, String.valueOf(position));
    }

}

// end of class
