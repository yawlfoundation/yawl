package org.yawlfoundation.yawl.editor.ui.properties.dialog.component;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.properties.data.StatusPanel;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.FlowConditionDialog;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.FlowPredicateDialog;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Adams
 * @date 9/08/13
 */
public class FlowConditionTablePanel extends JPanel
        implements ActionListener, ListSelectionListener {

    private FlowConditionTable table;
    private JToolBar toolbar;
    private Map<YAWLFlowRelation, Color> origFlowColours;
    private NetGraph _graph;
    private FlowConditionDialog _parent;

    // toolbar buttons
    private JButton btnUp;
    private JButton btnDown;
    private JButton btnMapping;
    private StatusPanel status;

    private static final String iconPath =
            "/org/yawlfoundation/yawl/editor/ui/resources/miscicons/";


    public FlowConditionTablePanel(FlowConditionDialog parent, YAWLTask task,
                                   NetGraph graph) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(5,5,0,5));
        _graph = graph;
        _parent = parent;
        List<YAWLFlowRelation> rows = new ArrayList<YAWLFlowRelation>(
                task.getOutgoingFlows());
        saveOriginalFlowColours(rows);
        add(createToolBar(parent), BorderLayout.SOUTH);
        JScrollPane scrollPane = new JScrollPane(createTable(rows));
        scrollPane.setPreferredSize(new Dimension(400, 180));
        add(scrollPane, BorderLayout.CENTER);
        enableButtons(true);
        setStatus();
    }


    public FlowConditionTable getTable() { return table; }


    public void showErrorStatus(String msg, java.util.List<String> more) {
        status.set("    " + msg, StatusPanel.ERROR, more);
    }


    public void showOKStatus(String msg, java.util.List<String> more) {
        status.set("    " + msg, StatusPanel.OK, more);
    }


    public void clearStatus() {
        status.clear();
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("Up")) {
            table.moveSelectedRowUp();
        }
        else if (action.equals("Down")) {
            table.moveSelectedRowDown();
        }
        else if (action.equals("Map")) {
            new FlowPredicateDialog(_parent, getTable().getSelectedFlow()).setVisible(true);
            table.getTableModel().fireTableDataChanged();
        }
    }


    public void valueChanged(ListSelectionEvent e) {
        if (! e.getValueIsAdjusting()) {        // if the mouse button has been released
            int row = table.getSelectedRow();
            btnUp.setEnabled(row > 0);
            btnDown.setEnabled(row > -1 && row < table.getRowCount() - 1);
            enableMappingButton(row);
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


    private JTable createTable(java.util.List<YAWLFlowRelation> rows) {
        table = new FlowConditionTable(new FlowConditionTableModel());
        table.setFlows(rows);
        table.getSelectionModel().addListSelectionListener(this);

//        VariableRowStringEditor stringEditor = new VariableRowStringEditor(this);
//        table.setDefaultEditor(String.class, stringEditor);
//        VariableRowStringRenderer stringRenderer = new VariableRowStringRenderer();
//        table.setDefaultRenderer(String.class, stringRenderer);
        if (table.getRowCount() > 0) table.selectRow(0);
        return table;
    }


    private JToolBar createToolBar(Window parent) {
        toolbar = new JToolBar();
        toolbar.setBorder(null);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        btnUp = createToolBarButton("arrow_up", "Up", " Move up ");
        toolbar.add(btnUp);
        btnDown = createToolBarButton("arrow_down", "Down", " Move down ");
        toolbar.add(btnDown);
        btnMapping = createToolBarButton("mapping", "Map", " Edit Condition ");
        toolbar.add(btnMapping);
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

    private void enableMappingButton(int selectedRow) {
        if (selectedRow < 0) {
            btnMapping.setEnabled(false);
        }
        else {
            boolean enabled = table.allowPredicateEdit();
            btnMapping.setEnabled(enabled);
            btnMapping.setToolTipText(enabled ? " Edit Condition " :
                " Condition of XOR-split default flow can't be edited ");
        }
    }

    private ImageIcon getIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(iconPath + iconName + ".png");
    }


    protected void enableButtons(boolean enable) {
        boolean hasRowSelected = table.getSelectedRow() > -1;
        btnUp.setEnabled(enable && hasRowSelected);
        btnDown.setEnabled(enable && hasRowSelected);
        btnMapping.setEnabled(enable && hasRowSelected);
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
