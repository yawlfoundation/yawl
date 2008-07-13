/* $Id: ProgramClassFile.java,v 1.37.2.3 2007/01/25 21:01:01 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.classfile;


import proguard.classfile.util.*;
import proguard.classfile.visitor.*;
import proguard.classfile.attribute.*;

import java.io.*;

/**
 * This is a complete representation of the data in a Java class file.
 * A ClassFile instance can be generated using the static create(DataInput)
 * method, manipulated using various operators, and persisted back using the
 * write(DataOutput) method.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class ProgramClassFile implements ClassFile
{
    public int                 u4magic;
    public int                 u2minorVersion;
    public int                 u2majorVersion;
    public int                 u2constantPoolCount;
    public CpInfo[]            constantPool;
    public int                 u2accessFlags;
    public int                 u2thisClass;
    public int                 u2superClass;
    public int                 u2interfacesCount;
    public int[]               u2interfaces;
    public int                 u2fieldsCount;
    public ProgramFieldInfo[]  fields;
    public int                 u2methodsCount;
    public ProgramMethodInfo[] methods;
    public int                 u2attributesCount;
    public AttrInfo[]          attributes;

    /**
     * An extra field pointing to the subclasses of this class.
     * This field is filled out by the <code>{@link ClassFileHierarchyInitializer}</code>.
     */
    public ClassFile[] subClasses = null;

    /**
     * An extra field in which visitors can store information.
     */
    public Object visitorInfo;


    /**
     * Creates a new ClassFile from the class file format data in the DataInput
     * stream.
     *
     * @throws IOException if class file is corrupt or incomplete
     */
    public static ClassFile create(DataInput din) throws IOException
    {
        ProgramClassFile cf = new ProgramClassFile();
        cf.read(din);
        return cf;
    }


    /**
     * Creates an empty ProgramClassFile.
     */
    private ProgramClassFile() {}


    /**
     * Imports the class data into this ProgramClassFile.
     */
    private void read(DataInput din) throws IOException
    {
        // Read and check the class file magic number.
        u4magic = din.readInt();
        ClassUtil.checkMagicNumber(u4magic);

        // Read and check the class file version numbers.
        u2minorVersion = din.readUnsignedShort();
        u2majorVersion = din.readUnsignedShort();
        ClassUtil.checkVersionNumbers(u2majorVersion, u2minorVersion);

        // Read the constant pool.
        u2constantPoolCount = din.readUnsignedShort();
        constantPool        = new CpInfo[u2constantPoolCount];

        // Fill the constant pool. The zero entry is not used, nor are the
        // entries following a Long or Double.
        for (int i = 1; i < u2constantPoolCount; i++)
        {
            constantPool[i] = CpInfo.create(din);

            int tag = constantPool[i].getTag();
            if (tag == ClassConstants.CONSTANT_Long ||
                tag == ClassConstants.CONSTANT_Double)
            {
                i++;
            }
        }

        // Read the access flags, class name, and super class name.
        u2accessFlags = din.readUnsignedShort();
        u2thisClass   = din.readUnsignedShort();
        u2superClass  = din.readUnsignedShort();

        // Read the interfaces.
        u2interfacesCount = din.readUnsignedShort();
        if (u2interfacesCount > 0)
        {
            u2interfaces = new int[u2interfacesCount];
            for (int i = 0; i < u2interfacesCount; i++)
            {
                u2interfaces[i] = din.readUnsignedShort();
            }
        }

        // Read the fields.
        u2fieldsCount = din.readUnsignedShort();
        if (u2fieldsCount > 0)
        {
            fields = new ProgramFieldInfo[u2fieldsCount];
            for (int i = 0; i < u2fieldsCount; i++)
            {
                fields[i] = ProgramFieldInfo.create(din, this);
            }
        }

        // Read the methods.
        u2methodsCount = din.readUnsignedShort();
        if (u2methodsCount > 0)
        {
            methods = new ProgramMethodInfo[u2methodsCount];
            for (int i = 0; i < u2methodsCount; i++)
            {
                methods[i] = ProgramMethodInfo.create(din, this);
            }
        }

        // Read the attributes.
        u2attributesCount = din.readUnsignedShort();
        if (u2attributesCount > 0)
        {
            attributes = new AttrInfo[u2attributesCount];
            for (int i = 0; i < u2attributesCount; i++)
            {
                attributes[i] = AttrInfo.create(din, this);
            }
        }
    }


    /**
     * Exports the representation to a DataOutput stream.
     */
    public void write(DataOutput dout) throws IOException
    {
        dout.writeInt(u4magic);
        dout.writeShort(u2minorVersion);
        dout.writeShort(u2majorVersion);
        dout.writeShort(u2constantPoolCount);
        for (int i = 1; i < u2constantPoolCount; i++)
        {
            CpInfo cpInfo = constantPool[i];
            if (cpInfo != null)
            {
                cpInfo.write(dout);
            }
        }
        dout.writeShort(u2accessFlags);
        dout.writeShort(u2thisClass);
        dout.writeShort(u2superClass);
        dout.writeShort(u2interfacesCount);
        for (int i = 0; i < u2interfacesCount; i++)
        {
            dout.writeShort(u2interfaces[i]);
        }
        dout.writeShort(u2fieldsCount);
        for (int i = 0; i < u2fieldsCount; i++)
        {
            fields[i].write(dout);
        }
        dout.writeShort(u2methodsCount);
        for (int i = 0; i < u2methodsCount; i++)
        {
            methods[i].write(dout);
        }
        dout.writeShort(u2attributesCount);
        for (int i = 0; i < u2attributesCount; i++)
        {
            attributes[i].write(dout);
        }
    }


    /**
     * Returns the CpInfo at the given index in the constant pool.
     */
    public CpInfo getCpEntry(int cpIndex)
    {
        return constantPool[cpIndex];
    }


    /**
     * Returns the attribute specified by the given name.
     */
    AttrInfo getAttribute(String name)
    {
        for (int i = 0; i < u2attributesCount; i++)
        {
            AttrInfo attribute = attributes[i];
            if (attribute.getAttributeName(this).equals(name))
            {
                return attribute;
            }
        }

        return null;
    }


    // Implementations for ClassFile.

    public int getAccessFlags()
    {
        return u2accessFlags;
    }

    public String getName()
    {
        return getCpClassNameString(u2thisClass);
    }

    public String getSuperName()
    {
        return u2superClass == 0 ? null : getCpClassNameString(u2superClass);
    }

    public int getInterfaceCount()
    {
        return u2interfacesCount;
    }

    public String getInterfaceName(int index)
    {
        return getCpClassNameString(u2interfaces[index]);
    }

    public int getCpTag(int cpIndex)
    {
        return constantPool[cpIndex].getTag();
    }

    public String getCpString(int cpIndex)
    {
        return ((Utf8CpInfo)constantPool[cpIndex]).getString();
    }

    public String getCpClassNameString(int cpIndex)
    {
        ClassCpInfo classEntry = (ClassCpInfo)constantPool[cpIndex];
        Utf8CpInfo  nameEntry  = (Utf8CpInfo)constantPool[classEntry.getNameIndex()];

        return nameEntry.getString();
    }

    public String getCpNameString(int cpIndex)
    {
        return ((NameAndTypeCpInfo)constantPool[cpIndex]).getName(this);
    }

    public String getCpTypeString(int cpIndex)
    {
        return ((NameAndTypeCpInfo)constantPool[cpIndex]).getType(this);
    }


    public void addSubClass(ClassFile classFile)
    {
        if (subClasses == null)
        {
            subClasses = new ClassFile[1];
        }
        else
        {
            // Copy the old elements into new larger array.
            ClassFile[] temp = new ClassFile[subClasses.length+1];
            System.arraycopy(subClasses, 0, temp, 0, subClasses.length);
            subClasses = temp;
        }

        subClasses[subClasses.length-1] = classFile;
    }


    public ClassFile getSuperClass()
    {
        return u2superClass != 0 ?
            ((ClassCpInfo)constantPool[u2superClass]).referencedClassFile :
            null;
    }


    public ClassFile getInterface(int index)
    {
        return ((ClassCpInfo)constantPool[u2interfaces[index]]).referencedClassFile;
    }


    public boolean extends_(ClassFile classFile)
    {
        if (this.equals(classFile))
        {
            return true;
        }

        ClassFile superClass = getSuperClass();
        return superClass != null &&
               superClass.extends_(classFile);
    }


    public boolean implements_(ClassFile classFile)
    {
        if (this.equals(classFile))
        {
            return true;
        }

        for (int i = 0; i < u2interfacesCount; i++)
        {
            ClassFile interfaceClass = getInterface(i);
            if (interfaceClass != null &&
                interfaceClass.implements_(classFile))
            {
                return true;
            }
        }

        return false;
    }


    public FieldInfo findField(String name, String descriptor)
    {
        for (int i = 0; i < u2fieldsCount; i++)
        {
            FieldInfo field = fields[i];
            if ((name       == null || field.getName(this).equals(name)) &&
                (descriptor == null || field.getDescriptor(this).equals(descriptor)))
            {
                return field;
            }
        }

        return null;
    }


    public MethodInfo findMethod(String name, String descriptor)
    {
        for (int i = 0; i < u2methodsCount; i++)
        {
            MethodInfo method = methods[i];
            if ((name       == null || method.getName(this).equals(name)) &&
                (descriptor == null || method.getDescriptor(this).equals(descriptor)))
            {
                return method;
            }
        }

        return null;
    }


    public void accept(ClassFileVisitor classFileVisitor)
    {
        classFileVisitor.visitProgramClassFile(this);
    }


    public void hierarchyAccept(boolean          visitThisClass,
                                boolean          visitSuperClass,
                                boolean          visitInterfaces,
                                boolean          visitSubclasses,
                                ClassFileVisitor classFileVisitor)
    {
        // First visit the current classfile.
        if (visitThisClass)
        {
            accept(classFileVisitor);
        }

        // Then visit its superclass, recursively.
        if (visitSuperClass)
        {
            ClassFile superClass = getSuperClass();
            if (superClass != null)
            {
                superClass.hierarchyAccept(true,
                                           true,
                                           visitInterfaces,
                                           false,
                                           classFileVisitor);
            }
        }

        // Then visit its interfaces, recursively.
        if (visitInterfaces)
        {
            for (int i = 0; i < u2interfacesCount; i++)
            {
                ClassFile interfaceClass = getInterface(i);
                if (interfaceClass != null)
                {
                    interfaceClass.hierarchyAccept(true,
                                                   true,
                                                   true,
                                                   false,
                                                   classFileVisitor);
                }
            }
        }

        // Then visit its subclasses, recursively.
        if (visitSubclasses)
        {
            if (subClasses != null)
            {
                for (int i = 0; i < subClasses.length; i++)
                {
                    ClassFile subClass = subClasses[i];
                    subClass.hierarchyAccept(true,
                                             false,
                                             false,
                                             true,
                                             classFileVisitor);
                }
            }
        }
    }


    public void constantPoolEntriesAccept(CpInfoVisitor cpInfoVisitor)
    {
        for (int i = 1; i < u2constantPoolCount; i++)
        {
            if (constantPool[i] != null)
            {
                constantPool[i].accept(this, cpInfoVisitor);
            }
        }
    }


    public void constantPoolEntryAccept(int index, CpInfoVisitor cpInfoVisitor)
    {
        constantPool[index].accept(this, cpInfoVisitor);
    }


    public void fieldsAccept(MemberInfoVisitor memberInfoVisitor)
    {
        for (int i = 0; i < u2fieldsCount; i++)
        {
            fields[i].accept(this, memberInfoVisitor);
        }
    }


    public void fieldAccept(String name, String descriptor, MemberInfoVisitor memberInfoVisitor)
    {
        FieldInfo field = findField(name, descriptor);
        if (field != null)
        {
            field.accept(this, memberInfoVisitor);
        }
    }


    public void methodsAccept(MemberInfoVisitor memberInfoVisitor)
    {
        for (int i = 0; i < u2methodsCount; i++)
        {
            methods[i].accept(this, memberInfoVisitor);
        }
    }


    public void methodAccept(String name, String descriptor, MemberInfoVisitor memberInfoVisitor)
    {
        MethodInfo method = findMethod(name, descriptor);
        if (method != null)
        {
            method.accept(this, memberInfoVisitor);
        }
    }


    public boolean mayHaveImplementations(MethodInfo methodInfo)
    {
        return
           (u2accessFlags & ClassConstants.INTERNAL_ACC_FINAL) == 0 &&
           (methodInfo == null ||
            ((methodInfo.getAccessFlags() & (ClassConstants.INTERNAL_ACC_PRIVATE |
                                             ClassConstants.INTERNAL_ACC_STATIC  |
                                             ClassConstants.INTERNAL_ACC_FINAL)) == 0 &&
             !methodInfo.getName(this).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT)));
    }


    private boolean isSpecial(MethodInfo methodInfo)
    {
        return
            (methodInfo.getAccessFlags() & (ClassConstants.INTERNAL_ACC_PRIVATE |
                                            ClassConstants.INTERNAL_ACC_STATIC)) != 0 ||
            methodInfo.getName(this).equals(ClassConstants.INTERNAL_METHOD_NAME_INIT);
    }


    public void methodImplementationsAccept(MethodInfo        methodInfo,
                                            boolean           visitThisMethod,
                                            MemberInfoVisitor memberInfoVisitor)
    {
        methodImplementationsAccept(methodInfo.getName(this),
                                    methodInfo.getDescriptor(this),
                                    methodInfo,
                                    visitThisMethod,
                                    true,
                                    true,
                                    true,
                                    memberInfoVisitor);
    }


    public void methodImplementationsAccept(String            name,
                                            String            descriptor,
                                            boolean           visitThisMethod,
                                            MemberInfoVisitor memberInfoVisitor)
    {
        methodImplementationsAccept(name,
                                    descriptor,
                                    visitThisMethod,
                                    true,
                                    true,
                                    true,
                                    memberInfoVisitor);
    }


    public void methodImplementationsAccept(String            name,
                                            String            descriptor,
                                            boolean           visitThisMethod,
                                            boolean           visitSpecialMethods,
                                            boolean           visitSuperMethods,
                                            boolean           visitOverridingMethods,
                                            MemberInfoVisitor memberInfoVisitor)
    {
        methodImplementationsAccept(name,
                                    descriptor,
                                    findMethod(name, descriptor),
                                    visitThisMethod,
                                    visitSpecialMethods,
                                    visitSuperMethods,
                                    visitOverridingMethods,
                                    memberInfoVisitor);
    }


    public void methodImplementationsAccept(String            name,
                                            String            descriptor,
                                            MethodInfo        methodInfo,
                                            boolean           visitThisMethod,
                                            boolean           visitSpecialMethods,
                                            boolean           visitSuperMethods,
                                            boolean           visitOverridingMethods,
                                            MemberInfoVisitor memberInfoVisitor)
    {
        // Do we have the method in this class?
        if (methodInfo != null)
        {
            // Is it a special method?
            if (isSpecial(methodInfo))
            {
                // Visit the special method in this class, if allowed.
                if (visitSpecialMethods)
                {
                    methodInfo.accept(this, memberInfoVisitor);

                    // The method can't have any other implementations.
                    return;
                }
            }
            else
            {
                // Visit the method in this class, if allowed.
                if (visitThisMethod)
                {
                    methodInfo.accept(this, memberInfoVisitor);
                }

                // We don't have to look in subclasses if there can't be
                // any overriding implementations.
                if (!mayHaveImplementations(methodInfo))
                {
                    visitOverridingMethods = false;
                }

                // We don't have to look in superclasses if we have a concrete
                // implementation here.
                if ((methodInfo.getAccessFlags() & ClassConstants.INTERNAL_ACC_ABSTRACT) == 0)
                {
                    visitSuperMethods = false;
                }
            }
        }

        // Then visit the method in its subclasses, recursively.
        if (visitOverridingMethods)
        {
            // Go looking for implementations in all of the subclasses.
            if (subClasses != null)
            {
                for (int i = 0; i < subClasses.length; i++)
                {
                    ClassFile subClass = subClasses[i];
                    subClass.methodImplementationsAccept(name,
                                                         descriptor,
                                                         true,
                                                         false,
                                                         visitSuperMethods,
                                                         true,
                                                         memberInfoVisitor);
                }
            }

            // We don't have to look in superclasses right away if we dont't
            // have a concrete class here.
            if ((u2accessFlags & (ClassConstants.INTERNAL_ACC_INTERFACE |
                                  ClassConstants.INTERNAL_ACC_ABSTRACT)) != 0)
            {
                visitSuperMethods = false;
            }
        }

        // Then visit the method in its superclass, recursively.
        if (visitSuperMethods)
        {
            ClassFile superClass = getSuperClass();
            if (superClass != null)
            {
                superClass.methodImplementationsAccept(name,
                                                       descriptor,
                                                       true,
                                                       false,
                                                       true,
                                                       false,
                                                       memberInfoVisitor);
            }
        }
    }


    public void attributesAccept(AttrInfoVisitor attrInfoVisitor)
    {
        for (int i = 0; i < u2attributesCount; i++)
        {
            attributes[i].accept(this, attrInfoVisitor);
        }
    }


    // Implementations for VisitorAccepter.

    public Object getVisitorInfo()
    {
        return visitorInfo;
    }

    public void setVisitorInfo(Object visitorInfo)
    {
        this.visitorInfo = visitorInfo;
    }
}
