/*
 * Created on 6/08/2004
 * YAWLEditor v1.01 
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
 */

package au.edu.qut.yawl.editor.swing.data;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;
import au.edu.qut.yawl.editor.swing.data.DataVariableField;
import au.edu.qut.yawl.editor.specification.SpecificationModel;

public class TaskDataVariableUpdateDialog extends AbstractDoneDialog {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public static final int SCOPE_NET = 0;
  public static final int SCOPE_TASK = 1;
  
  protected AbstractDoneDialog parent;
  
  protected DataVariableField nameField;

  protected JXMLSchemaInstanceEditor initialValueEditor;

  protected DataTypeComboBox   typeComboBox;
  protected VariableUsageComboBox   usageComboBox;
  
  private boolean firstAppearance = true;
  
  private JLabel usageLabel;
  
  private DataVariable variable;
  
  protected JButton attributesToggle; //MLF
  protected JPanel attributesPanel; //MLF
  protected ExtendedAttributesTableModel model; //MLF
  protected JTabbedPane pane; //MLF
  
  public TaskDataVariableUpdateDialog(AbstractDoneDialog parent) {
    super("Update Variable", true);
    this.parent = parent;
    
    setContentPanel(getVariablePanel());
    getDoneButton().addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          SpecificationModel.getInstance().changeVariableNameInQueries(
            variable,
            variable.getName(),
            nameField.getText()
          );
          variable.setName(nameField.getText());
          variable.setDataType((String) typeComboBox.getSelectedItem());
          variable.setUsage(usageComboBox.getSelectedIndex());
        }
      }
    );
    makeLastAdjustments();
  }

  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }
  
  public void setVariable(DataVariable variable) {
    this.variable = variable;
    setContent();
    setTitle(DataVariable.SCOPE_TASK);
  }
  
  protected void setTitle(int scope) {
    setTitle("Update " + 
              DataVariable.scopeToString(scope) + 
              " Variable" + 
              (getVariable() == null || getVariable().getName().equals("") ? 
                  "" : " \"" + getVariable().getName() +
                   "\"")
    );
  }
  
  public DataVariable getVariable() {
    return this.variable;
  }
  
  protected JPanel getVariablePanel() {
    JPanel panel = new JPanel(new GridLayout(1,1));

    panel.add(buildBaseVariablePanel());

    return panel;
  }

  protected JTabbedPane buildBaseVariablePanel() {
    pane = new JTabbedPane();

    pane.setBorder(
        new EmptyBorder(5,5,5,5)
    );
    
    pane.addTab(
        "Standard", 
        buildStandardPanel()
    );
    
    pane.addTab(
        "Extended Attributes", 
        createExtendedAttributePanel()
    );

    return pane;
  }
  
  protected JPanel buildStandardPanel() {

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.insets = new Insets(0,0,5,5);
    gbc.anchor = GridBagConstraints.EAST;
    
    JLabel nameLabel = new JLabel("Name:");
    nameLabel.setDisplayedMnemonic('N');
    
    panel.add(nameLabel, gbc);

    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = .5;
    gbc.insets = new Insets(0,2,5,0);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    panel.add(buildNameField(),gbc);
    nameLabel.setLabelFor(nameField);

    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.weightx = 0.25;
    gbc.weighty = 0;
    gbc.fill = GridBagConstraints.BOTH;
    panel.add(new JLabel(),gbc);

    gbc.gridx++;
    gbc.weightx = 0;
    gbc.weighty = 0;
    gbc.insets = new Insets(0,5,5,2);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel typeLabel = new JLabel("Type:");
    typeLabel.setDisplayedMnemonic('T');
    
    panel.add(typeLabel, gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;
    panel.add(buildTypeComboBox(),gbc);

    typeLabel.setLabelFor(typeComboBox);

    gbc.gridx++;
    gbc.weightx = 0.25;
    gbc.fill = GridBagConstraints.BOTH;
    panel.add(new JLabel(),gbc);
    
    gbc.gridy++;
    gbc.gridx = 0;
    gbc.insets = new Insets(0,5,5,2);
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;

    usageLabel = new JLabel("Usage:");
    usageLabel.setDisplayedMnemonic('U');
    
    panel.add(usageLabel,gbc);

    gbc.gridx++;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getVariableUsageComboBox(),gbc);
    usageLabel.setLabelFor(usageComboBox);

    return panel;
  }
  
  public void setVisible(boolean isVisible) {
    if (isVisible) {
      if (firstAppearance) {
        this.setLocationRelativeTo(parent);
        firstAppearance = false;
      }
    }
    super.setVisible(isVisible);
  }
  
  private DataVariableField buildNameField() {
    nameField = new DataVariableField(10);

    nameField.addKeyListener(
        new NameFieldDocumentListener()
    );

    return nameField;
  }
  
  protected DataVariableField getNameField() {
    return nameField;
  }

  private DataTypeComboBox buildTypeComboBox() {
    typeComboBox = new DataTypeComboBox();
    return typeComboBox;
  }
  
  protected DataTypeComboBox getTypeComboBox() {
    return typeComboBox;
  }

  private VariableUsageComboBox getVariableUsageComboBox() {
    usageComboBox = new VariableUsageComboBox();
    usageComboBox.setScope(DataVariable.SCOPE_TASK);

    return usageComboBox;
  }
  
  protected VariableUsageComboBox getUsageComboBox() {
    return usageComboBox;
  }
  
  protected JComponent createExtendedAttributePanel()
  {
    try {
      model = new ExtendedAttributesTableModel(getVariable());

      return getAttributesPanel();
    } catch (IOException e) {     //if the model can't be created we do nothing more.
      //todo do we report this at all?
    }
      return new JPanel();
  }

  //assumes model has been initialised
  protected JPanel getAttributesPanel() {
    attributesPanel = new JPanel(new GridLayout(1,1));
    attributesPanel.setBorder(new EmptyBorder(10,11,10,12));

    JTable table = new JTable() {
      public Dimension getPreferredScrollableViewportSize() {
        Dimension defaultPreferredSize = super.getPreferredSize();
        
        Dimension preferredSize = new Dimension(
            (int) defaultPreferredSize.getWidth(),
            (int) Math.min(
                defaultPreferredSize.getHeight(),
                getFontMetrics(getFont()).getHeight() * 10           
            )
        );
        
        return preferredSize;
      }
    };

    table.setModel(model);
    ExtendedAttributeEditor editor = new ExtendedAttributeEditor(this, DialogMode.NET);
    getDoneButton().addActionListener(editor);
    table.setDefaultEditor(ExtendedAttribute.class, editor);
    table.setDefaultRenderer(ExtendedAttribute.class, new ExtendedAttributeRenderer());

    attributesPanel.add(new JScrollPane(table));
    attributesPanel.setVisible(false);

    return attributesPanel;
  }
  //END: MLF

  protected void setContent() {
    nameField.setVariable(variable);
    
    if (nameField.getText().equals("")) {
      getDoneButton().setEnabled(false);
    }

    typeComboBox.setEnabled(false);
    typeComboBox.refresh();
    typeComboBox.setSelectedItem(variable.getDataType());
    typeComboBox.setEnabled(true);

    usageComboBox.setEnabled(false);
    usageComboBox.setSelectedIndex(variable.getUsage());
    usageComboBox.setEnabled(true);
    
    if(variable != null && model != null) {
        model.setVariable(variable);
    }
  }
  
  class NameFieldDocumentListener implements KeyListener {
    
    public void keyPressed(KeyEvent e) {
      // deliberately does nothing
    }
    
    public void keyTyped(KeyEvent e) {
      // deliberately does nothing
    }

    public void keyReleased(KeyEvent e) {
      getDoneButton().setEnabled(nameFieldValid());
    }
    
    private boolean nameFieldValid() {
      return nameField.getInputVerifier().verify(nameField);
    }
  }
}
