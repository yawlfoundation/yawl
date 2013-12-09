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

import com.l2fprod.common.propertysheet.CellEditorAdapter;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetTableModel;
import org.yawlfoundation.yawl.editor.ui.properties.editor.UserDefinedListPropertyEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.PropertyEditor;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 21/08/13
 */
public class ExtendedAttributesPropertySheet extends YPropertySheet {

    private UserDefinedAttributesBinder udAttributes;
    private ExtendedAttributeNameLookup nameLookup;
    private String propertyBeingRead;

    private static final String UDA_PROPERTY_NAME = "UdAttributeValue";


    public ExtendedAttributesPropertySheet() {
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

        for (Property property : getTableModel().getProperties()) {
            propertyBeingRead = property.getDisplayName();
            property.readFromObject(data);
        }
        repaint();
    }

    public boolean removeProperty(String name) {
        for (Property property : getProperties()) {
            if (isUdaProperty(property) && property.getDisplayName().equals(name)) {
                removeProperty(property);
                return true;
            }
        }
        return false;
    }


    public boolean isUserDefinedAttributeSelected() {
        PropertySheetTableModel.Item item = getSelectedItem();
        return item != null && isUdaProperty(item.getProperty());
    }


    public boolean uniquePropertyName(String name) {
        for (Property property : getProperties()) {
            if (property.getName().equals(name) ||
                    (isUdaProperty(property) && property.getDisplayName().equals(name))) {
                return false;
            }
        }
        return true;
    }


    public Set<String> filterForCurrentPropertyNames(Set<String> unfiltered) {
        if (unfiltered == null) return null;
        if (nameLookup == null) nameLookup = new ExtendedAttributeNameLookup();
        Set<String> filtered = new HashSet<String>();
        for (Property property : getProperties()) {
            String propertyName = isUdaProperty(property) ?
                    property.getDisplayName() : property.getName();
            String attributeName = nameLookup.getAttributeName(propertyName);
            if (unfiltered.contains(attributeName)) {
                filtered.add(attributeName);
            }
        }
        return filtered;
    }


    public String getPropertyBeingRead() {
        return propertyBeingRead;
    }

    public String getSelectedPropertyName() {
        PropertySheetTableModel.Item item = getSelectedItem();
        return item != null ? item.getName() : null;
    }


    private PropertySheetTableModel.Item getSelectedItem() {
        return getTableModel().getPropertySheetElement(getTable().getSelectedRow());
    }

    private PropertySheetTableModel getTableModel() {
        return (PropertySheetTableModel) getTable().getModel();
    }


    private boolean isUdaProperty(Property property) {
        return property.getName().equals(UDA_PROPERTY_NAME);
    }


    /*************************************************************************/

    private class UDAPropertySheetTable extends YPropertySheetTable {

        final UDACellRenderer udaCellRenderer = new UDACellRenderer();

        UDAPropertySheetTable() {
            super();
        }

        // override to paint UDAs a different colour
        public TableCellRenderer getCellRenderer(int row, int column) {
            if (getSheetModel().getPropertyCount() > 0) {
                if (column == PropertySheetTableModel.NAME_COLUMN) {
                    PropertySheetTableModel.Item item = getSheetModel()
                          .getPropertySheetElement(row);
                    if (item.getProperty().getName().equals("UdAttributeValue")) {
                        return udaCellRenderer;
                    }
                }
                return super.getCellRenderer(row, column);
            }
            return getDefaultRenderer(String.class);
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

                if (editor instanceof UserDefinedListPropertyEditor) {
                    ((UserDefinedListPropertyEditor) editor).setAvailableValues(
                            udAttributes.getListItems(property.getDisplayName()));
                }
            }
            return result;
        }
    }


    class UDACellRenderer extends DefaultTableCellRenderer {

        final Color foreColor =  new Color(0,0,200);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                  int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
            setForeground(foreColor);
            setText(((PropertySheetTableModel.Item) value).getName());
            return this;
        }
    }

}
