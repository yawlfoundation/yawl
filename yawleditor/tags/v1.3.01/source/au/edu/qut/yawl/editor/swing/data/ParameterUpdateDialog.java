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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import au.edu.qut.yawl.editor.data.Parameter;
import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.DataVariableSet;
import au.edu.qut.yawl.editor.data.Decomposition;

import au.edu.qut.yawl.editor.foundations.XMLUtilities;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;
import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.JXQueryEditorPane;
import au.edu.qut.yawl.editor.swing.data.DataVariableComboBox;

public class ParameterUpdateDialog extends AbstractDoneDialog {
  protected AbstractDoneDialog parent;
  
  public static final int NET_TO_TASK = 0;
  public static final int TASK_TO_NET = 1;

  private int transitionType = NET_TO_TASK;
  
  protected JXQueryEditorPane xQueryEditor;

  protected DataVariableComboBox sourceVariableComboBox;
  protected VariableParameterComboBox sinkVariableComboBox;
  
  private boolean firstAppearance = true;
  
  private int  inputType = DataVariable.SCOPE_NET;
  private int  outputType = DataVariable.SCOPE_TASK;
  
  private Parameter parameter;
  private Decomposition inputVariableScope;
  private Decomposition outputVariableScope;

  private JButton inputVariableQueryContentButton;
  private JButton inputVariableQueryElementButton;
  
  private JButton newVariableButton;
  private XQueryEditorPanel xQueryEditorPanel;
  
