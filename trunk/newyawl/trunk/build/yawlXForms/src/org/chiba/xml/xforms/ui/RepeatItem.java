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

import org.apache.log4j.Category;
import org.chiba.xml.xforms.Binding;
import org.chiba.xml.xforms.Initializer;
import org.chiba.xml.xforms.Model;
import org.chiba.xml.xforms.NamespaceCtx;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Element;

/**
 * Helper class to wrap a single repeat item. In the internal DOM of the processor xforms:group Elements are
 * created for every repeat item. This element has an additional chiba:transient attribute which signals a stylesheet
 * writer, that this group was not part of the original form and can be ignored for rendering. Adding these transient
 * groups helps to wrap mixed markup in the input document and also simplifies writing UI transformations for repeated
 * data.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: RepeatItem.java,v 1.3 2004/08/15 14:14:16 joernt Exp $
 */
public class RepeatItem extends Group {
    private static final Category LOGGER = Category.getInstance(RepeatItem.class);

    private int position;
    private Repeat repeat;

    /**
     * Creates a new repeat item.
     *
     * @param element the DOM Element
     * @param model   the model this repeat item belongs to.
     */
    public RepeatItem(Element element, Model model) {
        super(element, model);
    }

    // implementation of 'org.chiba.xml.xforms.Binding'

    /**
     * Returns the binding expression.
     *
     * @return the binding expression.
     */
    public String getBindingExpression() {
        // filter the enclosing repeat
        return this.repeat.getBindingExpression() + "[" + getPosition() + "]";
    }

    /**
     * Returns the id of the binding element.
     *
     * @return the id of the binding element.
     */
    public String getBindingId() {
        // filter the enclosing repeat
        return this.repeat.getBindingId();
    }

    /**
     * Returns the enclosing element.
     *
     * @return the enclosing element.
     */
    public Binding getEnclosingBinding() {
        // filter the enclosing repeat
        return this.repeat.getEnclosingBinding();
    }

    /**
     * Returns the location path.
     *
     * @return the location path.
     */
    public String getLocationPath() {
        // filter the enclosing repeat
        return this.repeat.getLocationPath() + "[" + getPosition() + "]";
    }

    /**
     * Returns the model id of the binding element.
     *
     * @return the model id of the binding element.
     */
    public String getModelId() {
        // filter the enclosing repeat
        return this.repeat.getModelId();
    }

    // repeat entry specific methods

    /**
     * Returns the repeat entry position.
     *
     * @return the repeat entry position.
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * Sets the repeat entry position.
     *
     * @param position the repeat entry position.
     */
    public void setPosition(int position) {
        this.position = position;

        // todo: really needed ?
        this.element.setAttributeNS(NamespaceCtx.CHIBA_NS, NamespaceCtx.CHIBA_PREFIX + ":position", String.valueOf(position));
    }

    /**
     * Returns the owning repeat.
     *
     * @return the owning repeat.
     */
    public Repeat getRepeat() {
        return this.repeat;
    }

    /**
     * Sets the owning repeat.
     *
     * @param repeat the owning repeat.
     */
    public void setRepeat(Repeat repeat) {
        this.repeat = repeat;
    }

    /**
     * Checks wether this repeat item is selected.
     *
     * @return <code>true</code> if this repeat item is selected,
     *         <code>false</code> otherwise.
     */
    public boolean isSelected() {
        boolean selected = this.repeat.getIndex() == this.position;

        if (this.repeat.isRepeated()) {
            // check enclosing repeat item
            RepeatItem repeatItem = (RepeatItem) this.container.lookup(this.repeat.getRepeatItemId());
            selected = selected && repeatItem.isSelected();
        }

        return selected;
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

        this.element.setAttributeNS(NamespaceCtx.CHIBA_NS, NamespaceCtx.CHIBA_PREFIX + ":selected", String.valueOf(isSelected()));

        Initializer.initializeUIElements(this.model, this.element, this.id);
        Initializer.initializeActionElements(this.model, this.element, this.id);
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

        this.element.setAttributeNS(NamespaceCtx.CHIBA_NS, NamespaceCtx.CHIBA_PREFIX + ":selected", String.valueOf(isSelected()));

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

        this.repeat = null;
        this.position = 0;
    }

    // standard methods

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object.
     */
    public String toString() {
        return "[" + this.element.getNodeName() + "/repeatitem id='" + getId() + "']";
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
