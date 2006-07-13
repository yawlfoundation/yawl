/* $Id: ClassNewInstanceChecker.java,v 1.8.2.1 2006/01/16 22:57:55 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
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
package proguard.classfile.util;

import proguard.classfile.*;
import proguard.classfile.visitor.*;


/**
 * This class can check whether method references point to
 * <code>Class.newInstance</code>.
 *
 * @author Eric Lafortune
 */
class      ClassNewInstanceChecker
implements CpInfoVisitor
{
    // This field acts as a return variable for the CpInfoVisitor.
    private boolean isClassNewInstanceInvocation;


    /**
     * Creates a new ClassNewInstanceChecker.
     */
    public ClassNewInstanceChecker()
    {
    }


    /**
     * Checks whether the specified method reference points to
     * <code>Class.newInstance</code>.
     * @param classFile            the class file in which the method reference
     *                             is located.
     * @param methodrefCpInfoIndex the index of the method reference in the
     *                             constant pool.
     * @return a boolean that indicates whether the reference points to the
     *         method.
     */
    public boolean check(ClassFile classFile, int methodrefCpInfoIndex)
    {
        isClassNewInstanceInvocation = false;

        classFile.constantPoolEntryAccept(methodrefCpInfoIndex, this);

        return isClassNewInstanceInvocation;
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo) {}
    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}

    /**
     * Checks whether the given method reference points to
     * <code>Class.newInstance</code>.
     */
    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        String className  = methodrefCpInfo.getClassName(classFile);
        String methodName = methodrefCpInfo.getName(classFile);
        String methodType = methodrefCpInfo.getType(classFile);

        // Is it a reference to "Object Class.newInstance()"?
        isClassNewInstanceInvocation =
            className .equals(ClassConstants.INTERNAL_CLASS_NAME_JAVA_LANG_CLASS) &&
            methodName.equals(ClassConstants.INTERNAL_METHOD_NAME_NEW_INSTANCE)   &&
            methodType.equals(ClassConstants.INTERNAL_METHOD_TYPE_NEW_INSTANCE);
    }
}
