/* $Id: NoSideEffectMethodMarker.java,v 1.2 2004/08/15 12:39:30 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
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
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;

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
    // A visitor info flag to indicate that a MethodInfo object doesn't have
    // anyside effects.
    private static final Object NO_SIDE_EFFECTS = new Object();


    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo) {}

    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        markAsNoSideEffects(programMethodInfo);
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}

    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        markAsNoSideEffects(libraryMethodInfo);
    }


    // Small utility methods.

    public static void markAsNoSideEffects(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(NO_SIDE_EFFECTS);
    }


    public static boolean hasNoSideEffects(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter != null &&
               visitorAccepter.getVisitorInfo() == NO_SIDE_EFFECTS;
    }
}
