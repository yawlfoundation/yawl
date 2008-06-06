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
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.WorkQueue;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.validator.ValidatorException;
import java.util.Set;

/*
 * The backing bean for the YAWL 2.0 login form
 *
 * @author Michael Adams
 * Date: 21/10/2007
 *
 * Last Date: 28/04/2008
 */

public class Login extends AbstractPageBean {

    // REQUIRED AND/OR IMPLEMENTED ABSTRACT PAGE BEAN METHODS //

    private int __placeholder;

    private void _init() throws Exception {}

    public void preprocess() { }

    public void prerender() {
        msgPanel.show(70, 0, "relative");             // show messages as required
    }

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


    public Login() { }

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


    private Button btnLogin = new Button();

    public Button getBtnLogin() { return btnLogin; }

    public void setBtnLogin(Button b) { btnLogin = b; }


    private Label lblUserName = new Label();

    public Label getLblUserName() { return lblUserName; }

    public void setLblUserName(Label l) { lblUserName = l; }


    private TextField txtUserName = new TextField();

    public TextField getTxtUserName() { return txtUserName; }

    public void setTxtUserName(TextField tf) { txtUserName = tf; }


    private Label lblPassword = new Label();

    public Label getLblPassword() { return lblPassword; }

    public void setLblPassword(Label l) { lblPassword = l; }


    private PasswordField txtPassword = new PasswordField();

    public PasswordField getTxtPassword() { return txtPassword; }

    public void setTxtPassword(PasswordField pf) { txtPassword = pf; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    /********************************************************************************/

    // SPECIFIC DELARATIONS AND METHODS //

    private ResourceManager rm = getApplicationBean().getResourceManager();
    private SessionBean sb = getSessionBean();
    private MessagePanel msgPanel = sb.getMessagePanel() ;

    
    /**
     * Respond to a user-click of the Login button
     * @return the next page to show, or null to stay on this page
     */
    public String btnLogin_action() {
        String nextPage = null ;

        // check if this browser session already has a logged in user
        if (sb.getParticipant() != null) {
            msgPanel.error("User '" + sb.getUserid() + "' is already logged on in this" +
                           " browser instance. Only one user logon per browser " +
                           " instance is possible. If you wish to logon, please " +
                           " logout the previous user first.") ;
        }

        // session is free, so if there's a valid org data source --> process the logon
        else if (rm.hasOrgDataSource()) {
            String user = (String) txtUserName.getText() ;
            String pword = (String) txtPassword.getText();
            if (validateUser(user, pword)) {
                if (user.equals("admin")) {                              // special case
                    sb.setMnuSelectorStyle("top: 128px");
                    nextPage = "showAdminQueues" ;
                }
                else nextPage =  "showUserQueues" ;
            }
        }

        // else no org data source --> can't proceed
        else {
            msgPanel.error("Missing or invalid organisational data source. The resource" +
                           " service requires a connection to a valid data source" +
                           " that contains organisational data. Please check the" +
                           " settings in the service's web.xml to ensure a valid" +
                           " data source is set.");
        }
        return nextPage;           
    }


    /**
     * Validates a userid - password combination
     * @param u the userid
     * @param p the password
     * @return true if the userid-password combination is valid
     */
    private boolean validateUser(String u, String p) {
        if ((u == null) || (p == null)) {
            msgPanel.info("Please enter a valid username and password") ;
            return false;
        }

        // attempt to log on (and gain a session) to the service
        if (rm != null) {
            String handle = rm.login(u, p);
            if (Interface_Client.successful(handle)) {           // successful login
                initSession(u, handle) ;
                msgPanel.clear();
                return true ;
            }
            else {
                msgPanel.error(msgPanel.format(handle));        // show error msg to user
                return false ;
            }    
        }
        else throw new ValidatorException(new FacesMessage("Could not connect to work queue"));
    }


    /**
     * Initialise session data in the session bean
     * @param userid the userid
     * @param handle the session handle supplied by the service 
     */
    private void initSession(String userid, String handle) {
        sb.setSessionhandle(handle);
        sb.setUserid(userid);
        if (! userid.equals("admin")) {
            sb.setParticipant(rm.getParticipantFromUserID(userid));
            getApplicationBean().addLiveUser(userid);

            // initialise workqueue
            Set<WorkItemRecord> wirSet = sb.getQueue(WorkQueue.OFFERED);

            if ((wirSet != null) && (! wirSet.isEmpty()))
                sb.setChosenWIR(wirSet.iterator().next());
        }
    }

}

