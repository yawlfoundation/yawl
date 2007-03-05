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
import org.chiba.xml.xforms.xpath.PathUtil;
import org.w3c.dom.Element;

/**
 * Implements the action as defined in <code>9.3.5 The insert Element</code>.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: InsertAction.java,v 1.30 2004/11/12 00:13:23 unl Exp $
 */
public class InsertAction extends AbstractBoundAction {
    private static Category LOGGER = Category.getInstance(InsertAction.class);
    private String atAttribute;
    private String positionAttribute;

    /**
     * Creates a new InsertAction object.
     *
     * @param element the DOM Element
     * @param model the context Model
     */
    public InsertAction(Element element, Model model) {
        super(element, model);
    }

    /**
     * Performs element init.
     */

    public void init() throws XFormsException {
        super.init();
        this.atAttribute = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, AT_ATTRIBUTE);
        this.positionAttribute = this.element.getAttributeNS(NamespaceCtx.XFORMS_NS, POSITION_ATTRIBUTE);
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

    // implementation of 'org.chiba.xml.xforms.action.XFormsAction'

    /**
     * Performs the <code>insert</code> action.
     *
     * @return always <code>true</code>.
     * @throws org.chiba.xml.xforms.exception.XFormsException if an error
     * occurred during <code>insert</code> processing.
     */
    public boolean perform() throws XFormsException {
        // get instance data
        String instanceId = getInstanceId(getLocationPath());
        Instance instance = this.model.getInstance(instanceId);

        // get path information
        String locationPath = stripInstanceFunction(getLocationPath());
        int contextSize = instance.countNodeset(locationPath);

        // set defaults
        String insertPath = locationPath;
        int insertPosition = 1;

        // check for existing path
        if (contextSize > 0) {
            // canonicalize path
            JXPathContext context = instance.getInstanceContext();
            context.setLenient(true);
            String instancePath = context.getPointer(locationPath + "[round(" + this.atAttribute + ")]").asPath();
            context.setLenient(false);

            if (instancePath.equals("null()")) {
                // caused by NaN
                insertPath = PathUtil.stripLastPredicate(context.getPointer(locationPath).asPath());
                insertPosition = contextSize + 1;
            }
            else {
                // strip down to insert path and position
                insertPath = PathUtil.stripLastPredicate(instancePath);
                contextSize = instance.countNodeset(insertPath);
                String lastStep = PathUtil.lastStep(instancePath);
                insertPosition = PathUtil.stepIndex(lastStep);

                // sanity checks (see 9.3.5)
                if (insertPosition < 1) {
                    insertPosition = 1;
                }
                if (insertPosition > contextSize) {
                    insertPosition = contextSize;
                }

                // check position specifier
                if (this.positionAttribute.equals("after")) {
                    insertPosition++;
                }
            }
        }

        // insert specified node and dispatch notification event
        instance.insertNode(insertPath, insertPosition);
        this.container.dispatch(instance.getTarget(), EventFactory.INSERT, insertPath + "[" + insertPosition + "]");

        // update behaviour
        setDeferredRebuild(this.model.getId(), true);
        setDeferredRecalculate(this.model.getId(), true);
        setDeferredRevalidate(this.model.getId(), true);
        setDeferredRefresh(this.model.getId(), true);

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
