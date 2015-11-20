package org.yawlfoundation.yawl.controlpanel.cli;

import org.yawlfoundation.yawl.controlpanel.update.ProgressPanel;

/**
 * @author Joerg Evermann
 * @author Michael Adams
 * @date 3/11/2015
 */
public class CliProgressPanel extends ProgressPanel {

    private static final int INCREMENTS = 25;

    private StringBuilder _bar;


    public CliProgressPanel() { super(); }


    public void setText(String text) { write(text); }


    public void update(int progress) {
        System.out.print(getProgressBar(progress));
    }


    public void setVisible(boolean b) { }             // consume call

    public void setIndeterminate(boolean b) { }       // consume call


    public void complete(boolean cancelled) {
        StringBuilder s = new StringBuilder(INCREMENTS + 3);
        s.append('\r');
        s.append("Download ");
        s.append(cancelled ? "cancelled" : "completed");
        for (int i=s.length(); i<INCREMENTS + 3; i++) s.append(' ');
        write(s.toString());
    }


    protected void build() {
        _bar = new StringBuilder(INCREMENTS + 3);
        _bar.append('|');
        for (int i=0; i<INCREMENTS; i++) _bar.append(' ');
        _bar.append('|');
        _bar.append('\r');
    }


    private String getProgressBar(int progress) {
        int marks = Math.round(progress * INCREMENTS / 100);
        for (int i=1; i<=marks; i++) _bar.setCharAt(i, '=');
        return _bar.toString();
    }


    private void write(String s) { System.out.println(s); }

}
