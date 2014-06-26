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

package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.LogPredicateTransport;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.LogPredicatePanel;
import org.yawlfoundation.yawl.logging.YLogPredicate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Author: Michael Adams
 * Creation Date: 8/04/2010
 */
public class LogPredicateDialog extends PropertyDialog implements ActionListener {

    private LogPredicatePanel _onStartPanel;
    private LogPredicatePanel _onCompletionPanel;
    private boolean _updated;

    private LogPredicateDialog() {
        super(YAWLEditor.getInstance());
        add(getContent());
        setPreferredSize(new Dimension(400, 350));
        getOKButton().setEnabled(true);
        _onStartPanel.setFocus();
        pack();
    }

    public LogPredicateDialog(LogPredicateTransport transport) {
        this();
        setTitle("Log Entries for " + transport.getTitle());
        _onStartPanel.setText(transport.getStartPredicate());
        _onCompletionPanel.setText(transport.getCompletionPredicate());
    }

    public LogPredicateDialog(VariableRow row) {
        this();
        setTitle("Log Entries for " + row.getName());
        YLogPredicate predicate = row.getLogPredicate();
        if (predicate != null) {
            _onStartPanel.setText(predicate.getStartPredicate());
            _onCompletionPanel.setText(predicate.getCompletionPredicate());
        }
    }


    protected JPanel getContent() {
        JPanel content = new JPanel();
        _onStartPanel = new LogPredicatePanel("On Start");
        _onCompletionPanel = new LogPredicatePanel("On Completion");
        content.add(_onStartPanel);
        content.add(_onCompletionPanel);
        content.add(getButtonBar(this));
        return content;
    }


    public String getStartText() {
        String text = _onStartPanel.getText();
        return text.isEmpty() ? null : text;
    }

    public String getCompletionText() {
        String text = _onCompletionPanel.getText();
        return text.isEmpty() ? null : text;
    }


    public YLogPredicate getUpdatedPredicate() {
        YLogPredicate predicate = new YLogPredicate();
        predicate.setStartPredicate(getStartText());
        predicate.setCompletionPredicate(getCompletionText());
        return predicate;
    }

    public boolean isUpdated() { return _updated; }


    public void actionPerformed(ActionEvent event) {
        _updated = event.getActionCommand().equals("OK");
        setVisible(false);
    }

}
