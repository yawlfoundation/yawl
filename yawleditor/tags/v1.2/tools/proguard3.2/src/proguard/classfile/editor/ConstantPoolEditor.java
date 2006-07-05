/* $Id: ConstantPoolEditor.java,v 1.7 2004/11/20 15:41:24 eric Exp $
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
package proguard.classfile.editor;

import proguard.classfile.*;

/**
 * This class can add constant pool entries to given class files.
 *
 * @author Eric Lafortune
 */
public class ConstantPoolEditor
{
    /**
     * Finds or creates a StringCpInfo constant pool entry with the given value,
     * in the given class file.
     * @return the constant pool index of the ClassCpInfo.
     */
    public int addStringCpInfo(ProgramClassFile programClassFile,
                              String            string,
                              ClassFile         referencedClassFile)
    {
        CpInfo[] constantPool      = programClassFile.constantPool;
        int      constantPoolCount = programClassFile.u2constantPoolCount;

        // Check if the entry already exists.
        for (int index = 1; index < constantPoolCount; index++)
        {
            CpInfo cpInfo = constantPool[index];

            if (cpInfo != null &&
                cpInfo.getTag() == ClassConstants.CONSTANT_String)
            {
                StringCpInfo classCpInfo = (StringCpInfo)cpInfo;
                if (classCpInfo.getString(programClassFile).equals(string))
                {
                    return index;
                }
            }
        }

        int nameIndex = addUtf8CpInfo(programClassFile, string);

        return addCpInfo(programClassFile,
                         new StringCpInfo(nameIndex,
                                          referencedClassFile));
    }


    /**
     * Finds or creates a FieldrefCpInfo constant pool entry with the given
     * class name, field name, and descriptor, in the given class file.
     * @return the constant pool index of the FieldrefCpInfo.
     */
    public int addFieldrefCpInfo(ProgramClassFile programClassFile,
                                 String           className,
                                 String           name,
                                 String           descriptor,
                                 ClassFile        referencedClassFile,
                                 MemberInfo       referencedMemberInfo,
                                 ClassFile[]      referencedClassFiles)
    {
        return addFieldrefCpInfo(programClassFile,
                                 className,
                                 addNameAndTypeCpInfo(programClassFile,
                                                      name,
                                                      descriptor,
                                                      referencedClassFiles),
                                 referencedClassFile,
                                 referencedMemberInfo);
    }


    /**
     * Finds or creates a FieldrefCpInfo constant pool entry with the given
     * class name, field name, and descriptor, in the given class file.
     * @return the constant pool index of the FieldrefCpInfo.
     */
    public int addFieldrefCpInfo(ProgramClassFile programClassFile,
                                 String           className,
                                 int              nameAndTypeIndex,
                                 ClassFile        referencedClassFile,
                                 MemberInfo       referencedMemberInfo)
    {
        return addFieldrefCpInfo(programClassFile,
                                 addClassCpInfo(programClassFile,
                                                className,
                                                referencedClassFile),
                                 nameAndTypeIndex,
                                 referencedClassFile,
                                 referencedMemberInfo);
    }


    /**
     * Finds or creates a FieldrefCpInfo constant pool entry with the given
     * class constant pool entry index, field name, and descriptor, in the
     * given class file.
     * @return the constant pool index of the FieldrefCpInfo.
     */
    public int addFieldrefCpInfo(ProgramClassFile programClassFile,
                                 int              classIndex,
                                 String           name,
                                 String           descriptor,
                                 ClassFile        referencedClassFile,
                                 MemberInfo       referencedMemberInfo,
                                 ClassFile[]      referencedClassFiles)
    {
        return addFieldrefCpInfo(programClassFile,
                                 classIndex,
                                 addNameAndTypeCpInfo(programClassFile,
                                                      name,
                                                      descriptor,
                                                      referencedClassFiles),
                                 referencedClassFile,
                                 referencedMemberInfo);
    }


