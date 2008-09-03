/* $Id: VariableSizeFont.java,v 1.6.2.2 2007/01/18 21:31:52 eric Exp $
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
package proguard.gui.splash;

import java.awt.Font;

/**
 * This VariableFont varies in size with respect to its Timing.
 *
 * @author Eric Lafortune
 */
public class VariableSizeFont implements VariableFont
{
    private Font           font;
    private VariableDouble size;

    private float cachedSize = -1.0f;
    private Font  cachedFont;


    /**
     * Creates a new VariableSizeFont
     * @param font the base font.
     * @param size the variable size of the font.
     */
    public VariableSizeFont(Font font, VariableDouble size)
    {
        this.font = font;
        this.size = size;
    }


    // Implementation for VariableFont.

    public Font getFont(long time)
    {
        float s = (float)size.getDouble(time);

        if (s != cachedSize)
        {
            cachedSize = s;
            cachedFont = font.deriveFont((float)s);
        }

        return cachedFont;
    }
}
