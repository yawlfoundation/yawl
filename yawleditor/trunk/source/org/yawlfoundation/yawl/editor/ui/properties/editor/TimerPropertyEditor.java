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

package org.yawlfoundation.yawl.editor.ui.properties.editor;

import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.elements.model.AtomicTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.properties.NetTaskPair;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.TimerDialog;
import org.yawlfoundation.yawl.elements.YDecomposition;
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
        ((DefaultCellRenderer) label).setValue(pair.getSimpleText());
    }


    protected void showDialog() {
        YTimerParameters oldParameters = getTimerParameters();
        TimerDialog dialog = new TimerDialog();
        dialog.setContent(oldParameters, getNet(), isAutoTask());
        dialog.setTitle(getTitle());
        dialog.setVisible(true);

        YTimerParameters newDetail = dialog.getContent();
        if (newDetail != null) {                               // dialog not cancelled
            NetTaskPair oldPair = pair;
            if (oldPair.hasMultipleTasks()) {
                pair = new NetTaskPair(oldPair.getNet(), oldPair.getVertexSet());
                for (YAWLVertex vertex : pair.getVertexSet()) {
                    ((AtomicTask) vertex).setTimerParameters(newDetail);
                }
            }
            else {
                pair = new NetTaskPair(oldPair.getNet(), null, oldPair.getTask());
                ((AtomicTask) pair.getTask()).setTimerParameters(newDetail);
            }
            pair.setSimpleText(newDetail.toString());
            firePropertyChange(oldPair, pair);
        }
    }


    private YTimerParameters getTimerParameters() {
        if (pair == null) return null;
        if (pair.hasMultipleTasks()) {
            YTimerParameters parameters = null;
            for (YAWLVertex vertex : pair.getVertexSet()) {
                YTimerParameters theseParameters = ((AtomicTask) vertex).getTimerParameters();
                if (theseParameters == null) return null;
                if (parameters == null) parameters = theseParameters;
                else if (! parameters.toString().equals(theseParameters.toString())) {
                    return null;
                }
            }
            return parameters;
        }

        return ((AtomicTask) pair.getTask()).getTimerParameters();
    }


    private String getTitle() {
        return pair.hasMultipleTasks() ? "Set Timers for Multiple Tasks" :
                "Set Timer for Task " + pair.getTask().getID();
    }


    private YNet getNet() {
        return pair != null ? pair.getNet() : null;
    }


    private boolean isAutoTask() {
        if (pair != null) {
            YDecomposition decomposition = pair.getTask().getDecomposition();
            return ! (decomposition == null || decomposition.requiresResourcingDecisions());
        }
        return false;
    }

}

