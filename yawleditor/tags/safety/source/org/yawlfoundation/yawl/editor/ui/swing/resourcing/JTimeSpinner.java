package org.yawlfoundation.yawl.editor.ui.swing.resourcing;

import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Author: Michael Adams
 * Creation Date: 13/06/2008
 */
public class JTimeSpinner extends JPanel {

    private enum Interval { Hour, Minute, Second, Open }

    public static final int TIME_TYPE = 0;
    public static final int DURATION_TYPE = 1;

    private JSpinner yearSpinner;
    private JSpinner monthSpinner;
    private JSpinner daySpinner;
    private JSpinner hourSpinner;
    private JSpinner minSpinner;
    private JSpinner secSpinner;

    public JTimeSpinner() {
        this(TIME_TYPE);
    }

    public JTimeSpinner(int type) {
        super(new BorderLayout());
        if (type == DURATION_TYPE) {
            add(createDurationSpinner(new JPanel(new BorderLayout())), BorderLayout.WEST);
            add(createTimeSpinner(new JPanel(new BorderLayout()), false), BorderLayout.CENTER);
        }
        else {
            createTimeSpinner(this, true);
            setSpinnerValuesToNow();
        }
    }


    public int getTimeAsSeconds() {
        int hour = getHourValue();
        int min = getMinuteValue();
        int sec = getSecondValue();
        return (hour * 3600) + (min * 60) + sec;
    }


    public int getHourValue() {
        return new Integer((String) hourSpinner.getValue());
    }


    public int getMinuteValue() {
        return new Integer((String) minSpinner.getValue());
    }


    public int getSecondValue() {
        return new Integer((String) secSpinner.getValue());
    }


    public void setTime(Date date) {
        if (date != null) {
            setSpinnerValues(date);
        }    
    }


