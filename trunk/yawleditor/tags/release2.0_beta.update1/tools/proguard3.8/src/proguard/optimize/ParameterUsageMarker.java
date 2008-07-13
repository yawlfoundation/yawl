/* $Id: ParameterUsageMarker.java,v 1.4.2.2 2007/01/18 21:31:53 eric Exp $
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
package proguard.optimize;

import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;

/**
 * This MemberInfoVisitor marks the used method parameters methods in the
 * class hierarchies. The used variables must have been marked first. The
 * 'this' parameters of methods that have a hierarchy are marked too.
 *
 * @see VariableUsageMarker
 * @author Eric Lafortune
 */
public class ParameterUsageMarker
implements   MemberInfoVisitor
{
    // Implementations for MemberInfoVisitor.

    public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo) {}


    public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        // Is this a native method?
        if ((programMethodInfo.getAccessFlags() & ClassConstants.INTERNAL_ACC_NATIVE) != 0)
        {
            // All parameters can be considered as being used.
            long usedParameters = parameterMask(programClassFile, programMethodInfo);

            // Is this a non-static method?
            if ((programMethodInfo.getAccessFlags() & ClassConstants.INTERNAL_ACC_STATIC) == 0)
            {
                // The 'this' parameter can be considered as being used too.
                usedParameters = 1L | (usedParameters << 1);
            }

            // Mark the parameters.
            markUsedVariables(programMethodInfo, usedParameters);
        }

        // Can the method have other implementations?
        if (programClassFile.mayHaveImplementations(programMethodInfo))
        {
            // All implementations must at least keep the 'this' parameter too.
            long usedParameters = 1L;

            // Mark the parameters.
            markUsedVariables(programMethodInfo, usedParameters);
        }
        // Is it an <init> method?
        else if (programMethodInfo.getName(programClassFile).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT))
        {
            // Instance initializers always require the 'this' parameter.
            long usedParameters = 1L;

            // Is there a name clash with some existing <init> method?
            if (programClassFile.findMethod(ClassConstants.INTERNAL_METHOD_NAME_INIT,
                                            ParameterShrinker.shrinkDescriptor(programClassFile,
                                                                               programMethodInfo)) != null)
            {
                // The shrinking is off. Mark all parameters.
                usedParameters |=  parameterMask(programClassFile, programMethodInfo) << 1;
            }

            // Mark the parameters.
            markUsedVariables(programMethodInfo, usedParameters);
        }
    }


    public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo) {}


    public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
    {
        // Can the method have other implementations?
        if (libraryClassFile.mayHaveImplementations(libraryMethodInfo))
        {
            // All implementations must keep all parameters of this method,
            // including the 'this' parameter.
            long usedParameters = 1L |
                                  (parameterMask(libraryClassFile, libraryMethodInfo) << 1);

            // Mark it.
            markUsedVariables(libraryMethodInfo, usedParameters);
        }
    }


    // Small utility methods.

    private void markUsedVariables(MethodInfo methodInfo, long usedParameters)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(methodInfo);
        if (info != null)
        {
            info.setUsedVariables(info.getUsedVariables() | usedParameters);
        }
    }


    public static long getUsedVariables(MethodInfo methodInfo)
    {
        MethodOptimizationInfo info = MethodOptimizationInfo.getMethodOptimizationInfo(methodInfo);
        return info != null ? info.getUsedVariables() : -1L;
    }


    /**
     * Returns the mask of the parameters on the stack, for the given method,
     * not including the 'this' parameter, if any.
     */
    private int parameterMask(ClassFile classFile, MethodInfo methodInfo)
    {
        return (1 << ClassUtil.internalMethodParameterSize(methodInfo.getDescriptor(classFile))) - 1;
    }
}
