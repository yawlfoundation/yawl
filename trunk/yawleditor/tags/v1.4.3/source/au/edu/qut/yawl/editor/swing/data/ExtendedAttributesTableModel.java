package au.edu.qut.yawl.editor.swing.data;

import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.Decomposition;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

/**
 * @author Mike Fowler
 *         Date: Oct 26, 2005
 */
public class ExtendedAttributesTableModel extends AbstractTableModel
{
    public static final int NUM_COLUMNS = 2;

    private Properties props = null;

    private DataVariable variable = null;
    private Decomposition decomposition = null;

    private Vector rows = new Vector();

    public ExtendedAttributesTableModel(DataVariable variable) throws IOException
    {
        this.variable = variable;
        loadProperties();
        parseProperties();
    }

    public ExtendedAttributesTableModel(Decomposition decomposition) throws IOException
    {
        this.decomposition = decomposition;
        loadProperties();
        parseProperties();
    }

    private void loadProperties() throws IOException
    {
        InputStream stream;

        if (variable != null)
        {
            stream = getClass().getResourceAsStream(DataVariable.PROPERTY_LOCATION);
        }
        else
        {
            stream = getClass().getResourceAsStream(Decomposition.PROPERTY_LOCATION);
        }

        if (stream != null)
        {
            props = new Properties();
            props.load(stream);
        }
    }

    private void parseProperties()
    {
        //todo what about properties that exist in the spec but not in the file? ignore or display? ignoring for now...

        if (variable != null)
        {
            parseVariableProperties();
        }
        else
        {
            parseDecompositionProperties();
        }
    }

    private void parseVariableProperties()
    {
        if (props != null)
        {
            rows = new Vector();  //clear out old - if any
            for (Enumeration enumerer = props.keys(); enumerer.hasMoreElements();)
            {
                String name =
                        enumerer.nextElement().toString();
                String type = props.getProperty(name).trim().toLowerCase();
                ExtendedAttribute att = new ExtendedAttribute(variable, name, type, variable == null ? "" :
                                                                                    variable.getAttributes().get(name) == null ? "" :
                                                                                    variable.getAttributes().get(name).toString());
                if (att.getAttribute() == ExtendedAttribute.USER_ATTRIBUTE) rows.add(att);
            }
        }
    }

    private void parseDecompositionProperties()
    {
        if (props != null)
        {
            rows = new Vector();  //clear out old - if any
            for (Enumeration enumerer = props.keys(); enumerer.hasMoreElements();)
            {
                String name =
                        enumerer.nextElement().toString();
                String type = props.getProperty(name).trim().toLowerCase();
                ExtendedAttribute att = new ExtendedAttribute(decomposition, name, type, decomposition == null ? "" :
                                                                                         decomposition.getAttributes().get(name) == null ? "" :
                                                                                         decomposition.getAttributes().get(name).toString());
                if (att.getAttribute() == ExtendedAttribute.USER_ATTRIBUTE) rows.add(att);
            }
        }
    }

    /**
     * Returns the number of columns in the model. A
     * <code>JTable</code> uses this method to determine how many columns it
     * should create and display by default.
     *
     * @return the number of columns in the model
     * @see #getRowCount
     */
    public int getColumnCount()
    {
        return NUM_COLUMNS;
    }

    /**
     * Returns the number of rows in the model. A
     * <code>JTable</code> uses this method to determine how many rows it
     * should display.  This method should be quick, as it
     * is called frequently during rendering.
     *
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    public int getRowCount()
    {
        return rows.size();
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     */
    public Object getValueAt(int rowIndex, int columnIndex)
    {
        switch (columnIndex)
        {
            case 0:
                return ((ExtendedAttribute) rows.get(rowIndex)).getName();
            case 1:
                return (ExtendedAttribute) rows.get(rowIndex);
            default:
                return ""; //todo exception?
        }
    }

    /**
     * @param aValue      value to assign to cell
     * @param rowIndex    row of cell
     * @param columnIndex column of cell
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex)
    {
        if (columnIndex == 1)
        {
            ExtendedAttribute hint = (ExtendedAttribute) rows.get(rowIndex);
            hint.setValue(aValue.toString());

            if (variable != null)
            {
                variable.setAttribute(hint.getName(), hint.getValue());
            }
            else
            {
                decomposition.setAttribute(hint.getName(), hint.getValue());
            }
        }
        fireTableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex));
    }

    /**
     * @param column the column being queried
     * @return a string containing the default name of <code>column</code>
     */
    public String getColumnName(int column)
    {
        switch (column)
        {
            case 0:
                return "Name";
            case 1:
                return "Value";
            default:
                return "";
        }
    }

    /**
     * @param columnIndex the column being queried
     * @return the Object.class
     */
    public Class getColumnClass(int columnIndex)
    {
        switch (columnIndex)
        {
            case 1:
                return ExtendedAttribute.class;
            default:
                return Object.class;
        }
    }

    /**
     * Only the second column in this model is editable.
     *
     * @param rowIndex    the row being queried
     * @param columnIndex the column being queried
     * @return false
     */
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return columnIndex == 1;
    }

    public void setVariable(DataVariable variable)
    {
        this.variable = variable;
        try
        {
            loadProperties();
        }
        catch (IOException e)
        {
            //todo anything at this exception?
        }
        parseProperties();
    }

    public void setDecomposition(Decomposition decomposition)
    {
        this.decomposition = decomposition;
        try
        {
            loadProperties();
        }
        catch (IOException e)
        {
            //todo anything at this exception?
        }
        parseProperties();
    }
}
