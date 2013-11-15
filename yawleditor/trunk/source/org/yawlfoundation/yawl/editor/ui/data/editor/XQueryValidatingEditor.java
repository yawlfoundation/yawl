package org.yawlfoundation.yawl.editor.ui.data.editor;

import org.yawlfoundation.yawl.editor.ui.data.Validity;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.XQueryValidatingEditorPane;
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.MappingTypeValidator;

/**
 * @author Michael Adams
 * @date 5/11/2013
 */
public class XQueryValidatingEditor extends XQueryEditor {

    private XQueryValidatingEditorPane _problemPane;
    private MappingTypeValidator _typeChecker;
    private boolean _isSplitPredicate;


    public void setProblemPane(XQueryValidatingEditorPane pane) { _problemPane = pane; }

    public void setTypeChecker(MappingTypeValidator checker) { _typeChecker = checker; }

    public void setTypeChecker(MappingTypeValidator checker, boolean isSplitPredicate) {
        _typeChecker = checker;
        _isSplitPredicate = isSplitPredicate;
    }

    /**
     * If text has valid XQuery syntax, check for data type compatibility,
     * otherwise show syntax errors
     * @param documentValid will be true if text has valid XQuery syntax
     */
    public void documentValidityChanged(Validity documentValid) {
        super.documentValidityChanged(documentValid);
        if (_typeChecker != null) {
            _problemPane.showProblems(documentValid == Validity.VALID ?
                     _typeChecker.validate(getPredicate()) : getProblemList());
        }
    }


    private String getPredicate() {
        return _isSplitPredicate ? "boolean(" + getText() + ")" : getText();
    }

}
