/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.ui.properties.data.binding;

import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * @author Michael Adams
 * @date 21/11/2013
 */
class NetVariablePanel extends AbstractBindingPanel implements ActionListener {

    private JComboBox _varsCombo;
    private JComboBox _gatewayCombo;
    private JRadioButton _netVarsButton;
    private JRadioButton _gatewayButton;


    NetVariablePanel(String title, java.util.List<VariableRow> varList,
                     ActionListener listener) {
        super();
        setLayout(new BorderLayout());
        if (!StringUtil.isNullOrEmpty(title)) {
            setBorder(new TitledBorder(title));
        }
        add(buildNetSelectionPanel(varList, listener), BorderLayout.CENTER);
        add(buildRadioPanel(listener), BorderLayout.WEST);
        setPreferredSize(new Dimension(410, 90));
        initContent();
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("netVarRadio")) {
            enableCombos(true, false);
        }
        else if (action.equals("gatewayRadio")) {
            enableCombos(false, true);
        }
    }


    public String getSelectedVariableName() {
        return _netVarsButton.isSelected() ? (String) _varsCombo.getSelectedItem()
                : null;
    }

    public String getSelectedDataGateway() {
        return _gatewayButton.isSelected() ? (String) _gatewayCombo.getSelectedItem()
                : null;
    }

    public String getFirstDataGateway() {
        return _gatewayCombo.getItemCount() > 0 ?
                (String) _gatewayCombo.getItemAt(0) : null;
    }


    protected void initContent() {
        if (_varsCombo.getItemCount() == 0) {
            _netVarsButton.setEnabled(false);
            _varsCombo.setEnabled(false);
        }
        if (_gatewayCombo.getItemCount() == 0) {
            _gatewayButton.setEnabled(false);
            _gatewayCombo.setEnabled(false);
        }
    }


    protected void setSelectedItem(String item) {
        if (item == null) {
            if (_varsCombo.getItemCount() > 0) {
                _netVarsButton.setSelected(true);
            }
            else if (_gatewayCombo.getItemCount() > 0) {
                _gatewayButton.setSelected(true);
            }
        }
        else if (! initExternalSelection(item)) {
            enableCombos(true, false);
            _netVarsButton.setSelected(true);
            _varsCombo.setSelectedItem(item);
            if (_varsCombo.getItemCount() > 0 && _varsCombo.getSelectedIndex() < 0) {
                _varsCombo.setSelectedIndex(0);
            }
        }
    }


    private JPanel buildRadioPanel(ActionListener listener) {
        ButtonGroup buttonGroup = new ButtonGroup();
        JPanel radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.setBorder(new EmptyBorder(0,10,0,10));

        _netVarsButton = new JRadioButton("Net Variable: ");
        _netVarsButton.setMnemonic(KeyEvent.VK_N);
        _netVarsButton.setActionCommand("netVarRadio");
        _netVarsButton.addActionListener(this);
        _netVarsButton.addActionListener(listener);
        buttonGroup.add(_netVarsButton);
        radioPanel.add(_netVarsButton);

        _gatewayButton = new JRadioButton("Data Gateway: ");
        _gatewayButton.setMnemonic(KeyEvent.VK_D);
        _gatewayButton.setActionCommand("gatewayRadio");
        _gatewayButton.addActionListener(this);
        _gatewayButton.addActionListener(listener);
        buttonGroup.add(_gatewayButton);
        radioPanel.add(_gatewayButton);

        return radioPanel;
    }


    private JPanel buildNetSelectionPanel(java.util.List<VariableRow> netVarList,
                                          ActionListener listener) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10));
        panel.setBorder(new EmptyBorder(3,0,3,0));
        _varsCombo = buildComboBox(getVarNames(netVarList),
                "netVarComboSelection", listener);
        panel.add(_varsCombo);
        _gatewayCombo = buildComboBox(getDataGatewayNames(),
                "gatewayComboSelection", listener);
        panel.add(_gatewayCombo);
        return panel;
    }


    private boolean initExternalSelection(String mapping) {
        if (mapping == null || ! mapping.contains("#external:")) return false;
        int first = mapping.indexOf(':');
        int last = mapping.lastIndexOf(':');
        if (first < 0 || last < 0) return false;
        String gatewayName = mapping.substring(first + 1, last);
        for (int i = 0; i < _gatewayCombo.getItemCount(); i++) {
            if (gatewayName.equals(_gatewayCombo.getItemAt(i))) {
                enableCombos(false, true);
                _gatewayCombo.setSelectedIndex(i);
                _gatewayButton.setSelected(true);
                return true;
            }
        }
        return false;          // gateway not in list
    }


    private void enableCombos(boolean enableNetVarsCombo, boolean enableGatewayCombo) {
        _varsCombo.setEnabled(enableNetVarsCombo);
        _gatewayCombo.setEnabled(enableGatewayCombo);
    }

    // when showing the MI parameter
    protected void disableSelections() {
        _netVarsButton.setEnabled(false);
        _gatewayButton.setEnabled(false);
        _varsCombo.setEnabled(false);
        _gatewayCombo.setEnabled(false);
    }

}
