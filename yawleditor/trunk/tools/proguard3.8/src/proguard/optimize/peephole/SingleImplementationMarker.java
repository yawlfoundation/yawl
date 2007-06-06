/* $Id: SingleImplementationMarker.java,v 1.8.2.3 2007/01/18 21:31:53 eric Exp $
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
    private static final boolean DEBUG = false;


    private boolean          allowAccessModification;
    private ClassFileVisitor extraClassFileVisitor;


    /**
     * Creates a new SingleImplementationMarker.
     * @param allowAccessModification indicates whether the access modifiers of
     *                                a class can be changed in order to inline
     *                                it.
     */
    public SingleImplementationMarker(boolean allowAccessModification)
    {
        this(allowAccessModification, null);
    }


    /**
     * Creates a new SingleImplementationMarker.
     * @param allowAccessModification indicates whether the access modifiers of
     *                                a class can be changed in order to inline
     *                                it.
     * @param extraClassFileVisitor   an optional extra visitor for all inlinable
     *                                interfaces.
     */
    public SingleImplementationMarker(boolean          allowAccessModification,
                                      ClassFileVisitor extraClassFileVisitor)
    {
        this.allowAccessModification = allowAccessModification;
        this.extraClassFileVisitor   = extraClassFileVisitor;
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

        // The single implementation must contain all non-static methods of this
        // interface, so invocations can easily be diverted.
        for (int index = 0; index < programClassFile.u2methodsCount; index++)
        {
            MethodInfo method = programClassFile.methods[index];
            if ((method.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) == 0 &&
                singleImplementationClassFile.findMethod(method.getName(programClassFile),
                                                         method.getDescriptor(programClassFile)) == null)
            {
                return;
            }
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

        if (DEBUG)
        {
            System.out.println("Single implementation of ["+programClassFile.getName()+"]: ["+singleImplementationClassFile.getName()+"]");
        }

        // Mark the interface and its single implementation.
        markSingleImplementation(programClassFile, singleImplementationClassFile);

        // Visit the interface, if required.
        if (extraClassFileVisitor != null)
        {
            singleImplementationClassFile.accept(extraClassFileVisitor);
        }
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile) {}


    // Small utility methods.

    public static void markSingleImplementation(VisitorAccepter visitorAccepter,
                                                ClassFile       singleImplementation)
    {
        // The interface has a single implementation.
        visitorAccepter.setVisitorInfo(singleImplementation);
    }


    public static ClassFile singleImplementation(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter != null &&
               visitorAccepter.getVisitorInfo() instanceof ClassFile ?
                   (ClassFile)visitorAccepter.getVisitorInfo() :
                   null;
    }
}
