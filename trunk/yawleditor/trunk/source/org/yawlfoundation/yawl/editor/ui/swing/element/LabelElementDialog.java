/*
 * Created on 21/02/2006
 * YAWLEditor v1.4
 *
 * @author Lindsay Bradford
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.yawlfoundation.yawl.editor.ui.swing.element;

import org.yawlfoundation.yawl.editor.ui.elements.model.Condition;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLVertex;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.swing.JFormattedSafeXMLCharacterField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LabelElementDialog extends AbstractVertexDoneDialog {

    protected JFormattedSafeXMLCharacterField labelField;
    protected JCheckBox cbxSynch;

    public LabelElementDialog() {
        super(null, true, true);
        setContentPanel(getLabelPanel());
        getDoneButton().addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                YAWLVertex vertex = getVertex();
                String newLabel = labelField.getText();
                if (newLabel.length() == 0) newLabel = null;
                graph.setElementLabel(vertex, newLabel);
                if (cbxSynch.isSelected()) {
                    vertex.setName(newLabel);
                }
                graph.clearSelection();
                SpecificationUndoManager.getInstance().setDirty(true);
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

        cbxSynch = new JCheckBox("Synchronise task name with label");
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

        labelField = new JFormattedSafeXMLCharacterField(15);

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
        cbxSynch.setText("Synchronise " + vType + " name with label");
    }

    public String getTitlePrefix() {
        return "Label ";
    }
}
