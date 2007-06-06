/* $Id: ProGuardRunnable.java,v 1.7.2.2 2007/01/18 21:31:52 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
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
import java.io.*;

import javax.swing.*;

import proguard.*;


/**
 * This <code>Runnable</code> runs ProGuard, sending console output to a text
 * area and any exceptions to message dialogs.
 *
 * @see ProGuard
 * @author Eric Lafortune
 */
class ProGuardRunnable implements Runnable
{
    private JTextArea     consoleTextArea;
    private Configuration configuration;
    private String        configurationFileName;


    /**
     * Creates a new ProGuardRunnable object.
     * @param consoleTextArea       the text area to send the console output to.
     * @param configuration         the ProGuard configuration.
     * @param configurationFileName the optional file name of the configuration,
     *                              for informational purposes.
     */
    public ProGuardRunnable(JTextArea     consoleTextArea,
                            Configuration configuration,
                            String        configurationFileName)
    {
        this.consoleTextArea       = consoleTextArea;
        this.configuration         = configuration;
        this.configurationFileName = configurationFileName;
    }


    // Implementation for Runnable.

    public void run()
    {
        consoleTextArea.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        consoleTextArea.setText("");

        // Redirect the System's out and err streams to the console text area.
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;

        PrintStream printStream =
            new PrintStream(new TextAreaOutputStream(consoleTextArea), true);

        System.setOut(printStream);
        System.setErr(printStream);

        try
        {
            // Create a new ProGuard object with the GUI's configuration.
            ProGuard proGuard = new ProGuard(configuration);

            // Run it.
            proGuard.execute();

            // Print out the completion message.
            System.out.println("Processing completed successfully");
        }
        catch (Exception ex)
        {
            //ex.printStackTrace();

            // Print out the exception message.
            System.out.println(ex.getMessage());

            // Show a dialog as well.
            MessageDialogRunnable.showMessageDialog(consoleTextArea,
                                                    ex.getMessage(),
                                                    msg("errorProcessing"),
                                                    JOptionPane.ERROR_MESSAGE);
        }
        catch (OutOfMemoryError er)
        {
            // Forget about the ProGuard object as quickly as possible.
            System.gc();

            // Print out a message suggesting what to do next.
            System.out.println(msg("outOfMemoryInfo", configurationFileName));

            // Show a dialog as well.
            MessageDialogRunnable.showMessageDialog(consoleTextArea,
                                                    msg("outOfMemory"),
                                                    msg("errorProcessing"),
                                                    JOptionPane.ERROR_MESSAGE);
        }

        // Make sure all output has been sent to the console text area.
        printStream.flush();

        // Restore the old System's out and err streams.
        System.setOut(oldOut);
        System.setErr(oldErr);

        consoleTextArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        // Reset the global static redirection lock.
        ProGuardGUI.systemOutRedirected = false;
    }


    // Small utility methods.

    /**
     * Returns the message from the GUI resources that corresponds to the given
     * key.
     */
    private String msg(String messageKey)
    {
         return GUIResources.getMessage(messageKey);
    }


    /**
     * Returns the message from the GUI resources that corresponds to the given
     * key and argument.
     */
    private String msg(String messageKey,
                       Object messageArgument)
    {
         return GUIResources.getMessage(messageKey, new Object[] {messageArgument});
    }
}
