package org.yawlfoundation.yawl.controlpanel.update;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * @author Michael Adams
 * @date 19/08/2014
 */
public class ProgressDialog extends JDialog {

    private JLabel _message;
    private JProgressBar _progressBar;
    private long _downloadSize;
    private String _downloadSizeString;

    public ProgressDialog(JDialog parent) {
        super(parent);
        init();
        setLocationRelativeTo(parent);
    }


    public void setText(String text) { _message.setText(text); }


    public void setIndeterminate(boolean indeterminate) {
        if (! indeterminate && _progressBar.isIndeterminate()) {
            _progressBar.setMaximum(100);
            _progressBar.setValue(0);
        }
        _progressBar.setIndeterminate(indeterminate);
    }


    public void update(int progress) {
        _progressBar.setValue(progress);
        _progressBar.setString(getProgressString(progress));
    }


    public void setDownloadSize(long size) {
        _downloadSize = size;
        _downloadSizeString = shorten(size);
    }


    private void init() {
        setAlwaysOnTop(true);
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setContentPane(createContent());
        pack();
    }


    private JPanel createContent() {
        JPanel panel = new JPanel(new GridLayout(0,1,5,5));
        panel.setBorder(new EmptyBorder(10,10,10,10));
        _message = new JLabel("Downloading updates...");
        panel.add(_message);
        panel.add(createProgressBar());
        return panel;
    }


    private JProgressBar createProgressBar() {
        _progressBar = new JProgressBar(0, 100);
        _progressBar.setValue(0);
        _progressBar.setStringPainted(true);
        _progressBar.setIndeterminate(true);
        _progressBar.setVisible(true);
        return _progressBar;
    }


    private String getProgressString(int progress) {
        StringBuilder s = new StringBuilder();
        s.append(getDownloadRatio(progress)).append(" (").append(progress).append("%)");
        return s.toString();
    }


    private String getDownloadRatio(int progress) {
        return shorten(_downloadSize * progress / 100) + "/" + _downloadSizeString;
    }


    private String shorten(long size) {
        if (size < 1024) return size + "b";
        else if (size < 1024 * 1024) return getScaledSize(size / 1024.0, 0) + "Kb";
        else return getScaledSize(size / (1024.0 * 1024.0), 2) + "Mb";
    }


    private BigDecimal getScaledSize(double size, int scale) {
        return BigDecimal.valueOf(size).setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

}
