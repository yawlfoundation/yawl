package org.yawlfoundation.yawl.editor.ui.data.editor;

import org.yawlfoundation.yawl.editor.ui.data.Validity;
import org.yawlfoundation.yawl.editor.ui.data.editorpane.XQueryValidatingEditorPane;
import org.yawlfoundation.yawl.editor.ui.properties.data.validation.BindingTypeValidator;

import javax.swing.*;

/**
 * @author Michael Adams
 * @date 5/11/2013
 */
public class XQueryValidatingEditor extends XQueryEditor {

    private XQueryValidatingEditorPane _problemPane;
    private BindingTypeValidator _typeChecker;
    private boolean _isSplitPredicate;


    public void setProblemPane(XQueryValidatingEditorPane pane) { _problemPane = pane; }

    public BindingTypeValidator getTypeChecker() { return _typeChecker; }

    public void setTypeChecker(BindingTypeValidator checker) {
        setTypeChecker(checker, false);
    }

    public void setTypeChecker(BindingTypeValidator checker, boolean isSplitPredicate) {
        _typeChecker = checker;
        _isSplitPredicate = isSplitPredicate;
        new InitialTypeCheckWorker().execute();
    }

    /**
     * If text has valid XQuery syntax, check for data type compatibility,
     * otherwise show syntax errors
     * @param documentValid will be true if text has valid XQuery syntax
     */
    public void documentValidityChanged(Validity documentValid) {
        super.documentValidityChanged(documentValid);
        _problemPane.showProblems(
                documentValid == Validity.VALID && _typeChecker != null ?
                _typeChecker.validate(getPredicate()) : getProblemList());
    }


    private String getPredicate() {
        return _isSplitPredicate ? "boolean(" + getText() + ")" : getText();
    }


    /*************************************************************************/

    // At the moment type-checker is set, it may not be yet fully initialised.
    // Rather than hold the EDT while we wait, this worker will wait until
    // it completes and then do an initial validity check of the editor text
    class InitialTypeCheckWorker extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            while (! _typeChecker.isInitialised()) {
                try {
                    Thread.sleep(200);
                }
                catch (InterruptedException ie) {
                    // continue
                }
            }
            return null;
        }

        protected void done() { getXMLStyledDocument().publishValidity(); }
    }
}
