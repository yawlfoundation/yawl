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

package org.yawlfoundation.yawl.engine.gui;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


/* YSplash.java
 *
 * Created on 12 december 2001, 16:34
 * @author  remco d
 * @version
 */

public class YSplash extends JWindow {
    final JProgressBar progressBar = new JProgressBar(0, 100);

    public YSplash(String filename, JFrame f, int waitTime) {
        super(f);
        JLabel l = new JLabel(
                new ImageIcon(getToolkit().getImage(
                        getClass().getResource(filename))));
        getContentPane().add(l, BorderLayout.CENTER);

        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        getContentPane().add(progressBar, BorderLayout.SOUTH);
        pack();

        /**
         * AJH: Changed to support dual-head graphics environments better
         */
        Dimension labelSize = l.getPreferredSize();
        Double screenWidth = new Double(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getWidth());
        Double screenHeight = new Double(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight());

        setLocation(screenWidth.intValue() / 2 - (labelSize.width / 2),
                    screenHeight.intValue() / 2 - (labelSize.height / 2));


        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                setVisible(false);
                dispose();
            }
        });
        setVisible(true);
    }


    public void setProgress(int i) {
        progressBar.setValue(i);
        if (i == 100) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.dispose();
        }
    }
}
