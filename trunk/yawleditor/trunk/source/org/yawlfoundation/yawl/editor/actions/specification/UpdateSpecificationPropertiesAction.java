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

package org.yawlfoundation.yawl.editor.actions.specification;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.util.Date;

import javax.swing.Action;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.actions.specification.YAWLOpenSpecificationAction;
import org.yawlfoundation.yawl.editor.foundations.XMLUtilities;
import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;
import org.yawlfoundation.yawl.editor.swing.TooltipTogglingWidget;

import org.yawlfoundation.yawl.editor.swing.JFormattedDateField;
import org.yawlfoundation.yawl.editor.swing.JFormattedAlphaNumericField;
import org.yawlfoundation.yawl.editor.swing.JFormattedNumberField;
import org.yawlfoundation.yawl.editor.swing.JFormattedSafeXMLCharacterField;

public class UpdateSpecificationPropertiesAction extends YAWLOpenSpecificationAction 
                                                 implements TooltipTogglingWidget {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final UpdateSpecificationPropertiesDialog dialog = 
    new UpdateSpecificationPropertiesDialog();

  private boolean invokedAtLeastOnce = false;
  
  {
    putValue(Action.SHORT_DESCRIPTION, getDisabledTooltipText());
    putValue(Action.NAME, "Update Specification Properties");
    putValue(Action.LONG_DESCRIPTION, "Update Specification Properties");
    putValue(Action.SMALL_ICON, getIconByName("Blank"));
    putValue(Action.MNEMONIC_KEY, new Integer(java.awt.event.KeyEvent.VK_U));
  }
  
  public void actionPerformed(ActionEvent event) {
    if (!invokedAtLeastOnce) {
      dialog.setLocationRelativeTo(YAWLEditor.getInstance());
      invokedAtLeastOnce = true;       
    }
    dialog.setVisible(true);
  }
  
  public String getEnabledTooltipText() {
    return " Update specification properties ";
  }
  
  public String getDisabledTooltipText() {
    return " You must have an open specification" + 
           " to update its properties ";
  }
}

class UpdateSpecificationPropertiesDialog extends AbstractDoneDialog {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private static final SpecificationIdVerifier SPEC_ID_VERIFIER = 
    new SpecificationIdVerifier();

  private JFormattedAlphaNumericField specificationIDField;
  private JFormattedSafeXMLCharacterField specificationNameField;
  private JFormattedSafeXMLCharacterField specificationDescriptionField;
  private JFormattedSafeXMLCharacterField specificationAuthorField;

  // Administration property widgets
  
  private JFormattedNumberField versionNumberField;
  private TimeStampPanel validFromPanel;
  private TimeStampPanel validUntilPanel;
  