    public JFormattedTextField getTextField(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            return ((JSpinner.DefaultEditor)editor).getTextField();
        }
        else {
            System.err.println("Unexpected editor type: "
                             + spinner.getEditor().getClass()
                             + " isn't a descendant of DefaultEditor");
            return null;
        }
    }


    public void setEnabled(boolean enable) {
        if (yearSpinner != null) yearSpinner.setEnabled(enable);
        if (monthSpinner != null) monthSpinner.setEnabled(enable);
        if (daySpinner != null) daySpinner.setEnabled(enable);
        hourSpinner.setEnabled(enable);
        minSpinner.setEnabled(enable);
        secSpinner.setEnabled(enable);
    }


    public String getDuration() {
        StringBuilder periodPart = new StringBuilder("P");
        periodPart.append(getDurationItem(yearSpinner, "Y"));
        periodPart.append(getDurationItem(monthSpinner, "M"));
        periodPart.append(getDurationItem(daySpinner, "D"));
        StringBuilder timePart = new StringBuilder("T");
        timePart.append(getDurationItem(hourSpinner, "H"));
        timePart.append(getDurationItem(minSpinner, "M"));
        timePart.append(getDurationItem(secSpinner, "S"));
        if (timePart.length() > 1) periodPart.append(timePart);
        return periodPart.length() > 1 ? periodPart.toString() : "PT0S";
    }


    /****************************************************************************/

    private JPanel createTimeSpinner(JPanel panel, boolean timeLimit) {
        hourSpinner = createSpinner(timeLimit ? Interval.Hour : Interval.Open);
        hourSpinner.setToolTipText(" Hours ");
        adjustField(hourSpinner);
        panel.add(hourSpinner, BorderLayout.WEST);

        minSpinner = createSpinner(timeLimit ? Interval.Minute : Interval.Open);
        minSpinner.setToolTipText(" Minutes ");
        adjustField(minSpinner);
        panel.add(minSpinner, BorderLayout.CENTER);

        secSpinner = createSpinner(timeLimit ? Interval.Second : Interval.Open);
        secSpinner.setToolTipText(" Seconds ");
        adjustField(secSpinner);
        panel.add(secSpinner, BorderLayout.EAST);
        return panel;
    }


    private JPanel createDurationSpinner(JPanel panel) {
        yearSpinner = createSpinner(Interval.Open);
        yearSpinner.setToolTipText(" Years ");
        adjustField(yearSpinner);
        panel.add(yearSpinner, BorderLayout.WEST);

        monthSpinner = createSpinner(Interval.Open);
        monthSpinner.setToolTipText(" Months ");
        adjustField(monthSpinner);
        panel.add(monthSpinner, BorderLayout.CENTER);

        daySpinner = createSpinner(Interval.Open);
        daySpinner.setToolTipText(" Days ");
        adjustField(daySpinner);
        panel.add(daySpinner, BorderLayout.EAST);
        return panel;
    }


    private String getDurationItem(JSpinner spinner, String marker) {
        String item = "";
        if (spinner != null) {
            int value = Integer.parseInt((String) spinner.getValue());
            if (value > 0) item = value + marker;
        }
        return item;
    }


    private String[] getRange(Interval interval) {
        int max;
        switch (interval) {
            case Hour   : max = 24; break;
            case Minute :
            case Second : max = 60; break;
            default     : max = 99; break;
        }
        return getItems(max);
    }


    private String[] getItems(int max) {
        String[] items = new String[max];
        for (int i = 0; i < max; i++) {
            items[i] = String.format("%02d", i);
        }
        return items;
    }

    private JSpinner createSpinner(Interval interval) {
        String[] items = getRange(interval);
        return new JSpinner(new CyclingSpinnerListModel(items));
    }


    private  void adjustField(JSpinner spinner) {
        JFormattedTextField ftf = getTextField(spinner);
        if (ftf != null) {
            ftf.setColumns(2);
            ftf.setHorizontalAlignment(JTextField.RIGHT);
        }
    }


    private void setSpinnerValuesToNow() {
        setSpinnerValues(new Date());
    }


    private void setSpinnerValues(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        hourSpinner.setValue(String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)));
        minSpinner.setValue(String.format("%02d", cal.get(Calendar.MINUTE)));
        secSpinner.setValue(String.format("%02d", cal.get(Calendar.SECOND)));
    }


    public void setDurationValue(String duration) {
        String valueBuffer = "";
        boolean tParsed = false;
        for (int i = 1; i < duration.length(); i++) {            // ignore the 'P'
            char c = duration.charAt(i);
            if (Character.isDigit(c)) {
                valueBuffer += c;
            }
            else {
                JSpinner spinner = null;
                switch (c) {
                    case 'Y' : spinner = yearSpinner; break;
                    case 'M' : spinner = tParsed ? minSpinner : monthSpinner; break;
                    case 'D' : spinner = daySpinner; break;
                    case 'H' : spinner = hourSpinner; break;
                    case 'S' : spinner = secSpinner; break;
                    case 'T' : tParsed = true; break;
                }
                if (spinner != null) {
                    spinner.setValue(StringUtil.pad(valueBuffer, 2, '0'));
                    valueBuffer = "";
                }
            }
        }
    }


    /*******************************************************************************/

    class CyclingSpinnerListModel extends SpinnerListModel {
        Object firstValue, lastValue;
        SpinnerModel linkedModel = null;

        public CyclingSpinnerListModel(Object[] values) {
            super(values);
            firstValue = values[0];
            lastValue = values[values.length - 1];
        }

        public void setLinkedModel(SpinnerModel linkedModel) {
            this.linkedModel = linkedModel;
        }

        public Object getNextValue() {
            Object value = super.getNextValue();
            if (value == null) {
                value = firstValue;
                if (linkedModel != null) {
                    linkedModel.setValue(linkedModel.getNextValue());
                }
            }
            return value;
        }

        public Object getPreviousValue() {
            Object value = super.getPreviousValue();
            if (value == null) {
                value = lastValue;
                if (linkedModel != null) {
                    linkedModel.setValue(linkedModel.getPreviousValue());
                }
            }
            return value;
        }

    }

}
