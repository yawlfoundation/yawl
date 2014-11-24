/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.editor.ui.properties.extended;

import java.util.HashMap;
import java.util.Map;

/**
 * A map of extended attribute names to their equivalent property names. Required
 * because property get & set methods are called by reflection and method names can't
 * match those properties with hyphens.
 *
 * @author Michael Adams
 * @date 23/08/13
 */
public class ExtendedAttributeNameLookup {

    // [extended attribute name, property name]
    private Map<String, String> _attributeToPropertyMap;
    private Map<String, String> _propertyToAttributeMap;

    public ExtendedAttributeNameLookup() {
        buildMaps();
    }

    public String getPropertyName(String attributeName) {
        String value = _attributeToPropertyMap.get(attributeName);
        return value != null ? value : attributeName;
    }


    public String getAttributeName(String propertyName) {
        String value = _propertyToAttributeMap.get(propertyName);
        return value != null ? value : propertyName;
    }


    private void buildMaps() {
        _attributeToPropertyMap = new HashMap<String, String>();
        _attributeToPropertyMap.put("background-color", "backgroundColour");
        _attributeToPropertyMap.put("background-alt-color", "backgroundAltColour");
        _attributeToPropertyMap.put("header-font", "headerFont");
        _attributeToPropertyMap.put("page-background-color", "pageBackgroundColour");
        _attributeToPropertyMap.put("page-background-image", "pageBackgroundImage");
        _attributeToPropertyMap.put("image-above", "imageAbove");
        _attributeToPropertyMap.put("image-below", "imageBelow");
        _attributeToPropertyMap.put("image-above-align", "imageAboveAlign");
        _attributeToPropertyMap.put("image-below-align", "imageBelowAlign");
        _attributeToPropertyMap.put("line-above", "lineAbove");
        _attributeToPropertyMap.put("line-below", "lineBelow");
        _attributeToPropertyMap.put("max-field-width", "maxFieldWidth");
        _attributeToPropertyMap.put("text-above", "textAbove");
        _attributeToPropertyMap.put("text-below", "textBelow");
        _attributeToPropertyMap.put("textarea", "textArea");

        _propertyToAttributeMap = new HashMap<String, String>();
        for (String key : _attributeToPropertyMap.keySet()) {
            _propertyToAttributeMap.put(_attributeToPropertyMap.get(key), key);
        }
    }
}
