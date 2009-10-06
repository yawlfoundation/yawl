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

package org.yawlfoundation.yawl.editor.swing.data;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.data.Parameter;
import org.yawlfoundation.yawl.editor.foundations.ResourceLoader;
import org.yawlfoundation.yawl.editor.foundations.XMLUtilities;
import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.swing.JUtilities;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ParameterUpdateDialog extends AbstractDoneDialog {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

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

  private JRadioButton rbElement;
  private JRadioButton rbExpression;
  
  private JButton newVariableButton;
  private XQueryEditorPanel xQueryEditorPanel;
  
//  private XQueryDescriptorLabel xQueryButtonDescriptor;
  
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
    
    getRootPane().setDefaultButton(getCancelButton());
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
    setSize(600,450);
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

  public void setRadioButtonSelected() {
    if (rbElement != null) rbElement.setSelected(true);
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

    if (transitionType == NET_TO_TASK) {
      gbc.anchor = GridBagConstraints.EAST;
      gbc.insets = new Insets(0,0,5,5);

      JLabel sourceVariableLabel =
        new JLabel(
            "from element of " +
            DataVariable.scopeToString(inputType).toLowerCase() +
            " variable:"
        );
      sourceVariableLabel.setHorizontalAlignment(JLabel.RIGHT);
      sourceVariableLabel.setDisplayedMnemonic('v');
      panel.add(sourceVariableLabel, gbc);
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.anchor = GridBagConstraints.EAST;
    }
    else {
      gbc.anchor = GridBagConstraints.FIRST_LINE_START;
      rbElement = new JRadioButton("from element of " +
            DataVariable.scopeToString(inputType).toLowerCase() +
            " variable:"
        );
      rbElement.setMnemonic('v');
      rbElement.setSelected(true);
      rbElement.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          enableElementControls(true);
        }
      } ) ;
      panel.add(rbElement, gbc);

      gbc.gridy++;
      rbExpression = new JRadioButton("from expression");
      rbExpression.setMnemonic('x');
      rbExpression.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          enableElementControls(false);
        }
      } ) ;
      panel.add(rbExpression, gbc);

      ButtonGroup group = new ButtonGroup();
      group.add(rbElement);
      group.add(rbExpression);
    
    gbc.gridy = 0;
    }

    gbc.gridx++;
    gbc.insets = new Insets(0,5,5,5);
    panel.add(getSourceVariableComboBox(),gbc);
    
    gbc.gridx++;
    gbc.gridheight = 1;
    gbc.anchor = GridBagConstraints.CENTER;
    panel.add(getNewInputVariableQueryContentButton(),gbc);
    
    gbc.gridy++;
    panel.add(getNewInputVariableQueryElementButton(),gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 3;
    gbc.insets = new Insets(0,0,5,5);
    
//    panel.add(getXQueryButtonDescriptor(), gbc);

    gbc.weighty = 1;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridy++;

    panel.add(getXQueryEditorPanel(),gbc);

    gbc.gridy++;
    gbc.gridwidth = 1;
    gbc.weighty = 0;
    gbc.weightx = 0.333;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.EAST;

    gbc.insets = new Insets(0,0,5,5);
    JLabel sinkVariableLabel = 
      new JLabel("populates the " + 
          DataVariable.scopeToString(outputType).toLowerCase() + 
          " variable:"
      );
    sinkVariableLabel.setHorizontalAlignment(JLabel.RIGHT);
    sinkVariableLabel.setDisplayedMnemonic('p');

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

  private void enableElementControls(boolean flag) {
    inputVariableQueryElementButton.setEnabled(flag);
    inputVariableQueryContentButton.setEnabled(flag);
    sourceVariableComboBox.setEnabled(flag);
  }
  
  private JButton getNewInputVariableQueryElementButton() {
    inputVariableQueryElementButton = new JButton("add XQuery of entire element");
    inputVariableQueryElementButton.setToolTipText("Generates an XQuery returning the entire variable XML element");
    inputVariableQueryElementButton.setMnemonic(KeyEvent.VK_E);
    inputVariableQueryElementButton.setDisplayedMnemonicIndex(7);

    inputVariableQueryElementButton.setIcon(
        ResourceLoader.getImageAsIcon("/org/yawlfoundation/yawl/editor/resources/menuicons/Warning16.gif")    
    );
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
    
//    inputVariableQueryElementButton.addMouseListener(new MouseAdapter() {
//      public void mouseEntered(MouseEvent e) {
//        if (!inputVariableQueryElementButton.isEnabled()) {
//          return;
//        }
//        xQueryButtonDescriptor.setMode(
//          XQueryDescriptorLabel.DISPLAY_FOR_ENTIRE_ELEMENT
//        );
//        xQueryEditor.refreshDividerLocation();
//      }
//
//      public void mouseExited(MouseEvent e) {
//        if (!inputVariableQueryElementButton.isEnabled()) {
//          return;
//        }
//        xQueryButtonDescriptor.setMode(
//            XQueryDescriptorLabel.DISPLAY_NOTHING
//        );
//        xQueryEditor.refreshDividerLocation();
//      }
//    });

    return inputVariableQueryElementButton; 
  }

  private JButton getNewInputVariableQueryContentButton() {
    inputVariableQueryContentButton = new JButton("add XQuery of element's content");
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

//    inputVariableQueryContentButton.addMouseListener(new MouseAdapter() {
//      public void mouseEntered(MouseEvent e) {
//        if (!inputVariableQueryContentButton.isEnabled()) {
//          return;
//        }
//        xQueryButtonDescriptor.setMode(
//          XQueryDescriptorLabel.DISPLAY_FOR_CONTENT_ONLY
//        );
//        xQueryEditor.refreshDividerLocation();
//      }
//
//      public void mouseExited(MouseEvent e) {
//        if (!inputVariableQueryContentButton.isEnabled()) {
//          return;
//        }
//        xQueryButtonDescriptor.setMode(
//            XQueryDescriptorLabel.DISPLAY_NOTHING
//        );
//        xQueryEditor.refreshDividerLocation();
//      }
//    });

    return inputVariableQueryContentButton; 
  }
  
//  private XQueryDescriptorLabel getXQueryButtonDescriptor() {
//    xQueryButtonDescriptor = new XQueryDescriptorLabel();
//
//    xQueryButtonDescriptor.setHorizontalAlignment(JLabel.CENTER);
//    xQueryButtonDescriptor.setForeground(Color.GREEN.darker().darker());
//
//    return xQueryButtonDescriptor;
//  }
  
  private JButton getNewOutputVariableButton() {
    newVariableButton = new JButton("Create...");
    newVariableButton.setMnemonic(KeyEvent.VK_R);
    newVariableButton.setToolTipText("Creates a new variable for this query");
    newVariableButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        DataVariable variable = new DataVariable();

        DataVariableUpdateDialog variableUpdateDialog;

        if (outputType == DataVariable.SCOPE_NET) {
          variableUpdateDialog = DataVariableUpdateDialogFactory.getNetDialog(parent);
        } else {
          variableUpdateDialog = DataVariableUpdateDialogFactory.getTaskDialog(parent);
        }
        
        outputVariableScope.addVariable(variable);
        variableUpdateDialog.setVariable(variable);
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
        
        /* 
         * Linds story time: For most of today I've been screwing
         * with trying to get the split-pane of the xQueryEditor
         * to behave when I want it to correctly position itself.
         * It works in all cases except the very first time the
         * dialog box appears.  Given the high wierdness I've been 
         * experiencing, I ended up  suspecting that threading might 
         * be behind the oddness, so I added a very small delay (using the
         * Timer below) before actually trying to refresh the divider
         * of that split-pane.  Now it works.
         */
        
        ActionListener sliderChanger = new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            xQueryEditor.refreshDividerLocation();
          }
        };
        
        Timer timer = new Timer(75, sliderChanger);
        timer.setRepeats(false);
        timer.start();
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
    xQueryEditor.setText(XMLDialogFormatter.format(parameter.getQuery()));
    xQueryEditor.setTargetVariableName(
        (String) sinkVariableComboBox.getSelectedItem()
    );
