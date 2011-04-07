/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.UserPrivileges;

import javax.faces.FacesException;
import javax.faces.event.ValueChangeEvent;

/**
 *  Backing bean for the participant data or 'User Mgt' form.
 *
 *  @author Michael Adams
 *  Date: 26/01/2008
 *  Last Date: 09/05/2008
 */

public class participantData extends AbstractPageBean {

    // REQUIRED AND/OR IMPLEMENTED ABSTRACT PAGE BEAN METHODS //

    private int __placeholder;

    private void _init() throws Exception { }

    public void init() {
        super.init();

        // *Note* - this code should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("participantData Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void destroy() { }

    public void preprocess() { }



    public participantData() { }

    // Return references to scoped data beans //
     protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }


    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }


    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }
    

    /********************************************************************************/

    // PAGE COMPONENT DECLARATIONS, GETTERS & SETTERS //

    private Page page1 = new Page();
    
    public Page getPage1() { return page1; }
    
    public void setPage1(Page p) { page1 = p; }


    private Html html1 = new Html();
    
    public Html getHtml1() { return html1; }
    
    public void setHtml1(Html h) { html1 = h; }


    private Head head1 = new Head();
    
    public Head getHead1() { return head1; }
    
    public void setHead1(Head h) { head1 = h; }


    private Link link1 = new Link();
    
    public Link getLink1() { return link1; }
    
    public void setLink1(Link l) { link1 = l; }
    

    private Body body1 = new Body();
    
    public Body getBody1() { return body1; }
    
    public void setBody1(Body b) { body1 = b; }
    

    private Form form1 = new Form();
    
    public Form getForm1() { return form1; }
    
    public void setForm1(Form f) { form1 = f; }


    private PanelLayout pnlPrivileges = new PanelLayout();

    public PanelLayout getPnlPrivileges() { return pnlPrivileges; }

    public void setPnlPrivileges(PanelLayout pl) { pnlPrivileges = pl; }


    private StaticText staticText1 = new StaticText();

    public StaticText getStaticText1() { return staticText1; }

    public void setStaticText1(StaticText st) { staticText1 = st; }


    private StaticText sttPassword = new StaticText();

    public StaticText getSttPassword() { return sttPassword; }

    public void setSttPassword(StaticText st) { sttPassword = st; }


    private PasswordField txtNewPassword ;

    public PasswordField getTxtNewPassword() { return txtNewPassword; }

    public void setTxtNewPassword(PasswordField pw) { txtNewPassword = pw; }


    private PasswordField txtConfirmPassword ;

    public PasswordField getTxtConfirmPassword() { return txtConfirmPassword; }

    public void setTxtConfirmPassword(PasswordField pw) { txtConfirmPassword = pw; }


    private Label lblFirstName = new Label();

    public Label getLblFirstName() { return lblFirstName; }

    public void setLblFirstName(Label l) { lblFirstName = l; }


    private Label lblUserID = new Label();

    public Label getLblUserID() { return lblUserID; }

    public void setLblUserID(Label l) { lblUserID = l; }


    private Label lblLastName = new Label();

    public Label getLblLastName() { return lblLastName; }

    public void setLblLastName(Label l) { lblLastName = l; }

    
    private Label lblDesc = new Label();

    public Label getLblDesc() { return lblDesc; }

    public void setLblDesc(Label l) { lblDesc = l; }


    private Label lblNotes = new Label();

    public Label getLblNotes() { return lblNotes; }

    public void setLblNotes(Label l) { lblNotes = l; }


    private Label lblPassword = new Label();

    public Label getLblPassword() { return lblPassword; }

    public void setLblPassword(Label l) { lblPassword = l; }


    private Label lblConfirm = new Label();

    public Label getLblConfirm() { return lblConfirm; }

    public void setLblConfirm(Label l) { lblConfirm = l; }

    
    private Checkbox cbxChooseItemToStart = new Checkbox();

    public Checkbox getCbxChooseItemToStart() { return cbxChooseItemToStart; }

    public void setCbxChooseItemToStart(Checkbox c) { cbxChooseItemToStart = c; }


    private Checkbox cbxStartConcurrent = new Checkbox();

    public Checkbox getCbxStartConcurrent() { return cbxStartConcurrent; }

    public void setCbxStartConcurrent(Checkbox c) { cbxStartConcurrent = c; }


    private Checkbox cbxReorderItems = new Checkbox();

    public Checkbox getCbxReorderItems() { return cbxReorderItems; }

    public void setCbxReorderItems(Checkbox c) { cbxReorderItems = c; }


