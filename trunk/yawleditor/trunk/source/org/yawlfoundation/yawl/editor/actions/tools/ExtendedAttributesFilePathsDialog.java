package org.yawlfoundation.yawl.editor.actions.tools;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * Author: Michael Adams
 * Creation Date: 20/04/2010
 */
public class ExtendedAttributesFilePathsDialog extends AbstractDoneDialog {

    private static final long serialVersionUID = 1L;

    private static final Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);

    private JTextField _fldDecomposition;
    private JTextField _fldVariable;


    public ExtendedAttributesFilePathsDialog() {
        super("Extended Attributes File Settings", true);
        setContentPanel(buildContentPanel());
        setSize(new Dimension(600, 230));
        setResizable(false);

        getDoneButton().addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
               prefs.put("ExtendedAttributeDecompositionFilePath", _fldDecomposition.getText());
               prefs.put("ExtendedAttributeVariableFilePath", _fldVariable.getText());
           }
        });
    }


    private JPanel buildContentPanel() {
        _fldVariable = new JTextField(getVariablePath());
        _fldDecomposition = new JTextField(getDecompositionPath());

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel filePanel = buildFileEntryPanel(_fldDecomposition, "Decomposition");
        panel.add(filePanel, BorderLayout.NORTH);
        filePanel = buildFileEntryPanel(_fldVariable, "Variable");
        panel.add(filePanel, BorderLayout.SOUTH);
        return panel;
    }


    private JPanel buildFileEntryPanel(JTextField textField, String name) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder(name + " Properties"));
        panel.add(buildFileButton(name), BorderLayout.EAST);
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }

    private JButton buildFileButton(String cmd) {
        JButton button = new JButton("...");
        button.setSize(new Dimension(15, 15));
        button.setActionCommand(cmd);

        final JDialog thisDialog = this;
        button.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent e) {
               String cmd = e.getActionCommand();
               String title = "Select " + cmd + " Attributes File";
               FileDialog fileDialog = new FileDialog(thisDialog, title);
               fileDialog.setDirectory(getInitialDir(cmd));
               fileDialog.setVisible(true);
               String fileName = fileDialog.getFile();
               if (fileName != null) {
                   String path = fileDialog.getDirectory() + fileName;
                   if (cmd.equals("Variable")) {
                       _fldVariable.setText(path);
                   }
                   else _fldDecomposition.setText(path);
               }
           }
        });
        return button;
    }


    private String getDecompositionPath() {
        return prefs.get("ExtendedAttributeDecompositionFilePath", Decomposition.PROPERTY_LOCATION);
    }

    private String getVariablePath() {
        return prefs.get("ExtendedAttributeVariableFilePath", DataVariable.PROPERTY_LOCATION);
    }

    private String getInitialDir(String type) {
        String path = (type.equals("Variable")) ? getVariablePath() : getDecompositionPath();
        return path.substring(0, path.lastIndexOf(File.separator));
    }

}
