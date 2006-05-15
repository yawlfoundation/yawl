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
import org.chiba.xml.xforms.*;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.util.DOMUtil;
import org.w3c.dom.Element;

/**
 * Implementation of <b>8.2.2 The item Element</b>.
 * <p/>
 * When a position and an itemset element is provided, this element behaves as a
 * positional item inserting positional information during binding resolution
 * (just like a repeat item).
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public class Item extends AbstractUIElement implements Binding {
    private static final Category LOGGER = Category.getInstance(Item.class);

    private int position;
    private Itemset itemset;

    /**
     * Creates a new Item object.
     *
     * @param element the DOM Element
     * @param model the Model this item belongs to
     */
    public Item(Element element, Model model) {
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

        if (!this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.SELECTED_ATTRIBUTE)) {
            this.element.setAttributeNS(NamespaceCtx.XFORMS_NS,
                    this.xformsPrefix + ":" + XFormsConstants.SELECTED_ATTRIBUTE,
                    String.valueOf(false));
        }

        if (this.itemset != null) {
            Initializer.initializeUIElements(this.model, this.element, this.id);
            Initializer.initializeActionElements(this.model, this.element, this.id);
            return;
        }

        initializeChildren();
        initializeActions();
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
        disposeSelf();
    }

    // implementation of 'org.chiba.xml.xforms.Binding'

    /**
     * Returns the binding expression.
     *
     * @return the binding expression.
     */
    public String getBindingExpression() {
        if (this.itemset != null) {
            return this.itemset.getBindingExpression() + "[" + getPosition() + "]";
        }

        return null;
    }

    /**
     * Returns the id of the binding element.
     *
     * @return the id of the binding element.
     */
    public String getBindingId() {
        if (this.itemset != null) {
            return this.itemset.getBindingId();
        }

        return null;
    }

    /**
     * Returns the enclosing element.
     *
     * @return the enclosing element.
     */
    public Binding getEnclosingBinding() {
        if (this.itemset != null) {
            return this.itemset.getEnclosingBinding();
        }

        return null;
    }

    /**
     * Returns the location path.
     *
     * @return the location path.
     */
    public String getLocationPath() {
        if (this.itemset != null) {
            return this.itemset.getLocationPath() + "[" + getPosition() + "]";
        }

        return null;
    }

    /**
     * Returns the model id of the binding element.
     *
     * @return the model id of the binding element.
     */
    public String getModelId() {
        if (this.itemset != null) {
            return this.itemset.getModelId();
        }

        return null;
    }

    // item specific methods

    /**
     * Checks wether this item is selected or not.
     *
     * @return <code>true</code> if this item is selected, otherwise
     * <code>false</code>.
     */
    public boolean isSelected() {
        return this.element
                .getAttributeNS(NamespaceCtx.XFORMS_NS, XFormsConstants.SELECTED_ATTRIBUTE)
                .equals("true");
    }

    /**
     * Selects this item.
     */
    public void select() {
        this.element.setAttributeNS(NamespaceCtx.XFORMS_NS,
                this.xformsPrefix + ":" + XFormsConstants.SELECTED_ATTRIBUTE,
                String.valueOf(true));

        if (this.itemset != null) {
            // todo: handle copy ?
        }
    }

    /**
     * Deselects this item.
     */
    public void deselect() {
        this.element.setAttributeNS(NamespaceCtx.XFORMS_NS,
                this.xformsPrefix + ":" + XFormsConstants.SELECTED_ATTRIBUTE,
                String.valueOf(false));

        if (this.itemset != null) {
            // todo: handle copy ?
        }
    }

    /**
     * Returns the current value of a value element or <code>null</code> if
     * there is no value element.
     *
     * @return the current value of a value element or <code>null</code> if
     * there is no value element.
     */
    public String getValue() {
        Element value = DOMUtil.findFirstChildNS(this.element,
                NamespaceCtx.XFORMS_NS,
                XFormsConstants.VALUE);

        if (value != null) {
            return DOMUtil.getTextNodeAsString(value);
        }

        // todo: handle copy ?
        return null;
    }

    /**
     * Returns the position of this item.
     *
     * @return the position of this item.
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * Sets the position of this item.
     *
     * @param position the position of this item.
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Returns the itemset this item belongs to.
     *
     * @return the itemset this item belongs to.
     */
    public Itemset getItemset() {
        return this.itemset;
    }

    /**
     * Sets the itemset this item belongs to.
     *
     * @param itemset the itemset this item belongs to.
     */
    public void setItemset(Itemset itemset) {
        this.itemset = itemset;
    }

    /**
     * Returns the logger object.
     *
     * @return the logger object.
     */
    protected Category getLogger() {
        return LOGGER;
    }

}

// end of class
