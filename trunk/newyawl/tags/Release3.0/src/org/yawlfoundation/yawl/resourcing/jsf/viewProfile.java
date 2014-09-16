/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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
import com.sun.rave.web.ui.model.Option;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.ResourceMap;
import org.yawlfoundation.yawl.resourcing.resource.Capability;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Position;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import javax.faces.FacesException;
import java.util.Set;

/**
 * Backing bean for edit profile form
 *
 * @author Michael Adams
 *
 * Date: 31/01/2008
 * Last Date: 16/03/2008
 */

public class viewProfile extends AbstractPageBean {
    private int __placeholder;

    private void _init() throws Exception { }

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


    public void init() {
        super.init();
        try {
            _init();
        } catch (Exception e) {
            log("participantData Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void preprocess() { }

    /**************************************************************************/

    private final SessionBean _sb = getSessionBean();
    private final MessagePanel msgPanel = _sb.getMessagePanel() ;
    private final Participant participant = _sb.getParticipant();
    private final ResourceManager rm = getApplicationBean().getResourceManager();


    public void prerender() {
        _sb.checkLogon();
        _sb.setActivePage(ApplicationBean.PageRef.viewProfile);

        // abort load if org data isn't currently available
        if (_sb.orgDataIsRefreshing()) return;

        showMessagePanel();
        populateFields(participant);
    }

    public void destroy() { }



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
            String caseID = chained.substring(0, chained.indexOf("::"));
            rm.removeChain(caseID) ;
        }    
        return null;
    }


    public String btnUnpile_action() {
        String selected = (String) lbxPiled.getSelected();
        if (selected != null) {
            ResourceMap selectedMap = _sb.getResourceMapFromLabel(selected);
            if (selectedMap != null) {
                String result = rm.unpileTask(selectedMap, participant) ;
                showResult(result);
            }
            else msgPanel.error("Failed to unpile task - could not load piled mappings.");
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

            // set buttons
            Option[] piledTasks = _sb.getPiledTasks();
            Option[] chainedCases = _sb.getChainedCases();
            btnUnpile.setDisabled((piledTasks == null) || (piledTasks.length == 0));
            btnUnchain.setDisabled((chainedCases == null) || (chainedCases.length == 0));
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


    private void showMessagePanel() {
        body1.setFocus(msgPanel.hasMessage() ? "form1:pfMsgPanel:btnOK001" :
                "form1:txtNewPassword");
        _sb.showMessagePanel();
    }
    
}
