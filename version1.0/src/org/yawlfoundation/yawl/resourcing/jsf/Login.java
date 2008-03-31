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
 * Last Date: 04/03/2008
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

    public Login() { }

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

    private MessagePanel msgPanel = getSessionBean().getMessagePanel() ;
    

    public String btnLogin_action() {
        String nextPage = null ;
        String user = (String) txtUserName.getText() ;
        String pword = (String) txtPassword.getText();
        if (validateUser(user, pword)) {
            if (user.equals("admin")) {                              // special case
                nextPage = "showAdminQueues" ;
                getSessionBean().setMnuSelectorStyle("top: 128px");
            }
            else nextPage =  "showUserQueues" ;
        }
        return nextPage;
    }


    private boolean validateUser(String u, String p) {
        if ((u == null) || (p == null)) {
            msgPanel.info("Please enter a username and password") ;
            return false;
        }

//        if (getApplicationBean().isLoggedOn(u)) {
//            msgPanel.error("Userid " + u + " is already logged on elsewhere.");
//            return false;
//        }

        ResourceManager rm = getApplicationBean().getResourceManager();
        if (rm != null) {
            String handle = rm.login(u, p);
            if (Interface_Client.successful(handle)) {
                initSession(rm, u, handle) ;
                msgPanel.clear();
                return true ;
            }
            else {
                msgPanel.error(msgPanel.format(handle));
                return false ;
            }    
        }
        else throw new ValidatorException(new FacesMessage("Could not connect to work queue"));
    }

    
    private void initSession(ResourceManager rm, String userid, String handle) {
        SessionBean sb = getSessionBean();
        sb.setSessionhandle(handle);
        sb.setUserid(userid);
        if (! userid.equals("admin")) {
            sb.setParticipant(rm.getParticipantFromUserID(userid));
            getApplicationBean().addLiveUser(userid);
            Set<WorkItemRecord> wirSet = sb.getQueue(WorkQueue.OFFERED);

            if ((wirSet != null) && (! wirSet.isEmpty()))
                sb.setChosenWIR(wirSet.iterator().next());
        }
    }

}

