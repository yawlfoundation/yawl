/* $Id: ClassFile.java,v 1.27.2.1 2006/01/16 22:57:55 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
 * Copyright (c) 2002-2006 Eric Lafortune (eric@graphics.cornell.edu)
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

import proguard.classfile.visitor.*;
import proguard.classfile.attribute.*;


/**
 * This interface provides access to the data in a Java class file (*.class).
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public interface ClassFile extends VisitorAccepter
{
    /**
     * Returns the access flags of this class.
     * @see ClassConstants
     */
    public int getAccessFlags();

    /**
     * Returns the full internal name of this class.
     */
    public String getName();

    /**
     * Returns the full internal name of the super class of this class, or
     * null if this class represents java.lang.Object.
     */
    public String getSuperName();

    /**
     * Returns the number of interfaces that this class implements.
     */
    public int getInterfaceCount();

    /**
     * Returns the full internal name of the interface at the given index of
     * this class.
     */
    public String getInterfaceName(int index);

    /**
     * Returns the tag value of the CpEntry at the specified index.
     */
    public int getCpTag(int cpIndex);

    /**
     * Returns the String value of the Utf8CpEntry at the specified index.
     */
    public String getCpString(int cpIndex);

    /**
     * Returns the class name of ClassCpEntry at the specified index.
     */
    public String getCpClassNameString(int cpIndex);

    /**
     * Returns the name of the NameAndTypeCpEntry at the specified index.
     */
    public String getCpNameString(int cpIndex);

    /**
     * Returns the type of the NameAndTypeCpEntry at the specified index.
     */
    public String getCpTypeString(int cpIndex);


    // Methods pertaining to related class files.

    /**
     * Notifies this ClassFile that it is being subclassed by another class.
     */
    public void addSubClass(ClassFile classFile);

    /**
     * Returns the super class of this class.
     */
    public ClassFile getSuperClass();

    /**
     * Returns the interface at the given index.
     */
    public ClassFile getInterface(int index);

    /**
     * Returns whether this class extends the given class.
     * A class is always considered to extend itself.
     * Interfaces are considered to only extend the root Object class.
     */
    public boolean extends_(ClassFile classFile);

    /**
     * Returns whether this class implements the given class.
     * A class is always considered to implement itself.
     * Interfaces are considered to implement all their superinterfaces.
     */
    public boolean implements_(ClassFile classFile);


    // Methods for getting specific class members.

    /**
     * Returns the field with the given name and descriptor.
     */
    FieldInfo findField(String name, String descriptor);

    /**
     * Returns the method with the given name and descriptor.
     */
    MethodInfo findMethod(String name, String descriptor);


    // Methods for accepting various types of visitors.

    /**
     * Accepts the given class file visitor.
     */
    public void accept(ClassFileVisitor classFileVisitor);

    /**
     * Accepts the given class file visitor in the class hierarchy.
     * @param visitThisClass   specifies whether to visit this class.
     * @param visitSuperClass  specifies whether to visit the super classes.
     * @param visitInterfaces  specifies whether to visit the interfaces.
     * @param visitSubclasses  specifies whether to visit the subclasses.
     * @param classFileVisitor the <code>ClassFileVisitor</code> that will
     *                         visit the class hierarchy.
     */
    public void hierarchyAccept(boolean          visitThisClass,
                                boolean          visitSuperClass,
                                boolean          visitInterfaces,
                                boolean          visitSubclasses,
                                ClassFileVisitor classFileVisitor);

    /**
     * Lets the given constant pool entry visitor visit all constant pool entries
     * of this class.
     */
    public void constantPoolEntriesAccept(CpInfoVisitor cpInfoVisitor);

    /**
     * Lets the given constant pool entry visitor visit the constant pool entry
     * at the specified index.
     */
    public void constantPoolEntryAccept(int index, CpInfoVisitor cpInfoVisitor);

    /**
     * Lets the given member info visitor visit all fields of this class.
     */
    public void fieldsAccept(MemberInfoVisitor memberInfoVisitor);

    /**
     * Lets the given member info visitor visit the specified field.
     */
    public void fieldAccept(String name, String descriptor, MemberInfoVisitor memberInfoVisitor);

    /**
     * Lets the given member info visitor visit all methods of this class.
     */
    public void methodsAccept(MemberInfoVisitor memberInfoVisitor);

    /**
     * Lets the given member info visitor visit the specified method.
     */
    public void methodAccept(String name, String descriptor, MemberInfoVisitor memberInfoVisitor);

    /**
     * Returns whether the given method may possibly have implementing or
     * overriding methods down the class hierarchy. This can only be true
     * if the class is not final, and the method is not private, static, or
     * final, or a constructor.
     * @param methodInfo the method that may have implementations.
     * @return whether it may have implementations.
     */
    public boolean mayHaveImplementations(MethodInfo methodInfo);

    /**
     * Lets the given member info visitor visit all concrete implementations of
     * the specified method in the class hierarchy.
     * @param methodInfo        the method that may have concrete implementations.
     * @param visitThisMethod   specifies whether to visit the method in
     *                          this class.
     * @param memberInfoVisitor the <code>MemberInfoVisitor</code> that will
     *                          visit the method hierarchy.
     */
    public void methodImplementationsAccept(MethodInfo        methodInfo,
                                            boolean           visitThisMethod,
                                            MemberInfoVisitor memberInfoVisitor);

    /**
     * Lets the given member info visitor visit all concrete implementations of
     * the specified method in the class hierarchy.
     * @param name              the method name.
     * @param type              the method descriptor.
     * @param visitThisMethod   specifies whether to visit the method in
     *                          this class.
     * @param memberInfoVisitor the <code>MemberInfoVisitor</code> that will
     *                          visit the method hierarchy.
     */
    public void methodImplementationsAccept(String            name,
                                            String            type,
                                            boolean           visitThisMethod,
                                            MemberInfoVisitor memberInfoVisitor);

    /**
     * Lets the given member info visitor visit all concrete implementations of
     * the specified method in the class hierarchy.
     * @param name                   the method name.
     * @param descriptor             the method descriptor.
     * @param visitThisMethod        specifies whether to visit the method in
     *                               this class.
     * @param visitSpecialMethods    specifies whether to visit the special
     *                               initializer methods.
     * @param visitSuperMethods      specifies whether to visit the method in
     *                               the super classes.
     * @param visitOverridingMethods specifies whether to visit the method in
     *                               the subclasses.
     * @param visitSpecialMethods    specifies whether to visit special methods.
     * @param memberInfoVisitor      the <code>MemberInfoVisitor</code> that
     *                               will visit the method hierarchy.
     */
    public void methodImplementationsAccept(String            name,
                                            String            descriptor,
                                            boolean           visitThisMethod,
                                            boolean           visitSpecialMethods,
                                            boolean           visitSuperMethods,
                                            boolean           visitOverridingMethods,
                                            MemberInfoVisitor memberInfoVisitor);
    /**
     * Lets the given member info visitor visit all concrete implementations of
     * the specified method in the class hierarchy.
     * @param name                   the method name.
     * @param descriptor             the method descriptor.
     * @param methodInfo             the method itself, if present.
     * @param visitThisMethod        specifies whether to visit the method in
     *                               this class.
     * @param visitSpecialMethods    specifies whether to visit the method in
     *                               the interfaces.
     * @param visitSuperMethods      specifies whether to visit the method in
     *                               the super classes.
     * @param visitOverridingMethods specifies whether to visit the method in
     *                               the subclasses.
     * @param visitSpecialMethods    specifies whether to visit special methods.
     * @param memberInfoVisitor      the <code>MemberInfoVisitor</code> that
     *                               will visit the method hierarchy.
     */
    public void methodImplementationsAccept(String            name,
                                            String            descriptor,
                                            MethodInfo        methodInfo,
                                            boolean           visitThisMethod,
                                            boolean           visitSuperMethods,
                                            boolean           visitOverridingMethods,
                                            boolean           visitSpecialMethods,
                                            MemberInfoVisitor memberInfoVisitor);

    /**
     * Lets the given attribute info visitor visit all attributes of this class.
     */
    public void attributesAccept(AttrInfoVisitor attrInfoVisitor);
}
