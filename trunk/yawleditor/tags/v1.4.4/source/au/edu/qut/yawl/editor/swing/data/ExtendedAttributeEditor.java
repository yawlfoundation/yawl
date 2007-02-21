package au.edu.qut.yawl.editor.swing.data;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.*;
import java.sql.SQLException;

/**
 * @author Mike Fowler
 *         Created 28-Oct-2005.
 */
public class ExtendedAttributeEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
{
    //cell data
    private ExtendedAttribute hint;

    /**
     * Returns the value contained in the editor.
     *
     * @return the value contained in the editor
     */
    public Object getCellEditorValue()
    {
        return hint;
    }

    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int row,
                                                 int column)
    {
        hint = (ExtendedAttribute) value;
        return hint.getComponent();
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e)
    {
        fireEditingStopped();
    }
}
