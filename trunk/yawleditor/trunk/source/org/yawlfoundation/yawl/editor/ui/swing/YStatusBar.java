/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.swing;

import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationState;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.SpecificationStateListener;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class YStatusBar extends JPanel implements SpecificationStateListener {

    private static final JLabel statusLabel = new JLabel();
    private static final ConnectionStatus modeIndicator = new ConnectionStatus();
    private JProgressBar progressBar;
    private String previousStatusText;
    private UpdateProgressByTime updater;


    public YStatusBar() {
        super();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        statusLabel.setForeground(Color.DARK_GRAY);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        add(modeIndicator, BorderLayout.WEST);
        add(statusLabel, BorderLayout.CENTER);
        add(getProgressBar(), BorderLayout.EAST);
        Publisher.getInstance().subscribe(this);
    }


    public String getText() {
        return statusLabel.getText();
    }


    public void setText(String message) {
        previousStatusText = getText();
        statusLabel.setText(" " + message);
    }


    public void setTextToPrevious() {
        setText(previousStatusText);
    }


    public boolean refreshConnectionStatus() {
        return modeIndicator.refresh();
    }


    private JPanel getProgressBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        panel.setSize(new Dimension(200, 16));

        progressBar = new JProgressBar();
        progressBar.setBorderPainted(false);
        progressBar.setVisible(false);
        panel.add(progressBar, BorderLayout.CENTER);
        return panel;
    }


    public void finishProgress() {
        progressBar.setVisible(false);
    }


    public void progressOverSeconds(final int pauseSeconds) {
        progressBar.setVisible(true);
        updater = new UpdateProgressByTime(pauseSeconds);
         updater.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) {
                    progressBar.setValue((Integer) evt.getNewValue());
                }
            }
        });

        updater.execute();
    }


    public void resetProgress() {
        if (updater != null) updater.cancel(true);
        finishProgress();
    }

    public void freeze() {
        if (updater != null) updater.cancel(true);
    }

    public void specificationStateChange(SpecificationState state) {
        switch(state) {
            case NoNetsExist: {
                setText("Open or create a specification to begin.");
                break;
            }
            case NetsExist:
            case NoNetSelected: {
                setText("Select a net to continue editing it.");
                break;
            }
            case NetSelected: {
                setText("Use the palette toolbar to edit the selected net.");
                break;
            }
        }
    }



    /******************************************************************************/

    class UpdateProgressByTime extends SwingWorker<Void, Integer> {

        private static final long WAIT_MSECS = 50;
        private final int seconds;

        UpdateProgressByTime(int seconds) {
            this.seconds = seconds;
         }

        protected Void doInBackground() throws Exception {
            int progress = 0;
            int maxMSecs = seconds * 1000;
            while (progress < maxMSecs && ! isCancelled()) {
                progress += WAIT_MSECS;
                setProgress(Math.min(progress * 100 / maxMSecs, 100));
                pause(WAIT_MSECS);
            }
            return null;
        }

        private void pause(long milliseconds) {
            try {
                Thread.sleep(milliseconds);
            }
            catch (InterruptedException ie) {
                // ignore
            }
        }
    }

}
