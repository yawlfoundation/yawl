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
        ((DefaultCellRenderer) label).setValue(pair.getSimpleText());
    }


    protected void showDialog() {
        YTimerParameters oldDetail = getTimerParameters();
        TimerDialog dialog = new TimerDialog();
        dialog.setContent(oldDetail, getNet());
        dialog.setVisible(true);

        NetTaskPair oldPair = pair;
        pair = new NetTaskPair(oldPair.getNet(), null, oldPair.getTask());
        YTimerParameters newDetail = dialog.getContent();
        ((AtomicTask) pair.getTask()).setTimerParameters(newDetail);
        pair.setSimpleText(newDetail.toString());
        firePropertyChange(oldPair, pair);
    }


    private YTimerParameters getTimerParameters() {
        return pair != null ? ((AtomicTask) pair.getTask()).getTimerParameters() : null;
    }

    private YNet getNet() {
        return pair != null ? pair.getNet() : null;
    }

}

