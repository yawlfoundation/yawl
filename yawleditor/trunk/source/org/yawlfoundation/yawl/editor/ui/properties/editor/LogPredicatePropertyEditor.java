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
import org.yawlfoundation.yawl.editor.ui.properties.LogPredicateTransport;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.LogPredicateDialog;

/**
 * @author Michael Adams
 * @date 12/07/12
 */
public class LogPredicatePropertyEditor extends DialogPropertyEditor {

    private LogPredicateTransport transport;

    public LogPredicatePropertyEditor() {
        super(new DefaultCellRenderer());
    }

    public Object getValue() {
        return transport;
    }

    public void setValue(Object value) {
        transport = (LogPredicateTransport) value;
        ((DefaultCellRenderer) label).setValue(transport.toString());
    }


    protected void showDialog() {
        LogPredicateDialog dialog = new LogPredicateDialog(transport);
        dialog.setVisible(true) ;

        if (dialog.isUpdated()) {
            LogPredicateTransport oldTransport = transport;
            transport = oldTransport.newInstance();
            transport.setStartPredicate(dialog.getStartText());
            transport.setCompletionPredicate(dialog.getCompletionText());
            firePropertyChange(oldTransport, transport);
        }
    }

}

