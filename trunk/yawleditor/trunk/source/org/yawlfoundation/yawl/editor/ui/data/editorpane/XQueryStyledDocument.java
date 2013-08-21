package org.yawlfoundation.yawl.editor.ui.data.editorpane;

import net.sf.saxon.s9api.SaxonApiException;
import org.yawlfoundation.yawl.util.SaxonUtil;

import java.util.ArrayList;
import java.util.List;


class XQueryStyledDocument extends AbstractXMLStyledDocument {

    private String preEditorText;
    private String postEditorText;
    private List<String> errorList;

    public XQueryStyledDocument(ValidityEditorPane editor) {
        super(editor);
        setDocumentFilter(new IgnoreBadCharactersFilter());
        setPreAndPostEditorText("","");
        errorList = new ArrayList<String>();
    }

    public void setPreAndPostEditorText(String preEditorText, String postEditorText) {
        this.preEditorText = preEditorText;
        this.postEditorText = postEditorText;
    }

    public void checkValidity() {
        errorList.clear();
        if (isValidating()) {
            if (getEditor().getText().equals("")) {
                errorList.add("Query required");
                setContentValidity(Validity.INVALID);
                return;
            }

            // external data gateway
            if (getEditor().getText().matches(
                    "^\\s*#external:\\w+\\s*:\\w+\\s*")) {
                setContentValidity(Validity.VALID);
                return;
            }

            // timer expression
            if (getEditor().getText().matches(
                    "^\\s*timer\\(\\w+\\)\\s*!?=\\s*'(dormant|active|closed|expired)'\\s*$")) {
                setContentValidity(Validity.VALID);
                return;
            }

            try {
                SaxonUtil.compileXQuery(preEditorText + getEditor().getText() + postEditorText);
                errorList = SaxonUtil.getCompilerMessages();
                setContentValidity(errorList.isEmpty() ? Validity.VALID : Validity.INVALID);
            }
            catch (SaxonApiException e) {
                errorList.add(e.getMessage().split("\n")[1].trim());
                setContentValidity(Validity.INVALID);
            }
        }
    }

    public List<String> getProblemList() {
        return errorList;
    }
}
