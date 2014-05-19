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

import com.l2fprod.common.propertysheet.*;
import org.yawlfoundation.yawl.editor.ui.properties.editor.ColorPropertyEditor;
import org.yawlfoundation.yawl.editor.ui.properties.editor.DecompositionNameEditor;
import org.yawlfoundation.yawl.editor.ui.properties.editor.FontPropertyEditor;
import org.yawlfoundation.yawl.editor.ui.properties.editor.SubNetNameEditor;
import org.yawlfoundation.yawl.editor.ui.util.UserSettings;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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

    private final Set<String> _readOnlyProperties;

    public YPropertySheet() {
        super();
        setTable(new YPropertySheetTable());
        _readOnlyProperties = new HashSet<String>();
        setMode(PropertySheet.VIEW_AS_CATEGORIES);
        setDescriptionVisible(UserSettings.getShowPropertyDescriptions());
        setSortingCategories(true);
        setSortingProperties(true);
        setRestoreToggleStates(true);
        setToolBarVisible(false);
        setMinimumSize(new Dimension(100, 250));
        setPreferredSize(new Dimension(200, 250));
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

    public void refresh() {
        validate();
        repaint();
    }


    private void registerGlobalEditors() {
        PropertyEditorRegistry editorFactory = (PropertyEditorRegistry) getEditorFactory();
        editorFactory.registerEditor(Color.class, new ColorPropertyEditor());
        editorFactory.registerEditor(Font.class, new FontPropertyEditor());
        editorFactory.registerEditor(FontColor.class, new FontPropertyEditor());
    }

    protected void pause(long milliseconds) {
        Object lock = new Object();
        long now = System.currentTimeMillis();
        long finishTime = now + milliseconds;
        while (now < finishTime) {
            long timeToWait = finishTime - now;
            synchronized (lock) {
                try {
                    lock.wait(timeToWait);
                }
                catch (InterruptedException ex) {
                }
            }
            now = System.currentTimeMillis();
        }
    }


    /*****************************************************************************/

    /**
     * Override of table class to allow provision to disable for read-only properties
     * on the fly
     */
    protected class YPropertySheetTable extends PropertySheetTable {

        public YPropertySheetTable() {
            super(new YPropertySheetTableModel());
            setGridColor(new Color(220,220,220));
        }


        // override to allow checking against read-only list
        public boolean isCellEditable(int row, int column) {
            return super.isCellEditable(row, column) && ! isReadOnly(row);
        }


        public TableCellRenderer getCellRenderer(int row, int column) {
            return getCellRenderer(row, column, 0);
        }

        // override to avoid IndexOutOFBoundsExceptions in super class
        public TableCellRenderer getCellRenderer(int row, int column, int threshold) {
            if (threshold >= 2) return new DefaultTableCellRenderer();
            try {
                return super.getCellRenderer(row, column);
            }
            catch (IndexOutOfBoundsException ioobe) {
                pause(20);                 // wait a bit for threads to catch up
                return getCellRenderer(row, column, ++threshold);     // & retry
            }
        }


        // override to flag property being read for user-defined attributes
        public TableCellEditor getCellEditor(int row, int column) {
            if (column == 0) { return null; }

            PropertySheetTableModel.Item item = getSheetModel().getPropertySheetElement(row);
            if (!item.isProperty()) return null;

            TableCellEditor result = null;
            Property property = item.getProperty();
            PropertyEditor editor = getEditorFactory().createPropertyEditor(property);
            if (editor != null) {
                result = new CellEditorAdapter(editor);

                // Remove 'Rename' item from decomposition name combo if name is 'None'
                if (editor instanceof DecompositionNameEditor) {
                    ((DecompositionNameEditor) editor).rationaliseItems(property);
                }
                if (editor instanceof SubNetNameEditor) {
                    ((SubNetNameEditor) editor).rationaliseItems(property);
                }
            }
            return result;
        }

        // override to allow checking against read-only list
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component component = null;
            if (getSheetModel().getPropertyCount() > 0) {
                component = renderer.getTableCellRendererComponent(this,
                    getValueAt(row, column), isCellSelected(row, column),
                    false, row, column);

                // disable for read-only
                PropertySheetTableModel.Item item = null;
                int threshold = 0;
                while (item == null && threshold < 10) {
                    try {
                        item = getSheetModel().getPropertySheetElement(row);
                    }
                    catch (IndexOutOfBoundsException ioobe) {
                        pause(100);
                        threshold++;
                    }
                }

                if (item != null && item.isProperty()) {
                    component.setEnabled(item.getProperty().isEditable() && !isReadOnly(row));
                }
            }

            return component != null ? component : new JLabel();
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


    class YPropertySheetTableModel extends PropertySheetTableModel {

        public YPropertySheetTableModel() {
            super();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return getValueAt(rowIndex, columnIndex, 0);
        }

        public Object getValueAt(int rowIndex, int columnIndex, int threshold) {
            if (threshold >= 10) return null;
            try {
                return super.getValueAt(rowIndex, columnIndex);
            }
            catch (IndexOutOfBoundsException ioobe) {
                pause(100);
                return getValueAt(rowIndex, columnIndex, ++threshold);
            }
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

        final java.util.List<String> categories = Arrays.asList("Specification", "Net", "Task",
                "Condition", "Flow", "Decomposition", "Ext. Attributes");

        public int compare(String s1, String s2) {
            if (s1 == null) return s2 == null ? 0 : -1;
            return categories.indexOf(s1) - categories.indexOf(s2);
        }
    }

}
