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

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.editor.foundations.LogWriter;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.resourcing.SelectCodeletDialog;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.thirdparty.engine.YAWLEngineProxy;
import org.yawlfoundation.yawl.editor.thirdparty.resourcing.ResourcingServiceProxy;

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

  private String cachedYAWLServiceID;
  private SelectCodeletDialog codeletDialog ;
  
  protected JPanel attributesPanel; //MLF
  protected ExtendedAttributesTableModel model; //MLF
  protected JTabbedPane pane; //MLF
  
  protected NetGraph graph;
  
  public TaskDecompositionUpdateDialog(Decomposition decomposition, NetGraph graph) {
    super(decomposition);
    this.graph = graph;
    setTitle(DataVariable.SCOPE_TASK);
    
    getDoneButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {

        String selectedYAWLServiceID = yawlServiceComboBox.getSelectedItemID();
        if (selectedYAWLServiceID != null) {
            if (cachedYAWLServiceID != null) {
                if (!selectedYAWLServiceID.equals(cachedYAWLServiceID)) {
                   applyChange();
                }
            }
            else { // getSelectedItemID != null && cachedYAWLServiceID == null
                applyChange();
            }
        }
        else { // getSelectedItemID == null
            if (cachedYAWLServiceID != null) {
                applyChange();
            }
            else {  // if both cached & selected are null, the def. worklist is selected
                getWebServiceDecomposition().setYawlServiceDescription(
                    (String) yawlServiceComboBox.getSelectedItem()
                );
            }
        }

        getWebServiceDecomposition().setManualInteraction(! cbxAutomated.isSelected());
        if (! cbxAutomated.isSelected()) getWebServiceDecomposition().setCodelet(null);
        getWebServiceDecomposition().setVariables(getDataVariablePanel().getVariables());

      }
      
      private void applyChange() {
        
        getWebServiceDecomposition().setYawlServiceID(
            yawlServiceComboBox.getSelectedItemID()    
        );

        getWebServiceDecomposition().setYawlServiceDescription(
            (String) yawlServiceComboBox.getSelectedItem()
        );

        getDecomposition().setVariables(
            generateDataVariablesFromServiceSelection()
        );

        SpecificationModel.getInstance().propogateVariableSetChange(
            getDecomposition()  
        );
      }
    });
    
    getDataVariablePanel().setScope(DataVariable.SCOPE_TASK);
  }
  
  
  protected void setDecomposition(Decomposition decomposition) {
    super.setDecomposition(decomposition);
    model.setDecomposition(decomposition);
  }
  
  private WebServiceDecomposition getWebServiceDecomposition() {
    return (WebServiceDecomposition) getDecomposition();
  }
  
  protected JPanel getDecompositionPanel() {
    JPanel panel = super.getDecompositionPanel();
    
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
        if (!yawlServiceComboBox.isEnabled()) {
          return;
        }
        getDataVariablePanel().setVariables(
            generateDataVariablesFromServiceSelection()
        );
        cbxAutomated.setEnabled(yawlServiceComboBox.getSelectedIndex() == 0);
      }
    });
    return yawlServiceComboBox;
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
        }
    });
    result.add(cbxAutomated);

    btnCodelet = new JButton("Set Codelet...");
    btnCodelet.setMnemonic(KeyEvent.VK_S);
    btnCodelet.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            if (ResourcingServiceProxy.getInstance().isLiveService()) {
                if (codeletDialog == null) {
                    codeletDialog = new SelectCodeletDialog();
                    codeletDialog.setDecomposition(getWebServiceDecomposition());
                }
                codeletDialog.setLocationRelativeTo(YAWLEditor.getInstance());
                codeletDialog.setSelectedCodelet();
                codeletDialog.setVisible(true);
            }
            else {
              JOptionPane.showMessageDialog(null,
                   "A connection to a running resource service could not be etablished.\n" +
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

    cachedYAWLServiceID = getWebServiceDecomposition().getYawlServiceID();
    cbxAutomated.setSelected(! getWebServiceDecomposition().isManualInteraction());
    btnCodelet.setEnabled(cbxAutomated.isSelected()) ;

    setTitle(DataVariable.SCOPE_TASK);

    yawlServiceComboBox.setEnabled(false);

    Thread refreshThread = new Thread(){
      public void run() {
        yawlServiceComboBox.refresh();
          
          String serviceDescription = getWebServiceDecomposition().getYawlServiceDescription();

          // if there's an id (url) but no description it means this is a freshly loaded file,
          // so we have to associate the url with its description so that it can be set as
          // selected
          if (cachedYAWLServiceID != null) {
              if (serviceDescription == null) {
                 serviceDescription = yawlServiceComboBox.getDescriptionFromID(cachedYAWLServiceID);
              }
          }
          else {
              serviceDescription = "Default Engine Worklist";   // no url, so set to worklist
          }


        if (yawlServiceComboBox.getItemCount() > 1) {
          yawlServiceComboBox.setSelectedItem(serviceDescription);
        }
        pack();
      }
    };
    refreshThread.start();
  }
  
  public DataVariableSet generateDataVariablesFromServiceSelection() {

    DataVariableSet newVariableSet = new DataVariableSet();

    if (yawlServiceComboBox.getSelectedItemID() == null && 
        cachedYAWLServiceID == null) {

      newVariableSet.addVariables(
          getDataVariablePanel().getVariables().getUserDefinedVariables()
      );

      return newVariableSet;
    }

    newVariableSet.addVariables(
      YAWLEngineProxy.getInstance().getEngineParametersForRegisteredService(
            yawlServiceComboBox.getSelectedItemID()    
      )
    );
    
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
    ExtendedAttributeEditor editor = new ExtendedAttributeEditor(this, DialogMode.TASK);
    getDoneButton().addActionListener(editor);
    table.setDefaultEditor(ExtendedAttribute.class, editor);
    table.setDefaultRenderer(ExtendedAttribute.class, new ExtendedAttributeRenderer());

    attributesPanel.add(new JScrollPane(table));
    attributesPanel.setVisible(true);

    return attributesPanel;
  }
  //END: MLF
}