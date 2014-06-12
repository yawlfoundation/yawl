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

import org.yawlfoundation.yawl.editor.core.data.YDataHandlerException;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.XQueryEditorPane;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.XQueryValidatingEditorPane;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.BindingTypeValidator;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;
import org.yawlfoundation.yawl.elements.YNet;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;

/**
 * Author: Michael Adams
 * Creation Date: 8/04/2010
 */
public class FlowPredicateDialog extends PropertyDialog implements ActionListener {

    private XQueryValidatingEditorPane _xQueryEditor;
    private final YAWLFlowRelation _flow;


    public FlowPredicateDialog(Window parent, YAWLFlowRelation flow) {
        super(parent, false);
        _flow = flow;
        setTitle(makeTitle());
        add(getContent());
        setPreferredSize(new Dimension(420, 290));
        getOKButton().setEnabled(true);
        pack();
    }

    public void setVisible(boolean visible) {
        if (visible) _xQueryEditor.requestFocus();
        super.setVisible(visible);
    }


    public void setTypeValidator(BindingTypeValidator validator) {
        _xQueryEditor.setTypeChecker(validator, true);
    }


    public void setText(String text) {
        _xQueryEditor.setText(formatQuery(text, true));
    }


    public String getText() { return _flow.getPredicate(); }


    protected JPanel getContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(12,7,7,7));
        content.add(createHeadPanel(), BorderLayout.NORTH);
        content.add(createXQueryPane(), BorderLayout.CENTER);
        content.add(getButtonBar(this), BorderLayout.SOUTH);
        _xQueryEditor.setParentDialogOKButton(getOKButton());
        return content;
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getActionCommand().equals("OK")) {
            _flow.setPredicate(_xQueryEditor.getText());
        }
        setVisible(false);
    }


    private String makeTitle() {
        return String.format("Predicate for Flow '%s->%s'", _flow.getSourceID(),
                _flow.getTargetID());
    }


    private JPanel createHeadPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(0,0,10,0));
        panel.add(createVarList(), BorderLayout.WEST);
        return panel;
    }

    private XQueryEditorPane createXQueryPane() {
        _xQueryEditor = new XQueryValidatingEditorPane();
        _xQueryEditor.setPreferredSize(new Dimension(400, 150));
        _xQueryEditor.setValidating(true);
        _xQueryEditor.setPreAndPostEditorText("<foo_bar>", "</foo_bar>");
        _xQueryEditor.setText(formatQuery(_flow.getPredicate(), true));
        return _xQueryEditor;
    }


    private String formatQuery(String query, boolean prettify) {
        return XMLUtilities.formatXML(query, prettify, true);
    }


    private JPanel createVarList() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Net Variables: "), BorderLayout.WEST);
        JComboBox combo = new JComboBox(getComboItems());
        combo.setEnabled(combo.getItemCount() > 0);
        panel.setPreferredSize(new Dimension(400, 26));
        panel.add(combo, BorderLayout.CENTER);
        panel.add(createToolBar(combo), BorderLayout.EAST);
        return panel;
    }


    private Vector<String> getComboItems() {
        YNet net = _flow.getYFlow().getSource().getNet();
        Vector<String> varIDs = new Vector<String>();
        varIDs.addAll(net.getInputParameterNames());
        varIDs.addAll(net.getLocalVariables().keySet());
        Collections.sort(varIDs);
        return varIDs;
    }


    private JPanel createToolBar(final JComboBox combo) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(5,20,0,0));
        JButton button = new JButton(getToolbarIcon("generate"));
        button.setToolTipText(" Insert variable query ");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String varID = (String) combo.getSelectedItem();
                String xQuery = createXQuery(varID);
                _xQueryEditor.setText(xQuery);
            }
        });
        button.setEnabled(combo.getItemCount() > 0);
        JToolBar toolbar = new JToolBar();
        toolbar.setBorder(null);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.add(button);
        content.add(toolbar, BorderLayout.EAST);
        return content;
    }


    private String createXQuery(String varID) {
        YNet net = _flow.getYFlow().getSource().getNet();
        StringBuilder s = new StringBuilder("/");
        s.append(net.getID())
         .append("/")
         .append(varID)
         .append("/")
         .append(getXQuerySuffix(net.getLocalOrInputVariable(varID).getDataTypeName()));
        return s.toString();
    }


    private String getXQuerySuffix(String dataType) {
        try {
            return SpecificationModel.getHandler().getDataHandler().getXQuerySuffix(
                             dataType);
        }
        catch (YDataHandlerException ydhe) {
            return "";
        }
    }

}
