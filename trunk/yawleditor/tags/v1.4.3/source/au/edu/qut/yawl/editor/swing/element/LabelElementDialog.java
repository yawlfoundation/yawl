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

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.swing.JFormattedSafeXMLCharacterField;

public class LabelElementDialog extends AbstractVertexDoneDialog {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  protected JFormattedSafeXMLCharacterField labelField;
  
  public LabelElementDialog() {
    super(null, true, true);
    setContentPanel(getLabelPanel());
    getDoneButton().addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          graph.setElementLabel(getVertex(), labelField.getText());
        }
      }
    );
    getRootPane().setDefaultButton(getDoneButton());
    labelField.requestFocus();
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

    labelField = getLabelField();
    
    label.setLabelFor(labelField);
    
    panel.add(labelField, gbc);

    return panel;
  }
  
  private JFormattedSafeXMLCharacterField getLabelField() {

    /* 
       Note that using a JFormattedSafeXMLCharacterField here is a workaround
       The current bleeding-edge engine (BETA 7.2) throws a 'nana if there
       are certain special characters in the <name/> element of the XML
       specification, even though those special characters have been quoted at 
       export time.  For the time being, I'll just limit users to inputing
       "safe" (non-special) characters that this text field enforces.  
    */
    
    labelField = new JFormattedSafeXMLCharacterField(15);

    labelField.setToolTipText(" Enter a label to go under this net element. ");
    return labelField;
  }

  
  public void setVertex(YAWLVertex vertex, NetGraph graph) {
    super.setVertex(vertex,graph);

    labelField.setText(vertex.getLabel());
  }
  
  public String getTitlePrefix() {
    return "Label ";
  }
}
