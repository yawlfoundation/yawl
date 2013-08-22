package org.yawlfoundation.yawl.editor.ui.properties;

import com.l2fprod.common.propertysheet.CellEditorAdapter;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;
import org.yawlfoundation.yawl.editor.ui.properties.editor.UserDefinedListPropertyEditor;

import javax.swing.table.TableCellEditor;
import java.beans.PropertyEditor;

/**
 * @author Michael Adams
 * @date 21/08/13
 */
public class UserDefinedAttributesPropertySheet extends YPropertySheet {

    private UserDefinedAttributesBinder udAttributes;
    private String propertyBeingRead;

    public UserDefinedAttributesPropertySheet() {
        super();
        setTable(new UDAPropertySheetTable());
        setSortingProperties(true);
        setPropertySortingComparator(new PropertySorter());
    }

    public void setUserDefinedAttributes(UserDefinedAttributesBinder attributes) {
        udAttributes = attributes;
    }

    public void readFromObject(Object data) {
        getTable().cancelEditing();    // cancel pending edits

        for (Property property : ((PropertySheetTableModel) getTable().getModel()).getProperties()) {
            propertyBeingRead = property.getDisplayName();
            property.readFromObject(data);
        }
        repaint();
    }

    public boolean removeProperty(String name) {
        for (Property property : getProperties()) {
            if (property.getName().equals("UdAttributeValue") &&
                    property.getDisplayName().equals(name)) {
                removeProperty(property);
                return true;
            }
        }
        return false;
    }


    public boolean uniquePropertyName(String name) {
        for (Property property : getProperties()) {
            if (property.getName().equals(name) ||
                    (property.getName().equals("UdAttributeValue") &&
                    property.getDisplayName().equals(name))) {
                return false;
            }
        }
        return true;
    }


    public String getPropertyBeingRead() { return propertyBeingRead; }


    /*************************************************************************/

    private class UDAPropertySheetTable extends YPropertySheetTable {

        UDAPropertySheetTable() {
            super();
        }


       // override to flag property being read for user-defined attributes
        public TableCellEditor getCellEditor(int row, int column) {
            if (column == 0) { return null; }

            PropertySheetTableModel.Item item = getSheetModel().getPropertySheetElement(row);
            if (!item.isProperty()) return null;

            TableCellEditor result = null;
            Property property = item.getProperty();
            propertyBeingRead = property.getDisplayName();
            PropertyEditor editor = getEditorFactory().createPropertyEditor(property);
            if (editor != null) {
                result = new CellEditorAdapter(editor);

                if (editor instanceof UserDefinedListPropertyEditor) {
                    ((UserDefinedListPropertyEditor) editor).setAvailableValues(
                            udAttributes.getListItems(propertyBeingRead));
                }
            }
            return result;
        }
    }

}
