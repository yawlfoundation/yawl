/* $Id: ClassFileHierarchyInitializer.java,v 1.12.2.2 2006/11/25 16:56:11 eric Exp $
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
 * This ClassFileVisitor initializes the class hierarchy of all class files that
 * it visits.
 * <p>
 * Visited class files are added to the subclass list of their superclasses and
 * interfaces. These subclass lists make it more convenient to travel down the
 * class hierarchy.
 * <p>
 * Visited library class files get direct references to their superclasses and
 * interfaces, replacing the superclass names and interface names. The direct
 * references are equivalent to the names, but they are more efficient to work
 * with.
 * <p>
 * This visitor optionally prints warnings if some items can't be found.
 *
 * @author Eric Lafortune
 */
public class ClassFileHierarchyInitializer
  implements ClassFileVisitor,
             CpInfoVisitor
{
    // A visitor info flag to indicate the class file has been initialized.
    private static final Object INITIALIZED = new Object();

    private ClassPool      programClassPool;
    private ClassPool      libraryClassPool;
    private WarningPrinter warningPrinter;


    /**
     * Creates a new ClassFileReferenceInitializer that initializes the
     * hierarchy of all visited class files, optionally printing warnings if
     * some classes can't be found.
     */
    public ClassFileHierarchyInitializer(ClassPool      programClassPool,
                                         ClassPool      libraryClassPool,
                                         WarningPrinter warningPrinter)
    {
        this.programClassPool = programClassPool;
        this.libraryClassPool = libraryClassPool;
        this.warningPrinter   = warningPrinter;
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Haven't we initialized this class before?
        if (!isInitialized(programClassFile))
        {
            // Mark this class.
            markAsInitialized(programClassFile);

            if (programClassFile.u2superClass != 0)
            {
                programClassFile.constantPoolEntryAccept(programClassFile.u2superClass,
                                                         this);
            }

            // Add this class to the subclasses of its superclass.
            if (programClassFile.u2superClass != 0)
            {
                addSubclass(programClassFile,
                            programClassFile.getSuperClass(),
                            programClassFile.getSuperName());
            }

            // Add this class to the subclasses of its interfaces.
            for (int index = 0; index < programClassFile.u2interfacesCount; index++)
            {
                programClassFile.constantPoolEntryAccept(programClassFile.u2interfaces[index],
                                                         this);

                addSubclass(programClassFile,
                            programClassFile.getInterface(index),
                            programClassFile.getInterfaceName(index));
            }
        }
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
        // Haven't we initialized this class before?
        if (!isInitialized(libraryClassFile))
        {
            // Mark this class.
            markAsInitialized(libraryClassFile);

            // Have a closer look at the superclass.
            String superClassName = libraryClassFile.superClassName;
            if (superClassName != null)
            {
                // Find and initialize the super class.
                ClassFile superClass = findAndInitializeClass(superClassName);

                // Add this class to the subclasses of its superclass,
                addSubclass(libraryClassFile,
                            superClass,
                            superClassName);

                // Keep a reference to the superclass.
                libraryClassFile.superClass = superClass;
            }

            // Have a closer look at the interface classes.
            if (libraryClassFile.interfaceNames != null)
            {
                String[]    interfaceNames   = libraryClassFile.interfaceNames;
                ClassFile[] interfaceClasses = new ClassFile[interfaceNames.length];

                for (int index = 0; index < interfaceNames.length; index++)
                {
                    // Find and initialize the interface class.
                    String    interfaceName  = interfaceNames[index];
                    ClassFile interfaceClass = findAndInitializeClass(interfaceName);

                    // Add this class to the subclasses of the interface class.
                    addSubclass(libraryClassFile,
                                interfaceClass,
                                interfaceName);

                    // Keep a reference to the interface class.
                    interfaceClasses[index] = interfaceClass;
                }

                libraryClassFile.interfaceClasses = interfaceClasses;
            }

            // Discard the name Strings. From now on, we'll use the object
            // references.
            libraryClassFile.superClassName = null;
            libraryClassFile.interfaceNames = null;
        }
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
    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        classCpInfo.referencedClassFile =
            findAndInitializeClass(classCpInfo.getName(classFile));
    }


    // Small utility methods.

    /**
     * Finds and initializes a class with the given name.
     *
     * @see #findClass(String)
     */
    private ClassFile findAndInitializeClass(String name)
    {
        // Try to find the class file.
        ClassFile referencedClassFile = findClass(name);

        // Did we find the referenced class file in either class pool?
        if (referencedClassFile != null)
        {
            // Initialize it.
            referencedClassFile.accept(this);
        }

        return referencedClassFile;
    }


    /**
     * Returns the class with the given name, either for the program class pool
     * or from the library class pool, or <code>null</code> if it can't be found.
     */
    private ClassFile findClass(String name)
    {
        // First look for the class in the program class pool.
        ClassFile classFile = programClassPool.getClass(name);

        // Otherwise look for the class in the library class pool.
        if (classFile == null)
        {
            classFile = libraryClassPool.getClass(name);
        }

        return classFile;
    }


    private static void markAsInitialized(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(INITIALIZED);
    }


    private static boolean isInitialized(VisitorAccepter visitorAccepter)
    {
        return visitorAccepter.getVisitorInfo() == INITIALIZED;
    }


    private void addSubclass(ClassFile subclass,
                             ClassFile classFile,
                             String    className)
    {
        if (classFile != null)
        {
            classFile.addSubClass(subclass);
        }
        else if (warningPrinter != null)
        {
            // We didn't find the superclass or interface. Print a warning.
            warningPrinter.print("Warning: " +
                                 ClassUtil.externalClassName(subclass.getName()) +
                                 ": can't find superclass or interface " +
                                 ClassUtil.externalClassName(className));
        }
    }
}
