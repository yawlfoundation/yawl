package org.yawlfoundation.yawl.editor.ui.resourcing;

import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.schema.XSDType;

public class DataVariableContent {

    public static final int DATA_CONTENT_TYPE = 0;
    public static final int PARTICIPANT_CONTENT_TYPE = 1;
    public static final int ROLE_CONTENT_TYPE = 2;

    private static final String DATA_STRING = "Data";
    private static final String PARTICIPANT_STRING = "Participant";
    private static final String ROLE_STRING = "Role";

    protected YVariable variable;
    protected int contentType = DATA_CONTENT_TYPE;

    public DataVariableContent() {}

    public DataVariableContent(YVariable variable) {
        this.variable = variable;
        this.contentType = DATA_CONTENT_TYPE;
    }

    public DataVariableContent(YVariable variable, int contentType) {
        this.variable = variable;
        this.contentType = contentType;
    }

    public void setVariable(YVariable variable) {
        this.variable = variable;
    }

    public YVariable getVariable() {
        return variable;
    }


    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public int getContentType() {
        return contentType;
    }

    public void setContentType(String contentTypeString) {
        contentType = getContentTypeForString(contentTypeString);
    }

    public boolean isValidForResourceContainment() {
        return isValidForResourceContainment(variable);
    }

    public static boolean isValidForResourceContainment(YVariable variable) {
        return XSDType.getOrdinal(variable.getDataTypeName()) == XSDType.STRING;
    }

    public String getContentTypeAsString() {
        return getContentTypeAsString(contentType);
    }

    public static String getContentTypeAsString(int contentType) {
        switch(contentType) {
            case DATA_CONTENT_TYPE: return DATA_STRING;
            case PARTICIPANT_CONTENT_TYPE: return PARTICIPANT_STRING;
            case ROLE_CONTENT_TYPE: return ROLE_STRING;
        }
        return DATA_STRING;
    }

    public static int getContentTypeForString(String contentTypeString) {
        if (contentTypeString.equals(DATA_STRING)) {
            return DATA_CONTENT_TYPE;
        }
        if (contentTypeString.equals(PARTICIPANT_STRING)) {
            return PARTICIPANT_CONTENT_TYPE;
        }
        if (contentTypeString.equals(ROLE_STRING)) {
            return ROLE_CONTENT_TYPE;
        }
        return DATA_CONTENT_TYPE;
    }
}