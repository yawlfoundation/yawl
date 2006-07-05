/* $Id: MappingReader.java,v 1.8 2004/08/15 12:39:30 eric Exp $
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
package proguard.obfuscate;

import java.io.*;


/**
 * This class can parse mapping files and invoke a processor for each of the
 * mapping entries.
 *
 * @author Eric Lafortune
 */
public class MappingReader
{
    private String mappingFileName;


    public MappingReader(String mappingFileName)
    {
        this.mappingFileName = mappingFileName;
    }


    /**
     * Reads the mapping file, presenting all of the encountered mapping entries
     * to the given processor.
     */
    public void pump(MappingProcessor mappingProcessor) throws IOException
    {
        LineNumberReader reader = null;

        try
        {
            reader = new LineNumberReader(
                     new BufferedReader(
                     new FileReader(mappingFileName)));

            String className = null;

            // Read the subsequent class mappings and class member mappings.
            while (true)
            {
                String line = reader.readLine();

                if (line == null)
                {
                    break;
                }

                // The distinction between a class file mapping and a class
                // member mapping is the initial whitespace.
                if (!line.startsWith("    "))
                {
                    // Process the class file mapping and remember the class's
                    // old name.
                    className = processClassFileMapping(line, mappingProcessor);
                }
                else if (className != null)
                {
                    // Process the class member mapping, in the context of the
                    // current old class name.
                    processClassMemberMapping(className, line, mappingProcessor);
                }
            }
        }
        catch (IOException ex)
        {
            throw new IOException("Can't process mapping file (" + ex.getMessage() + ")");
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (IOException ex)
                {
                }
            }
        }
    }


    /**
     * Parses the given line with a class file mapping and processes the
     * results with the given mapping processor. Returns the old class name,
     * or null if any subsequent class member lines can be ignored.
     */
    private String processClassFileMapping(String           line,
                                           MappingProcessor mappingProcessor)
    {
        // See if we can parse "___ -> ___:", containing the original
        // class name and the new class name.

        line = line.trim();

        int arrowIndex = line.indexOf("->");
        if (arrowIndex < 0)
        {
            return null;
        }

        int colonIndex = line.indexOf(':', arrowIndex + 2);
        if (colonIndex < 0)
        {
            return null;
        }

        // Extract the elements.
        String className    = line.substring(0, arrowIndex).trim();
        String newClassName = line.substring(arrowIndex + 2, colonIndex).trim();

        // Process this class name mapping.
        boolean interested = mappingProcessor.processClassFileMapping(className, newClassName);

        return interested ? className : null;
    }


    /**
     * Parses the given line with a class member mapping and processes the
     * results with the given mapping processor.
     */
    private void processClassMemberMapping(String           className,
                                           String           line,
                                           MappingProcessor mappingProcessor)
    {
        // See if we can parse "    ___:___:___ ___ -> ___",
        // containing the optional line numbers, the return type, the original
        // field/method name (including arguments), and the new method name.

        line = line.trim();

        int colonIndex1 = line.indexOf(':');
        int colonIndex2 = line.indexOf(':', colonIndex1 + 1);

        int spaceIndex = line.indexOf(' ', colonIndex2 + 1);
        if (spaceIndex < 0)
        {
            return;
        }

        int arrowIndex = line.indexOf("->", spaceIndex + 1);
        if (arrowIndex < 1)
        {
            return;
        }

        int firstLineNumber = colonIndex1 < 0 ? 0 :
            Integer.parseInt(line.substring(0, colonIndex1).trim());

        int lastLineNumber = colonIndex1 < 0 ||
                             colonIndex2 < 0 ? 0 :
            Integer.parseInt(line.substring(colonIndex1 + 1, colonIndex2).trim());

        // Extract the elements.
        String type    = line.substring(colonIndex2 + 1, spaceIndex).trim();
        String name    = line.substring(spaceIndex + 1, arrowIndex).trim();
        String newName = line.substring(arrowIndex + 2).trim();

        // Process this class member mapping.
        if (type.length() > 0 && name.length() > 0)
        {
            if (name.charAt(name.length() - 1) != ')')
            {
                mappingProcessor.processFieldMapping(className, type, name, newName);
            }
            else
            {
                mappingProcessor.processMethodMapping(className, firstLineNumber, lastLineNumber, type, name, newName);
            }
        }
    }
}
