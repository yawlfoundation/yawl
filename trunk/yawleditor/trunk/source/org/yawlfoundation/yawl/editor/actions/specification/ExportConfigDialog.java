package org.yawlfoundation.yawl.editor.actions.specification;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.specification.ArchivingThread;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.swing.JFormattedAlphaNumericField;
import org.yawlfoundation.yawl.editor.swing.JFormattedSelectField;
import org.yawlfoundation.yawl.editor.thirdparty.engine.EngineSpecificationExporter;
import org.yawlfoundation.yawl.elements.YSpecVersion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.prefs.Preferences;

/**
 * Author: Michael Adams
 * Creation Date: 15/10/2008
 */
class ExportConfigDialog extends AbstractDoneDialog {
  /**
   *
   */

  protected static final Preferences prefs =  Preferences.userNodeForPackage(YAWLEditor.class);

  private static final long serialVersionUID = 1L;

  private JFormattedAlphaNumericField specificationIDField;
  private JFormattedSelectField versionNumberField;
  private JCheckBox verificationCheckBox;
  private JCheckBox analysisCheckBox;
  private JCheckBox autoIncVersionCheckBox;
  private JCheckBox backupCheckBox;
  private JLabel idLabel;

  public ExportConfigDialog() {
    super("Specification File Save Options", true);

    setContentPanel(getFontSizePanel());
    getDoneButton().setText("OK");
    getDoneButton().addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            prefs.putBoolean(
                EngineSpecificationExporter.VERIFICATION_WITH_EXPORT_PREFERENCE,
                verificationCheckBox.isSelected()
            );
            prefs.putBoolean(
                EngineSpecificationExporter.ANALYSIS_WITH_EXPORT_PREFERENCE,
                analysisCheckBox.isSelected()
            );
            prefs.putBoolean(
                EngineSpecificationExporter.AUTO_INCREMENT_VERSION_WITH_EXPORT_PREFERENCE,
                autoIncVersionCheckBox.isSelected()
            );
              prefs.putBoolean(
                  EngineSpecificationExporter.FILE_BACKUP_PREFERENCE,
                  backupCheckBox.isSelected()
              );

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

    return panel;
  }


    private JFormattedAlphaNumericField getSpecificationIDField() {
      specificationIDField = new JFormattedAlphaNumericField(10);

      specificationIDField.setInputVerifier(new SpecificationIdVerifier());
 //     specificationIDField.allowXMLNames();
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
    verificationCheckBox.setMnemonic(KeyEvent.VK_E);

    return verificationCheckBox;
  }

  private JCheckBox getAnalysisCheckBox() {
    analysisCheckBox = new JCheckBox();

    analysisCheckBox.setText("Analyse on save");
    analysisCheckBox.setMnemonic(KeyEvent.VK_A);

    return analysisCheckBox;
  }

  private JCheckBox getAutoIncVersionCheckBox() {
    autoIncVersionCheckBox = new JCheckBox();

    autoIncVersionCheckBox.setText("Auto Increment Minor Version Number");
    autoIncVersionCheckBox.setMnemonic(KeyEvent.VK_I);

    autoIncVersionCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
            SpecificationVersionVerifier verifier =
                    (SpecificationVersionVerifier) versionNumberField.getInputVerifier();
            if (e.getStateChange() == ItemEvent.DESELECTED) {
                versionNumberField.setText(verifier.decStartingVersion());
            }
            else if (e.getStateChange() == ItemEvent.SELECTED) {
                versionNumberField.setText(verifier.incStartingVersion());
            }
        }
    });

    return autoIncVersionCheckBox;
  }

    private JCheckBox getBackupCheckBox() {
      backupCheckBox = new JCheckBox();
      backupCheckBox.setText("Create backup");
      backupCheckBox.setMnemonic(KeyEvent.VK_B);
      return backupCheckBox;
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
      verificationCheckBox.setSelected(
          prefs.getBoolean(
              EngineSpecificationExporter.VERIFICATION_WITH_EXPORT_PREFERENCE,
              true
          )
      );
      analysisCheckBox.setSelected(
          prefs.getBoolean(
              EngineSpecificationExporter.ANALYSIS_WITH_EXPORT_PREFERENCE,
              true
          )
      );
      autoIncVersionCheckBox.setSelected(
          prefs.getBoolean(
              EngineSpecificationExporter.AUTO_INCREMENT_VERSION_WITH_EXPORT_PREFERENCE,
              true
          )
      );
      backupCheckBox.setSelected(
            prefs.getBoolean(
                EngineSpecificationExporter.FILE_BACKUP_PREFERENCE,
                true
            )
        );


      String verStr = SpecificationModel.getInstance().getVersionNumber().toString();
      YSpecVersion version = new YSpecVersion(verStr);

      if (autoIncVersionCheckBox.isSelected() || verStr.equals("0.0")) {
          version.minorIncrement();
      }
      versionNumberField.setText(version.toString());
      SpecificationVersionVerifier svv =
              (SpecificationVersionVerifier) versionNumberField.getInputVerifier();
      svv.setStartingVersion(version);

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
