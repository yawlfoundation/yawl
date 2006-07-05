/*
 * Created on 21/02/2006
 * YAWLEditor v1.4
 *
 * @author Lindsay Bradford
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

package au.edu.qut.yawl.editor.swing.element;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import au.edu.qut.yawl.editor.elements.model.YAWLCondition;
import au.edu.qut.yawl.editor.net.NetGraph;

public class LabelConditionDialog extends AbstractVertexDoneDialog {

  protected String labelText;
  protected JTextField labelField;
  
  public LabelConditionDialog() {
    super(null, true, true);
    setContentPanel(getLabelPanel());
    getDoneButton().addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          if(labelText == null || !labelText.equals(labelField.getText())) {
            labelCondition();
          }
          labelField.requestFocus();
        }
        
        private void labelCondition() {
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

    label.setLabelFor(labelField);
    
    labelField.setText(labelText);
    panel.add(labelField, gbc);

    return panel;
  }
  
  public void setCondition(NetGraph graph, YAWLCondition condition) {
    super.setVertex(condition,graph);

    labelField.setText(condition.getLabel());
  }
  
  public String getTitlePrefix() {
    return "Label ";
  }
}
