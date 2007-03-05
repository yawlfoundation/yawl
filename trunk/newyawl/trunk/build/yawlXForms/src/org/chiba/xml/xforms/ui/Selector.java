/*
 *
 *    Artistic License
 *
 *    Preamble
 *
 *    The intent of this document is to state the conditions under which a
 *    Package may be copied, such that the Copyright Holder maintains some
 *    semblance of artistic control over the development of the package,
 *    while giving the users of the package the right to use and distribute
 *    the Package in a more-or-less customary fashion, plus the right to make
 *    reasonable modifications.
 *
 *    Definitions:
 *
 *    "Package" refers to the collection of files distributed by the
 *    Copyright Holder, and derivatives of that collection of files created
 *    through textual modification.
 *
 *    "Standard Version" refers to such a Package if it has not been
 *    modified, or has been modified in accordance with the wishes of the
 *    Copyright Holder.
 *
 *    "Copyright Holder" is whoever is named in the copyright or copyrights
 *    for the package.
 *
 *    "You" is you, if you're thinking about copying or distributing this Package.
 *
 *    "Reasonable copying fee" is whatever you can justify on the basis of
 *    media cost, duplication charges, time of people involved, and so
 *    on. (You will not be required to justify it to the Copyright Holder,
 *    but only to the computing community at large as a market that must bear
 *    the fee.)
 *
 *    "Freely Available" means that no fee is charged for the item itself,
 *    though there may be fees involved in handling the item. It also means
 *    that recipients of the item may redistribute it under the same
 *    conditions they received it.
 *
 *    1. You may make and give away verbatim copies of the source form of the
 *    Standard Version of this Package without restriction, provided that you
 *    duplicate all of the original copyright notices and associated
 *    disclaimers.
 *
 *    2. You may apply bug fixes, portability fixes and other modifications
 *    derived from the Public Domain or from the Copyright Holder. A Package
 *    modified in such a way shall still be considered the Standard Version.
 *
 *    3. You may otherwise modify your copy of this Package in any way,
 *    provided that you insert a prominent notice in each changed file
 *    stating how and when you changed that file, and provided that you do at
 *    least ONE of the following:
 *
 *        a) place your modifications in the Public Domain or otherwise make
 *        them Freely Available, such as by posting said modifications to
 *        Usenet or an equivalent medium, or placing the modifications on a
 *        major archive site such as ftp.uu.net, or by allowing the Copyright
 *        Holder to include your modifications in the Standard Version of the
 *        Package.
 *
 *        b) use the modified Package only within your corporation or
 *        organization.
 *
 *        c) rename any non-standard executables so the names do not conflict
 *        with standard executables, which must also be provided, and provide
 *        a separate manual page for each non-standard executable that
 *        clearly documents how it differs from the Standard Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    4. You may distribute the programs of this Package in object code or
 *    executable form, provided that you do at least ONE of the following:
 *
 *        a) distribute a Standard Version of the executables and library
 *        files, together with instructions (in the manual page or
 *        equivalent) on where to get the Standard Version.
 *
 *        b) accompany the distribution with the machine-readable source of
 *        the Package with your modifications.
 *
 *        c) accompany any non-standard executables with their corresponding
 *        Standard Version executables, giving the non-standard executables
 *        non-standard names, and clearly documenting the differences in
 *        manual pages (or equivalent), together with instructions on where
 *        to get the Standard Version.
 *
 *        d) make other distribution arrangements with the Copyright Holder.
 *
 *    5. You may charge a reasonable copying fee for any distribution of this
 *    Package. You may charge any fee you choose for support of this
 *    Package. You may not charge a fee for this Package itself.  However,
 *    you may distribute this Package in aggregate with other (possibly
 *    commercial) programs as part of a larger (possibly commercial) software
 *    distribution provided that you do not advertise this Package as a
 *    product of your own.
 *
 *    6. The scripts and library files supplied as input to or produced as
 *    output from the programs of this Package do not automatically fall
 *    under the copyright of this Package, but belong to whomever generated
 *    them, and may be sold commercially, and may be aggregated with this
 *    Package.
 *
 *    7. C or perl subroutines supplied by you and linked into this Package
 *    shall not be considered part of this Package.
 *
 *    8. The name of the Copyright Holder may not be used to endorse or
 *    promote products derived from this software without specific prior
 *    written permission.
 *
 *    9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 *    WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 *    MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package org.chiba.xml.xforms.ui;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.log4j.Category;
import org.chiba.xml.xforms.Model;
import org.chiba.xml.xforms.NamespaceCtx;
import org.chiba.xml.xforms.XFormsConstants;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * Implementation of both <b>8.1.10 The select Element</b> and
 * <b>8.1.11 The select1 Element</b>.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: Selector.java,v 1.3 2004/12/17 01:43:11 unl Exp $
 */
