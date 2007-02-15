/* $Id: TextSprite.java,v 1.7.2.1 2006/01/16 22:57:55 eric Exp $
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
package proguard.gui.splash;

import java.awt.*;

/**
 * This Sprite represents a text.
 *
 * @author Eric Lafortune
 */
public class TextSprite implements Sprite
{
    private VariableString[] text;
    private VariableInt      spacing;
    private VariableFont     font;
    private VariableColor    color;
    private VariableInt      x;
    private VariableInt      y;


    /**
     * Creates a new TextSprite containing a single line of text.
     * @param text  the variable text string.
     * @param font  the variable text font.
     * @param color the variable color.
     * @param x     the variable x-coordinate of the lower-left corner of the text.
     * @param y     the variable y-coordinate of the lower-left corner of the text.
     */
    public TextSprite(VariableString text,
                      VariableFont   font,
                      VariableColor  color,
                      VariableInt    x,
                      VariableInt    y)
    {
        this(new VariableString[] { text }, new ConstantInt(0), font, color, x, y);
    }


    /**
     * Creates a new TextSprite containing a multiple lines of text.
     * @param text    the variable text strings.
     * @param spacing the variable spacing between the lines of text.
     * @param font    the variable text font.
     * @param color   the variable color.
     * @param x       the variable x-coordinate of the lower-left corner of the
     *                first line of text.
     * @param y       the variable y-coordinate of the lower-left corner of the
     *                first line of text.
     */
    public TextSprite(VariableString[] text,
                      VariableInt      spacing,
                      VariableFont     font,
                      VariableColor    color,
                      VariableInt      x,
                      VariableInt      y)
    {

        this.text    = text;
        this.spacing = spacing;
        this.font    = font;
        this.color   = color;
        this.x       = x;
        this.y       = y;
    }


    // Implementation for Sprite.

    public void paint(Graphics graphics, long time)
    {

        int xt = x.getInt(time);
        int yt = y.getInt(time);

        graphics.setFont(font.getFont(time));
        graphics.setColor(color.getColor(time));

        for (int index = 0; index < text.length; index++)
        {
            graphics.drawString(text[index].getString(time), xt, yt + index * spacing.getInt(time));
        }
    }
}
