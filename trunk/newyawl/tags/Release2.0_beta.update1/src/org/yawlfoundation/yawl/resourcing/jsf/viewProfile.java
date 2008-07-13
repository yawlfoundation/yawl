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
import com.sun.rave.web.ui.model.Option;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.resource.Capability;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Position;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import javax.faces.FacesException;
import java.util.Set;

/**
 * Backing bean for edit profile form
 *
 * @author: Michael Adams
 *
 * Date: 31/01/2008
 * Last Date: 16/03/2008
 */

public class viewProfile extends AbstractPageBean {
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;

    /**
     * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
    private void _init() throws Exception {
    }

    private Page page1 = new Page();

    public Page getPage1() {
        return page1;
    }

    public void setPage1(Page p) {
        this.page1 = p;
    }

    private Html html1 = new Html();

    public Html getHtml1() {
        return html1;
    }

    public void setHtml1(Html h) {
        this.html1 = h;
    }

    private Head head1 = new Head();

    public Head getHead1() {
        return head1;
    }

    public void setHead1(Head h) {
        this.head1 = h;
    }

    private Link link1 = new Link();

    public Link getLink1() {
        return link1;
    }

    public void setLink1(Link l) {
        this.link1 = l;
    }

    private Body body1 = new Body();

    public Body getBody1() {
        return body1;
    }

    public void setBody1(Body b) {
        this.body1 = b;
    }

    private Form form1 = new Form();

    public Form getForm1() {
        return form1;
    }

    public void setForm1(Form f) {
        this.form1 = f;
    }

    private PanelLayout pnlProfile = new PanelLayout();

    public PanelLayout getPnlProfile() {
        return pnlProfile;
    }

    public void setPnlProfile(PanelLayout pl) {
        this.pnlProfile = pl;
    }

    private PanelLayout pnlPiled = new PanelLayout();

    public PanelLayout getPnlPiled() {
        return pnlPiled;
    }

    public void setPnlPiled(PanelLayout pnlPiled) {
        this.pnlPiled = pnlPiled;
    }

    private PanelLayout pnlChained = new PanelLayout();

    public PanelLayout getPnlChained() {
        return pnlChained;
    }

    public void setPnlChained(PanelLayout pnlChained) {
        this.pnlChained = pnlChained;
    }

    private StaticText sttPassword = new StaticText();

    public StaticText getSttPassword() {
        return sttPassword;
    }

    public void setSttPassword(StaticText st) {
        this.sttPassword = st;
    }

    private StaticText sttPiled = new StaticText();

    public StaticText getSttPiled() {
        return sttPiled;
    }

    public void setSttPiled(StaticText sttPiled) {
        this.sttPiled = sttPiled;
    }

    private StaticText sttChained = new StaticText();

    public StaticText getSttChained() {
        return sttChained;
    }

    public void setSttChained(StaticText sttChained) {
        this.sttChained = sttChained;
    }

    private Listbox lbxPiled = new Listbox() ;

    public Listbox getLbxPiled() {
        return lbxPiled;
    }

    public void setLbxPiled(Listbox lbxPiled) {
        this.lbxPiled = lbxPiled;
    }

    private Listbox lbxChained = new Listbox();

    public Listbox getLbxChained() {
        return lbxChained;
    }

    public void setLbxChained(Listbox lbxChained) {
        this.lbxChained = lbxChained;
    }

    private PasswordField txtNewPassword = new PasswordField();

    private PasswordField txtConfirmPassword = new PasswordField() ;

    public PasswordField getTxtNewPassword() {
        return txtNewPassword;
    }

    public void setTxtNewPassword(PasswordField txtNewPassword) {
        this.txtNewPassword = txtNewPassword;
    }

    public PasswordField getTxtConfirmPassword() {
        return txtConfirmPassword;
    }

    public void setTxtConfirmPassword(PasswordField txtConfirmPassword) {
        this.txtConfirmPassword = txtConfirmPassword;
    }


    private TextField txtName = new TextField();

    public TextField getTxtName() {
        return txtName;
    }

    public void setTxtName(TextField tf) {
        this.txtName = tf;
    }


    private TextField txtUserID = new TextField();

    public TextField getTxtUserID() {
        return txtUserID;
    }

    public void setTxtUserID(TextField tf) {
        this.txtUserID = tf;
    }

    private Checkbox cbxAdmin = new Checkbox();

    public Checkbox getCbxAdmin() {
        return cbxAdmin;
    }

    public void setCbxAdmin(Checkbox c) {
        this.cbxAdmin = c;
    }


    private PanelLayout pnlNewPassword = new PanelLayout();

    public PanelLayout getPnlNewPassword() {
        return pnlNewPassword;
    }

    public void setPnlNewPassword(PanelLayout pl) {
        this.pnlNewPassword = pl;
    }


    private DropDown cbbRoles = new DropDown();

    public DropDown getCbbRoles() {
        return cbbRoles;
    }

    public void setCbbRoles(DropDown dd) {
        this.cbbRoles = dd;
    }

    private DropDown cbbPositions = new DropDown();

    public DropDown getCbbPositions() {
        return cbbPositions;
    }

    public void setCbbPositions(DropDown cbbPositions) {
        this.cbbPositions = cbbPositions;
    }

    private DropDown cbbCapabilities = new DropDown();

    public DropDown getCbbCapabilities() {
        return cbbCapabilities;
    }

    public void setCbbCapabilities(DropDown cbbCapabilities) {
        this.cbbCapabilities = cbbCapabilities;
    }

    private Label lblName = new Label();

    public Label getLblName() {
        return lblName;
    }

    public void setLblName(Label l) {
        this.lblName = l;
    }

    private Label lblUserID = new Label();

    public Label getLblUserID() {
        return lblUserID;
    }

    public void setLblUserID(Label lblUserID) {
        this.lblUserID = lblUserID;
    }

    private Label lblRoles = new Label();

    public Label getLblRoles() {
        return lblRoles;
    }

    public void setLblRoles(Label lblRoles) {
        this.lblRoles = lblRoles;
    }

    private Label lblPositions = new Label();

    public Label getLblPositions() {
        return lblPositions;
    }

    public void setLblPositions(Label lblPositions) {
        this.lblPositions = lblPositions;
    }

    private Label lblCapabilities = new Label();

    public Label getLblCapabilities() {
        return lblCapabilities;
    }

    public void setLblCapabilities(Label lblCapabilities) {
        this.lblCapabilities = lblCapabilities;
    }

    private Label lblNewPassword = new Label();

    public Label getLblNewPassword() {
        return lblNewPassword;
    }

    public void setLblNewPassword(Label lblNewPassword) {
        this.lblNewPassword = lblNewPassword;
    }

    private Label lblConfirmPassword = new Label();

    public Label getLblConfirmPassword() {
        return lblConfirmPassword;
    }

    public void setLblConfirmPassword(Label lblConfirmPassword) {
        this.lblConfirmPassword = lblConfirmPassword;
    }

    private Button btnSave = new Button();

    public Button getBtnSave() {
        return btnSave;
    }

    public void setBtnSave(Button b) {
        this.btnSave = b;
    }

    private Button btnSavePassword = new Button();

    public Button getBtnSavePassword() {
        return btnSavePassword;
    }

    public void setBtnSavePassword(Button b) {
        this.btnSavePassword = b;
    }

    private Button btnUnpile = new Button();

    public Button getBtnUnpile() {
        return btnUnpile;
    }

    public void setBtnUnpile(Button btnUnpile) {
        this.btnUnpile = btnUnpile;
    }

    private Button btnUnchain = new Button();

    public Button getBtnUnchain() {
        return btnUnchain;
    }

    public void setBtnUnchain(Button b) {
        this.btnUnchain = b;
    }

    private Button btnRemove = new Button();

    public Button getBtnRemove() {
        return btnRemove;
    }

    public void setBtnRemove(Button b) {
        this.btnRemove = b;
    }

    private Button btnAdd = new Button();

    public Button getBtnAdd() {
        return btnAdd;
    }

    public void setBtnAdd(Button b) {
        this.btnAdd = b;
    }

    private MessageGroup msgGroup = new MessageGroup();

    public MessageGroup getMsgGroup() {
        return msgGroup;
    }

    public void setMsgGroup(MessageGroup msgGroup) {
        this.msgGroup = msgGroup;
    }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    // </editor-fold>


    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public viewProfile() {
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }


    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }


    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }


    /**
     * <p>Callback method that is called whenever a page is navigated to,
     * either directly via a URL, or indirectly via page navigation.
     * Customize this method to acquire resources that will be needed
     * for event handlers and lifecycle methods, whether or not this
     * page is performing post back processing.</p>
     *
     * <p>Note that, if the current request is a postback, the property
     * values of the components do <strong>not</strong> represent any
     * values submitted with this request.  Instead, they represent the
     * property values that were saved for this view when it was rendered.</p>
     */
    public void init() {
        // Perform initializations inherited from our superclass
        super.init();
        // Perform application initialization that must complete
        // *before* managed components are initialized
        // TODO - add your own initialiation code here

        // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Initialization">
        // Initialize automatically managed components
        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("participantData Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here

    }

    /**
     * <p>Callback method that is called after the component tree has been
     * restored, but before any event processing takes place.  This method
     * will <strong>only</strong> be called on a postback request that
     * is processing a form submit.  Customize this method to allocate
     * resources that will be required in your event handlers.</p>
     */
    public void preprocess() {
    }

    /**
     * <p>Callback method that is called just before rendering takes place.
     * This method will <strong>only</strong> be called for the page that
     * will actually be rendered (and not, for example, on a page that
     * handled a postback and then navigated to a different page).  Customize
     * this method to allocate resources that will be required for rendering
     * this page.</p>
     */
    public void prerender() {
        getSessionBean().checkLogon();
        populateFields(participant);
        getSessionBean().setActivePage(ApplicationBean.PageRef.viewProfile);
        msgPanel.show(455, 124, "absolute");
    }

    /**
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called (regardless of whether
     * or not this was the page that was actually rendered).  Customize this
     * method to release resources acquired in the <code>init()</code>,
     * <code>preprocess()</code>, or <code>prerender()</code> methods (or
     * acquired during execution of an event handler).</p>
     */
    public void destroy() { }


    private MessagePanel msgPanel = getSessionBean().getMessagePanel() ;
    private Participant participant = getSessionBean().getParticipant();
    private ResourceManager rm = getApplicationBean().getResourceManager();


    public String btnSavePassword_action() {
        if (checkValidPasswordChange()) {
            if (participant != null)
                participant.setPassword((String) txtNewPassword.getText());
            msgPanel.success("Password change successfully saved.");
        }
        return null;
    }

    public String btnUnchain_action() {
        String chained = (String) lbxChained.getSelected();
        if (chained != null) {
//            caseID = chained.substring(0, chained.indexOf("::"));
//            rm.unChainCase(caseID) ;
        }    
        return null;
    }


    public String btnUnpile_action() {
        String piled = (String) lbxPiled.getSelected();
        if (piled != null) {

            // split into specid, taskid
            String[] parts = piled.split("::");
            String result = rm.unpileTask(parts[0], parts[1]) ;
            showResult(result);
        }
        else msgPanel.warn("No task selected to unpile");
       return null;
    }

    private void populateFields(Participant p) {
        if (p != null) {

            // set simple fields
            txtName.setText(p.getFullName());
            txtUserID.setText(p.getUserID());
            cbbRoles.setItems(getRolesList(p));
            cbbPositions.setItems(getPositionsList(p));
            cbbCapabilities.setItems(getCapabilitiesList(p));
            cbxAdmin.setValue(p.isAdministrator());

            // clear any leftover passwords
            txtNewPassword.setPassword("");
            txtConfirmPassword.setPassword("");
        }
    }

    private Option[] getRolesList(Participant p) {
        Option[] result = null;
        Set<Role> roles = p.getRoles();
        if (! roles.isEmpty()) {
            result = new Option[roles.size()] ;
            int i = 0 ;
            for (Role r : roles)
                 result[i++] = new Option(r.getName());
        }
        return result ;
    }

    private Option[] getPositionsList(Participant p) {
        Option[] result = null;
        Set<Position> positions = p.getPositions();
        if (! positions.isEmpty()) {
            result = new Option[positions.size()] ;
            int i = 0 ;
            for (Position po : positions)
                 result[i++] = new Option(po.getTitle());
        }
        return result ;
    }

    private Option[] getCapabilitiesList(Participant p) {
        Option[] result = null;
        Set<Capability> capabilities = p.getCapabilities();
        if (! capabilities.isEmpty()) {
            result = new Option[capabilities.size()] ;
            int i = 0 ;
            for (Capability c : capabilities)
                 result[i++] = new Option(c.getCapability());            
        }
        return result ;
    }


    private void clearFields() {

        // clear simple fields
        txtName.setText("");
        txtUserID.setText("");
        cbbRoles.setItems(null);
        cbbPositions.setItems(null);
        cbbCapabilities.setItems(null);

        cbxAdmin.setValue("");
        txtNewPassword.setPassword("");
        txtConfirmPassword.setPassword("");
    }


    private boolean checkValidPasswordChange() {
        if (checkForRequiredValues()) {
            String password = (String) txtNewPassword.getPassword();
            if (password.length() > 0) {
                if (password.indexOf(" ") > -1)
                    return false;                // no spaces allowed
                String confirm = (String) txtConfirmPassword.getPassword();
                return password.equals(confirm);
            }
            return true ;
        }
        else return false;
    }


    private boolean checkForRequiredValues() {
        boolean result = true;
        if (! hasText(txtNewPassword)) {
            msgPanel.error("ERROR: A password is required");
            result = false;
        }
        if (! hasText(txtConfirmPassword)) {
            msgPanel.error("ERROR: A 'confirm' password is required");
            result = false;
        }
        return result ;
    }

    private boolean hasText(PasswordField field) {
        return ((String) field.getPassword()).length() > 0 ;
    }

    private void showResult(String result) {
        if (result.startsWith("Cannot"))
            msgPanel.error(result);
        else if (result.indexOf("success") != -1)
            msgPanel.success(result);
        else
            msgPanel.info(result);            
    }

}
