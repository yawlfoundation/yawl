package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.component.FlowConditionTablePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Author: Michael Adams
 * Creation Date: 8/04/2013
 */
public class FlowConditionDialog extends JDialog implements ActionListener {

    private FlowConditionTablePanel _tablePanel;

    public FlowConditionDialog(Window parent, YAWLTask task, NetGraph graph) {
        super(parent);
        setModal(true);
        setTitle(makeTitle(task));
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        add(getContent(task, graph));
        this.setPreferredSize(new Dimension(420, 270));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                _tablePanel.resetFlowColours();
                super.windowClosing(windowEvent);
            }
        });

        pack();
    }


    private JPanel getContent(YAWLTask task, NetGraph graph) {
        JPanel content = new JPanel();
        _tablePanel = new FlowConditionTablePanel(this, task, graph);
        JButton btnOK = new JButton("OK");
        btnOK.setActionCommand("OK");
        btnOK.setPreferredSize(new Dimension(75, 25));
        btnOK.addActionListener(this);
        content.add(_tablePanel);
        content.add(btnOK);
        return content;
    }

    public void actionPerformed(ActionEvent event) {
        _tablePanel.resetFlowColours();
        setVisible(false);
    }


    private String makeTitle(YAWLTask task) {
        return String.format("Split Conditions for Task '%s'", task.getID());
    }

}
