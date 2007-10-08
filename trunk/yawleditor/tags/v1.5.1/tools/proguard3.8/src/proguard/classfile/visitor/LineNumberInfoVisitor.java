/* $Id: LineNumberInfoVisitor.java,v 1.10.2.2 2007/01/18 21:31:51 eric Exp $
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
package proguard.classfile.visitor;

import proguard.classfile.*;
import proguard.classfile.attribute.*;


/**
 * This interface specifies the methods for a visitor of
 * <code>LineNumberInfo</code> objects. Note that there is only a single
 * implementation of <code>LineNumberInfo</code>, such that this interface
 * is not strictly necessary as a visitor.
 *
 * @author Eric Lafortune
 */
public interface LineNumberInfoVisitor
{
    public void visitLineNumberInfo(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, LineNumberInfo lineNumberInfo);
}