    /**
     * Finds or creates a FieldrefCpInfo constant pool entry with the given
     * class constant pool entry index and name and type constant pool entry index
     * the given class file.
     * @return the constant pool index of the FieldrefCpInfo.
     */
    public int addFieldrefCpInfo(ProgramClassFile programClassFile,
                                 int              classIndex,
                                 int              nameAndTypeIndex,
                                 ClassFile        referencedClassFile,
                                 MemberInfo       referencedMemberInfo)
    {
        CpInfo[] constantPool      = programClassFile.constantPool;
        int      constantPoolCount = programClassFile.u2constantPoolCount;

        // Check if the entry already exists.
        for (int index = 1; index < constantPoolCount; index++)
        {
            CpInfo cpInfo = constantPool[index];

            if (cpInfo != null &&
                cpInfo.getTag() == ClassConstants.CONSTANT_Fieldref)
            {
                FieldrefCpInfo fieldrefCpInfo = (FieldrefCpInfo)cpInfo;
                if (fieldrefCpInfo.u2classIndex       == classIndex &&
                    fieldrefCpInfo.u2nameAndTypeIndex == nameAndTypeIndex)
                {
                    return index;
                }
            }
        }

        return addCpInfo(programClassFile,
                         new FieldrefCpInfo(classIndex,
                                            nameAndTypeIndex,
                                            referencedClassFile,
                                            referencedMemberInfo));
    }


    /**
     * Finds or creates a MethodrefCpInfo constant pool entry with the given
     * class name, method name, and descriptor, in the given class file.
     * @return the constant pool index of the MethodrefCpInfo.
     */
    public int addMethodrefCpInfo(ProgramClassFile programClassFile,
                                  String           className,
                                  String           name,
                                  String           descriptor,
                                  ClassFile        referencedClassFile,
                                  MemberInfo       referencedMemberInfo,
                                  ClassFile[]      referencedClassFiles)
    {
        return addMethodrefCpInfo(programClassFile,
                                  className,
                                  addNameAndTypeCpInfo(programClassFile,
                                                       name,
                                                       descriptor,
                                                       referencedClassFiles),
                                  referencedClassFile,
                                  referencedMemberInfo);
    }


    /**
     * Finds or creates a MethodrefCpInfo constant pool entry with the given
     * class name, method name, and descriptor, in the given class file.
     * @return the constant pool index of the MethodrefCpInfo.
     */
    public int addMethodrefCpInfo(ProgramClassFile programClassFile,
                                  String           className,
                                  int              nameAndTypeIndex,
                                  ClassFile        referencedClassFile,
                                  MemberInfo       referencedMemberInfo)
    {
        return addMethodrefCpInfo(programClassFile,
                                  addClassCpInfo(programClassFile,
                                                 className,
                                                 referencedClassFile),
                                  nameAndTypeIndex,
                                  referencedClassFile,
                                  referencedMemberInfo);
    }


    /**
     * Finds or creates a MethodrefCpInfo constant pool entry with the given
     * class constant pool entry index, method name, and descriptor, in the
     * given class file.
     * @return the constant pool index of the MethodrefCpInfo.
     */
    public int addMethodrefCpInfo(ProgramClassFile programClassFile,
                                  int              classIndex,
                                  String           name,
                                  String           descriptor,
                                  ClassFile        referencedClassFile,
                                  MemberInfo       referencedMemberInfo,
                                  ClassFile[]      referencedClassFiles)
    {
        return addMethodrefCpInfo(programClassFile,
                                  classIndex,
                                  addNameAndTypeCpInfo(programClassFile,
                                                       name,
                                                       descriptor,
                                                       referencedClassFiles),
                                  referencedClassFile,
                                  referencedMemberInfo);
    }


    /**
     * Finds or creates a MethodrefCpInfo constant pool entry with the given
     * class constant pool entry index and name and type constant pool entry index
     * the given class file.
     * @return the constant pool index of the MethodrefCpInfo.
     */
    public int addMethodrefCpInfo(ProgramClassFile programClassFile,
                                  int              classIndex,
                                  int              nameAndTypeIndex,
                                  ClassFile        referencedClassFile,
                                  MemberInfo       referencedMemberInfo)
    {
        CpInfo[] constantPool      = programClassFile.constantPool;
        int      constantPoolCount = programClassFile.u2constantPoolCount;

        // Check if the entry already exists.
        for (int index = 1; index < constantPoolCount; index++)
        {
            CpInfo cpInfo = constantPool[index];

            if (cpInfo != null &&
                cpInfo.getTag() == ClassConstants.CONSTANT_Methodref)
            {
                MethodrefCpInfo methodrefCpInfo = (MethodrefCpInfo)cpInfo;
                if (methodrefCpInfo.u2classIndex       == classIndex &&
                    methodrefCpInfo.u2nameAndTypeIndex == nameAndTypeIndex)
                {
                    return index;
                }
            }
        }

        return addCpInfo(programClassFile,
                         new MethodrefCpInfo(classIndex,
                                             nameAndTypeIndex,
                                             referencedClassFile,
                                             referencedMemberInfo));
    }


