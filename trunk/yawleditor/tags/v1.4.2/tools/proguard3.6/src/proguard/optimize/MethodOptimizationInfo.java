/* $Id: MethodOptimizationInfo.java,v 1.4.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.optimize;

import proguard.classfile.*;
import proguard.classfile.util.MethodInfoLinker;

/**
 * This class stores some optimization information that can be attached to
 * a method.
 *
 * @author Eric Lafortune
 */
public class MethodOptimizationInfo
{
    private boolean hasNoSideEffects = false;
    private boolean hasSideEffects   = false;
    private boolean canBeMadePrivate = true;
    private long    usedVariables    = 0;


    public void setNoSideEffects()
    {
        hasNoSideEffects = true;
    }


    public boolean hasNoSideEffects()
    {
        return hasNoSideEffects;
    }


    public void setSideEffects()
    {
        hasSideEffects = true;
    }


    public boolean hasSideEffects()
    {
        return hasSideEffects;
    }


    public void setCanNotBeMadePrivate()
    {
        canBeMadePrivate = false;
    }


    public boolean canBeMadePrivate()
    {
        return canBeMadePrivate;
    }


    public void setUsedVariables(long usedVariables)
    {
        this.usedVariables = usedVariables;
    }


    public long getUsedVariables()
    {
        return usedVariables;
    }


    public void setVariableUsed(int variableIndex)
    {
        usedVariables |= 1 << variableIndex;
    }


    public boolean isVariableUsed(int variableIndex)
    {
        return variableIndex >= 64 || (usedVariables & (1 << variableIndex)) != 0;
    }


    public static void setMethodOptimizationInfo(MethodInfo methodInfo)
    {
        MethodInfoLinker.lastMethodInfo(methodInfo).setVisitorInfo(new MethodOptimizationInfo());
    }


    public static MethodOptimizationInfo getMethodOptimizationInfo(MethodInfo methodInfo)
    {
        Object visitorInfo = MethodInfoLinker.lastMethodInfo(methodInfo).getVisitorInfo();

        return visitorInfo instanceof MethodOptimizationInfo ?
            (MethodOptimizationInfo)visitorInfo :
            null;
    }
}
