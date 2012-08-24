package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.elements.model.TaskTimeoutDetail;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.TimerDialog;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class TimerPropertyEditor extends DialogPropertyEditor {

    private TaskTimeoutDetail currentDetail;


    public TimerPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return currentDetail;
    }

    public void setValue(Object value) {
        currentDetail = (TaskTimeoutDetail) value;
        String simpleText = currentDetail != null ? currentDetail.toString() : "None";
        ((DefaultCellRenderer) label).setValue(simpleText);
    }


    protected void showDialog() {
        TimerDialog dialog = new TimerDialog();
        dialog.setContent(currentDetail);
        dialog.setVisible(true);
        TaskTimeoutDetail newDetail = dialog.getContent();
        if ((newDetail == null && currentDetail != null) ||
           (! (newDetail == null || newDetail.equals(currentDetail)))) {
            TaskTimeoutDetail oldDetail = currentDetail;
            setValue(newDetail);
            firePropertyChange(oldDetail, newDetail);
        }
    }

}

