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

import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.specification.io.SpecificationUploader;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.PropertyDialog;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

/**
 * @author Michael Adams
 * @date 19/09/13
 */
public class SpecificationUploadDialog extends PropertyDialog
        implements ActionListener, ItemListener {

    private JCheckBox _cbxUnloadPrevious;
    private JCheckBox _cbxLaunchCase;
    private JCheckBox _cbxCancelCases;

    public SpecificationUploadDialog(Window parent) {
        super(parent);
        setTitle("Upload Options");
        setPreferredSize(new Dimension(200, 163));
        loadPreferences();
        pack();
    }


    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        if (e.getActionCommand().equals("OK")) {
            upload();
            savePreferences();
        }
    }


    public void itemStateChanged(ItemEvent itemEvent) {
        _cbxCancelCases.setEnabled(_cbxUnloadPrevious.isSelected());
    }


    protected JPanel getContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.add(getUnloadPanel(), BorderLayout.NORTH);
        content.add(getLaunchPanel(), BorderLayout.CENTER);
        content.add(getButtonBar(this), BorderLayout.SOUTH);
        return content;
    }


    private JPanel getUnloadPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        _cbxUnloadPrevious = new JCheckBox("Unload Previous Versions");
        _cbxUnloadPrevious.setBorder(new EmptyBorder(10,10,5,0));
        _cbxUnloadPrevious.setAlignmentX(LEFT_ALIGNMENT);
        _cbxUnloadPrevious.setMnemonic('U');
        _cbxUnloadPrevious.addItemListener(this);
        content.add(_cbxUnloadPrevious);
        _cbxCancelCases = new JCheckBox("Cancel Running Cases");
        _cbxCancelCases.setBorder(new EmptyBorder(3,30,10,0));
        _cbxCancelCases.setMnemonic('C');
        content.add(_cbxCancelCases);
        return content;
    }


    private JPanel getLaunchPanel() {
        JPanel content = new JPanel(new FlowLayout(FlowLayout.LEFT));
        _cbxLaunchCase = new JCheckBox("Launch New Case");
        _cbxLaunchCase.setBorder(new EmptyBorder(0,7,10,0));
        _cbxLaunchCase.setAlignmentX(LEFT_ALIGNMENT);
        _cbxLaunchCase.setMnemonic('L');
        content.add(_cbxLaunchCase);
        return content;
    }


    private void loadPreferences() {
        _cbxCancelCases.setSelected(UserSettings.getCancelCasesOnUpload());
        _cbxLaunchCase.setSelected(UserSettings.getLaunchCaseOnUpload());
        _cbxUnloadPrevious.setSelected(UserSettings.getUnloadPreviousOnUpload());
        _cbxCancelCases.setEnabled(_cbxUnloadPrevious.isSelected());
        getOKButton().setEnabled(true);
    }


    private void savePreferences() {
        UserSettings.setCancelCasesOnUpload(_cbxCancelCases.isSelected());
        UserSettings.setLaunchCaseOnUpload(_cbxLaunchCase.isSelected());
        UserSettings.setUnloadPreviousOnUpload(_cbxUnloadPrevious.isSelected());
    }


    private void upload() {
        YAWLEditor editor = YAWLEditor.getInstance();
        editor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        String message;
        int msgType = JOptionPane.INFORMATION_MESSAGE;
        SpecificationUploader uploader = new SpecificationUploader();
        try {
            String result = uploader.upload(_cbxUnloadPrevious.isSelected(),
                    _cbxCancelCases.isSelected());
            String errorMsg = processUploadResult(result);
            if (errorMsg.isEmpty()) {
                uploader.storeLayout();
                message = "Specification uploaded successfully." + launchCase(uploader);
            }
            else {
                message = errorMsg;
                msgType = JOptionPane.ERROR_MESSAGE;
            }
        }
        catch (IOException ioe) {
            msgType = JOptionPane.ERROR_MESSAGE;
            message = unwrap(ioe.getMessage());
            if (message.equals("Invalid Specification")) {
                message += ". Please resolve the issues listed in the " +
                        "\n'Validation Results' pane below before retrying an upload.";
            }
        }
        String title = "Upload " + (msgType == JOptionPane.ERROR_MESSAGE ?
                "Error" : "Success");
        editor.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        JOptionPane.showMessageDialog(YAWLEditor.getInstance(), message, title, msgType);
    }


    private String launchCase(SpecificationUploader uploader) throws IOException {
        if (_cbxLaunchCase.isEnabled() && _cbxLaunchCase.isSelected()) {
            String caseID = uploader.launchCase();
            if (caseID != null) {
                return caseID.contains("fail") ?
                    "\n\nCase launch failed: " + StringUtil.unwrap(caseID) :
                    "\n\nNew case launched with id: " + caseID;
            }
        }
        return "";
    }


    private String processUploadResult(String result) {
        if (result.contains("fail")) {
            XNode msgNode = new XNodeParser().parse(result);
            String errMsg = msgNode.getText();

            // simple error message - return immediately
            return errMsg != null ? unwrap(errMsg) :
                    processUploadValidationResult(msgNode);
        }
        return "";                                 // no errors
    }


    private String processUploadValidationResult(XNode msgNode) {
        StringBuilder s = new StringBuilder();
        XNode reason = msgNode.getChild();
        XNode messages = reason.getChild();

        // only interested in errors
        for (XNode errorNode : messages.getChildren("error")) {
            XNode message = errorNode.getChild("message");
            s.append(message.getText()).append('\n');
        }
        return s.toString();
    }


    private String unwrap(String xml) {
        return xml.startsWith("<") ? StringUtil.unwrap(xml) : xml;

    }

}
