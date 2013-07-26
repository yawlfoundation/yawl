package org.yawlfoundation.yawl.editor.ui.actions.element;

import org.yawlfoundation.yawl.editor.ui.elements.model.Decorator;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;
import org.yawlfoundation.yawl.editor.ui.swing.JUtilities;
import org.yawlfoundation.yawl.editor.ui.swing.element.AbstractTaskDoneDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * @author Michael Adams
 * @date 23/07/12
 */
public class FlowPriorityDialog extends AbstractTaskDoneDialog {

    private FlowDetailTablePanel flowDetailPanel;
    private JButton updatePredicateButton;
    private JLabel defaultLabel;


    public FlowPriorityDialog() {
        super(null, true, false);
        setResizable(false);
        buildContentPanel();
    }

    public void buildContentPanel() {
        flowDetailPanel = new FlowDetailTablePanel(this);
        JPanel panel = new JPanel();

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();

        panel.setLayout(gbl);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;

        panel.add(flowDetailPanel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10,0,0,0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(createDefaultLabel(), gbc);

        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridx = 1;
        gbc.insets = new Insets(0,0,0,10);
        gbc.fill = GridBagConstraints.NONE;

        panel.add(getUpdatePredicateButton(), gbc);

        setContentPanel(panel);
    }

    public FlowDetailTablePanel getFlowDetailTablePanel() {
        return this.flowDetailPanel;
    }

    protected void makeLastAdjustments() {
        pack();
        JUtilities.setMinSizeToCurrent(this);
    }

    public String getTitlePrefix() {
        return "Flow detail for ";
    }

    public void setTask(YAWLTask task, NetGraph graph) {
        super.setTask(task, graph);
        getFlowDetailTablePanel().setTaskAndNet(task, graph);
        getFlowDetailTablePanel().selectFlowAtRow(0);
        setDefaultLabelForSplitType(task);
        updatePredicateButton.setEnabled(getFlowDetailTablePanel().hasFlows());
    }


    public void setVisible(boolean state) {
        if (state) {
            JUtilities.centreWindowUnderVertex(graph, this, getTask(), 10);
        }
        else {
            getFlowDetailTablePanel().colorSelectedFlow(Color.BLACK);
        }
        super.setVisible(state);
    }


    private JButton getUpdatePredicateButton() {
        updatePredicateButton = new JButton("Predicate...");
        updatePredicateButton.setMnemonic(KeyEvent.VK_P);
        updatePredicateButton.setMargin(new Insets(2,11,3,12));
        updatePredicateButton.setToolTipText(" Change predicate of selected flow ");
        updatePredicateButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                getFlowDetailTablePanel().updatePredicateOfSelectedFlow();
            }
        }
        );
        return updatePredicateButton;
    }

    private JLabel createDefaultLabel() {
        defaultLabel = new JLabel("The bottom-most flow will be used as the default.");
        defaultLabel.setHorizontalAlignment(JLabel.CENTER);
        return defaultLabel;
    }

    public void setDefaultLabelForSplitType(YAWLTask task) {
        String text = "The bottom-most flow will be used as the default.";
        if (task.hasSplitDecorator() &&
                (task.getSplitDecorator().getType() == Decorator.XOR_TYPE)) {
            text = text.replaceFirst("be", "be set to 'true()' and");
        }
        defaultLabel.setText(text);
    }
}
