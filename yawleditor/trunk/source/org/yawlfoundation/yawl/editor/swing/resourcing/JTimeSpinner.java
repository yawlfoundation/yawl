package org.yawlfoundation.yawl.editor.swing.resourcing;

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

    private enum Interval { Hour, Minute, Second }

    JSpinner hourSpinner ;
    JSpinner minSpinner;
    JSpinner secSpinner;

    public JTimeSpinner() {
        super(new BorderLayout());
        Date now = Calendar.getInstance().getTime();

        hourSpinner = createSpinner(Interval.Hour);
        hourSpinner.setToolTipText(" Hours ");
        adjustField(hourSpinner);
        add(hourSpinner, BorderLayout.WEST);
      
        minSpinner = createSpinner(Interval.Minute);
        minSpinner.setToolTipText(" Minutes ");
        adjustField(minSpinner);
        add(minSpinner, BorderLayout.CENTER);

        secSpinner = createSpinner(Interval.Second);
        secSpinner.setToolTipText(" Seconds ");
        adjustField(secSpinner);
        add(secSpinner, BorderLayout.EAST);

        setSpinnerValuesToNow();
    }


    public int getTimeAsSeconds() {
        int hour = new Integer((String) hourSpinner.getValue()) ;
        int min = new Integer((String) minSpinner.getValue()) ;
        int sec = new Integer((String) secSpinner.getValue()) ;
        return (hour * 3600) + (min * 60) + sec ;
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


    private String[] getRange(Interval interval) {
        int max = (interval == Interval.Hour) ? 24 : 60;
        String[] result = new String[max];

        for (int i = 0; i < max; i++) {
            String item = String.format("%02d", i) ;
            result[i] = item ;
        }
        return result;
    }

    private JSpinner createSpinner(Interval interval) {
        String[] items = getRange(interval);
        return new JSpinner(new CyclingSpinnerListModel(items));
    }


    private  void adjustField(JSpinner spinner) {
       JFormattedTextField ftf = getTextField(spinner);
       if (ftf != null ) {
           ftf.setColumns(2);
           ftf.setHorizontalAlignment(JTextField.RIGHT);
       }
    }


    private void setSpinnerValuesToNow() {
        Calendar now = new GregorianCalendar();
        hourSpinner.setValue(String.format("%02d", now.get(Calendar.HOUR_OF_DAY)));
        minSpinner.setValue(String.format("%02d", now.get(Calendar.MINUTE)));
        secSpinner.setValue(String.format("%02d", now.get(Calendar.SECOND)));
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
