/*
 * pfQueueUI.java
 *
 * Created on October 23, 2007, 2:37 PM
 * Copyright adamsmj
 */
package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractFragmentBean;
import com.sun.rave.web.ui.component.*;
import com.sun.rave.web.ui.model.Option;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.resource.*;

import javax.faces.FacesException;
import javax.faces.event.ValueChangeEvent;

/**
 * <p>Fragment bean that corresponds to a similarly named JSP page
 * fragment.  This class contains component definitions (and initialization
 * code) for all components that you have defined on this fragment, as well as
 * lifecycle methods and event handlers where you may add behavior
 * to respond to incoming events.</p>
 */
public class pfOrgData extends AbstractFragmentBean {
    // <editor-fold defaultstate="collapsed" desc="Creator-managed Component Definition">
    private int __placeholder;

    /**
     * <p>Automatically managed component initialization. <strong>WARNING:</strong>
     * This method is automatically generated, so any user-specified code inserted
     * here is subject to being replaced.</p>
     */
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


    // </editor-fold>

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


    /**
     * <p>Callback method that is called whenever a page containing
     * this page fragment is navigated to, either directly via a URL,
     * or indirectly via page navigation.  Override this method to acquire
     * resources that will be needed for event handlers and lifecycle methods.</p>
     *
     * <p>The default implementation does nothing.</p>
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
            log("pfQueueUI Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
        // </editor-fold>
        // Perform application initialization that must complete
        // *after* managed components are initialized
        // TODO - add your own initialization code here

    }

    /**
     * <p>Callback method that is called after rendering is completed for
     * this request, if <code>init()</code> was called.  Override this
     * method to release resources acquired in the <code>init()</code>
     * resources that will be needed for event handlers and lifecycle methods.</p>
     *
     * <p>The default implementation does nothing.</p>
     */
    public void destroy() {
    }

    private MessagePanel msgPanel = getSessionBean().getMessagePanel();

    public void lbxItems_processValueChange(ValueChangeEvent event) {
        getSessionBean().setSourceTabAfterListboxSelection();
    }


