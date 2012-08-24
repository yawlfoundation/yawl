package org.yawlfoundation.yawl.editor.ui.properties;

import com.l2fprod.common.propertysheet.*;
import org.yawlfoundation.yawl.editor.ui.properties.editor.ColorPropertyEditor;
import org.yawlfoundation.yawl.editor.ui.properties.editor.FontPropertyEditor;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.PropertyEditor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 13/07/12
 */
public class YPropertySheet extends PropertySheetPanel {

    private Set<String> _readOnlyProperties;
    private String propertyBeingRead;

    public YPropertySheet() {
        setTable(new YPropertySheetTable());
        _readOnlyProperties = new HashSet<String>();
        setMode(PropertySheet.VIEW_AS_CATEGORIES);
        setDescriptionVisible(true);
        setSortingCategories(true);
        setSortingProperties(true);
        setRestoreToggleStates(false);
        setToolBarVisible(false);
        setMinimumSize(new Dimension(100, 250));
        setPreferredSize(new Dimension(250, 250));
        setPropertySortingComparator(new PropertySorter());
        setCategorySortingComparator(new CategorySorter());
        registerGlobalEditors();
    }


    public void addReadOnly(String propertyName) {
        _readOnlyProperties.add(propertyName);
    }

    public void removeReadOnly(String propertyName) {
        _readOnlyProperties.remove(propertyName);
    }

    public void setReadOnly(String propertyName, boolean isReadOnly) {
        if (isReadOnly) {
            addReadOnly(propertyName);
        }
        else {
            removeReadOnly(propertyName);
        }
    }

    public void resetReadOnly() { _readOnlyProperties.clear(); }


    public void firePropertyChange(String propertyName, Object newValue) {
        for (Property property : getProperties()) {
            if (property.getName().equals(propertyName)) {
                property.setValue(newValue);
            }
        }
    }


    public void readFromObject(Object data) {
        // cancel pending edits
        getTable().cancelEditing();

        for (Property property : ((PropertySheetTableModel) getTable().getModel()).getProperties()) {
            propertyBeingRead = property.getDisplayName();
            property.readFromObject(data);
        }
        repaint();
    }


    public String getPropertyBeingRead() { return propertyBeingRead; }


    private void registerGlobalEditors() {
        PropertyEditorRegistry editorFactory = (PropertyEditorRegistry) getEditorFactory();
        editorFactory.registerEditor(Color.class, new ColorPropertyEditor());
        editorFactory.registerEditor(Font.class, new FontPropertyEditor());
        editorFactory.registerEditor(FontColor.class, new FontPropertyEditor());
    }


    /*****************************************************************************/

    /**
     * Override of table class to allow provision to disable for read-only properties
     * on the fly
     */
    private class YPropertySheetTable extends PropertySheetTable {

        public YPropertySheetTable() {
            super();
        }


        // override to allow checking against read-only list
        public boolean isCellEditable(int row, int column) {
            return super.isCellEditable(row, column) && ! isReadOnly(row);
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
            if (editor != null)
                result = new CellEditorAdapter(editor);

            return result;
        }

        // override to allow checking against read-only list
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component component = renderer.getTableCellRendererComponent(this,
                    getValueAt(row, column), isCellSelected(row, column),
                    false, row, column);

            // disable for read-only
            if (getSheetModel().getPropertyCount() > 0) {
                PropertySheetTableModel.Item item = getSheetModel().getPropertySheetElement(row);
                if (item.isProperty()) {
                    component.setEnabled(item.getProperty().isEditable() && !isReadOnly(row));
                }
            }

            return component;
        }


        /**
         * A row is deemed read-only if its property name is contained in the
         * current read-only properties list
         * @param row the row to check
         * @return true if this row is read-only
         */
        protected boolean isReadOnly(int row) {
            PropertySheetTableModel.Item item = getSheetModel().getPropertySheetElement(row);
            return item.isProperty() &&
                    _readOnlyProperties.contains(item.getProperty().getName());
        }

    }


    /**
     * Sorts properties by display name
     */
    public class PropertySorter implements Comparator<Property> {

        public int compare(Property prop1, Property prop2) {
            if (prop1 == null) return prop2 == null ? 0 : -1;   // deal with null props

            String cat1 = prop1.getCategory();                  // deal with null cats
            String cat2 = prop2.getCategory();
            if (cat1 == null) return cat2 == null ? 0 : -1;

            int result = cat1.compareTo(cat2) * -1;

            // if these 2 properties have the same category
            if (result == 0) {
                String name1 = prop1.getDisplayName();
                String name2 = prop2.getDisplayName();

                // sort lower case precedes upper case
                if (isLower(name1.charAt(0)) && ! isLower(name2.charAt(0))) {
                    result = -1;
                }
                else if (isLower(name2.charAt(0)) && ! isLower(name1.charAt(0))) {
                    result = 1;
                }

                // both names are the same case, so natural sort them
                else result = name1.compareTo(name2);
            }
            return result;
        }

        boolean isLower(char c) { return Character.isLowerCase(c); }
    }


    /**
     * Sorts categories in a fixed order
     */
    public class CategorySorter implements Comparator<String> {

        java.util.List<String> categories = Arrays.asList("Specification", "Net", "Task",
                "Condition", "Flow", "Decomposition", "Ext. Attributes");

        public int compare(String s1, String s2) {
            if (s1 == null) return s2 == null ? 0 : -1;
            return categories.indexOf(s1) - categories.indexOf(s2);
        }
    }

}
