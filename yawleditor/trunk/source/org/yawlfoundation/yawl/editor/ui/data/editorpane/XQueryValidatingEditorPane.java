package org.yawlfoundation.yawl.editor.ui.data.editorpane;

import org.yawlfoundation.yawl.editor.ui.data.Validity;
import org.yawlfoundation.yawl.editor.ui.data.editor.XQueryValidatingEditor;
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.MappingTypeValidator;

/**
 * @author Michael Adams
 * @date 5/11/2013
 */
public class XQueryValidatingEditorPane extends XQueryEditorPane {


    public XQueryValidatingEditorPane() {
        super(new XQueryValidatingEditor());
        ((XQueryValidatingEditor) getEditor()).setProblemPane(this);
    }


    public void setTypeChecker(MappingTypeValidator checker) {
        ((XQueryValidatingEditor) getEditor()).setTypeChecker(checker);
    }

    public void documentValidityChanged(Validity documentValid) {
        //showProblems(editor.getProblemList());
    }


}
