/*
 *
 *	Artistic License
 *
 *	Preamble
 *
 *	The intent of this document is to state the conditions under which a
 *	Package may be copied, such that the Copyright Holder maintains some
 *	semblance of artistic control over the development of the package,
 *	while giving the users of the package the right to use and distribute
 *	the Package in a more-or-less customary fashion, plus the right to make
 *	reasonable modifications.
 *
 *	Definitions:
 *
 *	"Package" refers to the collection of files distributed by the
 *	Copyright Holder, and derivatives of that collection of files created
 *	through textual modification.
 *
 *	"Standard Version" refers to such a Package if it has not been
 *	modified, or has been modified in accordance with the wishes of the
 *	Copyright Holder.
 *
 *	"Copyright Holder" is whoever is named in the copyright or copyrights
 *	for the package.
 *
 *	"You" is you, if you're thinking about copying or distributing this Package.
 *
 *	"Reasonable copying fee" is whatever you can justify on the basis of
 *	media cost, duplication charges, time of people involved, and so
 *	on. (You will not be required to justify it to the Copyright Holder,
 *	but only to the computing community at large as a market that must bear
 *	the fee.)
 *
 *	"Freely Available" means that no fee is charged for the item itself,
 *	though there may be fees involved in handling the item. It also means
 *	that recipients of the item may redistribute it under the same
 *	conditions they received it.
 *
 *	1. You may make and give away verbatim copies of the source form of the
 *	Standard Version of this Package without restriction, provided that you
 *	duplicate all of the original copyright notices and associated
 *	disclaimers.
 *
 *	2. You may apply bug fixes, portability fixes and other modifications
 *	derived from the Public Domain or from the Copyright Holder. A Package
 *	modified in such a way shall still be considered the Standard Version.
 *
 *	3. You may otherwise modify your copy of this Package in any way,
 *	provided that you insert a prominent notice in each changed file
 *	stating how and when you changed that file, and provided that you do at
 *	least ONE of the following:
 *
 *		a) place your modifications in the Public Domain or otherwise make
 *		them Freely Available, such as by posting said modifications to
 *		Usenet or an equivalent medium, or placing the modifications on a
 *		major archive site such as ftp.uu.net, or by allowing the Copyright
 *		Holder to include your modifications in the Standard Version of the
 *		Package.
 *
 *		b) use the modified Package only within your corporation or
 *		organization.
 *
 *		c) rename any non-standard executables so the names do not conflict
 *		with standard executables, which must also be provided, and provide
 *		a separate manual page for each non-standard executable that
 *		clearly documents how it differs from the Standard Version.
 *
 *		d) make other distribution arrangements with the Copyright Holder.
 *
 *	4. You may distribute the programs of this Package in object code or
 *	executable form, provided that you do at least ONE of the following:
 *
 *		a) distribute a Standard Version of the executables and library
 *		files, together with instructions (in the manual page or
 *		equivalent) on where to get the Standard Version.
 *
 *		b) accompany the distribution with the machine-readable source of
 *		the Package with your modifications.
 *
 *		c) accompany any non-standard executables with their corresponding
 *		Standard Version executables, giving the non-standard executables
 *		non-standard names, and clearly documenting the differences in
 *		manual pages (or equivalent), together with instructions on where
 *		to get the Standard Version.
 *
 *		d) make other distribution arrangements with the Copyright Holder.
 *
 *	5. You may charge a reasonable copying fee for any distribution of this
 *	Package. You may charge any fee you choose for support of this
 *	Package. You may not charge a fee for this Package itself.  However,
 *	you may distribute this Package in aggregate with other (possibly
 *	commercial) programs as part of a larger (possibly commercial) software
 *	distribution provided that you do not advertise this Package as a
 *	product of your own.
 *
 *	6. The scripts and library files supplied as input to or produced as
 *	output from the programs of this Package do not automatically fall
 *	under the copyright of this Package, but belong to whomever generated
 *	them, and may be sold commercially, and may be aggregated with this
 *	Package.
 *
 *	7. C or perl subroutines supplied by you and linked into this Package
 *	shall not be considered part of this Package.
 *
 *	8. The name of the Copyright Holder may not be used to endorse or
 *	promote products derived from this software without specific prior
 *	written permission.
 *
 *	9. THIS PACKAGE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR IMPLIED
 *	WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
 *	MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package org.chiba.xml.xforms.exception;

import org.w3c.dom.events.EventTarget;

// todo: Maybe this class should be called XFormsException and the class named
// XFormsException today should be called ChibaException ? Or should this
// class and XFormsException melt together ?

/**
 * Base class for xforms error indications.
 * <p/>
 * Error indications are the events defined in [4.5 Error Indications]
 * as well as [4.4.19 The xforms-submit-error Event].
 * <p/>
 * Exceptions of this type are intended to be used in internal processing
 * only. They are designed to transport an error indication relevant for
 * event handling from its origin to the top level event handling or default
 * action routine. Once the corresponding error indications has been dispatched
 * as an event into the DOM, the exception has to be marked <code>handled</code>.
 * Then, the expception can be rethrown, but the event target and the context
 * information are not available anymore. This is both to ensure the event is only
 * dispatched once and to release the associated event target.
 *
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public abstract class XFormsErrorIndication extends XFormsException {

    /**
     * The exception state.
     */
    private boolean handled = false;

    /**
     * The event target.
     */
    private EventTarget target = null;

    /**
     * The context information.
     */
    private Object info = null;

    /**
     * Creates a new xforms error indication.
     *
     * @param message the error message.
     * @param cause   the root cause.
     * @param target  the event target.
     * @param info    the context information.
     */
    public XFormsErrorIndication(String message, Exception cause, EventTarget target, Object info) {
        super(message, cause);
        this.target = target;
        this.info = info;
    }

    /**
     * Specifies wether this error indication is fatal or non-fatal.
     *
     * @return <code>true</code> if this error indication is fatal,
     *         otherwise <code>false</code>.
     */
    public abstract boolean isFatal();

    /**
     * Checks wether this error indication is handled or not.
     *
     * @return <code>true</code> if this error indication is handled,
     *         otherwise <code>false</code>.
     */
    public final boolean isHandled() {
        return this.handled;
    }

    /**
     * Sets the error indication state to <code>handled</code>.
     */
    public final void setHandled() {
        this.handled = true;
        this.target = null;
        this.info = null;
    }

    /**
     * Returns the event type of this error indication.
     *
     * @return the event type of this error indication.
     */
    public final String getEventType() {
        return this.id;
    }

    /**
     * Returns the event target of this error indication.
     *
     * @return the event target of this error indication.
     */
    public final EventTarget getEventTarget() {
        return this.target;
    }

    /**
     * Returns the context information of this error indication.
     *
     * @return the context information of this error indication.
     */
    public final Object getContextInfo() {
        return this.info;
    }

}

//end of class
