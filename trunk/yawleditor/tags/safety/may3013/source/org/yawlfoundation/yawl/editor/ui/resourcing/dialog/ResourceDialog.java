package org.yawlfoundation.yawl.editor.ui.resourcing.dialog;

import org.yawlfoundation.yawl.editor.core.resourcing.DynParam;
import org.yawlfoundation.yawl.editor.core.resourcing.TaskResources;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.resourcing.filters.AbstractFilter;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * @author Michael Adams
 * @date 2/08/12
 */
public class ResourceDialog extends JDialog implements ActionListener, ItemListener {

    private ResourceTablePanel participantTablePanel;
    private ResourceTablePanel roleTablePanel;
    private ResourceTablePanel netParamTablePanel;
    private ResourceTablePanel filterTablePanel;
    private JPanel constraintsPanel;
    private JPanel offerPanelContent;
    private JPanel allocatePanelContent;
    private JCheckBox chkOffer;
    private JCheckBox chkAllocate;
    private JCheckBox chkStart;
    private JComboBox cbxAllocations;
    private JButton btnApply;

    private YNet net;
    private YTask task;
    private TaskResources resources;


    public ResourceDialog(YNet net, YAWLTask task) {
        super();
        initialise(net);
        this.task = getTask(task.getID());
        resources = SpecificationModel.getHandler().getTaskResources(net.getID(), task.getID());
        setTitle("Resource Settings for Task " + task.getID());
        add(getContent());
        setTableValues();
        setPreferredSize(new Dimension(668, 665));
        pack();
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (! action.equals("Cancel")) {
            updateTaskResources();
            btnApply.setEnabled(false);
        }

        if (! action.equals("Apply")) {
            setVisible(false);
        }
    }


    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        boolean selected = e.getStateChange() == ItemEvent.SELECTED;
        if (source == chkOffer) {
            enablePanelContent(offerPanelContent, selected);
        }
        else if (source == chkAllocate) {
            enablePanelContent(allocatePanelContent, selected);
        }
    }


    protected void enableApplyButton() { btnApply.setEnabled(true); }


    private YTask getTask(String name) {
        return (YTask) net.getNetElement(name);
    }


    private void initialise(YNet net) {
        this.net = net;
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setLocationByPlatform(true);
    }


    private JPanel getContent() {
        JTabbedPane pane = new JTabbedPane();
        pane.addTab("Primary Resources", getPrimaryResourcesContent());
        pane.addTab("Secondary Resources", getSecondaryResourcesContent());
        pane.addTab("Task Privileges", getTaskPrivilegesContent());

        JPanel content = new JPanel(new BorderLayout());
        content.add(pane, BorderLayout.CENTER);
        content.add(createButtonBar(), BorderLayout.SOUTH);
        return content;
    }


    private JPanel getPrimaryResourcesContent() {
        JPanel content = new JPanel();
        content.add(createOfferPanel());
        content.add(createAllocatePanel());
        content.add(createStartPanel());
        return content;
    }


    private JPanel getSecondaryResourcesContent() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("this is a panel"));
        return panel;
    }

    private JPanel getTaskPrivilegesContent() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("this is a panel"));
        return panel;
    }

    private JPanel createOfferPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Offer"));
        chkOffer = createCheckBox("Enable System Offer", KeyEvent.VK_O);
        panel.add(createCheckBoxPanel(chkOffer), BorderLayout.NORTH);
        panel.add(createOfferPanelContent(), BorderLayout.CENTER);
        return panel;
    }


    private JPanel createCheckBoxPanel(JCheckBox checkBox) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0,10,0,0));
        panel.add(checkBox, BorderLayout.PAGE_START);
        return panel;
    }


    private JCheckBox createCheckBox(String caption, int mnemonic) {
        JCheckBox checkBox = new JCheckBox(caption);
        checkBox.setMnemonic(mnemonic);
        checkBox.addItemListener(this);
        return checkBox;
    }


    private JPanel createAllocatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Allocate"));
        chkAllocate = createCheckBox("Enable System Allocation", KeyEvent.VK_A);
        panel.add(createCheckBoxPanel(chkAllocate), BorderLayout.NORTH);
        panel.add(createAllocatePanelContent(), BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(660, 90));
        return panel;
    }

    private JPanel createStartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Start"));
        chkStart = createCheckBox("Enable System Start", KeyEvent.VK_S);
        panel.add(createCheckBoxPanel(chkStart), BorderLayout.PAGE_START);
        panel.setPreferredSize(new Dimension(660, 50));
        return panel;
    }

    private JPanel createOfferPanelContent() {
        offerPanelContent = new JPanel(new BorderLayout());
        offerPanelContent.add(createOfferResourceTables(), BorderLayout.NORTH);
        offerPanelContent.add(createOfferFiltersTables(), BorderLayout.CENTER);
        return offerPanelContent;
    }


    private JPanel createAllocatePanelContent() {
        allocatePanelContent = new JPanel();
        allocatePanelContent.setBorder(new EmptyBorder(5,10,5,205));
        allocatePanelContent.add(new Label("Allocation Strategy: "));
        cbxAllocations = new JComboBox();
        cbxAllocations.setPreferredSize(new Dimension(300,25));
        allocatePanelContent.add(cbxAllocations);
        return allocatePanelContent;
    }


    private JPanel createOfferResourceTables() {
        JPanel panel = new JPanel();
        participantTablePanel = new ResourceTablePanel(ResourceTableType.Participant, this);
        participantTablePanel.showEditButton(false);
        panel.add(participantTablePanel);

        roleTablePanel = new ResourceTablePanel(ResourceTableType.Role, this);
        roleTablePanel.showEditButton(false);
        panel.add(roleTablePanel);

        netParamTablePanel = new ResourceTablePanel(ResourceTableType.NetParam, this);
        panel.add(netParamTablePanel);

        return panel;
    }

    private JPanel createOfferFiltersTables() {
        JPanel panel = new JPanel();
        filterTablePanel = new ResourceTablePanel(ResourceTableType.Filter, this);
        panel.add(filterTablePanel);
        panel.add(new ConstraintsPanel());
        return panel;
    }


    private JPanel createButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10,0,10,0));
        panel.add(createButton("Cancel"));
        btnApply = createButton("Apply");
        btnApply.setEnabled(false);
        panel.add(btnApply);
        panel.add(createButton("OK"));

        return panel;
    }


    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.setPreferredSize(new Dimension(70,25));
        button.addActionListener(this);
        return button;
    }


    private void enablePanelContent(JPanel panel, boolean enabled) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JPanel) {
                enablePanelContent((JPanel) component, enabled);
            }
            component.setEnabled(enabled);
        }
    }

    private ResourceTable getParticipantTable() { return participantTablePanel.getTable(); }

    private ResourceTable getRoleTable() { return roleTablePanel.getTable(); }

    private ResourceTable getNetParamTable() { return netParamTablePanel.getTable(); }

    private ResourceTable getFilterTable() { return filterTablePanel.getTable(); }



    private void setTableValues() {

        enablePanelContent(offerPanelContent, false);
        enablePanelContent(allocatePanelContent, false);

        ParticipantTableModel participantModel = (ParticipantTableModel)
                participantTablePanel.getTableModel();

        // temp line for demo
        java.util.List<Participant> ps = new ArrayList<Participant>();
        ps.add(new Participant("Jones", "Tom", "jonest"));
        ps.add(new Participant("Smith", "Abel", "smitha8"));
        participantModel.setValues(ps);

      //  participantModel.setValues(new ArrayList<Participant>(resources.getParticipants()));

        RoleTableModel roleModel = (RoleTableModel) roleTablePanel.getTableModel();

        // temp line for demo

        java.util.List<Role> rs = new ArrayList<Role>();
        rs.add(new Role("Finance Officer"));

        roleModel.setValues(rs);

    //    roleModel.setValues(new ArrayList<Role>(resources.getRoles()));

        NetParamTableModel netParamModel =
                 (NetParamTableModel) netParamTablePanel.getTableModel();
        netParamModel.setValues(new ArrayList<DynParam>(resources.getDynParams()));

        FilterTableModel filterModel = (FilterTableModel) filterTablePanel.getTableModel();
        filterModel.setValues(new ArrayList<AbstractFilter>(resources.getFilters()));
    }


    private void updateTaskResources() {
 //       resources.
    }


    private YDecomposition getDecomposition() {
        return task.getDecompositionPrototype();
    }

}


