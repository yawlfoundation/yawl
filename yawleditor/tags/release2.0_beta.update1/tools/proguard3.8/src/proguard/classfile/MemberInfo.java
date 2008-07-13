/* $Id: MemberInfo.java,v 1.20.2.3 2007/01/18 21:31:51 eric Exp $
 *
 * ProGuard -- shrinking, optimization, and obfuscation of Java class files.
 *
 * Copyright (c) 1999      Mark Welsh (markw@retrologic.com)
 * Copyright (c) 2002-2007 Eric Lafortune (eric@graphics.cornell.edu)
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
 * Representation of a field or method from a program class file.
 *
 * @author Mark Welsh
 * @author Eric Lafortune
 */
public interface MemberInfo extends VisitorAccepter
{
    /**
     * Returns access flags.
     */
    public int getAccessFlags();

    /**
     * Returns method/field string name.
     */
    public String getName(ClassFile classFile);

    /**
     * Returns descriptor string.
     */
    public String getDescriptor(ClassFile classFile);

    /**
     * Accepts the given class file visitor.
     */
    public void accept(ClassFile classFile, MemberInfoVisitor memberInfoVisitor);

    /**
     * Lets the ClassFile objects referenced in the descriptor string
     * accept the given visitor.
     */
    public void referencedClassesAccept(ClassFileVisitor classFileVisitor);
}
