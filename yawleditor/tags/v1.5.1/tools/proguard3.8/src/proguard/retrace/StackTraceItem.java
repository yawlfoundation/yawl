/* $Id: StackTraceItem.java,v 1.9.2.2 2007/01/18 21:31:53 eric Exp $
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
package proguard.retrace;

import java.io.*;
import java.util.*;

import proguard.obfuscate.MappingProcessor;


/**
 * This class represents an obfuscated stack trace item. It can read, de-obfuscate,
 * and then write its contents.
 *
 * @author Eric Lafortune
 */
class StackTraceItem implements MappingProcessor
{
    // The stack trace settings.
    private boolean verbose;

    public String prefix;
    public String obfuscatedClassName;
    public String obfuscatedMethodName;
    public String sourceFile;
    public int    lineNumber;
    public String suffix;

    public String originalClassName;
    public List   originalMethodNames;

    /**
     * Creates a new StackTraceItem.
     * @param verbose specifies whether the de-obfuscated stack trace should
     *                be verbose.
     */
    public StackTraceItem(boolean verbose)
    {
        this.verbose = verbose;
    }


    /**
     * Parses the stack trace
     */
    public void parse(String line) throws IOException
    {
        if (!parseAtLine(line) &&
            !parseExceptionInThreadLine(line))
        {
            parseAnyLine(line);
        }
    }


    /**
     * Tries to parse "at ___.___(___:___)", containing the class name,
     * the method name, the source file, and the optional line number.
     */
    private boolean parseAtLine(String line)
    {
        if (!line.startsWith("at "))
        {
            return false;
        }

        int openParenthesisIndex = line.indexOf('(', 3);
        if (openParenthesisIndex < 0)
        {
            return false;
        }

        int colonIndex = line.indexOf(':', openParenthesisIndex + 1);

        int closeParenthesisIndex = line.indexOf(')', Math.max(openParenthesisIndex, colonIndex) + 1);
        if (closeParenthesisIndex < 0)
        {
            return false;
        }

        int periodIndex = line.lastIndexOf('.', openParenthesisIndex - 1);
        if (periodIndex < 0)
        {
            return false;
        }

        prefix               = "        at ";
        obfuscatedClassName  = line.substring(3, periodIndex).trim();
        obfuscatedMethodName = line.substring(periodIndex + 1, openParenthesisIndex).trim();
        sourceFile           = line.substring(openParenthesisIndex + 1, colonIndex < 0 ? closeParenthesisIndex : colonIndex).trim();
        lineNumber           = colonIndex < 0 ? 0 : Integer.parseInt(line.substring(colonIndex + 1, closeParenthesisIndex).trim());

        return true;
    }


    /**
     * Tries to parse "Exception in thread "___" ___:___" or just "___:___",
     * containing the optional thread name, the exception class name and the
     * exception message.
     */
    private boolean parseExceptionInThreadLine(String line)
    {
        // Trim away the thread message part, if any.
        if (line.startsWith("Exception in thread \""))
        {
            int quote_index = line.indexOf('"', 21);
            if (quote_index < 0)
            {
                return false;
            }

            prefix = line.substring(0, quote_index+1) + " ";
            line   = line.substring(quote_index+1).trim();
        }

        int colonIndex = line.indexOf(':');
        if (colonIndex < 0)
        {
            return false;
        }

        int spaceIndex = line.lastIndexOf(' ', colonIndex);

        prefix              = line.substring(0, spaceIndex+1);
        obfuscatedClassName = line.substring(spaceIndex+1, colonIndex).trim();
        suffix              = line.substring(colonIndex);

        return true;
    }


    /**
     * Parses any line.
     */
    private void parseAnyLine(String line)
    {
        prefix = line;
    }


    /**
     * Prints out the de-obfuscated stack trace.
     */
    public void print()
    {
        // Get the original class name, if we found it.
        String className = originalClassName != null ?
            originalClassName :
            obfuscatedClassName;

        // Get the first original method name, if we found it.
        String methodName = originalMethodNames != null ?
            (String)originalMethodNames.get(0) :
            obfuscatedMethodName;

        // Compose the source file with the line number, if any.
        String source = lineNumber != 0 ?
            sourceFile + ":" + lineNumber :
            sourceFile;

        // Print out the resolved stack trace
        if (prefix != null)
        {
            System.out.print(prefix);
        }

        if (className != null)
        {
            System.out.print(className);
        }

        if (methodName != null)
        {
            System.out.print("." + methodName + "(" + source + ")");

            // Print out alternatives, if any.
            if (originalMethodNames != null)
            {
                for (int otherMethodNameIndex = 1; otherMethodNameIndex < originalMethodNames.size(); otherMethodNameIndex++) {
                    String otherMethodName = (String)originalMethodNames.get(otherMethodNameIndex);
                    System.out.println();
                    printSpaces(className.length()+12);
                    System.out.print(otherMethodName);
                }
            }
        }

        if (suffix != null)
        {
            System.out.print(suffix);
        }

        System.out.println();
    }


    /**
     * Prints the given number of spaces.
     */
    private void printSpaces(int aCount)
    {
        for (int counter = 0; counter < aCount; counter++)
          System.out.print(' ');
    }


    // Implementations for MappingProcessor.

    public boolean processClassFileMapping(String className,
                                           String newClassName)
    {
        boolean present = false;

        if (newClassName.equals(obfuscatedClassName))
        {
            originalClassName = className;
            present = true;
        }

        return present;
    }


    public void processFieldMapping(String className,
                                    String fieldType,
                                    String fieldName,
                                    String newFieldName)
    {
        // A stack trace item never contains any fields.
    }


    public void processMethodMapping(String className,
                                     int    firstLineNumber,
                                     int    lastLineNumber,
                                     String methodReturnType,
                                     String methodNameAndArguments,
                                     String newMethodName)
    {
        if (className.equals(originalClassName) &&
            newMethodName.equals(obfuscatedMethodName) &&
            (lineNumber == 0 ||
             firstLineNumber == 0 ||
             lastLineNumber  == 0 ||
             (firstLineNumber <= lineNumber &&
              lastLineNumber  >= lineNumber)))
        {
            // Create a List for storing solutions for this
            // method name.
            if (originalMethodNames == null)
            {
                originalMethodNames = new ArrayList();
            }

            // Does the method have line numbers?
            if (firstLineNumber != 0 &&
                lastLineNumber  != 0 &&
                lineNumber      != 0)
            {
                // Then it will be the one and only solution.
                obfuscatedMethodName = null;
                originalMethodNames.clear();
            }

            // Include return type and arguments in the method name if
            // we're in verbose mode, otherwise strip the arguments.
            String originalMethodName = verbose ?
                (methodReturnType + " " + methodNameAndArguments) :
                methodNameAndArguments.substring(0, methodNameAndArguments.indexOf('('));

            // Add this method name solution to the list.
            originalMethodNames.add(originalMethodName);
        }
    }
}
