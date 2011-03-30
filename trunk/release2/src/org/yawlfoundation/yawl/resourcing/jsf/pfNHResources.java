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

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.model.Option;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.orgdata.ResourceDataSet;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.NonHumanSubCategoryComparator;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanSubCategory;

import javax.faces.FacesException;
import javax.faces.event.ValueChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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


    public void lbxItems_processValueChange(ValueChangeEvent event) {
        _sb.setSourceTabAfterListboxSelection();
        _sb.setSelectedNonHumanResource(_orgDataSet.getNonHumanResource((String) event.getNewValue()));
        _sb.setOrigCategory(null);
        _sb.setOrigSubcategory(null);
    }

    public void lbxSubCatItems_processValueChange(ValueChangeEvent event) {
        _sb.setSourceTabAfterListboxSelection();
    }

    public void cbbCategory_processValueChange(ValueChangeEvent event) {
        NonHumanResource resource = _sb.getSelectedNonHumanResource();
        if (resource != null) {
            NonHumanCategory category = _orgDataSet.getNonHumanCategory((String) event.getNewValue());
            if (category != null) {
                if (_sb.getOrigCategory() == null) {
                    _sb.setOrigCategory((String) event.getOldValue());
                }
                if (_sb.getOrigSubcategory() == null) {
                    _sb.setOrigSubcategory(resource.getSubCategoryName());
                }
                resource.setCategory(category);
                resource.setSubCategory("None");
            }
        }
    }

    public void cbbSubCategory_processValueChange(ValueChangeEvent event) {
        NonHumanResource resource = _sb.getSelectedNonHumanResource();
        if (resource != null) {
            resource.setSubCategory((String) event.getNewValue());
            if (_sb.getOrigSubcategory() == null) {
                _sb.setOrigSubcategory((String) event.getOldValue());
            }
        }
    }


    public String btnAddSubCat_action() {
        _sb.setSubCatAddMode(true);
        setSubCatAddMode(true);
        return null;
    }

    public String btnRemoveSubCat_action() {
        String catID = _sb.getNhResourcesChoice();
        if (catID != null) {
            NonHumanCategory category = _orgDataSet.getNonHumanCategory(catID);
            if (category != null) {
                if (category.removeSubCategory(_sb.getNhResourcesSubcategoryChoice())) {
                    _orgDataSet.updateNonHumanCategory(category);
                }
                else _msgPanel.error("Failed to remove subcategory.");
            }
        }
        return null;
    }


    public String btnConfirmAddSubCat_action() {
        String name = (String) txtSubCat.getText();
        if ((name != null) && (name.length() > 0)) {
            String catID = _sb.getNhResourcesChoice();
            if (catID != null) {
                NonHumanCategory category = _orgDataSet.getNonHumanCategory(catID);
                if (category != null) {
                    if (! category.hasSubCategory(name)) {
                        category.addSubCategory(name);
                        _orgDataSet.updateNonHumanCategory(category);
                        _sb.setSubCatAddMode(false);
                        txtSubCat.setText(null);
                    }
                    else _msgPanel.error("Subcategory already exists: " + name);
                }
            }
        }
        else _msgPanel.error("Please enter a subcategory to add.");
        return null;
    }

    public String btnCancelAddSubCat_action() {
        _sb.setSubCatAddMode(false);
        return null;
    }

    protected void populateGUI(String id, nonHumanMgt.AttribType type) {
        switch (type) {
            case resource : populateGUI(_sb.getSelectedNonHumanResource(), id); break;
            case category : populateGUI(_orgDataSet.getNonHumanCategory(id)); break;
        }
        lbxItems.setSelected(id);
        setSubCatAddMode(_sb.getSubCatAddMode());
    }


    private void populateGUI(NonHumanResource resource, String id) {
        if (resource == null) {
            resource = _orgDataSet.getNonHumanResource(id);
            _sb.setSelectedNonHumanResource(resource);
        }
        if (resource != null) {
            txtDesc.setText(resource.getDescription());
            txtNotes.setText(resource.getNotes());
            txtName.setText(resource.getName());
            _sb.setNhResourcesCategoryItems(_sb.getNhResourcesCategoryList());
            NonHumanCategory category = resource.getCategory();
            _sb.setNhResourceCategoryLabelText("Category");
            cbbCategory.setSelected(category.getID());
            _sb.setNhResourcesSubcategoryItems(getSubCategoryList(category));
            cbbSubCategory.setSelected(resource.getSubCategoryName());
        }
    }


    private void populateGUI(NonHumanCategory category) {
        if (category != null) {
            txtDesc.setText(category.getDescription());
            txtNotes.setText(category.getNotes());
            txtName.setText(category.getName());
            Option[] subCatItems = getSubCategoryList(category);
            _sb.setNhResourcesSubcategoryItems(subCatItems);  
            int membership = _sb.setCategoryMembers(category);
            lblMembers.setText("Members (" + membership + ")");
            _sb.setNhResourceCategoryLabelText("Subcategories");
            enableAsSelected();
        }
    }


    public void setVisibleComponents(String tabName) {
        boolean catTab = tabName.equals("tabCategories");
        cbbCategory.setVisible(! catTab);
        lblSubCategory.setVisible(! catTab);
        cbbSubCategory.setVisible(! catTab);
        lblMembers.setVisible(catTab);
        cbbMembers.setVisible(catTab);
        lbxSubCatItems.setVisible(catTab);
        btnAddSubCat.setVisible(catTab);
        btnRemoveSubCat.setVisible(catTab);
        if (catTab) {
            cbbCategory.setItems(null);
            cbbSubCategory.setItems(null);
        }
        else lbxSubCatItems.setItems(null);
    }

    
    public void setAddMode(boolean addFlag) {
        lbxItems.setDisabled(addFlag);
        lbxSubCatItems.setDisabled(addFlag);
        btnAddSubCat.setDisabled(addFlag);
        btnRemoveSubCat.setDisabled(addFlag);
        cbbMembers.setDisabled(addFlag);
        if (addFlag) clearTextFields();
    }


    public void resetResource() {
        NonHumanResource resource = _sb.getSelectedNonHumanResource();
        if (resource != null) {
            if (_sb.getOrigCategory() != null) {
                NonHumanCategory category = _orgDataSet.getNonHumanCategory(_sb.getOrigCategory());
                resource.setCategory(category);
            }
            if (_sb.getOrigSubcategory() != null) {
                resource.setSubCategory(_sb.getOrigSubcategory());
            }
        }
        _sb.setOrigCategory(null);
        _sb.setOrigSubcategory(null);
    }


    public boolean saveChanges(String id) {
        String activeTab = _sb.getActiveTab();
        if (activeTab.equals("tabResources"))
            return saveResourceChanges(id) ;
        else if (activeTab.equals("tabCategories"))
            return saveCategoryChanges(id);

        return false;
    }


    public boolean saveResourceChanges(String id) {
        NonHumanResource resource = _sb.getSelectedNonHumanResource();
        if (resource == null) {
            _msgPanel.error("Invalid resource id: " + id);
            return false;
        }
        String name = (String) txtName.getText();
        if ((! name.equals(resource.getName())) && _orgDataSet.isKnownNonHumanResourceName(name)) {
            addDuplicationError("Resource");
            return false;
        }

        resource.setName(name);
        resource.setDescription((String) txtDesc.getText());
        resource.setNotes((String) txtNotes.getText());

        String catID = (String) cbbCategory.getSelected();
        NonHumanCategory category = _orgDataSet.getNonHumanCategory(catID);
        if (category != null) {
            resource.setCategory(category);
        }
        
        String subcat = (String) cbbSubCategory.getSelected();
        if (subcat != null) {
            resource.setSubCategory(subcat);
        }
        _sb.setOrigCategory(null);
        _sb.setOrigSubcategory(null);
        _orgDataSet.updateNonHumanResource(resource);
        return true;
    }


    public boolean saveCategoryChanges(String id) {
        NonHumanCategory category = _orgDataSet.getNonHumanCategory(id);
        if (category == null) {
            _msgPanel.error("Invalid category id: " + id);
            return false;
        }
        String name = (String) txtName.getText();
        if ((! name.equals(category.getName())) && _orgDataSet.isKnownNonHumanCategoryName(name)) {
            addDuplicationError("Category");
            return false;
        }

        category.setName(name);
        category.setDescription((String) txtDesc.getText());
        category.setNotes((String) txtNotes.getText());

        _orgDataSet.updateNonHumanCategory(category);
        return true;
    }


    public boolean addNewItem(String activeTab) {
        String newName = (String) txtName.getText();
        if (newName == null) {
            _msgPanel.error("Please enter a name for the new Item.");
            return false;
        }
        if (activeTab.equals("tabResources"))
            return addResource() ;
        else if (activeTab.equals("tabCategories"))
            return addCategory();

        return false;
    }


    public boolean addResource() {
        String name = (String) txtName.getText();
        if (! _orgDataSet.isKnownNonHumanResourceName(name)) {
            String catID = (String) cbbCategory.getSelected();
            NonHumanCategory category = _orgDataSet.getNonHumanCategory(catID);
            String subCat = (String) cbbSubCategory.getSelected();
            NonHumanResource resource = new NonHumanResource(name, category, subCat);
            resource.setDescription((String) txtDesc.getText());
            resource.setNotes((String) txtNotes.getText());
            lbxItems.setSelected(resource.getID());
            txtName.setText("");
            String result = _orgDataSet.addNonHumanResource(resource);
            if (_rm.successful(result)) {
                return true;
            }
            else _msgPanel.error(_msgPanel.format(result));
        }
        else {
            addDuplicationError("Resource");
        }
        return false;
    }


    public boolean addCategory() {
        String name = (String) txtName.getText();
        if (! _orgDataSet.isKnownNonHumanCategoryName(name)) {
            NonHumanCategory category = new NonHumanCategory(name);
            category.addSubCategory("None");
            category.setDescription((String) txtDesc.getText());
            category.setNotes((String) txtNotes.getText());
            txtName.setText("");
            String result = _orgDataSet.addNonHumanCategory(category);
            if (_rm.successful(result)) {
                return true;
            }
            else _msgPanel.error(_msgPanel.format(result));
        }
        else {
            addDuplicationError("Category");
        }
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

    public void clearFields() {
        clearTextFields();
        cbbCategory.setItems(null);
        cbbSubCategory.setItems(null);
        _sb.setOrgDataBelongsItems(null);
        _sb.setOrgDataGroupItems(null);
        _sb.setOrgDataChoice(null);
        _sb.setOrgDataBelongsChoice(null);
        _sb.setOrgDataGroupChoice(null);
    }

    private String getFirstListboxItem() {
        Option[] items = (Option[]) lbxItems.getItems();
        if ((items != null) && items.length > 0) {
            Option item = items[0];
            if (item != null) return (String) item.getValue();
        }
        return null;
    }


    private Option[] getSubCategoryList(NonHumanCategory category) {
        List<NonHumanSubCategory> subCatList =
                new ArrayList<NonHumanSubCategory>(category.getSubCategories());
        if (! subCatList.isEmpty()) {
            Collections.sort(subCatList, new NonHumanSubCategoryComparator());
            Option[] result = new Option[subCatList.size()];
            int i = 0 ;
            for (NonHumanSubCategory subCat : subCatList) {
                result[i++] = new Option(subCat.getName()) ;
            }
            return result ;
        }
        else return null ;
    }


    private void enableAsSelected() {
        if (! _sb.getSubCatAddMode()) {
            String subCatStr = _sb.getNhResourcesSubcategoryChoice();
            if (subCatStr != null) {
                btnRemoveSubCat.setDisabled(subCatStr.equals("None"));
            }
        }    
    }


    private void setSubCatAddMode(boolean addFlag) {
        txtSubCat.setVisible(addFlag);
        btnConfirmAddSubCat.setVisible(addFlag);
        btnCancelAddSubCat.setVisible(addFlag);
        lbxItems.setDisabled(addFlag);
        txtName.setDisabled(addFlag);
        txtDesc.setDisabled(addFlag);
        txtNotes.setDisabled(addFlag);
        cbbMembers.setDisabled(addFlag);
        lbxSubCatItems.setDisabled(addFlag);
        btnAddSubCat.setDisabled(addFlag);
        btnRemoveSubCat.setDisabled(addFlag);
    }

}