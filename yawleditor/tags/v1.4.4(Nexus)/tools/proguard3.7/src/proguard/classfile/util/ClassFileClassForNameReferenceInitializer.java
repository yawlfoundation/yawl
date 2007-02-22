/* $Id: ClassFileClassForNameReferenceInitializer.java,v 1.17.2.3 2006/11/25 16:56:11 eric Exp $
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
import proguard.classfile.attribute.CodeAttrInfo;
import proguard.classfile.instruction.*;
import proguard.classfile.visitor.CpInfoVisitor;
import proguard.util.ClassNameListMatcher;


/**
 * This InstructionVisitor initializes any special <code>Class.forName</code> or
 * <code>.class</code> references of all class files it visits. More specifically,
 * it fills out the references of String constant pool entries that refer to a
 * class file in the program class pool.
 * <p>
 * It optionally prints notes if on usage of
 * <code>(SomeClass)Class.forName(variable).newInstance()</code>.
 * <p>
 * The class file hierarchy must be initialized before using this visitor.
 *
 * @see ClassFileReferenceInitializer
 *
 * @author Eric Lafortune
 */
public class ClassFileClassForNameReferenceInitializer
  implements InstructionVisitor,
             CpInfoVisitor
{
    private ClassPool            programClassPool;
    private ClassPool            libraryClassPool;
    private WarningPrinter       notePrinter;
    private ClassNameListMatcher noteExceptionMatcher;

    // Fields to remember the previous StringCpInfo and MethodRefCpInfo objects
    // while visiting all instructions (to find Class.forName, class$, and
    // Class.newInstance invocations, and possible class casts afterwards).
    private int ldcStringCpIndex              = -1;
    private int invokestaticMethodRefCpIndex  = -1;
    private int invokevirtualMethodRefCpIndex = -1;

    private ClassForNameChecker     classForNameChecker     = new ClassForNameChecker();
    private ClassNewInstanceChecker classNewInstanceChecker = new ClassNewInstanceChecker();


    /**
     * Creates a new ClassFileClassForNameReferenceInitializer that optionally
     * prints notes, with optional class specifications for which never to
     * print notes.
     */
    public ClassFileClassForNameReferenceInitializer(ClassPool            programClassPool,
                                                     ClassPool            libraryClassPool,
                                                     WarningPrinter       notePrinter,
                                                     ClassNameListMatcher noteExceptionMatcher)
    {
        this.programClassPool     = programClassPool;
        this.libraryClassPool     = libraryClassPool;
        this.notePrinter          = notePrinter;
        this.noteExceptionMatcher = noteExceptionMatcher;
    }


    // Implementations for InstructionVisitor.

    public void visitSimpleInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, SimpleInstruction simpleInstruction)
    {
        // Nothing interesting; just forget any stored indices.
        clearConstantPoolIndices();
    }


    public void visitBranchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, BranchInstruction branchInstruction)
    {
        // Nothing interesting; just forget any stored indices.
        clearConstantPoolIndices();
    }


    public void visitTableSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, TableSwitchInstruction tableSwitchInstruction)
    {
        // Nothing interesting; just forget any stored indices.
        clearConstantPoolIndices();
    }


    public void visitLookUpSwitchInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, LookUpSwitchInstruction lookUpSwitchInstruction)
    {
        // Nothing interesting; just forget any stored indices.
        clearConstantPoolIndices();
    }


    public void visitVariableInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, VariableInstruction variableInstruction)
    {

        // Just ignore generic instructions and reset the constant pool indices.
        switch (variableInstruction.opcode)
        {
            case InstructionConstants.OP_ICONST_0:
            case InstructionConstants.OP_ICONST_1:
                // Still remember any loaded string; this instruction may be
                // setting up the second argument for class$(String, boolean).
                break;

            default:
                ldcStringCpIndex = -1;
                break;
        }

        invokestaticMethodRefCpIndex  = -1;
        invokevirtualMethodRefCpIndex = -1;
    }


    public void visitCpInstruction(ClassFile classFile, MethodInfo methodInfo, CodeAttrInfo codeAttrInfo, int offset, CpInstruction cpInstruction)
    {
        int currentCpIndex = cpInstruction.cpIndex;

        switch (cpInstruction.opcode)
        {
            case InstructionConstants.OP_LDC:
            case InstructionConstants.OP_LDC_W:
                // Are we loading a constant String?
                int currentCpTag = classFile.getCpTag(currentCpIndex);
                if (currentCpTag == ClassConstants.CONSTANT_String)
                {
                    // Remember it; it might be the argument of
                    // Class.forName(String), class$(String), or
                    // class$(String, boolean).
                    ldcStringCpIndex = currentCpIndex;
                }
                else
                {
                    ldcStringCpIndex = -1;
                }

                invokestaticMethodRefCpIndex  = -1;
                invokevirtualMethodRefCpIndex = -1;
                break;

            case InstructionConstants.OP_INVOKESTATIC:
                // Are we invoking a static method that might have a constant
                // String argument?
                if (ldcStringCpIndex > 0)
                {
                    // Check whether the method reference points to Class.forName.
                    if (classForNameChecker.check(classFile, currentCpIndex))
                    {
                        // Fill out the class file reference in the String.
                        classFile.constantPoolEntryAccept(ldcStringCpIndex, this);
                    }

                    // We've dealt with this invocation, so we can forget about it.
                    invokestaticMethodRefCpIndex = -1;
                }
                else
                {
                    // Remember it; it might still be a Class.forName with a
                    // variable String argument.
                    invokestaticMethodRefCpIndex = currentCpIndex;
                }

                ldcStringCpIndex              = -1;
                invokevirtualMethodRefCpIndex = -1;
                break;

            case InstructionConstants.OP_INVOKEVIRTUAL:
                // Are we invoking a virtual method right after a static method?
                if (invokestaticMethodRefCpIndex > 0)
                {
                    // Remember it; it might be Class.newInstance after a Class.forName.
                    invokevirtualMethodRefCpIndex = currentCpIndex;
                }
                else
                {
                    invokestaticMethodRefCpIndex  = -1;
                    invokevirtualMethodRefCpIndex = -1;
                }

                ldcStringCpIndex = -1;
                break;

            case InstructionConstants.OP_CHECKCAST:
                // Are we checking a cast right after a static method and a
                // virtual method?
                if (invokestaticMethodRefCpIndex  > 0 &&
                    invokevirtualMethodRefCpIndex > 0)
                {
                    // Check whether the first method reference points to Class.forName,
                    // and the second method reference to Class.newInstance.
                    if (classForNameChecker.check(classFile, invokestaticMethodRefCpIndex) &&
                        classNewInstanceChecker.check(classFile, invokevirtualMethodRefCpIndex))
                    {
                        // Note which class is being cast to.
                        classFile.constantPoolEntryAccept(currentCpIndex, this);
                    }
                }

                // We've handled the special case; forget about the indices.
                clearConstantPoolIndices();
                break;

            default:
                // Nothing interesting; just forget any stored indices.
                clearConstantPoolIndices();
                break;
        }
    }


    // Implementations for CpInfoVisitor.

    public void visitIntegerCpInfo(ClassFile classFile, IntegerCpInfo integerCpInfo) {}
    public void visitLongCpInfo(ClassFile classFile, LongCpInfo longCpInfo) {}
    public void visitFloatCpInfo(ClassFile classFile, FloatCpInfo floatCpInfo) {}
    public void visitDoubleCpInfo(ClassFile classFile, DoubleCpInfo doubleCpInfo) {}
    public void visitUtf8CpInfo(ClassFile classFile, Utf8CpInfo utf8CpInfo) {}
    public void visitFieldrefCpInfo(ClassFile classFile, FieldrefCpInfo fieldrefCpInfo) {}
    public void visitInterfaceMethodrefCpInfo(ClassFile classFile, InterfaceMethodrefCpInfo interfaceMethodrefCpInfo) {}
    public void visitMethodrefCpInfo(ClassFile classFile, MethodrefCpInfo methodrefCpInfo) {}
    public void visitNameAndTypeCpInfo(ClassFile classFile, NameAndTypeCpInfo nameAndTypeCpInfo) {}


    /**
     * Fills out the link to the referenced ClassFile.
     */
    public void visitStringCpInfo(ClassFile classFile, StringCpInfo stringCpInfo)
    {
        // Save a reference to the corresponding class file.
        String externalClassName = stringCpInfo.getString(classFile);
        String internalClassName = ClassUtil.internalClassName(externalClassName);

        stringCpInfo.referencedClassFile = findClass(internalClassName);
    }


    /**
     * Prints out a note about the class cast to this class, if applicable.
     */
    public void visitClassCpInfo(ClassFile classFile, ClassCpInfo classCpInfo)
    {
        if (notePrinter != null &&
            (noteExceptionMatcher == null ||
             !noteExceptionMatcher.matches(classCpInfo.getName(classFile))))
        {
            notePrinter.print("Note: " +
                                 ClassUtil.externalClassName(classFile.getName()) +
                                 " calls '(" +
                                 ClassUtil.externalClassName(classCpInfo.getName(classFile)) +
                                 ")Class.forName(variable).newInstance()'");
        }
    }


    // Small utility methods.

    /**
     * Clears all references to the constant pool.
     */
    private void clearConstantPoolIndices()
    {
        ldcStringCpIndex              = -1;
        invokestaticMethodRefCpIndex  = -1;
        invokevirtualMethodRefCpIndex = -1;
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
}
