/* $Id: JarWriter.java,v 1.3.2.1 2006/01/16 22:57:56 eric Exp $
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

import java.io.*;
import java.util.jar.*;
import java.util.*;
import java.util.zip.*;


/**
 * This DataEntryWriter sends data entries to a given jar/zip file.
 * The manifest and comment properties can optionally be set.
 *
 * @author Eric Lafortune
 */
public class JarWriter implements DataEntryWriter, Finisher
{
    private DataEntryWriter dataEntryWriter;
    private Manifest        manifest;
    private String          comment;

    private OutputStream    currentParentOutputStream;
    private ZipOutputStream currentJarOutputStream;
    private Finisher        currentFinisher;
    private String          currentEntryName;

    // The names of the jar entries that are already in the jar.
    private Set jarEntryNames = new HashSet();


    /**
     * Creates a new JarWriter without manifest or comment.
     */
    public JarWriter(DataEntryWriter dataEntryWriter)
    {
        this(dataEntryWriter, null, null);
    }


    /**
     * Creates a new JarWriter.
     */
    public JarWriter(DataEntryWriter dataEntryWriter,
                     Manifest        manifest,
                     String          comment)
    {
        this.dataEntryWriter = dataEntryWriter;
        this.manifest        = manifest;
        this.comment         = comment;
    }


    // Implementations for DataEntryWriter.

    public OutputStream getOutputStream(DataEntry dataEntry) throws IOException
    {
        return getOutputStream(dataEntry,  null);
    }


    public OutputStream getOutputStream(DataEntry dataEntry,
                                        Finisher  finisher) throws IOException
    {
        // Get the parent stream, new or exisiting.
        // This may finish our own jar output stream.
        OutputStream parentOutputStream =
            dataEntryWriter.getOutputStream(dataEntry.getParent(), this);

        // Did we get a stream?
        if (parentOutputStream == null)
        {
            return null;
        }

        // Do we need a new stream?
        if (currentParentOutputStream == null)
        {
            currentParentOutputStream = parentOutputStream;

            // Create a new jar stream, with a manifest, if set.
            currentJarOutputStream = manifest != null ?
                new JarOutputStream(parentOutputStream, manifest) :
                new ZipOutputStream(parentOutputStream);

            // Add a comment, if set.
            if (comment != null)
            {
                currentJarOutputStream.setComment(comment);
            }
        }

        // Get the entry name.
        String name = dataEntry.getName();

        // Do we need a new entry?
        if (!name.equals(currentEntryName))
        {
            // Close the previous ZIP entry, if any.
            closeEntry();

            // We have to check if the name is already used, because ZipOutputStream
            // doesn't handle this case properly (it throws an exception which can
            // be caught, but the ZipDataEntry is remembered anyway).
            if (!jarEntryNames.add(name))
            {
                throw new IOException("Duplicate zip entry ["+dataEntry+"]");
            }

            // Create a new entry.
            currentJarOutputStream.putNextEntry(new ZipEntry(name));

            currentFinisher  = finisher;
            currentEntryName = name;
        }

        return currentJarOutputStream;
    }


    public void finish() throws IOException
    {
        // Finish the entire ZIP stream, if any.
        if (currentJarOutputStream != null)
        {
            // Close the previous ZIP entry, if any.
            closeEntry();

            // Finish the entire ZIP stream.
            currentJarOutputStream.finish();
            currentJarOutputStream    = null;
            currentParentOutputStream = null;
            jarEntryNames.clear();
        }
    }


    public void close() throws IOException
    {
        // Close the parent stream.
        dataEntryWriter.close();
    }


    // Small utility methods.

    /**
     * Closes the previous ZIP entry, if any.
     */
    private void closeEntry() throws IOException
    {
        if (currentEntryName != null)
        {
            // Let any finisher finish up first.
            if (currentFinisher != null)
            {
                currentFinisher.finish();
                currentFinisher = null;
            }

            currentJarOutputStream.closeEntry();
            currentEntryName = null;
        }
    }
}
