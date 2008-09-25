package org.yawlfoundation.yawl.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 14/09/2008
 */
public class XSDType {

    public static final int INVALID_TYPE         = -1;
    public static final int ANY_TYPE             = 0;

    // Numeric Types
    public static final int INTEGER              = 1;                        // Integral
    public static final int POSITIVE_INTEGER     = 2;
    public static final int NEGATIVE_INTEGER     = 3;
    public static final int NON_POSITIVE_INTEGER = 4;
    public static final int NON_NEGATIVE_INTEGER = 5;
    public static final int INT                  = 6;
    public static final int LONG                 = 7;
    public static final int SHORT                = 8;
    public static final int BYTE                 = 9;
    public static final int UNSIGNED_LONG        = 10;
    public static final int UNSIGNED_INT         = 11;
    public static final int UNSIGNED_SHORT       = 12;
    public static final int UNSIGNED_BYTE        = 13;
    public static final int DOUBLE               = 14;                    // Non-integral
    public static final int FLOAT                = 15;
    public static final int DECIMAL              = 16;

    // String Types
    public static final int STRING               = 17;
    public static final int NORMALIZED_STRING    = 18;
    public static final int TOKEN                = 19;
    public static final int LANGUAGE             = 20;
    public static final int NMTOKEN              = 21;
    public static final int NMTOKENS             = 22;
    public static final int NAME                 = 23;
    public static final int NCNAME               = 24;

    // Date Time Types
    public static final int DATE                 = 25;
    public static final int TIME                 = 26;
    public static final int DATETIME             = 27;
    public static final int DURATION             = 28;
    public static final int GDAY                 = 29;
    public static final int GMONTH               = 30;
    public static final int GYEAR                = 31;
    public static final int GMONTHDAY            = 32;
    public static final int GYEARMONTH           = 33;

    // Magic Types
    public static final int ID                   = 34;
    public static final int IDREF                = 35;
    public static final int IDREFS               = 36;
    public static final int ENTITY               = 37;
    public static final int ENTITIES             = 38;

    // Oddball Types
    public static final int QNAME                = 39;
    public static final int BOOLEAN              = 40;
    public static final int HEX_BINARY           = 41;
    public static final int BASE64_BINARY        = 42;
    public static final int NOTATION             = 43;
    public static final int ANY_URI              = 44;


    private static XSDType _me;
    private static String[] _simpleYAWLTypes;
    private static List<String> _typeList;


    private XSDType() {
        _simpleYAWLTypes = makeYAWLTypeArray();
        _typeList = makeList();
    }


    public static XSDType getInstance() {
        if (_me == null) _me = new XSDType();
        return _me;
    }


    public static String getString(int type) {
        switch (type) {
            case ANY_TYPE:             return "anyType";
            case INTEGER:              return "integer";
            case POSITIVE_INTEGER:     return "positiveInteger";
            case NEGATIVE_INTEGER:     return "negativeInteger";
            case NON_POSITIVE_INTEGER: return "nonPositiveInteger";
            case NON_NEGATIVE_INTEGER: return "nonNegativeInteger";
            case INT:                  return "int";
            case LONG:                 return "long";
            case SHORT:                return "short";
            case BYTE:                 return "byte";
            case UNSIGNED_LONG:        return "unsignedLong";
            case UNSIGNED_INT:         return "unsignedInt";
            case UNSIGNED_SHORT:       return "unsignedShort";
            case UNSIGNED_BYTE:        return "unsignedByte";
            case DOUBLE:               return "double";
            case FLOAT:                return "float";
            case DECIMAL:              return "decimal";
            case STRING:               return "string";
            case NORMALIZED_STRING:    return "normalizedString";
            case TOKEN:                return "token";
            case LANGUAGE:             return "language";
            case NMTOKEN:              return "NMTOKEN";
            case NMTOKENS:             return "NMTOKENS";
            case NAME:                 return "Name";
            case NCNAME:               return "NCName";
            case DATE:                 return "date";
            case TIME:                 return "time";
            case DATETIME:             return "dateTime";
            case DURATION:             return "duration";
            case GDAY:                 return "gDay";
            case GMONTH:               return "gMonth";
            case GYEAR:                return "gYear";
            case GMONTHDAY:            return "gMonthDay";
            case GYEARMONTH:           return "gYearMonth";
            case ID:                   return "ID";
            case IDREF:                return "IDREF";
            case IDREFS:               return "IDREFS";
            case ENTITY:               return "ENTITY";
            case ENTITIES:             return "ENTITIES";
            case QNAME:                return "QName";
            case BOOLEAN:              return "boolean";
            case HEX_BINARY:           return "hexBinary";
            case BASE64_BINARY:        return "base64Binary";
            case NOTATION:             return "notation";
            case ANY_URI:              return "anyURI";
        }
        return "invalid_type" ;
    }


    private String[] makeYAWLTypeArray() {
        String[] simpleYAWLTypes = {"NCName", "anyURI", "boolean", "date", "double",
                                    "duration", "long", "string", "time" } ;
        return simpleYAWLTypes;
    }


    private List<String> makeList() {
        _typeList = new ArrayList<String>();
        for (int i = ANY_TYPE; i<= ANY_URI; i++) {
            _typeList.add(getString(i));
        }
        return _typeList;
    }


    public boolean isSimpleYAWLType(String type) {
        return Arrays.binarySearch(_simpleYAWLTypes, type) >= 0 ;
    }


    public boolean isBuiltInType(String type) {
        return _typeList.contains(type);
    }

    
    public int getOrdinal(String type) {
        return _typeList.indexOf(type);
    }

    public boolean isNumericType(String type) {
        int ordinal = getOrdinal(type);
        return (ordinal >= INTEGER) && (ordinal <= DECIMAL);
    }


    public boolean isStringForType(String s, int type) {
        return getString(type).equals(s);
    }

    public List<String> getBuiltInTypeList() {
        return _typeList;
    }

    public String[] getBuiltInTypeArray() {
        return _typeList.toArray(new String[_typeList.size()]);
    }

}
