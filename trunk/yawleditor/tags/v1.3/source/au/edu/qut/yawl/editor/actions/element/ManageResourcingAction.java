/*
 * Created on 27/05/2005
 * YAWLEditor v1.3 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package au.edu.qut.yawl.editor.actions.element;

import javax.swing.JComboBox;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;
import au.edu.qut.yawl.editor.swing.element.AbstractTaskDoneDialog;
import au.edu.qut.yawl.editor.actions.net.YAWLSelectedNetAction;
import au.edu.qut.yawl.editor.elements.model.YAWLTask;
import au.edu.qut.yawl.editor.thirdparty.orgdatabase.OrganisationDatabaseProxy;
import au.edu.qut.yawl.editor.resourcing.ResourceMapping;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class ManageResourcingAction extends YAWLSelectedNetAction
                                    implements TooltipTogglingWidget {
  private YAWLTask task;
  private NetGraph graph;
  
  private ManageResourcingDialog dialog = new ManageResourcingDialog();
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Manage Resourcing");
    putValue(Action.LONG_DESCRIPTION, "Manage the resourcing requirements of this task.");
    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_R));
  }
  
  public ManageResourcingAction(YAWLTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }  

  public void actionPerformed(ActionEvent event) {
    dialog.setTask(task, graph);
    dialog.setVisible(true);
  }
 
  public String getEnabledTooltipText() {
    return " Manage the resourcing requirements of this task ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have an atomic task with a worklist decomposition selected" + 
           " to update its resourcing requirements ";
  }
  
  public boolean shouldBeEnabled() {
    if (task.getDecomposition() != null && task.getDecomposition().invokesWorklist()) {
      return true;
    }
    return false;
  }
}

class ManageResourcingDialog extends AbstractTaskDoneDialog {
  
  private AllocationPanel allocationPanel;
  private AuthorisationPanel authorisationPanel;
  
  private HashMap humanResourceNames; 
  private List roleNames; 
  
  public ManageResourcingDialog() {
    super(null, false, true);
    setContentPanel(getPanel());
    getDoneButton().addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            allocationPanel.commitValues();
            authorisationPanel.commitValues();
          }
        }
    );
  }
  
  public void setTask(YAWLTask task, NetGraph graph) {
    super.setTask(task,graph);
    allocationPanel.setTask(task);
    authorisationPanel.setTask(task);
  }
  
  public String getTitlePrefix() {
    return "Manage Resourcing of ";
  }
    
  private JPanel getPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    
    panel.setBorder(new EmptyBorder(12,12,0,11));

    panel.add(getAuthorisationPanel(), BorderLayout.NORTH);
    panel.add(Box.createRigidArea(new Dimension(10,0)), BorderLayout.CENTER);
    panel.add(getAllocationPanel(), BorderLayout.SOUTH);

    return panel;
  }
  
  private JPanel getAllocationPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    panel.setBorder(new TitledBorder("Task Allocation"));
    allocationPanel = new AllocationPanel(this);
    panel.add(allocationPanel, BorderLayout.CENTER);

    return panel;    
  }

  private JPanel getAuthorisationPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    panel.setBorder(new TitledBorder("Task Authorisation"));
    authorisationPanel = new AuthorisationPanel(this);
    panel.add(authorisationPanel, BorderLayout.CENTER);

    return panel;
  }
  
  public void setVisible(boolean visible) {
    if (visible) {
      humanResourceNames = OrganisationDatabaseProxy.getInstance().getAllHumanResourceNames();
      roleNames = OrganisationDatabaseProxy.getInstance().getAllRoles();

      authorisationPanel.setContent();
      allocationPanel.setContent();
      
      pack();
    }
    super.setVisible(visible);
  }
  
  public HashMap getAllHumanResource() {
    return humanResourceNames;
  }
  
  public List getAllRoles() {
    return roleNames;
  }
}

abstract class ResourcingPanel extends JPanel implements ActionListener {
  private YAWLTask task;
  
  abstract void commitValues();
  abstract void setContent();
  
  public void setTask(YAWLTask task) {
    this.task = task;
  }
  
  public YAWLTask getTask() {
    return this.task;
  }
}

class AllocationPanel extends ResourcingPanel {

  private JRadioButton anyoneAllocationRadioButton;
  private JRadioButton directAllocationRadioButton;
  private JRadioButton roleAllocationRadioButton;
  
  private ButtonGroup allocationButtonGroup = new ButtonGroup();
  
  private JComboBox directAllocationBox;
  private JComboBox roleAllocationBox;
  
  private ManageResourcingDialog dialog;
  
  private int mappingType = ResourceMapping.ALLOCATE_TO_ANYONE;
  
  public AllocationPanel(ManageResourcingDialog dialog) {
    super();
    this.dialog = dialog;
    buildContent();
  }
  
  private void buildContent() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,5,5);
    gbc.weightx = 1;
    gbc.anchor = GridBagConstraints.WEST;
    
    add(getAnyoneAllocationRadioButton(),gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 1;

    
    add(getDirectAllocationRadioButton(), gbc);

    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridx++;
    
    add(getDirectAllocationComboBox(),gbc);
    
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.weightx = 1;

    add(getRoleAllocationRadioButton(), gbc);

    gbc.weightx = 0;
    gbc.gridx++;

    add(getRoleAllocationComboBox(),gbc);
    
    allocationButtonGroup.add(anyoneAllocationRadioButton);
    allocationButtonGroup.add(directAllocationRadioButton);
    allocationButtonGroup.add(roleAllocationRadioButton);
    setWidgetStateBasedOnSelectedRadioButton();
  }

  private JRadioButton getAnyoneAllocationRadioButton() {
    anyoneAllocationRadioButton = new JRadioButton("Allocate to anyone.");

    anyoneAllocationRadioButton.setMnemonic('a');
    anyoneAllocationRadioButton.setDisplayedMnemonicIndex(2);
    
    anyoneAllocationRadioButton.setEnabled(false);
    anyoneAllocationRadioButton.addActionListener(this);
    
    anyoneAllocationRadioButton.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          if (!anyoneAllocationRadioButton.isEnabled()) {
            return;
          }
          if (anyoneAllocationRadioButton.isSelected()) {
            mappingType = ResourceMapping.ALLOCATE_TO_ANYONE;
          }
        }
      }
    );
    
    return anyoneAllocationRadioButton;
  }
  
  private JRadioButton getDirectAllocationRadioButton() {
    directAllocationRadioButton = new JRadioButton("Allocate directly to person:");
    directAllocationRadioButton.setMnemonic('d');
    directAllocationRadioButton.setEnabled(false);

    directAllocationRadioButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            if (!directAllocationRadioButton.isEnabled()) {
              return;
            }
            if (directAllocationRadioButton.isSelected()) {
              mappingType = ResourceMapping.ALLOCATE_DIRECTLY;
            }
          }
        }
    );
    
    return directAllocationRadioButton;
  }
  
  private JComboBox getDirectAllocationComboBox() {
    directAllocationBox = new JComboBox();
    
    directAllocationRadioButton.addActionListener(this);
    directAllocationBox.setEnabled(false);

    return directAllocationBox;
  }

  private JRadioButton getRoleAllocationRadioButton() {
    roleAllocationRadioButton = new JRadioButton("Allocate to the role:");
    roleAllocationRadioButton.setMnemonic('e');
    roleAllocationRadioButton.setEnabled(false);

    roleAllocationRadioButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            if (!roleAllocationRadioButton.isEnabled()) {
              return;
            }
            if (roleAllocationRadioButton.isSelected()) {
              mappingType = ResourceMapping.ALLOCATE_TO_ROLE;
            }
          }
        }
    );
    
    return roleAllocationRadioButton;
  }
  
  private JComboBox getRoleAllocationComboBox() {
    roleAllocationBox = new JComboBox();

    roleAllocationRadioButton.addActionListener(this);
    roleAllocationBox.setEnabled(false);

    return roleAllocationBox;
  }
  
  public void actionPerformed(ActionEvent event) {
    setWidgetStateBasedOnSelectedRadioButton();
  }
  
  private void setWidgetStateBasedOnSelectedRadioButton() {
    directAllocationBox.setEnabled(false);
    roleAllocationBox.setEnabled(false);
    
    if (directAllocationRadioButton.isSelected()) {
      directAllocationBox.setEnabled(true);
    } 
    if (roleAllocationRadioButton.isSelected()) {
      roleAllocationBox.setEnabled(true);
    }
  }
  
  public void setContent() {
    disableRadioButtons();
    
    setDirectAllocationBoxContent();
    setRoleAllocationBoxContent();
    selectCorrectWidgetOptions();
    
    enableRadioButtonsWhereAppropriate();
    setWidgetStateBasedOnSelectedRadioButton();
  }
  
  private void disableRadioButtons() {
    anyoneAllocationRadioButton.setEnabled(false);
    directAllocationRadioButton.setEnabled(false);
    roleAllocationRadioButton.setEnabled(false);
  }

  private void enableRadioButtonsWhereAppropriate() {
    if (roleAllocationBox.getItemCount() > 0) {
      roleAllocationRadioButton.setEnabled(true);
      roleAllocationRadioButton.requestFocus();
    }
    if (directAllocationBox.getItemCount() > 0) {
      directAllocationRadioButton.setEnabled(true);
      directAllocationRadioButton.requestFocus();
    }
    anyoneAllocationRadioButton.setEnabled(true);
    anyoneAllocationRadioButton.requestFocus();
  }
  
  private void selectCorrectWidgetOptions() {
    if (getTask().getAllocationResourceMapping() == null) {
      anyoneAllocationRadioButton.setSelected(true);
      anyoneAllocationRadioButton.requestFocus();
      return;
    }
    
    mappingType = getTask().getAllocationResourceMapping().getMappingType();

    switch(mappingType) {
      case ResourceMapping.ALLOCATE_TO_ANYONE: {
        anyoneAllocationRadioButton.setSelected(true);
        anyoneAllocationRadioButton.requestFocus();
        break;
      }
      case ResourceMapping.ALLOCATE_DIRECTLY: {
        directAllocationRadioButton.setSelected(true);
        directAllocationRadioButton.requestFocus();
        directAllocationBox.setSelectedItem(
            getTask().getAllocationResourceMapping().getLabel()    
        );
        break;
      }
      case ResourceMapping.ALLOCATE_TO_ROLE: {
        roleAllocationRadioButton.setSelected(true);
        roleAllocationRadioButton.requestFocus();
        roleAllocationBox.setSelectedItem(
            getTask().getAllocationResourceMapping().getIdentifier()    
        );
        break;
      }
      default: {
        anyoneAllocationRadioButton.setSelected(true);
        anyoneAllocationRadioButton.requestFocus();
        break;
      }
    }
    setWidgetStateBasedOnSelectedRadioButton();
  }
  
  private void setDirectAllocationBoxContent() {
    directAllocationBox.removeAllItems();
    
    if (dialog.getAllHumanResource().size() == 0) {
      // recreating selectable data from what's been stored locally.
      if (getTask().getAllocationResourceMapping().getMappingType() != 
          ResourceMapping.ALLOCATE_DIRECTLY) {
        return;
      }
      
      dialog.getAllHumanResource().put(
          getTask().getAllocationResourceMapping().getLabel(),
          getTask().getAllocationResourceMapping().getIdentifier()
      );
      directAllocationBox.addItem(getTask().getAllocationResourceMapping().getLabel());
    }
    
    Iterator peopleIterator = dialog.getAllHumanResource().keySet().iterator();
    while(peopleIterator.hasNext()) {
      directAllocationBox.addItem(peopleIterator.next());
    }
  }
  
  private void setRoleAllocationBoxContent() {
    roleAllocationBox.removeAllItems();
    
    if (dialog.getAllRoles().size() == 0) {
      // recreating selectable data from what's been stored locally.

      if (getTask().getAllocationResourceMapping().getMappingType() != 
        ResourceMapping.ALLOCATE_TO_ROLE) {
      return;
    }

      dialog.getAllRoles().add(
          getTask().getAllocationResourceMapping().getIdentifier()
      );
    }
    
    Iterator roleIterator = dialog.getAllRoles().iterator();
    while(roleIterator.hasNext()) {
      roleAllocationBox.addItem(roleIterator.next());
    }
  }
  
  public void commitValues() {
    ResourceMapping mapping = getTask().getAllocationResourceMapping();

    if (mapping == null) {
      mapping = new ResourceMapping();
      getTask().setAllocationResourceMapping(mapping);
    }

    mapping.setMappingType(mappingType);

    switch(mappingType) {
      case ResourceMapping.ALLOCATE_TO_ANYONE: {
        mapping.setIdentifier(null);
        mapping.setLabel(null);
        break;
      }
      case ResourceMapping.ALLOCATE_DIRECTLY: {
        mapping.setIdentifier((String)
            dialog.getAllHumanResource().get(
                directAllocationBox.getSelectedItem()
            )
        );
        mapping.setLabel(
            (String) directAllocationBox.getSelectedItem()
        );
        break;
      }
      case ResourceMapping.ALLOCATE_TO_ROLE: {
        mapping.setIdentifier(
          (String) roleAllocationBox.getSelectedItem()    
        );
        mapping.setLabel(null);
        break;
      }
    }
  }
}

class AuthorisationPanel extends ResourcingPanel {

  private JRadioButton noAuthorisationRadioButton;
  private JRadioButton directAuthorisationRadioButton;
  private JRadioButton roleAuthorisationRadioButton;
  
  private ButtonGroup authorisationButtonGroup = new ButtonGroup();

  private JComboBox directAuthorisationBox;
  private JComboBox roleAuthorisationBox;
  
  private ManageResourcingDialog dialog;
  
  private int mappingType = ResourceMapping.AUTHORISATION_UNNECESSARY;
  
  public AuthorisationPanel(ManageResourcingDialog dialog) {
    super();
    this.dialog = dialog;
    buildContent();
  }
  
  private void buildContent() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    setLayout(gbl);

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,5,5);
    gbc.weightx = 1;
    gbc.anchor = GridBagConstraints.WEST;

    add(getNoAuthorisationRadioButton(), gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    
    add(getDirectAuthorisationRadioButton(), gbc);

    gbc.gridx++;

    add(getDirectAuthorisationComboBox(),gbc);

    gbc.gridx = 0;
    gbc.gridy++;

    add(getRoleAuthorisationRadioButton(), gbc);

    gbc.gridx++;

    add(getRoleAuthorisationComboBox(),gbc);
    
    authorisationButtonGroup.add(noAuthorisationRadioButton);
    authorisationButtonGroup.add(directAuthorisationRadioButton);
    authorisationButtonGroup.add(roleAuthorisationRadioButton);
    setWidgetStateBasedOnSelectedRadioButton();
  }
  
  private JRadioButton getNoAuthorisationRadioButton() {
    noAuthorisationRadioButton = new JRadioButton("All are authorised.");

    noAuthorisationRadioButton.setMnemonic('A');
    
    noAuthorisationRadioButton.setSelected(true);
    noAuthorisationRadioButton.addActionListener(this);
    
    noAuthorisationRadioButton.setEnabled(false);
    noAuthorisationRadioButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            if (!noAuthorisationRadioButton.isEnabled()) {
              return;
            }
            if (noAuthorisationRadioButton.isSelected()) {
              mappingType = ResourceMapping.AUTHORISATION_UNNECESSARY;
            }
          }
        }
    );
    
    return noAuthorisationRadioButton;
  }

  
  private JRadioButton getDirectAuthorisationRadioButton() {
    directAuthorisationRadioButton = new JRadioButton("Only this person is authorised:");
    directAuthorisationRadioButton.setMnemonic('p');
    directAuthorisationRadioButton.setEnabled(false);

    directAuthorisationRadioButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            if (!directAuthorisationRadioButton.isEnabled()) {
              return;
            }
            if (directAuthorisationRadioButton.isSelected()) {
              mappingType = ResourceMapping.AUTHORISED_DIRECTLY;
            }
          }
        }
    );

    return directAuthorisationRadioButton;
  }
  
  private JComboBox getDirectAuthorisationComboBox() {
    directAuthorisationBox = new JComboBox();
    
    directAuthorisationRadioButton.addActionListener(this);
    
    return directAuthorisationBox;
  }

  private JRadioButton getRoleAuthorisationRadioButton() {
    roleAuthorisationRadioButton = new JRadioButton("Only this role is authorised:");
    roleAuthorisationRadioButton.setMnemonic('r');
    roleAuthorisationRadioButton.setEnabled(false);

    roleAuthorisationRadioButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent event) {
            if (!roleAuthorisationRadioButton.isEnabled()) {
              return;
            }
            if (roleAuthorisationRadioButton.isSelected()) {
              mappingType = ResourceMapping.AUTHORISATION_VIA_ROLE;
            }
          }
        }
    );

    return roleAuthorisationRadioButton;
  }
  
  private JComboBox getRoleAuthorisationComboBox() {
    roleAuthorisationBox = new JComboBox();
    
    roleAuthorisationRadioButton.addActionListener(this);
    roleAuthorisationBox.setEnabled(false);
    
    return roleAuthorisationBox;
  }
  
  public void actionPerformed(ActionEvent event) {
    setWidgetStateBasedOnSelectedRadioButton();
  }

  private void setWidgetStateBasedOnSelectedRadioButton() {
    directAuthorisationBox.setEnabled(false);
    roleAuthorisationBox.setEnabled(false);
    
    if (directAuthorisationRadioButton.isSelected()) {
      directAuthorisationBox.setEnabled(true);
    } 
    if (roleAuthorisationRadioButton.isSelected()) {
      roleAuthorisationBox.setEnabled(true);
    }
  }
  
  public void setContent() {
    disableRadioButtons();
    
    setDirectAuthorisationBoxContent();
    setRoleAuthorisationBoxContent();
    selectCorrectWidgetOptions();
    
    enableRadioButtonsWhereAppropriate();
  }
  
  private void disableRadioButtons() {
    noAuthorisationRadioButton.setEnabled(false);
    directAuthorisationRadioButton.setEnabled(false);
    roleAuthorisationRadioButton.setEnabled(false);
  }
  
  private void enableRadioButtonsWhereAppropriate() {
    if (roleAuthorisationBox.getItemCount() > 0) {
      roleAuthorisationRadioButton.setEnabled(true);
      roleAuthorisationRadioButton.requestFocus();
    }
    if (directAuthorisationBox.getItemCount() > 0) {
      directAuthorisationRadioButton.setEnabled(true);
      directAuthorisationRadioButton.requestFocus();
    }
    noAuthorisationRadioButton.setEnabled(true);
    noAuthorisationRadioButton.requestFocus();
  }
  
  private void selectCorrectWidgetOptions() {
    if (getTask().getAllocationResourceMapping() == null) {
      noAuthorisationRadioButton.setSelected(true);
      noAuthorisationRadioButton.requestFocus();
      return;
    }
    
    mappingType = getTask().getAuthorisationResourceMapping().getMappingType();

    switch(mappingType) {
      case ResourceMapping.AUTHORISATION_UNNECESSARY: {
        noAuthorisationRadioButton.setSelected(true);
        noAuthorisationRadioButton.requestFocus();
        break;
      }
      case ResourceMapping.AUTHORISED_DIRECTLY: {
        directAuthorisationRadioButton.setSelected(true);
        directAuthorisationRadioButton.requestFocus();
        directAuthorisationBox.setSelectedItem(
            getTask().getAuthorisationResourceMapping().getLabel()    
        );
        break;
      }
      case ResourceMapping.AUTHORISATION_VIA_ROLE: {
        roleAuthorisationRadioButton.setSelected(true);
        roleAuthorisationRadioButton.requestFocus();
        roleAuthorisationBox.setSelectedItem(
            getTask().getAuthorisationResourceMapping().getIdentifier()    
        );
        break;
      }
      default: {
        noAuthorisationRadioButton.setSelected(true);
        noAuthorisationRadioButton.requestFocus();
        break;
      }
    }
    setWidgetStateBasedOnSelectedRadioButton();
  }
  
  private void setDirectAuthorisationBoxContent() {
    directAuthorisationBox.removeAllItems();
    
    if (dialog.getAllHumanResource().size() == 0) {
      // recreating selectable data from what's been stored locally.
      if (getTask().getAuthorisationResourceMapping().getMappingType() != 
          ResourceMapping.AUTHORISED_DIRECTLY) {
        return;
      }
      
      dialog.getAllHumanResource().put(
          getTask().getAuthorisationResourceMapping().getLabel(),
          getTask().getAuthorisationResourceMapping().getIdentifier()
      );
      directAuthorisationBox.addItem(getTask().getAuthorisationResourceMapping().getLabel());
    }
    
    Iterator peopleIterator = dialog.getAllHumanResource().keySet().iterator();
    while(peopleIterator.hasNext()) {
      directAuthorisationBox.addItem(peopleIterator.next());
    }
  }
  
  private void setRoleAuthorisationBoxContent() {
    roleAuthorisationBox.removeAllItems();
    
    if (dialog.getAllRoles().size() == 0) {
      // recreating selectable data from what's been stored locally.

      if (getTask().getAuthorisationResourceMapping().getMappingType() != 
        ResourceMapping.AUTHORISATION_VIA_ROLE) {
      return;
    }

      dialog.getAllRoles().add(
          getTask().getAuthorisationResourceMapping().getIdentifier()
      );  
    }
    
    Iterator roleIterator = dialog.getAllRoles().iterator();
    while(roleIterator.hasNext()) {
      roleAuthorisationBox.addItem(roleIterator.next());
    }
  }
  
  
  public void commitValues() {
    ResourceMapping mapping = getTask().getAuthorisationResourceMapping();
    mapping.setMappingType(mappingType);

    switch(mappingType) {
      case ResourceMapping.AUTHORISATION_UNNECESSARY: {
        mapping.setIdentifier(null);
        mapping.setLabel(null);
        break;
      }
      case ResourceMapping.AUTHORISED_DIRECTLY: {
        mapping.setIdentifier((String)
            dialog.getAllHumanResource().get(
                directAuthorisationBox.getSelectedItem()
            )
        );
        mapping.setLabel(
            (String) directAuthorisationBox.getSelectedItem()
        );
        break;
      }
      case ResourceMapping.AUTHORISATION_VIA_ROLE: {
        mapping.setIdentifier(
          (String) roleAuthorisationBox.getSelectedItem()    
        );
        mapping.setLabel(null);
        break;
      }
    }
  }
}
