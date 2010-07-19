package org.yawlfoundation.yawl.editor.actions.specification;

import org.yawlfoundation.yawl.editor.swing.JFormattedSelectField;
import org.yawlfoundation.yawl.elements.YSpecVersion;

import javax.swing.*;
import javax.swing.text.Document;


public class SpecificationVersionVerifier extends InputVerifier {

  private YSpecVersion _origVersion;

  public SpecificationVersionVerifier(YSpecVersion origVersion) {
    super();
    _origVersion = origVersion;
  }

  public void setStartingVersion(YSpecVersion value) {
      _origVersion = value;
  }

    public String decStartingVersion() { return _origVersion.minorRollback(); }

    public String incStartingVersion() { return _origVersion.minorIncrement(); }


  public boolean verify(JComponent component) {
    assert component instanceof JFormattedSelectField;
    JFormattedSelectField field = (JFormattedSelectField) component;

    try {
      Document doc = field.getDocument();
      String docContent = doc.getText(0,doc.getLength());

       // version nbrs match doubles in format - an exception here means a bad format
      new Double(docContent);

      // no exception - ok, convert to a version number & compare with original
      return (new YSpecVersion(docContent).compareTo(_origVersion) >= 0);

    }
    catch (Exception e) {
        return false;
    }
  }


  public boolean shouldYieldFocus(JComponent component) {
    boolean isValid = verify(component);
    JFormattedSelectField field = (JFormattedSelectField) component;
    if (!isValid) {
      field.invalidEdit();
    }
    return isValid;
  }
}