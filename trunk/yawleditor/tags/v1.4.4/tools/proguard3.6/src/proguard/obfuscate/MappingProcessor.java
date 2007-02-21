/* $Id: MappingProcessor.java,v 1.5.2.1 2006/01/16 22:57:56 eric Exp $
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
package proguard.obfuscate;


/**
 * This interface specifies methods to process name mappings between original
 * class files and their obfuscated versions. The mappings are typically read
 * from a mapping file.
 *
 * @see MappingReader
 *
 * @author Eric Lafortune
 */
public interface MappingProcessor
{
    /**
     * Processes the given class name mapping.
     *
     * @param className    the original class name.
     * @param newClassName the new class name.
     * @return whether the processor is interested in receiving mappings of the
     *         class members of this class.
     */
    public boolean processClassFileMapping(String className,
                                           String newClassName);

    /**
     * Processes the given field name mapping.
     *
     * @param className    the original class name.
     * @param fieldType    the original external field type.
     * @param fieldName    the original field name.
     * @param newFieldName the new field name.
     */
    public void processFieldMapping(String className,
                                    String fieldType,
                                    String fieldName,
                                    String newFieldName);

    /**
     * Processes the given method name mapping.
     *
     * @param className              the original class name.
     * @param firstLineNumber        the first line number of the method, or
     *                               0 if it is not known.
     * @param lastLineNumber         the last line number of the method, or
     *                               0 if it is not known.
     * @param methodReturnType       the original external method return type.
     * @param methodNameAndArguments the original external method name and
     *                               arguments.
     * @param newMethodName          the new method name.
     */
    public void processMethodMapping(String className,
                                     int    firstLineNumber,
                                     int    lastLineNumber,
                                     String methodReturnType,
                                     String methodNameAndArguments,
                                     String newMethodName);
}
