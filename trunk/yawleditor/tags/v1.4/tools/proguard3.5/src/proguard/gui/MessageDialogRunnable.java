/* $Id: MessageDialogRunnable.java,v 1.6.2.1 2006/01/16 22:57:55 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2006 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.gui;

import java.awt.*;

import javax.swing.*;


/**
 * This <code>Runnable</code> can show a message dialog.
 *
 * @author Eric Lafortune
 */
class MessageDialogRunnable implements Runnable
{
    private Component parentComponent;
    private Object    message;
    private String    title;
    private int       messageType;


    /**
     * Creates a new MessageDialogRunnable object.
     * @see JOptionPane.showMessageDialog
     */
    public static void showMessageDialog(Component parentComponent,
                                         Object    message,
                                         String    title,
                                         int       messageType)
    {
        SwingUtil.invokeAndWait(new MessageDialogRunnable(parentComponent,
                                                          message,
                                                          title,
                                                          messageType));
    }


    /**
     * Creates a new MessageDialogRunnable object.
     * @see JOptionPane.showMessageDialog
     */
    public MessageDialogRunnable(Component parentComponent,
                                 Object    message,
                                 String    title,
                                 int       messageType)
    {
        this.parentComponent = parentComponent;
        this.message         = message;
        this.title           = title;
        this.messageType     = messageType;
    }



    // Implementation for Runnable.

    public void run()
    {
        JOptionPane.showMessageDialog(parentComponent,
                                      message,
                                      title,
                                      messageType);
    }
}
