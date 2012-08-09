package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import com.toedter.calendar.JDateChooser;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.data.DataVariableSet;
import org.yawlfoundation.yawl.editor.ui.elements.model.TaskTimeoutDetail;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.swing.data.TimerDataVariableComboBox;
import org.yawlfoundation.yawl.editor.ui.swing.resourcing.JTimeSpinner;
import org.yawlfoundation.yawl.engine.time.YWorkItemTimer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Michael Adams
 * @date 18/07/12
 */
public class TimerDialog extends AbstractDoneDialog implements ActionListener {

    private TimerDataVariableComboBox timerVariableComboBox;
    private JPanel dateValueField;
    private JTimeSpinner durationValueField;
    private JPanel expiresPanel;
    private ButtonGroup expiresGroup;
    private JRadioButton variableButton;
    private JRadioButton neverButton;
    private JRadioButton offerButton;
    private JRadioButton startButton;
    private JRadioButton exactlyButton;
    private JRadioButton durationButton;


    public TimerDialog() {
        super("Set Timer", true, true);
        setContentPanel(buildPanel());
        setLocationRelativeTo(YAWLEditor.getInstance());
        getDoneButton().setText("OK");
    }

    protected void makeLastAdjustments() {
        setSize(523, 204);
        setResizable(false);
    }


    public void setContent(TaskTimeoutDetail content) {
        if (content == null) {
            performAction(neverButton, "never");
        }
        else {
            timerVariableComboBox.setDecomposition(content.getTask().getWSDecomposition());
            setTrigger(content.getTrigger());
            setValue(content);
        }
    }


