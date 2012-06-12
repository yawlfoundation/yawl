package org.yawlfoundation.yawl.editor.ui.actions.specification;

import org.yawlfoundation.yawl.editor.ui.swing.JFormattedAlphaNumericField;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;

import javax.swing.*;
import javax.swing.text.Document;


public class SpecificationIdVerifier extends InputVerifier {

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
    } catch (Exception e) {}

    if (docContent == null || docContent.equals("")) {
      return false;
    }
    return XMLUtilities.isValidXMLName(docContent.trim().replace(' ', '_'));

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
