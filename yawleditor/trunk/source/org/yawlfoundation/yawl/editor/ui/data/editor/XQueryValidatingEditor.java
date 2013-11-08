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


    public void setProblemPane(XQueryValidatingEditorPane pane) { _problemPane = pane; }

    public void setTypeChecker(MappingTypeValidator checker) { _typeChecker = checker; }

    /**
     * If text has valid XQuery syntax, check for data type compatibility,
     * otherwise show syntax errors
     * @param documentValid will be true if text has valid XQuery syntax
     */
    public void documentValidityChanged(Validity documentValid) {
        super.documentValidityChanged(documentValid);
        _problemPane.showProblems(documentValid == Validity.VALID ?
                _typeChecker.validate(getText()) : getProblemList());
    }

}
