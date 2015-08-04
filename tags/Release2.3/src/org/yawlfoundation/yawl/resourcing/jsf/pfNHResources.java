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

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.model.Option;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.OptionComparator;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanSubCategory;

import javax.faces.FacesException;
import javax.faces.event.ValueChangeEvent;
import java.util.Arrays;

/*
 * Fragment bean for non human resources form
 *
 * @author: Michael Adams
 * Date: 10/11/2010
 */

public class pfNHResources extends AbstractFragmentBean {
    private int __placeholder;

    private void _init() throws Exception {
    }

    private Listbox lbxItems = new Listbox();

    public Listbox getLbxItems() {
        return lbxItems;
    }

    public void setLbxItems(Listbox l) {
        this.lbxItems = l;
    }


    private Label lblItems = new Label();

    public Label getLblItems() {
        return lblItems;
    }

    public void setLblItems(Label l) {
        this.lblItems = l;
    }


    private Label lblName = new Label();

    public Label getLblName() {
        return lblName;
    }

    public void setLblName(Label l) {
        this.lblName = l;
    }


    private TextField txtName = new TextField();

    public TextField getTxtName() {
        return txtName;
    }

    public void setTxtName(TextField tf) {
        this.txtName = tf;
    }


    private Label lblCategory = new Label();

    public Label getLblCategory() {
        return lblCategory;
    }

    public void setLblCategory(Label l) {
        this.lblCategory = l;
    }


    private Label lblDesc = new Label();

    public Label getLblDesc() {
        return lblDesc;
    }

    public void setLblDesc(Label l) {
        this.lblDesc = l;
    }


    private Label lblNotes = new Label();

    public Label getLblNotes() {
        return lblNotes;
    }

    public void setLblNotes(Label l) {
        this.lblNotes = l;
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


    private DropDown cbbCategory = new DropDown();

    public DropDown getCbbCategory() {
        return cbbCategory;
    }

    public void setCbbCategory(DropDown dd) {
        this.cbbCategory = dd;
    }


    private DropDown cbbSubCategory = new DropDown();

    public DropDown getCbbSubCategory() {
        return cbbSubCategory;
    }

    public void setCbbSubCategory(DropDown dd) {
        this.cbbSubCategory = dd;
    }


    private Label lblSubCategory = new Label();

    public Label getLblSubCategory() {
        return lblSubCategory;
    }

    public void setLblSubCategory(Label l) {
        this.lblSubCategory = l;
    }


    private DropDown cbbMembers = new DropDown();

    public DropDown getCbbMembers() {
        return cbbMembers;
    }

    public void setCbbMembers(DropDown dd) {
        this.cbbMembers = dd;
    }

    
    private Label lblMembers = new Label();

    public Label getLblMembers() {
        return lblMembers;
    }

    public void setLblMembers(Label l) {
        this.lblMembers = l;
    }


    private Listbox lbxSubCatItems = new Listbox();

    public Listbox getLbxSubCatItems() {
        return lbxSubCatItems;
    }

    public void setLbxSubCatItems(Listbox l) {
        this.lbxSubCatItems = l;
    }


    private TextField txtSubCat = new TextField();

    public TextField getTxtSubCat() {
        return txtSubCat;
    }

    public void setTxtSubCat(TextField tf) {
        this.txtSubCat = tf;
    }


    private Button btnRemoveSubCat = new Button();

    public Button getBtnRemoveSubCat() { return btnRemoveSubCat; }

    public void setBtnRemoveSubCat(Button b) { btnRemoveSubCat = b; }


    private Button btnAddSubCat = new Button();

    public Button getBtnAddSubCat() { return btnAddSubCat; }

    public void setBtnAddSubCat(Button b) { btnAddSubCat = b; }


    private Button btnConfirmAddSubCat = new Button();

    public Button getBtnConfirmAddSubCat() { return btnConfirmAddSubCat; }

    public void setBtnConfirmAddSubCat(Button b) { btnConfirmAddSubCat = b; }


    private Button btnCancelAddSubCat = new Button();

    public Button getBtnCancelAddSubCat() { return btnCancelAddSubCat; }

    public void setBtnCancelAddSubCat(Button b) { btnCancelAddSubCat = b; }

    /*********************************************************/

    public pfNHResources() {
    }

    /**
     * <p>Return a reference to the scoped data bean.</p>
     */
    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
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
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }


