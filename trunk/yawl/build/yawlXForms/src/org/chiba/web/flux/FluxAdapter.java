// Copyright 2005 Chibacon
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
package org.chiba.web.flux;

import org.apache.log4j.Logger;
import org.chiba.web.WebAdapter;
import org.chiba.web.servlet.ChibaServlet;
import org.chiba.xml.events.ChibaEventNames;
import org.chiba.xml.events.DOMEventNames;
import org.chiba.xml.events.XFormsEventNames;
import org.chiba.xml.events.XMLEvent;
import org.chiba.xml.xforms.exception.XFormsException;
import org.chiba.adapter.ChibaEvent;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for processing DWR calls and building appropriate responses. This
 * class is not exposed through DWR. Instead a Facadeclass 'FluxFacade' will be
 * exposed that only allows to use the dispatch method. All other methods will
 * be hidden for security.
 *
 * @author Joern Turner
 * @version $Id: FluxAdapter.java,v 1.3 2006/09/27 15:14:39 joernt Exp $
 */
//public class FluxAdapter extends AbstractChibaAdapter implements EventListener {
public class FluxAdapter extends WebAdapter {
    private static final Logger LOGGER = Logger.getLogger(FluxAdapter.class);
    private EventLog eventLog;


    public FluxAdapter() {
        super();
        this.eventLog = new EventLog();
    }

    public EventLog getEventLog() {
        return this.eventLog;
    }

    /**
     * initialize the Adapter. This is necessary cause often the using
     * application will need to configure the Adapter before actually using it.
     *
     * @throws org.chiba.xml.xforms.exception.XFormsException
     *
     */
    public void init() throws XFormsException {
        super.init();

        // processor has already been shutdown
        if (checkForExitEvent() != null) {
            return;
        }

        // register for notification events
        this.root.addEventListener(ChibaEventNames.STATE_CHANGED, this, true);
        this.root.addEventListener(ChibaEventNames.PROTOTYPE_CLONED, this, true);
        this.root.addEventListener(ChibaEventNames.ID_GENERATED, this, true);
        this.root.addEventListener(ChibaEventNames.ITEM_INSERTED, this, true);
        this.root.addEventListener(ChibaEventNames.ITEM_DELETED, this, true);
        this.root.addEventListener(ChibaEventNames.INDEX_CHANGED, this, true);
        this.root.addEventListener(ChibaEventNames.SWITCH_TOGGLED, this, true);
        this.root.addEventListener(XFormsEventNames.SUBMIT_ERROR, this, true);
        this.root.addEventListener(ChibaEventNames.SCRIPT_ACTION, this, true);
    }

    /**
     * Dispatch a ChibaEvent to trigger some XForms processing such as updating
     * of values or execution of triggers.
     *
     * @param event an application specific event
     * @throws org.chiba.xml.xforms.exception.XFormsException
     *
     * @see org.chiba.adapter.DefaultChibaEventImpl
     */
    public void dispatch(ChibaEvent event) throws XFormsException {
        super.dispatch(event);

        this.eventLog.flush();
        String targetId = event.getId();

        if (event.getEventName().equalsIgnoreCase(FluxFacade.FLUX_ACTIVATE_EVENT)) {
            chibaBean.dispatch(targetId, DOMEventNames.ACTIVATE);
        } else if (event.getEventName().equalsIgnoreCase("SETINDEX")) {
            int index = Integer.parseInt((String) event.getContextInfo());
            this.chibaBean.updateRepeatIndex(targetId, index);
        } else if (event.getEventName().equalsIgnoreCase("SETVALUE")) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Event contextinfo: " + event.getContextInfo());
            }

            this.chibaBean.updateControlValue(targetId, (String) event.getContextInfo());
        } else if (event.getEventName().equalsIgnoreCase("http-request")) {
            HttpServletRequest request = (HttpServletRequest) event.getContextInfo();
            getHttpRequestHandler().handleRequest(request);
        } else {
            throw new XFormsException("Unknown or illegal event type");
        }
    }

    /**
     * listen to processor and add a DefaultChibaEventImpl object to the
     * EventQueue.
     *
     * @param event the handled DOMEvent
     */
    public void handleEvent(Event event) {
        super.handleEvent(event);
        try {
            if (event instanceof XMLEvent) {
                XMLEvent xmlEvent = (XMLEvent) event;
                String type = xmlEvent.getType();
                if (ChibaEventNames.REPLACE_ALL.equals(type)) {
                    // get event context and store it in session
                    Map submissionResponse = new HashMap();
                    submissionResponse.put("header", xmlEvent.getContextInfo("header"));
                    submissionResponse.put("body", xmlEvent.getContextInfo("body"));
                    this.xformsSession.setProperty(ChibaServlet.CHIBA_SUBMISSION_RESPONSE,submissionResponse);

                    // get event properties
                    Element target = (Element) event.getTarget();
                    String targetId = target.getAttributeNS(null, "id");
                    String targetName = target.getLocalName();

                    // add event properties to log
                    this.eventLog.add(type, targetId, targetName);

                    this.exitEvent = xmlEvent;
                    shutdown();
                    return;
                } else if (ChibaEventNames.LOAD_URI.equals(type)) {
                    // get event properties
                    String show = (String) xmlEvent.getContextInfo("show");

                    // add event to log
                    this.eventLog.add(xmlEvent);
                    if ("replace".equals(show)) {
                        this.exitEvent = xmlEvent;
                        shutdown();
                        this.xformsSession.getManager().deleteXFormsSession(this.xformsSession.getKey());
                    }

                    return;
                }

                // add event to log
                this.eventLog.add(xmlEvent);
            }
        }
        catch (Exception e) {
            this.chibaBean.getContainer().handleEventException(e);
        }
    }

    /**
     * terminates the XForms processing. right place to do cleanup of
     * resources.
     *
     * @throws org.chiba.xml.xforms.exception.XFormsException
     *
     */
    public void shutdown() throws XFormsException {
        // deregister for notification events
        this.root.removeEventListener(ChibaEventNames.STATE_CHANGED, this, true);
        this.root.removeEventListener(ChibaEventNames.PROTOTYPE_CLONED, this, true);
        this.root.removeEventListener(ChibaEventNames.ID_GENERATED, this, true);
        this.root.removeEventListener(ChibaEventNames.ITEM_INSERTED, this, true);
        this.root.removeEventListener(ChibaEventNames.ITEM_DELETED, this, true);
        this.root.removeEventListener(ChibaEventNames.INDEX_CHANGED, this, true);
        this.root.removeEventListener(ChibaEventNames.SWITCH_TOGGLED, this, true);
        this.root.removeEventListener(XFormsEventNames.SUBMIT_ERROR, this, true);
        this.root.removeEventListener(ChibaEventNames.SCRIPT_ACTION, this, true);

        super.shutdown();
    }


}
// end of class
