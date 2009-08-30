/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/*
 * The backing bean for the YAWL 2.0 login form
 *
 * @author Michael Adams
 * Date: 21/10/2007
 *
 * Last Date: 28/04/2008
 */

public class rsslink extends AbstractPageBean {

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


    public rsslink() { }

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

    private ResourceManager rm = getApplicationBean().getResourceManager();
    private SessionBean sb = getSessionBean();
    private MessagePanel msgPanel = sb.getMessagePanel() ;


    public void prerender() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) context.getRequest();
        String userid = request.getParameter("userid");
        String password = request.getParameter("password");
        String itemid = request.getParameter("itemid");

        String msg = validateCredentials(userid, password);
        WorkItemRecord wir = null;
        if (msg.equals("success")) {
            wir = rm.getWorkItemRecord(itemid);
            if (wir == null) {
                msg = "Unknown Work Item ID - perhaps it has already been actioned.";
            }
        }
        if (msg.equals("success")) {
            moveToStarted(userid, wir);
        }
        else {
            showMessage(msg);
        }
    }


    public String btnClose_action() {
        return null;
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
                           " browser instance. Only one user logon per browser " +
                           " instance is possible. If you wish to logon, please " +
                           " logout the current user first." ;
            }
            else sb.setRssAlreadyLoggedOn(true);
        }
        else {
            if (rm == null) {
               return "Could not connect to work queue, service unavailable.";
            }
            Participant p = rm.getParticipantFromUserID(userid);
            if (p == null) {
                return "Unknown userid: " + userid;
            }
            password = password.replaceAll(" ", "+");                     // decode
            if (! p.getPassword().equals(password)) {
                return "Incorrect password.";
            }
            if (! rm.hasOrgDataSource()) {
                return "Missing or invalid organisational data source. The resource" +
                           " service requires a connection to a valid data source" +
                           " that contains organisational data. Please check the" +
                           " settings in the service's web.xml to ensure a valid" +
                           " data source is set.";
            }
            String handle = rm.login(userid, password);
            if (! rm.successful(handle)) {
                return (msgPanel.format(handle));
           }

           initSession(p, userid, handle) ;
        }
        return "success" ;                 // successful login
     }


    /**
     * Initialise session data in the session bean
     * @param userid the userid
     * @param handle the session handle supplied by the service
     */
    private void initSession(Participant p, String userid, String handle) {
        sb.setSessionhandle(handle);
        sb.setUserid(userid);
        if (! userid.equals("admin")) {
            sb.setParticipant(p);
            getApplicationBean().addLiveUser(userid);
        }
    }


    private void moveToStarted(String userid, WorkItemRecord wir) {
        String errMsg = null;
        Participant p = rm.getParticipantFromUserID(userid);
        if (wir.hasResourceStatus(WorkItemRecord.statusResourceOffered) ||
            wir.hasResourceStatus(WorkItemRecord.statusResourceUnoffered)) {
            rm.acceptOffer(p, wir);
        }
        if (wir.hasResourceStatus(WorkItemRecord.statusResourceAllocated)) {
            if (! rm.start(p, wir, sb.getSessionhandle())) {
                errMsg = "Could not start workitem '" + wir.getID() +
                         "'. Please see the log files for details.";
            }
        }
        if (! wir.hasResourceStatus(WorkItemRecord.statusResourceSuspended)) {
            rm.unsuspendWorkItem(p, wir);
        }
        if (! wir.hasResourceStatus(WorkItemRecord.statusResourceStarted)) {
            errMsg = "Another participant has already accepted this offer.";
        }

        if (errMsg != null) {
            showMessage(errMsg);
        }
        else {
            List<WorkItemRecord> startedItems = rm.getChildren(wir.getID());
            for (WorkItemRecord item : startedItems) {
                showForm(item);
            }
        }
    }


    private void showMessage(String msg) {
        staticText1.setText(msg);
        logout();
    }


    private void showForm(WorkItemRecord wir) {
        staticText1.setText(wir.getID());
        logout();
    }


    private void logout() {
        if (! sb.isRssAlreadyLoggedOn()) {
            sb.doLogout();
        }
        else {
            sb.setRssAlreadyLoggedOn(true);
        }
    }

}