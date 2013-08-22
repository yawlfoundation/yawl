package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.util.FileUtilities;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import java.util.Map;

/**
 * @author Michael Adams
 * @date 21/08/13
 */
public class VariableUserDefinedAttributes extends UserDefinedAttributes {

    private Map<String, String> _map;                 // attribute name, type
    private String _filePath;

    private static final VariableUserDefinedAttributes INSTANCE =
            new VariableUserDefinedAttributes();


    public static VariableUserDefinedAttributes getInstance() {
        return INSTANCE;
    }


    protected Map<String, String> getMap() {
        if (_map == null) {
            _map = load(getFilePath());
        }
        return _map;
    }


    protected String getFilePath() {
        if (_filePath == null) {
            _filePath = UserSettings.getVariableAttributesFilePath();
            if (_filePath == null) {
                _filePath = FileUtilities.getVariableAttributePath();
            }
        }
        return _filePath;
    }


}
