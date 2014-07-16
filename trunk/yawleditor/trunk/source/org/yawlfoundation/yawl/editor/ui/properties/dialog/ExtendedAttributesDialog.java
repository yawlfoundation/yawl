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

package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.propertysheet.PropertySheet;
import org.yawlfoundation.yawl.editor.core.repository.Repo;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.*;
import org.yawlfoundation.yawl.editor.ui.properties.data.VariableRow;
import org.yawlfoundation.yawl.editor.ui.properties.extended.ExtendedAttributeProperties;
import org.yawlfoundation.yawl.editor.ui.properties.extended.ExtendedAttributesBeanInfo;
import org.yawlfoundation.yawl.editor.ui.properties.extended.ExtendedAttributesPropertySheet;
import org.yawlfoundation.yawl.editor.ui.properties.extended.UserDefinedAttributesBinder;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryAddAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryGetAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryRemoveAction;
import org.yawlfoundation.yawl.editor.ui.specification.SpecificationUndoManager;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLToolBarButton;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

/**
 * A dialog for editing attributes. Also supports adding and removing user-defined
 * attribute definitions
 *
 * @author Michael Adams
 * @date 21/06/13
 */
public class ExtendedAttributesDialog extends PropertyDialog
        implements ActionListener, ListSelectionListener {

    private JButton btnDel;
    private JButton btnOK;
    private JLabel statusBar;
    private ExtendedAttributesPropertySheet propertySheet;
    private ExtendedAttributesBeanInfo attributesBeanInfo;
    private ExtendedAttributeProperties properties;
    private UserDefinedAttributesBinder udAttributes;

    // the set of attributes for a YDecomposition or YVariable
    private YAttributeMap attributes;


    public ExtendedAttributesDialog(YDecomposition decomposition) {
        super(YAWLEditor.getInstance());
        initialise();
        setUp(decomposition);
        completeInitialisation();
    }

    public ExtendedAttributesDialog(JDialog owner, VariableRow row) {
        super(owner);
        initialise();
        setUp(row);
        completeInitialisation();
    }


    public YAttributeMap getAttributes() { return attributes; }


    /**
     * Loads attribute values from repository
     * @param loaded those attributes loaded from repository
     */
    public void loadAttributes(YAttributeMap loaded) {
        if (loaded == null) return;

        // throw away loaded attributes that don't match those currently on view
        Set<String> filtered = propertySheet.filterForCurrentPropertyNames(loaded.keySet());

        // add the values
        for (String key : filtered) {
            attributes.put(key, loaded.get(key));
        }
        propertySheet.readFromObject(properties);  // load new values to sheet

        // let the user know
        JOptionPane.showMessageDialog(this,
                filtered.size() + " attribute values loaded",
                "Load from Repository",
                JOptionPane.INFORMATION_MESSAGE);
    }


    /**
     * Listens for button clicks to add or remove user-defined attributes, or to
     * close the dialog
     * @param event the button click event
     */
    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("OK")) {
            propertySheet.getTable().commitEditing();        // save any pending edits
            SpecificationUndoManager.getInstance().setDirty(true);
            setVisible(false);
        }
        else if (action.equals("Add")) {
            addUdAttribute();
        }
        else if (action.equals("Del")) {
            removeUdAttribute();
        }
    }


    /**
     * Called from the property sheet's table, and used to enable or disable the
     * remove user-defined attribute button based on the selected attribute in the table
     * @param event the table's selection event
     */
    public void valueChanged(ListSelectionEvent event) {
        btnDel.setEnabled(propertySheet.isUserDefinedAttributeSelected());
    }


    public void setStatus(String status) { statusBar.setText(status); }


    public boolean isUniqueName(String name) {
        return propertySheet.uniquePropertyName(name);
    }


    /********************************************************************************/

    private void initialise() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
    }

    // setup for decomposition attributes
    private void setUp(YDecomposition decomposition) {
        attributes = decomposition.getAttributes();
        udAttributes = new UserDefinedAttributesBinder(propertySheet, decomposition);
        properties = new ExtendedAttributeProperties(propertySheet, udAttributes,
                decomposition);
        bind(properties, udAttributes, null);
        setTitle("Attributes for Decomposition: " + decomposition.getID());
    }


    // setup for variable attributes
    private void setUp(VariableRow row) {
        attributes = row.getAttributes();
        udAttributes = new UserDefinedAttributesBinder(propertySheet, attributes);
        properties = new ExtendedAttributeProperties(propertySheet, udAttributes,
                row);
        bind(properties, udAttributes, row.getDataType());
        setTitle("Attributes for Variable: " + row.getName());
    }


    // binds the properties class with the bean class (augmented with the
    // appropriate user-defined attributes)
    private void bind(ExtendedAttributeProperties properties,
                      UserDefinedAttributesBinder udAttributes, String dataType) {
        attributesBeanInfo = new ExtendedAttributesBeanInfo(udAttributes, dataType);
        new Binder(properties, attributesBeanInfo);
    }


    private void completeInitialisation() {
        propertySheet.getTable().getSelectionModel().addListSelectionListener(this);
        setPreferredSize(new Dimension(400, 350));
        setMinimumSize(new Dimension(250, 300));
        getRootPane().setDefaultButton(btnOK);
        pack();
    }

    protected JPanel getContent() {
        propertySheet = new ExtendedAttributesPropertySheet(this);  // must be created first up
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(5,5,5,5));
        content.add(createToolbar(), BorderLayout.NORTH);
        content.add(createPropertiesPane(), BorderLayout.CENTER);
        content.add(getButtonBar(this), BorderLayout.SOUTH);
        return content;
    }

    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setBorder(null);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.add(createToolBarButton("table_add", "Add", " Add a new attribute "));
        btnDel = createToolBarButton("table_delete", "Del", " Remove an attribute ");
        btnDel.setEnabled(false);  // initially disabled
        toolbar.add(btnDel);

        toolbar.addSeparator();
        toolbar.add(new YAWLToolBarButton(
                new RepositoryAddAction(this, Repo.ExtendedAttributes)));
        toolbar.add(new YAWLToolBarButton(
                new RepositoryGetAction(this, Repo.ExtendedAttributes)));
        toolbar.add(new YAWLToolBarButton(
                new RepositoryRemoveAction(this, Repo.ExtendedAttributes)));

        toolbar.addSeparator();
        statusBar = new JLabel();
        statusBar.setForeground(Color.RED);
        statusBar.setFont(statusBar.getFont().deriveFont(11f));
        toolbar.add(statusBar);
        return toolbar;
    }


    private JButton createToolBarButton(String iconName, String action, String tip) {
        JButton button = new JButton(getMenuIcon(iconName));
        button.setActionCommand(action);
        button.setToolTipText(tip);
        button.setFocusPainted(false);
        button.addActionListener(this);
        return button;
    }


    private JPanel createPropertiesPane() {
        JPanel panel = new JPanel(new BorderLayout());
        propertySheet.setMode(PropertySheet.VIEW_AS_FLAT_LIST);
        panel.add(propertySheet, BorderLayout.CENTER);
        return panel;
    }


    protected JPanel getButtonBar(ActionListener listener) {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));
        btnOK = createButton("OK", listener);
        panel.add(btnOK);
        return panel;
    }


    // adds a new user-defined attribute template (to table and disk)
    private void addUdAttribute() {
        AddUserDefinedAttributeDialog dialog = new AddUserDefinedAttributeDialog(this);
        dialog.setVisible(true);
        if (! dialog.isCancelled()) {
            String name = dialog.getName();
            String type = dialog.getSelectedType();
            if (!StringUtil.isNullOrEmpty(name) && propertySheet.uniquePropertyName(name)) {
                udAttributes.add(name, type);

                // create a property for the sheet
                ExtendedPropertyDescriptor property =
                    attributesBeanInfo.addProperty("UdAttributeValue");
                property.setCategory("Ext. Attributes");
                property.setDisplayName(name);
                property.setPropertyEditorClass(udAttributes.getEditorClass(name));
                property.setPropertyTableRendererClass(udAttributes.getRendererClass(name));

                // update the table
                propertySheet.setProperties(attributesBeanInfo.getPropertyDescriptors());
                propertySheet.readFromObject(properties);
            }
        }
    }


    private void removeUdAttribute() {
        String name = propertySheet.getSelectedPropertyName();
        if (name != null) {

            // will remove only if it is a user-defined attribute,from table & bean
            if (propertySheet.removeProperty(name)) {
                attributesBeanInfo.removeUserDefinedAttribute(udAttributes, name);
            }
            else {
                // not a user-defined attribute, but since the button that fires this
                // method is only enabled for user-defined attributes, this should
                // never be reached
            }
        }
    }

}
