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
import org.chiba.xml.xforms.Model;
import org.chiba.xml.xforms.ModelItem;
import org.chiba.xml.xforms.NamespaceCtx;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.constraints.Validator;
import org.chiba.xml.xforms.exception.XFormsBindingException;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Element;

import java.math.BigDecimal;

/**
 * Implementation of XForms Rnage Element.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: Range.java,v 1.12 2004/11/18 15:57:58 unl Exp $
 */
public class Range extends AbstractFormControl {
    private static final Category LOGGER = Category.getInstance(Range.class);

    /**
     * Creates a new Range object.
     *
     * @param element the DOM Element
     * @param model the Model this range belongs to
     */
    public Range(Element element, Model model) {
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
        initializeRange();
        initializeChildren();
        initializeActions();
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
            dispatchValueChangeSequence();
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

    protected final void initializeRange() throws XFormsException {
        if (!isBound()) {
            return;
        }

        // get model item datatype
        Validator validator = this.model.getValidator();
        String datatype = this.dataElement.getDatatype();

        if (validator.isRestricted("decimal", datatype) ||
                validator.isRestricted("float", datatype) ||
                validator.isRestricted("double", datatype)) {
            // get bound value
            String value = getInstanceValue();
            BigDecimal decimalValue;
            if (value != null && value.length() > 0) {
                decimalValue = new BigDecimal(value);
            }
            else {
                // set '0.0' as default value
                decimalValue = new BigDecimal(0d);
            }

            // get step size
            BigDecimal decimalStep;
            if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, STEP_ATTRIBUTE)) {
                decimalStep = new BigDecimal(this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, STEP_ATTRIBUTE));
            }
            else {
                // set '1.0' as default step
                decimalStep = new BigDecimal(1d);
                this.element.setAttributeNS(NamespaceCtx.XFORMS_NS,
                        this.xformsPrefix + ':' + STEP_ATTRIBUTE,
                        decimalStep.toString());
            }

            // get range start
            BigDecimal decimalStart;
            if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, START_ATTRIBUTE)) {
                decimalStart = new BigDecimal(this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, START_ATTRIBUTE));
            }
            else {
                // set 'value - (2 * step)' as default start
                decimalStart = decimalValue.subtract(decimalStep.multiply(new BigDecimal(2d)));
                this.element.setAttributeNS(NamespaceCtx.XFORMS_NS,
                        this.xformsPrefix + ':' + START_ATTRIBUTE,
                        decimalStart.toString());
            }

            // get range end
            BigDecimal decimalEnd;
            if (this.element.hasAttributeNS(NamespaceCtx.XFORMS_NS, END_ATTRIBUTE)) {
                decimalEnd = new BigDecimal(this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, END_ATTRIBUTE));
            }
            else {
                // set 'value + (2 * step)' as default end
                decimalEnd = decimalValue.add(decimalStep.multiply(new BigDecimal(2d)));
                this.element.setAttributeNS(NamespaceCtx.XFORMS_NS,
                        this.xformsPrefix + ':' + END_ATTRIBUTE,
                        decimalEnd.toString());
            }

            if (decimalValue.compareTo(decimalStart) < 0 ||
                    decimalValue.compareTo(decimalEnd) > 0) {
                this.model.getContainer().dispatch(this.target, EventFactory.OUT_OF_RANGE, null);
            }

            return;
        }

        throw new XFormsBindingException("datatype not supported by range control", this.target, datatype);
    }

}

// end of class
