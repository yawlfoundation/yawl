/*
 * userWorkQueues.java
 *
 * Created on October 23, 2007, 11:18 AM
 * Copyright adamsmj
 */
package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.model.Option;
import org.yawlfoundation.yawl.resourcing.ResourceManager;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import java.io.IOException;

/**
 * <p>Page bean that corresponds to a similarly named JSP page.  This
 * class contains component definitions (and initialization code) for
 * all components that you have defined on this page, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class orgDataMgt extends AbstractPageBean {
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

    private TabSet tabSet = new TabSet();

    public TabSet getTabSet() {
        return tabSet;
    }

    public void setTabSet(TabSet ts) {
        this.tabSet = ts;
    }

    private Tab tabRoles = new Tab();

    public Tab getTabRoles() {
        return tabRoles;
    }

    public void setTabRoles(Tab t) {
        this.tabRoles = t;
    }

    private PanelLayout lpRoles = new PanelLayout();

    public PanelLayout getLpRoles() {
        return lpRoles;
    }

    public void setLpRoles(PanelLayout pl) {
        this.lpRoles = pl;
    }

    private Tab tabCapability = new Tab();

    public Tab getTabCapability() {
        return tabCapability;
    }

    public void setTabCapability(Tab t) {
        this.tabCapability = t;
    }

    private PanelLayout lpCapabilities = new PanelLayout();

    public PanelLayout getLpCapabilities() {
        return lpCapabilities;
    }

    public void setLpCapabilities(PanelLayout pl) {
        this.lpCapabilities = pl;
    }

    private Tab tabPosition = new Tab();

    public Tab getTabPosition() {
        return tabPosition;
    }

    public void setTabPosition(Tab t) {
        this.tabPosition = t;
    }

    private PanelLayout lpPositions = new PanelLayout();

    public PanelLayout getLpPositions() {
        return lpPositions;
    }

    public void setLpPositions(PanelLayout pl) {
        this.lpPositions = pl;
    }


    private Tab tabOrgGroup = new Tab();

    public Tab getTabOrgGroup() {
        return tabOrgGroup;
    }

    public void setTabOrgGroup(Tab t) {
        this.tabOrgGroup = t;
    }

    private PanelLayout lpOrgGroups = new PanelLayout();

    public PanelLayout getLpOrgGroups() {
        return lpOrgGroups;
    }

    public void setLpOrgGroups(PanelLayout pl) {
        this.lpOrgGroups = pl;
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

    private Meta metaRefresh = new Meta();

    public Meta getMetaRefresh() {
        return metaRefresh;
    }

    public void setMetaRefresh(Meta m) {
        this.metaRefresh = m;
    }



    // </editor-fold>

    private boolean pageLoaded = false ;
    /**
     * <p>Construct a new Page bean instance.</p>
     */
    public orgDataMgt() {
    }

    private SessionBean _sb = getSessionBean();

    private ResourceManager _rm = getApplicationBean().getResourceManager();

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
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

    /** @return a reference to the session scoped factory bean. */
    private DynFormFactory getDynFormFactory() {
        return (DynFormFactory) getBean("DynFormFactory");
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
            log("userWorkQueues Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here
        //tabRoles_action();
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
        msgPanel.show(420, 200, "absolute");


        if (_sb.getSourceTab() != null) {
            tabSet.setSelected(_sb.getSourceTab());
            _sb.setSourceTab(null);
        }

        String selTabName = tabSet.getSelected() ;
        Tab selTab = null;

        if (selTabName == null) {

       //     setRefreshRate(0) ;               // get default refresh rate from web.xml
            tabSet.setSelected("tabRoles");
            selTab = tabRoles;
            tabRoles_action() ;           // default
            setVisibleComponents("tabRoles");
            setMode(Mode.edit);
        }
        else {
            if (btnAdd.getText().equals("Add"))
                ((pfOrgData) getBean("pfOrgData")).setCombosToNil();
            
            if (! _sb.getActiveTab().equals(selTabName)) {
                _sb.setOrgDataChoice(null);
                setMode(Mode.edit);
                setVisibleComponents(selTabName);
            }

            if (selTabName.equals("tabRoles")) {
                tabRoles_action() ;
                selTab = tabRoles;
            }
            else if (selTabName.equals("tabCapability")) {
                tabCapabilities_action() ;
                selTab = tabCapability;
            }
            else if (selTabName.equals("tabPosition")) {
                tabPositions_action() ;
                selTab = tabPosition;
            }
            else if (selTabName.equals("tabOrgGroup")) {
                tabOrgGroups_action() ;
                selTab = tabOrgGroup;
            }
        }
        updateTabHeaders(selTab) ;
        _sb.setActiveTab(tabSet.getSelected());
        _sb.setActivePage(ApplicationBean.PageRef.orgDataMgt);

        if (_sb.isOrgDataItemRemovedFlag()) {
            btnReset_action();
            _sb.setOrgDataItemRemovedFlag(false) ;
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

    private MessagePanel msgPanel = getSessionBean().getMessagePanel() ;

    public enum AttribType { role, position, capability, orggroup }

    private pfOrgData innerForm = (pfOrgData) getBean("pfOrgData");

    public String btnRefresh_action() {
        return null ;
    }

    private void setVisibleComponents(String tabName) {
        innerForm.setVisibleComponents(tabName);
    }

    public String btnSave_action() {
        if (innerForm.saveChanges(_sb.getOrgDataChoice()))
            populateForm(getAttribType(_sb.getActiveTab())) ;
        return null;
     }


    public String btnAdd_action() {
        // if 'new', we're in edit mode - move to add mode
        if (btnAdd.getText().equals("New")) {
            setMode(Mode.add);
        }
        else {
            if (innerForm.addNewItem(_sb.getActiveTab())) {
                setMode(Mode.edit);
                msgPanel.success("New item added successfully.");
            }
        }
        return null;
    }


    public String btnReset_action() {

        // if in 'add new' mode, discard inputs and go back to edit mode
        if (btnAdd.getText().equals("Add")) {
            setMode(Mode.edit);
        }
        return null ;
    }


    public String btnRemove_action() {
        ResourceManager rm = getApplicationBean().getResourceManager();
        String id = _sb.getOrgDataChoice();
        if (id != null) {
            AttribType type = getAttribType(_sb.getActiveTab());
            switch (type) {
                case role       : rm.removeRole(rm.getRole(id)); break ;
                case capability : rm.removeCapability(rm.getCapability(id)); break ;
                case position   : rm.removePosition(rm.getPosition(id)); break;
                case orggroup   : rm.removeOrgGroup(rm.getOrgGroup(id));
            }
            innerForm.clearFieldsAfterRemove();
            msgPanel.success("Chosen item successfully removed.");
            _sb.setOrgDataItemRemovedFlag(true) ;
        }
        return null;
    }


    private void setMode(Mode mode) {
        pfOrgData innerForm = (pfOrgData) getBean("pfOrgData");
        if (mode == Mode.edit) {
            innerForm.setAddMode(false);
            btnAdd.setText("New");
            btnAdd.setToolTip("Add a new item");
            btnReset.setToolTip("Discard unsaved changes");
            btnSave.setDisabled(false);
            btnRemove.setDisabled(false);
            populateForm(getAttribType(_sb.getActiveTab())) ;            
        }
        else {
            innerForm.setAddMode(true);
            btnAdd.setText("Add");
            btnAdd.setToolTip("Save entered data to create a new " + getActiveAttribText());
            btnReset.setToolTip("Discard data and revert to edit mode");
            btnSave.setDisabled(true);
            btnRemove.setDisabled(true);
        }
    }


    public void forceRefresh() {
        ExternalContext externalContext = getFacesContext().getExternalContext();
        if (externalContext != null) {
            try {
                externalContext.redirect("userWorkQueues.jsp");
            }
            catch (IOException ioe) {}
        }
    }


    private void updateTabHeaders(Tab selected) {
        tabRoles.setStyle("");
        tabCapability.setStyle("");
        tabPosition.setStyle("");
        tabOrgGroup.setStyle("");
        if (selected != null) selected.setStyle("color: #3277ba");
    }

    public String tabRoles_action() {
        populateForm(AttribType.role);
        _sb.setOrgDataListLabelText("Role Names");
        return null;
    }


    public String tabCapabilities_action() {
        populateForm(AttribType.capability);
        _sb.setOrgDataListLabelText("Capability Names");
        return null;
    }


    public String tabPositions_action() {
        populateForm(AttribType.position);
        _sb.setOrgDataListLabelText("Position Titles");
        return null;
    }


    public String tabOrgGroups_action() {
        populateForm(AttribType.orggroup);
        _sb.setOrgDataListLabelText("Org Group Titles");
        return null;
    }

    /**
     * Sets the auto refresh rate of the page
     * @param rate if <0, disables page refreshes; if >0, set refresh rate to that
     *        number of seconds; if 0, set the rate to the default provided by the
     *        resourceService's web.xml
     */
    public void setRefreshRate(int rate) {
        if (rate < 0)
            metaRefresh.setContent(null) ;
        else {
            if (rate == 0) rate = getApplicationBean().getDefaultJSFRefreshRate() ;
            metaRefresh.setContent(rate + "; url=./userWorkQueues.jsp");
        }
    }

    /******************************************************************************/


    private int populateForm(AttribType aType) {
        int result = -1;                                    // default for empty queue
        if (aType != null) {
            Option[] attribs = _sb.getFullResourceAttributeList(getTabString(aType));
            _sb.setOrgDataOptions(attribs);
            if ((attribs != null) && (attribs.length > 0)) {
                String id = _sb.getOrgDataChoice();
                if (id == null) id = (String) attribs[0].getValue();
                showItem(id, aType);
                result = attribs.length ;
            }
        }
        return result ;
    }

    private String getTabString(AttribType type) {
        switch (type) {
            case role       : return "tabRoles";
            case capability : return "tabCapability";
            case position   : return "tabPosition";
            case orggroup   : return "tabOrgGroup";
        }
        return "";
    }


    private AttribType getAttribType(String tabName) {
        if (tabName != null) {
            if (tabName.equals("tabRoles")) return AttribType.role;
            if (tabName.equals("tabCapability")) return AttribType.capability;
            if (tabName.equals("tabPosition")) return AttribType.position;
            if (tabName.equals("tabOrgGroup")) return AttribType.orggroup;
        }
        return null;
    }

    private String getActiveAttribText() {
        String activeTab = _sb.getActiveTab();
        if (activeTab != null) {
            AttribType type = getAttribType(activeTab);
            if (type != null)
                return type.name();
        }
        return "role";       // default 
    }

    private void showItem(String id, AttribType type) {
        innerForm.populateGUI(id, type);
    }

}