package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.util.FileUtilities;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

/**
 * @author Michael Adams
 * @date 21/08/13
 */
public class DecompositionUserDefinedAttributes extends UserDefinedAttributes {

    private String _filePath;

    private static final DecompositionUserDefinedAttributes INSTANCE =
            new DecompositionUserDefinedAttributes();


    public static DecompositionUserDefinedAttributes getInstance() {
        return INSTANCE;
    }


    protected String getFilePath() {
        if (_filePath == null) {
            _filePath = UserSettings.getDecompositionAttributesFilePath();
            if (_filePath == null) {
                _filePath = FileUtilities.getDecompositionAttributePath();
            }
        }
        return _filePath;
    }


}
