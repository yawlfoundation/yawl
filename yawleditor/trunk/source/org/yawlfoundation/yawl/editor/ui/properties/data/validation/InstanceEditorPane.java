package org.yawlfoundation.yawl.editor.ui.properties.data.validation;

import org.yawlfoundation.yawl.editor.ui.data.editorpane.ProblemReportingEditorPane;

/**
 * @author Michael Adams
 * @date 16/08/13
 */
public class InstanceEditorPane extends ProblemReportingEditorPane {

    public InstanceEditorPane(String dataType, String value) {
        super(new InstanceEditor(dataType, value));
    }
}
