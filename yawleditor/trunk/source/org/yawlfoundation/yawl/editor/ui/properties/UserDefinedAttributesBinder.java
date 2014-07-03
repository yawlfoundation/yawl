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

package org.yawlfoundation.yawl.editor.ui.properties;

import com.l2fprod.common.beans.editor.BooleanAsCheckBoxPropertyEditor;
import com.l2fprod.common.beans.editor.DoublePropertyEditor;
import com.l2fprod.common.beans.editor.IntegerPropertyEditor;
import com.l2fprod.common.beans.editor.StringPropertyEditor;
import com.l2fprod.common.swing.renderer.BooleanCellRenderer;
import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.editor.*;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.util.StringUtil;

import java.awt.*;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 25/07/13
 */
public class UserDefinedAttributesBinder {

    private YAttributeMap _attributeMap;
    private final ExtendedAttributesPropertySheet _sheet;

    private OwnerType _ownerClass;

    public enum OwnerType { Decomposition, Parameter }


    private UserDefinedAttributesBinder(ExtendedAttributesPropertySheet sheet) {
        _sheet = sheet;
        _sheet.setUserDefinedAttributes(this);
    }

    public UserDefinedAttributesBinder(ExtendedAttributesPropertySheet sheet,
                                       YDecomposition decomposition) {
        this(sheet);
        _ownerClass = OwnerType.Decomposition;
        setAttributeMap(decomposition.getAttributes());
    }


    public UserDefinedAttributesBinder(ExtendedAttributesPropertySheet sheet,
                                       YAttributeMap attributes) {
        this(sheet);
        _ownerClass = OwnerType.Parameter;
        setAttributeMap(attributes);
    }


    public OwnerType getOwnerClass() { return _ownerClass; }


    public Object getValue() {
        String key = getSelectedKey();
        return (key != null) ? strToObject(key, _attributeMap.get(key)) : null;
    }


    public void setValue(Object value) {
        String key = getSelectedKey();
        if (key != null) {
            String valueStr = objToString(key, value);
            if (StringUtil.isNullOrEmpty(valueStr)) {    // no value supplied, so del
                _attributeMap.remove(key);
            }
            else {
                _attributeMap.put(key, valueStr);       // add attr value
            }
        }
    }


    public String getSelectedType() {
        String key = getSelectedKey();
        return key != null ? getAttributes().getType(key) : null;
    }

    public boolean add(String name, String dataType) {
        return getAttributes().add(name, dataType);
    }

    public boolean remove(String name) {
        return getAttributes().remove(name);
    }


    public Set<String> getNames() { return getAttributes().getNames(); }


    public Class getEditorClass(String name) {
        String type = getAttributes().getType(name);
        if (type != null) {
            if (type.equalsIgnoreCase("boolean")) {
                return BooleanAsCheckBoxPropertyEditor.class;
            }
            if (type.equalsIgnoreCase("color")) {
                return ColorPropertyEditor.class;
            }
            if (type.equalsIgnoreCase("font")) {
                return FontPropertyEditor.class;
            }
            if (type.equalsIgnoreCase("integer")) {
                return IntegerPropertyEditor.class;
            }
            if (type.equalsIgnoreCase("double")) {
                return DoublePropertyEditor.class;
            }
            if (type.equalsIgnoreCase("xquery")) {
                return XQueryPropertyEditor.class;
            }
            if (type.equalsIgnoreCase("text")) {
                return TextPropertyEditor.class;
            }
            if (isEnumeration(type)) {
                return UserDefinedListPropertyEditor.class;
            }
        }
        return StringPropertyEditor.class;   // default
    }


    public Class getRendererClass(String name) {
        String type = getAttributes().getType(name);
        if (type != null) {
            if (type.equalsIgnoreCase("boolean")) {
                return BooleanCellRenderer.class;
            }
            if (type.equalsIgnoreCase("color")) {
                return ColorPropertyRenderer.class;
            }
            if (type.equalsIgnoreCase("font")) {
                return FontColorRenderer.class;
            }
        }
        return DefaultCellRenderer.class;   // default
    }


    private void setAttributeMap(YAttributeMap map) {
        _attributeMap = map;
        addDynamicAttributes();
    }

    private String objToString(String key, Object value) {
        if (value == null) return null;
        String type = getAttributes().getType(key);
        if (type == null) {
            return value.toString();
        }
        if (type.equalsIgnoreCase("boolean")) {
            return value.toString();
        }
        if (type.equalsIgnoreCase("color")) {
            return PropertyUtil.colorToHex((Color) value);
        }
        if (type.equalsIgnoreCase("font")) {
            Font font = (Font) value;
            return font.getFamily() + "," + font.getStyle() + "," + font.getSize();
        }
        return String.valueOf(value);
    }


    private Object strToObject(String key, String value) {
        String type = getAttributes().getType(key);
        if (type == null) {
            return value;
        }
        if (type.equalsIgnoreCase("boolean")) {
            if (value == null) return Boolean.FALSE;
            return Boolean.valueOf(value);
        }
        if (type.equalsIgnoreCase("integer")) {
            return StringUtil.strToInt(value, 0);
        }
        if (type.equalsIgnoreCase("double")) {
            return StringUtil.strToDouble(value, 0);
        }
        if (type.equalsIgnoreCase("color")) {
            if (value == null) return null;
            return PropertyUtil.hexToColor(value);
        }
        if (type.equalsIgnoreCase("font")) {
            if (value == null) return null;
            String[] args = value.split(",");
            return new Font(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        }
        return value;
    }


    public String[] getListItems(String key) {
        String type = getAttributes().getType(key);
        if (type == null) return new String[0];

        String[] rawItems = type.split("\\{|\\}|,");

        // remove 'enumeration'
        String[] items = new String[rawItems.length -1];
        System.arraycopy(rawItems, 1, items,  0, rawItems.length -1);

        return items;
    }


    private boolean isEnumeration(String type) {
        return type.startsWith("enumeration{");
    }

    private boolean isDynamic(String type) {
        return type.startsWith("dynamic{");
    }

    private String getSelectedKey() {
        String key =  _sheet.getPropertyBeingRead();
        if (key == null) key = _sheet.getSelectedPropertyName();
        return key;
    }


    private UserDefinedAttributes getAttributes() {
        switch (_ownerClass) {
            case Decomposition:
                return DecompositionUserDefinedAttributes.getInstance();
            case Parameter:
                return VariableUserDefinedAttributes.getInstance();
        }
        return null;
    }


    /**
     * A dynamic attribute has the form 'name=dynamic{property}', where 'property' is
     * the name of a data member of either a YDecomposition or YVariable, depending on
     * which kind of object the attribute is added to. At runtime, the value will
     * be assigned whatever the value of the data member is when the attribute is
     * accessed.
     */
    private void addDynamicAttributes() {
        for (Map.Entry<String, String> entry : getAttributes().getMap().entrySet()) {
            String type = entry.getValue();
            if (isDynamic(type)) {
                _attributeMap.put(entry.getKey(), type);
            }
        }
    }

}
