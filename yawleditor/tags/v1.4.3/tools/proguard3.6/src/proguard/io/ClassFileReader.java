/* $Id: ClassFileReader.java,v 1.6.2.2 2006/04/12 07:15:05 eric Exp $
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
package proguard.io;

import proguard.classfile.*;
import proguard.classfile.util.ClassUtil;
import proguard.classfile.visitor.*;

import java.io.*;

/**
 * This DataEntryReader applies a given ClassFileVisitor to the class file
 * definitions that it reads.
 * <p>
 * Class files are read as ProgramClassFile objects or LibraryClassFile objects,
 * depending on the <code>isLibrary</code> flag.
 * <p>
 * In case of libraries, only public class files are considered, if the
 * <code>skipNonPublicLibraryClasses</code> flag is set.
 *
 * @author Eric Lafortune
 */
public class ClassFileReader implements DataEntryReader
{
    private boolean          isLibrary;
    private boolean          skipNonPublicLibraryClasses;
    private boolean          skipNonPublicLibraryClassMembers;
    private boolean          note;
    private ClassFileVisitor classFileVisitor;


    /**
     * Creates a new DataEntryClassFileFilter for reading the specified
     * ClassFile objects.
     */
    public ClassFileReader(boolean          isLibrary,
                           boolean          skipNonPublicLibraryClasses,
                           boolean          skipNonPublicLibraryClassMembers,
                           boolean          note,
                           ClassFileVisitor classFileVisitor)
    {
        this.isLibrary                        = isLibrary;
        this.skipNonPublicLibraryClasses      = skipNonPublicLibraryClasses;
        this.skipNonPublicLibraryClassMembers = skipNonPublicLibraryClassMembers;
        this.note                             = note;
        this.classFileVisitor                 = classFileVisitor;
    }


    // Implementations for DataEntryReader.

    public void read(DataEntry dataEntry) throws IOException
    {
        try
        {
            // Get the input stream.
            InputStream inputStream = dataEntry.getInputStream();

            // Wrap it into a data input stream.
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // Create a ClassFile representation.
            ClassFile classFile = isLibrary ?
                (ClassFile)LibraryClassFile.create(dataInputStream, skipNonPublicLibraryClasses, skipNonPublicLibraryClassMembers) :
                (ClassFile)ProgramClassFile.create(dataInputStream);

            // Apply the visitor.
            if (classFile != null)
            {
                if (note &&
                    !dataEntry.getName().replace(File.pathSeparatorChar, ClassConstants.INTERNAL_PACKAGE_SEPARATOR).equals(classFile.getName()+ClassConstants.CLASS_FILE_EXTENSION))
                {
                    System.err.println("Note: class file [" + dataEntry.getName() + "] unexpectedly contains class [" + ClassUtil.externalClassName(classFile.getName()) + "]");
                }

                classFile.accept(classFileVisitor);
            }

            dataEntry.closeInputStream();
        }
        catch (Exception ex)
        {
            throw new IOException("Can't process class file ["+dataEntry.getName()+"] ("+ex.getMessage()+")");
        }
    }
}