//    xQueryButtonDescriptor.setMode(
//        XQueryDescriptorLabel.DISPLAY_NOTHING
//    );
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
    
    private static final long serialVersionUID = 1L;
    private JLabel openingSinkTagLabel = new JLabel("<sinkVarible>");
    private JLabel closingSinkTagLabel = new JLabel("</sinkVarible>");
    private JButton btnFormat = new JButton(
              ResourceLoader.getImageAsIcon(
                  "/org/yawlfoundation/yawl/editor/resources/taskicons/AutoFormat.png")
              );
    
    public XQueryEditorPanel() {
      super(new BorderLayout());
      buildContent();
    }
    
    private void buildContent() {
      setBorder(
          new CompoundBorder(
            new TitledBorder("XQuery"),
            new EmptyBorder(0,5,5,5)
          )
      );

      xQueryEditor = new JXQueryEditorPane();
      xQueryEditor.setMinimumSize(new Dimension(400,400));

      xQueryEditor.getXQueryEditor().addKeyListener(
          new ParameterEditorDocumentListener()
      );

      btnFormat.setToolTipText("Auto-format content");
        
      btnFormat.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
              String text = xQueryEditor.getText();
              xQueryEditor.setText(XMLDialogFormatter.format(text));
          }
      });

      JPanel top = new JPanel(new BorderLayout());
      top.add(openingSinkTagLabel, BorderLayout.WEST);
      top.add(btnFormat, BorderLayout.EAST);

      add(top, BorderLayout.NORTH);
       
      add(xQueryEditor,BorderLayout.CENTER);

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
      getDoneButton().setEnabled(shouldDoneButtonBeEnabled());
    }

    public void keyReleased(KeyEvent e) {
      // deliberately does nothing
    }
  }
  
  class XQueryDescriptorLabel extends JLabel {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public static final int DISPLAY_NOTHING = 0;
    public static final int DISPLAY_FOR_CONTENT_ONLY = 1;
    public static final int DISPLAY_FOR_ENTIRE_ELEMENT = 2;
    
    private static final String DISPLAY_FOR_CONTENT_ONLY_TEXT = 
      "<html><body>This query is all that's needed for assigning the value of<p>" +
      "one variable to some type-compatible variable.</body></html>";
    
    private static final String DISPLAY_FOR_ENTIRE_ELEMENT_TEXT = 
      "<html><body>This query is typically used with similar queries to construct<p>" +
      "a complex-type variable value from several simple-type variable elements.<p>" +
      "Deep knowledge of XQuery is a necessity.</body></html>";
    
    private int displayMode = DISPLAY_NOTHING;    

    public XQueryDescriptorLabel() {
      super();
    }
    
    public void setMode(int mode) {
      if (displayMode == mode) {
        return;
      }
      displayMode = mode;
      renderDisplayForGivenMode();
    }
     
    public void renderDisplayForGivenMode() {
      switch(displayMode) {
        case DISPLAY_NOTHING: {
          setText(null);
          break;
        }
        case DISPLAY_FOR_CONTENT_ONLY: {
          setText(DISPLAY_FOR_CONTENT_ONLY_TEXT);
          break;
        }
        case DISPLAY_FOR_ENTIRE_ELEMENT: {
          setText(DISPLAY_FOR_ENTIRE_ELEMENT_TEXT);
          break;
        }
      }
    }
  }
}
