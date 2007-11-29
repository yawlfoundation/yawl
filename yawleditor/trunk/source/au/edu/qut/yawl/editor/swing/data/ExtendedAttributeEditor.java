package au.edu.qut.yawl.editor.swing.data;

import au.edu.qut.yawl.editor.swing.AbstractDoneDialog;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Mike Fowler
 *         Created 28-Oct-2005.
 */
public class ExtendedAttributeEditor extends AbstractCellEditor implements TableCellEditor, ActionListener
{
    private XQueryUpdateDialog dialog;

    //cell data
    private ExtendedAttribute hint;

    public ExtendedAttributeEditor(AbstractDoneDialog parent, DialogMode mode)
    {
        dialog = new XQueryUpdateDialog(parent, mode);
    }

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
        JComponent component = hint.getComponent();

        if(component instanceof XQuery) ((XQuery) component).appendActionListener(this);
        return component;
    }

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equals(XQuery.ACTION_COMMAND) && hint.getComponent() instanceof XQuery)
        {
            //Don't put on a separate thread - the update event fires before the data is acutally
            //captured meaning the table model and the display are out of sync!! (4 hours of debug later...)
//            EventQueue.invokeLater(new Runnable(){
//                    public void run()
//                    {
                        showXQueryDialog(hint);
//                    }
//                });
        }
        fireEditingStopped();
    }

    private void showXQueryDialog(ExtendedAttribute attribute)
    {
        dialog.setExtendedAttribute(attribute);
        dialog.setVisible(true);
    }
}
