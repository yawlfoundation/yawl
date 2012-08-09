/*
 * Created on 23/01/2005
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2004-5 Queensland University of Technology
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

package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.editor.ui.data.Decomposition;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUtilities;
import org.yawlfoundation.yawl.editor.ui.util.XMLUtilities;

import javax.swing.*;
import javax.swing.text.Document;

public class DecompositionLabelField extends XMLElementField {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private Decomposition decomposition;
  
  private final DecompositionLabelVerifier verifier = new DecompositionLabelVerifier();
  
  public DecompositionLabelField(int columns) {
    super(columns);
    setInputVerifier(verifier);
    getAlphaNumericFormatter().allowSpaces();
  }
  
  public void setDecomposition(Decomposition decomposition) {
    this.decomposition = decomposition;
    this.setText(decomposition.getLabel());
  }
  
  public Decomposition getDecomposition() {
    return this.decomposition;
  }
}

class DecompositionLabelVerifier extends InputVerifier {

  public DecompositionLabelVerifier() {
    super();
  }

  public boolean verify(JComponent component) {
    assert component instanceof DecompositionLabelField;
    DecompositionLabelField field = (DecompositionLabelField) component;

    String docContent = null;
    try {
      Document doc = field.getDocument();
      docContent = doc.getText(0,doc.getLength());
    } catch (Exception e) {}

    if (docContent == null || docContent.equals("")) {
      return false;
    }
    
    if (!XMLUtilities.isValidXMLName(docContent.trim().replace(' ','_'))) {
      return false;
    }

    if ((SpecificationUtilities.isValidDecompositionLabel(SpecificationModel.getInstance(), docContent))) {
      return true;
    }

      return field.getDecomposition() != null && field.getDecomposition().getLabel().equals(docContent);

  }

  public boolean shouldYieldFocus(JComponent component) {
    boolean isValid = verify(component);
    DecompositionLabelField field = (DecompositionLabelField) component;
    if (!isValid) {
      field.invalidEdit();
    } 
    return isValid;
  }
}


