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

import org.chiba.xml.xforms.Model;
import org.chiba.xml.xforms.NamespaceCtx;
import org.chiba.xml.xforms.XFormsElement;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.events.XMLEventsConstants;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

/**
 * Base class for all action implementations.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id: AbstractAction.java,v 1.29 2004/11/30 13:00:43 unl Exp $
 */
public abstract class AbstractAction extends XFormsElement implements EventListener, XFormsAction {
    /**
     * The event type by which this action is triggered.
     */
    protected String eventType;

    /**
     * The id of the containing repeat item, if any.
     */
    protected String repeatItemId;

    /**
     * Creates a new AbstractAction object.
     *
     * @param element the DOM Element
     * @param model the context Model
     */
    public AbstractAction(Element element, Model model) {
        super(element, model);
    }

    // repeat stuff

    /**
     * Sets the id of the repeat item this element is contained in.
     *
     * @param repeatItemId the id of the repeat item this element is contained
     * in.
     */
    public void setRepeatItemId(String repeatItemId) throws XFormsException {
        this.repeatItemId = repeatItemId;
    }

    /**
     * Returns the id of the repeat item this element is contained in.
     *
     * @return the id of the repeat item this element is contained in.
     */
    public String getRepeatItemId() {
        return this.repeatItemId;
    }

    /**
     * Checks wether this element is repeated.
     *
     * @return <code>true</code> if this element is contained in a repeat item,
     * <code>false</code> otherwise.
     */
    public boolean isRepeated() {
        return this.repeatItemId != null;
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
        try {
            if (event.getType().equals(this.eventType)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug(this + " handle event " + this.eventType);
                }

                perform();
            }
        }
        catch (Exception e) {
            // handle exception, prevent default action and stop event propagation
            handleException(e);
            event.preventDefault();
            event.stopPropagation();
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

        initializeAction();
    }

    // lifecycle template methods

    /**
     * Initializes this action.
     */
    protected final void initializeAction() {
        if (!(getParentObject() instanceof XFormsAction)) {
            String event = this.element.getAttributeNS(NamespaceCtx.XMLEVENTS_NS, XMLEventsConstants.EVENT_ATTRIBUTE);
            if (event.length() == 0) {
                // default if nothing other is specified
                event = EventFactory.DOM_ACTIVATE;
            }

            this.eventType = event;

            // todo: implement event stuff like observer, target, ...
            XFormsElement parent = getParentObject();
            parent.getTarget().addEventListener(this.eventType, this, false);

            if (getLogger().isDebugEnabled()) {
                getLogger().debug(this + " init: added handler for " + this.eventType + " to " + parent);
            }
        }
    }

    /**
     * Disposes this action.
     */
    protected final void disposeAction() {
        if (!(getParentObject() instanceof XFormsAction)) {
            XFormsElement parent = getParentObject();
            parent.getTarget().removeEventListener(this.eventType, this, false);
        }
    }

    /**
     * Performs a deferred rebuild.
     * <p/>
     * If the outermost action handler is running on the specified model, the
     * rebuild will be deferred until its termination. Otherwise the rebuild
     * takes place immediately.
     *
     * @param modelId the id of the model to rebuild.
     * @param rebuild the state of deferred rebuild behaviour.
     */
    protected final void setDeferredRebuild(String modelId, boolean rebuild) throws XFormsException {
        Model rebuildModel = this.container.getModel(modelId);
        OutermostActionHandler outermostActionHandler = rebuildModel.getOutermostActionHandler();
        if (outermostActionHandler.isRunning()) {
            outermostActionHandler.setRebuild(rebuild);
            return;
        }

        if (rebuild) {
            this.container.dispatch(rebuildModel.getTarget(), EventFactory.REBUILD, null);
        }
    }

    /**
     * Performs a deferred recalculate.
     * <p/>
     * If the outermost action handler is running on the specified model, the
     * recalculate will be deferred until its termination. Otherwise the recalculate
     * takes place immediately.
     *
     * @param modelId the id of the model to recalculate.
     * @param recalculate the state of deferred recalculate behaviour.
     */
    protected final void setDeferredRecalculate(String modelId, boolean recalculate) throws XFormsException {
        Model recalculateModel = this.container.getModel(modelId);
        OutermostActionHandler outermostActionHandler = recalculateModel.getOutermostActionHandler();
        if (outermostActionHandler.isRunning()) {
            outermostActionHandler.setRecalculate(recalculate);
            return;
        }

        if (recalculate) {
            this.container.dispatch(recalculateModel.getTarget(), EventFactory.RECALCULATE, null);
        }
    }

    /**
     * Performs a deferred revalidate.
     * <p/>
     * If the outermost action handler is running on the specified model, the
     * revalidate will be deferred until its termination. Otherwise the revalidate
     * takes place immediately.
     *
     * @param modelId the id of the model to revalidate.
     * @param revalidate the state of deferred revalidate behaviour.
     */
    protected final void setDeferredRevalidate(String modelId, boolean revalidate) throws XFormsException {
        Model revalidateModel = this.container.getModel(modelId);
        OutermostActionHandler outermostActionHandler = revalidateModel.getOutermostActionHandler();
        if (outermostActionHandler.isRunning()) {
            outermostActionHandler.setRevalidate(revalidate);
            return;
        }

        if (revalidate) {
            this.container.dispatch(revalidateModel.getTarget(), EventFactory.REVALIDATE, null);
        }
    }

    /**
     * Performs a deferred refresh.
     * <p/>
     * If the outermost action handler is running on the specified model, the
     * refresh will be deferred until its termination. Otherwise the refresh
     * takes place immediately.
     *
     * @param modelId the id of the model to refresh.
     * @param refresh the state of deferred refresh behaviour.
     */
    protected final void setDeferredRefresh(String modelId, boolean refresh) throws XFormsException {
        Model refreshModel = this.container.getModel(modelId);
        OutermostActionHandler outermostActionHandler = refreshModel.getOutermostActionHandler();
        if (outermostActionHandler.isRunning()) {
            outermostActionHandler.setRefresh(refresh);
            return;
        }

        if (refresh) {
            this.container.dispatch(refreshModel.getTarget(), EventFactory.REFRESH, null);
        }
    }

}

// end of class
