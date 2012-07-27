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

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.data.*;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.resourcing.SelectCodeletDialog;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class TaskDecompositionUpdateDialog extends NetDecompositionUpdateDialog {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private YawlServiceComboBox yawlServiceComboBox;
  
  private JPanel webServicePanel;
  private JCheckBox cbxAutomated ;
  private JButton btnCodelet;  

  private SelectCodeletDialog codeletDialog ;
    private boolean refreshing;
  
  protected JPanel attributesPanel; //MLF
  protected ExtendedAttributesTableModel model; //MLF
  protected JTabbedPane pane; //MLF

  protected LogPredicatesPanel logPredicatesPanel;  
  
  protected NetGraph graph;
  
  public TaskDecompositionUpdateDialog(Decomposition decomposition, NetGraph graph) {
    super(decomposition);
    this.graph = graph;
    setTitle(DataVariable.SCOPE_TASK);
    if (pane != null) pane.setSelectedIndex(0);  
    
    getDoneButton().addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            YAWLServiceReference currentService = getWebServiceDecomposition().getService();
            YAWLServiceReference selectedService = yawlServiceComboBox.getSelectedService();
            if (selectedService != null) {

                // if its the default worklist
                if (selectedService.getServiceName().equals(WebServiceDecomposition.ENGINE_WORKLIST_NAME)) {
                    DataVariableSet variableSet =
                            getWebServiceDecomposition().getCodelet() != null ?
                            generateDataVariablesFromCodeletSelection() : null;
                    applyChange(selectedService, variableSet);
                }
                else if (! selectedService.equals(currentService)) {
                    applyChange(selectedService, generateDataVariablesFromServiceSelection());
                }
            }

            getWebServiceDecomposition().setManualInteraction(! cbxAutomated.isSelected());
            if (! cbxAutomated.isSelected()) getWebServiceDecomposition().setCodelet(null);
            getWebServiceDecomposition().setVariables(getDataVariablePanel().getVariables());

            String startPredicate = logPredicatesPanel.getStartedPredicate();
            getWebServiceDecomposition().setLogPredicateStarted(startPredicate);

            String completionPredicate = logPredicatesPanel.getCompletionPredicate();
            getWebServiceDecomposition().setLogPredicateCompletion(completionPredicate);
        }

        
      private void applyChange(YAWLServiceReference selectedService,
                               DataVariableSet variableSet) {
          getWebServiceDecomposition().setService(selectedService);
          if (variableSet != null) {
              getDecomposition().setVariables(variableSet);
              SpecificationModel.getInstance().propogateVariableSetChange(getDecomposition());
          }
      }
    });
    
    getDataVariablePanel().setScope(DataVariable.SCOPE_TASK);
  }
  
  
  protected void setDecomposition(Decomposition decomposition) {
      refreshing = true;
    super.setDecomposition(decomposition);
    model.setDecomposition(decomposition);
    loadLogPredicates(decomposition);
      refreshing = false;
  }
  
  private WebServiceDecomposition getWebServiceDecomposition() {
    return (WebServiceDecomposition) getDecomposition();
  }
  
  protected JPanel getDecompositionPanel(int scope) {
    JPanel panel = super.getDecompositionPanel(DataVariable.SCOPE_TASK);  
    
    JPanel innerPanel = new JPanel(new BorderLayout()); //MLF

    innerPanel.add(getWebServiceDecompositionPanel(), BorderLayout.NORTH);
    innerPanel.add(getInteractionPanel(), BorderLayout.CENTER);
    innerPanel.add(getExtendedAttributeDisablePanel(), BorderLayout.SOUTH);
    panel.add(innerPanel, BorderLayout.SOUTH);

    pane = new JTabbedPane();
    
    pane.setFocusable(false);
    
    pane.addTab("Standard", panel); //MLF

    panel = new JPanel(new BorderLayout()); //MLF
    createAttributePanel(panel); //MLF
    pane.addTab("Extended Attributes", panel); //MLF

    logPredicatesPanel = new LogPredicatesPanel(30, 8, LogPredicatesPanel.Parent.Decomposition);
    pane.addTab("Log Predicates", logPredicatesPanel);

    panel = new JPanel(new BorderLayout()); //MLF
    panel.setBorder(new EmptyBorder(5,5,5,5));
    panel.add(pane, BorderLayout.CENTER); //MLF

    return panel;
  }
  
  //MLF: adds a panel that allows the disablement of the extended attributes
  private JPanel getExtendedAttributeDisablePanel() {
    JPanel panel = new JPanel();

    panel.setBorder(
        new EmptyBorder(10,10,10,12)
    );

    return panel;
  }

  private void loadLogPredicates(Decomposition decomp) {
      logPredicatesPanel.setStartedPredicate(decomp.getLogPredicateStarted());
      logPredicatesPanel.setCompletionPredicate(decomp.getLogPredicateCompletion());
  }


  private JPanel getWebServiceDecompositionPanel() {

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    webServicePanel = new JPanel(gbl);
    
    webServicePanel.setBorder(
      new CompoundBorder(
        new EmptyBorder(0,10,0,12),
        new TitledBorder("YAWL Registered Service Detail")
      )
    );
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.5;
    gbc.weighty = 0;
    gbc.insets = new Insets(0,5,5,5);
    gbc.anchor = GridBagConstraints.EAST;
    
    JLabel yawlServiceLabel = new JLabel("YAWL Service:");
    yawlServiceLabel.setDisplayedMnemonic('Y');
    
    webServicePanel.add(yawlServiceLabel, gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    webServicePanel.add(getYawlServiceComboBox(),gbc);
    yawlServiceLabel.setLabelFor(yawlServiceComboBox);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.anchor = GridBagConstraints.EAST;

    return webServicePanel;
  }

  private YawlServiceComboBox getYawlServiceComboBox() {
    yawlServiceComboBox = new YawlServiceComboBox();
    yawlServiceComboBox.setEnabled(false);
    yawlServiceComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if ((! yawlServiceComboBox.isEnabled()) || refreshing) {
            return;
        }
        cbxAutomated.setEnabled(isDefaultWorklistSelected());
        if (isDefaultWorklistSelected() &&
                (getWebServiceDecomposition().getCodelet() != null)) {
                getDataVariablePanel().setVariables(
                    generateDataVariablesFromCodeletSelection()
                );
             return;
        }
        getDataVariablePanel().setVariables(
                    generateDataVariablesFromServiceSelection()
                );

      }
    });
    return yawlServiceComboBox;
  }

  private boolean isDefaultWorklistSelected() {
      return yawlServiceComboBox.getSelectedItem().equals(
              WebServiceDecomposition.DEFAULT_WORKLIST_LABEL);
  }


  private JPanel getInteractionPanel() {
    JPanel result = new JPanel();

    result.setBorder(
      new CompoundBorder(
        new EmptyBorder(0,10,0,12),
        new TitledBorder("External Interaction")
      )
    );

    cbxAutomated = new JCheckBox("Automated");
    cbxAutomated.setMnemonic(KeyEvent.VK_A);
    cbxAutomated.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          btnCodelet.setEnabled(cbxAutomated.isSelected());
          yawlServiceComboBox.setEnabled(! cbxAutomated.isSelected());
          if (! cbxAutomated.isSelected()) {
              getWebServiceDecomposition().setCodelet(null);
              getDataVariablePanel().setVariables(
                          generateDataVariablesFromServiceSelection()
                      );
          }
        }
    });
    result.add(cbxAutomated);

    btnCodelet = new JButton("Set Codelet...");
    btnCodelet.setMnemonic(KeyEvent.VK_S);
    btnCodelet.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (YConnector.isResourceConnected()) {
                if (codeletDialog == null) {
                    codeletDialog = new SelectCodeletDialog();
                    codeletDialog.setDecomposition(getWebServiceDecomposition());
                }
                codeletDialog.setLocationRelativeTo(YAWLEditor.getInstance());
                codeletDialog.setSelectedCodelet();
                codeletDialog.setVisible(true);
                String codeletName = getWebServiceDecomposition().getCodelet();
                if (codeletName != null) {
                    getDataVariablePanel().setVariables(
                        generateDataVariablesFromCodeletSelection()
                    );
                }
            }
            else {
              JOptionPane.showMessageDialog(null,
                   "A connection to a running resource service could not be established.\n" +
                   "Codelets cannot be selected without a valid resource service connection.",
                   "Resource Service Connection Error", JOptionPane.ERROR_MESSAGE);
            }           
        }
    });    
    result.add(btnCodelet);

    return result;
  }


  protected void setContent() {
    super.setContent();
    cbxAutomated.setSelected(! getWebServiceDecomposition().isManualInteraction());
    btnCodelet.setEnabled(cbxAutomated.isSelected()) ;

    setTitle(DataVariable.SCOPE_TASK);

    yawlServiceComboBox.setEnabled(false);

    Thread refreshThread = new Thread(){
      public void run() {
        yawlServiceComboBox.refresh();
        yawlServiceComboBox.setEnabled(! cbxAutomated.isSelected());

        String serviceDescription = getWebServiceDecomposition().getServiceDescription();
        if (yawlServiceComboBox.getItemCount() > 1) {
            refreshing = true;
          yawlServiceComboBox.setSelectedItem(serviceDescription);
            refreshing = false;
        }
        pack();
      }
    };
    refreshThread.start();
  }


    public DataVariableSet generateDataVariablesFromCodeletSelection() {
        DataVariableSet newVariableSet = new DataVariableSet();
        newVariableSet.setDecomposition(getDecomposition());
        Decomposition decomp = getDecomposition();
        if (decomp instanceof WebServiceDecomposition) {
            String codelet = ((WebServiceDecomposition) decomp).getCodelet();
            if (codelet != null) {
                try {
                    newVariableSet.addVariables(DataVariableUtilities.convertYParameters(
                                            YConnector.getCodeletParameters(codelet)));
                }
                catch (IOException ioe) {
                    // failed to get codelet params
                }
            }
        }
        newVariableSet.addVariables(
                getDataVariablePanel().getVariables().getUserDefinedVariables()
        );

        return newVariableSet;
    }


    public DataVariableSet generateDataVariablesFromServiceSelection() {

        DataVariableSet newVariableSet = new DataVariableSet();
        newVariableSet.setDecomposition(getDecomposition());

        if (yawlServiceComboBox.getSelectedService() == null) {

            newVariableSet.addVariables(
                    getDataVariablePanel().getVariables().getUserDefinedVariables()
            );

            return newVariableSet;
        }
        String uri = yawlServiceComboBox.getSelectedService().getURI();
        try {
            newVariableSet.addVariables(
                    DataVariableUtilities.convertYParameters(
                            YConnector.getServiceParameters(uri)));

            java.util.List<DataVariable> currentVars =
                    getDataVariablePanel().getVariables().getUserDefinedVariables();

            int count = newVariableSet.size();
            if (count > 0) {
                for (DataVariable var : currentVars) {
                    var.setIndex(count++);
                }
            }
        }
        catch (IOException ioe) {
            // failed to get service params
        }
        newVariableSet.addVariables(
                getDataVariablePanel().getVariables().getUserDefinedVariables()
        );

        return newVariableSet;
    }
  
  //BEGIN: MLF
  private void createAttributePanel(JPanel panel)
  {
    try {
      model = new ExtendedAttributesTableModel(getDecomposition(), graph);

      panel.add(getAttributesPanel(), BorderLayout.NORTH);
    } catch (IOException e) {     //if the model can't be created we do nothing more.
      LogWriter.error("Error creating external attributes panel.", e) ;
    }
  }

  //assumes model has been initialised
  protected JPanel getAttributesPanel() {
    attributesPanel = new JPanel(new GridLayout(1,1));
    attributesPanel.setBorder(new EmptyBorder(10,10,10,12));

    JTable table = new JTable() ;

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
}