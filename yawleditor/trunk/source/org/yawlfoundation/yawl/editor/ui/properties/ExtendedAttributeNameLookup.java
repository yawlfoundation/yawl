package org.yawlfoundation.yawl.editor.ui.properties;

import java.util.Hashtable;
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
        _attributeToPropertyMap = new Hashtable<String, String>();
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
        _attributeToPropertyMap.put("text-above", "textAbove");
        _attributeToPropertyMap.put("text-below", "textBelow");
        _attributeToPropertyMap.put("text-area", "textArea");

        _propertyToAttributeMap = new Hashtable<String, String>();
        for (String key : _attributeToPropertyMap.keySet()) {
            _propertyToAttributeMap.put(_attributeToPropertyMap.get(key), key);
        }
    }
}
