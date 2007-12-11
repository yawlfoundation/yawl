/*
 * Created on 16/05/2004
 * YAWLEditor v1.01
 *
 * @author Lindsay Bradford
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
 */


package au.edu.qut.yawl.editor.swing.data;

import java.util.List;

import au.edu.qut.yawl.editor.thirdparty.engine.YAWLEngineProxy;

public class JXMLSchemaEditorPane extends JProblemReportingEditorPane {
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public JXMLSchemaEditorPane() {
    super(new JXMLSchemaEditor());
  }
}

class JXMLSchemaEditor extends ValidityEditorPane {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public JXMLSchemaEditor() {
    super();
    setDocument(
        new XMLSchemaStyledDocument(this)
    );
  }

  class XMLSchemaStyledDocument extends AbstractXMLStyledDocument {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public XMLSchemaStyledDocument(ValidityEditorPane editor) {
      super(editor);
    }
    
    public List getProblemList() {
      return YAWLEngineProxy.getInstance().getSchemaValidationResults(
          getEditor().getText()
      );
    }
    
    public void setPreAndPostEditorText(String preEditorText, String postEditorText) {
      // deliberately does nothing.
    }

    public void checkValidity() {
      
      if (getEditor().getText().equals("")) {
        setContentValid(AbstractXMLStyledDocument.Validity.VALID);
        return;
      }
      
      List validationResults = getProblemList();
 
      setContentValid(validationResults == null ? 
          AbstractXMLStyledDocument.Validity.VALID: 
          AbstractXMLStyledDocument.Validity.INVALID
      );
    }
  }
}

