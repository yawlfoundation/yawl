/* $Id: MappingKeeper.java,v 1.8 2004/08/15 12:39:30 eric Exp $
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
package proguard.obfuscate;

import proguard.classfile.*;
import proguard.classfile.util.ClassUtil;


/**
 * This MappingKeeper applies the mappings that it receives to its class pool,
 * so these mappings are ensured in a subsequent obfuscation step.
 *
 * @author Eric Lafortune
 */
public class MappingKeeper implements MappingProcessor
{
    private ClassPool classPool;

    // A field acting as a parameter.
    private ClassFile classFile;


    /**
     * Creates a new MappingKeeper.
     * @param classPool the class pool in which class names and class member names
     *                  have to be mapped.
     */
    public MappingKeeper(ClassPool classPool)
    {
        this.classPool = classPool;
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
            // Make sure the mapping name will be kept.
            String newName = ClassUtil.internalClassName(newClassName);

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
                // Make sure the mapping name will be kept.
                MemberInfoObfuscator.setNewMemberName(fieldInfo, newFieldName);
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
                // Make sure the mapping name will be kept.
                MemberInfoObfuscator.setNewMemberName(methodInfo, newMethodName);
            }
        }
    }
}
