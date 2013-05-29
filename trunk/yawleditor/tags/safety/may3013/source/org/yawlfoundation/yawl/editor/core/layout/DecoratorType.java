package org.yawlfoundation.yawl.editor.core.layout;

import java.util.Hashtable;
import java.util.Map;

/**
 * An enumeration of Decorator Types
 *
 * @author Michael Adams
 * @date 20/06/12
*/
public enum DecoratorType {

    AndSplit("AND_split"),
    XorSplit("XOR_split"),
    OrSplit("OR_split"),
    AndJoin("AND_join"),
    XorJoin("XOR_join"),
    OrJoin("OR_join");

    private String stringValue;

    private static final Map<String, DecoratorType> _typeMap = new Hashtable<String, DecoratorType>(6);

    static {
        for (DecoratorType type : values()) _typeMap.put(type.toString(), type);
    }


    private DecoratorType(String s) { stringValue = s; }


    public String toString() { return stringValue; }


    /**
     * Gets a decorator type from a String.
     * @param s the String. Valid values are: AND_split, XOR_split, OR_split, AND_join,
     *          XOR_join, OR_join
     * @return the equivalent type for the String passed
     */
    public static DecoratorType fromString(String s) {
        return (s != null) ? _typeMap.get(s) : null;
    }


    public boolean isJoin() { return stringValue.endsWith("join"); }


    public boolean isSplit() { return ! isJoin(); }
}
