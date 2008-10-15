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

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import java.io.IOException;

/*
 * The backing bean for the YAWL 2.0 org data mgt form
 *
 * @author Michael Adams
 * Date: 10/12/2007
 *
 * Last Date: 09/04/2008
 */

public class orgDataMgt extends AbstractPageBean {

    private int __placeholder;

    private void _init() throws Exception { }

    public orgDataMgt() { }


    // Return reference to scoped data beans
    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }

    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }

    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }


    public void init() {
        super.init();

        try {
            _init();
        } catch (Exception e) {
            log("userWorkQueues Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
//        tabRoles_action();
    }

    public void preprocess() { }

    public void destroy() { }


    /*******************************************************************************/

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


    private TabSet tabSet = new TabSet();

    public TabSet getTabSet() { return tabSet; }

    public void setTabSet(TabSet ts) { tabSet = ts; }


    private Tab tabRoles = new Tab();

    public Tab getTabRoles() { return tabRoles; }

    public void setTabRoles(Tab t) { tabRoles = t; }


    private PanelLayout lpRoles = new PanelLayout();

    public PanelLayout getLpRoles() { return lpRoles; }

    public void setLpRoles(PanelLayout pl) { lpRoles = pl; }


    private Tab tabCapability = new Tab();

    public Tab getTabCapability() { return tabCapability; }

    public void setTabCapability(Tab t) { tabCapability = t; }


    private PanelLayout lpCapabilities = new PanelLayout();

    public PanelLayout getLpCapabilities() { return lpCapabilities; }

    public void setLpCapabilities(PanelLayout pl) { lpCapabilities = pl; }


    private Tab tabPosition = new Tab();

    public Tab getTabPosition() { return tabPosition; }

    public void setTabPosition(Tab t) { tabPosition = t; }


    private PanelLayout lpPositions = new PanelLayout();

    public PanelLayout getLpPositions() { return lpPositions; }

    public void setLpPositions(PanelLayout pl) { lpPositions = pl; }


    private Tab tabOrgGroup = new Tab();

    public Tab getTabOrgGroup() { return tabOrgGroup; }

    public void setTabOrgGroup(Tab t) { tabOrgGroup = t; }


    private PanelLayout lpOrgGroups = new PanelLayout();

    public PanelLayout getLpOrgGroups() { return lpOrgGroups; }

    public void setLpOrgGroups(PanelLayout pl) { lpOrgGroups = pl; }


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


    private Meta metaRefresh = new Meta();

    public Meta getMetaRefresh() { return metaRefresh; }

    public void setMetaRefresh(Meta m) { metaRefresh = m; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }
    


    /********************************************************************************/


    public enum AttribType { role, position, capability, orggroup }

    private SessionBean _sb = getSessionBean();
    private ResourceManager _rm = getApplicationBean().getResourceManager();
    private MessagePanel msgPanel = _sb.getMessagePanel() ;
    private pfOrgData innerForm = (pfOrgData) getBean("pfOrgData");


    // Callback method that is called just before rendering takes place.
    public void prerender() {
        getSessionBean().checkLogon();
        msgPanel.show();

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
            _sb.getOrgDataOptions();
            setMode(SessionBean.Mode.edit);
            _sb.setOrgDataChoice(null);
            tabRoles_action() ;           // default
            setVisibleComponents("tabRoles");
        }
        else {
            if (btnAdd.getText().equals("Add"))
                ((pfOrgData) getBean("pfOrgData")).setCombosToNil();
            
            if (! _sb.getActiveTab().equals(selTabName)) {
                _sb.setOrgDataChoice(null);
                setMode(SessionBean.Mode.edit);
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

        btnRemove.setDisabled(_sb.getOrgDataOptions().length == 0);
        
        if (_sb.isOrgDataItemRemovedFlag()) {
            btnReset_action();
            _sb.setOrgDataItemRemovedFlag(false) ;
        }
    }



    public String btnRefresh_action() {
        return null ;
    }


    public String btnSave_action() {
        if (innerForm.saveChanges(_sb.getOrgDataChoice()))
            populateForm(getAttribType(_sb.getActiveTab())) ;
        return null;
     }


    public String btnAdd_action() {
        // if 'new', we're in edit mode - move to add mode
        if (btnAdd.getText().equals("New")) {
            setMode(SessionBean.Mode.add);
        }
        else {
            if (innerForm.addNewItem(_sb.getActiveTab())) {
                setMode(SessionBean.Mode.edit);
                msgPanel.success("New item added successfully.");
            }
        }
        return null;
    }


    public String btnReset_action() {

        // if in 'add new' mode, discard inputs and go back to edit mode
        if (btnAdd.getText().equals("Add")) {
            setMode(SessionBean.Mode.edit);
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
            _sb.setOrgDataChoice(null);
        }
        return null;
    }

    
    public String tabRoles_action() {
        if (getMode() == SessionBean.Mode.edit) populateForm(AttribType.role);
        _sb.setOrgDataListLabelText("Role Names");
        return null;
    }


    public String tabCapabilities_action() {
        if (getMode() == SessionBean.Mode.edit) populateForm(AttribType.capability);
        _sb.setOrgDataListLabelText("Capability Names");
        return null;
    }


    public String tabPositions_action() {
        if (getMode() == SessionBean.Mode.edit) populateForm(AttribType.position);
        _sb.setOrgDataListLabelText("Position Titles");
        return null;
    }


    public String tabOrgGroups_action() {
        if (getMode() == SessionBean.Mode.edit) populateForm(AttribType.orggroup);
        _sb.setOrgDataListLabelText("Org Group Titles");
        return null;
    }


    private SessionBean.Mode getMode() {
        return _sb.getOrgMgtMode();
    }

    private void setMode(SessionBean.Mode mode) {
        pfOrgData innerForm = (pfOrgData) getBean("pfOrgData");
        if (mode == SessionBean.Mode.edit) {
            innerForm.setAddMode(false);
            btnAdd.setText("New");
            btnAdd.setToolTip("Add a new item");
            btnReset.setToolTip("Discard unsaved changes");
            btnSave.setDisabled(false);
            btnRemove.setDisabled(false);
            body1.setFocus("form1:pfQueueUI:txtAdd");
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
        _sb.setOrgMgtMode(mode);
    }


    private void setVisibleComponents(String tabName) {
        innerForm.setVisibleComponents(tabName);
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
            innerForm.getLbxItems().setItems(attribs);
            if ((attribs != null) && (attribs.length > 0)) {
                String id = _sb.getOrgDataChoice();
                if (id == null) id = (String) attribs[0].getValue();
                showItem(id, aType);
                result = attribs.length ;
            }
            else innerForm.clearFields();
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
