/* $Id: Optimizer.java,v 1.9.2.4 2006/02/13 00:19:28 eric Exp $
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
package proguard.optimize;

import proguard.*;
import proguard.classfile.*;
import proguard.classfile.attribute.*;
import proguard.classfile.editor.*;
import proguard.classfile.instruction.*;
import proguard.classfile.util.MethodInfoLinker;
import proguard.classfile.visitor.*;
import proguard.optimize.evaluation.EvaluationSimplifier;
import proguard.optimize.peephole.*;

import java.io.IOException;

/**
 * This class optimizes class pools according to a given configuration.
 *
 * @author Eric Lafortune
 */
public class Optimizer
{
    private Configuration configuration;


    /**
     * Creates a new Optimizer.
     */
    public Optimizer(Configuration configuration)
    {
        this.configuration = configuration;
    }


    /**
     * Performs optimization of the given program class pool.
     */
    public void execute(ClassPool programClassPool,
                        ClassPool libraryClassPool) throws IOException
    {
        // Create counters to count the numbers of optimizations, if required.
        ClassCounter       singleImplementationCounter = null;
        ClassCounter       finalClassCounter           = null;
        MemberCounter      writeOnlyFieldCounter       = null;
        MemberCounter      finalMethodCounter          = null;
        MemberCounter      privateMethodCounter        = null;
        MemberCounter      staticMethodCounter         = null;
        MemberCounter      parameterShrinkCounter      = null;
        InstructionCounter getterSetterCounter         = null;
        InstructionCounter commonCodeCounter           = null;
        InstructionCounter pushCounter                 = null;
        InstructionCounter branchCounter               = null;
        InstructionCounter deletedCounter              = null;
//      InstructionCounter nopCounter                  = null;
        InstructionCounter pushPopCounter              = null;
        InstructionCounter loadStoreCounter            = null;
        InstructionCounter storeLoadCounter            = null;
        InstructionCounter gotoGotoCounter             = null;
        InstructionCounter gotoReturnCounter           = null;

        if (configuration.verbose)
        {
            singleImplementationCounter = new ClassCounter();
            finalClassCounter           = new ClassCounter();
            writeOnlyFieldCounter       = new MemberCounter();
            finalMethodCounter          = new MemberCounter();
            privateMethodCounter        = new MemberCounter();
            staticMethodCounter         = new MemberCounter();
            parameterShrinkCounter      = new MemberCounter();
            getterSetterCounter         = new InstructionCounter();
            commonCodeCounter           = new InstructionCounter();
            pushCounter                 = new InstructionCounter();
            branchCounter               = new InstructionCounter();
            deletedCounter              = new InstructionCounter();
//          nopCounter                  = new InstructionCounter();
            pushPopCounter              = new InstructionCounter();
            loadStoreCounter            = new InstructionCounter();
            storeLoadCounter            = new InstructionCounter();
            gotoGotoCounter             = new InstructionCounter();
            gotoReturnCounter           = new InstructionCounter();
        }

        // Clean up any old visitor info.
        programClassPool.classFilesAccept(new ClassFileCleaner());
        libraryClassPool.classFilesAccept(new ClassFileCleaner());

        // Link all methods that should get the same optimization info.
        programClassPool.classFilesAccept(new BottomClassFileFilter(
                                          new MethodInfoLinker()));

        // Create a visitor for marking the seeds.
        KeepMarker keepMarker = new KeepMarker();
        ClassPoolVisitor classPoolvisitor =
            new MultiClassPoolVisitor(new ClassPoolVisitor[]
            {
                ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.keep,
                                                                        keepMarker,
                                                                        keepMarker),
                ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.keepNames,
                                                                        keepMarker,
                                                                        keepMarker)
            });

        // Mark the seeds.
        programClassPool.accept(classPoolvisitor);
        libraryClassPool.accept(classPoolvisitor);

        // All library classes and library class members remain unchanged.
        libraryClassPool.classFilesAccept(keepMarker);
        libraryClassPool.classFilesAccept(new AllMemberInfoVisitor(keepMarker));

        // We also keep all classes that are involved in .class constructs.
        programClassPool.classFilesAccept(new AllMethodVisitor(
                                          new AllAttrInfoVisitor(
                                          new AllInstructionVisitor(
                                          new DotClassClassFileVisitor(keepMarker)))));

        // We also keep all classes that are involved in Class.forName constructs.
        programClassPool.classFilesAccept(new AllCpInfoVisitor(
                                          new ClassForNameClassFileVisitor(keepMarker)));

        // Attach some optimization info to all methods, so it can be filled
        // out later.
        programClassPool.classFilesAccept(new AllMethodVisitor(
                                          new MethodOptimizationInfoSetter()));
        libraryClassPool.classFilesAccept(new AllMethodVisitor(
                                          new MethodOptimizationInfoSetter()));

        // Mark all interfaces that have single implementations.
        programClassPool.classFilesAccept(new SingleImplementationMarker(configuration.allowAccessModification, singleImplementationCounter));

        // Make class files and methods final, as far as possible.
        programClassPool.classFilesAccept(new ClassFileFinalizer(finalClassCounter, finalMethodCounter));

        // Mark all fields that are write-only, and mark the used local variables.
        programClassPool.classFilesAccept(new AllMethodVisitor(
                                          new AllAttrInfoVisitor(
                                          new AllInstructionVisitor(
                                          new MultiInstructionVisitor(
                                          new InstructionVisitor[]
                                          {
                                              new WriteOnlyFieldMarker(),
                                              new VariableUsageMarker(),
                                          })))));

        // Mark all fields that are write-only, and mark the used local variables.
        if (configuration.verbose)
        {
            programClassPool.classFilesAccept(new AllFieldVisitor(
                                              new WriteOnlyFieldFilter(
                                              writeOnlyFieldCounter)));
        }

        // Mark all methods that can not be made private.
        programClassPool.classFilesAccept(new NonPrivateMethodMarker());
        libraryClassPool.classFilesAccept(new NonPrivateMethodMarker());

        // Make all non-private and unmarked methods in final classes private.
        programClassPool.classFilesAccept(new ClassFileAccessFilter(ClassConstants.INTERNAL_ACC_FINAL, 0,
                                          new AllMethodVisitor(
                                          new MemberInfoAccessFilter(0, ClassConstants.INTERNAL_ACC_PRIVATE,
                                          new MethodPrivatizer(privateMethodCounter)))));

        // Mark all used parameters, including the 'this' parameters.
        programClassPool.classFilesAccept(new AllMethodVisitor(
                                          new ParameterUsageMarker()));
        libraryClassPool.classFilesAccept(new AllMethodVisitor(
                                          new ParameterUsageMarker()));

        if (configuration.assumeNoSideEffects != null)
        {
            // Create a visitor for marking methods that don't have any side effects.
            NoSideEffectMethodMarker noSideEffectMethodMarker = new NoSideEffectMethodMarker();
            ClassPoolVisitor noClassPoolvisitor =
                ClassSpecificationVisitorFactory.createClassPoolVisitor(configuration.assumeNoSideEffects,
                                                                        null,
                                                                        noSideEffectMethodMarker);

            // Mark the seeds.
            programClassPool.accept(noClassPoolvisitor);
            libraryClassPool.accept(noClassPoolvisitor);
        }

        // Mark all methods that have side effects.
        programClassPool.accept(new SideEffectMethodMarker());

        // Perform partial evaluation.
        programClassPool.classFilesAccept(new AllMethodVisitor(
                                          new EvaluationSimplifier(pushCounter, branchCounter, deletedCounter)));

        // Inline interfaces with single implementations.
        programClassPool.classFilesAccept(new SingleImplementationInliner());

        // Restore the interface references from these single implementations.
        programClassPool.classFilesAccept(new SingleImplementationFixer());

        // Shrink the method parameters and make methods static.
        programClassPool.classFilesAccept(new AllMethodVisitor(
                                          new ParameterShrinker(1024, 64, parameterShrinkCounter, staticMethodCounter)));

        // Fix all references to class files.
        programClassPool.classFilesAccept(new ClassFileReferenceFixer(true));

        // Fix all references to class members.
        programClassPool.classFilesAccept(new MemberReferenceFixer(1024));

        // Create a branch target marker and a code attribute editor that can
        // be reused for all code attributes.
        BranchTargetFinder branchTargetFinder = new BranchTargetFinder(1024);
        CodeAttrInfoEditor codeAttrInfoEditor = new CodeAttrInfoEditor(1024);

        // Visit all code attributes.
        // First let the branch marker mark all branch targets.
        // Then share common blocks of code at branches.
        // Finally apply all changes to the code.
        programClassPool.classFilesAccept(
            new AllMethodVisitor(
            new AllAttrInfoVisitor(
            new MultiAttrInfoVisitor(
            new AttrInfoVisitor[]
            {
                branchTargetFinder,
                new CodeAttrInfoEditorResetter(codeAttrInfoEditor),
                new AllInstructionVisitor(
                new GotoCommonCodeReplacer(branchTargetFinder, codeAttrInfoEditor, commonCodeCounter)),
                codeAttrInfoEditor
            }))));

        // Visit all code attributes.
        // First let the branch marker mark all branch targets.
        // Then perform peephole optimisations on the instructions:
        // - Fix invocations of methods that have become private, static,...
        // - Remove nop instructions.
        // - Remove push/pop instruction pairs.
        // - Remove load/store instruction pairs.
        // - Replace store/load instruction pairs by dup/store instructions.
        // - Simplify branches to branch instructions.
        // - Replace branches to return instructions by return instructions.
        // - Inline simple getters and setters.
        // Finally apply all changes to the code.
        programClassPool.classFilesAccept(
            new AllMethodVisitor(
            new AllAttrInfoVisitor(
            new MultiAttrInfoVisitor(
            new AttrInfoVisitor[]
            {
                branchTargetFinder,
                new CodeAttrInfoEditorResetter(codeAttrInfoEditor),
                new AllInstructionVisitor(
                new MultiInstructionVisitor(
                new InstructionVisitor[]
                {
                    new MethodInvocationFixer(                                     codeAttrInfoEditor),
//                  new NopRemover(                                                codeAttrInfoEditor, nopCounter),
                    new PushPopRemover(        branchTargetFinder,                 codeAttrInfoEditor, pushPopCounter),
                    new LoadStoreRemover(      branchTargetFinder,                 codeAttrInfoEditor, loadStoreCounter),
                    new StoreLoadReplacer(     branchTargetFinder,                 codeAttrInfoEditor, storeLoadCounter),
                    new GotoGotoReplacer(                                          codeAttrInfoEditor, gotoGotoCounter),
                    new GotoReturnReplacer(                                        codeAttrInfoEditor, gotoReturnCounter),
                    new GetterSetterInliner(configuration.allowAccessModification, codeAttrInfoEditor, getterSetterCounter),
                })),
                codeAttrInfoEditor
            }))));

        if (configuration.verbose)
        {
            System.out.println("  Number of inlined interfaces:             "+singleImplementationCounter.getCount());
            System.out.println("  Number of finalized classes:              "+finalClassCounter          .getCount());
            System.out.println("  Number of removed write-only fields:      "+writeOnlyFieldCounter      .getCount());
            System.out.println("  Number of finalized methods:              "+finalMethodCounter         .getCount());
            System.out.println("  Number of privatized methods:             "+privateMethodCounter       .getCount());
            System.out.println("  Number of staticized methods:             "+staticMethodCounter        .getCount());
            System.out.println("  Number of simplified method declarations: "+parameterShrinkCounter     .getCount());
            System.out.println("  Number of inlined getters/setter calls:   "+getterSetterCounter        .getCount());
            System.out.println("  Number of merged code blocks:             "+commonCodeCounter          .getCount());
            System.out.println("  Number of simplified push instructions:   "+pushCounter                .getCount());
            System.out.println("  Number of simplified branches:            "+branchCounter              .getCount());
            System.out.println("  Number of removed instructions:           "+deletedCounter             .getCount());
//          System.out.println("  Number of removed nop instructions:       "+nopCounter                 .getCount());
            System.out.println("  Number of removed push/pop pairs:         "+pushPopCounter             .getCount());
            System.out.println("  Number of removed load/store pairs:       "+loadStoreCounter           .getCount());
            System.out.println("  Number of simplified store/load pairs:    "+storeLoadCounter           .getCount());
            System.out.println("  Number of simplified goto/goto pairs:     "+gotoGotoCounter            .getCount());
            System.out.println("  Number of simplified goto/return pairs:   "+gotoReturnCounter          .getCount());
        }
    }
}