//    private Checkbox cbxViewAllOffered = new Checkbox();
//
//    public Checkbox getCbxViewAllOffered() { return cbxViewAllOffered; }
//
//    public void setCbxViewAllOffered(Checkbox c) { cbxViewAllOffered = c; }
//
//
//    private Checkbox cbxViewAllAllocated = new Checkbox();
//
//    public Checkbox getCbxViewAllAllocated() { return cbxViewAllAllocated; }
//
//    public void setCbxViewAllAllocated(Checkbox c) { cbxViewAllAllocated = c; }
//
//
//    private Checkbox cbxViewAllExecuting = new Checkbox();
//
//    public Checkbox getCbxViewAllExecuting() { return cbxViewAllExecuting; }
//
//    public void setCbxViewAllExecuting(Checkbox c) { cbxViewAllExecuting = c; }


    private Checkbox cbxViewTeamItems = new Checkbox();

    public Checkbox getCbxViewTeamItems() { return cbxViewTeamItems; }

    public void setCbxViewTeamItems(Checkbox c) { cbxViewTeamItems = c; }


    private Checkbox cbxViewOrgGroupItems = new Checkbox();

    public Checkbox getCbxViewOrgGroupItems() { return cbxViewOrgGroupItems; }

    public void setCbxViewOrgGroupItems(Checkbox c) { cbxViewOrgGroupItems = c; }


    private Checkbox cbxChainItems = new Checkbox();

    public Checkbox getCbxChainItems() { return cbxChainItems; }

    public void setCbxChainItems(Checkbox c) { cbxChainItems = c; }


    private Checkbox cbxManageCases = new Checkbox();

    public Checkbox getCbxManageCases() { return cbxManageCases; }

    public void setCbxManageCases(Checkbox c) { cbxManageCases = c; }


    private PanelLayout pnlUserDetails = new PanelLayout();

    public PanelLayout getPnlUserDetails() { return pnlUserDetails; }

    public void setPnlUserDetails(PanelLayout pl) { pnlUserDetails = pl; }


    private TextField txtFirstName = new TextField();

    public TextField getTxtFirstName() { return txtFirstName; }

    public void setTxtFirstName(TextField tf) { txtFirstName = tf; }


    private TextField txtLastName = new TextField();

    public TextField getTxtLastName() { return txtLastName; }

    public void setTxtLastName(TextField tf) { txtLastName = tf; }


    private TextField txtUserID = new TextField();

    public TextField getTxtUserID() { return txtUserID; }

    public void setTxtUserID(TextField tf) { txtUserID = tf; }


    private Checkbox cbxAdmin = new Checkbox();

    public Checkbox getCbxAdmin() { return cbxAdmin; }

    public void setCbxAdmin(Checkbox c) { cbxAdmin = c; }


    private TextArea txtDesc = new TextArea();

    public TextArea getTxtDesc() { return txtDesc; }

    public void setTxtDesc(TextArea ta) { txtDesc = ta; }


    private TextArea txtNotes = new TextArea();

    public TextArea getTxtNotes() { return txtNotes; }

    public void setTxtNotes(TextArea ta) { txtNotes = ta; }


    private TabSet tabSetAttributes = new TabSet();

    public TabSet getTabSetAttributes() { return tabSetAttributes; }

    public void setTabSetAttributes(TabSet ts) { tabSetAttributes = ts; }


    private Tab tabRoles = new Tab();

    public Tab getTabRoles() { return tabRoles; }

    public void setTabRoles(Tab t) { tabRoles = t; }


    private PanelLayout tabPanelRole = new PanelLayout();

    public PanelLayout getTabPanelRole() { return tabPanelRole; }

    public void setTabPanelRole(PanelLayout pl) { tabPanelRole = pl; }


    private Tab tabPosition = new Tab();

    public Tab getTabPosition() { return tabPosition; }

    public void setTabPosition(Tab t) { tabPosition = t; }


    private PanelLayout tabPanelPosition = new PanelLayout();

    public PanelLayout getTabPanelPosition() { return tabPanelPosition; }

    public void setTabPanelPosition(PanelLayout pl) { tabPanelPosition = pl; }


    private Tab tabCapability = new Tab();

    public Tab getTabCapability() { return tabCapability; }

    public void setTabCapability(Tab t) { tabCapability = t; }


    private PanelLayout tabPanelCapability = new PanelLayout();

    public PanelLayout getTabPanelCapability() { return tabPanelCapability; }

    public void setTabPanelCapability(PanelLayout pl) { tabPanelCapability = pl; }


    private PanelLayout pnlSelectUser = new PanelLayout();

    public PanelLayout getPnlSelectUser() { return pnlSelectUser; }

    public void setPnlSelectUser(PanelLayout pl) { pnlSelectUser = pl; }


    private PanelLayout pnlNewPassword = new PanelLayout();

    public PanelLayout getPnlNewPassword() { return pnlNewPassword; }

    public void setPnlNewPassword(PanelLayout pl) { pnlNewPassword = pl; }


    private DropDown cbbParticipants = new DropDown();

    public DropDown getCbbParticipants() { return cbbParticipants; }

    public void setCbbParticipants(DropDown dd) { cbbParticipants = dd; }


    private Label label1 = new Label();

    public Label getLabel1() { return label1; }

    public void setLabel1(Label l) { label1 = l; }


    private Button btnSave = new Button();

    public Button getBtnSave() { return btnSave; }

    public void setBtnSave(Button b) { btnSave = b; }


    private Button btnReset = new Button();

    public Button getBtnReset() { return btnReset; }

    public void setBtnReset(Button b) { btnReset = b; }


    private Button btnClose = new Button();

    public Button getBtnClose() { return btnClose; }

    public void setBtnClose(Button b) { btnClose = b; }


    private Button btnRemove = new Button();

    public Button getBtnRemove() { return btnRemove; }

    public void setBtnRemove(Button b) { btnRemove = b; }


    private Button btnAdd = new Button();

    public Button getBtnAdd() { return btnAdd; }

    public void setBtnAdd(Button b) { btnAdd = b; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }

    

    /********************************************************************************/

    // SPECIFIC DELARATIONS AND IMPLEMENTATION //

    private enum Mode {edit, add}

    private SessionBean _sb = getSessionBean();
    private MessagePanel msgPanel = _sb.getMessagePanel() ;
    

    // This method is called immediately before the page is rendered
    public void prerender() {
        _sb.checkLogon();                // check if session still active
        _sb.setActivePage(ApplicationBean.PageRef.participantData);
        _sb.showMessagePanel();

        // a null btnAdd tooltip indicates the first rendering of this page
        if (btnAdd.getToolTip() == null) {
            setMode(Mode.edit);
            _sb.setActiveResourceAttributeTab("tabRoles") ;
        }

        // prime the resources listboxes
        Participant p;
        if (getCurrentMode() == Mode.edit) {
            p = _sb.getEditedParticipant();
            if (p != null) {
                String selTab = _sb.getActiveResourceAttributeTab() ;
                if (selTab == null) selTab = "tabRoles";
                _sb.getFullResourceAttributeList(selTab) ;
                _sb.getParticipantAttributeList(selTab, p);
                enableFields(true);
            }
            else {
                clearFields();
                cbbParticipants.setSelected("");
                _sb.setAvailableResourceAttributes(null);
                _sb.setOwnedResourceAttributes(null);
                enableFields(false);
            }
        }
        else {
            enableFields(true);
        }

        // set active page and show any pending messages to user
        _sb.setBlankStartOfParticipantList(true);
    }


    // respond to a 'save' button click
    public String btnSave_action() {
        Participant p = _sb.getEditedParticipant() ;
        if (checkValidPasswordChange(true) && (checkValidUserID(p, true))) {
            boolean nameChange = ! (txtLastName.getText()).equals(p.getLastName());
            saveChanges(p);
            try {
                _sb.saveParticipantUpdates(p);
                if (nameChange) _sb.refreshOrgDataParticipantList();
                msgPanel.success("Participant changes successfully saved.");
            }
            catch (CloneNotSupportedException cnse) {
                msgPanel.error("Could not save changes: cloning Exception");
            }
        }
        return null;
    }


    // discards edits/iputs, and resets to original values
    public String btnReset_action() {

        // if in 'add new' mode, discard inputs and go back to edit mode
        if (getCurrentMode() == Mode.add) {
            clearFields();
            setMode(Mode.edit);
        }
        else {
            // if already in edit mode, discard edits
            try {
                Participant p = _sb.resetParticipant();
                populateFields(p) ;
            }
            catch (CloneNotSupportedException cnse) {
                msgPanel.error("Could not reset changes: cloning Exception");
            }
        }
        return null;   
    }


    // delete the selected participant
    public String btnRemove_action() {
        Participant p = _sb.getEditedParticipant() ;
        if (p != null) {
            _sb.removeParticipant(p);
            cbbParticipants.setSelected("");
            clearFields();
            setMode(Mode.edit);
            msgPanel.success("Chosen participant successfully removed.");
        }
        return null;    
    }


    public String btnAdd_action() {
        // if 'new', we're in edit mode - move to add mode
        if (getCurrentMode() == Mode.edit) {
            setMode(Mode.add) ;
        }
        else {
            // we're in add mode - add new participant and go back to edit mode
            if (validateNewData()) {
                try {
                    Participant p = createParticipant();
                    String newID = _sb.addParticipant(p);
                    cbbParticipants.setSelected(newID);
                    setMode(Mode.edit);
                    msgPanel.success("New participant added successfully.");
                }
                catch (CloneNotSupportedException cnse) {
                    msgPanel.error("Could not add participant: cloning Exception");
                }
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
            tabSetAttributes.setSelected("tabRoles");
            _sb.setActiveResourceAttributeTab("tabRoles") ;
            ((pfAddRemove) getBean("pfAddRemove")).clearOwnsList();
            ((pfAddRemove) getBean("pfAddRemove")).populateAvailableList();
            _sb.setAddParticipantMode(true);
            _sb.setAddedParticipant(new Participant());
            _sb.setEditedParticipantToNull();
        }
        else {   // edit mode
            btnAdd.setText("New");
            btnAdd.setToolTip("Add a new participant");
            btnReset.setToolTip("Discard changes for the current participant");
            boolean participantIsNull = (_sb.getEditedParticipant() == null);
            btnSave.setDisabled(participantIsNull);
            btnRemove.setDisabled(participantIsNull);
            btnReset.setDisabled(participantIsNull);
            cbbParticipants.setDisabled(false);
            ((pfAddRemove) getBean("pfAddRemove")).clearLists();            
            _sb.setAddParticipantMode(false);
            _sb.setAddedParticipant(null);
        }
    }

    private Mode getCurrentMode() {
        return _sb.isAddParticipantMode() ? Mode.add : Mode.edit ;
    }

    public String tabRoles_action() {
        Participant p = _sb.getParticipantForCurrentMode() ;
        ((pfAddRemove) getBean("pfAddRemove")).populateLists("tabRoles", p);
        _sb.setActiveResourceAttributeTab("tabRoles") ;
        return null;
    }


    public String tabPosition_action() {
        Participant p = _sb.getParticipantForCurrentMode() ;
        ((pfAddRemove) getBean("pfAddRemove")).populateLists("tabPosition", p);
        _sb.setActiveResourceAttributeTab("tabPosition") ;
        return null;
    }


    public String tabCapability_action() {
        Participant p = _sb.getParticipantForCurrentMode() ;
        ((pfAddRemove) getBean("pfAddRemove")).populateLists("tabCapability", p);
        _sb.setActiveResourceAttributeTab("tabCapability") ;
        return null;
    }


    public void cbbParticipants_processValueChange(ValueChangeEvent event) {
        String pid = (String) event.getNewValue();
        if (pid.length() > 0) {
            try {
                Participant p = _sb.setEditedParticipant(pid);
                populateFields(p) ;
            }
            catch (CloneNotSupportedException cnse) {
                msgPanel.error("Could not change selected Participant: cloning Exception");
            }
        }
        else {
            clearFields();                    // blank (first) option selected
            _sb.setEditedParticipantToNull() ;
        }
        setMode(Mode.edit);
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
        if (txtNewPassword != null) txtNewPassword.setPassword("");
        if (txtConfirmPassword != null) txtConfirmPassword.setPassword("");

        // clear privileges
        cbxChooseItemToStart.setSelected(false);
        cbxChainItems.setSelected(false);
        cbxManageCases.setSelected(false);
        cbxReorderItems.setSelected(false);
        cbxStartConcurrent.setSelected(false);
        cbxViewOrgGroupItems.setSelected(false);
        cbxViewTeamItems.setSelected(false);

        // clear Resource Attributes
        ((pfAddRemove) getBean("pfAddRemove")).clearLists();
    }


    private void enableFields(boolean enabled) {

        // only do it if a change is required
        if (txtFirstName.isDisabled() == enabled) {

            // enable simple fields
            txtFirstName.setDisabled(!enabled);
            txtLastName.setDisabled(!enabled);
            txtUserID.setDisabled(!enabled);
            txtDesc.setDisabled(!enabled);
            txtNotes.setDisabled(!enabled);
            cbxAdmin.setDisabled(!enabled);
            if (txtNewPassword != null) {
                txtNewPassword.setDisabled(!enabled);
                txtConfirmPassword.setDisabled(!enabled);
            }

            // enable privileges
            cbxChooseItemToStart.setDisabled(!enabled);
            cbxChainItems.setDisabled(!enabled);
            cbxManageCases.setDisabled(!enabled);
            cbxReorderItems.setDisabled(!enabled);
            cbxStartConcurrent.setDisabled(!enabled);
            cbxViewOrgGroupItems.setDisabled(!enabled);
            cbxViewTeamItems.setDisabled(!enabled);

            // enable Resource Attributes
            ((pfAddRemove) getBean("pfAddRemove")).enableFields(enabled);
        }
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
        if (password.length() > 0) {
            try {
                p.setPassword(password, true);
            }
            catch (Exception e) {
                msgPanel.warn("Could not change password - encryption service unavailable.");
            }
        }
        txtNewPassword.setPassword("");
        txtConfirmPassword.setPassword("");

        // set privileges
        UserPrivileges up = p.getUserPrivileges();
        up.setCanChooseItemToStart((Boolean) cbxChooseItemToStart.getValue());
        up.setCanChainExecution((Boolean) cbxChainItems.getValue());
        up.setCanManageCases((Boolean) cbxManageCases.getValue());
        up.setCanReorder((Boolean) cbxReorderItems.getValue());
        up.setCanStartConcurrent((Boolean) cbxStartConcurrent.getValue());
        up.setCanViewOrgGroupItems((Boolean) cbxViewOrgGroupItems.getValue());
        up.setCanViewTeamItems((Boolean) cbxViewTeamItems.getValue());
    }


    /** @return true if the password is valid */
    private boolean checkValidPasswordChange(boolean updating) {
        boolean result = true ;
        String password = (String) txtNewPassword.getPassword();
        String confirm = (String) txtConfirmPassword.getPassword();

        // if this is an update an no changes are made, no validation necessary
        if (updating && (password.length() == 0) && (confirm.length() == 0))
            return result ;

        String errMsg = getApplicationBean().checkPassword(password, confirm);
        if (errMsg != null) {
            msgPanel.error(errMsg);
        }
        return (errMsg == null) ;
    }


    private Participant createParticipant() {
        Participant p = new Participant(true);
        Participant temp = _sb.getAddedParticipant();
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
        if (! checkValidUserID(null, false))
            result = false;

        // password check
        if (! checkValidPasswordChange(false))
            result = false;

        // warn if no attributes
        Participant p = _sb.getAddedParticipant();
        if (p.getRoles().isEmpty())
            msgPanel.warn("No role specified for participant.") ;
        if (p.getPositions().isEmpty())
            msgPanel.warn("No position specified for participant.") ;
        if (p.getCapabilities().isEmpty())
            msgPanel.warn("No capability specified for participant.") ;

        return result ;
    }


    private boolean checkForRequiredValues() {
        boolean result = true;
        if (! hasText(txtLastName)) {
            msgPanel.error("A last name is required.");
            result = false;
        }
        if (! hasText(txtFirstName)) {
            msgPanel.error("A first name is required.");
            result = false;
        }
        if (! hasText(txtUserID)) {
            msgPanel.error("A userid is required.");
            result = false;
        }
        if (! hasText(txtNewPassword)) {
            msgPanel.error("A password is required.");
            result = false;
        }
        if (! hasText(txtConfirmPassword)) {
            msgPanel.error("A 'confirm' password is required.");
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


    private boolean checkValidUserID(Participant p, boolean updating) {
        boolean result = true;
        if (hasText(txtUserID)) {
            String newUserID = (String) txtUserID.getText();
            if (newUserID.equalsIgnoreCase("admin")) {
                msgPanel.error("'admin' is a reserved User ID - please try another.");
                result = false;
            }
            else {
                boolean modified = (! updating) || (! p.getUserID().equals(newUserID)) ;
                if (modified && (! getApplicationBean().isUniqueUserID(newUserID))) {
                    msgPanel.error("That User ID is already in use - please try another.");
                    result = false;
                }
            }
        }
        else {
            msgPanel.error("Please supply a userid.");
            result = false; 
        }
        return result;
    }

}

