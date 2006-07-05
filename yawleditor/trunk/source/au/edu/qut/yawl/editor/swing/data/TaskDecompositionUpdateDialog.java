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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;

import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.DataVariableSet;
import au.edu.qut.yawl.editor.data.Decomposition;
import au.edu.qut.yawl.editor.data.WebServiceDecomposition;

import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.swing.data.YawlServiceComboBox;

import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;

public class TaskDecompositionUpdateDialog extends NetDecompositionUpdateDialog {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private YawlServiceComboBox yawlServiceComboBox;
  
  private JPanel webServicePanel;
  
  private String cachedYAWLServiceID;
  
  public TaskDecompositionUpdateDialog(Decomposition decomposition) {
    super(decomposition);
    setTitle(DataVariable.SCOPE_TASK);
    
    getDoneButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        
        if (yawlServiceComboBox.getSelectedItemID() != null) {
          if (cachedYAWLServiceID != null) {
            if (!yawlServiceComboBox.getSelectedItemID().equals(cachedYAWLServiceID)) {
               applyChange();              
            }
          } else { // getSelectedItemID != null && cachedYAWLServiceID == null 
            applyChange();
          }
        } else { // getSelectedItemID == null 
          if (cachedYAWLServiceID != null) {
            applyChange();
          }
        }
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
  
  
  private WebServiceDecomposition getWebServiceDecomposition() {
    return (WebServiceDecomposition) getDecomposition();
  }
  
  protected JPanel getDecompositionPanel() {
    JPanel panel = super.getDecompositionPanel();
    
    panel.add(getWebServiceDecompositionPanel(), BorderLayout.SOUTH);

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
      }
    });
    return yawlServiceComboBox;
  }
  
  protected void setContent() {
    super.setContent();

    cachedYAWLServiceID = getWebServiceDecomposition().getYawlServiceID();

    setTitle(DataVariable.SCOPE_TASK);
    
    yawlServiceComboBox.setEnabled(false);
    
    Thread refreshThread = new Thread(){
      public void run() {
        yawlServiceComboBox.refresh();
        yawlServiceComboBox.setSelectedItem(getWebServiceDecomposition().getYawlServiceDescription());
        if (yawlServiceComboBox.getItemCount() > 1) {
          yawlServiceComboBox.setEnabled(true);
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
}