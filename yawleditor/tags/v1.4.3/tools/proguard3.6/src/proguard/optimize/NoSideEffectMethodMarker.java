/* $Id: NoSideEffectMethodMarker.java,v 1.5.2.2 2006/04/17 02:18:30 eric Exp $
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
import proguard.classfile.visitor.MemberInfoVisitor;

/**
 * This MemberInfoVisitor marks all methods that it visits as not having any side
 * effects. It will make the SideEffectMethodMarker consider them as such
 * without further analysis.
 *
 * @see SideEffectMethodMarker
 * @author Eric Lafortune
 */
public class NoSideEffectMethodMarker
  implements MemberInfoVisitor
{
    // A visitor info flag to indicate the visitor accepter is being kept,
    // but that it doesn't have any side effects.
    private static final Object KEPT_BUT_NO_SIDE_EFFECTS = new Object();


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo) {}

    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        markNoSideEffects(programMethodInfo);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}

    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        markNoSideEffects(libraryMethodInfo);
    }


    // Small utility methods.

    public static void markNoSideEffects(MethodInfo methodInfo)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(methodInfo);
        if (info != null)
        {
            info.setNoSideEffects();
        }
        else
        {
            MethodInfoLinker.lastMethodInfo(methodInfo).setVisitorInfo(KEPT_BUT_NO_SIDE_EFFECTS);
        }
    }


    public static boolean hasNoSideEffects(MethodInfo methodInfo)
    {
        if (MethodInfoLinker.lastVisitorAccepter(methodInfo).getVisitorInfo() == KEPT_BUT_NO_SIDE_EFFECTS)
        {
            return true;
        }

        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(methodInfo);
        return info != null &&
               info.hasNoSideEffects();
    }
}
