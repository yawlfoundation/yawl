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

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

/**
 * @author Michael Adams
 * @date 21/11/2013
 */
class AbstractBindingPanel extends JPanel {

    private JButton _btnOK;

    AbstractBindingPanel() { super(); }


    protected JButton getOKButton() { return _btnOK; }

    protected Vector<String> getVarNames(java.util.List<VariableRow> varList) {
        Vector<String> names = new Vector<String>();
        for (VariableRow row : varList) {
            names.add(row.getName());
        }
        return names;
    }


    protected Vector<String> getDataGatewayNames() {
        try {
            return new Vector<String>(YConnector.getExternalDataGateways().keySet());
        }
        catch (IOException ioe) {
            return new Vector<String>();
        }
    }


    protected JComboBox buildComboBox(Vector<String> items, String action,
                                    ActionListener listener) {
        JComboBox comboBox = new JComboBox(items);
        comboBox.setPreferredSize(new Dimension(250, 22));
        comboBox.addActionListener(listener);
        comboBox.setActionCommand(action);
        return comboBox;
    }


    protected String formatQuery(String query) {
        return XMLUtilities.formatXML(query, true, true);
    }


}
