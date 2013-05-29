package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.schema.XSDType;

public class JXMLSchemaDurationEditorPane extends JXMLSchemaInstanceEditorPane {

  private static final long serialVersionUID = 1L;

  public JXMLSchemaDurationEditorPane () {
    super();
    setVariableType(XSDType.getString(XSDType.DURATION));
    setVariableName("durationPane");
    setText("");
  }
}
