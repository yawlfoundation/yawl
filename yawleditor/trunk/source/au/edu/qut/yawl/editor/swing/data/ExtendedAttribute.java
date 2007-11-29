package au.edu.qut.yawl.editor.swing.data;

import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.Decomposition;
import au.edu.qut.yawl.editor.net.NetGraph;

import javax.swing.*;

/**
 * @author Mike Fowler
 *         Date: Oct 28, 2005
 */
public class ExtendedAttribute
{
    public static final int USER_ATTRIBUTE = 1;
    public static final int SYSTEM_ATTRIBUTE = 2;

    private DataVariable variable;
    private Decomposition decomposition;
    private NetGraph graph;
    private String name;
    private String type;
    private String value;
    private JComponent component;
    private int attribute = USER_ATTRIBUTE;

    /**
     * @param variable
     * @param name
     * @param type
     * @param value    Passed in separately as ExtendedAttribute is not responsible for keeping the DataVariable consistent.
     */
    public ExtendedAttribute(DataVariable variable, Decomposition decomposition, String name, String type, String value)
    {
        this.variable = variable;
        this.decomposition = decomposition;
        this.name = name;
        this.type = type;
        this.value = value;
        getComponent();
    }

    /**
     * @param decomposition
     * @param name
     * @param type
     * @param value         Passed in separately as ExtendedAttribute is not responsible for keeping the Decomposition consistent.
     */
    public ExtendedAttribute(NetGraph graph, Decomposition decomposition, String name, String type, String value)
    {
        this.graph = graph;
        this.decomposition = decomposition;
        this.name = name;
        this.type = type;
        this.value = value;
        getComponent();
    }

    public JComponent getComponent()
    {
        if (component == null)
        {
            type = type.trim().toLowerCase();
            if (type.equalsIgnoreCase("boolean"))
            {
                JComboBox box = new JComboBox();
                box.addItem("");
                box.addItem("true");
                box.addItem("false");

                if (value.trim().equalsIgnoreCase("true"))
                {
                    box.setSelectedIndex(1);
                }
                else
                {
                    box.setSelectedIndex(2);
                }

                component = box;
            }
            else if (type.startsWith("enumeration"))
            {
                if (type.indexOf("{") == -1 || type.indexOf("}") == -1)
                {
                    //todo ill-formed enumeration. exception?
                    component = new JTextField(value);
                }
                else
                {
                    String[] values = type.substring(type.indexOf("{") + 1, type.indexOf("}")).split(",");
                    JComboBox box = new JComboBox();
                    box.addItem("");
                    for (int i = 0; i < values.length; i++)
                    {
                        box.addItem(values[i].trim());
                        if (value.trim().equals(values[i].trim()))
                        {
                            box.setSelectedIndex(i + 1);
                        }
                    }

                    component = box;
                }
            }
            else if (type.startsWith("attribute"))
            {
                if (type.indexOf("${") == -1 || type.indexOf("}") == -1)
                {
                    //todo ill-formed attribute. exception?
                    component = new JTextField(value);
                }
                else
                {
                    String property = type.substring(type.indexOf("${") + 2, type.indexOf("}"));

                    attribute = SYSTEM_ATTRIBUTE;
                    if (variable != null)
                    {
                        variable.setAttribute(this.name, new DynamicValue(property, variable));
                    }
                    else
                    {
                        decomposition.setAttribute(this.name, new DynamicValue(property, decomposition));
                    }
                }
            }
            else if (type.equalsIgnoreCase("xquery"))
            {
                component = new XQuery(value);
            }
            else
            {
                component = new JTextField(value);
            }
        }
        return component;

    }

    public String toString()
    {
        return getValue();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public int getAttribute()
    {
        return attribute;
    }

    public String getValue()
    {
        if (component != null)
        {
            if (component instanceof JTextField)
            {
                value = ((JTextField) component).getText();
            }
            else if (component instanceof JComboBox)
            {
                value = ((JComboBox) component).getSelectedItem().toString();
            }
        }
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
        if (component != null)
        {
            if (component instanceof JTextField)
            {
                ((JTextField) component).setText(value);
            }
            else if (component instanceof JButton)
            {
                ((JButton) component).setText(value);
            }
            else if (component instanceof JComboBox)
            {
                //todo
            }
        }
    }

    public Decomposition getDecomposition()
    {
        if (graph != null)
        {
            return graph.getNetModel().getDecomposition();
        }
        else
        {
            return decomposition;
        }
    }
}
