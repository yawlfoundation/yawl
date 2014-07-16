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

package org.yawlfoundation.yawl.editor.ui.properties.extended;

import com.l2fprod.common.propertysheet.*;
import org.yawlfoundation.yawl.editor.ui.properties.FontColor;
import org.yawlfoundation.yawl.editor.ui.properties.NonNegativeInteger;
import org.yawlfoundation.yawl.editor.ui.properties.YPropertySheet;
import org.yawlfoundation.yawl.editor.ui.properties.dialog.ExtendedAttributesDialog;
import org.yawlfoundation.yawl.editor.ui.properties.editor.ColorPropertyRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.editor.FontColorRenderer;
import org.yawlfoundation.yawl.editor.ui.properties.editor.UserDefinedListPropertyEditor;
import org.yawlfoundation.yawl.editor.ui.properties.extended.editor.AttributeColorPropertyEditor;
import org.yawlfoundation.yawl.editor.ui.properties.extended.editor.AttributeFontPropertyEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.beans.PropertyEditor;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 21/08/13
 */
public class ExtendedAttributesPropertySheet extends YPropertySheet {

    private UserDefinedAttributesBinder udAttributes;
    private ExtendedAttributeNameLookup nameLookup;
    private ExtendedAttributesDialog dialog;
    private String propertyBeingRead;

    private static final String UDA_PROPERTY_NAME = "UdAttributeValue";


    public ExtendedAttributesPropertySheet(ExtendedAttributesDialog dialog) {
        super();
        this.dialog = dialog;
        setTable(new ExAttributePropertySheetTable());
        setSortingProperties(true);
        setPropertySortingComparator(new AttributeSorter());
        registerGlobalEditors();
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
        propertyBeingRead = null;
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
            if (property.getName().equalsIgnoreCase(name) ||
                    (isUdaProperty(property) &&
                            property.getDisplayName().equalsIgnoreCase(name))) {
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


    public String getPropertyBeingRead() { return propertyBeingRead; }


    public String getSelectedPropertyName() {
        PropertySheetTableModel.Item item = getSelectedItem();
        return item != null ? item.getName() : null;
    }


    public void showStatus(String status) { dialog.setStatus(status); }


    private PropertySheetTableModel.Item getSelectedItem() {
        int selectedRow = getTable().getSelectedRow();
        return selectedRow > -1 ?
                getTableModel().getPropertySheetElement(selectedRow) : null;
    }

    private PropertySheetTableModel getTableModel() {
        return (PropertySheetTableModel) getTable().getModel();
    }


    private boolean isUdaProperty(Property property) {
        return property.getName().equals(UDA_PROPERTY_NAME);
    }


    private void registerGlobalEditors() {
        PropertyEditorRegistry editorFactory = (PropertyEditorRegistry) getEditorFactory();
        editorFactory.registerEditor(Color.class, new AttributeColorPropertyEditor());
        editorFactory.registerEditor(Font.class, new AttributeFontPropertyEditor());
        editorFactory.registerEditor(FontColor.class, new AttributeFontPropertyEditor());

        PropertyRendererRegistry rendererFactory =
                (PropertyRendererRegistry) getRendererFactory();
        rendererFactory.registerRenderer(Color.class, new ColorPropertyRenderer());
        rendererFactory.registerRenderer(FontColor.class, new FontColorRenderer());
    }


    /*************************************************************************/

    private class ExAttributePropertySheetTable extends YPropertySheetTable {

        final AttributeCellRenderer exCellRenderer = new AttributeCellRenderer();

        ExAttributePropertySheetTable() {
            super();
        }

        // override to bold set attributes and paint UDAs a different colour
        public TableCellRenderer getCellRenderer(int row, int column) {
            if (getSheetModel().getPropertyCount() > 0) {
                if (column == PropertySheetTableModel.NAME_COLUMN) {
                    return exCellRenderer;
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
            showStatus(null);
            return result;
        }
    }


    /************************************************************************/

    class AttributeCellRenderer extends DefaultTableCellRenderer {

        final Color udaColor =  new Color(0,0,190);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                  int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);

            Property property = ((PropertySheetTableModel.Item) value).getProperty();
            if (hasSetValue(property.getValue())) {
                setFont(getFont().deriveFont(Font.BOLD));
            }
            if (property.getName().equals("UdAttributeValue")) {
                setForeground(udaColor);
            }
            else setForeground(Color.BLACK);

            setText(((PropertySheetTableModel.Item) value).getName());
            return this;
        }


        // return true if value not null or Boolean false
        private boolean hasSetValue(Object value) {
            if (value == null) return false;
            if (value instanceof Boolean) return (Boolean) value;
            if (value instanceof NonNegativeInteger) {
                return ((NonNegativeInteger) value).getValue() != null;
            }
            return true;
        }
    }


    /**********************************************************************/

    /**
     * Sorts properties by display name (case insensitive)
     */
    public class AttributeSorter implements Comparator<Property> {

        public int compare(Property prop1, Property prop2) {
            if (prop1 == null) return prop2 == null ? 0 : -1;   // deal with null props

            String name1 = prop1.getDisplayName().toLowerCase();
            String name2 = prop2.getDisplayName().toLowerCase();
            return name1.compareTo(name2);
        }
    }

}
