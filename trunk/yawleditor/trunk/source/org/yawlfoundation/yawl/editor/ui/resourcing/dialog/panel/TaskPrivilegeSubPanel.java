package org.yawlfoundation.yawl.editor.ui.resourcing.dialog.panel;

import org.yawlfoundation.yawl.editor.core.resourcing.TaskPrivilege;
import org.yawlfoundation.yawl.editor.core.resourcing.TaskPrivileges;
import org.yawlfoundation.yawl.editor.core.resourcing.TaskResourceSet;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.ResourceDialog;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.panel.ResourceTablePanel;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.ResourceTableType;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel.ParticipantTableModel;
import org.yawlfoundation.yawl.editor.ui.resourcing.dialog.tablemodel.RoleTableModel;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author Michael Adams
 * @date 26/06/13
 */
public class TaskPrivilegeSubPanel extends JPanel implements ItemListener {

    private JCheckBox _chkPrivilege;
    private JCheckBox _chkRestricted;
    private ParticipantTableModel _participantTableModel;
    private RoleTableModel _roleTableModel;

    private String _privilege;

    public TaskPrivilegeSubPanel(String caption, String privilege, ResourceDialog owner) {
        super();
        _privilege = privilege;
        setLayout(new GridLayout(1,0));
        setBorder(new LineBorder(Color.DARK_GRAY));
        createContent(caption, owner);
        setPreferredSize(new Dimension(660, 90));
    }


    public void load(TaskResourceSet resources) {
        TaskPrivilege privilege = getTaskPrivilege(resources);
        setSelected(privilege.isAllowed());
        setRestricted(privilege.isRestricted());
        if (isRestricted()) {
            _participantTableModel.setValues(
                    new ArrayList<Participant>(privilege.getParticipants().getAll()));
            _roleTableModel.setValues(new ArrayList<Role>(privilege.getRoles().getAll()));
        }
    }


    public void save(TaskResourceSet resources) {
        TaskPrivilege privilege = getTaskPrivilege(resources);
        privilege.setAllowAll(isSelected() && ! isRestricted());
        if (isRestricted()) {
            privilege.getParticipants().set(
                    new HashSet<Participant>(_participantTableModel.getValues()));
            privilege.getRoles().set(new HashSet<Role>(_roleTableModel.getValues()));
        }
    }


    public void setSelected(boolean selected) {
        _chkPrivilege.setSelected(selected);
    }

    public boolean isSelected() { return _chkPrivilege.isSelected(); }

    public void setRestricted(boolean restricted) {
        _chkRestricted.setSelected(restricted);
    }

    public boolean isRestricted() {
        return _chkRestricted.isEnabled() && _chkRestricted.isSelected();
    }

    public java.util.List<Participant> getParticipants() {
        if (isRestricted()) {
            return _participantTableModel.getValues();
        }
        else return Collections.emptyList();
    }

    public void setParticipants(java.util.List<Participant> participants) {
        _participantTableModel.setValues(participants);
    }


    public java.util.List<Role> getRoles() {
        if (isRestricted()) {
            return _roleTableModel.getValues();
        }
        else return Collections.emptyList();
    }

    public void setRoles(java.util.List<Role> roles) {
        _roleTableModel.setValues(roles);
    }


    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        boolean selected = e.getStateChange() == ItemEvent.SELECTED;
        if (source == _chkPrivilege) {
            _chkRestricted.setEnabled(selected);
        }
        enableTablePanels(isRestricted());
    }

    private void createContent(String caption, ResourceDialog owner) {
        add(createCheckBoxPanel(caption, owner));
        add(createParticipantTablePanel(owner));
        add(createRoleTablePanel(owner));
    }


    private JPanel createCheckBoxPanel(String caption, ResourceDialog owner) {
        JPanel panel = new JPanel(new GridLayout(2,1));
        _chkPrivilege = new JCheckBox("<html>" + caption + "</html>");
        _chkPrivilege.setBorder(new EmptyBorder(0,5,0,0));
        _chkPrivilege.setSelected(false);
        _chkPrivilege.addItemListener(this);
        _chkPrivilege.addItemListener(owner);
        panel.add(_chkPrivilege);
        _chkRestricted = new JCheckBox("Restrict Privilege");
        _chkRestricted.setBorder(new EmptyBorder(0,30,0,0));
        _chkRestricted.setSelected(false);
        _chkRestricted.setEnabled(false);
        _chkRestricted.addItemListener(this);
        _chkRestricted.addItemListener(owner);
        panel.add(_chkRestricted);
        panel.setPreferredSize(new Dimension(400,90));
        return panel;
    }


    private ResourceTablePanel createParticipantTablePanel(ResourceDialog owner)  {
        ResourceTablePanel tablePanel = createTablePanel(ResourceTableType.Participant);
        _participantTableModel = (ParticipantTableModel) tablePanel.getTableModel();
        _participantTableModel.setOwner(owner);
        _participantTableModel.addTableModelListener(tablePanel);
        tablePanel.setPreferredSize(new Dimension(120,90));
        return tablePanel;
    }


    private ResourceTablePanel createRoleTablePanel(ResourceDialog owner)  {
        ResourceTablePanel tablePanel = createTablePanel(ResourceTableType.Role);
        _roleTableModel = (RoleTableModel) tablePanel.getTableModel();
        _roleTableModel.setOwner(owner);
        _roleTableModel.addTableModelListener(tablePanel);
        return tablePanel;
    }


    private ResourceTablePanel createTablePanel(ResourceTableType tableType) {
        ResourceTablePanel tablePanel = new ResourceTablePanel(tableType);
        tablePanel.showEditButton(false);
        tablePanel.setEnabled(false);
        tablePanel.setToolbarOrientation(JToolBar.VERTICAL);
        return tablePanel;
    }


    private TaskPrivilege getTaskPrivilege(TaskResourceSet resources) {
        TaskPrivileges privileges = resources.getTaskPrivileges();
        return privileges.getPrivilege(_privilege);
    }


    private void enableTablePanels(boolean enabled) {
        for (Component component : getComponents()) {
            if (component instanceof ResourceTablePanel) {
                component.setEnabled(enabled);
            }
        }
    }

}
