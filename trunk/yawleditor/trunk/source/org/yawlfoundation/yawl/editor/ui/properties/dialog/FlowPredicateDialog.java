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

import org.yawlfoundation.yawl.editor.ui.data.editorpane.XQueryEditorPane;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
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

    private XQueryEditorPane _xQueryEditor;
    private YAWLFlowRelation _flow;

    public FlowPredicateDialog(Window parent, YAWLFlowRelation flow) {
        super(parent, false);
        _flow = flow;
        setTitle(makeTitle());
        add(getContent());
        setPreferredSize(new Dimension(420, 290));
        getOKButton().setEnabled(true);
        pack();
    }


    protected JPanel getContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(7,7,7,7));
        content.add(createHeadPanel(), BorderLayout.NORTH);
        content.add(createXQueryPane(), BorderLayout.CENTER);
        content.add(getButtonBar(this), BorderLayout.SOUTH);
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
        panel.add(createVarList(), BorderLayout.WEST);
        return panel;
    }

    private XQueryEditorPane createXQueryPane() {
        _xQueryEditor = new XQueryEditorPane();
        _xQueryEditor.setPreferredSize(new Dimension(400, 150));
        _xQueryEditor.setValidating(true);
        _xQueryEditor.setText(formatQuery(_flow.getPredicate(), true));
        return _xQueryEditor;
    }


    private String formatQuery(String query, boolean prettify) {
        return XMLUtilities.formatXML(query, prettify, true);
    }


    private JPanel createVarList() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Net Variables: "));
        final JComboBox combo = new JComboBox(getComboItems());
        combo.setPreferredSize(new Dimension(250,25));
        combo.setEnabled(combo.getItemCount() > 0);
        combo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String varID = (String) combo.getSelectedItem();
                String xQuery = createXQuery(varID);
                _xQueryEditor.setText(xQuery);
            }
        });
        panel.add(combo);
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

    private String createXQuery(String varID) {
        YNet net = _flow.getYFlow().getSource().getNet();
        StringBuilder s = new StringBuilder("/");
        s.append(net.getID())
         .append("/")
         .append(varID)
         .append("/")
         .append(SpecificationModel.getHandler().getDataHandler().getXQuerySuffix(
                 net.getLocalOrInputVariable(varID).getDataTypeName()));
        return s.toString();
    }


}
