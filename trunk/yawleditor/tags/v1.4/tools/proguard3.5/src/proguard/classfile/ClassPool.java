/* $Id: ClassPool.java,v 1.18.2.1 2006/01/16 22:57:55 eric Exp $
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

import proguard.classfile.util.*;
import proguard.classfile.visitor.*;

import java.util.*;

/**
 * This is a set of representations of class files. They can be enumerated or
 * retrieved by name. They can also be accessed by means of class file visitors.
 *
 * @author Eric Lafortune
 */
public class ClassPool
{
    private Map classFiles = new HashMap();


    /**
     * Adds the given ClassFile to the class pool. If a class file of the same
     * name is already present, it is left unchanged and the old class file is
     * returned.
     */
    public ClassFile addClass(ClassFile classFile)
    {
        String name = classFile.getName();

        ClassFile previousClassFile = (ClassFile)classFiles.put(name, classFile);
        if (previousClassFile != null)
        {
            // We'll put the original one back.
            classFiles.put(name, previousClassFile);
        }

        return previousClassFile;
    }


    /**
     * Removes the given ClassFile from the class pool.
     */
    public void removeClass(ClassFile classFile)
    {
        classFiles.remove(classFile.getName());
    }


    /**
     * Returns a ClassFile from the class pool based on its name. Returns
     * <code>null</code> if the class with the given name is not in the class
     * pool. Returns the base class if the class name is an array type, and the
     * <code>java.lang.Object</code> class if that base class is a primitive type.
     */
    public ClassFile getClass(String className)
    {
        return (ClassFile)classFiles.get(ClassUtil.internalClassNameFromType(className));
    }


    /**
     * Returns an Iterator of all ClassFile objects in the class pool.
     */
    public Iterator elements()
    {
        return classFiles.values().iterator();
    }


    /**
     * Returns the number of class files in the class pool.
     */
    public int size()
    {
        return classFiles.size();
    }


    /**
     * Applies the given ClassPoolVisitor to the class pool.
     */
    public void accept(ClassPoolVisitor classPoolVisitor)
    {
        classPoolVisitor.visitClassPool(this);
    }


    /**
     * Applies the given ClassFileVisitor to all classes in the class pool,
     * in random order.
     */
    public void classFilesAccept(ClassFileVisitor classFileVisitor)
    {
        Iterator iterator = elements();
        while (iterator.hasNext())
        {
            ClassFile classFile = (ClassFile)iterator.next();
try{
            classFile.accept(classFileVisitor);
}catch (RuntimeException ex) {
    System.out.println("Runtime exception while processing class file ["+classFile.getName()+"]");
    throw ex;
}catch (Error er) {
    System.out.println("Runtime error while processing class file ["+classFile.getName()+"]");
    throw er;}
        }
    }


    /**
     * Applies the given ClassFileVisitor to all classes in the class pool,
     * in sorted order.
     */
    public void classFilesAcceptAlphabetically(ClassFileVisitor classFileVisitor)
    {
        TreeMap sortedClassFiles = new TreeMap(classFiles);
        Iterator iterator = sortedClassFiles.values().iterator();
        while (iterator.hasNext())
        {
            ClassFile classFile = (ClassFile)iterator.next();
            classFile.accept(classFileVisitor);
        }
    }


    /**
     * Applies the given ClassFileVisitor to the class with the given name,
     * if it is present in the class pool.
     */
    public void classFileAccept(ClassFileVisitor classFileVisitor, String className)
    {
        ClassFile classFile = getClass(className);
        if (classFile != null)
        {
            classFile.accept(classFileVisitor);
        }
    }
}
