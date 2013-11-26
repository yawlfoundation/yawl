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

package org.yawlfoundation.yawl.editor.ui.properties.data.binding;

import org.yawlfoundation.yawl.editor.ui.data.editorpane.XQueryEditorPane;

import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * @author Michael Adams
 * @date 21/11/2013
 */
class MIQueryPanel extends AbstractBindingPanel {

    private XQueryEditorPane _miQueryEditor;

    MIQueryPanel(String title) {
        super();
        setBorder(new TitledBorder(title));
        add(createMiQueryEditor());
    }

    protected void setTargetVariableName(String name) {
        _miQueryEditor.setTargetVariableName(name);
    }

    protected void setText(String text) {
        _miQueryEditor.setText(formatQuery(text, true));
    }

    protected String getText() { return _miQueryEditor.getXQueryEditor().getText(); }


    private XQueryEditorPane createMiQueryEditor() {
        _miQueryEditor = new XQueryEditorPane();
        _miQueryEditor.setPreferredSize(new Dimension(400, 90));
        _miQueryEditor.setValidating(true);
        return _miQueryEditor;
    }

}
