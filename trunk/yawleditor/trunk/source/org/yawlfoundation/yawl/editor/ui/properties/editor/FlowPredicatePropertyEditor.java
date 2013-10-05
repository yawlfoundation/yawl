package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.FlowPredicateDialog;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class FlowPredicatePropertyEditor extends DialogPropertyEditor {

    private YAWLFlowRelation flow;
    private String currentPredicate;

    public FlowPredicatePropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return flow;
    }

    public void setValue(Object value) {
        flow = (YAWLFlowRelation) value;
        currentPredicate = flow.getPredicate();
        ((DefaultCellRenderer) label).setValue(currentPredicate);
    }


    protected void showDialog() {
        FlowPredicateDialog dialog = new FlowPredicateDialog(
                YAWLEditor.getInstance(), flow);
        dialog.setVisible(true);
        String newPredicate = flow.getPredicate();
        if (! (newPredicate == null || newPredicate.equals(currentPredicate))) {
            firePropertyChange(flow, flow);
        }
    }

}

