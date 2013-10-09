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
