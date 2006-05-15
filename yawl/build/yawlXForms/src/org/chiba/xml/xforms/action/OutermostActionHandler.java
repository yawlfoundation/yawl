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
package org.chiba.xml.xforms.action;

import org.apache.log4j.Category;
import org.chiba.xml.xforms.Model;
import org.chiba.xml.xforms.Container;
import org.chiba.xml.xforms.events.EventFactory;
import org.chiba.xml.xforms.exception.XFormsException;

import java.util.List;
import java.util.ArrayList;

/**
 * An outermost action handler component. It implements deferred update
 * behaviour in terms of <code>10.1.1 The action Element</code>.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public class OutermostActionHandler {
    private static Category LOGGER = Category.getInstance(OutermostActionHandler.class);

    private Model model;
    private boolean running;
    private boolean rebuild;
    private boolean recalculate;
    private boolean revalidate;
    private boolean refresh;

    /**
     * Creates a new outermost action handler.
     *
     * @param model the model to work with.
     */
    public OutermostActionHandler(Model model) {
        this.model = model;
        this.running = false;
    }

    // member access

    /**
     * Checks wether this outermost action handler is running.
     *
     * @return <code>true</code> if this outermost action handler is running,
     * <code>false</code> otherwise.
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * Checks wether this outermost action handler will rebuild its model
     * upon termination.
     *
     * @return <code>true</code> if this outermost action handler is rebuilding,
     * <code>false</code> otherwise.
     */
    public boolean isRebuilding() {
        return this.rebuild;
    }

    /**
     * Checks wether this outermost action handler will recalculate its model
     * upon termination.
     *
     * @return <code>true</code> if this outermost action handler is
     * recalculating, <code>false</code> otherwise.
     */
    public boolean isRecalculating() {
        return this.recalculate;
    }

    /**
     * Checks wether this outermost action handler will revalidate its model
     * upon termination.
     *
     * @return <code>true</code> if this outermost action handler is
     * revalidating, <code>false</code> otherwise.
     */
    public boolean isRevalidating() {
        return this.revalidate;
    }

    /**
     * Checks wether this outermost action handler will refresh its model
     * upon termination.
     *
     * @return <code>true</code> if this outermost action handler is
     * refreshing, <code>false</code> otherwise.
     */
    public boolean isRefreshing() {
        return this.refresh;
    }

    /**
     * Sets the deferred rebuild flag.
     *
     * @param rebuild the deferred rebuild flag.
     */
    public void setRebuild(boolean rebuild) {
        this.rebuild = rebuild;
    }

    /**
     * Sets the deferred recalculate flag.
     *
     * @param recalculate the deferred recalculate flag.
     */
    public void setRecalculate(boolean recalculate) {
        this.recalculate = recalculate;
    }

    /**
     * Sets the deferred revalidate flag.
     *
     * @param revalidate the deferred revalidate flag.
     */
    public void setRevalidate(boolean revalidate) {
        this.revalidate = revalidate;
    }

    /**
     * Sets the deferred refresh flag.
     *
     * @param refresh the deferred refresh flag.
     */
    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    // behaviour control

    /**
     * Starts this outermost action handler.
     * <p/>
     * All deferred flags are set to <code>false</code>. The handler is
     * running.
     *
     * @throws XFormsException if this outermost action handler is already
     * running.
     */
    public void start() throws XFormsException {
        if (this.running) {
            throw new XFormsException("outermost action handler already running");
        }

        this.rebuild = false;
        this.recalculate = false;
        this.revalidate = false;
        this.refresh = false;

        this.running = true;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("started at " + this.model);
        }
    }

    /**
     * Terminates this outermost action handler.
     * <p/>
     * For any deferred flag set to <code>true</code> the corresponding events
     * are dispatched. The handler stops.
     *
     * @throws XFormsException if this outermost action handler is not running
     * yet or any error occurred during a model action.
     */
    public void terminate() throws XFormsException {
        if (!this.running) {
            throw new XFormsException("outermost action handler not running yet");
        }

        boolean deferred = this.rebuild || this.recalculate || this.revalidate || this.refresh;
        if (LOGGER.isDebugEnabled() && deferred) {
            LOGGER.debug("performing deferred updates for " + this.model);
        }

        Container container = this.model.getContainer();
        if (this.rebuild) {
            container.dispatch(this.model.getTarget(), EventFactory.REBUILD, null);
        }
        if (this.recalculate) {
            container.dispatch(this.model.getTarget(), EventFactory.RECALCULATE, null);
        }
        if (this.revalidate) {
            container.dispatch(this.model.getTarget(), EventFactory.REVALIDATE, null);
        }
        if (this.refresh) {
            container.dispatch(this.model.getTarget(), EventFactory.REFRESH, null);
        }

        this.running = false;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("terminated at " + this.model);
        }
    }

    // convenience helper

    /**
     * Starts all outermost action handlers not yet running.
     *
     * @param models a list of models for which the handlers should be started.
     * @return a list of models for which the handlers has been started by this
     * method.
     * @throws XFormsException if a handler could not be started.
     */
    public static List startHandlers(List models) throws XFormsException {
        List started = new ArrayList();
        Model model = null;
        OutermostActionHandler handler = null;

        for (int index = 0; index < models.size(); index++) {
            model = (Model) models.get(index);
            handler = model.getOutermostActionHandler();

            if (! handler.isRunning()) {
                handler.start();
                started.add(model);
            }
        }

        return started;
    }

    /**
     * Terminates all outermost action handlers.
     *
     * @param models a list of models for which the handlers should be
     * terminated.
     * @throws XFormsException if a handler could not be terminated.
     */
    public static void terminateHandlers(List models) throws XFormsException {
        Model model = null;
        OutermostActionHandler handler = null;

        for (int index = 0; index < models.size(); index++) {
            model = (Model) models.get(index);
            handler = model.getOutermostActionHandler();
            handler.terminate();
        }
    }
}

// end of class
