package org.yawlfoundation.yawl.editor.ui.properties.data.validation;

import org.yawlfoundation.yawl.editor.core.data.YDataHandler;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.AbstractXMLStyledDocument;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.Validity;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Adams
 * @date 16/08/13
 */
public class InstanceStyledDocument extends AbstractXMLStyledDocument {

    private List<String> _errorList;
    private String _dataType;

    private static final YDataHandler DATA_HANDLER =
            SpecificationModel.getHandler().getDataHandler();

    public InstanceStyledDocument(InstanceEditor editor) {
        super(editor);
        _dataType = editor.getDataType();
        _errorList = new ArrayList<String>();
    }

    public void checkValidity() {
        _errorList = DATA_HANDLER.validate(_dataType, getEditor().getText());
        setContentValidity(_errorList.isEmpty() ? Validity.VALID : Validity.INVALID);
    }

    public List<String> getProblemList() { return _errorList; }

    public void setPreAndPostEditorText(String pre, String post) { }

}
