/*
 * Created on 6/08/2004
 * YAWLEditor v1.01 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.CompoundBorder;

import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.Decomposition;
import au.edu.qut.yawl.editor.data.WebServiceDecomposition;

import au.edu.qut.yawl.editor.swing.data.YawlServiceComboBox;


public class TaskDecompositionUpdateDialog extends NetDecompositionUpdateDialog {
  
  /* TODO: the serviceURI and serviceOperation fields should only be active when the
     yawlServiceComboBox is with the the RPC-Web Service option */
  
  private JTextField serviceUriField;
  private JTextField serviceOperationField;

  private YawlServiceComboBox yawlServiceComboBox;
  
  private JPanel webServicePanel;
  
  public TaskDecompositionUpdateDialog(Decomposition decomposition) {
    super(decomposition);
    setTitle(DataVariable.SCOPE_TASK);
    
    getDoneButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        
        WebServiceDecomposition wsDecomposition = 
          (WebServiceDecomposition) getDecomposition(); 
          
        wsDecomposition.setServiceDescriptionURI(serviceUriField.getText());
        wsDecomposition.setServiceOperation(serviceOperationField.getText());
        wsDecomposition.setYawlServiceDescription(
            (String) yawlServiceComboBox.getSelectedItem()
        );
        wsDecomposition.setYawlServiceID(
            yawlServiceComboBox.getSelectedItemID()
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
        new TitledBorder("Web Service Detail")
      )
    );
    
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.5;
    gbc.weighty = 0;
    gbc.insets = new Insets(0,0,5,5);
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
    
    JLabel serviceOperationLabel = new JLabel("WSDL Operation:");
    serviceOperationLabel.setDisplayedMnemonic('O');
    
    webServicePanel.add(serviceOperationLabel, gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    webServicePanel.add(getServiceOperationField(),gbc);
    serviceOperationLabel.setLabelFor(serviceOperationField);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.insets = new Insets(0,0,7,5);
    gbc.anchor = GridBagConstraints.EAST;
    
    JLabel serviceUriLabel = new JLabel("WSDL URI:");
    serviceUriLabel.setDisplayedMnemonic('W');
    
    webServicePanel.add(serviceUriLabel, gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    webServicePanel.add(getServiceUriField(),gbc);
    serviceUriLabel.setLabelFor(serviceUriField);

    return webServicePanel;
  }

  private JTextField getServiceUriField() {
    serviceUriField = new JTextField(10);
    return serviceUriField;
  }

  private JTextField getServiceOperationField() {
    serviceOperationField = new JTextField(10);
    return serviceOperationField;
  }
  
  private YawlServiceComboBox getYawlServiceComboBox() {
    yawlServiceComboBox = new YawlServiceComboBox();
    return yawlServiceComboBox;
  }
  
  protected void setContent() {
    super.setContent();
    setTitle(DataVariable.SCOPE_TASK);
    serviceUriField.setText(getWebServiceDecomposition().getServiceDescriptionURI());
    serviceOperationField.setText(getWebServiceDecomposition().getServiceOperation());
    
    yawlServiceComboBox.setEnabled(false);
    yawlServiceComboBox.refresh();
    yawlServiceComboBox.setSelectedItem(getWebServiceDecomposition().getYawlServiceDescription());
    if (yawlServiceComboBox.getItemCount() > 1) {
      yawlServiceComboBox.setEnabled(true);
    }
  }
}