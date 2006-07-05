/* $Id: SwingUtil.java,v 1.5.2.1 2006/01/16 22:57:55 eric Exp $
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

import javax.swing.*;


/**
 * This utility class provides variants of the invocation method from the
 * <code>SwingUtilities</code> class.
 *
 * @see SwingUtilities
 * @author Eric Lafortune
 */
class SwingUtil
{
    /**
     * Invokes the given Runnable in the AWT event dispatching thread,
     * and waits for it to finish. This method may be called from any thread,
     * including the event dispatching thread itself.
     * @see SwingUtilities#invokeAndWait(java.lang.Runnable)
     * @param runnable the Runnable to be executed.
     */
    public static void invokeAndWait(Runnable runnable)
    {
        try
        {
            if (SwingUtilities.isEventDispatchThread())
            {
                runnable.run();
            }
            else
            {
                SwingUtilities.invokeAndWait(runnable);
            }
        }
        catch (Exception ex)
        {
            // Ignore any exceptions.
        }
    }


    /**
     * Invokes the given Runnable in the AWT event dispatching thread, not
     * necessarily right away. This method may be called from any thread,
     * including the event dispatching thread itself.
     * @see SwingUtilities#invokeLater(java.lang.Runnable)
     * @param runnable the Runnable to be executed.
     */
    public static void invokeLater(Runnable runnable)
    {
        if (SwingUtilities.isEventDispatchThread())
        {
            runnable.run();
        }
        else
        {
            SwingUtilities.invokeLater(runnable);
        }
    }
}