  public UpdateSpecificationPropertiesDialog() {
    super("Update Specification Properties", true);
    
    setContentPanel(getPropertiesPanel());
    
    getDoneButton().addActionListener( 
      new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          SpecificationModel.getInstance().setName(specificationNameField.getText());
          SpecificationModel.getInstance().setDescription(specificationDescriptionField.getText());
          SpecificationModel.getInstance().setId(specificationIDField.getText());
          SpecificationModel.getInstance().setAuthor(specificationAuthorField.getText());
          SpecificationModel.getInstance().setVersionNumber(versionNumberField.getDouble());
          SpecificationModel.getInstance().setValidFromTimestamp(validFromPanel.getTimestamp());
          SpecificationModel.getInstance().setValidUntilTimestamp(validUntilPanel.getTimestamp());
        }
      }
    );
    
    this.getRootPane().setDefaultButton(getDoneButton());
  }

  protected void makeLastAdjustments() {
    pack();
    setResizable(false);
  }
  
  private JPanel getPropertiesPanel() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();

    JPanel panel = new JPanel(gbl);
    panel.setBorder(new EmptyBorder(12,12,5,11));

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0,0,5,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel idLabel = new JLabel("Specification ID :");
    idLabel.setDisplayedMnemonic('I');
    panel.add(idLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getSpecificationIDField(), gbc);
    idLabel.setLabelFor(specificationIDField);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.insets = new Insets(5,0,5,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel nameLabel = new JLabel("Specification Name:");
    nameLabel.setDisplayedMnemonic('N');
    panel.add(nameLabel, gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getSpecificationNameField(), gbc);
    nameLabel.setLabelFor(specificationNameField);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.insets = new Insets(5,0,5,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel descriptionLabel = new JLabel("Specification Description:");
    nameLabel.setDisplayedMnemonic('D');
    panel.add(descriptionLabel, gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getSpecificationDescriptionField(), gbc);
    descriptionLabel.setLabelFor(specificationDescriptionField);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.insets = new Insets(5,0,5,5);
    gbc.anchor = GridBagConstraints.EAST;

    JLabel authorLabel = new JLabel("Specification Author:");
    nameLabel.setDisplayedMnemonic('A');
    panel.add(authorLabel, gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    panel.add(getSpecificationAuthorField(), gbc);
    authorLabel.setLabelFor(specificationAuthorField);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    panel.add(new JSeparator(), gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.EAST;
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
    gbc.anchor = GridBagConstraints.EAST;

    JLabel validFromLabel = new JLabel("Specification valid from:");
    validFromLabel.setDisplayedMnemonicIndex(21);
    
    panel.add(validFromLabel, gbc);
    
    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    validFromPanel = new TimeStampPanel();
    panel.add(validFromPanel,gbc);
    
    gbc.gridx = 0;
    gbc.gridy++;
    gbc.insets = new Insets(10,0,0,0);
    gbc.anchor = GridBagConstraints.EAST;
    
    JLabel validUntilLabel = new JLabel("Specification valid until:");
    validUntilLabel.setDisplayedMnemonic('u');
    
    panel.add(validUntilLabel, gbc);

    gbc.gridx++;
    gbc.anchor = GridBagConstraints.WEST;

    validUntilPanel = new TimeStampPanel();
    panel.add(validUntilPanel,gbc);
    
    return panel;
  }

  public void setVisible(boolean state) {
    if (state) {
      specificationNameField.setText(
        SpecificationModel.getInstance().getName()    
      );
      specificationDescriptionField.setText(
          SpecificationModel.getInstance().getDescription()    
      );
      specificationIDField.setText(
          SpecificationModel.getInstance().getId()    
      );
      specificationAuthorField.setText(
          SpecificationModel.getInstance().getAuthor()    
      );
      versionNumberField.setDouble(
          SpecificationModel.getInstance().getVersionNumber()    
      );
      versionNumberField.setLowerBound(
          SpecificationModel.getInstance().getVersionNumber()    
      );
      validFromPanel.setTimestamp(SpecificationModel.getInstance().getValidFromTimestamp());
      validUntilPanel.setTimestamp(SpecificationModel.getInstance().getValidUntilTimestamp());
    } 
    super.setVisible(state);
  }

  private JFormattedAlphaNumericField getSpecificationIDField() {
    specificationIDField = new JFormattedAlphaNumericField(20);

    specificationIDField.setInputVerifier(SPEC_ID_VERIFIER);
    specificationIDField.allowXMLNames();
    specificationIDField.addKeyListener(new SpecificationIdFieldDocumentListener());

    
    specificationIDField.setToolTipText(" Enter the unique engine identifier (XML element name) for this specification ");
    return specificationIDField;
  }

  private JFormattedSafeXMLCharacterField getSpecificationNameField() {
    specificationNameField = new JFormattedSafeXMLCharacterField(20);
    specificationNameField.setToolTipText(" Enter a human-friendly name for this specification ");
    return specificationNameField;
  }
  
  private JFormattedSafeXMLCharacterField getSpecificationDescriptionField() {
    specificationDescriptionField = new JFormattedSafeXMLCharacterField(40);
    specificationDescriptionField.setToolTipText(" Enter a description of what this specification intends to do overall ");
    return specificationDescriptionField;
  }

  private JFormattedSafeXMLCharacterField getSpecificationAuthorField() {
    specificationAuthorField = new JFormattedSafeXMLCharacterField(20);
    specificationAuthorField.setToolTipText(" Enter the author's name for this specification ");
    return specificationAuthorField;
  }
  
  private JFormattedNumberField getVersionNumberField() {
    versionNumberField = new JFormattedNumberField("###,###,##0.0###",0.1,10);
    versionNumberField.setToolTipText(" Enter a version number for this specification ");
    return versionNumberField;
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
      return specificationIDField.getInputVerifier().verify(specificationIDField);
    }
  }
}

class TimeStampPanel extends JPanel {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private JRadioButton alwaysButton;
  private JRadioButton timestampButton;
  
  private JFormattedDateField dateField;
  
  private ButtonGroup buttonGroup = new ButtonGroup();
  
  public TimeStampPanel() {
     super();
     createPanelContent();
   }
   
   private void createPanelContent() {
     setLayout(new GridBagLayout());
     GridBagConstraints gbc = new GridBagConstraints();

     gbc.gridx = 0;
     gbc.gridy = 0;
     gbc.insets = new Insets(0,2,0,3);
     gbc.anchor = GridBagConstraints.WEST;
     
     createAlwaysButton();
     add(alwaysButton, gbc);
     
     gbc.gridy++;

     createTimestampButton();
     add(timestampButton, gbc);

     gbc.anchor = GridBagConstraints.CENTER;
     gbc.gridx++;

     createDateField();
     add(dateField, gbc);
     
     buttonGroup.add(alwaysButton);
     buttonGroup.add(timestampButton);
     
     setWidgetsCorrectly();
  }
   
   private void createAlwaysButton() {
     alwaysButton = new JRadioButton("always");
     alwaysButton.setToolTipText(" Indicate that specification validity is unconstrained in time here ");

     alwaysButton.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         setWidgetsCorrectly();  
       }
     });
     alwaysButton.setSelected(true);
   }
   
   private void createTimestampButton() {
     timestampButton = new JRadioButton("the date of");
     timestampButton.setToolTipText(" Indicate that specification validity is fixed in time here ");
     timestampButton.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         setWidgetsCorrectly();  
       }
     });
   }
   
   private void setWidgetsCorrectly() {
     dateField.setEnabled(false);
     if (timestampButton.isSelected()) {
       dateField.setEnabled(true);
     }
   }
   
   private void createDateField() {
     dateField = new JFormattedDateField("dd/MM/yyyy",10);
     dateField.setToolTipText(" Specify a date limit in the form of 'dd/mm/yyyy' here ");
   }
   
   public void setTimestamp(String timestamp) {
     if (timestamp == null || timestamp.trim().equals("")) {
       buttonGroup.setSelected(alwaysButton.getModel(), true);
       dateField.setDate(new Date());  // today
     } else {
       dateField.setDateViaTimestamp(timestamp.substring(0,8));
       buttonGroup.setSelected(timestampButton.getModel(), true);
     }
     setWidgetsCorrectly();
   }
   
   public String getTimestamp() {
     if (alwaysButton.isSelected()) {
       return "";
     }
     return dateField.getTimestamp();
   }
}

class SpecificationIdVerifier extends InputVerifier {

  public SpecificationIdVerifier() {
    super();
  }

  public boolean verify(JComponent component) {
    assert component instanceof JFormattedAlphaNumericField;
    JFormattedAlphaNumericField field = (JFormattedAlphaNumericField) component;

    String docContent = null;
    try {
      Document doc = field.getDocument();
      docContent = doc.getText(0,doc.getLength());
    } catch (Exception e) {};

    if (docContent == null || docContent.equals("")) {
      return false;
    }
    if (XMLUtilities.isValidXMLName(docContent.trim().replace(' ','_'))) {
      return true;
    }
    
    return false;
  }

  public boolean shouldYieldFocus(JComponent component) {
    boolean isValid = verify(component);
    JFormattedAlphaNumericField field = (JFormattedAlphaNumericField) component;
    if (!isValid) {
      field.invalidEdit();
    } 
    return isValid;
  }
}

