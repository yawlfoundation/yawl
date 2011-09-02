/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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


    public enum SelType { resource, category }                // which tab is selected?

    private final SessionBean _sb = getSessionBean();
    private final ResourceManager _rm = getApplicationBean().getResourceManager();
    private final ResourceDataSet _orgDataSet = _rm.getOrgDataSet();
    private final MessagePanel _msgPanel = _sb.getMessagePanel() ;
    private final pfNHResources _innerForm = (pfNHResources) getBean("pfNHResources");
    private final Logger _log = Logger.getLogger(this.getClass());


    // Callback method that is called just before rendering takes place.
    public void prerender() {
        _sb.checkLogon();
        _sb.setActivePage(ApplicationBean.PageRef.nonHumanMgt);

        // abort load if org data isn't currently available
        if (_sb.orgDataIsRefreshing()) return;

        showMessagePanel();

        SelType sType = getSelTypeForTab();
        if (tabSet.getSelected() == null) {                           // first rendering
            tabSet.setSelected("tabResources");
            setMode(SessionBean.Mode.edit);
            nullifyChoices();
            setVisibleComponents(SelType.resource);
        }
        else {
            if (! _sb.getActiveTab().equals(tabSet.getSelected())) {       // tab change
                nullifyChoices();
                setMode(SessionBean.Mode.edit);
                _innerForm.setSubCatAddMode(false);
                _sb.setSubCatAddMode(false);
                setVisibleComponents(getSelTypeForTab());
            }
        }
        doTabAction(sType);

        disableButtonsOnSubCatAddMode(sType);
        updateTabHeaders(sType) ;
        _sb.setActiveTab(tabSet.getSelected());

        if (_sb.isNhResourcesItemRemovedFlag()) {
            btnReset_action();
            _sb.setNhResourcesItemRemovedFlag(false) ;
        }
    }


    /* reloads the form */
    public String btnRefresh_action() {
        return null ;
    }


    /* saves current changes to the selected resource or category */
    public String btnSave_action() {
        if (_innerForm.saveChanges(_sb.getNhResourcesChoice())) {
            populateForm(getAttribType(_sb.getActiveTab())) ;
        }
        return null;
     }


    /* toggles between add & edit modes */
    public String btnAdd_action() {

        // if in edit mode - move to add mode
        if (! isAddMode()) {
            if (getSelTypeForActiveTab() == SelType.resource) {
                if (! _sb.hasAtLeastOneNonHumanCategory()) {
                    _msgPanel.warn("Cannot add a new non-human resource yet, because " +
                            "there are no non-human categories to add it to. Please " +
                            "add at least one category first (on the 'Categories' tab), " +
                            "then try again.");
                    return null;
                }
                _innerForm.createNewResource();
            }
            setMode(SessionBean.Mode.add);
        }
        else {
            if (_innerForm.addNewItem(getSelTypeForActiveTab())) {
                setMode(SessionBean.Mode.edit);
                _msgPanel.success("New item added successfully.");
            }
        }
        return null;
    }


    /* resets any unsaved changes back to original values */
    public String btnReset_action() {

        // if in 'add new' mode, discard inputs and go back to edit mode
        if (isAddMode()) {
            setMode(SessionBean.Mode.edit);
        }
        _innerForm.updateSelectedResource(null, true);             // refresh selection
        return null ;
    }


    /* deletes the selected resource or category from the org database */
    public String btnRemove_action() {
        String id = _sb.getNhResourcesChoice();
        if (id != null) {
            try {
                switch (getSelTypeForActiveTab()) {
                    case resource : _orgDataSet.removeNonHumanResource(id); break ;
                    case category : _orgDataSet.removeNonHumanCategory(id); break ;
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


    /* do the action appropriate to populate the selected tab */
    private void doTabAction(SelType sType) {
        switch (sType) {
            case resource : tabResources_action(); break;
            case category : tabCategories_action(); break;
        }
        showButtons();
    }


    /* take action when the resource tab is selected */
    public String tabResources_action() {
        _sb.setSubCatAddMode(false);
        _sb.setNhResourceListLabelText("Resources");
        _sb.setNhResourceCategoryLabelText("Category");
        _innerForm.showSubCatAddFields(false);
        populateForm(SelType.resource);
        return null;
    }


    /* take action when the category tab is selected */
    public String tabCategories_action() {
        _sb.setNhResourceListLabelText("Categories");
        _sb.setNhResourceCategoryLabelText("Subcategories");
        if (! isAddMode()) {
            _innerForm.setSubCatAddMode(_sb.isSubCatAddMode());
            populateForm(SelType.category);
        }
        if (_sb.isSubCatAddMode() && (! _msgPanel.hasMessage())) {
            body1.setFocus("form1:pfNHResources:txtSubCat");            
        }
        return null;
    }


    /* returns the current edit mode (ie. browse/edit or add) */
    private SessionBean.Mode getMode() {
        return _sb.getNhrMgtMode();
    }


    private boolean isAddMode() {
        return getMode() == SessionBean.Mode.add;
    }


    /* enables/disables fields when the add mode changes */
    private void setMode(SessionBean.Mode mode) {
        if (mode == SessionBean.Mode.edit) {
            _innerForm.setAddMode(false, tabSet.getSelected());
            btnAdd.setText("New");
            btnAdd.setToolTip("Add a new item");
            btnReset.setToolTip("Discard unsaved changes");
            btnSave.setDisabled(false);
            btnRemove.setDisabled(false);
            if (! _msgPanel.hasMessage()) body1.setFocus("form1:pfNHResources:lbxItems");
        }
        else {
            _innerForm.setAddMode(true, tabSet.getSelected());
            btnAdd.setText("Add");
            btnAdd.setToolTip("Save entered data to create a new " + getActiveAttribText());
            btnReset.setToolTip("Discard data and revert to edit mode");
            btnSave.setDisabled(true);
            btnRemove.setDisabled(true);
            if (! _msgPanel.hasMessage()) body1.setFocus("form1:pfNHResources:txtName");
        }
        _sb.setNhrMgtMode(mode);
    }


    /* enables/disables fields depending on which tab is selected */
    private void setVisibleComponents(SelType sType) {
        _innerForm.setVisibleComponents(sType);
    }


    /* resets all session stored selection values */
    private void nullifyChoices() {
        _sb.setNhResourcesChoice(null);
        _sb.setNhResourcesCategoryChoice(null);
        _sb.setNhResourcesSubcategoryChoice(null);
        _innerForm.updateSelectedResource(null);
        _innerForm.clearCombos();
    }


    /* highlights the selected tab */
    private void updateTabHeaders(SelType sType) {
        tabResources.setStyle("");
        tabCategories.setStyle("");
        Tab tab = (sType == SelType.resource) ? tabResources : tabCategories;
        tab.setStyle("color: #3277ba");
    }


    /* enables/disables fields when the subcategory edit mode changes */
    private void disableButtonsOnSubCatAddMode(SelType sType) {
        if (isAddMode()) return;
        boolean addingSubCat = _sb.isSubCatAddMode() && (sType == SelType.category);
        btnSave.setDisabled(addingSubCat);
        btnAdd.setDisabled(addingSubCat);
        btnReset.setDisabled(addingSubCat);
        btnRemove.setDisabled(addingSubCat);
    }


    /* enables/disables buttons depending on whether a listbox item is selected */
    private void showButtons() {
        boolean noSelectedListItem = (_sb.getNhResourcesOptions().length == 0) ||
                (_sb.getNhResourcesChoice() == null);
        boolean adding = isAddMode();
        if (! adding) {
            btnSave.setDisabled(noSelectedListItem);
            btnReset.setDisabled(noSelectedListItem);
        }
        btnRemove.setDisabled(adding || noSelectedListItem);
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


    private int populateForm(SelType sType) {
        int result = -1;                                    // default for empty queue
        if (sType != null) {

            // set listbox items for the selected tab
            Option[] items = _sb.getNhrItems(getTabString(sType));
            _sb.setNhResourcesOptions(items);
            _innerForm.getLbxItems().setItems(items);
            
            if ((items != null) && (items.length > 0)) {
                String id = _sb.getNhResourcesChoice();
                if (id == null) {
                    id = (String) items[0].getValue();
                    _sb.setNhResourcesChoice(id);
                }
                _innerForm.populateGUI(id, sType);
                result = items.length ;
            }
            else {
                nullifyChoices();
                _innerForm.clearAllFieldsAndLists();
                _innerForm.disableInputFields(! isAddMode());
            }
        }
        return result ;
    }


    private String getTabString(SelType type) {
        switch (type) {
            case resource : return "tabResources";
            case category : return "tabCategories";
        }
        return "";     // default
    }


    private SelType getAttribType(String tabName) {
        if (tabName != null) {
            if (tabName.equals("tabResources")) return SelType.resource;
            if (tabName.equals("tabCategories")) return SelType.category;
        }
        return null;
    }

    private String getActiveAttribText() {
        String activeTab = _sb.getActiveTab();
        if (activeTab != null) {
            SelType type = getAttribType(activeTab);
            if (type != null)
                return type.name();
        }
        return "resource";       // default
    }


    private SelType getSelTypeForTab() {
        String selected = tabSet.getSelected();
        return ((selected == null) || selected.equals("tabResources")) ? SelType.resource
                : SelType.category;
    }

    private SelType getSelTypeForActiveTab() {
        return getAttribType(_sb.getActiveTab());
    }


    private void showMessagePanel() {
        if (_msgPanel.hasMessage()) body1.setFocus("form1:pfMsgPanel:btnOK001");
        _sb.showMessagePanel();
    }

}