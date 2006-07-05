/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
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
 *
 */

package au.edu.qut.yawl.editor.actions.element;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.actions.net.YAWLSelectedNetAction;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.elements.model.AtomicTask;
import au.edu.qut.yawl.editor.elements.model.MultipleAtomicTask;
import au.edu.qut.yawl.editor.elements.model.VertexLabel;
import au.edu.qut.yawl.editor.elements.model.VertexContainer;

import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.swing.element.AbstractVertexDoneDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;


public class LabelElementAction extends YAWLSelectedNetAction {

  private static final LabelDialog labelDialog = new LabelDialog();

  private NetGraph graph;
  private YAWLVertex vertex;
  
  {
    putValue(Action.SHORT_DESCRIPTION, " Label this element ");
    putValue(Action.NAME, "Set Label...");
    putValue(Action.LONG_DESCRIPTION, "Labels this element.");
    putValue(Action.SMALL_ICON, getIconByName("LabelElement"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_L));
  }
  
  public LabelElementAction(YAWLVertex vertex, NetGraph graph) {
    super();
    this.vertex = vertex;
    this.graph = graph;
  }  

  public void actionPerformed(ActionEvent event) {
    String oldLabelText = null;
    
    if (vertex!= null && vertex.getParent() != null) {
      VertexContainer container = (VertexContainer) vertex.getParent();
      VertexLabel label = container.getLabel();
      if (label != null) {
        oldLabelText = label.toString();         
      }
    }

    labelDialog.setVertex(graph, vertex, oldLabelText);
    labelDialog.setVisible(true);

    graph.clearSelection();
  }
}

class LabelDialog extends AbstractVertexDoneDialog {
  protected String labelText;
  
  protected JTextField labelField;
  
  public LabelDialog() {
    super(null, true, true);
    setContentPanel(getLabelPanel());
    getDoneButton().addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          if(labelText == null || !labelText.equals(labelField.getText())) {
          	if (getVertex() instanceof AtomicTask || 
          	    getVertex() instanceof MultipleAtomicTask) {
							if (SpecificationModel.getInstance().isValidLabelForAtomicTasks(labelField.getText())) {
								labelVertex();
							}
          	} else {
              labelVertex();
            }
          }
          labelField.requestFocus();
        }
        
        private void labelVertex() {
					graph.setElementLabel(getVertex(), labelField.getText());
					labelText = labelField.getText();
        }
      }
    );
  }

  private JPanel getLabelPanel() {

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,0,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel label = new JLabel("Set label to:");
    label.setDisplayedMnemonic('S');
    panel.add(label, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    labelField = new JTextField(10){
      protected void processFocusEvent(FocusEvent e) {
        super.processFocusEvent(e);
        if (e.getID() == FocusEvent.FOCUS_GAINED) {
          selectAll();
        }
      }

      public void postActionEvent() {
        super.postActionEvent();
        selectAll();
      }
    };
    labelField.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          graph.setElementLabel(getVertex(), labelField.getText());
          labelText = labelField.getText();
        }
      }
    );

    label.setLabelFor(labelField);
    
    labelField.setText(labelText);
    panel.add(labelField, gbc);

    return panel;
  }
  
  public void setVertex(NetGraph graph, YAWLVertex vertex, String oldLabelText) {
    super.setVertex(vertex,graph);

    this.labelText = oldLabelText;
    labelField.setText(labelText);
  }
  
  public String getTitlePrefix() {
    return "Label ";
  }
}
