package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.ui.properties.NetTaskPair;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.TimerDialog;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YTimerParameters;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class TimerPropertyEditor extends DialogPropertyEditor {

    private NetTaskPair pair;


    public TimerPropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return pair;
    }

    public void setValue(Object value) {
        pair = (NetTaskPair) value;
        String simpleText = "None";
        if (pair != null) {
            YTimerParameters timerParameters = getTimerParameters();
            if (timerParameters != null) simpleText = timerParameters.toString();
        }
        ((DefaultCellRenderer) label).setValue(simpleText);
    }


    protected void showDialog() {
        YTimerParameters oldDetail = getTimerParameters();
        TimerDialog dialog = new TimerDialog();
        dialog.setContent(oldDetail, getNet());
        dialog.setVisible(true);
        YTimerParameters newDetail = dialog.getContent();
        if ((newDetail == null && oldDetail != null) ||
           (! (newDetail == null || newDetail.equals(oldDetail)))) {
            ((AtomicTask) pair.getTask()).setTimerParameters(newDetail);
            setValue(pair);
            firePropertyChange(pair, pair);
        }
    }


    private YTimerParameters getTimerParameters() {
        return pair != null ? ((AtomicTask) pair.getTask()).getTimerParameters() : null;
    }

    private YNet getNet() {
        return pair != null ? pair.getNet() : null;
    }

}

