package org.yawlfoundation.yawl.controlpanel.update;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.math.BigDecimal;

/**
 * @author Michael Adams
 * @date 27/08/2014
 */
public class ProgressPanel extends JPanel {

    private JLabel _prompt;
    private JProgressBar _bar;
    private long _downloadSize;
    private String _downloadSizeString;


    public ProgressPanel() {
        build();
    }


    public void setText(String text) { _prompt.setText(text); }


    public void setIndeterminate(boolean indeterminate) {
        if (! indeterminate && _bar.isIndeterminate()) {
            _bar.setMaximum(100);
            _bar.setValue(0);
        }
        else if (indeterminate) {
            _bar.setString("");
        }
        _bar.setIndeterminate(indeterminate);
    }


    public void update(int progress) {
        _bar.setValue(progress);
        _bar.setString(getProgressString(progress));
    }


    public void setDownloadSize(long size) {
        _downloadSize = size;
        _downloadSizeString = shorten(size);
    }


    protected void build() {
        setLayout(new GridLayout(0,1,5,5));
        setOpaque(true);
        setBorder(new CompoundBorder(new LineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 10, 10, 10)));
        _prompt = new JLabel("Downloading updates...");
        _bar = createBar();
        add(_prompt);
        add(_bar);
    }


    private JProgressBar createBar() {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(0);
        bar.setStringPainted(true);
        bar.setIndeterminate(true);
        bar.setVisible(true);
        return bar;
    }


    protected String getProgressString(int progress) {
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
