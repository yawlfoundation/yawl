package org.yawlfoundation.yawl.editor.ui.swing.data;

import org.yawlfoundation.yawl.editor.ui.data.DataVariable;
import org.yawlfoundation.yawl.editor.ui.data.Decomposition;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

import javax.swing.*;
import java.awt.*;

/**
 * @author Mike Fowler
 *         Date: Oct 28, 2005
 * @author Michael Adams updated for 2.1
 */
public class ExtendedAttribute implements Comparable<ExtendedAttribute> {

    public static final int DEFAULT_ATTRIBUTE = 0;
    public static final int USER_ATTRIBUTE = 1;
    public static final int SYSTEM_ATTRIBUTE = 2;

    protected static final Font componentFont = new Font("SansSerif", Font.PLAIN, 12);

    private DataVariable variable;
    private Decomposition decomposition;
    private NetGraph graph;
    private String name;
    private String type;
    private String value;
    private JComponent component;
    private ExtendedAttributeGroup group;
    private int attributeType;

    /**
     * Constructor for variable level attributes
     *
     * @param variable
     * @param name
     * @param type
     * @param value    Passed in separately as ExtendedAttribute is not responsible for
     * keeping the DataVariable consistent.
     */
    public ExtendedAttribute(DataVariable variable, Decomposition decomposition,
                             String name, String type, String value) {
        this.variable = variable;
        this.decomposition = decomposition;
        this.name = name;
        this.type = type;
        this.value = value;
        getComponent();
    }


    public ExtendedAttribute(DataVariable variable, Decomposition decomposition,
                             String name, String type, String value,
                             ExtendedAttributeGroup group) {
        this(variable, decomposition, name, type, value);
        this.group = group;
    }


    /**
     * Constructor for decomposition level attributes
     *
     * @param decomposition
     * @param name
     * @param type
     * @param value Passed in separately as ExtendedAttribute is not responsible for
     * keeping the Decomposition consistent.
     */
    public ExtendedAttribute(NetGraph graph, Decomposition decomposition, String name,
                             String type, String value) {
        this.graph = graph;
        this.decomposition = decomposition;
        this.name = name;
        this.type = type;
        this.value = value;
        getComponent();
    }

    public ExtendedAttribute(NetGraph graph, Decomposition decomposition, String name,
                             String type, String value, ExtendedAttributeGroup group) {
        this(graph, decomposition, name, type, value);
        this.group = group;
    }


    public JComponent getComponent() {
        if (component != null) return component;

        type = type.trim();
        if (type.equalsIgnoreCase("colour")) type = "color";

        if (type.equalsIgnoreCase("boolean")) {
            JCheckBox box = new JCheckBox();
            box.setSelected(value.trim().equalsIgnoreCase("true"));
            box.setHorizontalAlignment(SwingConstants.CENTER);
            component = box;
        }
        else if (type.matches("enumeration\\s*\\{.*\\}")) {
            String[] values = type.split("\\{|\\}|,");
            JComboBox box = new JComboBox();
            for (int i = 1; i < values.length; i++) {             // 0 = "enumeration"
                String valueAdded = values[i].trim();
                box.addItem(valueAdded);
            }
            if ((value != null) && value.trim().length() > 0) {
                box.setSelectedItem(value.trim());
            }
            else {
                box.setSelectedIndex(0);
            }
            component = box;
        }
        else if (type.matches("attribute\\s*\\{.*\\}")) {
            String property = type.substring(type.indexOf("${") + 2, type.indexOf("}"));
            attributeType = SYSTEM_ATTRIBUTE;
            if (variable != null) {
                variable.setAttribute(name, new DynamicValue(property, variable));
            }
            else {
                decomposition.setAttribute(name, new DynamicValue(property, decomposition));
            }
        }
        else if (isNumericType()) {
            component = makeSpinner();
        }
        else {
            component = new JTextField(value);
        }
        if (component != null) component.setFont(componentFont);

        return component;
    }

    public String toString() {
        return getValue();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean hasExtendedField() {
        return ! (type.equals("string") || isNumericType());
    }

    public boolean isNumericType() {
        return type.startsWith("integer") || type.startsWith("double");
    }


    public int getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(int type) {
        attributeType = type;
        if (type == USER_ATTRIBUTE) {
            component.setForeground(Color.BLUE);
        }
    }

    public ExtendedAttributeGroup getGroup() {
        return group;
    }

    public void setGroup(ExtendedAttributeGroup group) {
        this.group = group;
    }

    public String getValue() {
        if (component != null) {
            if (component instanceof JTextField) {
                if (isNumericType()) {
                    value = validateNumeric(((JTextField) component).getText(), value);
                }
                else value = ((JTextField) component).getText();
            }
            else if (component instanceof JCheckBox) {
                value = String.valueOf(((JCheckBox) component).isSelected());
            }
            else if (component instanceof JComboBox) {
                value = ((JComboBox) component).getSelectedItem().toString();
            }
            else if (component instanceof JSpinner) {
                value = ((JSpinner) component).getValue().toString();
            }
        }
        return value;
    }


    public void setValue(String value) {
        this.value = value;
        if (component != null) {
            if (component instanceof JTextField) {
                ((JTextField) component).setText(value);
            }
            else if (component instanceof JCheckBox) {
                ((JCheckBox) component).setSelected(value.equalsIgnoreCase("true"));
            }
            else if (component instanceof JComboBox) {
                ((JComboBox) component).setSelectedItem(value);
            }
        }
    }

    public Decomposition getDecomposition() {
        if (graph != null) {
            return graph.getNetModel().getDecomposition();
        }
        else {
            return decomposition;
        }
    }


    private String validateNumeric(String numStr, String fallback) {
        if (numStr.length() == 0) return numStr;        // if removed accept empty value
        try {
            if (type.startsWith("integer")) {
                new Integer(numStr);
            }
            else {
                new Double(numStr);
            }
            return numStr;
        }
        catch (NumberFormatException nfe) {
            return fallback;
        }
    }

    
    public int compareTo(ExtendedAttribute other) {
        return name.compareTo(other.getName());
    }

    public Color hexToColour(String hexStr) {

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

    public String colourToHex(Color color) {
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


    /* if the current value is an empty string, value defaults to min */
    private JComponent makeSpinner() {
       boolean intType = type.trim().startsWith("integer");
       if (type.matches("^.*\\s*\\{.*\\}")) {
            String[] params = type.split("^.*\\{\\s*|\\s*,\\s*|\\s*\\}\\s*");
            if (params.length == 4) {
                SpinnerNumberModel model;
                if (intType) {
                    try {
                        int min = new Integer(params[1]);
                        int max = new Integer(params[2]);
                        int step = new Integer(params[3]);
                        int val = (value.length() == 0) ? min : new Integer(value);
                        model = new SpinnerNumberModel(val, min, max, step);
                    }
                    catch (NumberFormatException nfe) {
                        return new JTextField(value);
                    }
                }
                else {
                    try {
                        double min = new Double(params[0]);
                        double max = new Double(params[1]);
                        double step = new Double(params[2]);
                        double val = (value.length() == 0) ? min : new Double(value);
                        model = new SpinnerNumberModel(val, min, max, step);
                    }
                    catch (NumberFormatException nfe) {
                        return new JTextField(value);
                    }
                }
                return new JSpinner(model);
            }
        }
        return new JTextField(value);
    }

}
