/*
 * Created on 9/10/2003
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

package org.yawlfoundation.yawl.editor.ui.actions.net;

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.swing.menu.MenuUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class SetStartingNetAction extends YAWLExistingNetAction {

    private static final long serialVersionUID = 1L;
    private static final StartingNetDialog dialog = new StartingNetDialog();
    private boolean isFirstInvocation = false;

    {
        putValue(Action.SHORT_DESCRIPTION, " Specify the net workflow execution starts in. ");
        putValue(Action.NAME, "Set Starting Net...");
        putValue(Action.LONG_DESCRIPTION, "Specify the net workflow execution starts in.");
        putValue(Action.SMALL_ICON, getPNGIcon("table_key"));
        putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_S));
        putValue(Action.ACCELERATOR_KEY, MenuUtilities.getAcceleratorKeyStroke("T"));
    }

    public SetStartingNetAction() {
    }

    public void actionPerformed(ActionEvent event) {
        if (!isFirstInvocation) {
            dialog.setLocationRelativeTo(YAWLEditor.getInstance());
            isFirstInvocation = true;
        }
        dialog.setVisible(true);
    }
}

/*********************************************************************************/

class StartingNetDialog extends AbstractDoneDialog {

    private static final long serialVersionUID = 1L;
    protected JComboBox netComboBox;
    protected JComboBox dbGatewayComboBox;


    public StartingNetDialog() {
        super("Choose Starting Net", true);
        setContentPanel(getStartingNetPanel());
        getDoneButton().addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    SpecificationModel specModel = SpecificationModel.getInstance();
                    if (netComboBox.isEnabled()) {
                        specModel.getNets().setRootNet(
                                (String) netComboBox.getSelectedItem());
                    }

                    String gateway = null;
                    if (dbGatewayComboBox.isEnabled() && (dbGatewayComboBox.getSelectedIndex() > 0)) {
                        gateway = (String) dbGatewayComboBox.getSelectedItem();
                    }
                    specModel.getNets().getRootNet().setExternalDataGateway(gateway);
                    SpecificationUndoManager.getInstance().setDirty(true);
                }
            }
        );
    }

    protected void makeLastAdjustments() {
        pack();
        setResizable(false);
    }

    public void setVisible(boolean state) {
        if (state) {
            populateNetComboBox();
            populateDbGatewayComboBox();
            pack();
        }
        super.setVisible(state);
    }

    private JPanel getStartingNetPanel() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel panel = new JPanel(gbl);
        panel.setBorder(new EmptyBorder(12, 12, 0, 11));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 5);
        gbc.anchor = GridBagConstraints.EAST;

        JLabel label = new JLabel("Execution of the workflow starts in net:");
        label.setDisplayedMnemonicIndex(26);
        panel.add(label, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;

        netComboBox = new JComboBox();
        label.setLabelFor(netComboBox);
        panel.add(netComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;

        label = new JLabel("External data gateway for case data:");
        label.setDisplayedMnemonicIndex(9);
        panel.add(label, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;

        dbGatewayComboBox = new JComboBox();
        label.setLabelFor(dbGatewayComboBox);
        panel.add(dbGatewayComboBox, gbc);

        return panel;
    }

    private void populateNetComboBox() {
        netComboBox.setEnabled(false);
        netComboBox.removeAllItems();

        String rootNetName = null;
        java.util.List<String> netNames = new ArrayList<String>();
        for (NetGraphModel net : SpecificationModel.getInstance().getNets()) {
            netNames.add(net.getName());
            if (net.isRootNet()) rootNetName = net.getName();
        }
        Collections.sort(netNames);

        for (String name : netNames) netComboBox.addItem(name);

        netComboBox.setSelectedItem(rootNetName);
        netComboBox.setEnabled(true);
    }

    private void populateDbGatewayComboBox() {
        dbGatewayComboBox.setEnabled(false);
        dbGatewayComboBox.removeAllItems();
        dbGatewayComboBox.addItem("None");
        try {
            for (String name : YConnector.getExternalDataGateways().keySet()) {
                dbGatewayComboBox.addItem(name);
            }
            dbGatewayComboBox.setEnabled(true);
        } catch (IOException ioe) {
            // leave combo disabled
        }
    }
}
