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
import org.chiba.adapter.ChibaEvent;
import org.chiba.adapter.DefaultChibaEventImpl;
import org.chiba.web.session.XFormsSession;
import org.chiba.web.session.XFormsSessionManager;
import org.chiba.web.upload.UploadInfo;
import org.chiba.web.WebAdapter;
import org.chiba.xml.dom.DOMUtil;
import org.chiba.xml.xforms.exception.XFormsException;
import org.w3c.dom.Element;
import uk.ltd.getahead.dwr.ExecutionContext;

import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;

/**
 * AJAX Facade class to hide the full functionality from the web-client.
 *
 * @author Joern Turner
 * @version $Id: FluxFacade.java,v 1.6 2007/02/28 12:03:45 joernt Exp $
 */
public class FluxFacade {
    //this is a custom event to activate a trigger in XForms.
    public static final String FLUX_ACTIVATE_EVENT = "flux-action-event";
    private static final Logger LOGGER = Logger.getLogger(FluxAdapter.class);
    private HttpSession session;


    /**
     * grabs the actual web from the session.
     */
    public FluxFacade() {
        session = ExecutionContext.get().getSession();
    }

    /**
     * executes a trigger
     *
     * @param id the id of the trigger to execute
     * @return the list of events that may result through this action
     * @throws FluxException
     */
    public org.w3c.dom.Element fireAction(String id, String sessionKey) throws FluxException {
        ChibaEvent chibaActivateEvent = new DefaultChibaEventImpl();
        chibaActivateEvent.initEvent(FLUX_ACTIVATE_EVENT, id, null);
        return dispatch(chibaActivateEvent, sessionKey);
    }

    /**
     * sets the value of a control in the processor.
     *
     * @param id    the id of the control in the host document
     * @param value the new value
     * @return the list of events that may result through this action
     * @throws FluxException
     */
    public org.w3c.dom.Element setXFormsValue(String id, String value, String sessionKey) throws FluxException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("FluxFacade instance: " + this.toString());
        }
        ChibaEvent event = new DefaultChibaEventImpl();
        event.initEvent("SETVALUE", id, value);
        return dispatch(event, sessionKey);
    }

    public org.w3c.dom.Element setRepeatIndex(String id, String position, String sessionKey) throws FluxException {
        ChibaEvent event = new DefaultChibaEventImpl();
        event.initEvent("SETINDEX", id, position);
        return dispatch(event, sessionKey);
    }

    /**
     * fetches the progress of a running upload.
     *
     * @param id       id of the upload control in use
     * @param filename filename for uploaded data
     * @return a array containing two elements for evaluation in browser. First
     *         param is the upload control id and second will be the current
     *         progress of the upload.
     */
    public org.w3c.dom.Element fetchProgress(String id, String filename, String sessionKey) {
        String progress;
        UploadInfo uploadInfo;

        if (session.getAttribute(XFormsSession.ADAPTER_PREFIX + sessionKey + "-uploadInfo") != null) {
            uploadInfo = (UploadInfo) session.getAttribute(XFormsSession.ADAPTER_PREFIX + sessionKey + "-uploadInfo");

            if (uploadInfo.isInProgress()) {
                double p = uploadInfo.getBytesRead() / uploadInfo.getTotalSize();

                progress = p + "";
                float total = uploadInfo.getTotalSize();
                float read = uploadInfo.getBytesRead();
                int iProgress = (int) Math.ceil((read / total) * 100);
                if (iProgress < 100) {
                	progress = Integer.toString(iProgress);
                }
                else {
                	progress = "99";
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Bytes total: " + uploadInfo.getTotalSize());
                    LOGGER.debug("Bytes read: " + uploadInfo.getBytesRead());
                    LOGGER.debug("elapsed time: " + uploadInfo.getElapsedTime());
                    LOGGER.debug("status: " + uploadInfo.getStatus());
                    LOGGER.debug("Percent completed: " + Math.ceil((read / total) * 100));
                }
            } else {
                progress = "100";
            }
        } else {
            //if session info is not found for some reason return 100 for safety which allows to exit
            //javascript polling of progress info
            progress = "100";
        }
        XFormsSessionManager manager = (XFormsSessionManager) session.getAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER);
        XFormsSession xFormsSession = manager.getXFormsSession(sessionKey);

        FluxAdapter adapter = (FluxAdapter) xFormsSession.getAdapter();
        EventLog eventLog = adapter.getEventLog();

        Element eventlogElement = eventLog.getLog();
        eventLog.flush();

        Element progressEvent = eventLog.add("upload-progress-event", id, "upload");
        eventLog.addProperty(progressEvent, "progress", progress);
        return eventlogElement;
    }

    /**
     * Note user typing activity (not value change),
     * which extends session lifetime.
     */
    public void keepAlive(String sessionKey) throws FluxException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("FluxFacade keepAlive: " + sessionKey);
        }
        XFormsSessionManager manager = (XFormsSessionManager) session.getAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER);
        XFormsSession xFormsSession = manager.getXFormsSession(sessionKey);
        xFormsSession.getKey();
    }

    /**
     * Note page unload, which rapidly ages session.
     */
    public void close(String sessionKey) throws FluxException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("FluxFacade close: " + sessionKey);
        }
        XFormsSessionManager manager = (XFormsSessionManager) session.getAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER);
        XFormsSession xFormsSession = manager.getXFormsSession(sessionKey);
        try {

            // don't use getXFormsSession to avoid needless error
            if (xFormsSession == null) return;
            WebAdapter adapter = xFormsSession.getAdapter();
            if (adapter == null) return;
            adapter.shutdown();
        } catch (XFormsException e) {
            LOGGER.warn("FluxFacade close: " + sessionKey, e);
        } finally {
            manager.deleteXFormsSession(sessionKey);
        }
    }

    private org.w3c.dom.Element dispatch(ChibaEvent event, String sessionKey) throws FluxException {
        XFormsSessionManager manager = (XFormsSessionManager) session.getAttribute(XFormsSessionManager.XFORMS_SESSION_MANAGER);
        XFormsSession xFormsSession = manager.getXFormsSession(sessionKey);

        if(xFormsSession == null){
            LOGGER.fatal("XFormsSession not found - stopping");
            throw new FluxException("Your session has expired - Please start again.");
        }

        FluxAdapter adapter = (FluxAdapter) xFormsSession.getAdapter();
        if (adapter != null) {
            try {
                adapter.dispatch(event);
            }
            catch (XFormsException e) {
                LOGGER.error(e.getMessage());
            }
        } else {
            //session expired or cookie got lost
            throw new FluxException("Session expired. Please start again.");
        }
        EventLog eventLog = adapter.getEventLog();
        Element eventlogElement = eventLog.getLog();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(this);
            LOGGER.debug(eventLog.toString());
            LOGGER.debug(adapter);
            try {
                DOMUtil.prettyPrintDOM(eventlogElement, System.out);
            }
            catch (TransformerException e) {
                e.printStackTrace();
            }
        }
        return eventlogElement;
    }

}

// end of class
