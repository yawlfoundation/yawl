/* $Id: LinearTiming.java,v 1.7.2.2 2007/01/18 21:31:52 eric Exp $
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

/**
 * This Timing ramps up linearly from 0 to 1 in a given time interval.
 *
 * @author Eric Lafortune
 */
public class LinearTiming implements Timing
{
    private long fromTime;
    private long toTime;


    /**
     * Creates a new LinearTiming.
     * @param fromTime the time at which the timing starts ramping up from 0.
     * @param toTime   the time at which the timing stops ramping up at 1.
     */
    public LinearTiming(long fromTime, long toTime)
    {
        this.fromTime = fromTime;
        this.toTime   = toTime;
    }


    // Implementation for Timing.

    public double getTiming(long time)
    {
        // Compute the clamped linear interpolation.
        return time <= fromTime ? 0.0 :
               time >= toTime   ? 1.0 :
                                  (double)(time - fromTime) / (double)(toTime - fromTime);
    }
}
