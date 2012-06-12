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

package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.data.DataVariable;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.swing.ActionAndFocusListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

abstract public class DataVariableUpdateDialog extends AbstractDoneDialog {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public static final int SCOPE_NET = 0;
  public static final int SCOPE_TASK = 1;
  
  protected AbstractDoneDialog parent;
  
  protected DataVariableField nameField;

  protected JXMLSchemaInstanceEditorPane variableValueEditor;

  protected DataTypeComboBox   typeComboBox;
  protected VariableUsageComboBox   usageComboBox;
  
  private boolean firstAppearance = true;
  
  private JLabel usageLabel;
  
  private DataVariable variable;
  
  private JLabel variableValueEditorLabel;
  
  protected JPanel attributesPanel; //MLF
  protected ExtendedAttributesTableModel model; //MLF
  protected JTabbedPane pane; //MLF

  protected LogPredicatesPanel logPredicatesPanel;  

  public DataVariableUpdateDialog(AbstractDoneDialog parent) {
    super("Update Variable", true);
    this.parent = parent;
    
    setContentPanel(getVariablePanel());
    getDoneButton().addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          SpecificationModel.getInstance().changeVariableNameInQueries(
            getVariable(),
            getVariable().getName(),
            nameField.getText()
          );
          getVariable().setName(
              nameField.getText()
          );
          getVariable().setDataType(
              (String) typeComboBox.getSelectedItem()
          );
          getVariable().setUsage(
              usageComboBox.getSelectedIndex()
          );

          String startPredicate = logPredicatesPanel.getStartedPredicate();
          getVariable().setLogPredicateStarted(startPredicate);

          String completionPredicate = logPredicatesPanel.getCompletionPredicate();
          getVariable().setLogPredicateCompletion(completionPredicate);

          setVariableValueFromEditorContent();
        }
      }
    );
    addExtraEventListeners();
    makeLastAdjustments();
  }
  

  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }
  
  protected void addExtraEventListeners() {
    new ActionAndFocusListener(getTypeComboBox()) {
      protected void process(Object eventSource) {
        DataTypeComboBox thisBox = (DataTypeComboBox) eventSource;
        if (thisBox.isEnabled()) {
          variableValueEditor.setVariableType((String) thisBox.getSelectedItem());   
        }
      }
    };
    new ActionAndFocusListener(getNameField()) {
      protected void process(Object eventSource) {
        JTextField thisField = (JTextField) eventSource;
        variableValueEditor.setVariableName(thisField.getText()); 
      }
    };
    new ActionAndFocusListener(getUsageComboBox()) {
      protected void process(Object eventSource) {
        enableVariableValueEditorIfAppropriate();
        setLogPredicateEnablings();
      }
    };
  }
  
  public void setVariable(DataVariable variable) {
    this.variable = variable;
    setContent();
    setTitle(
        getVariableScope()
    );
    setLogPredicates();  
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

  private void setLogPredicates() {
    logPredicatesPanel.setStartedPredicate(variable.getLogPredicateStarted());
    logPredicatesPanel.setCompletionPredicate(variable.getLogPredicateCompletion());
    setLogPredicateEnablings(variable.getUsage());
  }

  protected void setLogPredicateEnablings(int usage) {
      logPredicatesPanel.setStartedPredicateEnabled(DataVariable.isInputUsage(usage));
      logPredicatesPanel.setCompletionPredicateEnabled(DataVariable.isOutputUsage(usage));
  }

  protected void setLogPredicateEnablings() {
      if (getUsageComboBox().isEnabled() && getUsageComboBox().getSelectedItem() != null) {
          setLogPredicateEnablings(getUsageComboBox().getSelectedIndex());
      }
  }

  
  protected JPanel getVariablePanel() {
    JPanel panel = new JPanel(new BorderLayout());

    panel.add(buildBaseVariablePanel(), BorderLayout.CENTER);

    return panel;
  }

  protected JTabbedPane buildBaseVariablePanel() {
    pane = new JTabbedPane();
    pane.setFocusable(false);
    pane.setBorder(new EmptyBorder(5,5,5,5));

    pane.addTab("Standard", buildStandardPanel());

    if (getVariableScope() == DataVariable.SCOPE_TASK) {
        pane.addTab("Extended Attributes", createExtendedAttributePanel());
    }    

    logPredicatesPanel = new LogPredicatesPanel(40, 4, LogPredicatesPanel.Parent.DataVariable);
    pane.addTab("Log Predicates", logPredicatesPanel);

    return pane;
  }
  
  protected JPanel buildStandardPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    panel.add(buildCoreStandardPanel(), BorderLayout.NORTH);

    return panel;
  }
  
  
  protected JPanel buildCoreStandardPanel() {

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
    
    gbc.gridwidth = 1;
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.insets = new Insets(0,0,5,5);
    gbc.anchor = GridBagConstraints.NORTHEAST;
    
    variableValueEditorLabel = new JLabel("Value:");
    variableValueEditorLabel.setDisplayedMnemonic('V');
    
    panel.add(variableValueEditorLabel, gbc);

    gbc.gridwidth = 4;
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.gridx++;
    gbc.fill = GridBagConstraints.BOTH;
    
    panel.add(buildVariableValueEditor(),gbc);
    variableValueEditorLabel.setLabelFor(variableValueEditor);

    return panel;
  }
  
  protected JLabel getVariableValueEditorLabel() {
    return variableValueEditorLabel;
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
    nameField = new DataVariableField(25);

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
  
  private JXMLSchemaInstanceEditorPane buildVariableValueEditor() {
    variableValueEditor = new JXMLSchemaInstanceEditorPane();
    variableValueEditor.setPreferredSize(new Dimension(300,150));
    return variableValueEditor;
  }

  private VariableUsageComboBox getVariableUsageComboBox() {
    usageComboBox = new VariableUsageComboBox();
    usageComboBox.setScope(
      getVariableScope()    
    );

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

      table.setRowHeight(getFontMetrics(table.getFont()).getHeight() +
                         (int) (table.getFont().getSize() *0.75));

    table.setShowGrid(true);
    ExtendedAttributeEditor editor = new ExtendedAttributeEditor(this, DialogMode.TASK);
    getDoneButton().addActionListener(editor);
    table.setDefaultEditor(ExtendedAttribute.class, editor);
    ExtendedAttributeRenderer renderer = new ExtendedAttributeRenderer();
    table.setDefaultRenderer(ExtendedAttribute.class, renderer);
    table.setDefaultRenderer(String.class, renderer);
    attributesPanel.add(new JScrollPane(table));
    attributesPanel.setVisible(true);

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
    
    variableValueEditorLabel.setVisible(true);
    variableValueEditor.setVisible(true);
    variableValueEditor.setVariableName(variable.getName());

    // if variable has an invalid datatype
    if (! variable.getDataType().equals(typeComboBox.getSelectedItem())) {
        JOptionPane.showMessageDialog(
             YAWLEditor.getInstance(),
             "The datatype '" + variable.getDataType() + "' for variable '" +
             variable.getName() + "' is missing or invalid.\nBy default, the variable " +
             "has been set to 'boolean' type in the update dialog, but\nno changes are " +
             "saved until the 'Done' button has been clicked.",
             "Invalid Data Type",
             JOptionPane.INFORMATION_MESSAGE
        );
        if (variable.isLocalVariable()) {
            variable.setInitialValue("");
        }
        else if (variable.isOutputVariable()) {
            variable.setDefaultValue("");
        }
        variableValueEditor.setVariableType((String) typeComboBox.getSelectedItem());
    }
    else {
        variableValueEditor.setVariableType(variable.getDataType());
    }

    setEditorValueFromVariable();
    enableVariableValueEditorIfAppropriate();
  }
  
  protected JXMLSchemaInstanceEditorPane getVariableValueEditor() {
    return this.variableValueEditor;
  }

  
  abstract protected void enableVariableValueEditorIfAppropriate();
  abstract protected int getVariableScope();
  abstract protected void setVariableValueFromEditorContent();
  abstract protected void setEditorValueFromVariable();
  
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
