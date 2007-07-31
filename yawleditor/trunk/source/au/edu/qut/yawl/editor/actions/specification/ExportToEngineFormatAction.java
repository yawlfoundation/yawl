/*
 * Created on 9/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package au.edu.qut.yawl.editor.actions.specification;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import au.edu.qut.yawl.editor.YAWLEditor;
import au.edu.qut.yawl.editor.actions.specification.YAWLOpenSpecificationAction;
import au.edu.qut.yawl.editor.specification.ArchivingThread;
import au.edu.qut.yawl.editor.specification.SpecificationModel;
import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;
import au.edu.qut.yawl.editor.swing.TooltipTogglingWidget;
import au.edu.qut.yawl.editor.thirdparty.engine.EngineSpecificationExporter;

public class ExportToEngineFormatAction extends YAWLOpenSpecificationAction implements TooltipTogglingWidget {

  protected static final Preferences prefs =  Preferences.userNodeForPackage(YAWLEditor.class);
  public static final String SHOW_EXPORT_DIALOG_PREFERENCE = "showExportDialog";
  boolean shouldShowExportDialog = true;
  private boolean isDialogShownPreviously = false;
 
  ExportConfigDialog dialog = new ExportConfigDialog();
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Export to YAWL Engine file...");
    putValue(Action.LONG_DESCRIPTION, "Export this specification to the YAWL engine file format. ");
    putValue(Action.SMALL_ICON, getIconByName("Export"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_X));
  }
  
  public void actionPerformed(ActionEvent event) {
    shouldShowExportDialog = prefs.getBoolean(SHOW_EXPORT_DIALOG_PREFERENCE, true);
    if (shouldShowExportDialog) {
      if (!isDialogShownPreviously) {
        dialog.setLocationRelativeTo(YAWLEditor.getInstance());
        isDialogShownPreviously = true;       
      }
      dialog.setVisible(true);
    } else {
      ArchivingThread.getInstance().engineFileExport(
          SpecificationModel.getInstance()    
      );
    }
  }
  
  public String getEnabledTooltipText() {
    return " Export this specification to the YAWL engine file format ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have an open specification" + 
           " to export it to engine format ";
  }
}


class ExportConfigDialog extends AbstractDoneDialog {
  /**
   * 
   */
  
  protected static final Preferences prefs =  Preferences.userNodeForPackage(YAWLEditor.class);

  private static final long serialVersionUID = 1L;
  protected JSpinner labelFontSizeSpinner;
  
  private JCheckBox verificationCheckBox;
  private JCheckBox analysisCheckBox;
  private JCheckBox showDialogCheckBox;
  
  public ExportConfigDialog() {
    super("Configure Export Behaviour", true);

    setContentPanel(getFontSizePanel());
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
                ExportToEngineFormatAction.SHOW_EXPORT_DIALOG_PREFERENCE, 
                showDialogCheckBox.isSelected()
            );
            
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
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getVerifyCheckBox(), gbc);
     
    gbc.gridy++;
    
    panel.add(getAnalysisCheckBox(), gbc);

    gbc.gridy++;
    gbc.insets = new Insets(15,5,5,5);
    
    panel.add(getShowDialogCheckBox(), gbc);
    
    return panel;
  }
  
  private JCheckBox getVerifyCheckBox() {
    verificationCheckBox = new JCheckBox();

    verificationCheckBox.setText("Verify on export");
    verificationCheckBox.setMnemonic(KeyEvent.VK_V);

    return verificationCheckBox;
  }
  
  private JCheckBox getAnalysisCheckBox() {
    analysisCheckBox = new JCheckBox();

    analysisCheckBox.setText("Analyse on export");
    analysisCheckBox.setMnemonic(KeyEvent.VK_A);

    return analysisCheckBox;
  }

  private JCheckBox getShowDialogCheckBox() {
    showDialogCheckBox = new JCheckBox();

    showDialogCheckBox.setText("Show this dialog in the future");
    showDialogCheckBox.setMnemonic(KeyEvent.VK_S);

    return showDialogCheckBox;
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
      showDialogCheckBox.setSelected(true);
    }
    super.setVisible(visible);
  }
}
