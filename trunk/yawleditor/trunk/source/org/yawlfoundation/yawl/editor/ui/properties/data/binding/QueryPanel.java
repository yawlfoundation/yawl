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

import org.yawlfoundation.yawl.editor.ui.data.editorpane.XQueryValidatingEditorPane;
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.BindingTypeValidator;
import org.yawlfoundation.yawl.editor.ui.util.IconList;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 21/11/2013
 */
class QueryPanel extends AbstractBindingPanel {

    private XQueryValidatingEditorPane _xQueryEditor;
    private JToolBar _toolbar;

    QueryPanel() {
        super();
    }


    QueryPanel(String title, ActionListener listener) {
        setLayout(new BorderLayout());
        if (!StringUtil.isNullOrEmpty(title)) {
            setBorder(new TitledBorder(title));
        }
        add(createToolBar(listener), BorderLayout.NORTH);
        add(createXQueryEditor(), BorderLayout.CENTER);
    }


    protected void setTypeValidator(BindingTypeValidator validator) {
        _xQueryEditor.setTypeChecker(validator);
    }

    protected BindingTypeValidator getTypeValidator() {
        return _xQueryEditor.getTypeChecker();
    }

    protected void setTargetVariableName(String name) {
        _xQueryEditor.setTargetVariableName(name);
    }

    protected void setText(String text) {
        _xQueryEditor.setText(formatQuery(text, true));
    }

    protected String getText() { return _xQueryEditor.getText(); }


    protected void setParentDialogOKButton(JButton okButton) {
        _xQueryEditor.setParentDialogOKButton(okButton);
    }

    private XQueryValidatingEditorPane createXQueryEditor() {
        _xQueryEditor = new XQueryValidatingEditorPane();
        _xQueryEditor.setPreferredSize(new Dimension(400, 150));
        _xQueryEditor.setValidating(true);
        return _xQueryEditor;
    }


    private JPanel createToolBar(ActionListener listener) {
        JPanel content = new JPanel(new BorderLayout());
        _toolbar = new JToolBar();
        _toolbar.setBorder(null);
        _toolbar.setFloatable(false);
        _toolbar.setRollover(true);
        _toolbar.add(createToolBarButton("insertMapping", "insertBinding",
                " Generate and insert binding ", listener));
        _toolbar.add(createToolBarButton("arrow-repeat", "resetBinding",
                " Reset binding to original ", listener));
        _toolbar.add(createFormatButton());
        content.add(_toolbar, BorderLayout.EAST);
        return content;
    }


    private JButton createFormatButton() {
        JButton btnFormat = new JButton(IconList.getInstance().getIcon("autoformat.png"));
        btnFormat.setToolTipText(" Auto-format binding ");
        btnFormat.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = _xQueryEditor.getText();
                _xQueryEditor.setText(formatQuery(text, true));
            }
        });
        return btnFormat;
    }


    private JButton createToolBarButton(String iconName, String action, String tip,
                                        ActionListener listener) {
        JButton button = new JButton(getIcon(iconName));
        button.setActionCommand(action);
        button.setToolTipText(tip);
        button.addActionListener(listener);
        return button;
    }


    private ImageIcon getIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(
                "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/" +
                        iconName + ".png");
    }

}
