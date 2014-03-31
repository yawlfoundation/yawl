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

package org.yawlfoundation.yawl.editor.ui.swing.element;

import org.yawlfoundation.yawl.editor.core.YSpecificationHandler;
import org.yawlfoundation.yawl.editor.ui.elements.model.Condition;
import org.yawlfoundation.yawl.editor.ui.elements.model.VertexContainer;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.swing.JFormattedSafeXMLCharacterField;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LabelElementDialog extends AbstractVertexDoneDialog {

    private JFormattedSafeXMLCharacterField labelField;
    private JCheckBox cbxSynch;

    public LabelElementDialog() {
        super();
        setContentPanel(getLabelPanel());
        getDoneButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                YAWLVertex vertex = getVertex();
                String newLabel = labelField.getText();
                if (newLabel.length() == 0) newLabel = null;
                graph.setElementLabel(vertex, newLabel);
                if (cbxSynch.isSelected()) {
                    updateVertexID(vertex, newLabel);
                }
                graph.clearSelection();
                SpecificationUndoManager.getInstance().setDirty(true);
                VertexContainer container = (VertexContainer) vertex.getParent();
                graph.setSelectionCell(container != null ? container : vertex);
            }
        }
        );
        getRootPane().setDefaultButton(getDoneButton());
        labelField.requestFocus();
    }

    private JPanel getLabelPanel() {

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel panel = new JPanel(gbl);
        panel.setBorder(new EmptyBorder(12,12,0,11));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0,0,0,5);
        gbc.anchor = GridBagConstraints.EAST;

        JLabel label = new JLabel("Set label to:");
        label.setDisplayedMnemonic('S');
        panel.add(label, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;

        labelField = getLabelField();

        label.setLabelFor(labelField);

        panel.add(labelField, gbc);

        gbc.gridx--;
        gbc.gridy++;
        gbc.gridwidth = 2 ;
        gbc.insets = new Insets(10,0,0,0);

        cbxSynch = new JCheckBox("Synchronise task id with label");
        cbxSynch.setSelected(true);                                  // always by default
        panel.add(cbxSynch, gbc);
        pack();
        return panel;
    }

    private JFormattedSafeXMLCharacterField getLabelField() {

    /* 
       Note that using a JFormattedSafeXMLCharacterField here is a workaround
       The current bleeding-edge engine (BETA 7.2) throws a 'nana if there
       are certain special characters in the <name/> element of the XML
       specification, even though those special characters have been quoted at 
       export time.  For the time being, I'll just limit users to inputing
       "safe" (non-special) characters that this text field enforces.  
    */

        labelField = new JFormattedSafeXMLCharacterField();

        labelField.setToolTipText(" Enter a label to go under this net element. ");
        labelField.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        getDoneButton().doClick();
                    }
                }
        );

        return labelField;
    }


    public void setVertex(YAWLVertex vertex, NetGraph graph) {
        super.setVertex(vertex,graph);
        labelField.setText(vertex.getLabel());
        String vType = (getVertex() instanceof Condition) ? "condition" : "task" ;
        cbxSynch.setText("Synchronise " + vType + " id with label");
    }

    public String getTitlePrefix() {
        return "Label ";
    }


    private void updateVertexID(YAWLVertex vertex, String id) {
        if (id != null) {
            String validID = XMLUtilities.toValidXMLName(id);
            if (!vertex.getID().equals(validID)) {
                YSpecificationHandler handler = SpecificationModel.getHandler();
                validID = handler.getControlFlowHandler().replaceID(vertex.getID(), validID);
                if (vertex instanceof YAWLTask) {
                    handler.getResourceHandler().replaceID(vertex.getID(), validID);
                }
                vertex.setID(validID);
            }
        }
    }
}
