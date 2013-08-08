package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.editor.core.data.DataSchemaValidator;
import org.yawlfoundation.yawl.editor.core.data.YInternalType;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationModel;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.LinkedList;
import java.util.List;

public class XMLInstanceStyledDocument extends AbstractXMLStyledDocument {

    private List problemList = new LinkedList();

    public XMLInstanceStyledDocument(XMLSchemaInstanceEditor editor) {
        super(editor);
    }

    public void checkValidity() {
        if (isValidating()) {
            String dataType = getInstanceEditor().getVariableType();
            if (getEditor().getText().equals("") || dataType == null || dataType.equals("string")) {
                setContentValidity(Validity.VALID);
                return;
            }

            for (YInternalType type : YInternalType.values()) {
                if (type.name().equals(dataType)) {
                    setProblemList(getYInternalTypeInstanceProblems(dataType));
                    setValidity();
                    return;
                }
            }

            validateUserSuppliedDataTypeInstance();
        }
    }

    private void validateBaseDataTypeInstance() {
        setProblemList(getBaseDataTypeInstanceProblems());
        setValidity();
    }


    private void setValidity() {
        setContentValidity(getProblemList().isEmpty() ?
                Validity.VALID :
                Validity.INVALID);
    }

    public List getProblemList() {
        return problemList;
    }

    private LinkedList getBaseDataTypeInstanceProblems() {
        LinkedList problemList = new LinkedList();

        DataSchemaValidator validator = SpecificationModel.getInstance().getSchemaValidator();
        validator.setDataTypeSchema(getInstanceEditor().getTypeDefinition());
        String errors = validator.validate(getInstanceEditor().getSchemaInstance());

        if (errors != null && errors.trim().length() > 0) {
            problemList.add(errors);
        }

        return problemList;
    }

    private List getYInternalTypeInstanceProblems(String typeName) {
        List problemList = new LinkedList();
        String varName = getInstanceEditor().getVariableName();
        String validationSchema = YInternalType.valueOf(typeName).getValidationSchema(varName);

        DataSchemaValidator validator = SpecificationModel.getInstance().getSchemaValidator();
        validator.setDataTypeSchema(validationSchema);
        String errors = validator.validate(getInstanceEditor().getSchemaInstance());


        if (errors != null && errors.trim().length() > 0) {
            problemList.add(errors);
        }

        return problemList;
    }

    private void setProblemList(List problemList) {
        this.problemList = problemList;
    }

    private void validateUserSuppliedDataTypeInstance() {
        setProblemList(getUserSuppliedDataTypeInstanceProblems());
        setValidity();
    }

    private LinkedList getUserSuppliedDataTypeInstanceProblems() {
        LinkedList problemList = new LinkedList();

        DataSchemaValidator validator = SpecificationModel.getInstance().getSchemaValidator();
        validator.setDataTypeSchema(StringUtil.wrap(
                getInstanceEditor().getVariableType(), getInstanceEditor().getVariableName()));
        String errors = validator.validate(getInstanceEditor().getSchemaInstance());

        if (errors != null && errors.trim().length() > 0) {
            problemList.add(errors);
        }

        return problemList;
    }


    private XMLSchemaInstanceEditor getInstanceEditor() {
        return (XMLSchemaInstanceEditor) getEditor();
    }

    public void setPreAndPostEditorText(String preEditorText, String postEditorText) {
        // deliberately does nothing.
    }
}
