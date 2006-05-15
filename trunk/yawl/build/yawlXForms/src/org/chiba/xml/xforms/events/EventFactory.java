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
package org.chiba.xml.xforms.events;

import org.apache.log4j.Category;
import org.w3c.dom.DOMException;
import org.w3c.dom.events.Event;

/**
 * XFormsEventFactory
 *
 * @author Joern Turner
 * @author Ulrich Nicolas Liss&eacute;
 * @version $Id$
 */
public class EventFactory {
    private static final Category LOGGER = Category.getInstance(EventFactory.class);

    // initialization events
    /**
     * XForms init Event constant
     */
    public static final String MODEL_CONSTRUCT = "xforms-model-construct";

    /**
     * XForms init Event constant
     */
    public static final String MODEL_CONSTRUCT_DONE = "xforms-model-construct-done";

    /**
     * XForms init Event constant
     */
    public static final String READY = "xforms-ready";

    /**
     * XForms init Event constant
     */
    public static final String MODEL_DESTRUCT = "xforms-model-destruct";

    // interaction events
    /**
     * XForms interaction Event constant
     */
    public static final String PREVIOUS = "xforms-previous";

    /**
     * XForms interaction Event constant
     */
    public static final String NEXT = "xforms-next";

    /**
     * XForms interaction Event constant
     */
    public static final String FOCUS = "xforms-focus";

    /**
     * XForms interaction Event constant
     */
    public static final String HELP = "xforms-help";

    /**
     * XForms interaction Event constant
     */
    public static final String HINT = "xforms-hint";

    /**
     * XForms interaction Event constant
     */
    public static final String REBUILD = "xforms-rebuild";

    /**
     * XForms interaction Event constant
     */
    public static final String REFRESH = "xforms-refresh";

    /**
     * XForms interaction Event constant
     */
    public static final String REVALIDATE = "xforms-revalidate";

    /**
     * XForms interaction Event constant
     */
    public static final String RECALCULATE = "xforms-recalculate";

    /**
     * XForms interaction Event constant
     */
    public static final String RESET = "xforms-reset";

    /**
     * XForms interaction Event constant
     */
    public static final String SUBMIT = "xforms-submit";

    // notification events
    /**
     * XForms notification Event constant
     */
    public static final String DOM_ACTIVATE = "DOMActivate";

    /**
     * XForms notification Event constant
     */
    public static final String VALUE_CHANGED = "xforms-value-changed";

    /**
     * XForms notification Event constant
     */
    public static final String SELECT = "xforms-select";

    /**
     * XForms notification Event constant
     */
    public static final String DESELECT = "xforms-deselect";

    /**
     * XForms notification Event constant
     */
    public static final String SCROLL_FIRST = "xforms-scroll-first";

    /**
     * XForms notification Event constant
     */
    public static final String SCROLL_LAST = "xforms-scroll-last";

    /**
     * XForms notification Event constant
     */
    public static final String INSERT = "xforms-insert";

    /**
     * XForms notification Event constant
     */
    public static final String DELETE = "xforms-delete";

    /**
     * XForms notification Event constant
     */
    public static final String VALID = "xforms-valid";

    /**
     * XForms notification Event constant
     */
    public static final String INVALID = "xforms-invalid";

    /**
     * XForms notification Event constant
     */
    public static final String DOM_FOCUS_IN = "DOMFocusIn";

    /**
     * XForms notification Event constant
     */
    public static final String DOM_FOCUS_OUT = "DOMFocusOut";

    /**
     * XForms notification Event constant
     */
    public static final String READONLY = "xforms-readonly";

    /**
     * XForms notification Event constant
     */
    public static final String READWRITE = "xforms-readwrite";

    /**
     * XForms notification Event constant
     */
    public static final String REQUIRED = "xforms-required";

    /**
     * XForms notification Event constant
     */
    public static final String OPTIONAL = "xforms-optional";

    /**
     * XForms notification Event constant
     */
    public static final String ENABLED = "xforms-enabled";

    /**
     * XForms notification Event constant
     */
    public static final String DISABLED = "xforms-disabled";

    /**
     * XForms notification Event constant
     */
    public static final String IN_RANGE = "xforms-in-range";

    /**
     * XForms notification Event constant
     */
    public static final String OUT_OF_RANGE = "xforms-out-of-range";

    /**
     * XForms notification Event constant
     */
    public static final String SUBMIT_DONE = "xforms-submit-done";

    /**
     * XForms notification Event constant
     */
    public static final String SUBMIT_ERROR = "xforms-submit-error";

    // error indications

    /**
     * XForms error indication Event constant
     */
    public static final String BINDING_EXCEPTION = "xforms-binding-exception";

    /**
     * XForms error indication Event constant
     */
    public static final String LINK_EXCEPTION = "xforms-link-exception";

    /**
     * XForms error indication Event constant
     */
    public static final String LINK_ERROR = "xforms-link-error";

    /**
     * XForms error indication Event constant
     */
    public static final String COMPUTE_EXCEPTION = "xforms-compute-exception";

    // Chiba events

    /**
     * Chiba notification event.
     */
    public static final String NODE_INSERTED = "chiba-node-inserted";

