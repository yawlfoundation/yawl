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

import org.yawlfoundation.yawl.editor.core.YConnector;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.plugin.YPluginHandler;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.PropertyDialog;
import org.yawlfoundation.yawl.editor.ui.specification.io.LayoutRepository;
import org.yawlfoundation.yawl.editor.ui.specification.io.SpecificationReader;
import org.yawlfoundation.yawl.editor.ui.specification.pubsub.Publisher;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.SpecificationData;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Michael Adams
 * @date 18/10/13
 */
public class SpecificationDownloadDialog extends PropertyDialog
        implements ActionListener, ListSelectionListener {

    private JList listBox;

    public SpecificationDownloadDialog(Window parent) {
        super(parent);
        setTitle("Available Specifications");
        setPreferredSize(new Dimension(400, 200));
        pack();
    }

    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        if (e.getActionCommand().equals("OK")) {
            download();
        }
    }

    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        getOKButton().setEnabled(true);
    }

    protected JPanel getContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.add(getListPanel(), BorderLayout.CENTER);
        content.add(getButtonBar(this), BorderLayout.SOUTH);
        return content;
    }


    private JPanel getListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(7,7,7,7));
        panel.add(createListBox(), BorderLayout.CENTER);
        return panel;
    }


    private JScrollPane createListBox() {
        listBox = new JList(new SpecificationListModel(getLoadedSpecifications()));
        listBox.addListSelectionListener(this);
        listBox.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    setVisible(false);
                    download();
                }
            }
        });

        return new JScrollPane(listBox);
    }


    private void download() {
        YSpecificationID selectedID =
                ((SpecificationListModel)listBox.getModel()).getSelectedID();
        if (selectedID != null) {
            try {
                String specXML = YConnector.getSpecification(selectedID);
                if (specXML.startsWith("<fail")) {
                    throw new IOException(StringUtil.unwrap(specXML));
                }
                YAWLEditor editor = YAWLEditor.getInstance();
                YStatusBar statusBar = YAWLEditor.getStatusBar();
                editor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Publisher.getInstance().publishFileBusyEvent();
                statusBar.setText("Downloading Specification...");
                statusBar.progressOverSeconds(4);
                YAWLEditor.getNetsPane().setVisible(false);

                SpecificationReader reader = new SpecificationReader(specXML,
                        getSpecificationLayout(selectedID));
                reader.addPropertyChangeListener(new LoadCompletionListener());
                reader.execute();
            }
            catch (IOException ioe) {
                showError("Failed to get download the selected specification from" +
                        " the YAWL Engine:", ioe);
            }
        }
    }


    private java.util.List<YSpecificationID> getLoadedSpecifications() {
        java.util.List<YSpecificationID> list = new ArrayList<YSpecificationID>();
        try {
            for (SpecificationData specData : YConnector.getLoadedSpecificationList()) {
                YSpecificationID specID = specData.getID();
                list.add(specID);
            }
        }
        catch (IOException ioe) {
            showError("Failed to get list of specifications from the YAWL Engine:", ioe);
        }
        sortSpecificationsList(list);
        return list;
    }


    private String getSpecificationLayout(YSpecificationID specID) {
        return LayoutRepository.getInstance().get(specID);
    }


    private void showError(String message, Exception e) {
        JOptionPane.showMessageDialog(this, message + '\n' + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }


    private void sortSpecificationsList(java.util.List<YSpecificationID> list) {
        Collections.sort(list, new Comparator<YSpecificationID>() {
            public int compare(YSpecificationID specID1, YSpecificationID specID2) {
                return specID1.toString().compareTo(specID2.toString());
            }
        });
    }


    /******************************************************************************/

    class SpecificationListModel extends AbstractListModel {

        private final java.util.List<YSpecificationID> items;


        protected SpecificationListModel(java.util.List<YSpecificationID> items) {
            this.items = items;
        }

        public int getSize() {
            return items != null ? items.size() : 0;
        }

        public Object getElementAt(int i) {
            YSpecificationID specID = items.get(i);
            return specID.getUri() + " (version " + specID.getVersionAsString() + ")";
        }

        public YSpecificationID getSelectedID() {
            int i = listBox.getSelectedIndex();
            return i > -1 ? items.get(i) : null;
        }
    }


    /******************************************************************************/

    class LoadCompletionListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent event) {
            if (event.getNewValue() == SwingWorker.StateValue.DONE) {
                YAWLEditor.getNetsPane().setVisible(true);
                YAWLEditor.getStatusBar().resetProgress();
                Publisher.getInstance().publishFileUnbusyEvent();
                YAWLEditor.getInstance().setCursor(
                        Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                YPluginHandler.getInstance().specificationLoaded();
            }
        }
    }


}
