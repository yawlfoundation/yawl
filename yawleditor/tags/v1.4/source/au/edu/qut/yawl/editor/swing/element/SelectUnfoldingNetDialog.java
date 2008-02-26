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
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import au.edu.qut.yawl.editor.elements.model.YAWLCompositeTask;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;
import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.net.NetGraphModel;
import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;
import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.YAWLEditorDesktop;
import au.edu.qut.yawl.editor.swing.net.YAWLEditorNetFrame;

public class SelectUnfoldingNetDialog extends AbstractDoneDialog {
  protected NetGraph graph;
  protected YAWLCompositeTask task;
  
  protected JComboBox netComboBox;
  
  public SelectUnfoldingNetDialog() {
    super("Composite Task Unfolding", true);
    setContentPanel(getUnfoldingNetPanel());

    getDoneButton().addActionListener(new ActionListener(){
       public void actionPerformed(ActionEvent e) {
         NetGraphModel netModel = 
           SpecificationModel.getInstance().getNetModelFromName(
             (String) netComboBox.getSelectedItem()
            );
         
         if (netModel != null) {
           graph.setUnfoldingNet(task, netModel.getGraph());
         } else {
           graph.setUnfoldingNet(task, null);
         }
       }
    });
  }

  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }

  public void setVisible(boolean state) {
    if (state == true) {
      JUtilities.centreWindowUnderVertex(graph, this, (YAWLVertex) task, 10);
    }
    super.setVisible(state);
  }
  
  private JPanel getUnfoldingNetPanel() {

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,0,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel label = new JLabel("This task unfolds to the net:");
    label.setDisplayedMnemonic('u');
    panel.add(label, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    netComboBox = new JComboBox();
    label.setLabelFor(netComboBox);
    
    panel.add(netComboBox, gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.CENTER;
    
    panel.add(getCreateButton(), gbc);

    return panel;
  }
  
  public void setTask(NetGraph graph, YAWLCompositeTask task) {
    this.graph = graph;
    this.task = task;
    populateComboBox();
  }
  
  private void populateComboBox() {
    netComboBox.setEnabled(false);
    netComboBox.removeAllItems();
    netComboBox.addItem(null);

    Set netSet = SpecificationModel.getInstance().getNets();
    
    Object[] netSetArray = netSet.toArray();
    String[] netSetNames = new String[netSet.size()];
    for(int i = 0; i < netSet.size(); i++) {
      netSetNames[i] = ((NetGraphModel) netSetArray[i]).getName();
    }

    try {
      Arrays.sort(netSetNames);
    } catch (Exception e) {};
    
    for(int i = 0; i < netSetNames.length; i++) {
      netComboBox.addItem(netSetNames[i]);
      
      if (task.getUnfoldingNetName() != null && 
          task.getUnfoldingNetName().equals(netSetNames[i])) {
        netComboBox.setSelectedItem(netSetNames[i]);
      }
    }
    netComboBox.setEnabled(true);
    pack();
  }
  
  private JButton getCreateButton() {
    JButton button = new JButton("Create...");
    button.setMnemonic(KeyEvent.VK_C);
    
    final AbstractDoneDialog dialog = this;
    
    button.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        YAWLEditorNetFrame newFrame = YAWLEditorDesktop.getInstance().newNet();

        graph.setUnfoldingNet(task, newFrame.getGraph());

        dialog.setVisible(false);
      }
    });
    
    return button;
  }
}