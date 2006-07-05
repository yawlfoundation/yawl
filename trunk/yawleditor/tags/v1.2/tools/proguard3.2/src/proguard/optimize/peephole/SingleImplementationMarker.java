/* $Id: SingleImplementationMarker.java,v 1.3 2004/11/20 15:41:24 eric Exp $
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
package proguard.optimize.peephole;

import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.optimize.*;

/**
 * This ClassFileVisitor investigates all class files that it visits to see
 * whether they have/are the sole (non-abstract) implementation of an interface.
 * It may already modify the access of the single implementing class to match
 * the access of the interface.
 *
 * @author Eric Lafortune
 */
public class SingleImplementationMarker
implements   ClassFileVisitor
{
    // A visitor info flag to indicate that a ClassFile object is the single
    // implementation of an interface.
    private static final Object SINGLE_IMPLEMENTATION = new Object();


    private boolean allowAccessModification;


    /**
     * Creates a new SingleImplementationMarker.
     * @param allowAccessModification indicates whether the access modifiers of
     *                                a class can be changed in order to inline
     *                                it.
     */
    public SingleImplementationMarker(boolean allowAccessModification)
    {
        this.allowAccessModification = allowAccessModification;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // The program class must be an interface class that cannot be
        // implemented again.
        if ((programClassFile.getAccessFlags() & ClassConstants.INTERNAL_ACC_INTERFACE) == 0 ||
            KeepMarker.isKept(programClassFile))
        {
            return;
        }

        // The interface class must have a single implementation.
        ClassFile[] subClasses = programClassFile.subClasses;
        if (subClasses == null ||
            subClasses.length != 1)
        {
            return;
        }

        // If the single implementation is an interface, check it recursively.
        ClassFile singleImplementationClassFile = subClasses[0];
        int singleImplementationAccessFlags = singleImplementationClassFile.getAccessFlags();
        if ((singleImplementationAccessFlags & ClassConstants.INTERNAL_ACC_INTERFACE) != 0)
        {
            singleImplementationClassFile.accept(this);

            // See if the subinterface has a single implementation.
            singleImplementationClassFile = singleImplementation(singleImplementationClassFile);
            if (singleImplementationClassFile == null)
            {
                return;
            }

            singleImplementationAccessFlags = singleImplementationClassFile.getAccessFlags();
        }

        // The single implementation must not be abstract.
        else if ((singleImplementationAccessFlags & ClassConstants.INTERNAL_ACC_ABSTRACT) != 0)
        {
            return;
        }

        // Doesn't the implementation have at least the same access as the
        // interface?
        if (AccessUtil.accessLevel(singleImplementationAccessFlags) <
            AccessUtil.accessLevel(programClassFile.getAccessFlags()))
        {
            // Are we allowed to fix the access?
            if (allowAccessModification)
            {
                // Fix the access.
                ((ProgramClassFile)singleImplementationClassFile).u2accessFlags =
                    AccessUtil.replaceAccessFlags(singleImplementationAccessFlags,
                                                  programClassFile.getAccessFlags());
            }
            else
            {
                // We can't give the implementation the access of the interface.
                // Forget about inlining it after all.
                return;
            }
        }

        // Mark the interface and its single implementation.
        markSingleImplementation(programClassFile, singleImplementationClassFile);
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile) {}


    // Small utility methods.

    public static void markSingleImplementation(VisitorAccepter visitorAccepter,
                                                ClassFile       singleImplementation)
    {
        //System.out.println("Marking single implementation ["+((ClassFile)visitorAccepter).getName()+"] -> ["+singleImplementation.getName()+"]");

        // The interface has a single implementation.
        visitorAccepter.setVisitorInfo(singleImplementation);

        // The class is a single implementation.
        singleImplementation.setVisitorInfo(SINGLE_IMPLEMENTATION);
    }


    public static ClassFile singleImplementation(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter != null &&
               visitorAccepter.getVisitorInfo() instanceof ClassFile ?
                   (ClassFile)visitorAccepter.getVisitorInfo() :
                   null;
    }


    public static boolean isSingleImplementation(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter != null &&
               visitorAccepter.getVisitorInfo() == SINGLE_IMPLEMENTATION;
    }
}
