/* $Id: TimeSwitchSprite.java,v 1.7.2.2 2007/01/18 21:31:52 eric Exp $
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

import java.awt.Graphics;

/**
 * This Sprite displays another Sprite in a given time interval.
 * The time of the encapsulated Sprite is shifted by the start time.
 *
 * @author Eric Lafortune
 */
public class TimeSwitchSprite implements Sprite
{
    private long   onTime;
    private long   offtime;
    private Sprite sprite;


    /**
     * Creates a new TimeSwitchSprite for displaying a given Sprite starting at a
     * given time.
     * @param onTime the start time.
     * @param sprite the toggled Sprite.
     */
    public TimeSwitchSprite(long onTime, Sprite sprite)
    {
        this(onTime, 0L, sprite);
    }


    /**
     * Creates a new TimeSwitchSprite for displaying a given Sprite  in a given
     * time interval.
     * @param onTime the start time.
     * @param offTime the stop time.
     * @param sprite the toggled Sprite.
     */
    public TimeSwitchSprite(long onTime, long offtime, Sprite sprite)
    {
        this.onTime  = onTime;
        this.offtime = offtime;
        this.sprite  = sprite;
    }


    // Implementation for Sprite.

    public void paint(Graphics graphics, long time)
    {
        if (time >= onTime && (offtime <= 0 || time <= offtime))
        {
            sprite.paint(graphics, time - onTime);
        }

    }
}
