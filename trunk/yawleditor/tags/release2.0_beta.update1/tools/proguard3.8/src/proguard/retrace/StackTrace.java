/* $Id: StackTrace.java,v 1.8.2.2 2007/01/18 21:31:53 eric Exp $
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
 * This class represents an obfuscated stack trace. It can read, de-obfuscate,
 * and then write its contents.
 *
 * @author Eric Lafortune
 */
class StackTrace implements MappingProcessor
{
    // The stack trace settings.
    private boolean verbose;

    // The stack trace items.
    private List stackTraceItems = new ArrayList();


    /**
     * Creates a new StackTrace.
     * @param verbose specifies whether the de-obfuscated stack trace should
     *                be verbose.
     */
    public StackTrace(boolean verbose)
    {
        this.verbose = verbose;
    }


    /**
     * Reads the stack trace file.
     */
    public void read(File stackTraceFile) throws IOException
    {
        LineNumberReader lineNumberReader = null;

        try
        {
            Reader reader = stackTraceFile == null ?
                (Reader)new InputStreamReader(System.in) :
                (Reader)new BufferedReader(new FileReader(stackTraceFile));

            lineNumberReader = new LineNumberReader(reader);

            // Read the line in the stack trace.
            while (true)
            {
                String line = lineNumberReader.readLine();
                if (line == null)
                {
                    break;
                }

                line = line.trim();
                if (line.length() == 0)
                {
                    continue;
                }

                // Put the line in a stack trace item.
                StackTraceItem item = new StackTraceItem(verbose);

                item.parse(line);

                stackTraceItems.add(item);
            }
        }
        catch (IOException ex)
        {
            throw new IOException("Can't read stack trace (" + ex.getMessage() + ")");
        }
        finally
        {
            if (stackTraceFile != null &&
                lineNumberReader != null)
            {
                try
                {
                    lineNumberReader.close();
                }
                catch (IOException ex)
                {
                }
            }
        }
    }


    /**
     * Prints out the de-obfuscated stack trace.
     */
    public void print()
    {
        // Delegate to each of the stack trace items.
        for (int index = 0; index < stackTraceItems.size(); index++)
        {
            StackTraceItem item = (StackTraceItem)stackTraceItems.get(index);

            item.print();
        }
    }


    // Implementations for MappingProcessor.

    public boolean processClassFileMapping(String className,
                                           String newClassName)
    {
        // Delegate to each of the stack trace items.
        boolean present = false;
        for (int index = 0; index < stackTraceItems.size(); index++)
        {
            StackTraceItem item = (StackTraceItem)stackTraceItems.get(index);

            present |= item.processClassFileMapping(className,
                                                    newClassName);
        }

        return present;
    }


    public void processFieldMapping(String className,
                                    String fieldType,
                                    String fieldName,
                                    String newFieldName)
    {
        // A stack trace never contains any fields.
    }


    public void processMethodMapping(String className,
                                     int    firstLineNumber,
                                     int    lastLineNumber,
                                     String methodReturnType,
                                     String methodNameAndArguments,
                                     String newMethodName)
    {
        // Delegate to each of the stack trace items.
        for (int index = 0; index < stackTraceItems.size(); index++)
        {
            StackTraceItem item = (StackTraceItem)stackTraceItems.get(index);

            item.processMethodMapping(className,
                                      firstLineNumber,
                                      lastLineNumber,
                                      methodReturnType,
                                      methodNameAndArguments,
                                      newMethodName);
        }
    }
}
