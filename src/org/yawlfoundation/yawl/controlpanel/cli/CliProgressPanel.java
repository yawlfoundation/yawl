/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

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
