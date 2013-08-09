package org.yawlfoundation.yawl.editor.ui.properties.dialog;

import com.l2fprod.common.propertysheet.PropertySheet;
import org.yawlfoundation.yawl.editor.core.repository.Repo;
import org.yawlfoundation.yawl.editor.ui.YAWLEditor;
import org.yawlfoundation.yawl.editor.ui.properties.Binder;
import org.yawlfoundation.yawl.editor.ui.properties.ExtendedAttributeProperties;
import org.yawlfoundation.yawl.editor.ui.properties.ExtendedAttributesBeanInfo;
import org.yawlfoundation.yawl.editor.ui.properties.YPropertySheet;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryAddAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryGetAction;
import org.yawlfoundation.yawl.editor.ui.repository.action.RepositoryRemoveAction;
import org.yawlfoundation.yawl.editor.ui.swing.menu.YAWLToolBarButton;
import org.yawlfoundation.yawl.editor.ui.util.ResourceLoader;
import org.yawlfoundation.yawl.elements.YAttributeMap;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.data.YParameter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Michael Adams
 * @date 21/06/13
 */
public class ExtendedAttributesDialog extends JDialog implements ActionListener {

    private JButton btnDel;
    private YPropertySheet propertySheet;
    private YAttributeMap attributes;

    private static final String iconPath = "/org/yawlfoundation/yawl/editor/ui/resources/menuicons/";


    public ExtendedAttributesDialog(YDecomposition decomposition) {
        super(YAWLEditor.getInstance());
        initialise();
        setUp(decomposition);
        completeInitialisation();
    }

    public ExtendedAttributesDialog(JDialog owner, YParameter parameter) {
        super(owner);
        initialise();
        setUp(parameter);
        completeInitialisation();
    }


    public YAttributeMap getAttributes() { return attributes; }

    public void loadAttributes(YAttributeMap loaded) {
        for (String key : loaded.keySet()) {
            if (attributes.containsKey(key)) {
                attributes.put(key, loaded.get(key));
            }
        }
        propertySheet.repaint();
    }


    public void actionPerformed(ActionEvent event) {
        String action = event.getActionCommand();
        if (action.equals("OK")) {
            setVisible(false);
        }
        else if (action.equals("Add")) {
            // add
        }
        else if (action.equals("Del")) {
            // del
        }
    }



    private void initialise() {
        propertySheet = new YPropertySheet();      // must be created first up
        setModal(true);
        setResizable(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationByPlatform(true);
    }

    private void setUp(YDecomposition decomposition) {
        attributes = decomposition.getAttributes();
        ExtendedAttributeProperties properties =
                new ExtendedAttributeProperties(propertySheet, decomposition);
        new Binder(properties, new ExtendedAttributesBeanInfo(decomposition));
        setTitle("Attributes for Decomposition: " + decomposition.getID());
    }

    private void setUp(YParameter parameter) {
        attributes = parameter.getAttributes();
        ExtendedAttributeProperties properties =
                new ExtendedAttributeProperties(propertySheet, parameter);
        new Binder(properties, new ExtendedAttributesBeanInfo(parameter));
        setTitle("Attributes for Variable: " + parameter.getPreferredName());
    }



    private void completeInitialisation() {
        add(getContent());
        setPreferredSize(new Dimension(400, 350));
        setMinimumSize(new Dimension(250, 300));
        pack();
    }

    private JPanel getContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(new EmptyBorder(5,5,5,5));
        content.add(createToolbar(), BorderLayout.NORTH);
        content.add(createPropertiesPane(), BorderLayout.CENTER);
        content.add(createButtonBar(), BorderLayout.SOUTH);
        return content;
    }

    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setBorder(null);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.add(createToolBarButton("table_add", "Add", " Add a new attribute "));
        btnDel = createToolBarButton("table_delete", "Del", " Remove an attribute ");
        toolbar.add(btnDel);

        toolbar.addSeparator();
        toolbar.add(new YAWLToolBarButton(
                new RepositoryAddAction(this, Repo.ExtendedAttributes)));
        toolbar.add(new YAWLToolBarButton(
                new RepositoryGetAction(this, Repo.ExtendedAttributes)));
        toolbar.add(new YAWLToolBarButton(
                new RepositoryRemoveAction(this, Repo.ExtendedAttributes)));
        return toolbar;
    }



    private JButton createToolBarButton(String iconName, String action, String tip) {
        JButton button = new JButton(getIcon(iconName));
        button.setActionCommand(action);
        button.setToolTipText(tip);
        button.setFocusPainted(false);
        button.addActionListener(this);
        return button;
    }

    private ImageIcon getIcon(String iconName) {
        return ResourceLoader.getImageAsIcon(iconPath + iconName + ".png");
    }


    private JPanel createPropertiesPane() {
        JPanel panel = new JPanel(new BorderLayout());
        propertySheet.setMode(PropertySheet.VIEW_AS_FLAT_LIST);
        panel.add(propertySheet, BorderLayout.CENTER);
        return panel;
    }


    private JPanel createButtonBar() {
        JPanel panel = new JPanel();
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));
        panel.add(createButton("OK"));
        return panel;
    }


    private JButton createButton(String label) {
        JButton button = new JButton(label);
        button.setActionCommand(label);
        button.setMnemonic(label.charAt(0));
        button.setPreferredSize(new Dimension(70,25));
        button.addActionListener(this);
        return button;
    }


}
