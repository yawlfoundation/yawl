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
import com.sun.rave.web.ui.model.Option;
import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import java.io.IOException;

/*
 * The backing bean for the YAWL 2.0 nonhuman resources mgt form
 *
 * @author Michael Adams
 * @date 10/11/2010
 */

public class nonHumanMgt extends AbstractPageBean {

    private int __placeholder;

    private void _init() throws Exception { }

    public nonHumanMgt() { }


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
            log("NonHumanResource Management Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
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


    private Tab tabResources = new Tab();

    public Tab getTabResources() { return tabResources; }

    public void setTabResources(Tab t) { tabResources = t; }


    private PanelLayout lpResources = new PanelLayout();

    public PanelLayout getLpResources() { return lpResources; }

    public void setLpResources(PanelLayout pl) { lpResources = pl; }


    private Tab tabCategories = new Tab();

    public Tab getTabCategories() { return tabCategories; }

    public void setTabCategories(Tab t) { tabCategories = t; }


    private PanelLayout lpCategories = new PanelLayout();

    public PanelLayout getLpCategories() { return lpCategories; }

    public void setLpCategories(PanelLayout pl) { lpCategories = pl; }


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


    private StaticText staticText1 = new StaticText();

    public StaticText getStaticText1() { return staticText1; }

    public void setStaticText1(StaticText st) { staticText1 = st; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    private Meta metaRefresh = new Meta();

    public Meta getMetaRefresh() { return metaRefresh; }

    public void setMetaRefresh(Meta m) { metaRefresh = m; }


    /********************************************************************************/


    public enum AttribType { resource, category }

    private SessionBean _sb = getSessionBean();
    private ResourceManager _rm = getApplicationBean().getResourceManager();
    private ResourceDataSet _orgDataSet = _rm.getOrgDataSet();
    private MessagePanel _msgPanel = _sb.getMessagePanel() ;
    private pfNHResources _innerForm = (pfNHResources) getBean("pfNHResources");
    private Logger _log = Logger.getLogger(this.getClass());


    // Callback method that is called just before rendering takes place.
    public void prerender() {
        _sb.checkLogon();
        _sb.setActivePage(ApplicationBean.PageRef.nonHumanMgt);
        _sb.showMessagePanel();

        String selTabName = tabSet.getSelected() ;
        Tab selTab = null;

        if (selTabName == null) {
            tabSet.setSelected("tabResources");
            selTab = tabResources;
            _sb.getNhResourcesOptions();
            setMode(SessionBean.Mode.edit);
            nullifyChoices();
            tabResources_action() ;           // default
            setVisibleComponents("tabResources");
        }
        else {
            if (btnAdd.getText().equals("Add")) {
                _innerForm.getTxtName().setText("");
                _innerForm.getLblMembers().setText("Members (0)");
                _sb.setCategoryMembers(null);
            }
            else {
                btnRemove.setDisabled(_sb.getNhResourcesOptions().length == 0);
            }

            if (! _sb.getActiveTab().equals(selTabName)) {
                nullifyChoices();
                setMode(SessionBean.Mode.edit);
                setVisibleComponents(selTabName);
            }

            if (selTabName.equals("tabResources")) {
                tabResources_action() ;
                selTab = tabResources;
            }
            else if (selTabName.equals("tabCategories")) {
                tabCategories_action() ;
                selTab = tabCategories;
            }
        }
        updateTabHeaders(selTab) ;
        _sb.setActiveTab(tabSet.getSelected());

        if (_sb.isNhResourcesItemRemovedFlag()) {
            btnReset_action();
            _sb.setNhResourcesItemRemovedFlag(false) ;
        }
    }


    public String btnRefresh_action() {
        return null ;
    }


    public String btnSave_action() {
        if (_innerForm.saveChanges(_sb.getNhResourcesChoice()))
            populateForm(getAttribType(_sb.getActiveTab())) ;
        return null;
     }


    public String btnAdd_action() {
        // if 'new', we're in edit mode - move to add mode
        if (btnAdd.getText().equals("New")) {
            setMode(SessionBean.Mode.add);
        }
        else {
            if (_innerForm.addNewItem(_sb.getActiveTab())) {
                setMode(SessionBean.Mode.edit);
                _msgPanel.success("New item added successfully.");
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
        String id = _sb.getNhResourcesChoice();
        if (id != null) {
            AttribType type = getAttribType(_sb.getActiveTab());
            try {
                switch (type) {
                    case resource    : _orgDataSet.removeNonHumanResource(id); break ;
                    case category    : _orgDataSet.removeNonHumanCategory(id); break ;
                }
                _innerForm.clearFieldsAfterRemove();
                nullifyChoices();
                _msgPanel.success("Chosen item successfully removed.");
            }
            catch (Exception e) {
                _msgPanel.error("Could not remove chosen item. See log file for details.");
                _log.error("Handled Exception: Unable to remove resource", e);
            }
            _sb.setNhResourcesItemRemovedFlag(true) ;
            _sb.setNhResourcesChoice(null);
        }
        return null;
    }


    public String tabResources_action() {
        if (getMode() == SessionBean.Mode.edit) populateForm(AttribType.resource);
        _sb.setNhResourceListLabelText("Resources");
        return null;
    }


    public String tabCategories_action() {
        if (getMode() == SessionBean.Mode.edit) populateForm(AttribType.category);
        _sb.setNhResourceListLabelText("Categories");
        return null;
    }


    private SessionBean.Mode getMode() {
        return _sb.getNhrMgtMode();
    }


    private void setMode(SessionBean.Mode mode) {
        if (mode == SessionBean.Mode.edit) {
            _innerForm.setAddMode(false);
            btnAdd.setText("New");
            btnAdd.setToolTip("Add a new item");
            btnReset.setToolTip("Discard unsaved changes");
            btnSave.setDisabled(false);
            btnRemove.setDisabled(false);
            body1.setFocus("form1:pfNHResources:txtAdd");
            populateForm(getAttribType(_sb.getActiveTab())) ;
        }
        else {
            _innerForm.setAddMode(true);
            btnAdd.setText("Add");
            btnAdd.setToolTip("Save entered data to create a new " + getActiveAttribText());
            btnReset.setToolTip("Discard data and revert to edit mode");
            btnSave.setDisabled(true);
            btnRemove.setDisabled(true);
        }
        _sb.setNhrMgtMode(mode);
    }


    private void setVisibleComponents(String tabName) {
        _innerForm.setVisibleComponents(tabName);
    }


    private void nullifyChoices() {
        _sb.setNhResourcesChoice(null);
        _sb.setNhResourcesCategoryChoice(null);
        _sb.setNhResourcesSubcategoryChoice(null);
    }


    public void forceRefresh() {
        ExternalContext externalContext = getFacesContext().getExternalContext();
        if (externalContext != null) {
            try {
                externalContext.redirect("nonHumanMgt.jsp");
            }
            catch (IOException ioe) {}
        }
    }


    private void updateTabHeaders(Tab selected) {
        tabResources.setStyle("");
        tabCategories.setStyle("");
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
            metaRefresh.setContent(rate + "; url=./nonHumanMgt.jsp");
        }
    }

    /******************************************************************************/


    private int populateForm(AttribType aType) {
        int result = -1;                                    // default for empty queue
        if (aType != null) {
            Option[] items = _sb.getNhrItems(getTabString(aType));
            _sb.setNhResourcesOptions(items);
            _innerForm.getLbxItems().setItems(items);
            if ((items != null) && (items.length > 0)) {
                String id = _sb.getNhResourcesChoice();
                if (id == null) id = (String) items[0].getValue();
                showItem(id, aType);
                result = items.length ;
            }
            else _innerForm.clearFields();
        }
        return result ;
    }

    private String getTabString(AttribType type) {
        switch (type) {
            case resource    : return "tabResources";
            case category    : return "tabCategories";
        }
        return "";
    }


    private AttribType getAttribType(String tabName) {
        if (tabName != null) {
            if (tabName.equals("tabResources")) return AttribType.resource;
            if (tabName.equals("tabCategories")) return AttribType.category;
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
        return "resource";       // default
    }

    private void showItem(String id, AttribType type) {
        _innerForm.populateGUI(id, type);
    }

}