package org.yawlfoundation.yawl.editor.ui.actions.element;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractOrderedTablePanel;
import org.yawlfoundation.yawl.editor.ui.swing.data.FlowPriorityTable;
import org.yawlfoundation.yawl.editor.ui.swing.element.AbstractTaskDoneDialog;

import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.*;

/**
 * @author Michael Adams
 * @date 23/07/12
 */
class FlowDetailTablePanel extends AbstractOrderedTablePanel {

    private FlowPriorityTable flowTable;

    private NetGraph graph;
    private Map<YAWLFlowRelation, Color> flowColours;             // previous flow colour

    public FlowDetailTablePanel(AbstractTaskDoneDialog parent) {
        super();
        flowTable = new FlowPriorityTable();
        setOrderedTable(flowTable);
        flowTable.setParentWindow(parent);
        setPreferredSize(new Dimension(450, 112));
    }

    public void setTaskAndNet(YAWLTask task, NetGraph net) {
        graph = net;
        flowTable.setTaskAndNet(task, net);

        // don't show alternate row colours if there's less than 5 rows
        if (task.getSplitDecorator().getFlowCount() < 5)
            getOrderedTable().setOddRowColor(Color.WHITE);
        else
            getOrderedTable().setOddRowColor(Color.decode("0xFAEBD7"));  //default odd row colour

        rememberOriginalFlowColours(task);
    }


    public void updatePredicateOfSelectedFlow() {
        flowTable.updatePredicateOfSelectedFlow();
    }

    public void selectFlowAtRow(int rowNumber) {
        flowTable.selectRow(rowNumber);
    }

    public YAWLFlowRelation getSelectedFlow() {
        return flowTable.getSelectedFlow();
    }

    public boolean hasFlows() {
        return (flowTable.getRowCount() > 0);
    }

    public java.util.List<YAWLFlowRelation> getAllFlows() {
        return  flowTable.getFlowModel().getOrderedRows();
    }

    public void setFlowColours(Map<YAWLFlowRelation, Color> colours) {
        flowColours = colours;
    }

    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;  // The mouse button has not yet been released
        }

        super.valueChanged(e);
        colorFlowOfSelectedRow();
    }

    private void colorFlowOfSelectedRow() {
        for(YAWLFlowRelation flow: getAllFlows()) {
            if (getSelectedFlow() == flow) {
                colorSelectedFlow(Color.GREEN.darker());
            } else {
                colorFlow(flow, flowColours.get(flow));
            }
        }
    }

    public void colorSelectedFlow(Color color) {
        colorFlow(getSelectedFlow(), color);
    }

    private void colorFlow(YAWLFlowRelation flow, Color color) {
        graph.stopUndoableEdits();
        graph.changeCellForeground(flow, color);
        graph.startUndoableEdits();
    }

    private void rememberOriginalFlowColours(YAWLTask task) {
        SortedSet flows = task.getSplitDecorator().getFlowsInPriorityOrder();
        flowColours = new HashMap<YAWLFlowRelation, Color>();
        for (Object obj: flows) {
            YAWLFlowRelation flow = (YAWLFlowRelation) obj;
            Color origColor = graph.getCellForeground(flow);
            if (origColor == null) origColor = Color.BLACK;
            flowColours.put(flow, origColor);
        }
    }

}
