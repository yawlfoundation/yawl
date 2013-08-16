package org.yawlfoundation.yawl.editor.ui.properties.data.validation;

import org.yawlfoundation.yawl.editor.ui.data.editorpane.ValidityEditorPane;

/**
 * @author Michael Adams
 * @date 16/08/13
 */
public class InstanceEditor extends ValidityEditorPane {

    private String _dataType;

    public InstanceEditor(String dataType, String value) {
        super();
        _dataType = dataType;
        setDocument(new InstanceStyledDocument(this));
        subscribeForValidityEvents();
        setText(value);
    }

    public String getDataType() {
        return _dataType;
    }

    public void validate() {
        super.validate();
    }

}
