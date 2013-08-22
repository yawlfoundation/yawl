package org.yawlfoundation.yawl.editor.ui.properties;

import org.yawlfoundation.yawl.editor.ui.util.FileUtilities;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import java.util.Map;

/**
 * @author Michael Adams
 * @date 21/08/13
 */
public class DecompositionUserDefinedAttributes extends UserDefinedAttributes {

    private Map<String, String> _map;                 // attribute name, type
    private String _filePath;

    private static final DecompositionUserDefinedAttributes INSTANCE =
            new DecompositionUserDefinedAttributes();


    public static DecompositionUserDefinedAttributes getInstance() {
        return INSTANCE;
    }


    public boolean add(String name, String type) throws IllegalArgumentException {
        if (validateType(type)) {
            Map<String, String> map = getMap();
            if (map != null) {
                map.put(name, type);
                return save(getMap(), getFilePath());
            }
            return false;
        }
        else throw new IllegalArgumentException("Invalid type name: " + type);
    }


    public boolean remove(String name) {
        Map<String, String> map = getMap();
        if (map != null) {
            String type = map.remove(name);
            if (type != null) {
                return save(getMap(), getFilePath());
            }
        }
        return false;
    }


    public String getType(String name) {
        Map<String, String> map = getMap();
        return map != null ? map.get(name) : null;
    }



    protected Map<String, String> getMap() {
        if (_map == null) {
            _map = load(getFilePath());
        }
        return _map;
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
