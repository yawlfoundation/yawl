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
import org.yawlfoundation.yawl.worklet.rdr.RdrPrimitive;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * @author Michael Adams
 * @date 30/09/2014
 */
public class ConclusionTablePanel extends JPanel implements ActionListener {

    private ConclusionTable table;
    private MiniToolBar toolbar;

    private JButton btnAdd;
    private JButton btnDel;
    private JButton btnGraphical;


    public ConclusionTablePanel() {
        super();
        setLayout(new BorderLayout());
        setBorder(new TitledBorder("Actions"));
        JScrollPane scrollPane = new JScrollPane(createTable());
        scrollPane.setSize(new Dimension(600, 200));
        add(scrollPane, BorderLayout.CENTER);
        add(populateToolBar(), BorderLayout.SOUTH);
        setConclusion(new ArrayList<RdrPrimitive>());
    }


    public void setConclusion(java.util.List<RdrPrimitive> primitives) {
        table.setConclusion(primitives);
        table.setPreferredScrollableViewportSize(getPreferredSize());
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("Add")) {
            table.addRow();
        }
        else if (action.equals("Del")) {
            table.removeRow();
        }
        else if (action.equals("Graphical")) {

        }
    }


    private ConclusionTable createTable() {
        table = new ConclusionTable();
        return table;
    }


    private JToolBar populateToolBar() {
        toolbar = new MiniToolBar(this);
        btnAdd = toolbar.addButton("plus", "Add", " Add ");
        btnDel = toolbar.addButton("minus", "Del", " Remove ");
        btnGraphical = toolbar.addButton("arrow_up", "Graphical", " Show graphical editor ");
        return toolbar;
    }


}