    /**
     * Finds or creates a InterfaceMethodrefCpInfo constant pool entry with the
     * given class name, method name, and descriptor, in the given class file.
     * @return the constant pool index of the InterfaceMethodrefCpInfo.
     */
    public int addInterfaceMethodrefCpInfo(ProgramClassFile programClassFile,
                                           String           className,
                                           String           name,
                                           String           descriptor,
                                           ClassFile        referencedClassFile,
                                           MemberInfo       referencedMemberInfo,
                                           ClassFile[]      referencedClassFiles)
    {
        return addInterfaceMethodrefCpInfo(programClassFile,
                                           className,
                                           addNameAndTypeCpInfo(programClassFile,
                                                                name,
                                                                descriptor,
                                                                referencedClassFiles),
                                                                referencedClassFile,
                                                                referencedMemberInfo);
    }


    /**
     * Finds or creates a InterfaceMethodrefCpInfo constant pool entry with the
     * given class name, method name, and descriptor, in the given class file.
     * @return the constant pool index of the InterfaceMethodrefCpInfo.
     */
    public int addInterfaceMethodrefCpInfo(ProgramClassFile programClassFile,
                                           String           className,
                                           int              nameAndTypeIndex,
                                           ClassFile        referencedClassFile,
                                           MemberInfo       referencedMemberInfo)
    {
        return addInterfaceMethodrefCpInfo(programClassFile,
                                           addClassCpInfo(programClassFile,
                                                          className,
                                                          referencedClassFile),
                                                          nameAndTypeIndex,
                                                          referencedClassFile,
                                                          referencedMemberInfo);
    }


    /**
     * Finds or creates a InterfaceMethodrefCpInfo constant pool entry with the
     * given class constant pool entry index, method name, and descriptor, in
     * the given class file.
     * @return the constant pool index of the InterfaceMethodrefCpInfo.
     */
    public int addInterfaceMethodrefCpInfo(ProgramClassFile programClassFile,
                                           int              classIndex,
                                           String           name,
                                           String           descriptor,
                                           ClassFile        referencedClassFile,
                                           MemberInfo       referencedMemberInfo,
                                           ClassFile[]      referencedClassFiles)
    {
        return addInterfaceMethodrefCpInfo(programClassFile,
                                           classIndex,
                                           addNameAndTypeCpInfo(programClassFile,
                                                                name,
                                                                descriptor,
                                                                referencedClassFiles),
                                                                referencedClassFile,
                                                                referencedMemberInfo);
    }


    /**
     * Finds or creates a InterfaceMethodrefCpInfo constant pool entry with the
     * given class constant pool entry index and name and type constant pool
     * entry index the given class file.
     * @return the constant pool index of the InterfaceMethodrefCpInfo.
     */
    public int addInterfaceMethodrefCpInfo(ProgramClassFile programClassFile,
                                           int              classIndex,
                                           int              nameAndTypeIndex,
                                           ClassFile        referencedClassFile,
                                           MemberInfo       referencedMemberInfo)
    {
        CpInfo[] constantPool      = programClassFile.constantPool;
        int      constantPoolCount = programClassFile.u2constantPoolCount;

        // Check if the entry already exists.
        for (int index = 1; index < constantPoolCount; index++)
        {
            CpInfo cpInfo = constantPool[index];

            if (cpInfo != null &&
                            cpInfo.getTag() == ClassConstants.CONSTANT_InterfaceMethodref)
            {
                InterfaceMethodrefCpInfo methodrefCpInfo = (InterfaceMethodrefCpInfo)cpInfo;
                if (methodrefCpInfo.u2classIndex       == classIndex &&
                                methodrefCpInfo.u2nameAndTypeIndex == nameAndTypeIndex)
                {
                    return index;
                }
            }
        }

        return addCpInfo(programClassFile,
                         new InterfaceMethodrefCpInfo(classIndex,
                                                      nameAndTypeIndex,
                                                      referencedClassFile,
                                                      referencedMemberInfo));
    }


