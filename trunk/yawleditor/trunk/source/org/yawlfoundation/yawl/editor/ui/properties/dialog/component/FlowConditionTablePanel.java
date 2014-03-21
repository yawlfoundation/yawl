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

package org.yawlfoundation.yawl.editor.ui.properties.dialog.component;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.properties.data.StatusPanel;
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.BindingTypeValidator;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.FlowConditionDialog;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * @author Michael Adams
 * @date 9/08/13
 */
public class FlowConditionTablePanel extends JPanel
        implements ActionListener, ListSelectionListener {

    private FlowConditionTable table;
    private Map<YAWLFlowRelation, Color> origFlowColours;
    private final NetGraph _graph;
    private final FlowConditionDialog _parent;
    private final BindingTypeValidator _conditionValidator;
    private final Set<Integer> _invalidRows;

    // toolbar buttons
    private JButton btnUp;
    private JButton btnDown;
    private StatusPanel status;

    private static final String iconPath =
            "/org/yawlfoundation/yawl/editor/ui/resources/miscicons/";


    public FlowConditionTablePanel(FlowConditionDialog parent, YAWLTask task,
                                   NetGraph graph) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(5,5,0,5));
        _graph = graph;
        _parent = parent;
        _conditionValidator = new BindingTypeValidator(
                        YAWLEditor.getNetsPane().getSelectedYNet());
        _invalidRows = new HashSet<Integer>();
        List<YAWLFlowRelation> rows = new ArrayList<YAWLFlowRelation>(
                task.getOutgoingFlows());
        Collections.sort(rows);
        saveOriginalFlowColours(rows);
        add(createToolBar(parent), BorderLayout.SOUTH);
        JScrollPane scrollPane = new JScrollPane(createTable(rows));
        scrollPane.setPreferredSize(new Dimension(400, 180));
        add(scrollPane, BorderLayout.CENTER);
        enableButtons();
        setStatus();
    }


    public FlowConditionTable getTable() { return table; }


    public void showErrorStatus(String msg, java.util.List<String> more) {
        status.set("    " + msg, StatusPanel.ERROR, more);
        _invalidRows.add(table.getSelectedRow());
        _parent.enableOK(false);
    }


    public void showOKStatus() {
        setStatus();
        _invalidRows.remove(table.getSelectedRow());
        _parent.enableOK(_invalidRows.isEmpty());
    }


    public boolean isValidRow(int row) {
        return ! _invalidRows.contains(row);
    }

    public void clearStatus() {
        status.clear();
    }

    public FlowConditionDialog getParentDialog() { return _parent; }

    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("Up")) {
            table.moveSelectedRowUp();
        }
        else if (action.equals("Down")) {
            table.moveSelectedRowDown();
        }
    }


    public void valueChanged(ListSelectionEvent e) {
        if (! e.getValueIsAdjusting()) {        // if the mouse button has been released
            int row = table.getSelectedRow();
            btnUp.setEnabled(row > 0);
            btnDown.setEnabled(row > -1 && row < table.getRowCount() - 1);
            highlightFlowOfSelectedRow();
        }
    }


    public YAWLFlowRelation getFlowAtRow(int row) {
        return table.getFlows().get(row);
    }

    public void resetFlowColours() {
        setFlowColours(null, null);
        table.getTableModel().cleanupFlows();
    }

    public void stopEdits() {
        TableCellEditor editor = table.getCellEditor();
        if (editor != null) editor.stopCellEditing();
    }


    private JTable createTable(java.util.List<YAWLFlowRelation> rows) {
        table = new FlowConditionTable(new FlowConditionTableModel());
        table.setFlows(rows);
        table.getSelectionModel().addListSelectionListener(this);
        FlowConditionEditor conditionEditor = new FlowConditionEditor(
                this, _conditionValidator);
        FlowConditionRenderer conditionRenderer = new FlowConditionRenderer(this);
        table.setDefaultEditor(String.class, conditionEditor);
        table.setDefaultRenderer(String.class, conditionRenderer);
        if (table.getRowCount() > 0) table.selectRow(0);
        return table;
    }


    private JToolBar createToolBar(Window parent) {
        JToolBar toolbar = new JToolBar();
        toolbar.setBorder(null);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        btnUp = createToolBarButton("arrow_up", "Up", " Move up ");
        toolbar.add(btnUp);
        btnDown = createToolBarButton("arrow_down", "Down", " Move down ");
        toolbar.add(btnDown);
        status = new StatusPanel(parent);
        toolbar.add(status);
        return toolbar;
    }


    private JButton createToolBarButton(String iconName, String action, String tip) {
        JButton button = new JButton(getIcon(iconName));
        button.setActionCommand(action);
        button.setToolTipText(tip);
        button.addActionListener(this);
        return button;
    }


    private ImageIcon getIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(iconPath + iconName + ".png");
    }


    protected void enableButtons() {
        boolean hasRowSelected = table.getSelectedRow() > -1;
        btnUp.setEnabled(hasRowSelected);
        btnDown.setEnabled(hasRowSelected);
    }


    private void saveOriginalFlowColours(java.util.List<YAWLFlowRelation> flows) {
        origFlowColours = new HashMap<YAWLFlowRelation, Color>();
        for (YAWLFlowRelation flow: flows) {
            Color origColor = _graph.getCellForeground(flow);
            if (origColor == null) origColor = Color.BLACK;
            origFlowColours.put(flow, origColor);
        }
    }


    private void highlightFlowOfSelectedRow() {
        YAWLFlowRelation selectedFlow = table.getSelectedFlow();
        if (selectedFlow != null) {
            setFlowColours(selectedFlow, Color.GREEN.darker());
        }
    }


    private void setFlowColours(YAWLFlowRelation selectedFlow, Color colour) {
        _graph.stopUndoableEdits();
        for (YAWLFlowRelation flow: table.getFlows()) {
            Color flowColour = flow == selectedFlow ? colour : origFlowColours.get(flow);
            if (flowColour == null) flowColour = Color.BLACK;
            _graph.changeCellForeground(flow, flowColour);
        }
        _graph.startUndoableEdits();
    }

    private void setStatus() {
        status.set(
           "<html><i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
                   "The bottom-most flow is the default</i></html>",
                Color.DARK_GRAY);
    }

}