    public void init() {
        // Perform initializations inherited from our superclass
        super.init();

        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("pfNHResources Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void destroy() { }

    private SessionBean _sb = getSessionBean();
    private MessagePanel _msgPanel = _sb.getMessagePanel();
    private ResourceManager _rm = getApplicationBean().getResourceManager() ;
    private ResourceDataSet _orgDataSet = _rm.getOrgDataSet();


    /* triggered by both tabs */
    public void lbxItems_processValueChange(ValueChangeEvent event) {
        _sb.setSourceTabAfterListboxSelection();
        updateSelectedResource(null);                     // resets currently selected
    }


    /* triggered by categories tab only */
    public void lbxSubCatItems_processValueChange(ValueChangeEvent event) {
        _sb.setSourceTabAfterListboxSelection();
    }


    /* triggered by resources tab only */
    public void cbbCategory_processValueChange(ValueChangeEvent event) {
        NonHumanResource resource = _sb.getSelectedNonHumanResource();
        if (resource != null) {
            storeSimpleFieldValues(resource);
            NonHumanCategory category = _orgDataSet.getNonHumanCategory((String) event.getNewValue());
            if (category != null) {
                resource.setCategory(category);
                resource.setSubCategory("None");
            }
        }
    }


    /* triggered by resources tab only */
    public void cbbSubCategory_processValueChange(ValueChangeEvent event) {
        NonHumanResource resource = _sb.getSelectedNonHumanResource();
        if (resource != null) {
            storeSimpleFieldValues(resource);
            resource.setSubCategory((String) event.getNewValue());
        }
    }


    /* set mode to add a new subcategory - triggered by categories tab only */
    public String btnAddSubCat_action() {
        _sb.setSubCatAddMode(true);
        setSubCatAddMode(true);
        return null;
    }


    /* removes a subcategory - triggered by categories tab only */
    public String btnRemoveSubCat_action() {
        NonHumanCategory category = getCurrentlySelectedCategory();
        if (category != null) {
            if (category.removeSubCategory(_sb.getNhResourcesSubcategoryChoice())) {
                _orgDataSet.updateNonHumanCategory(category);
                _sb.setNhResourcesSubcategoryChoice("None");
            }
            else _msgPanel.error("Failed to remove subcategory.");
        }
        return null;
    }


    /* adds a new subcategory - triggered by categories tab only */
    public String btnConfirmAddSubCat_action() {
        String name = (String) txtSubCat.getText();
        if ((name != null) && (name.length() > 0)) {
            NonHumanCategory category = getCurrentlySelectedCategory();
            if (category != null) {
                if (category.addSubCategory(name)) {           // won't add if duplicate
                    _orgDataSet.updateNonHumanCategory(category);
                    _sb.setSubCatAddMode(false);
                    txtSubCat.setText(null);
                }
                else _msgPanel.error("Subcategory already exists: " + name);
            }
        }
        else _msgPanel.error("Please enter a subcategory to add.");
        return null;
    }


    /* reset mode to browse/edit for subcategories - triggered by categories tab only */
    public String btnCancelAddSubCat_action() {
        _sb.setSubCatAddMode(false);
        return null;
    }

    
    protected void populateGUI(String id, nonHumanMgt.SelType type) {
        switch (type) {
            case resource : populateGUI(_sb.getSelectedNonHumanResource(), id); break;
            case category : populateGUI(_orgDataSet.getNonHumanCategory(id)); break;
        }
        lbxItems.setSelected(id);
    }


    /* fills ui with the selected resource's values */
    private void populateGUI(NonHumanResource resource, String id) {

        // selected resource is null after a selection change
        if (resource == null) {
            resource = _orgDataSet.getNonHumanResource(id);
            updateSelectedResource(resource);
        }
        if (resource != null) {
            populateSimpleFields(resource);
            NonHumanCategory category = resource.getCategory();
            String subCatName = resource.getSubCategoryName();
            if (category == null) {
                category = getFirstListedCategory();  // default
            }
            if (subCatName == null) subCatName = "None";
            if (category != null) {
                cbbCategory.setSelected(category.getID());
                _sb.setNhResourcesSubcategoryItems(getSubCategoryList(category));
                cbbSubCategory.setSelected(subCatName);
            }
            else _sb.setNhResourcesSubcategoryItems(null);    // no categories defined
        }
        _sb.setNhResourcesSubcategoryList(null);     // empty the category tab list box
    }


    /* fills ui with the selected category's values */
    private void populateGUI(NonHumanCategory category) {
        if (category != null) {
            populateSimpleFields(category);
            _sb.setNhResourcesSubcategoryList(getSubCategoryList(category));
            int membership = _sb.setCategoryMembers(category);
            lblMembers.setText("Members (" + membership + ")");
        }
        _sb.setNhResourcesSubcategoryItems(null);   // empty the resource tab combo box
    }


    /* sets or hides fields depending on which tab is shown */
    public void setVisibleComponents(nonHumanMgt.SelType sType) {
        boolean catTab = (sType == nonHumanMgt.SelType.category);

        // these only appear on the resources tab
        cbbCategory.setVisible(! catTab);
        lblSubCategory.setVisible(! catTab);
        cbbSubCategory.setVisible(! catTab);

        // these only appear on the categories tab
        lblMembers.setVisible(catTab);
        cbbMembers.setVisible(catTab);
        lbxSubCatItems.setVisible(catTab);
        btnAddSubCat.setVisible(catTab);
        btnRemoveSubCat.setVisible(catTab);

        // nullify lists
        if (catTab) {
            cbbCategory.setItems(null);
            cbbSubCategory.setItems(null);
        }
        else lbxSubCatItems.setItems(null);
    }


    /* enable or disable fields depending on whether we are in browse/edit or add mode */
    public void setAddMode(boolean adding, String selectedTab) {
        lbxItems.setDisabled(adding);
        if (selectedTab.equals("tabCategories")) {
            lbxSubCatItems.setDisabled(adding);
            btnAddSubCat.setDisabled(adding);
            btnRemoveSubCat.setDisabled(adding);
            disableInputFields(! adding);
            disableSubCatButtons(adding);
            _sb.setCategoryMembers(null);
        }
        if (adding) clearTextFields();
        txtName.setText("");
        lblMembers.setText("Members (0)");
    }


    /* saves updates to the values of the selected resource or category */
    public boolean saveChanges(String id) {
        String activeTab = _sb.getActiveTab();
        if (activeTab.equals("tabResources"))
            return saveResourceChanges(id) ;
        else if (activeTab.equals("tabCategories"))
            return saveCategoryChanges(id);

        return false;
    }


    /* saves updates to the values of the currently selected resource */    
    public boolean saveResourceChanges(String id) {
        NonHumanResource cloned = _sb.getSelectedNonHumanResource();
        if (cloned == null) {
            _msgPanel.error("Could not retrieve changes from session");
            return false;
        }
        NonHumanResource resource = _orgDataSet.getNonHumanResource(id);
        if (resource == null) {
            _msgPanel.error("Invalid resource id: " + id);
            return false;
        }

        // check that any name change is valid
        String name = (String) txtName.getText();
        if ((! name.equals(resource.getName())) && _orgDataSet.isKnownNonHumanResourceName(name)) {
            addDuplicationError("Resource");
            return false;
        }
        storeSimpleFieldValues(cloned);
        resource.merge(cloned);                   // update resource with clone's values
        cloned.clearCategory();                   // remove cloned from its category
        _orgDataSet.updateNonHumanResource(resource);
        updateSelectedResource(resource);
        return true;
    }


    /* saves updates to the values of the currently selected category */
    public boolean saveCategoryChanges(String id) {
        NonHumanCategory category = _orgDataSet.getNonHumanCategory(id);
        if (category == null) {
            _msgPanel.error("Invalid category id: " + id);
            return false;
        }

        // check that any name change is valid
        String name = (String) txtName.getText();
        if ((! name.equals(category.getName())) && _orgDataSet.isKnownNonHumanCategoryName(name)) {
            addDuplicationError("Category");
            return false;
        }

        storeSimpleFieldValues(category);
        _orgDataSet.updateNonHumanCategory(category);
        return true;
    }


    /* saves a newly added resource or category to the org database */
    public boolean addNewItem(nonHumanMgt.SelType sType) {
        String newName = (String) txtName.getText();
        if ((newName == null) || (newName.length() == 0)) {
            _msgPanel.error("Please enter a name for the new Item.");
            return false;
        }
        switch (sType) {
            case resource : return addResource(newName);
            case category : return addCategory(newName);
        }
        return false;
    }


    /* saves a newly added resource to the org database */
    public boolean addResource(String name) {
        if (! _orgDataSet.isKnownNonHumanResourceName(name)) {
            String catID = (String) cbbCategory.getSelected();
            NonHumanCategory category = _orgDataSet.getNonHumanCategory(catID);
            String subCat = (String) cbbSubCategory.getSelected();

            NonHumanResource resource = _sb.getSelectedNonHumanResource();
            if (resource == null) resource = new NonHumanResource();
            resource.setName(name);
            resource.setCategory(category);
            resource.setSubCategory(subCat);
            resource.setDescription((String) txtDesc.getText());
            resource.setNotes((String) txtNotes.getText());
            String newID = _orgDataSet.addNonHumanResource(resource);
            if (_rm.successful(newID)) {
                updateSelectedResource(resource, false);
                _sb.setNhResourcesChoice(newID);
                return true;
            }
            else _msgPanel.error(_msgPanel.format(newID));
        }
        else addDuplicationError("Resource");

        return false;
    }


    /* saves a newly added category to the org database */
    public boolean addCategory(String name) {
        if (! _orgDataSet.isKnownNonHumanCategoryName(name)) {
            NonHumanCategory category = new NonHumanCategory(name);
            storeSimpleFieldValues(category);
            category.addSubCategory("None");
            String newID = _orgDataSet.addNonHumanCategory(category);
            if (_rm.successful(newID)) {
                _sb.setNhResourcesChoice(newID);
                return true;
            }
            else _msgPanel.error(_msgPanel.format(newID));
        }
        else addDuplicationError("Category");

        return false;
    }


    private void addDuplicationError(String type) {
        String dupErrMsg = "There is already a %s by that name - please choose another.";
        _msgPanel.error(String.format(dupErrMsg, type)) ;
    }


    public void clearTextFields() {
        txtDesc.setText("");
        txtNotes.setText("");
    }


    public void clearFieldsAfterRemove() {
        clearTextFields();
        lbxItems.setSelected(getFirstListboxItem());
    }


    public void clearAllFieldsAndLists() {
        clearTextFields();
        clearCombos();
        _sb.setNhResourcesCategoryItems(null);
        _sb.setNhResourcesSubcategoryItems(null);   // resource tab combo box
        _sb.setNhResourcesSubcategoryList(null);    // category tab list box
        lblMembers.setText("Members (0)");
    }


    protected void clearCombos() {
        cbbCategory.setSelected(null);
        cbbSubCategory.setSelected(null);
        cbbCategory.setItems(null);
        cbbSubCategory.setItems(null);
    }


    /* returns the value of the first item in the listbox, if any */
    private String getFirstListboxItem() {
        Option[] items = (Option[]) lbxItems.getItems();
        if ((items != null) && items.length > 0) {
            Option item = items[0];
            if (item != null) return (String) item.getValue();
        }
        return null;
    }


    /* returns the value of the first listed category, sorted */
    private NonHumanCategory getFirstListedCategory() {
        Option[] catItems = _sb.getNhResourcesCategoryList();
        if ((catItems != null) && (catItems.length > 0)) {
            String catID = (String) catItems[0].getValue();
            return _orgDataSet.getNonHumanCategory(catID);
        }
        return null;
    }

    private void storeSimpleFieldValues(NonHumanResource resource) {
        String name = (String) txtName.getText();
        if ((name != null) && (name.length() > 0)) resource.setName(name);
        String desc = (String) txtDesc.getText();
        if (desc != null) resource.setDescription(desc);
        String notes = (String) txtNotes.getText();
        if (notes != null) resource.setNotes(notes);
    }


    private void storeSimpleFieldValues(NonHumanCategory category) {
        String name = (String) txtName.getText();
        if (name.length() > 0) category.setName(name);
        category.setDescription((String) txtDesc.getText());
        category.setNotes((String) txtNotes.getText());
    }


    private void populateSimpleFields(NonHumanResource resource) {
        populateSimpleFields(resource.getName(), resource.getDescription(),
                resource.getNotes());
    }


    private void populateSimpleFields(NonHumanCategory category) {
        populateSimpleFields(category.getName(), category.getDescription(),
                category.getNotes());
    }


    private void populateSimpleFields(String name, String desc, String notes) {
        txtName.setText(name);
        txtDesc.setText(desc);
        txtNotes.setText(notes);
    }


    /* gets the full list of subcategories for a given category as listbox items */
    private Option[] getSubCategoryList(NonHumanCategory category) {
        Option[] subCatList = new Option[category.getSubCategoryCount()];
        int i = 0 ;
        for (NonHumanSubCategory subCat : category.getSubCategories()) {
            subCatList[i++] = new Option(subCat.getName()) ;
        }
        Arrays.sort(subCatList, new OptionComparator());
        return subCatList;
    }


    /* checks if currently selected subcat may be removed. pre: in 'edit' mode */
    private boolean subCatUnremovable() {
        String subCatStr = _sb.getNhResourcesSubcategoryChoice();
        return (subCatStr == null) || subCatStr.equals("None"); 
    }


    /* enables or disables fields depending on whether a sub category is being added */
    protected void setSubCatAddMode(boolean adding) {
        showSubCatAddFields(adding);
        disableInputFields(adding);
        lbxItems.setDisabled(adding);
        lbxSubCatItems.setDisabled(adding);
    }


    protected void disableInputFields(boolean disable) {
        txtName.setDisabled(disable);
        txtDesc.setDisabled(disable);
        txtNotes.setDisabled(disable);
        cbbMembers.setDisabled(disable);
        cbbCategory.setDisabled(disable);
        cbbSubCategory.setDisabled(disable);
        btnAddSubCat.setDisabled(disable);
        btnRemoveSubCat.setDisabled(disable || subCatUnremovable());
    }


    protected void disableSubCatButtons(boolean disable) {
        btnAddSubCat.setDisabled(disable);
        btnRemoveSubCat.setDisabled(disable || subCatUnremovable());
    }
    

    protected void showSubCatAddFields(boolean adding) {
        txtSubCat.setVisible(adding);
        btnConfirmAddSubCat.setVisible(adding);
        btnCancelAddSubCat.setVisible(adding);
    }


    protected void updateSelectedResource(NonHumanResource resource) {
        updateSelectedResource(resource, true);
    }


    protected void updateSelectedResource(NonHumanResource resource, boolean editing) {
        try {
            _sb.setSelectedNonHumanResource(resource, editing);
        }
        catch (CloneNotSupportedException cnse) {
            _msgPanel.error("Could not update form: cloning Exception");
        }
    }


    protected void createNewResource() {
        NonHumanResource resource = new NonHumanResource();
        resource.setID("_TEMP_");
        NonHumanCategory category = getFirstListedCategory();
        if (category != null) {
            resource.setCategory(category);
        }
        updateSelectedResource(resource);
    }


    private NonHumanCategory getCurrentlySelectedCategory() {
        String catID = _sb.getNhResourcesChoice();         // get selected category id
        return (catID != null) ? _orgDataSet.getNonHumanCategory(catID) : null;
    }

}