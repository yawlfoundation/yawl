/* $Id: InterfaceMethodrefCpInfo.java,v 1.21.2.1 2006/01/16 22:57:55 eric Exp $
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

/**
 * Representation of a 'interface method reference' entry in the ConstantPool.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public class InterfaceMethodrefCpInfo extends RefCpInfo
{
    protected InterfaceMethodrefCpInfo()
    {
    }


    /**
     * Creates a new InterfaceMethodrefCpInfo with the given name and type indices.
     * @param u2classIndex         the index of the class in the constant pool.
     * @param u2nameAndTypeIndex   the index of the name and type entry in the constant pool.
     * @param referencedClassFile  the referenced class file.
     * @param referencedMemberInfo the referenced member info.
     */
    public InterfaceMethodrefCpInfo(int        u2classIndex,
                                    int        u2nameAndTypeIndex,
                                    ClassFile  referencedClassFile,
                                    MemberInfo referencedMemberInfo)
    {
        this.u2classIndex         = u2classIndex;
        this.u2nameAndTypeIndex   = u2nameAndTypeIndex;
        this.referencedClassFile  = referencedClassFile;
        this.referencedMemberInfo = referencedMemberInfo;
    }


    // Implementations for CpInfo.

    public int getTag()
    {
        return ClassConstants.CONSTANT_InterfaceMethodref;
    }

    public void accept(ClassFile classFile, CpInfoVisitor cpInfoVisitor)
    {
        cpInfoVisitor.visitInterfaceMethodrefCpInfo(classFile, this);
    }
}
