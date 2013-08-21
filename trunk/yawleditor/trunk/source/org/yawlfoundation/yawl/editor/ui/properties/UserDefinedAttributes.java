package org.yawlfoundation.yawl.editor.ui.properties;

import com.l2fprod.common.beans.editor.BooleanAsCheckBoxPropertyEditor;
import com.l2fprod.common.beans.editor.DoublePropertyEditor;
import com.l2fprod.common.beans.editor.IntegerPropertyEditor;
import com.l2fprod.common.beans.editor.StringPropertyEditor;
import com.l2fprod.common.swing.renderer.BooleanCellRenderer;
import com.l2fprod.common.swing.renderer.ColorCellRenderer;
import com.l2fprod.common.swing.renderer.DefaultCellRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.editor.*;
import org.yawlfoundation.yawl.editor.ui.util.FileUtilities;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.util.StringUtil;

import java.awt.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 25/07/12
 */
public class UserDefinedAttributes {

    private Map<String, String> _typeMap;     // name, type
    private YAttributeMap _attributeMap;
    private UserDefinedAttributesPropertySheet _sheet;

    private OwnerType _ownerClass;

    public enum OwnerType { Decomposition, Parameter }


    private UserDefinedAttributes(UserDefinedAttributesPropertySheet sheet) {
        _sheet = sheet;
        _sheet.setUserDefinedAttributes(this);
        _typeMap = new Hashtable<String, String>();
    }

    public UserDefinedAttributes(UserDefinedAttributesPropertySheet sheet,
                                 YDecomposition decomposition) {
        this(sheet);
        _attributeMap = decomposition.getAttributes();
        _ownerClass = OwnerType.Decomposition;
        loadAttributes();
    }


    public UserDefinedAttributes(UserDefinedAttributesPropertySheet sheet,
                                 YParameter parameter) {
        this(sheet);
        _attributeMap = parameter.getAttributes();
        _ownerClass = OwnerType.Parameter;
        loadAttributes();
    }


    public OwnerType getOwnerClass() {
        return _ownerClass;
    }

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


    public String getType() {
        String key = getSelectedKey();
        return key != null ? _typeMap.get(key) : null;
    }


    public Map<String, String> getTypeMap() { return _typeMap; }

    public Set<String> getTypeNames() { return _typeMap.keySet(); }


    public Class getEditorClass(String typeName) {
        String type = _typeMap.get(typeName);
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
        String type = _typeMap.get(name);
        if (type != null) {
            if (type.equalsIgnoreCase("boolean")) {
                return BooleanCellRenderer.class;
            }
            if (type.equalsIgnoreCase("color")) {
                return ColorCellRenderer.class;
            }
            if (type.equalsIgnoreCase("font")) {
                return FontPropertyRenderer.class;
            }
        }
        return DefaultCellRenderer.class;   // default
    }


    private String objToString(String key, Object value) {
        if (value == null) return null;
        String type = _typeMap.get(key);
        if (type == null) {
            return value.toString();
        }
        if (type.equalsIgnoreCase("boolean")) {
            return value.toString();
        }
        if (type.equalsIgnoreCase("color")) {
            return colorToHex((Color) value);
        }
        if (type.equalsIgnoreCase("font")) {
            Font font = (Font) value;
            return font.getFamily() + "," + font.getStyle() + "," + font.getSize();
        }
        return String.valueOf(value);
    }


    private Object strToObject(String key, String value) {
        String type = _typeMap.get(key);
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
            return hexToColor(value);
        }
        if (type.equalsIgnoreCase("font")) {
            if (value == null) return null;
            String[] args = value.split(",");
            return new Font(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        }
        return value;
    }


    public String[] getListItems(String key) {
        String type = _typeMap.get(key);
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

    private String getSelectedKey() {
        return _sheet.getPropertyBeingRead();
    }


    protected Color hexToColor(String hexStr) {

        // expects the format #123456
        if ((hexStr == null) || (hexStr.length() < 7)) {
            return Color.WHITE;
        }

        try {
            int r = Integer.valueOf(hexStr.substring(1, 3), 16);
            int g = Integer.valueOf(hexStr.substring(3, 5), 16);
            int b = Integer.valueOf(hexStr.substring(5, 7), 16);
            return new Color(r, g, b);
        }
        catch (NumberFormatException nfe) {
            return Color.WHITE;
        }
    }

    protected String colorToHex(Color color) {
        String hex = "#";
        hex += intToHex(color.getRed());
        hex += intToHex(color.getGreen());
        hex += intToHex(color.getBlue());
        return hex;
    }

    private String intToHex(int i) {
        String hex = Integer.toHexString(i).toUpperCase();
        if (hex.length() == 1) hex = "0" + hex;
        return hex;
    }


    private void loadAttributes() {
        String path = null;
        switch (_ownerClass) {
            case Decomposition: {
                path = UserSettings.getDecompositionAttributesFilePath();
                if (path == null) {
                    path = FileUtilities.getDecompositionExtendeAttributePath();
                }
                break;
            }
            case Parameter:
                path = UserSettings.getVariableAttributesFilePath();
                if (path == null) {
                    path = FileUtilities.getVariableExtendedAttributePath();
                }
                break;
        }
        load(path);
    }


    private void load(String path) {
        try {
            InputStream in = new FileInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = reader.readLine();
            while (line != null) {
                processLine(line);
                line = reader.readLine();
            }
        }
        catch (IOException ioe) {
            // "Error reading file");
        }
        // else "Error opening file");
    }


    private void processLine(String rawLine) {
        rawLine = rawLine.trim();

        // ignore comments and empty lines
        if ((rawLine.length() == 0) || rawLine.startsWith("#")) return;

        int equalsPos = rawLine.indexOf('=');
        if (equalsPos > -1) {
            String name = rawLine.substring(0, equalsPos).trim();
            String type = rawLine.substring(equalsPos + 1).trim();
            if (! (StringUtil.isNullOrEmpty(name) || StringUtil.isNullOrEmpty(type))) {
                _typeMap.put(name, type);
            }
        }
        // else "malformed entry";
    }

}
