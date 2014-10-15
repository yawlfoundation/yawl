/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.schema;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

    // Other Types
    public static final int QNAME                = 39;
    public static final int BOOLEAN              = 40;
    public static final int HEX_BINARY           = 41;
    public static final int BASE64_BINARY        = 42;
    public static final int NOTATION             = 43;
    public static final int ANY_URI              = 44;


    public static enum RestrictionFacet { minExclusive, maxExclusive,
            minInclusive, maxInclusive, minLength, maxLength, length,
            totalDigits, fractionDigits, whiteSpace, pattern, enumeration }


    private static final List<String> _typeList = makeList();


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


    public static boolean isBuiltInType(String type) {
        return _typeList.contains(type);
    }

    
    public static int getOrdinal(String type) {
        return _typeList.indexOf(type);
    }

    public static boolean isNumericType(String type) {
        int ordinal = getOrdinal(type);
        return (ordinal >= INTEGER) && (ordinal <= DECIMAL);
    }

    public static boolean isIntegralType(String type) {
        int ordinal = getOrdinal(type);
        return (ordinal >= INTEGER) && (ordinal <= UNSIGNED_BYTE);
    }

    public static boolean isFloatType(String type) {
        int ordinal = getOrdinal(type);
        return (ordinal >= DOUBLE) && (ordinal <= DECIMAL);        
    }

    public static boolean isBooleanType(String type) {
        return getOrdinal(type) == BOOLEAN;
    }

    public static boolean isDateType(String type) {
        int ordinal = getOrdinal(type);
        return (ordinal >= DATE) && (ordinal <= DATETIME);
    }

    public static boolean isListType(String type) {
        int ordinal = getOrdinal(type);
        return ordinal == NMTOKENS || ordinal == ENTITIES || ordinal == IDREFS;
    }

    public static boolean isBinaryType(String type) {
        int ordinal = getOrdinal(type);
        return ordinal == HEX_BINARY || ordinal == BASE64_BINARY;
    }

    public static boolean isStringForType(String s, int type) {
        return getString(type).equals(s);
    }

    public static List<String> getBuiltInTypeList() {
        return new ArrayList<String>(_typeList);                        // send a copy  
    }

    public static String[] getBuiltInTypeArray() {
        return _typeList.toArray(new String[_typeList.size()]);
    }


    public static String getSampleValue(String type) {
        switch (getOrdinal(type)) {
            case INTEGER:
            case POSITIVE_INTEGER:
            case INT:
            case LONG:
            case SHORT:
            case UNSIGNED_LONG:
            case UNSIGNED_INT:
            case UNSIGNED_SHORT:
            case UNSIGNED_BYTE:
            case NON_NEGATIVE_INTEGER:
            case GYEAR:
            case BYTE:
            case DECIMAL:               return "100";
            case NEGATIVE_INTEGER:
            case NON_POSITIVE_INTEGER:  return "-100";
            case STRING:
            case NORMALIZED_STRING:     return "a string";
            case TOKEN:
            case NMTOKEN:
            case NMTOKENS:              return "token";
            case NAME:
            case NCNAME:
            case ID:
            case IDREF:
            case IDREFS:
            case ENTITY:
            case ENTITIES:
            case BASE64_BINARY:
            case NOTATION:
            case ANY_URI:
            case ANY_TYPE:    return "name";
            case BOOLEAN:     return "false";
            case LANGUAGE:    return "en";
            case QNAME:       return "xs:name";
            case HEX_BINARY:  return "FF";
            case DOUBLE:
            case FLOAT:       return "3.142";
            case DATE:        return getDateTimeValue("yyyy-MM-dd");    // "2013-01-01"
            case TIME:        return getDateTimeValue("HH:mm:ss");      // "12:12:12";
            case DATETIME:    return getDateTimeValue("yyyy-MM-dd'T'HH:mm:ss");
            case DURATION:    return "PY2";
            case GDAY:        return getDateTimeValue("'---'dd");
            case GMONTH:      return getDateTimeValue("'--'MM");
            case GMONTHDAY:   return getDateTimeValue("'--'MM-dd");
            case GYEARMONTH:  return getDateTimeValue("yyyy-MM");
        }
        return "name";
    }


    public static char[] getConstrainingFacetMap(String type) {
        String vMap;
        switch (getOrdinal(type)) {
            case INTEGER:
            case POSITIVE_INTEGER:
            case NEGATIVE_INTEGER:
            case NON_POSITIVE_INTEGER:
            case NON_NEGATIVE_INTEGER:
            case INT:
            case LONG:                 
            case SHORT:
            case UNSIGNED_LONG:
            case UNSIGNED_INT:
            case UNSIGNED_SHORT:
            case UNSIGNED_BYTE:        vMap = "111100010111"; break;
            case STRING:
            case NORMALIZED_STRING:
            case TOKEN:
            case LANGUAGE:
            case NMTOKEN:
            case NMTOKENS:
            case NAME:                 
            case NCNAME:
            case ID:
            case IDREF:                
            case IDREFS:
            case ENTITY:               
            case ENTITIES:
            case QNAME:
            case HEX_BINARY:
            case BASE64_BINARY:
            case NOTATION:
            case ANY_URI:              vMap = "000011100111"; break;
            case DOUBLE:
            case FLOAT:
            case DATE:
            case TIME:                 
            case DATETIME:
            case DURATION:           
            case GDAY:
            case GMONTH:
            case GYEAR:
            case GMONTHDAY:
            case GYEARMONTH:           vMap = "111100000111"; break;
            case BOOLEAN:              vMap = "000000000110"; break;
            case BYTE:                 vMap = "111100110111"; break;
            case DECIMAL:              vMap = "111100011111"; break;
            case ANY_TYPE:
            default:                   vMap = "000000000000"; break;
        }
        return vMap.toCharArray() ;
    }


    public static boolean isValidFacet(String facetName, String type) {
        char[] validationMap = getConstrainingFacetMap(type);
        try {
            RestrictionFacet facet = RestrictionFacet.valueOf(facetName);
            int ordinal = facet.ordinal();
            return validationMap[ordinal] == '1';
        }
        catch (IllegalArgumentException iae) {
            return false;                                  // invalid restriction name
        }
    }


    private static String[] makeYAWLTypeArray() {
        String[] simpleYAWLTypes = {"NCName", "anyURI", "boolean", "date", "double",
                                    "duration", "long", "string", "time" } ;
        return simpleYAWLTypes;
    }


    private static List<String> makeList() {
        List<String> typeList = new ArrayList<String>();
        for (int i = ANY_TYPE; i<= ANY_URI; i++) {
            typeList.add(getString(i));
        }
        return typeList;
    }


    private static String getDateTimeValue(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date());
    }

}
