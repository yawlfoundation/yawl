/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.QueueSet;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

/*
 * The backing bean for the YAWL 2.0 rss form - shows a dynform instance via an RSS
 * request (such as the iGoogle gadget)
 *
 * @author Michael Adams
 * Date: 21/10/2007
 *
 * Last Date: 28/04/2008
 */

public class rssFormViewer extends AbstractPageBean {

    // REQUIRED AND/OR IMPLEMENTED ABSTRACT PAGE BEAN METHODS //

    private int __placeholder;

    private void _init() throws Exception {}

    public void preprocess() { }

    public void destroy() { }

    public void init() {
        super.init();

        // *Note* - this code must not be modified
        try {
            _init();
        } catch (Exception e) {
            log("Page1 Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }


    public rssFormViewer() { }

    // Return references to scoped data beans //
    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }

    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }

    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }


    /********************************************************************************/

    // COMPONENT DECLARATIONS, GETTERS & SETTERS //

    private Page page = new Page();

    public Page getPage() { return page; }

    public void setPage(Page p) { page = p; }


    private Html html = new Html();

    public Html getHtml() { return html; }

    public void setHtml(Html h) { html = h; }


    private Head head = new Head();

    public Head getHead() { return head; }

    public void setHead(Head h) { head = h; }


    private Link link = new Link();

    public Link getLink() { return link; }

    public void setLink(Link l) { link = l; }


    private Body body = new Body();

    public Body getBody() { return body; }

    public void setBody(Body b) { body = b; }


    private Form form = new Form();

    public Form getForm() { return form; }

    public void setForm(Form f) { form = f; }


    private Button btnClose = new Button();

    public Button getBtnClose() { return btnClose; }

    public void setBtnClose(Button b) { btnClose = b; }


    private StaticText staticText1 = new StaticText();

    public StaticText getStaticText1() { return staticText1; }

    public void setStaticText1(StaticText st) { staticText1 = st; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    /********************************************************************************/

    // SPECIFIC DELARATIONS AND METHODS //

    private final ResourceManager rm = getApplicationBean().getResourceManager();
    private final SessionBean sb = getSessionBean();
    private final MessagePanel msgPanel = sb.getMessagePanel() ;

    private final static String success = "<success/>";

    public void prerender() {
        String msg = "";
        if (sb.isRssFormDisplay()) {
            FormViewer form = sb.getFormViewerInstance();
            if (form != null) {
                msg = form.postDisplay(sb.getRssFormWIR()) ;
                sb.resetPostFormDisplay();
                if (successful(msg)) {
                    msg = "Work Item successfully processed.";
                }
            }    
            else {
                msg = "Unsuccessful form completion - could not finalise form." ;
            }
            showMessage(msg + " Please click the button below to close this window/tab.");
        }
        else if (sb.isRssFormCloseAttempted()) {
            btnClose.setVisible(false);
            sb.setRssFormCloseAttempted(false);
            msg = "This browser does not support automatic closing of the current window/tab." +
                  " Please close it manually.";
            staticText1.setText(msg);            
        }
        else {
            HttpServletRequest request = getRequest();
            String userid = request.getParameter("userid");
            String password = request.getParameter("password");
            String itemid = request.getParameter("itemid");

            msg = validateCredentials(userid, password);
            WorkItemRecord wir = null;
            if (successful(msg)) {
                wir = rm.getWorkItemRecord(itemid);
                if (wir == null) {
                    msg = "Unknown Work Item ID - perhaps it has already been actioned" +
                          " and/or moved to another queue. Please refresh your worklist.";
                }
            }
            if (successful(msg)) {
                startAndShow(userid, wir);
            }
            else {
                showMessage(msg);
            }
        }
    }


    public String btnClose_action() {
        sb.setRssFormCloseAttempted(true);
        return null;
    }


    private boolean successful(String msg) {
        return msg.equals(success);
    }

    private String validateCredentials(String userid, String password) {
        if (userid == null) {
            return "No userid was specified.";
        }
        if (password == null) {
            return "No password was specified.";
        }

        String loggedOnUserID = sb.getUserid();
        if (loggedOnUserID != null) {
            if (! loggedOnUserID.equals(userid)) {
                return "User '" + loggedOnUserID + "' is already logged on in this" +
                           " browser instance (in another tab or window)." +
                           " Only one user logon per browser " +
                           " instance is possible. If you wish to view you work" +
                           " queued items via your iGoogle Gadget, please " +
                           " logout the currently logged on user first." ;
            }
        }
        else {
            if (rm == null) {
               return "Could not connect to work queue, service unavailable.";
            }
            Participant p = rm.getParticipantFromUserID(userid);
            if (p == null) {
                return "Unknown userid: " + userid;
            }
            if (! p.getPassword().equals(password)) {
                return "Incorrect password.";
            }
            if (! rm.hasOrgDataSource()) {
                msgPanel.error("Missing or invalid organisational data source. The resource" +
                               " service requires a connection to a valid data source" +
                               " that contains organisational data. Please check the" +
                               " settings of the 'OrgDataSource' parameter in the service's" +
                               " web.xml to ensure a valid data source is set, and/or check" +
                               " the configuration properties set for the data source.");
            }
            String handle = rm.login(userid, password, sb.getExternalSessionID());
            if (! rm.successful(handle)) {
                return (msgPanel.format(handle));
           }

           initSession(p, userid, handle) ;
        }
        return success ;                 // successful login
     }


    /**
     * Initialise session data in the session bean
     * @param userid the userid
     * @param handle the session handle supplied by the service
     */
    private void initSession(Participant p, String userid, String handle) {
        sb.setRssUserAlreadyLoggedOn(false);
        sb.setSessionhandle(handle);
        sb.setUserid(userid);
        if (! userid.equals("admin")) {
            sb.setParticipant(p);
            getApplicationBean().addLiveUser(userid);
        }
    }


    private void startAndShow(String userid, WorkItemRecord wir) {
        String errMsg = null;
        WorkItemRecord startedItem = null;
        Participant p = rm.getParticipantFromUserID(userid);
        if (wir.hasStatus(WorkItemRecord.statusEnabled) || wir.hasStatus(WorkItemRecord.statusFired)) {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceOffered) ||
                    wir.hasResourceStatus(WorkItemRecord.statusResourceUnoffered)) {
                errMsg = checkParticipantHasQueuedItem(p, wir, WorkQueue.OFFERED);
                if (successful(errMsg)) {
                    errMsg = null;
                    wir = rm.acceptOffer(p, wir);
                    if (wir.hasStatus(WorkItemRecord.statusExecuting)) {  // system start
                        startedItem = wir;
                    }
                }
            }
            if ((errMsg == null) && (startedItem == null)) {
                if (wir.hasResourceStatus(WorkItemRecord.statusResourceAllocated)) {
                    errMsg = checkParticipantHasQueuedItem(p, wir, WorkQueue.ALLOCATED);
                    if (successful(errMsg)) {
                        errMsg = null;
                        if (rm.start(p, wir)) {
                            startedItem = rm.getExecutingChild(wir);
                        }
                        else {
                            errMsg = "Could not start workitem '" + wir.getID() +
                                     "'. Please see the log files for details.";
                        }
                    }
                }
                else {
                    errMsg = "Could not allocate workitem '" + wir.getID() +
                            "'. Please see the log files for details.";
                }
            }
        }
        else {
            if (wir.hasResourceStatus(WorkItemRecord.statusResourceSuspended)) {
                errMsg = checkParticipantHasQueuedItem(p, wir, WorkQueue.SUSPENDED);
                if (successful(errMsg)) {
                    errMsg = null;
                    rm.unsuspendWorkItem(p, wir);
                }
            }
            if (errMsg == null) {
                errMsg = checkParticipantHasQueuedItem(p, wir, WorkQueue.STARTED);
                if (successful(errMsg)) {
                    errMsg = null;
                    startedItem = wir;
                }    
            }
        }
        if ((errMsg == null) && (startedItem == null)) {
            errMsg = "Could not start workitem '" + wir.getID() +
                     "'. Please see the log files for details.";
        }