    public TaskTimeoutDetail getContent() {
        if (neverButton.isSelected()) return null;

        TaskTimeoutDetail timerDetail = new TaskTimeoutDetail(null);
        getValue(timerDetail);
        return timerDetail;
    }


    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals("never")) {
            enablePanel(expiresPanel, false);
        }
        else if (action.startsWith("on")) {
            enablePanel(expiresPanel, true);
            enableWidgets(expiresGroup.getSelection().getActionCommand());
        }
        else {
            enableWidgets(action);
        }
        variableButton.setEnabled(timerVariableComboBox.getItemCount() > 0);
    }


    /*****************************************************************************/

    private void setTrigger(YWorkItemTimer.Trigger trigger) {
        switch (trigger) {
            case OnEnabled: {
                offerButton.setSelected(true);
                performAction(offerButton, "onOffer");
                break;
            }
            case OnExecuting: {
                startButton.setSelected(true);
                performAction(startButton, "onStart");
                break;
            }
            default : {
                neverButton.setSelected(true);
                performAction(neverButton, "never");
                break;
            }
        }
    }


    private YWorkItemTimer.Trigger getTrigger() {
        if (offerButton.isSelected()) return YWorkItemTimer.Trigger.OnEnabled;
        if (startButton.isSelected()) return YWorkItemTimer.Trigger.OnExecuting;
        return YWorkItemTimer.Trigger.Never;
    }


    private void setValue(TaskTimeoutDetail content) {
        switch (content.getTimerParameters().getTimerType()) {
            case Expiry: {
                setDateValue(content.getTimeoutDate());
                exactlyButton.setSelected(true);
                performAction(exactlyButton, "exactly");
                break;
            }
            case Duration: {
                durationValueField.setDurationValue(content.getDurationValue());
                durationButton.setSelected(true);
                performAction(durationButton, "duration");
                break;
            }
            case LateBound: {
                timerVariableComboBox.setSelectedItem(content.getTimeoutVariable());
                variableButton.setSelected(true);
                performAction(variableButton, "variable");
                break;
            }
        }
    }


    private void getValue(TaskTimeoutDetail timerDetail) {
        if (exactlyButton.isSelected()) {
            timerDetail.setValue(getTrigger(), getDateValue());
        }
        else if (durationButton.isSelected()) {
            timerDetail.setValue(getTrigger(), durationValueField.getDuration());
        }
        else if (variableButton.isSelected()) {
            timerDetail.setValue(timerVariableComboBox.getSelectedVariable());
        }
    }


    private void setDateValue(Date date) {
        for (Component widget : dateValueField.getComponents()) {
            if (widget instanceof JDateChooser) {
                JDateChooser chooser = (JDateChooser) widget ;
                chooser.setDate(date);
            }
            else if (widget instanceof JTimeSpinner) {
                JTimeSpinner spinner = (JTimeSpinner) widget;
                spinner.setTime(date);
            }
        }
    }


    private Date getDateValue() {
        Date date = null;
        int time = 0;
        for (Component widget : dateValueField.getComponents()) {
            if (widget instanceof JDateChooser) {
                JDateChooser chooser = (JDateChooser) widget ;
                date = chooser.getDate();
            }
            else if (widget instanceof JTimeSpinner) {
                JTimeSpinner spinner = (JTimeSpinner) widget;
                time = spinner.getTimeAsSeconds();
            }
        }
        GregorianCalendar cal = new GregorianCalendar();
        if (date != null) {
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.add(Calendar.SECOND, time) ;
            return cal.getTime();
        }
        else return null ;
    }


    private void performAction(Object source, String action) {
        actionPerformed(new ActionEvent(source, ActionEvent.ACTION_PERFORMED, action));
    }


    private void enableWidgets(String action) {
        enablePanel(dateValueField, action.equals("exactly"));
        durationValueField.setEnabled(action.equals("duration"));
        enablePanel(timerVariableComboBox.getParent(), action.equals("variable"));
    }


    private void enablePanel(Container panel, boolean enable) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JPanel) {
                enablePanel((JPanel) component, enable);      // recurse
            }
            component.setEnabled(enable);
        }
        panel.setEnabled(enable);
    }


    private JPanel buildPanel() {

        // expires panel
        expiresPanel = new JPanel(new BorderLayout());
        expiresPanel.setBorder(new TitledBorder("Expires"));
        expiresPanel.add(buildExpiresButtonsPanel(), BorderLayout.WEST);
        expiresPanel.add(buildExpiresWidgetsPanel(), BorderLayout.CENTER);

        // final panel
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10,10,0,10));
        panel.add(buildBeginsPanel(), BorderLayout.WEST);
        panel.add(expiresPanel, BorderLayout.CENTER);

        return panel;
    }


    private JPanel buildBeginsPanel() {
        neverButton = new JRadioButton("Never");
        neverButton.setMnemonic(KeyEvent.VK_N);
        neverButton.setActionCommand("never");
        neverButton.addActionListener(this);
        neverButton.setSelected(true);

        offerButton = new JRadioButton("On offer");
        offerButton.setMnemonic(KeyEvent.VK_O);
        offerButton.setActionCommand("onOffer");
        offerButton.addActionListener(this);

        startButton = new JRadioButton("On start");
        startButton.setMnemonic(KeyEvent.VK_S);
        startButton.setActionCommand("onStart");
        startButton.addActionListener(this);

        ButtonGroup beginsGroup = new ButtonGroup();
        beginsGroup.add(neverButton);
        beginsGroup.add(offerButton);
        beginsGroup.add(startButton);

        JPanel beginsPanel = new JPanel(new BorderLayout());
        beginsPanel.setBorder(new TitledBorder("Begins"));
        JPanel innerBeginsPanel = new JPanel(new GridLayout(0, 1));
        innerBeginsPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        innerBeginsPanel.add(neverButton);
        innerBeginsPanel.add(offerButton);
        innerBeginsPanel.add(startButton);
        beginsPanel.add(innerBeginsPanel, BorderLayout.CENTER);
        return beginsPanel;
    }


    private JPanel buildExpiresButtonsPanel() {
        exactlyButton = new JRadioButton("At exactly");
        exactlyButton.setMnemonic(KeyEvent.VK_E);
        exactlyButton.setActionCommand("exactly");
        exactlyButton.addActionListener(this);
        exactlyButton.setSelected(true);

        durationButton = new JRadioButton("After duration");
        durationButton.setMnemonic(KeyEvent.VK_D);
        durationButton.setActionCommand("duration");
        durationButton.addActionListener(this);

        variableButton = new JRadioButton("Via variable");
        variableButton.setMnemonic(KeyEvent.VK_V);
        variableButton.setActionCommand("variable");
        variableButton.addActionListener(this);

        expiresGroup = new ButtonGroup();
        expiresGroup.add(exactlyButton);
        expiresGroup.add(durationButton);
        expiresGroup.add(variableButton);

        JPanel expiresButtonsPanel = new JPanel(new GridLayout(0, 1));
        expiresButtonsPanel.setBorder(new EmptyBorder(0,10,0,10));
        expiresButtonsPanel.add(exactlyButton);
        expiresButtonsPanel.add(durationButton);
        expiresButtonsPanel.add(variableButton);
        return expiresButtonsPanel;
    }


    private JPanel buildExpiresWidgetsPanel() {
        JPanel expiresWidgetsPanel = new JPanel(new BorderLayout());
        expiresWidgetsPanel.setBorder(new EmptyBorder(0,0,0,10));
        expiresWidgetsPanel.add(buildDateTimeValueField(), BorderLayout.NORTH);
        expiresWidgetsPanel.add(buildDurationValueField(), BorderLayout.CENTER);
        expiresWidgetsPanel.add(buildVariableComboBox(), BorderLayout.SOUTH);
        return expiresWidgetsPanel;
    }


    private JPanel buildDateTimeValueField() {
        dateValueField = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        dateValueField.setBorder(new EmptyBorder(3,3,3,3));
        JDateChooser chooser = new JDateChooser();
        chooser.setMinSelectableDate(Calendar.getInstance().getTime());
        chooser.setDate(Calendar.getInstance().getTime());

        // workaround for too narrow an edit field
        chooser.setDateFormatString("dd/MM/yyyy ");
        chooser.setMinimumSize(chooser.getPreferredSize());
        dateValueField.add(chooser) ;

        JTimeSpinner spinner = new JTimeSpinner();
        spinner.setMinimumSize(spinner.getPreferredSize());
        dateValueField.add(spinner) ;

        return dateValueField ;
    }


    private JPanel buildDurationValueField() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(3,3,3,3));
        durationValueField = new JTimeSpinner(JTimeSpinner.DURATION_TYPE);
        durationValueField.setMinimumSize(durationValueField.getPreferredSize());
        panel.add(durationValueField);
        return panel;
    }


    private JPanel buildVariableComboBox() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(3,3,3,3));
        timerVariableComboBox = new TimerDataVariableComboBox();
        timerVariableComboBox.setValidUsageType(DataVariableSet.VALID_USAGE_INPUT_FROM_NET);
        panel.add(timerVariableComboBox, BorderLayout.CENTER);

        return panel;
    }

}
