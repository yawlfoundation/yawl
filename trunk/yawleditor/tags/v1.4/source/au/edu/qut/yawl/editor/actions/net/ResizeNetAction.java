/*
 * Created on 20/02/2006
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
 *
 */

package au.edu.qut.yawl.editor.actions.net;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.actions.net.YAWLSelectedNetAction;

import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;

import au.edu.qut.yawl.editor.net.NetGraph;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JViewport;

import javax.swing.border.EmptyBorder;

public class ResizeNetAction extends YAWLSelectedNetAction {

   private static final ResizeNetDialog dialog = new ResizeNetDialog();
   private boolean isFirstInvocation = false;
  
   {
    putValue(Action.SHORT_DESCRIPTION, " Resize this Net  ");
    putValue(Action.NAME, "Resize Net...");
    putValue(Action.LONG_DESCRIPTION, "Resize net.");
    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_R));
  }
  
  public void actionPerformed(ActionEvent event) {
    dialog.setNet(this.getGraph());
    if (!isFirstInvocation) {
      dialog.setLocationRelativeTo(YAWLEditor.getInstance());
      isFirstInvocation = true;       
    }
    dialog.setVisible(true);
  }
}

class ResizeNetDialog extends AbstractDoneDialog  {
  protected JSpinner netWidthSpinner;
  protected JSpinner netHeightSpinner;
  
  private int smallestAllowableWidth = 0;
  private int smallestAllowableHeight = 0;
  
  private NetGraph net;
  
  public ResizeNetDialog() {
    super("Resize Net", false);
    setContentPanel(getResizeDetailPanel());
    
    getDoneButton().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        net.setPreferredSize(
          new Dimension(
              Math.max(
                  ((Integer) netWidthSpinner.getModel().getValue()).intValue(),
                  smallestAllowableWidth                  
              ),
              Math.max(
                  ((Integer) netHeightSpinner.getModel().getValue()).intValue(),
                  smallestAllowableHeight
              )
          )
        );
        ((JViewport) net.getParent()).setViewSize(net.getPreferredSize());
      }
    });
  }
  
  public void setNet(NetGraph net) {
    this.net = net;
  }
  
  private JPanel getResizeDetailPanel() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,5,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel widthLabel = new JLabel("Net pixel width:");
    widthLabel.setDisplayedMnemonicIndex(11);
    panel.add(widthLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    netWidthSpinner = getNetWidthSpinner();

    widthLabel.setLabelFor(netWidthSpinner);
    
    panel.add(netWidthSpinner, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.insets = new Insets(0,0,0,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel heightLabel = new JLabel("Net pixel height:");
    heightLabel.setDisplayedMnemonicIndex(11);
    panel.add(heightLabel, gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    netHeightSpinner = getNetHeightSpinner();

    heightLabel.setLabelFor(netHeightSpinner);
    
    panel.add(netHeightSpinner, gbc);
    
    return panel;
  }
  
  private JSpinner getNetWidthSpinner() {
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));

    return spinner;
  }

  private JSpinner getNetHeightSpinner() {
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));

    return spinner;
  }

  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }

  public void setVisible(boolean state) {
    if (state == true) {
      setSpinnerValues();
    }
    super.setVisible(state);
  }
  
  private void setSpinnerValues() {
    setNetWidthSpinnerValue();
    setNetHeightSpinnerValue();
    pack();
  }

  private void setNetWidthSpinnerValue() {
    netWidthSpinner.setEnabled(false);

    smallestAllowableWidth = (int) net.getCellBounds(net.getRoots()).getMaxX();

    netWidthSpinner.setModel(
        new SpinnerNumberModel(
            (int) net.getBounds().getWidth(), 
            smallestAllowableWidth, 
            9999, 
            1
        )
    );

    netWidthSpinner.setEnabled(true);
  }

  private void setNetHeightSpinnerValue() {
    netHeightSpinner.setEnabled(false);

    smallestAllowableHeight = (int) net.getCellBounds(net.getRoots()).getMaxY();
    
    netHeightSpinner.setModel(
        new SpinnerNumberModel(
            (int) net.getBounds().getHeight(), 
            smallestAllowableHeight, 
            9999, 
            1
        )
    );  
    
    netHeightSpinner.setEnabled(true);
  }
}