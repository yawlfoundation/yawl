package org.yawlfoundation.yawl.schema.internal;

import org.jdom2.Element;

import java.util.Hashtable;
import java.util.Map;

/**
 * An enumeration of available internal type definitions
 *
 * @author Michael Adams
 * @date 2/08/12
 */
public enum YInternalType {

    // all available internal types are listed here as enums
    YDocumentType(new YDocumentType()),
    YStringListType(new YStringListType()),
    YTimerType(new YTimerType());

    private YDataType _type;

    private static Map<String, YInternalType> _fromStringMap =
            new Hashtable<String, YInternalType>();


    static {
        for (YInternalType type : values()) {
            _fromStringMap.put(type.name(), type);
        }
    }

    // enum constructor
    private YInternalType(YDataType type) {
        _type = type;
    }

    /************************************************************************/
    // Methods for each enumeration

    public String getSchemaString() { return _type.getSchemaString(); }

    private Element getSchema(String varName) { return _type.getSchema(varName); }


    /************************************************************************/
    // Static methods for entire YInternalType

    public static boolean isType(String name) {
        return _fromStringMap.containsKey(name);
    }

    public static Element getSchemaFor(String type, String varName) {
        YInternalType internalType = _fromStringMap.get(type);
        return internalType != null ? internalType.getSchema(varName) : null;
    }

}
