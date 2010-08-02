package org.yawlfoundation.yawl.editor.swing.data;

import org.yawlfoundation.yawl.editor.YAWLEditor;
import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.Decomposition;
import org.yawlfoundation.yawl.editor.net.NetGraph;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * @author Mike Fowler
 *         Date: Oct 26, 2005
 * @author Michael Adams for 2.1 04/2010
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
        if (variable !=null && variable.getScope() != null) {
            this.decomposition = variable.getScope().getDecomposition();
            loadDefaultProperties();
            loadUserDefinedProperties();
            variable.setAttributes(removeDefunctAttributes(variable.getAttributes()));
        }
    }

    public ExtendedAttributesTableModel(Decomposition decomposition, NetGraph graph)
            throws IOException {
        this.decomposition = decomposition;
        this.graph = graph;
        if (this.decomposition != null) {
            loadDefaultProperties();
            loadUserDefinedProperties();
            decomposition.setAttributes(removeDefunctAttributes(decomposition.getAttributes()));
        }
    }

    private void loadUserDefinedProperties() throws IOException {
        Preferences prefs = Preferences.userNodeForPackage(YAWLEditor.class);
        String streamPath = (variable != null) ?
                prefs.get("ExtendedAttributeVariableFilePath",
                                     DataVariable.PROPERTY_LOCATION) :
                prefs.get("ExtendedAttributeDecompositionFilePath",
                                     Decomposition.PROPERTY_LOCATION);

        if (streamPath != null) {
          props = new Properties();

          try {
              props.load(new FileInputStream(streamPath));
              parseProperties();
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
        else if (decomposition != null) {
            parseDecompositionProperties();
        }
    }

    private void parseVariableProperties() {
        if (props != null) {
            Vector<ExtendedAttribute> udAttributes = new Vector<ExtendedAttribute>();
            for (Object o : props.keySet()) {
                String key = (String) o;
                String type = props.getProperty(key).trim().toLowerCase();
                String value = null;
                if (variable != null) {
                    value = (String) variable.getAttributes().get(key);
                    if (value == null) value = "";
                }
                ExtendedAttribute attribute = new ExtendedAttribute(variable, decomposition,
                        key, type, value);
                if (attribute.getAttributeType() != ExtendedAttribute.SYSTEM_ATTRIBUTE) {
                    attribute.setAttributeType(ExtendedAttribute.USER_ATTRIBUTE);
                }
                udAttributes.add(attribute);
            }
            Collections.sort(udAttributes);
            rows.addAll(udAttributes);
        }
    }

    private void parseDecompositionProperties() {
        if (props != null) {
            Vector<ExtendedAttribute> udAttributes = new Vector<ExtendedAttribute>();
            for (Object o : props.keySet()) {  
                String key = (String) o;
                String type = props.getProperty(key).trim().toLowerCase();
                String value = null;
                if (decomposition != null) {
                    value = (String) decomposition.getAttributes().get(key);
                    if (value == null) value = "";
                }
                ExtendedAttribute attribute = new ExtendedAttribute(graph, decomposition,
                        key, type, value);
                if (attribute.getAttributeType() != ExtendedAttribute.SYSTEM_ATTRIBUTE) {
                    attribute.setAttributeType(ExtendedAttribute.USER_ATTRIBUTE);
                }
                udAttributes.add(attribute);
            }
            Collections.sort(udAttributes);
            rows.addAll(udAttributes);
        }
    }


    private Hashtable removeDefunctAttributes(Hashtable attributes) {
        List<String> toRemove = new ArrayList<String>();
        if (attributes != null) {
            for (Object o : attributes.keySet()) {
                String key = (String) o;
                if (! rowsContainsKey(key)) {
                   toRemove.add(key);
                }
            }
            for (String remKey : toRemove) {
                attributes.remove(remKey);
            }
        }
        return attributes;
    }


    private boolean rowsContainsKey(String key) {
        for (ExtendedAttribute exAttr : rows) {
            if (exAttr.getName().equals(key)) {
                return true;
            }
        }
        return false;
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
        return (columnIndex == 1) ? ExtendedAttribute.class : String.class;
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
        if (variable != null) {
            variable.setAttributes(removeDefunctAttributes(variable.getAttributes()));            
        }
    }

    public void setDecomposition(Decomposition decomposition) {
        this.decomposition = decomposition;
        loadAndParseProperties();
        if (decomposition != null) {
            decomposition.setAttributes(removeDefunctAttributes(decomposition.getAttributes()));
        }
    }

    private void loadAndParseProperties() {
        loadDefaultProperties();
        try {
            loadUserDefinedProperties();
        }
        catch (IOException e) {
            //todo anything at this exception?
        }
        finally {
            fireTableDataChanged();
        }
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
