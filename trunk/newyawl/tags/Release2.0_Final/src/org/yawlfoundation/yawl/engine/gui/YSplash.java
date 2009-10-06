/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
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