    /**
     * Finds or creates a ClassCpInfo constant pool entry with the given name,
     * in the given class file.
     * @return the constant pool index of the ClassCpInfo.
     */
    public int addClassCpInfo(ProgramClassFile programClassFile,
                              String           name,
                              ClassFile        referencedClassFile)
    {
        CpInfo[] constantPool        = programClassFile.constantPool;
        int      constantPoolCount = programClassFile.u2constantPoolCount;

        // Check if the entry already exists.
        for (int index = 1; index < constantPoolCount; index++)
        {
            CpInfo cpInfo = constantPool[index];

            if (cpInfo != null &&
                cpInfo.getTag() == ClassConstants.CONSTANT_Class)
            {
                ClassCpInfo classCpInfo = (ClassCpInfo)cpInfo;
                if (classCpInfo.getName(programClassFile).equals(name))
                {
                    return index;
                }
            }
        }

        int nameIndex = addUtf8CpInfo(programClassFile, name);

        return addCpInfo(programClassFile,
                         new ClassCpInfo(nameIndex,
                                         referencedClassFile));
    }


    /**
     * Finds or creates a NameAndTypeCpInfo constant pool entry with the given
     * name and type, in the given class file.
     * @return the constant pool index of the NameAndTypeCpInfo.
     */
    public int addNameAndTypeCpInfo(ProgramClassFile programClassFile,
                                    String           name,
                                    String           type,
                                    ClassFile[]      referencedClassFiles)
    {
        CpInfo[] constantPool        = programClassFile.constantPool;
        int      constantPoolCount = programClassFile.u2constantPoolCount;

        // Check if the entry already exists.
        for (int index = 1; index < constantPoolCount; index++)
        {
            CpInfo cpInfo = constantPool[index];

            if (cpInfo != null &&
                cpInfo.getTag() == ClassConstants.CONSTANT_NameAndType)
            {
                NameAndTypeCpInfo nameAndTypeCpInfo = (NameAndTypeCpInfo)cpInfo;
                if (nameAndTypeCpInfo.getName(programClassFile).equals(name) &&
                    nameAndTypeCpInfo.getType(programClassFile).equals(type))
                {
                    return index;
                }
            }
        }

        int nameIndex       = addUtf8CpInfo(programClassFile, name);
        int descriptorIndex = addUtf8CpInfo(programClassFile, type);

        return addCpInfo(programClassFile,
                         new NameAndTypeCpInfo(nameIndex,
                                               descriptorIndex,
                                               referencedClassFiles));
    }


    /**
     * Finds or creates an Utf8CpInfo constant pool entry for the given string,
     * in the given class file.
     * @return the constant pool index of the Utf8CpInfo.
     */
    public int addUtf8CpInfo(ProgramClassFile programClassFile,
                             String           string)
    {
        CpInfo[] constantPool        = programClassFile.constantPool;
        int      constantPoolCount = programClassFile.u2constantPoolCount;

        // Check if the entry already exists.
        for (int index = 1; index < constantPoolCount; index++)
        {
            CpInfo cpInfo = constantPool[index];

            if (cpInfo != null &&
                cpInfo.getTag() == ClassConstants.CONSTANT_Utf8)
            {
                Utf8CpInfo utf8CpInfo = (Utf8CpInfo)cpInfo;
                if (utf8CpInfo.getString().equals(string))
                {
                    return index;
                }
            }
        }

        return addCpInfo(programClassFile, new Utf8CpInfo(string));
    }


    /**
     * Adds a given constant pool entry to the end of the constant pool
     * in the given class file.
     * @return the constant pool index for the added entry.
     */
    private int addCpInfo(ProgramClassFile programClassFile,
                          CpInfo           cpInfo)
    {
        CpInfo[] constantPool        = programClassFile.constantPool;
        int      constantPoolCount = programClassFile.u2constantPoolCount;

        // Make sure there is enough space for another constant pool entry.
        if (constantPoolCount == constantPool.length)
        {
            programClassFile.constantPool = new CpInfo[constantPoolCount+1];
            System.arraycopy(constantPool, 0,
                             programClassFile.constantPool, 0,
                             constantPoolCount);
            constantPool = programClassFile.constantPool;
        }

        // Create a new Utf8CpInfo for the given string.
        constantPool[programClassFile.u2constantPoolCount++] = cpInfo;

        return constantPoolCount;
    }
}
