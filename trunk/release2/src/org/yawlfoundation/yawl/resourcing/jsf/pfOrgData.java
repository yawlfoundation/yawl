/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.model.Option;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.faces.FacesException;
import javax.faces.event.ValueChangeEvent;

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

    private TextField txtAdd = new TextField();

    public TextField getTxtAdd() {
        return txtAdd;
    }

    public void setTxtAdd(TextField tf) {
        this.txtAdd = tf;
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


    public void lbxItems_processValueChange(ValueChangeEvent event) {
        _sb.setSourceTabAfterListboxSelection();
    }


    protected void populateGUI(String id, orgDataMgt.AttribType type) {
        AbstractResourceAttribute attrib = null;
        switch (type) {
            case role       : attrib = _rm.getRole(id) ; break ;
            case capability : attrib = _rm.getCapability(id); break;
            case position   : attrib = _rm.getPosition(id); break ;
            case orggroup   : attrib = _rm.getOrgGroup(id);
        }
        if (attrib != null) {
            txtDesc.setText(attrib.getDescription());
            txtNotes.setText(attrib.getNotes());
            if (attrib instanceof Role) {
                _sb.setOrgDataBelongsItems(_sb.getFullResourceAttributeListPlusNil("tabRoles"));
                Role owner = ((Role) attrib).getOwnerRole();
                if (owner != null)
                    cbbBelongs.setSelected(owner.getID());
                else
                    cbbBelongs.setSelected("nil");
            }
            else if (attrib instanceof Position) {
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

    
    private Option[] optionise(String item) {
        Option[] result = new Option[1];
        result[0] = new Option(item);
        return result;
    }


    public void setVisibleComponents(String tabName) {
        if (tabName.equals("tabRoles")) {
            cbbGroup.setVisible(false);
            lblGroup.setVisible(false);
            cbbGroup.setItems(null);
            cbbBelongs.setVisible(true);
            lblBelongs.setVisible(true);
            _sb.setOrgDataBelongsLabelText("Belongs To");
        }
        else if (tabName.equals("tabCapability")) {
            cbbGroup.setVisible(false);
            lblGroup.setVisible(false);
            cbbGroup.setItems(null);
            cbbBelongs.setVisible(false);
            lblBelongs.setVisible(false);
            cbbBelongs.setItems(null);
        }
        else if (tabName.equals("tabPosition")) {
            cbbBelongs.setVisible(true);
            lblBelongs.setVisible(true);
            cbbGroup.setVisible(true);
            lblGroup.setVisible(true);
            _sb.setOrgDataBelongsLabelText("Reports To");
            _sb.setOrgDataGroupLabelText("Org Group");
        }
        else if (tabName.equals("tabOrgGroup")) {
            cbbGroup.setVisible(true);
            lblGroup.setVisible(true);
            cbbGroup.setItems(null);
            cbbBelongs.setVisible(true);
            lblBelongs.setVisible(true);
            _sb.setOrgDataBelongsLabelText("Belongs To");
            _sb.setOrgDataGroupLabelText("Group Type");
        }
    }

    public void setAddMode(boolean addFlag) {
        lblAdd.setVisible(addFlag);
        txtAdd.setVisible(addFlag);
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
            attrib = _rm.getRole(id) ;
        else if (activeTab.equals("tabCapability"))
            attrib = _rm.getCapability(id);
        else if (activeTab.equals("tabPosition"))
            attrib = _rm.getPosition(id);
        else if (activeTab.equals("tabOrgGroup"))
            attrib = _rm.getOrgGroup(id);

        if (attrib != null) {
            String belongsToID = (String) cbbBelongs.getSelected();
            if (hasCyclicReferences(attrib, belongsToID)) return false;

            setCommonFields(attrib);

            if (attrib instanceof Role) {
                Role owner = _rm.getRole(belongsToID) ;
                ((Role) attrib).setOwnerRole(owner);
            }
            else if (attrib instanceof Position) {
                Position boss = _rm.getPosition(belongsToID);
                ((Position) attrib).setReportsTo(boss);

                String groupID = (String) cbbGroup.getSelected();
                OrgGroup group = _rm.getOrgGroup(groupID);
                ((Position) attrib).setOrgGroup(group);
            }
            else if (attrib instanceof OrgGroup) {
                OrgGroup group = _rm.getOrgGroup(belongsToID);
                ((OrgGroup) attrib).setBelongsTo(group);
                String groupType = ((String) cbbGroup.getSelected()).toUpperCase();
                ((OrgGroup) attrib).set_groupType(groupType.trim());
                _rm.updateOrgGroup((OrgGroup) attrib);
            }

            _rm.updateResourceAttribute(attrib);
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
        String cyclicErrMsg = _rm.checkCyclicAttributeReference(attrib, belongsID);
        if (cyclicErrMsg != null) {
            msgPanel.error(cyclicErrMsg);
            return true;
        }

        // ok - no cyclic references
        return false;
    }


    public boolean addNewItem(String activeTab) {
        if ( txtAdd.getText() == null) {
            msgPanel.error("Please enter a name for the new Item");
            return false;
        }

        String belongsToID = (String) cbbBelongs.getSelected();

        if (activeTab.equals("tabRoles")) {
            Role role = new Role((String) txtAdd.getText()) ;
            role.setOwnerRole(_rm.getRole(belongsToID));
            setCommonFields(role);
            _rm.addRole(role);
            lbxItems.setSelected(role.getID());
        }
        else if (activeTab.equals("tabCapability")) {
            Capability capability = new Capability((String) txtAdd.getText(), null);
            setCommonFields(capability);
            _rm.addCapability(capability);
            lbxItems.setSelected(capability.getID());
        }
        else if (activeTab.equals("tabPosition")) {
            Position position = new Position((String) txtAdd.getText());
            position.setReportsTo(_rm.getPosition(belongsToID));
            position.setOrgGroup(_rm.getOrgGroup((String) cbbGroup.getSelected()));
            setCommonFields(position);
            _rm.addPosition(position);
            lbxItems.setSelected(position.getID());
        }
        else if (activeTab.equals("tabOrgGroup")) {
            OrgGroup orgGroup = new OrgGroup();
            orgGroup.setGroupName((String) txtAdd.getText());
            orgGroup.setBelongsTo(_rm.getOrgGroup(belongsToID));
            orgGroup.set_groupType(((String) cbbGroup.getSelected()).toUpperCase().trim());
            setCommonFields(orgGroup);
            _rm.addOrgGroup(orgGroup);
            lbxItems.setSelected(orgGroup.getID());
        }

        txtAdd.setText("");
        return true ;
    }


    private void setCommonFields(AbstractResourceAttribute attrib) {
        attrib.setDescription((String) txtDesc.getText());
        attrib.setNotes((String) txtNotes.getText());
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

}
