package org.yawlfoundation.yawl.editor.swing.data;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * @author mfowler
 *         Date: 07-Sep-2007
 */
public class XQueryButton extends JButton
{
    public static final String ACTION_COMMAND = "EDIT_XQUERY";

    /**
     * Creates a button with text.
     *
     * @param text the text of the button
     */
    public XQueryButton(String text)
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
