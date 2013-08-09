package org.yawlfoundation.yawl.editor.ui.data.editorpane;

import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.editor.ui.swing.menu.DataTypeDialogToolBarMenu;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLToolBarButton;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
* @author Michael Adams
* @date 8/08/13
*/
public class XMLStyledDocument extends AbstractXMLStyledDocument {

     public XMLStyledDocument(ValidityEditorPane editor) {
        super(editor);
    }

    public List<String> getProblemList() {
        String content;
        try {
            content = new String(getEditor().getText().getBytes(), "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            content = getEditor().getText();
        }
        return SpecificationModel.getInstance().getSchemaValidator().
                getSchemaValidationResults(content);
    }

    public void setPreAndPostEditorText(String preEditorText, String postEditorText) {
        // deliberately does nothing.
    }

    public void checkValidity() {
        if (getEditor().getText().equals("")) {
            setContentValidity(Validity.VALID);
        }
        else if (isValidating()) {
            setContentValidity(
                    getProblemList().isEmpty() ? Validity.VALID : Validity.INVALID);
        }
        DataTypeDialogToolBarMenu menu = DataTypeDialogToolBarMenu.getInstance();
        if (menu != null) {
            YAWLToolBarButton formatBtn = menu.getButton("format");
            if (formatBtn != null) formatBtn.setEnabled(isContentValid());
        }
    }
}
