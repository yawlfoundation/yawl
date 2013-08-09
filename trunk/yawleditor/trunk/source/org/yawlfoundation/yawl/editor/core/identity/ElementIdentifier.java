package org.yawlfoundation.yawl.editor.core.identity;

import org.yawlfoundation.yawl.util.StringUtil;

/**
 * Creates an identifier with a name part and an optional numeric suffix.
 *
 * @author Michael Adams
 * @date 1/03/12
 */
public class ElementIdentifier {
    
    private String _label;
    private int _suffix;
    

    public ElementIdentifier(String label, int suffix) {
        _label = label;
        _suffix = suffix;
    }

    public ElementIdentifier(String label, boolean parse) {
        this(label, 0);                                        // default
        if (parse) parseLabel(label);
    }

    public String getName() { return _label; }

    
    public int getSuffix() { return _suffix; }

    public void setSuffix(int suffix) { _suffix = suffix; }


    /**
     * Parses an id string into a name part, and a numeric suffix part if required
     * @param id the id string to parse
     */
    private void parseLabel(String id) {
        int pos = id.lastIndexOf("_");
        if (pos > -1) {
            String suffixStr = id.substring(pos + 1);
            if (StringUtil.isIntegerString(suffixStr)) {
                _label = id.substring(0, pos);
                _suffix = StringUtil.strToInt(suffixStr, 0);
            }
        }
    }


    public String toString() {
        return _suffix == 0 ? _label : _label + "_" + _suffix;
    }
    
}
