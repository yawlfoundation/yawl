/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.schema.internal;

import org.jdom2.Element;

import java.util.HashMap;
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
    YTimerType(new YTimerType()),
    YGeoLatLongType(new YGeoLatLongType()),
    YGeoCircleType(new YGeoCircleType()),
    YGeoRectType(new YGeoRectType()),
    YGeoPolygonType(new YGeoPolygonType())
    ;

    private final YDataType _type;

    private static Map<String, YInternalType> _fromStringMap =
            new HashMap<String, YInternalType>();


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
