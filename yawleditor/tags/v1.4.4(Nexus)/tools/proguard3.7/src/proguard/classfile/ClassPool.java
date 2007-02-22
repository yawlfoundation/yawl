/* $Id: ClassPool.java,v 1.18.2.6 2006/11/26 15:29:20 eric Exp $
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
     * Clears the class pool.
     */
    public void clear()
    {
        classFiles.clear();
    }


    /**
     * Adds the given ClassFile to the class pool.
     */
    public void addClass(ClassFile classFile)
    {
        classFiles.put(classFile.getName(), classFile);
    }


    /**
     * Removes the given ClassFile from the class pool.
     */
    public void removeClass(ClassFile classFile)
    {
        classFiles.remove(classFile.getName());
    }


    /**
     * Returns a Clazz from the class pool based on its name. Returns
     * <code>null</code> if the class with the given name is not in the class
     * pool. Returns the base class if the class name is an array type.
     */
    public ClassFile getClass(String className)
    {
        return (ClassFile)classFiles.get(ClassUtil.internalClassNameFromClassType(className));
    }


    /**
     * Returns an Iterator of all class file names in the class pool.
     */
    public Iterator classNames()
    {
        return classFiles.keySet().iterator();
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
        Iterator iterator = classFiles.values().iterator();
        while (iterator.hasNext())
        {
            ClassFile classFile = (ClassFile)iterator.next();
            classFile.accept(classFileVisitor);
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
