package org.yawlfoundation.yawl.editor.swing.data;

import org.yawlfoundation.yawl.editor.data.DataVariable;

public class JXMLSchemaDurationEditorPane extends JXMLSchemaInstanceEditorPane {

  private static final long serialVersionUID = 1L;

  public JXMLSchemaDurationEditorPane () {
    super();
    setVariableType(DataVariable.XML_SCHEMA_DURATION_TYPE);
    setVariableName("anyOldVariableName");
    setText("");
  }
}