    protected void populateGUI(String id, orgDataMgt.AttribType type) {
        ResourceManager rm = getApplicationBean().getResourceManager() ;
        SessionBean sb = getSessionBean();
        AbstractResourceAttribute attrib = null;
        switch (type) {
            case role       : attrib = rm.getRole(id) ; break ;
            case capability : attrib = rm.getCapability(id); break;
            case position   : attrib = rm.getPosition(id); break ;
            case orggroup   : attrib = rm.getOrgGroup(id);
        }
        if (attrib != null) {
            txtDesc.setText(attrib.getDescription());
            txtNotes.setText(attrib.getNotes());
            if (attrib instanceof Role) {
                sb.setOrgDataBelongsItems(sb.getFullResourceAttributeListPlusNil("tabRoles"));
                Role owner = ((Role) attrib).getOwnerRole();
                if (owner != null)
                    cbbBelongs.setSelected(owner.getID());
                else
                    cbbBelongs.setSelected("nil");
            }
            else if (attrib instanceof Position) {
                sb.setOrgDataBelongsItems(sb.getFullResourceAttributeListPlusNil("tabPosition"));
                Position boss = ((Position) attrib).getReportsTo();
                if (boss != null)
                    cbbBelongs.setSelected(boss.getID());
                else
                    cbbBelongs.setSelected("nil");

                sb.setOrgDataGroupItems(sb.getFullResourceAttributeListPlusNil("tabOrgGroup"));
                OrgGroup group = ((Position) attrib).getOrgGroup();
                if (group != null)
                    cbbGroup.setSelected(group.getID());
                else
                    cbbGroup.setSelected("nil");
            }
            else if (attrib instanceof OrgGroup) {
                sb.setOrgDataBelongsItems(sb.getFullResourceAttributeListPlusNil("tabOrgGroup"));
                OrgGroup group = ((OrgGroup) attrib).getBelongsTo();
                if (group != null)
                    cbbBelongs.setSelected(group.getID());
                else
                    cbbBelongs.setSelected("nil");

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
            getSessionBean().setOrgDataBelongsLabelText("Belongs To");
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
            getSessionBean().setOrgDataBelongsLabelText("Reports To");
        }
        else if (tabName.equals("tabOrgGroup")) {
            cbbGroup.setVisible(false);
            lblGroup.setVisible(false);
            cbbGroup.setItems(null);
            cbbBelongs.setVisible(true);
            lblBelongs.setVisible(true);
            getSessionBean().setOrgDataBelongsLabelText("Belongs To");
        }
    }

    public void setAddMode(boolean addFlag) {
        lblAdd.setVisible(addFlag);
        txtAdd.setVisible(addFlag);
        lbxItems.setDisabled(addFlag);
    }


    public void setCombosToNil() {
        if (cbbBelongs.isVisible()) cbbBelongs.setSelected("nil");
        if (cbbGroup.isVisible()) cbbGroup.setSelected("nil");        
    }

    public boolean saveChanges(String id) {
        ResourceManager rm = getApplicationBean().getResourceManager() ;
        AbstractResourceAttribute attrib = null;
        String activeTab = getSessionBean().getActiveTab();
        if (activeTab.equals("tabRoles"))
            attrib = rm.getRole(id) ;
        else if (activeTab.equals("tabCapability"))
            attrib = rm.getCapability(id);
        else if (activeTab.equals("tabPosition"))
            attrib = rm.getPosition(id);
        else if (activeTab.equals("tabOrgGroup"))
            attrib = rm.getOrgGroup(id);

        if (attrib != null) {
            String belongsToID = (String) cbbBelongs.getSelected();
            if (! validateUpdates(attrib, belongsToID)) return false;

            setCommonFields(attrib);

            if (attrib instanceof Role) {
                Role owner = rm.getRole(belongsToID) ;
                ((Role) attrib).setOwnerRole(owner);
            }
            else if (attrib instanceof Position) {
                Position boss = rm.getPosition(belongsToID);
                ((Position) attrib).setReportsTo(boss);

                String groupID = (String) cbbGroup.getSelected();
                OrgGroup group = rm.getOrgGroup(groupID);
                ((Position) attrib).setOrgGroup(group);
            }
            else if (attrib instanceof OrgGroup) {
                OrgGroup group = rm.getOrgGroup(belongsToID);
                ((OrgGroup) attrib).setBelongsTo(group);
                rm.updateOrgGroup((OrgGroup) attrib);
            }

            rm.updateResourceAttribute(attrib);
        }
        return true;
    }

    private boolean validateUpdates(AbstractResourceAttribute attrib, String belongsID) {
        String attribID = attrib.getID();
        if (attribID.equals(belongsID)) {
           if (attrib instanceof Role)
               msgPanel.error("A Role cannot belong to itself");
           else if (attrib instanceof OrgGroup)
               msgPanel.error("An Org Group cannot belong to itself");
           else if (attrib instanceof Position)
               msgPanel.error("A Position cannot report to itself");
           return false;
        }
        return true;
    }


    public boolean addNewItem(String activeTab) {
        if ( txtAdd.getText() == null) {
            msgPanel.error("Please enter a name for the new Item");
            return false;
        }

        ResourceManager rm = getApplicationBean().getResourceManager() ;
        String belongsToID = (String) cbbBelongs.getSelected();

        if (activeTab.equals("tabRoles")) {
            Role role = new Role((String) txtAdd.getText()) ;
            role.setOwnerRole(rm.getRole(belongsToID));
            setCommonFields(role);
            rm.addRole(role);
            lbxItems.setSelected(role.getID());
        }
        else if (activeTab.equals("tabCapability")) {
            Capability capability = new Capability((String) txtAdd.getText(), null);
            setCommonFields(capability);
            rm.addCapability(capability);
            lbxItems.setSelected(capability.getID());
        }
        else if (activeTab.equals("tabPosition")) {
            Position position = new Position((String) txtAdd.getText());
            position.setReportsTo(rm.getPosition(belongsToID));
            position.setOrgGroup(rm.getOrgGroup((String) cbbGroup.getSelected()));
            setCommonFields(position);
            rm.addPosition(position);
            lbxItems.setSelected(position.getID());
        }
        else if (activeTab.equals("tabOrgGroup")) {
            OrgGroup orgGroup = new OrgGroup();
            orgGroup.setGroupName((String) txtAdd.getText());
            orgGroup.setBelongsTo(rm.getOrgGroup(belongsToID));
            orgGroup.setGroupType(OrgGroup.GroupType.GROUP);           // default type
            setCommonFields(orgGroup);
            rm.addOrgGroup(orgGroup);
            lbxItems.setSelected(orgGroup.getID());
        }

        txtAdd.setText("");
        return true ;
    }


    private void setCommonFields(AbstractResourceAttribute attrib) {
        attrib.setDescription((String) txtDesc.getText());
        attrib.setNotes((String) txtNotes.getText());
    }


    public void clearFieldsAfterRemove() {
        txtDesc.setText("");
        txtNotes.setText("");
        lbxItems.setSelected(getFirstListboxItem());
    }

    public void clearFields() {
        txtDesc.setText("");
        txtNotes.setText("");        
    }

    private String getFirstListboxItem() {
        Option[] items = (Option[]) lbxItems.getItems();
        if ((items != null) && items.length > 0) {
            Option item = items[0];
            if (item != null) return (String) item.getValue();
        }
        return null;
    }

}