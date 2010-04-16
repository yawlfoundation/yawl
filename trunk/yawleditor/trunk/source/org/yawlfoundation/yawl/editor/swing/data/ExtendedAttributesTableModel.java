package org.yawlfoundation.yawl.editor.swing.data;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.net.NetGraph;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
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
    private NetGraph graph = null;

    private Vector<ExtendedAttribute> rows = new Vector<ExtendedAttribute>();

    public ExtendedAttributesTableModel(DataVariable variable) throws IOException {
        this.variable = variable;
        if (variable !=null && variable.getScope() != null)
            this.decomposition = variable.getScope().getDecomposition();
        loadDefaultProperties();
        loadUserDefinedProperties();
        parseProperties();
    }

    public ExtendedAttributesTableModel(Decomposition decomposition, NetGraph graph)
            throws IOException {
        this.decomposition = decomposition;
        this.graph = graph;
        loadDefaultProperties();
        loadUserDefinedProperties();
        parseProperties();
    }

    private void loadUserDefinedProperties() throws IOException {
        String streamPath;

        if (variable != null) {
            streamPath = DataVariable.PROPERTY_LOCATION;
        }
        else {
            streamPath = Decomposition.PROPERTY_LOCATION;
        }

        if (streamPath != null) {
          props = new Properties();

          try {
              props.load(new FileInputStream(streamPath));
          }
          catch (FileNotFoundException fnfe) {
            // deliberately does nothing.   
          }
        }
    }


    private void loadDefaultProperties() {
        if (variable != null) {
            rows = new DefaultExtendedAttributes(decomposition, variable).getAttributes();
        }
        else
            rows = new DefaultExtendedAttributes(graph, decomposition).getAttributes();
    }

    private void parseProperties() {
        //todo what about properties that exist in the spec but not in the file?
        // ignore or display? ignoring for now...

        if (variable != null) {
            parseVariableProperties();
        }
        else {
            parseDecompositionProperties();
        }
    }

    private void parseVariableProperties() {
        if (props != null) {
            Vector<String> keys = new Vector<String>();
            for (Enumeration enumer = props.keys(); enumer.hasMoreElements();
                 keys.add(enumer.nextElement().toString()));

            Collections.sort(keys);
            for (String key : keys) {
                String type = props.getProperty(key).trim().toLowerCase();
                ExtendedAttribute att = new ExtendedAttribute(variable, decomposition,
                        key, type,
                        variable == null ? "" :
                        variable.getAttributes().get(key) == null ? "" :
                        variable.getAttributes().get(key).toString());
                if (att.getAttributeType() == ExtendedAttribute.USER_ATTRIBUTE) rows.add(att);
            }
        }
    }

    private void parseDecompositionProperties() {
        if (props != null) {
            rows = new Vector<ExtendedAttribute>();  //clear out old - if any
            Vector<String> keys = new Vector<String>();
            for (Enumeration enumer = props.keys(); enumer.hasMoreElements();
                 keys.add(enumer.nextElement().toString()));

            Collections.sort(keys);
            for (String key : keys) {
                String type = props.getProperty(key).trim().toLowerCase();
                ExtendedAttribute att = new ExtendedAttribute(graph, decomposition,
                        key, type,
                        decomposition == null ? "" :
                        decomposition.getAttributes().get(key) == null ? "" :
                        decomposition.getAttributes().get(key).toString());
                if (att.getAttributeType() == ExtendedAttribute.USER_ATTRIBUTE) rows.add(att);
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
    public int getColumnCount() {
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
    public int getRowCount() {
        return rows.size();
    }

    /**
     * Returns the value for the cell at <code>columnIndex</code> and
     * <code>rowIndex</code>.
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:  return rows.get(rowIndex).getName();
            case 1:  return rows.get(rowIndex);
            default: return ""; //todo exception?
        }
    }

    /**
     * @param aValue      value to assign to cell
     * @param rowIndex    row of cell
     * @param columnIndex column of cell
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            ExtendedAttribute attribute = rows.get(rowIndex);

            attribute.setValue(aValue.toString());

            if (variable != null) {
                variable.setAttribute(attribute.getName(), attribute.getValue());
                if (attribute.getGroup() != null) {
                    updateGroup(attribute.getGroup());
                }
            }
            else {
                decomposition.setAttribute(attribute.getName(), attribute.getValue());
                if (attribute.getGroup() != null) {
                    updateGroup(attribute.getGroup());
                }
            }
        }
        fireTableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex));
    }

    /**
     * @param column the column being queried
     * @return a string containing the default name of <code>column</code>
     */
    public String getColumnName(int column) {
        switch (column) {
            case 0: return "Name";
            case 1: return "Value";
            default: return "";
        }
    }

    /**
     * @param columnIndex the column being queried
     * @return the Object.class
     */
    public Class getColumnClass(int columnIndex) {
        return (columnIndex == 1) ? ExtendedAttribute.class : Object.class;
    }

    /**
     * Only the second column in this model is editable.
     *
     * @param rowIndex    the row being queried
     * @param columnIndex the column being queried
     * @return false
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    public void setVariable(DataVariable variable) {
        this.variable = variable;
        this.decomposition = variable.getScope().getDecomposition();
        loadAndParseProperties();
    }

    public void setDecomposition(Decomposition decomposition) {
        this.decomposition = decomposition;
        loadAndParseProperties();
    }

    private void loadAndParseProperties() {
        loadDefaultProperties();
        try {
            loadUserDefinedProperties();
        }
        catch (IOException e) {
            //todo anything at this exception?
        }
        parseProperties();
    }


    private void updateGroup(ExtendedAttributeGroup group) {
        for (ExtendedAttribute attribute : group) {
            if (variable != null) {
                variable.setAttribute(attribute.getName(), attribute.getValue());
            }
            else {
                decomposition.setAttribute(attribute.getName(), attribute.getValue());                
            }
        }
    }
}
