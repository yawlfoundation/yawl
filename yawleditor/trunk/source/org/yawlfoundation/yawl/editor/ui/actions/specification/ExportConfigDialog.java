package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.specification.ArchivingThread;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.ui.swing.JFormattedAlphaNumericField;
import org.yawlfoundation.yawl.editor.ui.swing.JFormattedSelectField;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.elements.YSpecVersion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * Author: Michael Adams
 * Creation Date: 15/10/2008
 */
class ExportConfigDialog extends AbstractDoneDialog {

  private JFormattedAlphaNumericField specificationIDField;
  private JFormattedSelectField versionNumberField;
  private JCheckBox verificationCheckBox;
  private JCheckBox analysisCheckBox;
  private JCheckBox autoIncVersionCheckBox;
  private JCheckBox backupCheckBox;
  private JCheckBox versionCopyCheckBox;
  private JLabel idLabel;
  private boolean initialising;

    public ExportConfigDialog() {
        super("Specification File Save Options", true);

        setContentPanel(getFontSizePanel());
        getDoneButton().setText("OK");
        getDoneButton().addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        UserSettings.setVerifyOnSave(verificationCheckBox.isSelected());
                        UserSettings.setAnalyseOnSave(analysisCheckBox.isSelected());
                        UserSettings.setAutoIncrementVersionOnSave(
                                autoIncVersionCheckBox.isSelected());
                        UserSettings.setFileBackupOnSave(backupCheckBox.isSelected());
                        UserSettings.setFileVersioningOnSave(versionCopyCheckBox.isSelected());

                        SpecificationModel.getInstance().setVersionNumber(
                                new YSpecVersion(versionNumberField.getText()));

                        if (showSpecIDField()) {
                            SpecificationModel.getInstance().setId(specificationIDField.getText());
                        }

                        ArchivingThread.getInstance().engineFileExport(
                                SpecificationModel.getInstance()
                        );
                    }
                }
        );
    }

  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }

  private JPanel getFontSizePanel() {

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,0,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,5,5,5);
    gbc.anchor = GridBagConstraints.EAST;

    if (showSpecIDField()) {
        idLabel = new JLabel("Specification ID :");
        idLabel.setDisplayedMnemonic('S');
        panel.add(idLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;

        panel.add(getSpecificationIDField(), gbc);
        idLabel.setLabelFor(specificationIDField);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;

        getDoneButton().setEnabled(false);
    }

    gbc.fill = GridBagConstraints.NONE;

    JLabel versionNumberLabel = new JLabel("Version Number:");
    versionNumberLabel.setDisplayedMnemonic('V');
    panel.add(versionNumberLabel, gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getVersionNumberField(), gbc);
    versionNumberLabel.setLabelFor(versionNumberField);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;
    gbc.weightx = 1;

    panel.add(getVerifyCheckBox(), gbc);

    gbc.gridy++;

    panel.add(getAnalysisCheckBox(), gbc);

    gbc.gridy++;

    panel.add(getAutoIncVersionCheckBox(), gbc);

    gbc.gridy++;

   panel.add(getBackupCheckBox(), gbc);

    gbc.gridy++;

   panel.add(getVersionCopyCheckBox(), gbc);

    return panel;
  }


    private JFormattedAlphaNumericField getSpecificationIDField() {
      specificationIDField = new JFormattedAlphaNumericField(10);

      specificationIDField.setInputVerifier(new SpecificationIdVerifier());
      specificationIDField.addKeyListener(new SpecificationIdFieldDocumentListener());
      specificationIDField.setToolTipText(" Enter the unique engine identifier (XML element name) for this specification ");
      return specificationIDField;
    }


    private JFormattedSelectField getVersionNumberField() {
    versionNumberField = new JFormattedSelectField(10);
    versionNumberField.setInputVerifier(
            new SpecificationVersionVerifier(
                    SpecificationModel.getInstance().getVersionNumber()));
    versionNumberField.addKeyListener(new SpecificationVersionFieldDocumentListener());
    versionNumberField.setToolTipText(" Enter a version number for this specification ");
    return versionNumberField;
  }

  private JCheckBox getVerifyCheckBox() {
    verificationCheckBox = new JCheckBox();

    verificationCheckBox.setText("Verify on save");
    verificationCheckBox.setToolTipText(" Check the model for errors ");
    verificationCheckBox.setMnemonic(KeyEvent.VK_E);

    return verificationCheckBox;
  }

  private JCheckBox getAnalysisCheckBox() {
    analysisCheckBox = new JCheckBox();

    analysisCheckBox.setText("Analyse on save");
    analysisCheckBox.setToolTipText(" Perform a full analysis of the model ");
    analysisCheckBox.setMnemonic(KeyEvent.VK_A);

    return analysisCheckBox;
  }

  private JCheckBox getAutoIncVersionCheckBox() {
    autoIncVersionCheckBox = new JCheckBox();

    autoIncVersionCheckBox.setText("Auto Increment Minor Version Number");
    autoIncVersionCheckBox.setToolTipText(" Increment version number for each save ");
    autoIncVersionCheckBox.setMnemonic(KeyEvent.VK_I);

    autoIncVersionCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
            if (! initialising) {
                SpecificationVersionVerifier verifier =
                    (SpecificationVersionVerifier) versionNumberField.getInputVerifier();
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    if (! versionNumberField.getText().equals("0.1")) {
                        versionNumberField.setText(verifier.decStartingVersion());
                    }    
                }
                else if (e.getStateChange() == ItemEvent.SELECTED) {
                    versionNumberField.setText(verifier.incStartingVersion());
                }    
            }
        }
    });

    return autoIncVersionCheckBox;
  }

    private JCheckBox getBackupCheckBox() {
      backupCheckBox = new JCheckBox();
      backupCheckBox.setText("Create backup");
      backupCheckBox.setToolTipText(" Keep the previous copy of this file ");
      backupCheckBox.setMnemonic(KeyEvent.VK_B);
      return backupCheckBox;
    }

    private JCheckBox getVersionCopyCheckBox() {
      versionCopyCheckBox = new JCheckBox();
      versionCopyCheckBox.setText("File Versioning");
      versionCopyCheckBox.setToolTipText(" Save all previous versions of this file ");
      versionCopyCheckBox.setMnemonic(KeyEvent.VK_F);
      return versionCopyCheckBox;
    }


  private boolean showSpecIDField() {
      String id = SpecificationModel.getInstance().getId();
      return (id == null) || (id.length() == 0) || (id.equals("unnamed.ywl"));
  }

  public void showOrHideSpecIDField() {
      boolean showField = showSpecIDField();
      if (idLabel != null)
          idLabel.setVisible(showField);
      if (specificationIDField != null)
          specificationIDField.setVisible(showField);
      getDoneButton().setEnabled(! showField);
  }

  public void setVisible(boolean visible) {
    if (visible) {
        initialising = true;
        verificationCheckBox.setSelected(UserSettings.getVerifyOnSave());
        analysisCheckBox.setSelected(UserSettings.getAnalyseOnSave());
        autoIncVersionCheckBox.setSelected(UserSettings.getAutoIncrementVersionOnSave());
        backupCheckBox.setSelected(UserSettings.getFileBackupOnSave());
        versionCopyCheckBox.setSelected(UserSettings.getFileVersioningOnSave());

      String verStr = SpecificationModel.getInstance().getVersionNumber().toString();
      YSpecVersion version = new YSpecVersion(verStr);

      if (autoIncVersionCheckBox.isSelected() || verStr.equals("0.0")) {
          version.minorIncrement();
      }
      versionNumberField.setText(version.toString());
      SpecificationVersionVerifier svv =
              (SpecificationVersionVerifier) versionNumberField.getInputVerifier();
      svv.setStartingVersion(version);

      initialising = false;
    }
    super.setVisible(visible);
  }


  class SpecificationIdFieldDocumentListener implements KeyListener {

    public void keyPressed(KeyEvent e) {
      // deliberately does nothing
    }

    public void keyTyped(KeyEvent e) {
      // deliberately does nothing
    }

    public void keyReleased(KeyEvent e) {
      getDoneButton().setEnabled(nameFieldValid());
    }

    private boolean nameFieldValid() {
      return specificationIDField.getInputVerifier().verify(specificationIDField) &&
             ! specificationIDField.getText().equals("unnamed.ywl") ;
    }
  }

  class SpecificationVersionFieldDocumentListener implements KeyListener {

     public void keyPressed(KeyEvent e) {
       // deliberately does nothing
     }

     public void keyTyped(KeyEvent e) {
       // deliberately does nothing
     }

     public void keyReleased(KeyEvent e) {
       getDoneButton().setEnabled(versionFieldValid());
     }

     private boolean versionFieldValid() {
       return versionNumberField.getInputVerifier().verify(versionNumberField);
     }
   }


}