    /**
     * Chiba notification event.
     */
    public static final String NODE_DELETED = "chiba-node-deleted";

    /**
     * Chiba notification event.
     */
    public static final String PROTOTYPE_CLONED = "chiba-prototype-cloned";

    /**
     * Chiba notification event.
     */
    public static final String ID_GENERATED = "chiba-id-generated";

    /**
     * Chiba notification event.
     */
    public static final String ITEM_INSERTED = "chiba-item-inserted";

    /**
     * Chiba notification event.
     */
    public static final String ITEM_DELETED = "chiba-item-deleted";

    /**
     * Chiba notification event.
     */
    public static final String INDEX_CHANGED = "chiba-index-changed";

    /**
     * Chiba notification event.
     */
    public static final String MESSAGE = "chiba-message";

    /**
     * Creates a new event factory.
     */
    public EventFactory() {
        // NOP
    }

    // implementation of 'org.w3c.dom.events.DocumentEvent'

    /**
     * Returns a new event of the specified type.
     *
     * @param type specifies the event type.
     * @return a new event of the specified type.
     * @throws DOMException if the event type is not supported
     * (<code>DOMException.NOT_SUPPORTED_ERR</code>).
     */
    public static Event createEvent(String type) throws DOMException {
        return createXFormsEvent(type, null);
    }

    /**
     * Returns a new event of the specified type.
     *
     * @param type specifies the event type.
     * @param info optionally specifies contextual information.
     * @return a new event of the specified type.
     * @throws org.w3c.dom.DOMException if the event type is not supported
     * (<code>DOMException.NOT_SUPPORTED_ERR</code>).
     */
    public static Event createEvent(String type, Object info) throws DOMException {
        Event event = createXFormsEvent(type, info);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("create event: type: " + event.getType() + ", bubbles: " + event.getBubbles() + ", cancelable: " + event.getCancelable());
        }

        return event;
    }

    /**
     * Returns a new and initialized XForms event of the specified type.
     *
     * @param type specifies the event type.
     * @param info optionally specifies contextual information.
     * @return a new XForms event of the specified type.
     */
    private static XFormsEvent createXFormsEvent(String type, Object info) {
        XFormsEvent event = new XFormsEventImpl();

        // initialization events
        if (MODEL_CONSTRUCT.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (MODEL_CONSTRUCT_DONE.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (READY.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (MODEL_DESTRUCT.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }

        // interaction events
        if (PREVIOUS.equals(type)) {
            event.initXFormsEvent(type, false, true, null);
            return event;
        }
        if (NEXT.equals(type)) {
            event.initXFormsEvent(type, false, true, null);
            return event;
        }
        if (FOCUS.equals(type)) {
            event.initXFormsEvent(type, false, true, null);
            return event;
        }
        if (HELP.equals(type)) {
            event.initXFormsEvent(type, true, true, null);
            return event;
        }
        if (HINT.equals(type)) {
            event.initXFormsEvent(type, true, true, null);
            return event;
        }
        if (REBUILD.equals(type)) {
            event.initXFormsEvent(type, true, true, null);
            return event;
        }
        if (REFRESH.equals(type)) {
            event.initXFormsEvent(type, true, true, null);
            return event;
        }
        if (REVALIDATE.equals(type)) {
            event.initXFormsEvent(type, true, true, null);
            return event;
        }
        if (RECALCULATE.equals(type)) {
            event.initXFormsEvent(type, true, true, null);
            return event;
        }
        if (RESET.equals(type)) {
            event.initXFormsEvent(type, true, true, null);
            return event;
        }
        if (SUBMIT.equals(type)) {
            event.initXFormsEvent(type, true, true, null);
            return event;
        }

        // notification events
        if (DOM_ACTIVATE.equals(type)) {
            event.initXFormsEvent(type, true, true, null);
            return event;
        }
        if (VALUE_CHANGED.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (SELECT.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (DESELECT.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (SCROLL_FIRST.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (SCROLL_LAST.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (INSERT.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }
        if (DELETE.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }
        if (VALID.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (INVALID.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (DOM_FOCUS_IN.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (DOM_FOCUS_OUT.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (READONLY.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (READWRITE.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (REQUIRED.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (OPTIONAL.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (ENABLED.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (DISABLED.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (IN_RANGE.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (OUT_OF_RANGE.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (SUBMIT_DONE.equals(type)) {
            event.initXFormsEvent(type, true, false, null);
            return event;
        }
        if (SUBMIT_ERROR.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }

        // error indications
        if (BINDING_EXCEPTION.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }
        if (LINK_EXCEPTION.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }
        if (LINK_ERROR.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }
        if (COMPUTE_EXCEPTION.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }

        // chiba events
        if (NODE_INSERTED.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }
        if (NODE_DELETED.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }
        if (PROTOTYPE_CLONED.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }
        if (ID_GENERATED.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }
        if (ITEM_INSERTED.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }
        if (ITEM_DELETED.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }
        if (INDEX_CHANGED.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }
        if (MESSAGE.equals(type)) {
            event.initXFormsEvent(type, true, false, info);
            return event;
        }

        return event;
    }
}

//end of class

