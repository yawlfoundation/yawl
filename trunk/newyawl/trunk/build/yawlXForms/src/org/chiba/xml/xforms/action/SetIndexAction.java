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
package org.chiba.xml.xforms.action;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.log4j.Category;
import org.chiba.xml.xforms.Instance;
import org.chiba.xml.xforms.Model;
import org.chiba.xml.xforms.NamespaceCtx;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.xml.xforms.ui.Repeat;
import org.chiba.xml.xforms.ui.RepeatItem;
import org.chiba.xml.xforms.xpath.PathUtil;
import org.w3c.dom.Element;

/**
 * Implements the action as defined in <code>9.3.7 The setindex Element</code>.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: SetIndexAction.java,v 1.26 2004/11/12 00:13:24 unl Exp $
 */
public class SetIndexAction extends AbstractAction {
    private static Category LOGGER = Category.getInstance(SetIndexAction.class);
    private String indexAttribute;
    private String repeatAttribute;

    /**
     * Creates a new SetIndexAction object.
     *
     * @param element the DOM Element
     * @param model the context Model
     */
    public SetIndexAction(Element element, Model model) {
        super(element, model);
    }

    // lifecycle methods

    /**
     * Performs element init.
     *
     * @throws XFormsException if any error occurred during init.
     */
    public void init() throws XFormsException {
        super.init();
        this.repeatAttribute = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, REPEAT_ATTRIBUTE);
        this.indexAttribute = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, INDEX_ATTRIBUTE);
    }


    // implementation of 'org.chiba.xml.xforms.action.XFormsAction'

    /**
     * Performs the <code>setindex</code> action.
     *
     * @return always <code>true</code>.
     * @throws XFormsException if an error occurred during <code>setindex</code>
     * processing.
     */
    public boolean perform() throws XFormsException {
        // lookup repeat element
        Repeat repeat = (Repeat) this.container.lookup(this.repeatAttribute);

        // todo: check wether this interpretation is correct/useful and write a test for it
        String locationPath;
        if (isRepeated()) {
            // use the enclosing repeat as context
            RepeatItem repeatItem = (RepeatItem) getContainerObject().lookup(getRepeatItemId());
            locationPath = repeatItem.getLocationPath() + "[" + this.indexAttribute + "]";
        }
        else {
            // use the target repeat as context
            locationPath = repeat.getLocationPath() + "[" + this.indexAttribute + "]";
        }

        // get instance
        Instance instance = repeat.getModel().getInstance(repeat.getInstanceId());
        JXPathContext context = instance.getInstanceContext();

        int index;
        if (instance.existsNode(locationPath)) {
            // compute index from position
            String instancePath = context.getPointer(locationPath).asPath();
            index = PathUtil.stepIndex(PathUtil.lastStep(instancePath));
        }
        else {
            // compute index w/o context
            index = ((Integer) context.getValue(this.indexAttribute, java.lang.Integer.class)).intValue();
        }

        // check boundaries
        if (index < 1) {
            repeat.setIndex(1);
            this.container.dispatch(repeat.getTarget(), EventFactory.SCROLL_FIRST, null);
        }
        else if (index > repeat.getContextSize()) {
            index = repeat.getContextSize();
            repeat.setIndex(index);
            this.container.dispatch(repeat.getTarget(), EventFactory.SCROLL_LAST, null);
        }
        else {
            // set repeat index
            repeat.setIndex(index);
        }

        // update behaviour (defined by chiba ;-)
        setDeferredRefresh(repeat.getModel().getId(), true);

        // always indicate success
        return true;
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
