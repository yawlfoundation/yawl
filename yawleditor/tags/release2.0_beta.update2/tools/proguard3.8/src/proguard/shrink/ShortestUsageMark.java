/* $Id: ShortestUsageMark.java,v 1.4.2.2 2007/01/18 21:31:53 eric Exp $
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
 * This class can be used as a mark when keeping classes, class members, and
 * other elements. It can be certain or preliminary. It also contains additional
 * information about the reasons why an element is being kept.
 *
 * @see ClassFileShrinker
 *
 * @author Eric Lafortune
 */
class ShortestUsageMark
{
    private boolean    certain;
    private String     reason;
    private int        depth;
    private ClassFile  classFile;
    private MethodInfo methodInfo;


    /**
     * Creates a new certain ShortestUsageMark.
     * @param reason the reason for this mark.
     */
    public ShortestUsageMark(String reason)
    {
        this.certain = true;
        this.reason  = reason;
        this.depth   = 0;
    }


    /**
     * Creates a new certain ShortestUsageMark.
     * @param previousUsageMark the previous mark to which this one is linked.
     * @param reason            the reason for this mark.
     * @param classFile         the class causing this mark.
     */
    public ShortestUsageMark(ShortestUsageMark previousUsageMark,
                             String            reason,
                             int               cost,
                             ClassFile         classFile)
    {
        this(previousUsageMark, reason, cost, classFile, null);
    }


    /**
     * Creates a new certain ShortestUsageMark.
     * @param previousUsageMark the previous mark to which this one is linked.
     * @param reason            the reason for this mark.
     * @param classFile         the class causing this mark.
     * @param methodInfo        the method in the above class causing this mark.
     * @param cost              the added cost of following this path.
     */
    public ShortestUsageMark(ShortestUsageMark previousUsageMark,
                             String            reason,
                             int               cost,
                             ClassFile         classFile,
                             MethodInfo        methodInfo)
    {
        this.certain    = true;
        this.reason     = reason;
        this.depth      = previousUsageMark.depth + cost;
        this.classFile  = classFile;
        this.methodInfo = methodInfo;
    }


    /**
     * Creates a new ShortestUsageMark, based on another mark.
     * @param otherUsageMark the other mark, whose properties will be copied.
     * @param certain        specifies whether this is a certain mark.
     */
    public ShortestUsageMark(ShortestUsageMark otherUsageMark,
                             boolean           certain)
    {
        this.certain    = certain;
        this.reason     = otherUsageMark.reason;
        this.depth      = otherUsageMark.depth;
        this.classFile  = otherUsageMark.classFile;
        this.methodInfo = otherUsageMark.methodInfo;
    }


    /**
     * Returns whether this is a certain mark.
     */
    public boolean isCertain()
    {
        return certain;
    }


    /**
     * Returns the reason for this mark.
     */
    public String getReason()
    {
        return reason;
    }


    /**
     * Returns whether this mark has a shorter chain of reasons than the
     * given mark.
     */
    public boolean isShorter(ShortestUsageMark otherUsageMark)
    {
        return this.depth < otherUsageMark.depth;
    }


    /**
     * Returns whether this is mark is caused by the given class file.
     */
    public boolean isCausedBy(ClassFile classFile)
    {
        return classFile.equals(this.classFile);
    }


    /**
     * Applies the given class file visitor to this mark's class, if any,
     * and if this mark doesn't have a method.
     */
    public void acceptClassFileVisitor(ClassFileVisitor classFileVisitor)
    {
        if (classFile  != null &&
            methodInfo == null)
        {
            classFile.accept(classFileVisitor);
        }
    }


    /**
     * Applies the given class file visitor to this mark's method, if any.
     */
    public void acceptMethodInfoVisitor(MemberInfoVisitor memberInfoVisitor)
    {
        if (classFile  != null &&
            methodInfo != null)
        {
            methodInfo.accept(classFile, memberInfoVisitor);
        }
    }


    // Implementations for Object.

    public String toString()
    {
        return "certain=" + certain + ", depth="+depth+": " +
               reason +
               (classFile  != null ? classFile.getName() : "(none)") + ": " +
               (methodInfo != null ? methodInfo.getName(classFile) : "(none)");
    }
}
