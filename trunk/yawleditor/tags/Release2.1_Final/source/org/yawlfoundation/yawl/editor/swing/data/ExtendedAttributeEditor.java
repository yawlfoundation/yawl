package org.yawlfoundation.yawl.editor.swing.data;

import org.yawlfoundation.yawl.editor.swing.AbstractDoneDialog;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Mike Fowler
 *         Created 28-Oct-2005.
 * @author Michael Adams - modified for 2.1
 */
public class ExtendedAttributeEditor extends AbstractCellEditor
        implements TableCellEditor, ActionListener {

    private AbstractDoneDialog _parent;
    private DialogMode _mode;

    //cell data
    private ExtendedAttribute attribute;

    public ExtendedAttributeEditor(AbstractDoneDialog parent, DialogMode mode) {
        _parent = parent;
        _mode = mode;
    }

    /**
     * @return the value contained in the editor
     */
    public Object getCellEditorValue() {
        return attribute;
    }

    
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row,
                                                 int column) {
        attribute = (ExtendedAttribute) value;
        JComponent component = attribute.getComponent();

        if ((component instanceof JTextField) && (attribute.hasExtendedField())) {
            return getExtendedField(component);
        }
        return component;
    }
    

    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getActionCommand().equals("EXPAND")) {
            String type = attribute.getType();
            if (type.equals("text")) {
                showTextDialog();
            }
            else if (type.equals("color")) {
                showColorDialog();
            }
            else if (type.equals("xquery")) {
                showXQueryDialog();
            }
            else if (type.equals("font")) {
                showFontDialog();
            }
        }
        fireEditingStopped();
    }


    public JPanel getExtendedField(Component component) {
        JTextField textField = (JTextField) component;
        JButton btnExpand = new JButton("...");
        btnExpand.setPreferredSize(new Dimension(25, 25));
        btnExpand.addActionListener(this);
        btnExpand.setActionCommand("EXPAND");
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(btnExpand, BorderLayout.EAST);
        panel.add(textField, BorderLayout.CENTER);
        return panel;
    }


    private void showTextDialog() {
        TextAreaDialog dialog = new TextAreaDialog(_parent, attribute.getName(),
                attribute.getValue());
        String text = dialog.showDialog();
        if (text != null) {
            attribute.setValue(text);
        }
    }


    private void showColorDialog() {
        Color currentColor = attribute.hexToColour(attribute.getValue());
        Color newColour = JColorChooser.showDialog(_parent, "Colour Picker", currentColor);
        if (newColour != null) {
            attribute.setValue(attribute.colourToHex(newColour));
        }
    }


    private void showXQueryDialog() {
        XQueryUpdateDialog xqDialog = new XQueryUpdateDialog(_parent, _mode);
        xqDialog.setExtendedAttribute(attribute);
        String text = xqDialog.showDialog();
        if (text != null) {
            attribute.setValue(text);
        }
    }

    
    private void showFontDialog() {
        FontDialog dialog = new FontDialog(_parent, attribute.getComponent().getFont());
        Font font = dialog.showDialog();
        if (font != null) {
            attribute.setValue(font.getName());
            ExtendedAttributeGroup group = attribute.getGroup();
            if (group != null) {
                for (ExtendedAttribute attribute : group) {
                    if (attribute.getName().endsWith("size")) {
                        attribute.setValue(String.valueOf(font.getSize()));                        
                    }
                    else if (attribute.getName().endsWith("style")) {
                        ((JComboBox) attribute.getComponent())
                                .setSelectedIndex(font.getStyle());
                    }
                }
            }
        }
    }
}


