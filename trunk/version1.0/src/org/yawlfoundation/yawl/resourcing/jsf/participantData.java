/*
 * participantData.java
 *
 * Created on 26 January 2008, 19:35
 * Copyright adamsmj
 */
package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.Body;
import com.sun.rave.web.ui.component.Form;
import com.sun.rave.web.ui.component.Head;
import com.sun.rave.web.ui.component.Html;
import com.sun.rave.web.ui.component.Link;
import com.sun.rave.web.ui.component.Page;
import javax.faces.FacesException;
import com.sun.rave.web.ui.component.PanelLayout;
import com.sun.rave.web.ui.component.StaticText;
import com.sun.rave.web.ui.component.Checkbox;
import javax.faces.event.ValueChangeEvent;
import com.sun.rave.web.ui.component.TextField;
import com.sun.rave.web.ui.component.TextArea;
import com.sun.rave.web.ui.component.TabSet;
import com.sun.rave.web.ui.component.Tab;
import com.sun.rave.web.ui.component.DropDown;
import com.sun.rave.web.ui.model.SingleSelectOptionsList;
import com.sun.rave.web.ui.component.Label;
import com.sun.rave.web.ui.component.Button;
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

    private Button btnEditStructure = new Button();

    public Button getBtnEditStructure() {
        return btnEditStructure;
    }

    public void setBtnEditStructure(Button b) {
        this.btnEditStructure = b;
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


    public String btnSave_action() {
        Participant p = getSessionBean().getEditedParticipant() ;
        saveChanges(p);
        return null;
    }


    public String btnReset_action() {
//        Participant p = getSessionBean().restoreParticipant();
//        populateFields(p) ;
        return null;
    }


    public String btnEditStructure_action() {
        // TODO: Replace with your code

        return null;
    }

    public String tabRoles_action() {
        Participant p = getSessionBean().getEditedParticipant() ;
        ((pfAddRemove) getBean("pfAddRemove")).populateLists("tabRoles", p);
        getSessionBean().setActiveResourceAttributeTab("tabRoles") ;
        return null;
    }


    public String tabPosition_action() {
        Participant p = getSessionBean().getEditedParticipant() ;
        ((pfAddRemove) getBean("pfAddRemove")).populateLists("tabPosition", p);
        getSessionBean().setActiveResourceAttributeTab("tabPosition") ;
        return null;
    }


    public String tabCapability_action() {
        Participant p = getSessionBean().getEditedParticipant() ;
        ((pfAddRemove) getBean("pfAddRemove")).populateLists("tabCapability", p);
        getSessionBean().setActiveResourceAttributeTab("tabCapability") ;
        return null;
    }


    public void cbbParticipants_processValueChange(ValueChangeEvent event) {
        Participant p = getSessionBean().setEditedParticipant((String) event.getNewValue());
        populateFields(p) ;
    }

    
    private void populateFields(Participant p) {

        // set simple fields
        txtFirstName.setText(p.getFirstName());
        txtLastName.setText(p.getLastName());
        txtUserID.setText(p.getUserID());
        txtDesc.setText(p.getDescription());
        txtNotes.setText(p.getNotes());
        cbxAdmin.setValue(p.isAdministrator());

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


    private void saveChanges(Participant p) {

        // update fields     
        p.setFirstName((String) txtFirstName.getText());
        p.setLastName((String) txtLastName.getText());
        p.setUserID((String) txtUserID.getText());
        p.setDescription((String) txtDesc.getText());
        p.setNotes((String) txtNotes.getText());
        p.setAdministrator((Boolean) cbxAdmin.getValue());

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


}

