/* $Id: ShortestUsageMarker.java,v 1.3.2.2 2007/01/18 21:31:53 eric Exp $
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
import proguard.classfile.attribute.*;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.*;


/**
 * This ClassFileVisitor and MemberInfoVisitor recursively marks all classes
 * and class elements that are being used. For each element, it finds the
 * shortest chain of dependencies.
 *
 * @see ClassFileShrinker
 *
 * @author Eric Lafortune
 */
public class ShortestUsageMarker extends UsageMarker
{
    private static final ShortestUsageMark INITIAL_MARK =
        new ShortestUsageMark("is kept by a directive in the configuration.\n\n");


    // A field acting as a parameter to the visitor methods.
    private ShortestUsageMark currentUsageMark = INITIAL_MARK;

    // A utility object to check for recursive causes.
    private MyRecursiveCauseChecker recursiveCauseChecker = new MyRecursiveCauseChecker();


    // Overriding implementations for UsageMarker.

    protected void markProgramClassBody(ProgramClassFile programClassFile)
    {
        ShortestUsageMark previousUsageMark = currentUsageMark;

        currentUsageMark = new ShortestUsageMark(getShortestUsageMark(programClassFile),
                                                 "is extended by ",
                                                 10000,
                                                 programClassFile);

        super.markProgramClassBody(programClassFile);

        currentUsageMark = previousUsageMark;
    }


    protected void markProgramMethodBody(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
    {
        ShortestUsageMark previousUsageMark = currentUsageMark;

        currentUsageMark = new ShortestUsageMark(getShortestUsageMark(programMethodInfo),
                                                 "is invoked by  ",
                                                 1,
                                                 programClassFile,
                                                 programMethodInfo);

        super.markProgramMethodBody(programClassFile, programMethodInfo);

        currentUsageMark = previousUsageMark;
    }


    protected void markMethodHierarchy(ClassFile classFile, MethodInfo methodInfo)
    {
        ShortestUsageMark previousUsageMark = currentUsageMark;

        currentUsageMark = new ShortestUsageMark(getShortestUsageMark(methodInfo),
                                                 "implements     ",
                                                 100,
                                                 classFile,
                                                 methodInfo);

        super.markMethodHierarchy(classFile, methodInfo);

        currentUsageMark = previousUsageMark;
    }


    // Small utility methods.

    protected void markAsUsed(VisitorAccepter visitorAccepter)
    {
        Object visitorInfo = visitorAccepter.getVisitorInfo();

        ShortestUsageMark shortestUsageMark =
            visitorInfo != null                           &&
            visitorInfo instanceof ShortestUsageMark      &&
            !((ShortestUsageMark)visitorInfo).isCertain() &&
            !currentUsageMark.isShorter((ShortestUsageMark)visitorInfo) ?
                new ShortestUsageMark((ShortestUsageMark)visitorInfo, true):
                currentUsageMark;

        visitorAccepter.setVisitorInfo(shortestUsageMark);
    }


    protected boolean shouldBeMarkedAsUsed(VisitorAccepter visitorAccepter)
    {
        Object visitorInfo = visitorAccepter.getVisitorInfo();

        return //!(visitorAccepter instanceof ClassFile &&
               //  isCausedBy(currentUsageMark, (ClassFile)visitorAccepter)) &&
               (visitorInfo == null                           ||
               !(visitorInfo instanceof ShortestUsageMark)   ||
               !((ShortestUsageMark)visitorInfo).isCertain() ||
               currentUsageMark.isShorter((ShortestUsageMark)visitorInfo));
    }


    protected boolean isUsed(VisitorAccepter visitorAccepter)
    {
        Object visitorInfo = visitorAccepter.getVisitorInfo();

        return visitorInfo != null                      &&
               visitorInfo instanceof ShortestUsageMark &&
               ((ShortestUsageMark)visitorInfo).isCertain();
    }


    protected void markAsPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        visitorAccepter.setVisitorInfo(new ShortestUsageMark(currentUsageMark, false));
    }


    protected boolean shouldBeMarkedAsPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        Object visitorInfo = visitorAccepter.getVisitorInfo();

        return visitorInfo == null                         ||
               !(visitorInfo instanceof ShortestUsageMark) ||
               (!((ShortestUsageMark)visitorInfo).isCertain() &&
                currentUsageMark.isShorter((ShortestUsageMark)visitorInfo));
    }


    protected boolean isPossiblyUsed(VisitorAccepter visitorAccepter)
    {
        Object visitorInfo = visitorAccepter.getVisitorInfo();

        return visitorInfo != null                      &&
               visitorInfo instanceof ShortestUsageMark &&
               !((ShortestUsageMark)visitorInfo).isCertain();
    }


    protected ShortestUsageMark getShortestUsageMark(VisitorAccepter visitorAccepter)
    {
        Object visitorInfo = visitorAccepter.getVisitorInfo();

        return (ShortestUsageMark)visitorInfo;
    }


    // Small utility methods.

    private boolean isCausedBy(ShortestUsageMark shortestUsageMark,
                               ClassFile         classFile)
    {
        return recursiveCauseChecker.check(shortestUsageMark, classFile);
    }


    private class MyRecursiveCauseChecker implements ClassFileVisitor, MemberInfoVisitor
    {
        private ClassFile checkClassFile;
        private boolean   isRecursing;


        public boolean check(ShortestUsageMark shortestUsageMark,
                             ClassFile         classFile)
        {
            checkClassFile = classFile;
            isRecursing    = false;

            shortestUsageMark.acceptClassFileVisitor(this);
            shortestUsageMark.acceptMethodInfoVisitor(this);

            return isRecursing;
        }

        // Implementations for ClassFileVisitor.

        public void visitProgramClassFile(ProgramClassFile programClassFile)
        {
            checkCause(programClassFile);
        }


        public void visitLibraryClassFile(LibraryClassFile libraryClassFile)
        {
            checkCause(libraryClassFile);
        }


        // Implementations for MemberInfoVisitor.

        public void visitProgramFieldInfo(ProgramClassFile programClassFile, ProgramFieldInfo programFieldInfo)
        {
            checkCause(programFieldInfo);
        }


        public void visitProgramMethodInfo(ProgramClassFile programClassFile, ProgramMethodInfo programMethodInfo)
        {
            checkCause(programMethodInfo);
        }


        public void visitLibraryFieldInfo(LibraryClassFile libraryClassFile, LibraryFieldInfo libraryFieldInfo)
        {
             checkCause(libraryFieldInfo);
       }


        public void visitLibraryMethodInfo(LibraryClassFile libraryClassFile, LibraryMethodInfo libraryMethodInfo)
        {
            checkCause(libraryMethodInfo);
        }


        // Small utility methods.

        private void checkCause(VisitorAccepter visitorAccepter)
        {
            if (ShortestUsageMarker.this.isUsed(visitorAccepter))
            {
                ShortestUsageMark shortestUsageMark = ShortestUsageMarker.this.getShortestUsageMark(visitorAccepter);

                // Check the class of this mark, if any
                isRecursing = shortestUsageMark.isCausedBy(checkClassFile);

                // Check the causing class or method, if still necessary.
                if (!isRecursing)
                {
                    shortestUsageMark.acceptClassFileVisitor(this);
                    shortestUsageMark.acceptMethodInfoVisitor(this);
                }
            }
        }
    }
}
