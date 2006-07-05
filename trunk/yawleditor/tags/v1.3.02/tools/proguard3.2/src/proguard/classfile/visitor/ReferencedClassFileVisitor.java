/* $Id: ReferencedClassFileVisitor.java,v 1.4 2004/08/15 12:39:30 eric Exp $
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
package proguard.classfile.visitor;

import proguard.classfile.*;


/**
 * This CpInfoVisitor lets a given ClassFileVisitor visit all the referenced
 * class files of the constant pool entries that it visits.
 *
 * @author Eric Lafortune
 */
public class ReferencedClassFileVisitor
  implements CpInfoVisitor
{
    private ClassFileVisitor classFileVisitor;


    public ReferencedClassFileVisitor(ClassFileVisitor classFileVisitor)
    {
        this.classFileVisitor = classFileVisitor;
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}


    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo)
    {
        visitReferencedClassFile(stringCpInfo.referencedClassFile);
    }


    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo)
    {
        visitReferencedClassFile(fieldrefCpInfo.referencedClassFile);
    }


    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo)
    {
        visitReferencedClassFile(interfaceMethodrefCpInfo.referencedClassFile);
    }


    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo)
    {
        visitReferencedClassFile(methodrefCpInfo.referencedClassFile);
    }


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        visitReferencedClassFile(classCpInfo.referencedClassFile);
    }


    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo)
    {
        visitReferencedClassFiles(nameAndTypeCpInfo.referencedClassFiles);
    }


    // Small utility methods.

    private void visitReferencedClassFiles(ClassFile[] referencedClassFiles)
    {
        if (referencedClassFiles != null)
        {
            for (int index = 0; index < referencedClassFiles.length; index++)
            {
                visitReferencedClassFile(referencedClassFiles[index]);
            }
        }
    }


    private void visitReferencedClassFile(ClassFile referencedClassFile)
    {
        if (referencedClassFile != null)
        {
            referencedClassFile.accept(classFileVisitor);
        }
    }
}
