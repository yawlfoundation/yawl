/* $Id: ReTrace.java,v 1.7 2004/08/15 12:39:30 eric Exp $
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
package proguard.retrace;

import java.io.IOException;

import proguard.obfuscate.MappingReader;


/**
 * Tool for de-obfuscating stack traces of applications that were obfuscated
 * with ProGuard.
 *
 * @author Eric Lafortune
 */
public class ReTrace
{
    private static final String VERBOSE_OPTION = "-verbose";


    // The class settings.
    private boolean verbose;
    private String  mappingFileName;
    private String  stackTraceFileName;


    /**
     * Creates a new ReTrace object to process stack traces on the standard
     * input, based on the given mapping file name.
     * @param verbose         specifies whether the de-obfuscated stack trace
     *                        should be verbose.
     * @param mappingFileName the mapping file that was written out by ProGuard.
     */
    public ReTrace(boolean verbose,
                   String  mappingFileName)
    {
        this(verbose, mappingFileName, null);
    }


    /**
     * Creates a new ReTrace object to process a stack trace from the given file,
     * based on the given mapping file name.
     * @param verbose            specifies whether the de-obfuscated stack trace
     *                           should be verbose.
     * @param mappingFileName    the mapping file that was written out by
     *                           ProGuard.
     * @param stackTraceFileName the name of the file that contains the stack
     *                           trace.
     */
    public ReTrace(boolean verbose,
                   String  mappingFileName,
                   String  stackTraceFileName)
    {
        this.verbose            = verbose;
        this.mappingFileName    = mappingFileName;
        this.stackTraceFileName = stackTraceFileName;
    }


    /**
     * Performs the subsequent ReTrace operations.
     */
    public void execute() throws IOException
    {
        StackTrace stackTrace = new StackTrace(verbose);
        MappingReader reader = new MappingReader(mappingFileName);

        // Read the obfuscated stack trace.
        stackTrace.read(stackTraceFileName);

        // Resolve the obfuscated stack trace by means of the mapping file.
        reader.pump(stackTrace);

        // Print out the resolved stack trace.
        stackTrace.print();
    }


    /**
     * The main program for ReTrace.
     */
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.err.println("Usage: java proguard.ReTrace [-verbose] <mapping_file> [<stacktrace_file>]");
            System.exit(-1);
        }

        int argumentIndex = 0;

        boolean verbose = false;
        if (args[argumentIndex].equals(VERBOSE_OPTION))
        {
            verbose = true;
            argumentIndex++;

            if (args.length < 2)
            {
                System.err.println("Usage: java proguard.ReTrace [-verbose] <mapping_file> [<stacktrace_file>]");
                System.exit(-1);
            }
        }

        String mappingFileName    = args[argumentIndex++];
        String stackTraceFileName = argumentIndex < args.length ?
            args[argumentIndex++] :
            null;

        ReTrace reTrace = new ReTrace(verbose,
                                      mappingFileName,
                                      stackTraceFileName);
        try
        {
            // Execute ReTrace with its given settings.
            reTrace.execute();
        }
        catch (IOException ex)
        {
            if (verbose)
            {
                // Print a verbose stack trace.
                ex.printStackTrace();
            }
            else
            {
                // Print just the stack trace message.
                System.err.println("Error: "+ex.getMessage());
            }

            System.exit(1);
        }

        System.exit(0);
    }
}
