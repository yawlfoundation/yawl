/*
 * Created on 09/10/2003
 * YAWLEditor v1.0 
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
 *
 */

package au.edu.qut.yawl.editor.actions.element;

import au.edu.qut.yawl.editor.net.NetGraph;
import au.edu.qut.yawl.editor.net.NetGraphModel;
import au.edu.qut.yawl.editor.actions.net.YAWLSelectedNetAction;
import au.edu.qut.yawl.editor.elements.model.YAWLCompositeTask;
import au.edu.qut.yawl.editor.elements.model.YAWLVertex;

import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;
import au.edu.qut.yawl.editor.swing.JUtilities;
import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;
import au.edu.qut.yawl.editor.swing.net.YAWLEditorNetFrame;
import au.edu.qut.yawl.editor.swing.YAWLEditorDesktop;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.util.Set;
import java.util.Arrays;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;


public class SetUnfoldingNetAction extends YAWLSelectedNetAction implements TooltipTogglingWidget {

  private static final NetDialog netDialog = new NetDialog();

  private NetGraph graph;
  private YAWLCompositeTask task;
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Unfold to net...");
    putValue(Action.LONG_DESCRIPTION, "Specify the net this task unfolds to.");
    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_U));
  }
  
  public SetUnfoldingNetAction(YAWLCompositeTask task, NetGraph graph) {
    super();
    this.task = task;
    this.graph = graph;
  }  

  public void actionPerformed(ActionEvent event) {

    netDialog.setTask(graph, task);
    netDialog.setVisible(true);

    graph.clearSelection();
  }
  
  public String getEnabledTooltipText() {
    return " Specify the net this task unfolds to ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have a composite task selected" + 
           " to specify the net it unfolds to ";
  }
}

class NetDialog extends AbstractDoneDialog {
  protected NetGraph graph;
  protected YAWLCompositeTask task;
  
  protected JComboBox netComboBox;
  
  public NetDialog() {
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
