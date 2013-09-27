package org.yawlfoundation.yawl.editor.ui.preferences;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;

/**
 * @author Michael Adams
 * @date 25/09/13
 */
public class ConnectionPanel extends JPanel {

    JTextField _hostField;
    JFormattedTextField _portField;


    public ConnectionPanel(String title) {
        super();
        setBorder(new TitledBorder(title));
        addFields();
        setPreferredSize(new Dimension(500, 75));
    }


    public String getHost() { return _hostField.getText(); }

    public void setHost(String host) { _hostField.setText(host); }

    public int getPort() { return Integer.parseInt(_portField.getText()); }

    public void setPort(int port) { _portField.setText(String.valueOf(port)); }


    public void addCaretListener(CaretListener listener) {
        _hostField.addCaretListener(listener);
        _portField.addCaretListener(listener);
    }

    private void addFields() {
        _hostField = new JTextField();
        _hostField.setPreferredSize(new Dimension(300, 25));
        add(new JLabel("Host"));
        add(_hostField);
        _portField = new JFormattedTextField(getPortValueFormatter());
        _portField.setPreferredSize(new Dimension(75, 25));
        add(new JLabel("Port"));
        add(_portField);
    }


    private NumberFormatter getPortValueFormatter() {
        NumberFormat plainIntegerFormat = NumberFormat.getInstance();
        plainIntegerFormat.setGroupingUsed(false);                      // no commas

        NumberFormatter portFormatter = new NumberFormatter(plainIntegerFormat);
        portFormatter.setValueClass(Integer.class);
        portFormatter.setAllowsInvalid(false);
        portFormatter.setMinimum(0);
        portFormatter.setMaximum(65535);
        return portFormatter;
    }

}
