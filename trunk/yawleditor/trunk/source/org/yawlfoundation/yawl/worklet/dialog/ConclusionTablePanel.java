/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

/*
 * Copyright (c) 2004-2014 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.dialog;

import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.MiniToolBar;
import org.yawlfoundation.yawl.worklet.client.WorkletClient;
import org.yawlfoundation.yawl.worklet.rdr.RdrConclusion;
import org.yawlfoundation.yawl.worklet.rdr.RdrPrimitive;
import org.yawlfoundation.yawl.worklet.support.ConclusionValidator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Michael Adams
 * @date 30/09/2014
 */
public class ConclusionTablePanel extends JPanel implements ActionListener {

    private ConclusionTable table;
    private MiniToolBar toolbar;
    private JTextArea _txtStatus;


    public ConclusionTablePanel(JComboBox cbxType, JTextArea statusArea) {
        super();
        _txtStatus = statusArea;
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Actions"));
        table = new ConclusionTable(cbxType);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setSize(new Dimension(600, 200));
        add(scrollPane, BorderLayout.CENTER);
        add(populateToolBar(), BorderLayout.SOUTH);
        setConclusion(new ArrayList<RdrPrimitive>());
    }


    public void setConclusion(java.util.List<RdrPrimitive> primitives) {
        table.setConclusion(primitives);
        table.setPreferredScrollableViewportSize(getPreferredSize());
    }


    public RdrConclusion getConclusion() {
        return table.getConclusion();
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("Add")) {
            table.addRow();
        }
        else if (action.equals("Del")) {
            table.removeRow();
        }
        else if (action.equals("Validate")) {
            validateConclusion();
        }
    }


    private void validateConclusion() {
        java.util.List<String> errors = new ConclusionValidator().validate(
                table.getConclusion(), getWorkletList());
        updateStatus("==== Action Set Validation ====");
        if (errors.isEmpty()) {
            updateStatus("OK");
        }
        else for (String msg : errors) {
            updateStatus(msg);
        }
    }


    private void updateStatus(String msg) {
        String currentText = _txtStatus.getText();
        if (! currentText.isEmpty()) currentText += '\n';
        _txtStatus.setText(currentText + msg);
    }


    private java.util.List<String> getWorkletList() {
        try {
            return new WorkletClient().getWorkletList();
        }
        catch (IOException ioe) {
            return Collections.emptyList();
        }
    }


    private JToolBar populateToolBar() {
        toolbar = new MiniToolBar(this);
        toolbar.addButton("plus", "Add", " Add ");
        toolbar.addButton("minus", "Del", " Remove ");
        toolbar.addButton("validate", "Validate", " Validate ");
        return toolbar;
    }

}