        if (errMsg != null) {
            showMessage(errMsg);
        }
        else {
            showForm(startedItem);
        }
    }


    private String checkParticipantHasQueuedItem(Participant p, WorkItemRecord wir, int qType) {
        String result = success;
        boolean queuedItem = false;
        if (p != null) {
            QueueSet qSet = p.getWorkQueues();
            if (qSet != null) {
                Set<WorkItemRecord> items = qSet.getQueuedWorkItems(qType);
                if (items != null) {
                    queuedItem = items.contains(wir);
                }
            }
            if (! queuedItem) {
                result = "Work item '" + wir.getID() + "' is no longer in the " +
                        "participant's queue. Please refresh your worklist.";
            }
        }
        else {
            result = "Could not locate participant with userid supplied.";
        }
        return result;
    }


    private HttpServletRequest getRequest() {
        HttpServletRequest request = null;
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        if (context != null) {
            request = (HttpServletRequest) context.getRequest();
        }
        return request;
    }


    private void showMessage(String msg) {
        staticText1.setText(msg);
        logout();
    }


    private void showForm(WorkItemRecord wir) {
        FormViewer form = new FormViewer(sb);
        String formURI = form.display(wir);
        if (formURI.startsWith("<failure>")) {
            showMessage(unwrap(formURI));
        }
        else {
            sb.setFormViewerInstance(form);
            sb.setRssFormWIR(wir);
            sb.setRssFormDisplay(true);
            if (formURI.equals("showDynForm")) formURI = "dynForm.jsp";
            redirect(formURI);
        }       
    }


    private void redirect(String uri) {
        try {
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            if (context != null) {
                context.redirect(uri);
            }
        }
        catch (IOException ioe) {
            showMessage(ioe.getMessage());
        }
    }


    /* returns strings from inside xml tags */
    private String unwrap(String xml) {
        String result = null;
        if (xml != null) {
            int start = xml.indexOf(">") + 1;
            int finish = xml.lastIndexOf("<");
            if (start >= 0 && finish >= 0 && finish > start) {
                result = xml.substring(start, finish);
            }
        }
        return result;
    }


    private void logout() {
        if (! sb.isRssUserAlreadyLoggedOn()) {
            sb.doLogout();
        }
    }

}