  public ParameterUpdateDialog(AbstractDoneDialog parent, int transitionType) {
    super("", true);
    this.parent = parent;

    this.setAttributesForTransitionType(transitionType);
    this.setTitle("Update " + outputType + " Parameter");
    
    setContentPanel(getVariablePanel());
    
    getDoneButton().addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          parameter.setVariable(
              getVariableWithName(
                  (String) sinkVariableComboBox.getSelectedItem()
              )
          );
          parameter.setQuery(xQueryEditor.getText());
        }
      }
    );
  }
  
  private void setAttributesForTransitionType(int transitionType) {
    this.transitionType = transitionType;
    switch(transitionType) {
      case TASK_TO_NET: 
        inputType = DataVariable.SCOPE_TASK;
        outputType = DataVariable.SCOPE_NET;
        break;
      case NET_TO_TASK: 
        inputType = DataVariable.SCOPE_NET;
        outputType = DataVariable.SCOPE_TASK;
        break;
    };
  }

  protected void makeLastAdjustments() {
    setSize(500,400);
    JUtilities.setMinSizeToCurrent(this);
  }
  
  public void setParameter(Parameter parameter) {
    assert parameter != null : "null parameter passed to setParameter()";
    this.parameter = parameter;
    setContent();
    setTitle();
  }

  public void setInputVariableScope(Decomposition variableScope) {
    this.inputVariableScope = variableScope;
  }
  
  public void setOutputVariableScope(Decomposition variableScope) {
    this.outputVariableScope = variableScope;
  }
  
  private void setTitle() {
    if (parameter.getVariable() == null ||
        parameter.getVariable().equals("")) {
      super.setTitle("Update " + DataVariable.scopeToString(outputType) + " Parameter");
    } else {
      super.setTitle("Update " + DataVariable.scopeToString(outputType) + " Parameter" + " \"" +
          parameter.getVariable().getName() + "\"");
    }
  }
  
  public Parameter getParameter() {
    return this.parameter;
  }
  
  private JPanel getVariablePanel() {

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,0,11));
    
    gbc.gridy = 0;
    gbc.gridx = 0;
    gbc.gridheight = 2;
    gbc.weighty = 0;
    gbc.weightx = 0.333;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;

    gbc.insets = new Insets(0,0,5,5);
    JLabel sourceVariableLabel = 
      new JLabel(
          DataVariable.scopeToString(inputType).toLowerCase() + 
          " variable:"
      );
    sourceVariableLabel.setHorizontalAlignment(JLabel.RIGHT);
    sourceVariableLabel.setDisplayedMnemonic('v');

    panel.add(sourceVariableLabel, gbc);

    gbc.gridx++;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(0,5,5,5);
    panel.add(getSourceVariableComboBox(),gbc);
    sourceVariableLabel.setLabelFor(sourceVariableComboBox);
    
    gbc.gridx++;
    gbc.gridheight = 1;
    gbc.anchor = GridBagConstraints.CENTER;
    panel.add(getNewInputVariableQueryElementButton(),gbc);
    
    gbc.gridy++;
    panel.add(getNewInputVariableQueryContentButton(),gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 3;
    gbc.weighty = 1;
    gbc.weightx = 1;
    gbc.insets = new Insets(0,0,5,5);
    gbc.fill = GridBagConstraints.BOTH;

    panel.add(getXQueryEditorPanel(),gbc);

    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.weighty = 0;
    gbc.weightx = 0.333;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;

    gbc.insets = new Insets(0,0,5,5);
    JLabel sinkVariableLabel = 
      new JLabel("populates " + 
          DataVariable.scopeToString(outputType).toLowerCase() + 
          " variable:"
      );
    sinkVariableLabel.setHorizontalAlignment(JLabel.RIGHT);
    sinkVariableLabel.setDisplayedMnemonic('v');

    panel.add(sinkVariableLabel, gbc);

    gbc.gridx++;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(0,5,5,5);
    panel.add(getSinkVariableComboBox(),gbc);
    sinkVariableLabel.setLabelFor(sinkVariableComboBox);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.CENTER;
    panel.add(getNewOutputVariableButton(),gbc);

    return panel;
  }
  
  private JButton getNewInputVariableQueryElementButton() {
    inputVariableQueryElementButton = new JButton("XQuery entire element");
    inputVariableQueryElementButton.setToolTipText("Generates an XQuery returning the entire variable XML element");
    inputVariableQueryElementButton.setMnemonic(KeyEvent.VK_E);
    inputVariableQueryElementButton.setDisplayedMnemonicIndex(7);
    
    inputVariableQueryElementButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        try {
          xQueryEditor.getDocument().insertString(
              xQueryEditor.getCaretPosition(),
              XMLUtilities.getTagEnclosedEntireVariableXQuery(sourceVariableComboBox.getSelectedVariable()),
              null
          );        
        } catch (Exception e) {
          xQueryEditor.setText(
              xQueryEditor.getText() + 
              XMLUtilities.getTagEnclosedEntireVariableXQuery(
                  sourceVariableComboBox.getSelectedVariable()
              )
          );
        }
        enableDoneButtonIfAppropriate();  
      }
    });

    return inputVariableQueryElementButton; 
  }

  private JButton getNewInputVariableQueryContentButton() {
    inputVariableQueryContentButton = new JButton("XQuery content only");
    inputVariableQueryContentButton.setToolTipText("Generates an XQuery returning the content between this variable's element tags");
    inputVariableQueryContentButton.setMnemonic(KeyEvent.VK_O);
    inputVariableQueryContentButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        try {
          xQueryEditor.getDocument().insertString(
              xQueryEditor.getCaretPosition(),
              XMLUtilities.getTagEnclosedVariableContentXQuery(sourceVariableComboBox.getSelectedVariable()),
              null
          );        
        } catch (Exception e) {
          xQueryEditor.setText(
              xQueryEditor.getText() + 
              XMLUtilities.getTagEnclosedVariableContentXQuery(
                  sourceVariableComboBox.getSelectedVariable()
              )
          );
        }
        enableDoneButtonIfAppropriate();  
      }
    });

    return inputVariableQueryContentButton; 
  }
  
  private JButton getNewOutputVariableButton() {
    newVariableButton = new JButton("Create...");
    newVariableButton.setMnemonic(KeyEvent.VK_R);
    newVariableButton.setToolTipText("Creates a new variable for this query");
    newVariableButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        DataVariable variable = new DataVariable();

        TaskDataVariableUpdateDialog variableUpdateDialog;

        if (outputType == DataVariable.SCOPE_NET) {
          variableUpdateDialog = DataVariableUpdateDialogFactory.getNetDialog(parent);
        } else {
          variableUpdateDialog = DataVariableUpdateDialogFactory.getTaskDialog(parent);
        }
        
        variableUpdateDialog.setVariable(variable);
        outputVariableScope.addVariable(variable);
        variableUpdateDialog.setVisible(true);

        if (variableUpdateDialog.cancelButtonSelected()) {
          outputVariableScope.removeVariable(variable);
        } else {
          parameter.setVariable(variable);
          populateOutputVariableComboBox();
        }
      }
    });

    return newVariableButton; 
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
  
  private JPanel getXQueryEditorPanel() {
    xQueryEditorPanel = new XQueryEditorPanel();
    return xQueryEditorPanel;
  }

  
  private JComboBox getSourceVariableComboBox() {
    
    sourceVariableComboBox = new DataVariableComboBox(
      transitionType == NET_TO_TASK ? 
          DataVariableSet.VALID_USAGE_INPUT_FROM_NET :
          DataVariableSet.VALID_USAGE_OUTPUT_FROM_TASK
    );
    return sourceVariableComboBox;
  }
  
  private JComboBox getSinkVariableComboBox() {
    sinkVariableComboBox = new VariableParameterComboBox(
      transitionType == NET_TO_TASK ? 
          DataVariableSet.VALID_USAGE_INPUT_TO_TASK :
          DataVariableSet.VALID_USAGE_OUTPUT_TO_NET
    );
    
    sinkVariableComboBox.addActionListener(
      new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          xQueryEditorPanel.setSinkVariableName(
            (String) sinkVariableComboBox.getSelectedItem()    
          );
        }
      }
    );
    
    return sinkVariableComboBox;
  }
  
  private DataVariable getVariableWithName(String name) {
    return outputVariableScope.getVariableWithName(name);
  }
  
  private void populateInputVariableComboBox() {
    sourceVariableComboBox.setEnabled(false);

    sourceVariableComboBox.setDecomposition(inputVariableScope);
    
    if (sourceVariableComboBox.getItemCount() > 0) {
      sourceVariableComboBox.setEnabled(true);
      inputVariableQueryContentButton.setEnabled(true);
      inputVariableQueryElementButton.setEnabled(true);
    } else {
      sourceVariableComboBox.setEnabled(false);
      inputVariableQueryContentButton.setEnabled(false);
      inputVariableQueryElementButton.setEnabled(false);
    }
  }  
  
  private void populateOutputVariableComboBox() {
    sinkVariableComboBox.setEnabled(false);

    sinkVariableComboBox.setDetail(
        parameter, 
        outputVariableScope
    );
    
    if (sinkVariableComboBox.getItemCount() > 0) {
      sinkVariableComboBox.setSelectedItem(parameter.getVariable().getName());
      sinkVariableComboBox.setEnabled(true);
      if (parameter.getVariable().getName().equals("")) {
        parameter.setVariable(
            getVariableWithName(
                (String) sinkVariableComboBox.getSelectedItem()
            )
        );
      }
    } else {
      xQueryEditor.setTargetVariableName(
          (String) sinkVariableComboBox.getSelectedItem()
      );
      newVariableButton.setSelected(true);
      sinkVariableComboBox.setEnabled(false);
    }

    enableDoneButtonIfAppropriate();  
  }
  
  public void setContent() {
    populateInputVariableComboBox();
    populateOutputVariableComboBox();
    xQueryEditor.setText(parameter.getQuery());
    xQueryEditor.setTargetVariableName(
        (String) sinkVariableComboBox.getSelectedItem()
    );
    enableDoneButtonIfAppropriate();
  }
  
  private boolean shouldDoneButtonBeEnabled() {
    return (!xQueryEditor.getText().equals("") && 
            sinkVariableComboBox.isEnabled());
  }
  
  private void enableDoneButtonIfAppropriate() {
    if (shouldDoneButtonBeEnabled()) {
      getDoneButton().setEnabled(true);
    } else {
      getDoneButton().setEnabled(false);
    }
  }
  
  class XQueryEditorPanel extends JPanel {
    
    private JLabel openingSinkTagLabel = new JLabel("<sinkVarible>");
    private JLabel closingSinkTagLabel = new JLabel("</sinkVarible>");
    
    public XQueryEditorPanel() {
      super(new BorderLayout());
      setContent();
    }
    
    private void setContent() {
      setBorder(
          new CompoundBorder(
            new TitledBorder("XQuery"),
            new EmptyBorder(0,5,5,5)
          )
      );

      xQueryEditor = new JXQueryEditorPane();
      xQueryEditor.setMinimumSize(new Dimension(400,400));
      xQueryEditor.addKeyListener(new ParameterEditorDocumentListener());

      add(openingSinkTagLabel,BorderLayout.NORTH);
        
      add(new JScrollPane(xQueryEditor),BorderLayout.CENTER);

      add(closingSinkTagLabel,BorderLayout.SOUTH);
    }
    
    public void setSinkVariableName(String sinkVariableName) {
      openingSinkTagLabel.setText("<" + sinkVariableName + ">");
      closingSinkTagLabel.setText("</" + sinkVariableName + ">");
    }
  }
  
  class ParameterEditorDocumentListener implements KeyListener {
    
    public void keyPressed(KeyEvent e) {
      // deliberately does nothing
    }
    
    public void keyTyped(KeyEvent e) {
      // deliberately does nothing
    }

    public void keyReleased(KeyEvent e) {
      getDoneButton().setEnabled(shouldDoneButtonBeEnabled());
    }
  }
}
