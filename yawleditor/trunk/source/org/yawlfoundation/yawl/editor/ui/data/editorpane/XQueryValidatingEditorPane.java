package org.yawlfoundation.yawl.editor.ui.data.editorpane;

import org.yawlfoundation.yawl.editor.ui.data.Validity;
import org.yawlfoundation.yawl.editor.ui.data.editor.XQueryValidatingEditor;
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.MappingTypeValidator;

import javax.swing.*;

/**
 * @author Michael Adams
 * @date 5/11/2013
 */
public class XQueryValidatingEditorPane extends XQueryEditorPane {

    private JButton _parentDialogOK;

    public XQueryValidatingEditorPane() {
        super(new XQueryValidatingEditor());
        ((XQueryValidatingEditor) getEditor()).setProblemPane(this);
    }

    public void setParentDialogOKButton(JButton ok) {
        _parentDialogOK = ok;
    }

    public void setTypeChecker(MappingTypeValidator checker) {
        ((XQueryValidatingEditor) getEditor()).setTypeChecker(checker);
    }

    public void setTypeChecker(MappingTypeValidator checker, boolean isSplitPredicate) {
        ((XQueryValidatingEditor) getEditor()).setTypeChecker(checker, isSplitPredicate);
    }

    public void documentValidityChanged(Validity documentValid) {
        if (_parentDialogOK != null) {
            _parentDialogOK.setEnabled(! showsErrors());
        }
    }


}
