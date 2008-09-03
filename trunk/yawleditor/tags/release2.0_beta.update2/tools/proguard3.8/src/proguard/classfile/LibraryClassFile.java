/* $Id: LibraryClassFile.java,v 1.40.2.3 2007/01/25 21:01:01 eric Exp $
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
 * This is a compact representation of the essential data in a Java class file.
 * A LibraryClassFile instance representing a *.class file can be generated
 * using the static create(DataInput) method, but not persisted back.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class LibraryClassFile implements ClassFile
{
    // Some objects and arrays that can be reused.
    private static LibraryClassFile    reusableLibraryClassFile;
    private static CpInfo[]            reusableConstantPool;
    private static LibraryFieldInfo[]  reusableFields;
    private static LibraryMethodInfo[] reusableMethods;


    public int                 u2accessFlags;
    public String              thisClassName;
    public String              superClassName;
    public String[]            interfaceNames;
    public LibraryFieldInfo[]  fields;
    public LibraryMethodInfo[] methods;

    /**
     * An extra field pointing to the superclass of this class.
     * This field is filled out by the <code>{@link ClassFileHierarchyInitializer}</code>.
     */
    public ClassFile   superClass          = null;

    /**
     * An extra field pointing to the interfaces of this class.
     * This field is filled out by the <code>{@link ClassFileHierarchyInitializer}</code>.
     */
    public ClassFile[] interfaceClasses    = null;

    /**
     * An extra field pointing to the subclasses of this class.
     * This field is filled out by the <code>{@link ClassFileHierarchyInitializer}</code>.
     */
    public ClassFile[] subClasses          = null;

    /**
     * An extra field in which visitors can store information.
     */
    public Object visitorInfo;


    /**
     * Creates a new LibraryClassFile from the class file format data in the DataInput
     * stream. If specified, this method may return <code>null</code> if the
     * class file is not visible.
     *
     * @throws IOException if the class file is corrupt or incomplete
     */
    public static LibraryClassFile create(DataInput din,
                                          boolean skipNonPublicClasses,
                                          boolean skipNonPublicClassMembers)
    throws IOException
    {
        // See if we have to create a new library class file object.
        if (reusableLibraryClassFile == null)
        {
            reusableLibraryClassFile = new LibraryClassFile();
        }

        // We'll start using the reusable object.
        LibraryClassFile libraryClassFile = reusableLibraryClassFile;

        libraryClassFile.read(din, skipNonPublicClasses, skipNonPublicClassMembers);

        // Did we actually read a useful library class file?
        if (libraryClassFile.thisClassName != null)
        {
            // We can't reuse this library class file object next time.
            reusableLibraryClassFile = null;
        }
        else
        {
            // We don't have a useful library class file to return.
            libraryClassFile = null;
        }

        return libraryClassFile;
    }


    /**
     * Creates an empty LibraryClassFile.
     */
    private LibraryClassFile() {}


    /**
     * Imports the class data into this LibraryClassFile.
     */
    private void read(DataInput din,
                      boolean skipNonPublicClasses,
                      boolean skipNonPublicClassmembers) throws IOException
    {
        // Read and check the class file magic number.
        int u4magic = din.readInt();
        ClassUtil.checkMagicNumber(u4magic);

        // Read and check the class file version numbers.
        int u2minorVersion = din.readUnsignedShort();
        int u2majorVersion = din.readUnsignedShort();
        ClassUtil.checkVersionNumbers(u2majorVersion, u2minorVersion);

        // Read the constant pool.
        int u2constantPoolCount = din.readUnsignedShort();

        // Make sure there's sufficient space in the reused constant pool array.
        if (reusableConstantPool == null ||
            reusableConstantPool.length < u2constantPoolCount)
        {
            reusableConstantPool = new CpInfo[u2constantPoolCount];
        }

        // Fill the constant pool. The zero entry is not used, nor are the
        // entries following a Long or Double.
        for (int i = 1; i < u2constantPoolCount; i++)
        {
            reusableConstantPool[i] = CpInfo.createOrShare(din);
            int tag = reusableConstantPool[i].getTag();
            if (tag == ClassConstants.CONSTANT_Long ||
                tag == ClassConstants.CONSTANT_Double)
            {
                i++;
            }
        }

        u2accessFlags = din.readUnsignedShort();

        // We may stop parsing this library class file if it's not public anyway.
        // E.g. only about 60% of all rt.jar classes need to be parsed.
        if (skipNonPublicClasses && !isVisible())
        {
            return;
        }

        // Read the class and super class indices.
        int u2thisClass  = din.readUnsignedShort();
        int u2superClass = din.readUnsignedShort();

        // Store their actual names.
        thisClassName  = toName(reusableConstantPool, u2thisClass);
        superClassName = (u2superClass == 0) ? null :
                         toName(reusableConstantPool, u2superClass);

        // Read the interface indices.
        int u2interfacesCount = din.readUnsignedShort();

        // Store their actual names.
        interfaceNames = new String[u2interfacesCount];
        for (int i = 0; i < u2interfacesCount; i++)
        {
            int u2interface = din.readUnsignedShort();
            interfaceNames[i] = toName(reusableConstantPool, u2interface);
        }

        // Read the fields.
        int u2fieldsCount = din.readUnsignedShort();

        // Make sure there's sufficient space in the reused fields array.
        if (reusableFields == null ||
            reusableFields.length < u2fieldsCount)
        {
            reusableFields = new LibraryFieldInfo[u2fieldsCount];
        }

        int visibleFieldsCount = 0;
        for (int i = 0; i < u2fieldsCount; i++)
        {
            LibraryFieldInfo field = LibraryFieldInfo.create(din, reusableConstantPool);

            // Only store fields that are visible.
            if (field.isVisible() ||
                (!skipNonPublicClassmembers &&
                 (field.getAccessFlags() & ClassConstants.INTERNAL_ACC_PRIVATE) == 0))
            {
                reusableFields[visibleFieldsCount++] = field;
            }
        }

        // Copy the visible fields into a fields array of the right size.
        fields = new LibraryFieldInfo[visibleFieldsCount];
        System.arraycopy(reusableFields, 0, fields, 0, visibleFieldsCount);


        // Read the methods.
        int u2methodsCount = din.readUnsignedShort();

        // Make sure there's sufficient space in the reused methods array.
        if (reusableMethods == null ||
            reusableMethods.length < u2methodsCount)
        {
            reusableMethods = new LibraryMethodInfo[u2methodsCount];
        }

        int visibleMethodsCount = 0;
        for (int i = 0; i < u2methodsCount; i++)
        {
            LibraryMethodInfo method = LibraryMethodInfo.create(din, reusableConstantPool);

            // Only store methods that are visible.
            if (method.isVisible() ||
                (!skipNonPublicClasses &&
                 (method.getAccessFlags() & ClassConstants.INTERNAL_ACC_PRIVATE) == 0))
            {
                reusableMethods[visibleMethodsCount++] = method;
            }
        }

        // Copy the visible methods into a methods array of the right size.
        methods = new LibraryMethodInfo[visibleMethodsCount];
        System.arraycopy(reusableMethods, 0, methods, 0, visibleMethodsCount);


        // Skip the attributes.
        int u2attributesCount = din.readUnsignedShort();
        for (int i = 0; i < u2attributesCount; i++)
        {
            LibraryAttrInfo.skip(din);
        }
    }


    /**
     * Returns whether this library class file is visible to the outside world.
     */
    boolean isVisible()
    {
        return (u2accessFlags & ClassConstants.INTERNAL_ACC_PUBLIC) != 0;
    }


    /**
     * Returns the class name of the ClassCpInfo at the specified index in the
     * given constant pool.
     */
    private String toName(CpInfo[] constantPool, int cpIndex)
    {
        ClassCpInfo classEntry = (ClassCpInfo)constantPool[cpIndex];
        Utf8CpInfo  nameEntry  = (Utf8CpInfo)constantPool[classEntry.getNameIndex()];

        return nameEntry.getString();
    }


    // Implementations for ClassFile.

    public int getAccessFlags()
    {
        return u2accessFlags;
    }

    public String getName()
    {
        return thisClassName;
    }

    public String getSuperName()
    {
        // This may be java/lang/Object, in which case there is no super.
        return superClassName;
    }

    public int getInterfaceCount()
    {
        return interfaceClasses.length;
    }

    public String getInterfaceName(int index)
    {
        return interfaceNames[index];
    }

    public int getCpTag(int cpIndex)
    {
        return -1;
    }

    public String getCpString(int cpIndex)
    {
        return null;
    }

    public String getCpClassNameString(int cpIndex)
    {
        return null;
    }

    public String getCpNameString(int cpIndex)
    {
        return null;
    }

    public String getCpTypeString(int cpIndex)
    {
        return null;
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
        return superClass;
    }


    public ClassFile getInterface(int index)
    {
        return interfaceClasses[index];
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

        if (interfaceClasses != null)
        {
            for (int i = 0; i < interfaceClasses.length; i++)
            {
                ClassFile interfaceClass = interfaceClasses[i];
                if (interfaceClass != null &&
                    interfaceClass.implements_(classFile))
                {
                    return true;
                }
            }
        }

        return false;
    }


    public FieldInfo findField(String name, String descriptor)
    {
        for (int i = 0; i < fields.length; i++)
        {
            FieldInfo field = fields[i];
            if (field != null &&
                (name       == null || field.getName(this).equals(name)) &&
                (descriptor == null || field.getDescriptor(this).equals(descriptor)))
            {
                return field;
            }
        }

        return null;
    }


    public MethodInfo findMethod(String name, String descriptor)
    {
        for (int i = 0; i < methods.length; i++)
        {
            MethodInfo method = methods[i];
            if (method != null &&
                (name       == null || method.getName(this).equals(name)) &&
                (descriptor == null || method.getDescriptor(this).equals(descriptor)))
            {
                return method;
            }
        }

        return null;
    }


    public void accept(ClassFileVisitor classFileVisitor)
    {
        classFileVisitor.visitLibraryClassFile(this);
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
            if (interfaceClasses != null)
            {
                for (int i = 0; i < interfaceClasses.length; i++)
                {
                    ClassFile interfaceClass = interfaceClasses[i];
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
        // This class doesn't keep references to its constant pool entries.
    }


    public void constantPoolEntryAccept(int index, CpInfoVisitor cpInfoVisitor)
    {
        // This class doesn't keep references to its constant pool entries.
    }


    public void fieldsAccept(MemberInfoVisitor memberInfoVisitor)
    {
        for (int i = 0; i < fields.length; i++)
        {
            if (fields[i] != null)
            {
                fields[i].accept(this, memberInfoVisitor);
            }
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
        for (int i = 0; i < methods.length; i++)
        {
            if (methods[i] != null)
            {
                methods[i].accept(this, memberInfoVisitor);
            }
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
        // This class doesn't keep references to its attributes.
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
