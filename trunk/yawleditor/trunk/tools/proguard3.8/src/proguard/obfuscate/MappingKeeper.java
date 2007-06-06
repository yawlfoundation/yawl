/* $Id: MappingKeeper.java,v 1.10.2.3 2007/01/18 21:31:52 eric Exp $
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
package proguard.obfuscate;

import proguard.classfile.*;
import proguard.classfile.util.*;


/**
 * This MappingKeeper applies the mappings that it receives to its class pool,
 * so these mappings are ensured in a subsequent obfuscation step.
 *
 * @author Eric Lafortune
 */
public class MappingKeeper implements MappingProcessor
{
    private ClassPool      classPool;
    private WarningPrinter warningPrinter;

    // A field acting as a parameter.
    private ClassFile classFile;


    /**
     * Creates a new MappingKeeper.
     * @param classPool      the class pool in which class names and class
     *                       member names have to be mapped.
     * @param warningPrinter the optional warning printer to which warnings
     *                       can be printed.
     */
    public MappingKeeper(ClassPool      classPool,
                         WarningPrinter warningPrinter)
    {
        this.classPool      = classPool;
        this.warningPrinter = warningPrinter;
    }


    // Implementations for MappingProcessor.

    public boolean processClassFileMapping(String className,
                                           String newClassName)
    {
        // Find the class.
        String name = ClassUtil.internalClassName(className);

        classFile = classPool.getClass(name);
        if (classFile != null)
        {
            String newName = ClassUtil.internalClassName(newClassName);

            // Print out a warning if the mapping conflicts with a name that
            // was set before.
            if (warningPrinter != null)
            {
                String currentNewName = ClassFileObfuscator.newClassName(classFile);
                if (currentNewName != null &&
                    !currentNewName.equals(newName))
                {
                    warningPrinter.print("Warning: " +
                                         className +
                                         " is not being kept as '" +
                                         ClassUtil.externalClassName(currentNewName) +
                                         "', but remapped to '" +
                                         newClassName + "'");
                }
            }

            // Make sure the mapping name will be kept.
            ClassFileObfuscator.setNewClassName(classFile, newName);

            // The class members have to be kept as well.
            return true;
        }

        return false;
    }


    public void processFieldMapping(String className,
                                    String fieldType,
                                    String fieldName,
                                    String newFieldName)
    {
        if (classFile != null)
        {
            // Find the field.
            String name       = fieldName;
            String descriptor = ClassUtil.internalType(fieldType);

            FieldInfo fieldInfo = classFile.findField(name, descriptor);
            if (fieldInfo != null)
            {
                // Print out a warning if the mapping conflicts with a name that
                // was set before.
                if (warningPrinter != null)
                {
                    String currentNewName = MemberInfoObfuscator.newMemberName(fieldInfo);
                    if (currentNewName != null &&
                        !currentNewName.equals(newFieldName))
                    {
                        warningPrinter.print("Warning: " +
                                             className +
                                             ": field '" + fieldType + " " + fieldName +
                                             "' is not being kept as '" + currentNewName +
                                             "', but remapped to '" + newFieldName + "'");
                    }
                }

                // Make sure the mapping name will be kept.
                MemberInfoObfuscator.setFixedNewMemberName(fieldInfo, newFieldName);
            }
        }
    }


    public void processMethodMapping(String className,
                                     int    firstLineNumber,
                                     int    lastLineNumber,
                                     String methodReturnType,
                                     String methodNameAndArguments,
                                     String newMethodName)
    {
        if (classFile != null)
        {
            // Find the method.
            String name       = ClassUtil.externalMethodName(methodNameAndArguments);
            String descriptor = ClassUtil.internalMethodDescriptor(methodReturnType,
                                                                   methodNameAndArguments);

            MethodInfo methodInfo = classFile.findMethod(name, descriptor);
            if (methodInfo != null)
            {
                // Print out a warning if the mapping conflicts with a name that
                // was set before.
                if (warningPrinter != null)
                {
                    String currentNewName = MemberInfoObfuscator.newMemberName(methodInfo);
                    if (currentNewName != null &&
                        !currentNewName.equals(newMethodName))
                    {
                        warningPrinter.print("Warning: " +
                                             className +
                                             ": method '" + methodReturnType + " " + methodNameAndArguments +
                                             "' is not being kept as '" + currentNewName +
                                             "', but remapped to '" + newMethodName + "'");
                    }
                }

                // Make sure the mapping name will be kept.
                MemberInfoObfuscator.setFixedNewMemberName(methodInfo, newMethodName);
            }
        }
    }
}
