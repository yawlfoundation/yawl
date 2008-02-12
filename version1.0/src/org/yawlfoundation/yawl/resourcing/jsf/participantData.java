/*
 * participantData.java
 *
 * Created on 26 January 2008, 19:35
 * Copyright adamsmj
 */
package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;

import javax.faces.FacesException;
import javax.faces.event.ValueChangeEvent;

import com.sun.rave.web.ui.model.SingleSelectOptionsList;
import com.sun.rave.web.ui.component.*;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.UserPrivileges;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class participantData extends AbstractPageBean {
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

    private PanelLayout pnlPrivileges = new PanelLayout();

    public PanelLayout getPnlPrivileges() {
        return pnlPrivileges;
    }

    public void setPnlPrivileges(PanelLayout pl) {
        this.pnlPrivileges = pl;
    }

    private StaticText staticText1 = new StaticText();

    public StaticText getStaticText1() {
        return staticText1;
    }

    public void setStaticText1(StaticText st) {
        this.staticText1 = st;
    }

    private StaticText sttPassword = new StaticText();

    public StaticText getSttPassword() {
        return sttPassword;
    }

    public void setSttPassword(StaticText st) {
        this.sttPassword = st;
    }

    private PasswordField txtNewPassword ;

    private PasswordField txtConfirmPassword ;

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

    private Checkbox cbxChooseItemToStart = new Checkbox();

    public Checkbox getCbxChooseItemToStart() {
        return cbxChooseItemToStart;
    }

    public void setCbxChooseItemToStart(Checkbox c) {
        this.cbxChooseItemToStart = c;
    }

    private Checkbox cbxStartConcurrent = new Checkbox();

    public Checkbox getCbxStartConcurrent() {
        return cbxStartConcurrent;
    }

    public void setCbxStartConcurrent(Checkbox c) {
        this.cbxStartConcurrent = c;
    }

    private Checkbox cbxReorderItems = new Checkbox();

    public Checkbox getCbxReorderItems() {
        return cbxReorderItems;
    }

    public void setCbxReorderItems(Checkbox c) {
        this.cbxReorderItems = c;
    }

    private Checkbox cbxViewAllOffered = new Checkbox();

    public Checkbox getCbxViewAllOffered() {
        return cbxViewAllOffered;
    }

    public void setCbxViewAllOffered(Checkbox c) {
        this.cbxViewAllOffered = c;
    }

    private Checkbox cbxViewAllAllocated = new Checkbox();

    public Checkbox getCbxViewAllAllocated() {
        return cbxViewAllAllocated;
    }

    public void setCbxViewAllAllocated(Checkbox c) {
        this.cbxViewAllAllocated = c;
    }

    private Checkbox cbxViewAllExecuting = new Checkbox();

    public Checkbox getCbxViewAllExecuting() {
        return cbxViewAllExecuting;
    }

    public void setCbxViewAllExecuting(Checkbox c) {
        this.cbxViewAllExecuting = c;
    }

    private Checkbox cbxViewTeamItems = new Checkbox();

    public Checkbox getCbxViewTeamItems() {
        return cbxViewTeamItems;
    }

    public void setCbxViewTeamItems(Checkbox c) {
        this.cbxViewTeamItems = c;
    }

    private Checkbox cbxViewOrgGroupItems = new Checkbox();

    public Checkbox getCbxViewOrgGroupItems() {
        return cbxViewOrgGroupItems;
    }

    public void setCbxViewOrgGroupItems(Checkbox c) {
        this.cbxViewOrgGroupItems = c;
    }

    private Checkbox cbxChainItems = new Checkbox();

    public Checkbox getCbxChainItems() {
        return cbxChainItems;
    }

    public void setCbxChainItems(Checkbox c) {
        this.cbxChainItems = c;
    }

    private Checkbox cbxManageCases = new Checkbox();

    public Checkbox getCbxManageCases() {
        return cbxManageCases;
    }

    public void setCbxManageCases(Checkbox c) {
        this.cbxManageCases = c;
    }

    private PanelLayout pnlUserDetails = new PanelLayout();

    public PanelLayout getPnlUserDetails() {
        return pnlUserDetails;
    }

    public void setPnlUserDetails(PanelLayout pl) {
        this.pnlUserDetails = pl;
    }

    private TextField txtFirstName = new TextField();

    public TextField getTxtFirstName() {
        return txtFirstName;
    }

    public void setTxtFirstName(TextField tf) {
        this.txtFirstName = tf;
    }

    private TextField txtLastName = new TextField();

    public TextField getTxtLastName() {
        return txtLastName;
    }

    public void setTxtLastName(TextField tf) {
        this.txtLastName = tf;
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

    private TextArea txtDesc = new TextArea();

    public TextArea getTxtDesc() {
        return txtDesc;
    }

    public void setTxtDesc(TextArea ta) {
        this.txtDesc = ta;
    }

    private TextArea txtNotes = new TextArea();

    public TextArea getTxtNotes() {
        return txtNotes;
    }

    public void setTxtNotes(TextArea ta) {
        this.txtNotes = ta;
    }

    private TabSet tabSetAttributes = new TabSet();

    public TabSet getTabSetAttributes() {
        return tabSetAttributes;
    }

    public void setTabSetAttributes(TabSet ts) {
        this.tabSetAttributes = ts;
    }

    private Tab tabRoles = new Tab();

    public Tab getTabRoles() {
        return tabRoles;
    }

    public void setTabRoles(Tab t) {
        this.tabRoles = t;
    }

    private PanelLayout tabPanelRole = new PanelLayout();

    public PanelLayout getTabPanelRole() {
        return tabPanelRole;
    }

    public void setTabPanelRole(PanelLayout pl) {
        this.tabPanelRole = pl;
    }

    private Tab tabPosition = new Tab();

    public Tab getTabPosition() {
        return tabPosition;
    }

    public void setTabPosition(Tab t) {
        this.tabPosition = t;
    }

    private PanelLayout tabPanelPosition = new PanelLayout();

    public PanelLayout getTabPanelPosition() {
        return tabPanelPosition;
    }

    public void setTabPanelPosition(PanelLayout pl) {
        this.tabPanelPosition = pl;
    }

    private Tab tabCapability = new Tab();

    public Tab getTabCapability() {
        return tabCapability;
    }

    public void setTabCapability(Tab t) {
        this.tabCapability = t;
    }

    private PanelLayout tabPanelCapability = new PanelLayout();

    public PanelLayout getTabPanelCapability() {
        return tabPanelCapability;
    }

    public void setTabPanelCapability(PanelLayout pl) {
        this.tabPanelCapability = pl;
    }

    private PanelLayout pnlSelectUser = new PanelLayout();

    public PanelLayout getPnlSelectUser() {
        return pnlSelectUser;
    }

    public void setPnlSelectUser(PanelLayout pl) {
        this.pnlSelectUser = pl;
    }

    private PanelLayout pnlNewPassword = new PanelLayout();

    public PanelLayout getPnlNewPassword() {
        return pnlNewPassword;
    }

    public void setPnlNewPassword(PanelLayout pl) {
        this.pnlNewPassword = pl;
    }


    private DropDown cbbParticipants = new DropDown();

    public DropDown getCbbParticipants() {
        return cbbParticipants;
    }

    public void setCbbParticipants(DropDown dd) {
        this.cbbParticipants = dd;
    }

    private SingleSelectOptionsList cbbParticipantsDefaultOptions = new SingleSelectOptionsList();

    public SingleSelectOptionsList getCbbParticipantsDefaultOptions() {
        return cbbParticipantsDefaultOptions;
    }

    public void setCbbParticipantsDefaultOptions(SingleSelectOptionsList ssol) {
        this.cbbParticipantsDefaultOptions = ssol;
    }

    private Label label1 = new Label();

    public Label getLabel1() {
        return label1;
    }

    public void setLabel1(Label l) {
        this.label1 = l;
    }

    private Button btnSave = new Button();

    public Button getBtnSave() {
        return btnSave;
    }

    public void setBtnSave(Button b) {
        this.btnSave = b;
    }

    private Button btnReset = new Button();

    public Button getBtnReset() {
        return btnReset;
    }

    public void setBtnReset(Button b) {
        this.btnReset = b;
    }

    private Button btnClose = new Button();

    public Button getBtnClose() {
        return btnClose;
    }

    public void setBtnClose(Button b) {
        this.btnClose = b;
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

    // </editor-fold>


    /** 
     * <p>Construct a new Page bean instance.</p>
     */
    public participantData() {
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
        // a null tooltip indicates the first rendering of this page
        if (btnAdd.getToolTip() == null) {
            setMode(Mode.edit);
            btnSave.setDisabled(true);
            btnRemove.setDisabled(true);
            btnReset.setDisabled(true);
        }
    }

    /** 
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called (regardless of whether
     * or not this was the page that was actually rendered).  Customize this
     * method to release resources acquired in the <code>init()</code>,
     * <code>preprocess()</code>, or <code>prerender()</code> methods (or
     * acquired during execution of an event handler).</p>
     */
    public void destroy() {
    }

    private enum Mode {add, edit}

    public String btnSave_action() {
        if (checkValidPasswordChange()) {
            Participant p = getSessionBean().getEditedParticipant() ;
            boolean nameChange = ! ((String) txtLastName.getText()).equals(p.getLastName());
            saveChanges(p);
            getSessionBean().saveParticipantUpdates(p);
            if (nameChange) getSessionBean().refreshOrgDataParticipantList();
            info("INFO: Participant changes successfully saved.");
        }
        return null;
    }


    public String btnReset_action() {

        // if in 'add new' mode, discard inputs and go back to edit mode
        if (btnAdd.getText().equals("Add")) {
            setMode(Mode.edit);
        }
        else {
            // if already in edit mode, discard edits
            Participant p = getSessionBean().resetParticipant();
            populateFields(p) ;
        }
        return null;   
    }


    public String btnRemove_action() {
        Participant p = getSessionBean().getEditedParticipant() ;
        getSessionBean().removeParticipant(p);
        cbbParticipants.setSelected("");
        clearFields();
        btnSave.setDisabled(true);
        btnRemove.setDisabled(true);
        btnReset.setDisabled(true);
        info("INFO: Chosen participant successfully removed.");
        return null;
    }


    public String btnAdd_action() {
        // if 'new', we're in edit mode - move to add mode
        if (btnAdd.getText().equals("New")) {
            setMode(Mode.add) ;
        }
        else {
            // we're in add mode - add new participant and go back to edit mode
            if (validateNewData()) {
                Participant p = createParticipant();
                String newID = getSessionBean().addParticipant(p);
                cbbParticipants.setSelected(newID);
                setMode(Mode.edit);
                info("INFO: New participant added successfully.");
            }
        }
        return null;
    }

    private void setMode(Mode mode) {
        if (mode == Mode.add) {
            btnAdd.setText("Add");
            btnAdd.setToolTip("Save input data to create a new participant");
            btnReset.setToolTip("Discard data and revert to edit mode");
            btnSave.setDisabled(true);
            btnRemove.setDisabled(true);
            btnReset.setDisabled(false);
            clearFields();
            cbbParticipants.setSelected("");
            cbbParticipants.setDisabled(true);
            ((pfAddRemove) getBean("pfAddRemove")).clearOwnsList();
            ((pfAddRemove) getBean("pfAddRemove")).populateAvailableList();
            getSessionBean().setAddParticipantMode(true);
            getSessionBean().setAddedParticipant(new Participant());
        }
        else {   // edit mode
            btnAdd.setText("New");
            btnAdd.setToolTip("Add a new participant");
            btnReset.setToolTip("Discard changes for the current participant");
            btnSave.setDisabled(false);
            btnRemove.setDisabled(false);
            btnReset.setDisabled(false);
            cbbParticipants.setDisabled(false);
            ((pfAddRemove) getBean("pfAddRemove")).clearLists();            
            getSessionBean().setAddParticipantMode(false);
            getSessionBean().setAddedParticipant(null);
        }
    }

    public String tabRoles_action() {
        Participant p = getSessionBean().getParticipantForCurrentMode() ;
        ((pfAddRemove) getBean("pfAddRemove")).populateLists("tabRoles", p);
        getSessionBean().setActiveResourceAttributeTab("tabRoles") ;
        return null;
    }


    public String tabPosition_action() {
        Participant p = getSessionBean().getParticipantForCurrentMode() ;
        ((pfAddRemove) getBean("pfAddRemove")).populateLists("tabPosition", p);
        getSessionBean().setActiveResourceAttributeTab("tabPosition") ;
        return null;
    }


    public String tabCapability_action() {
        Participant p = getSessionBean().getParticipantForCurrentMode() ;
        ((pfAddRemove) getBean("pfAddRemove")).populateLists("tabCapability", p);
        getSessionBean().setActiveResourceAttributeTab("tabCapability") ;
        return null;
    }


    public void cbbParticipants_processValueChange(ValueChangeEvent event) {
        String pid = (String) event.getNewValue();
        if (pid.length() > 0) {
            Participant p = getSessionBean().setEditedParticipant(pid);
            populateFields(p) ;
            setMode(Mode.edit);
        }
        else {
            clearFields();                    // blank (first) option selected
            btnSave.setDisabled(true);
            btnRemove.setDisabled(true);
            btnReset.setDisabled(true);
        }
    }

    
    private void populateFields(Participant p) {

        // set simple fields
        txtFirstName.setText(p.getFirstName());
        txtLastName.setText(p.getLastName());
        txtUserID.setText(p.getUserID());
        txtDesc.setText(p.getDescription());
        txtNotes.setText(p.getNotes());
        cbxAdmin.setValue(p.isAdministrator());

        // clear any leftover passwords
        txtNewPassword.setPassword("");
        txtConfirmPassword.setPassword("");
        
        // set privileges
        UserPrivileges up = p.getUserPrivileges();
        cbxChooseItemToStart.setValue(up.canChooseItemToStart());
        cbxChainItems.setValue(up.canChainExecution());
        cbxManageCases.setValue(up.canManageCases());
        cbxReorderItems.setValue(up.canReorder());
        cbxStartConcurrent.setValue(up.canStartConcurrent());
        cbxViewAllAllocated.setValue(up.canViewAllAllocated());
        cbxViewAllExecuting.setValue(up.canViewAllExecuting());
        cbxViewAllOffered.setValue(up.canViewAllOffered());
        cbxViewOrgGroupItems.setValue(up.canViewOrgGroupItems());
        cbxViewTeamItems.setValue(up.canViewTeamItems());
        
        // set Resource Attributes
        ((pfAddRemove) getBean("pfAddRemove"))
                .populateLists(tabSetAttributes.getSelected(), p);
    }


    private void clearFields() {

        // clear simple fields
        txtFirstName.setText("");
        txtLastName.setText("");
        txtUserID.setText("");
        txtDesc.setText("");
        txtNotes.setText("");
        cbxAdmin.setValue("");
        txtNewPassword.setPassword("");
        txtConfirmPassword.setPassword("");

        // clear privileges
        cbxChooseItemToStart.setSelected(false);
        cbxChainItems.setSelected(false);
        cbxManageCases.setSelected(false);
        cbxReorderItems.setSelected(false);
        cbxStartConcurrent.setSelected(false);
        cbxViewAllAllocated.setSelected(false);
        cbxViewAllExecuting.setSelected(false);
        cbxViewAllOffered.setSelected(false);
        cbxViewOrgGroupItems.setSelected(false);
        cbxViewTeamItems.setSelected(false);

        // clear Resource Attributes
        ((pfAddRemove) getBean("pfAddRemove")).clearLists();
    }


    private void saveChanges(Participant p) {

        // update fields     
        p.setFirstName((String) txtFirstName.getText());
        p.setLastName((String) txtLastName.getText());
        p.setUserID((String) txtUserID.getText());
        p.setDescription((String) txtDesc.getText());
        p.setNotes((String) txtNotes.getText());
        p.setAdministrator((Boolean) cbxAdmin.getValue());

        // only change password if a new one is entered and after its been validated
        String password = (String) txtNewPassword.getPassword();
        if (password.length() > 0) p.setPassword(password);
        txtNewPassword.setPassword("");
        txtConfirmPassword.setPassword("");

        // set privileges
        UserPrivileges up = p.getUserPrivileges();
        up.setCanChooseItemToStart((Boolean) cbxChooseItemToStart.getValue());
        up.setCanChainExecution((Boolean) cbxChainItems.getValue());
        up.setCanManageCases((Boolean) cbxManageCases.getValue());
        up.setCanReorder((Boolean) cbxReorderItems.getValue());
        up.setCanStartConcurrent((Boolean) cbxStartConcurrent.getValue());
        up.setCanViewAllAllocated((Boolean) cbxViewAllAllocated.getValue());
        up.setCanViewAllExecuting((Boolean) cbxViewAllExecuting.getValue());
        up.setCanViewAllOffered((Boolean) cbxViewAllOffered.getValue());
        up.setCanViewOrgGroupItems((Boolean) cbxViewOrgGroupItems.getValue());
        up.setCanViewTeamItems((Boolean) cbxViewTeamItems.getValue());
    }


    private boolean checkValidPasswordChange() {
        String password = (String) txtNewPassword.getPassword();
        if (password.length() > 0) {
            if (password.indexOf(" ") > -1)
                return false;                // no spaces allowed
            String confirm = (String) txtConfirmPassword.getPassword();
            return password.equals(confirm);
        }
        return true ;
    }


    private Participant createParticipant() {
        Participant p = new Participant(true);
        Participant temp = getSessionBean().getAddedParticipant();
        saveChanges(p);
        p.setRoles(temp.getRoles());
        p.setPositions(temp.getPositions());
        p.setCapabilities(temp.getCapabilities());
        return p;
    }

    private boolean validateNewData() {

        // all required text inputs there?
        boolean result = checkForRequiredValues();

        // unique id?
        if (hasText(txtUserID)) {
            if (! getSessionBean().isUniqueUserID((String) txtUserID.getText())) {
                error("ERROR: That User ID is already in use - please try another");
                result = false;
            }    
        }

        // password check
        if (! checkValidPasswordChange()) {
            error("ERROR: Password and comfirmation are different");
            result = false;
        }

        // warn if no attributes
        Participant p = getSessionBean().getAddedParticipant();
        if (p.getRoles().isEmpty())
            warn("WARNING: No role specified for participant") ;
        if (p.getPositions().isEmpty())
            warn("WARNING: No position specified for participant") ;
        if (p.getCapabilities().isEmpty())
            warn("WARNING: No capability specified for participant") ;

        return result ;
    }


    private boolean checkForRequiredValues() {
        boolean result = true;
        if (! hasText(txtLastName)) {
            error("ERROR: A last name is required");
            result = false;
        }
        if (! hasText(txtFirstName)) {
            error("ERROR: A first name is required");
            result = false;
        }
        if (! hasText(txtUserID)) {
            error("ERROR: A userid is required");
            result = false;
        }
        if (! hasText(txtNewPassword)) {
            error("ERROR: A password is required");
            result = false;
        }
        if (! hasText(txtConfirmPassword)) {
            error("ERROR: A 'confirm' password is required");
            result = false;
        }
        return result ;
    }

    private boolean hasText(TextField field) {
        return ((String) field.getText()).length() > 0 ;
    }

    private boolean hasText(PasswordField field) {
        return ((String) field.getPassword()).length() > 0 ;
    }

}

