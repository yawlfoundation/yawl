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
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.faces.FacesException;
import javax.faces.event.ValueChangeEvent;
import java.io.UnsupportedEncodingException;

/*
 * Fragment bean for org data form
 *
 * @author: Michael Adams
 * Date: 23/10/2007
 */

public class pfOrgData extends AbstractFragmentBean {
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

    private Label lblAdd = new Label();

    public Label getLblAdd() {
        return lblAdd;
    }

    public void setLblAdd(Label l) {
        this.lblAdd = l;
    }

    private TextField txtName = new TextField();

    public TextField getTxtName() {
        return txtName;
    }

    public void setTxtName(TextField tf) {
        this.txtName = tf;
    }

    private Label lblBelongs = new Label();

    public Label getLblBelongs() {
        return lblBelongs;
    }

    public void setLblBelongs(Label l) {
        this.lblBelongs = l;
    }

    private Label lblDesc = new Label();

    public Label getLblGroup() {
        return lblGroup;
    }

    public void setLblGroup(Label l) {
        this.lblGroup = l;
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

    private DropDown cbbBelongs = new DropDown();

    public DropDown getCbbBelongs() {
        return cbbBelongs;
    }

    public void setCbbBelongs(DropDown dd) {
        this.cbbBelongs = dd;
    }

    private DropDown cbbGroup = new DropDown();

    public DropDown getCbbGroup() {
        return cbbGroup;
    }

    public void setCbbGroup(DropDown dd) {
        this.cbbGroup = dd;
    }

    private Label lblGroup = new Label();

    public Label getLblDesc() {
        return lblDesc;
    }

    public void setLblDesc(Label l) {
        this.lblDesc = l;
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


    public pfOrgData() {
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
            log("pfQueueUI Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void destroy() { }

    private SessionBean _sb = getSessionBean();
    private MessagePanel msgPanel = _sb.getMessagePanel();
    private ResourceManager _rm = getApplicationBean().getResourceManager() ;
    private ResourceDataSet orgDataSet = _rm.getOrgDataSet();


    public void lbxItems_processValueChange(ValueChangeEvent event) {
        _sb.setSourceTabAfterListboxSelection();
    }


    protected void populateGUI(String id, orgDataMgt.AttribType type) {
        AbstractResourceAttribute attrib = null;
        switch (type) {
            case role       : attrib = orgDataSet.getRole(id) ; break ;
            case capability : attrib = orgDataSet.getCapability(id); break;
            case position   : attrib = orgDataSet.getPosition(id); break ;
            case orggroup   : attrib = orgDataSet.getOrgGroup(id);
        }
        if (attrib != null) {
            txtDesc.setText(attrib.getDescription());
            txtNotes.setText(attrib.getNotes());
            int membership = _sb.setOrgDataMembers(attrib);
            lblMembers.setText("Members (" + membership + ")");
            if (attrib instanceof Capability) {
                txtName.setText(((Capability) attrib).getCapability());                
            }
            else if (attrib instanceof Role) {
                txtName.setText(((Role) attrib).getName());
                _sb.setOrgDataBelongsItems(_sb.getFullResourceAttributeListPlusNil("tabRoles"));
                Role owner = ((Role) attrib).getOwnerRole();
                if (owner != null)
                    cbbBelongs.setSelected(owner.getID());
                else
                    cbbBelongs.setSelected("nil");
            }
            else if (attrib instanceof Position) {
                txtName.setText(((Position) attrib).getTitle());
                _sb.setOrgDataBelongsItems(_sb.getFullResourceAttributeListPlusNil("tabPosition"));
                Position boss = ((Position) attrib).getReportsTo();
                if (boss != null)
                    cbbBelongs.setSelected(boss.getID());
                else
                    cbbBelongs.setSelected("nil");

                _sb.setOrgDataGroupItems(_sb.getFullResourceAttributeListPlusNil("tabOrgGroup"));
                OrgGroup group = ((Position) attrib).getOrgGroup();
                if (group != null)
                    cbbGroup.setSelected(group.getID());
                else
                    cbbGroup.setSelected("nil");
            }
            else if (attrib instanceof OrgGroup) {
                txtName.setText(((OrgGroup) attrib).getGroupName());
                _sb.setOrgDataBelongsItems(_sb.getFullResourceAttributeListPlusNil("tabOrgGroup"));
                OrgGroup group = ((OrgGroup) attrib).getBelongsTo();
                if (group != null)
                    cbbBelongs.setSelected(group.getID());
                else
                    cbbBelongs.setSelected("nil");

                _sb.setOrgDataGroupItems(getOrgGroupTypeOptions());
                String groupType = ((OrgGroup) attrib).get_groupType();
                cbbGroup.setSelected(StringUtil.capitalise(groupType));
            }
            lbxItems.setSelected(id);
        }
    }

    
    public void setVisibleComponents(String tabName) {
        if (tabName.equals("tabRoles")) {
            cbbGroup.setVisible(false);
            lblGroup.setVisible(false);
            cbbGroup.setItems(null);
            cbbBelongs.setVisible(true);
            lblBelongs.setVisible(true);
            lblMembers.setVisible(true);
            cbbMembers.setVisible(true);
            _sb.setOrgDataBelongsLabelText("Belongs To");
        }
        else if (tabName.equals("tabCapability")) {
            cbbGroup.setVisible(false);
            lblGroup.setVisible(false);
            cbbGroup.setItems(null);
            cbbBelongs.setVisible(false);
            lblBelongs.setVisible(false);
            cbbBelongs.setItems(null);
            lblMembers.setVisible(true);
            cbbMembers.setVisible(true);
        }
        else if (tabName.equals("tabPosition")) {
            cbbBelongs.setVisible(true);
            lblBelongs.setVisible(true);
            cbbGroup.setVisible(true);
            lblGroup.setVisible(true);
            lblMembers.setVisible(true);
            cbbMembers.setVisible(true);
            _sb.setOrgDataBelongsLabelText("Reports To");
            _sb.setOrgDataGroupLabelText("Org Group");
        }
        else if (tabName.equals("tabOrgGroup")) {
            cbbGroup.setVisible(true);
            lblGroup.setVisible(true);
            cbbGroup.setItems(null);
            cbbBelongs.setVisible(true);
            lblBelongs.setVisible(true);
            lblMembers.setVisible(false);
            cbbMembers.setVisible(false);
            _sb.setOrgDataBelongsLabelText("Belongs To");
            _sb.setOrgDataGroupLabelText("Group Type");
        }
    }

    public void setAddMode(boolean addFlag) {
        lbxItems.setDisabled(addFlag);
        if (addFlag) clearTextFields();
    }


    public void setCombosToNil() {
        if (cbbBelongs.isVisible()) cbbBelongs.setSelected("nil");
        if (cbbGroup.isVisible()) {
            if (lblGroup.getText().equals("Group Type")) {
                cbbGroup.setSelected("Group");
            }
            else {
                cbbGroup.setSelected("nil");
            }
        }
    }

    public boolean saveChanges(String id) {
        AbstractResourceAttribute attrib = null;
        String activeTab = _sb.getActiveTab();
        if (activeTab.equals("tabRoles"))
            attrib = orgDataSet.getRole(id) ;
        else if (activeTab.equals("tabCapability"))
            attrib = orgDataSet.getCapability(id);
        else if (activeTab.equals("tabPosition"))
            attrib = orgDataSet.getPosition(id);
        else if (activeTab.equals("tabOrgGroup"))
            attrib = orgDataSet.getOrgGroup(id);

        if ((attrib != null) && isValidNameChange(attrib)) {
            String belongsToID = (String) cbbBelongs.getSelected();
            if (hasCyclicReferences(attrib, belongsToID)) return false;

            setCommonFields(attrib);
            String name = toUTF8((String) txtName.getText());
            if (attrib instanceof Capability) {
                ((Capability) attrib).setCapability(name);
            }
            else if (attrib instanceof Role) {
                ((Role) attrib).setName(name);
                Role owner = orgDataSet.getRole(belongsToID) ;
                ((Role) attrib).setOwnerRole(owner);
            }
            else if (attrib instanceof Position) {
                ((Position) attrib).setTitle(name);
                Position boss = orgDataSet.getPosition(belongsToID);
                ((Position) attrib).setReportsTo(boss);

                String groupID = (String) cbbGroup.getSelected();
                OrgGroup group = orgDataSet.getOrgGroup(groupID);
                ((Position) attrib).setOrgGroup(group);
            }
            else if (attrib instanceof OrgGroup) {
                ((OrgGroup) attrib).setGroupName(name);
                OrgGroup group = orgDataSet.getOrgGroup(belongsToID);
                ((OrgGroup) attrib).setBelongsTo(group);
                String groupType = ((String) cbbGroup.getSelected()).toUpperCase();
                ((OrgGroup) attrib).set_groupType(groupType.trim());
                orgDataSet.updateOrgGroup((OrgGroup) attrib);
            }

            orgDataSet.updateResourceAttribute(attrib);
        }
        return true;
    }

    private boolean hasCyclicReferences(AbstractResourceAttribute attrib, String belongsID) {

        // if belongsID == 'nil', there are no references, cyclic or otherwise
        if (belongsID.equals("nil")) return false;

        // first check for self references
        if (attrib.getID().equals(belongsID)) {
           String errSelfReference = "A %s cannot %s to itself.";
           if (attrib instanceof Role)
               msgPanel.error(String.format(errSelfReference, "Role", "belong"));
           else if (attrib instanceof OrgGroup)
               msgPanel.error(String.format(errSelfReference, "Org Group", "belong"));
           else if (attrib instanceof Position)
               msgPanel.error(String.format(errSelfReference, "Position", "report"));
           return true;
        }

        // now check for cyclics
        String cyclicErrMsg = orgDataSet.checkCyclicAttributeReference(attrib, belongsID);
        if (cyclicErrMsg != null) {
            msgPanel.error(cyclicErrMsg);
            return true;
        }

        // ok - no cyclic references
        return false;
    }


    public boolean addNewItem(String activeTab) {
        String newName = toUTF8((String) txtName.getText());
        if (newName == null) {
            msgPanel.error("Please enter a name for the new Item.");
            return false;
        }

        String belongsToID = (String) cbbBelongs.getSelected();

        if (activeTab.equals("tabRoles")) {
            if (! orgDataSet.isKnownRoleName(newName)) {
                Role role = new Role(newName) ;
                role.setOwnerRole(orgDataSet.getRole(belongsToID));
                setCommonFields(role);
                orgDataSet.addRole(role);
                lbxItems.setSelected(role.getID());
            }
            else {
                addDuplicationError("a Role");
                return false;
            }
        }
        else if (activeTab.equals("tabCapability")) {
            if (! orgDataSet.isKnownCapabilityName(newName)) {
                Capability capability = new Capability(newName, null);
                setCommonFields(capability);
                orgDataSet.addCapability(capability);
                lbxItems.setSelected(capability.getID());
            }
            else {
                addDuplicationError("a Capability");
                return false;
            }
        }
        else if (activeTab.equals("tabPosition")) {
            if (! orgDataSet.isKnownPositionName(newName)) {
                Position position = new Position(newName);
                position.setReportsTo(orgDataSet.getPosition(belongsToID));
                position.setOrgGroup(orgDataSet.getOrgGroup((String) cbbGroup.getSelected()));
                setCommonFields(position);
                orgDataSet.addPosition(position);
                lbxItems.setSelected(position.getID());
            }
            else {
                addDuplicationError("a Position");
                return false;
            }
        }
        else if (activeTab.equals("tabOrgGroup")) {
            if (! orgDataSet.isKnownOrgGroupName(newName)) {
                OrgGroup orgGroup = new OrgGroup();
                orgGroup.setGroupName(newName);
                orgGroup.setBelongsTo(orgDataSet.getOrgGroup(belongsToID));
                orgGroup.set_groupType(((String) cbbGroup.getSelected()).toUpperCase().trim());
                setCommonFields(orgGroup);
                orgDataSet.addOrgGroup(orgGroup);
                lbxItems.setSelected(orgGroup.getID());
            }
            else {
                addDuplicationError("an Org Group");
                return false;
            }
        }

        txtName.setText("");
        return true ;
    }


    private boolean isValidNameChange(AbstractResourceAttribute attrib) {
        String name = toUTF8((String) txtName.getText());
        if (attrib instanceof Capability) {
            if (! name.equals(((Capability) attrib).getCapability())) {  // if name changed
                if (orgDataSet.isKnownCapabilityName(name)) {
                    addDuplicationError("a Capability") ;
                    return false;
                }
            }
        }
        else if (attrib instanceof Role) {
            if (! name.equals(((Role) attrib).getName())) {
                if (orgDataSet.isKnownRoleName(name)) {
                    addDuplicationError("a Role") ;
                    return false;
                }
            }
        }
        else if (attrib instanceof Position) {
            if (! name.equals(((Position) attrib).getTitle())) {
                if (orgDataSet.isKnownPositionName(name)) {
                    addDuplicationError("a Position") ;
                    return false;
                }
            }
        }
        else if (attrib instanceof OrgGroup) {
            if (! name.equals(((OrgGroup) attrib).getGroupName())) {
                if (orgDataSet.isKnownOrgGroupName(name)) {
                    addDuplicationError("an Org Group") ;
                    return false;
                }
            }
        }
        return true;
    }


    private void setCommonFields(AbstractResourceAttribute attrib) {
        attrib.setDescription(toUTF8((String) txtDesc.getText()));
        attrib.setNotes(toUTF8((String) txtNotes.getText()));
    }


    private void addDuplicationError(String type) {
        String dupErrMsg = "There is already %s by that name - please choose another.";
        msgPanel.error(String.format(dupErrMsg, type)) ;
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
        cbbBelongs.setItems(null);
        cbbGroup.setItems(null);
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

    public Option[] getOrgGroupTypeOptions() {
        OrgGroup.GroupType[] groupTypes = OrgGroup.GroupType.values();
        Option[] result = new Option[groupTypes.length];
        for (int i = 0; i < groupTypes.length; i++) {
            result[i] = new Option(StringUtil.capitalise(groupTypes[i].name()));
        }
        return result;
    }
    
    /*
    This method is required to workaround a bug in JSF 1.2 page fragments, where the
    encoding for the fragment can't be changed from the default ISO-8859-1 when the
    parent form contains a file upload component
     */
    private String toUTF8(String s) {
        if (s == null) return s;
        try {
            return new String(s.getBytes("ISO-8859-1"), "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            return s;
        }
    }

}
