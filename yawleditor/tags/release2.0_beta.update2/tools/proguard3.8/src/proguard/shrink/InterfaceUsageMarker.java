/* $Id: InterfaceUsageMarker.java,v 1.14.2.2 2007/01/18 21:31:53 eric Exp $
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
package proguard.shrink;

import proguard.classfile.*;
import proguard.classfile.visitor.*;


/**
 * This ClassFileVisitor recursively marks all interface
 * classes that are being used in the visited class.
 *
 * @see UsageMarker
 *
 * @author Eric Lafortune
 */
public class InterfaceUsageMarker
  implements ClassFileVisitor,
             CpInfoVisitor
{
    private UsageMarker usageMarker;

    // A field acting as a return parameter for several methods.
    private boolean used;


    /**
     * Creates a new InterfaceUsageMarker.
     * @param usageMarker the usage marker that is used to mark the classes
     *                    and class members.
     */
    public InterfaceUsageMarker(UsageMarker usageMarker)
    {
        this.usageMarker = usageMarker;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        boolean classUsed         = usageMarker.isUsed(programClassFile);
        boolean classPossiblyUsed = usageMarker.isPossiblyUsed(programClassFile);

        if (classUsed || classPossiblyUsed)
        {
            // Mark the references to interfaces that are being used.
            for (int i = 0; i < programClassFile.u2interfacesCount; i++)
            {
                // Check if the interface is used. Mark the constant pool entry
                // if so.
                markCpEntry(programClassFile, programClassFile.u2interfaces[i]);
                classUsed |= used;
            }

            // Is this an interface with a preliminary mark?
            if (classPossiblyUsed)
            {
                // Should it be included now?
                if (classUsed)
                {
                    // At least one if this interface's interfaces is being used.
                    // Mark this interface as well.
                    usageMarker.markAsUsed(programClassFile);

                    // Mark this interface's name.
                    markCpEntry(programClassFile, programClassFile.u2thisClass);

                    // Mark the superclass (java/lang/Object).
                    if (programClassFile.u2superClass != 0)
                    {
                        markCpEntry(programClassFile, programClassFile.u2superClass);
                    }
                }
                else
                {
                    // Unmark this interface, so we don't bother looking at it again.
                    usageMarker.markAsUnused(programClassFile);
                }
            }
        }

        // The return value.
        used = classUsed;
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        // The return value.
        used = true;
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo) {}
    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo) {}
    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        boolean classUsed = usageMarker.isUsed(classCpInfo);

        if (!classUsed)
        {
            // The ClassCpInfo isn't marked as being used yet. But maybe it should
            // be included as an interface, so check the actual class.
            classCpInfo.referencedClassAccept(this);
            classUsed = used;

            if (classUsed)
            {
                // The class is being used. Mark the ClassCpInfo as being used
                // as well.
                usageMarker.markAsUsed(classCpInfo);

                markCpEntry(classFile, classCpInfo.u2nameIndex);
            }
        }

        // The return value.
        used = classUsed;
    }


    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo)
    {
        if (!usageMarker.isUsed(utf8CpInfo))
        {
            usageMarker.markAsUsed(utf8CpInfo);
        }
    }


    // Small utility methods.

    /**
     * Marks the given constant pool entry of the given class. This includes
     * visiting any referenced objects.
     */
    private void markCpEntry(ClassFile classFile, int index)
    {
         classFile.constantPoolEntryAccept(index, this);
    }
}
