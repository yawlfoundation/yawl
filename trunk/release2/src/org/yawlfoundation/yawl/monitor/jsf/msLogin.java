/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.monitor.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;
import org.yawlfoundation.yawl.monitor.MonitorClient;
import org.yawlfoundation.yawl.resourcing.jsf.MessagePanel;
import org.yawlfoundation.yawl.resourcing.util.PasswordEncryptor;

import javax.faces.FacesException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/*
 * The backing bean for the YAWL 2.0 login form
 *
 * @author Michael Adams
 * Date: 21/10/2007
 *
 * Last Date: 28/04/2008
 */

public class msLogin extends AbstractPageBean {

    // REQUIRED AND/OR IMPLEMENTED ABSTRACT PAGE BEAN METHODS //

    private int __placeholder;

    private void _init() throws Exception {}

    public void preprocess() { }

    public void prerender() {
        msgPanel.show();                                  // show messages as required
    }

    public void destroy() { }

    public void init() {
        super.init();

        // *Note* - this code must not be modified
        try {
            _init();
        } catch (Exception e) {
            log("Page Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }


    public msLogin() { }

    // Return references to scoped data beans //
    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
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

    private MonitorClient mc = MonitorClient.getInstance();
    private SessionBean sb = getSessionBean();
    private MessagePanel msgPanel = sb.getMessagePanel() ;


    /**
     * Respond to a user-click of the Login button
     * @return the next page to show, or null to stay on this page
     */
    public String btnLogin_action() {
        String nextPage = null ;

        // check if this browser session already has a logged in user
        if (sb.getUserid() != null) {
            msgPanel.error("User '" + sb.getUserid() + "' is already logged on in this" +
                           " browser instance. Only one user logon per browser " +
                           " instance is possible. If you wish to logon, please " +
                           " logout the current user first.") ;
        }

        // session is free --> process the logon
        else {
            String user = (String) txtUserName.getText() ;
            String pword = (String) txtPassword.getText();
            if (validateUser(user, pword)) {
                nextPage = "showCases";
            }
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
        if (mc != null) {
            String pEncrypt = p ;                                // default for admin
            if (! u.equals("admin")) {
                try {
                    pEncrypt = PasswordEncryptor.encrypt(p) ;
                }
                catch(NoSuchAlgorithmException nsae) {
                    msgPanel.error("Password Encryption Algorithm not available. Login failed.");
                    return false;
                }
                catch(UnsupportedEncodingException uee) {
                    msgPanel.error("Password could not be encrypted. Login failed.");
                    return false;
                }
            }

            String handle = mc.login(u, pEncrypt);
            if (mc.successful(handle)) {           // successful login
                initSession(u, handle) ;
                msgPanel.clear();
                return true ;
            }
            else {
                msgPanel.error(msgPanel.format(handle));        // show error msg to user
                return false ;
            }
        }
        else {
            msgPanel.error("Could not connect to monitor client, service unavailable.");
            return false;
        }

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
   //         sb.setParticipant(mc.getParticipantFromUserID(userid));
            getApplicationBean().addLiveUser(userid);
        }    
    }

}