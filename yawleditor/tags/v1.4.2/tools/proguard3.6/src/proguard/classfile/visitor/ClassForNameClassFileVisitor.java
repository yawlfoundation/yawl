/* $Id: ClassForNameClassFileVisitor.java,v 1.1.2.1 2006/02/13 00:19:28 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java bytecode.
 *
 * Copyright (c) 2002-2006 Eric Lafortune (eric@graphics.cornell.edu)
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
package proguard.classfile.visitor;

import proguard.classfile.*;
import proguard.classfile.instruction.*;
import proguard.classfile.util.*;


/**
 * This CpInfoVisitor lets a given <code>ClassFileVisitor</code> visit all
 * constant classes involved in any <code>Class.forName</code> constructs
 * that it visits.
 *
 * @see DotClassClassFileVisitor
 *
 * @author Eric Lafortune
 */
public class ClassForNameClassFileVisitor implements CpInfoVisitor
{
    private ClassFileVisitor classFileVisitor;


    /**
     * Creates a new ClassForNameClassFileVisitor
     * @param classFileVisitor the <code>ClassFileVisitor</code> to which
     *                         visits will be delegated.
     */
    public ClassForNameClassFileVisitor(ClassFileVisitor classFileVisitor)
    {
        this.classFileVisitor = classFileVisitor;
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo) {}
    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo) {}
    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo)
    {
        // Visit the referenced class from the ClassFile.forName construct,
        // if any.
        stringCpInfo.referencedClassAccept(classFileVisitor);
    }
}
