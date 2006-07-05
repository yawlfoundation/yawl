/* $Id: RuntimeInvisibleAnnotationsAttrInfo.java,v 1.1 2004/10/10 21:10:04 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 2002-2004 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
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
package proguard.classfile.attribute.annotation;

import proguard.classfile.ClassFile;
import proguard.classfile.attribute.AttrInfoVisitor;

/**
 * Representation of a runtime invisible annotations attribute.
 *
 * @author Eric Lafortune
 */
public class RuntimeInvisibleAnnotationsAttrInfo extends RuntimeAnnotationsAttrInfo
{
    public RuntimeInvisibleAnnotationsAttrInfo()
    {
    }


    // Implementations for AttrInfo.

    public void accept(ClassFile classFile, AttrInfoVisitor attrInfoVisitor)
    {
        attrInfoVisitor.visitRuntimeInvisibleAnnotationAttrInfo(classFile, this);
    }
}