public class Selector extends AbstractFormControl {
    private static final Category LOGGER = Category.getInstance(Selector.class);

    private boolean multiple;
    private JXPathContext context;

    /**
     * Creates a new selector.
     *
     * @param element the DOM Element of this selector.
     * @param model the Model this select1 is bound to.
     */
    public Selector(Element element, Model model) {
        super(element, model);
    }

    /**
     * Checks wether this is a multiple selector or not.
     *
     * @return <code>true</code> if this is a multiple selector, otherwise
     * <code>false</code>.
     */
    public boolean isMultiple () {
        return this.multiple;
    }

    /**
     * Specifies the behaviour of this selector.
     *
     * @param multiple specifies wether to allow multiple selections.
     */
    public void setMultiple (boolean multiple) {
        this.multiple = multiple;
    }

    /**
     * Sets the value of this form control.
     * <p/>
     * The bound instance data is updated and the event sequence for this
     * control is executed. Event sequences are described in Chapter 4.6 of
     * XForms 1.0 Recommendation.
     *
     * @param value the value to be set.
     */
    public void setValue(String value) throws XFormsException {
        if (isBound()) {
            this.model.getInstance(getInstanceId()).setNodeValue(getLocationPath(), value);
            dispatchSelectionWithoutValueChange();
            dispatchValueChangeSequence();
        }
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
        initializeSelection();
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

        updateDataElement();
        updateChildren();
        updateSelection();
    }

    // lifecycle template methods

    /**
     * Initializes all items' selection state.
     * <p/>
     * The selection state of all items is set according to the bound instance
     * value of this select.
     */
    protected void initializeSelection() throws XFormsException {
        if (isBound()) {
            setSelection(true, false);
        }
    }

    /**
     * Updates all items' selection state.
     * <p/>
     * The selection state of any items which is not set according to the bound
     * instance value of this select is updated.
     */
    protected void updateSelection() throws XFormsException {
        if (isBound()) {
            setSelection(false, false);
        }
    }

    /**
     * Updates all items' selection state and dispatches the appropriate
     * <code>xforms-select</code> and <code>xforms-deselect events</code>.
     * <p/>
     * The selection state of any items which is not set according to the bound
     * instance value of this select is updated.
     */
    protected void dispatchSelectionWithoutValueChange() throws XFormsException {
        if (isBound()) {
            setSelection(false, true);
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

    // helper

    private void setSelection (boolean force, boolean dispatch) throws XFormsException {
        if (this.context == null ){
            this.context = JXPathContext.newContext(this.element);
        }

        Item item;
        String itemId;
        String value = getValue();
        boolean selectable = true;

        Iterator iterator = this.context.iterate("//" + this.xformsPrefix + ":" +  XFormsConstants.ITEM + "[not(ancestor::" + NamespaceCtx.CHIBA_PREFIX + ":data)]/@id");
        while (iterator.hasNext()) {
            itemId = iterator.next().toString();
            item = (Item) this.container.lookup(itemId);

            if (selectable && isInRange(value, item.getValue())) {
                if (force || !item.isSelected()) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug(this + " selecting item " + itemId);
                    }

                    item.select();

                    if (dispatch) {
                        this.container.dispatch(item.getTarget(), EventFactory.SELECT, null);
                    }
                }

                // allow only one first selection for non-multiple selectors
                selectable = isMultiple();
            }
            else {
                if (force || item.isSelected()) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug(this + " deselecting item " + itemId);
                    }

                    item.deselect();

                    if (dispatch) {
                        this.container.dispatch(item.getTarget(), EventFactory.DESELECT, null);
                    }
                }
            }
        }
    }

    private boolean isInRange (String boundValue, String itemValue) {
        if (boundValue == null || itemValue == null) {
            return false;
        }

        if (boundValue == itemValue) {
            return true;
        }

        if (isMultiple()) {
            String bound = " " + boundValue + " ";
            String item = " " + itemValue + " ";
            return bound.indexOf(item) > -1;
        }

        return boundValue.equals(itemValue);
    }
}

// end of class
