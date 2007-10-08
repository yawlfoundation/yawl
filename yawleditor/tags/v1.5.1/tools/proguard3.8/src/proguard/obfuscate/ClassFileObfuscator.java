/* $Id: ClassFileObfuscator.java,v 1.24.2.3 2007/01/18 21:31:52 eric Exp $
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
package proguard.obfuscate;

import proguard.classfile.*;
import proguard.classfile.util.*;
import proguard.classfile.visitor.*;

import java.util.*;


/**
 * This <code>ClassFileVisitor</code> comes up with obfuscated names for the
 * class files it visits, and for their class members. The actual renaming is
 * done afterward.
 *
 * @see ClassFileRenamer
 *
 * @author Eric Lafortune
 */
public class ClassFileObfuscator
  implements ClassFileVisitor
{
    private String  defaultPackageName;
    private boolean useMixedCaseClassNames;

    // Map: [package name - class name factory]
    private final Map         packageMap = new HashMap();
    private final NameFactory defaultPackageClassNameFactory;
    private final Set         namesToAvoid = new HashSet();


    /**
     * Creates a new ClassFileObfuscator.
     * @param programClassPool       the class pool in which class names have
     *                               to be unique.
     * @param defaultPackageName     the package in which all classes that don't
     *                               have fixed names will be put, or
     *                               <code>null</code>, if all classes can
     *                               remain in their original packages.
     * @param useMixedCaseClassNames specifies whether to use mixed case
     *                               class names.
     */
    public ClassFileObfuscator(ClassPool programClassPool,
                               String    defaultPackageName,
                               boolean   useMixedCaseClassNames)
    {
        this.defaultPackageName             = defaultPackageName;
        this.useMixedCaseClassNames         = useMixedCaseClassNames;
        this.defaultPackageClassNameFactory = new SimpleNameFactory(useMixedCaseClassNames);

        // Collect all names that have been taken already.
        programClassPool.classFilesAccept(new ClassFileVisitor()
        {
            public void visitProgramClassFile(ProgramClassFile programClassFile)
            {
                String newClassName = newClassName(programClassFile);
                if (newClassName != null)
                {
                    namesToAvoid.add(newClassName);
                }
            }

            public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
            {
            }
        });
    }


    // Implementations for ClassFileVisitor.

    public void visitProgramClassFile(ProgramClassFile programClassFile)
    {
        // Does this class file still need a new name?
        if (newClassName(programClassFile) == null)
        {
            // Figure out a new name.
            String className   = programClassFile.getName();
            String packageName = ClassUtil.internalPackageName(className);

            String newPackageName = packageName;

            // Find the right name factory for this package, or use the default.
            NameFactory packageClassNameFactory = (NameFactory)packageMap.get(packageName);
            if (packageClassNameFactory == null)
            {
                // Do we have a default package name?
                if (defaultPackageName == null)
                {
                    // We haven't seen this package before. Create a new name factory
                    // for it.
                    packageClassNameFactory = new SimpleNameFactory(useMixedCaseClassNames);
                    packageMap.put(packageName, packageClassNameFactory);
                }
                else
                {
                    // Fall back on the default package class name factory and name.
                    packageClassNameFactory = defaultPackageClassNameFactory;
                    newPackageName          = defaultPackageName;
                }
            }

            // Come up with class names until we get an original one.
            String newClassName;
            do
            {
                // Let the factory produce a class name.
                newClassName = packageClassNameFactory.nextName();

                // We may have to add a package part to the class name.
                if (newPackageName.length() > 0)
                {
                    newClassName =
                        newPackageName +
                        ClassConstants.INTERNAL_PACKAGE_SEPARATOR +
                        newClassName;
                }

            }
            while (namesToAvoid.contains(newClassName));

            setNewClassName(programClassFile, newClassName);
        }
    }


    public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
    {
    }


    // Small utility methods.

    /**
     * Assigns a new name to the given class file.
     * @param classFile the given class file.
     * @param name      the new name.
     */
    static void setNewClassName(ClassFile classFile, String name)
    {
        classFile.setVisitorInfo(name);
    }



    /**
     * Retrieves the new name of the given class file.
     * @param classFile the given class file.
     * @return the class file's new name, or <code>null</code> if it doesn't
     *         have one yet.
     */
    static String newClassName(ClassFile classFile)
    {
        Object visitorInfo = classFile.getVisitorInfo();

        return visitorInfo instanceof String ?
            (String)visitorInfo :
            null;
    }
}
