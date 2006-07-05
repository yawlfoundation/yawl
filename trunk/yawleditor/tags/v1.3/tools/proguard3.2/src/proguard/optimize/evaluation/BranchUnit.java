/* $Id: BranchUnit.java,v 1.3 2004/10/10 20:56:58 eric Exp $
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
package proguard.optimize.evaluation;

import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.optimize.evaluation.value.*;

/**
 * This InstructionVisitor evaluates the instructions that it visits.
 *
 * @author Eric Lafortune
 */
public interface BranchUnit
{
    /**
     * Sets the new instruction offset.
     */
    public void branch(ClassFile    classFile,
                       CodeAttrInfo codeAttrInfo,
                       int          offset,
                       int          branchTarget);


    /**
     * Sets the new instruction offset, depending on the certainty of the
     * conditional branch.
     */
    public void branchConditionally(ClassFile    classFile,
                                    CodeAttrInfo codeAttrInfo,
                                    int          offset,
                                    int          branchTarget,
                                    int          conditional);


    /**
     * Returns from the method with the given value.
     */
    public void returnFromMethod(Value returnValue);


    /**
     * Handles the throwing of an exception.
     */
    public void throwException();
}
