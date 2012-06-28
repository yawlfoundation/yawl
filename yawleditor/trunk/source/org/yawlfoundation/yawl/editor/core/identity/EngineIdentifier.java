package org.yawlfoundation.yawl.editor.core.identity;

/**
 * @author Michael Adams
 * @date 1/03/12
 */
public class EngineIdentifier {
    
    private String _name;
    private int _suffix = 0;
    
    public EngineIdentifier(String name) {
        _name = name;
    }

    public EngineIdentifier(String name, int suffix) {
        this(name);
        _suffix = suffix;
    }
    
    public String getName() { return _name; }

    
    public int getSuffix() { return _suffix; }

    public void setSuffix(int suffix) { _suffix = suffix; }


    public String toString() {
        return _suffix == 0 ? _name : _name + "_" + _suffix;
    }
    
}
