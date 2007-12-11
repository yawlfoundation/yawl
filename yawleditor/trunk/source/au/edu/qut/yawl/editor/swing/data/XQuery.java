package au.edu.qut.yawl.editor.swing.data;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * @author mfowler
 *         Date: 07-Sep-2007
 */
public class XQuery extends JButton
{
    public static final String ACTION_COMMAND = "EDIT_XQUERY";

    /**
     * Creates a button with text.
     *
     * @param text the text of the button
     */
    public XQuery(String text)
    {
        super(text);
        setActionCommand(ACTION_COMMAND);
    }

    public void appendActionListener(ActionListener listener)
    {
        ActionListener[] listeners = getActionListeners();
        for(ActionListener l : listeners)
        {
            if(l.equals(listener)) return;
        }
        addActionListener(listener);
    }
}
