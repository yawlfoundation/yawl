/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.documentStore;

import java.io.*;

/**
 * Holds a binary file (of any type) to be passed as work item data. Note that the file
 * itself is located in the 'Document Store' service, only its name and id are passed
 * to and from the engine
 *
 * @author Michael Adams
 * @date 18/11/11
 */

public class YDocument {
    long id;                                             // a hibernate primary key
    String caseId;                                       // the case it belongs to
    byte[] document;                                     // the binary file


    /**
     * Constructs a new, empty YDocument
     */
    public YDocument() {
        id = -1;
        caseId = "";
    }


    /**
     * Constructs a new YDocument
     * @param caseID the id of the case the document is a member of
     * @param docID the id of the internal binary file. Set it to a negative value if
     *              the document has not yet been stored (and so does not yet have an id)
     * @param docAsBytes a byte array representing the contents of the binary file
     */
    public YDocument(String caseID, long docID, byte[] docAsBytes) {
        caseId = caseID;
        id = docID;
        document = docAsBytes;
    }


    /**
     * Constructs a new YDocument
     * @param caseID the id of the case the document is a member of
     * @param docID the id of the internal binary file. Set it to a negative value if
     *              the document has not yet been stored (and so does not yet have an id)
     * @param docStream a stream from which the contents of the binary file may be read
     * @throws IOException if there's a problem reading from the stream
     */
    public YDocument(String caseID, long docID, InputStream docStream) throws IOException {
        caseId = caseID;
        id = docID;
        setDocument(docStream);
    }


    /**********************************************************************************/

    /**
     * Gets the document identifier
     * @return the current document identifier
     */
    public long getId() { return id; }


    /**
     * Sets the document identifier
     * @param id the document identifier to set
     */
    public void setId(long id) { this.id = id; }


    /**
     * Checks that the document identifier has a valid value
     * @return true if the identifier is set and valid
     */
    public boolean hasValidId() { return id > -1; }


    /**
     * Gets the case id for this document
     * @return the case id, or an empty String id it has not been set
     */
    public String getCaseId() { return caseId != null ? caseId : ""; }


    /**
     * Sets the case id for this document
     * @param id the case id to set for this document
     */
    public void setCaseId(String id) { caseId = id; }


    /**
     * Gets the binary file stored in this document
     * @return the store binary file, as a byte array
     */
    public byte[] getDocument() { return document; }


    /**
     * Gets the size of the store binary file
     * @return the size of the file as a number of bytes
     */
    public int getDocumentSize() { return (document != null) ? document.length : 0; }


    /**
     * Sets the binary file to be stored in this document
     * @param doc a byte array representing the contents of a binary file
     */
    public void setDocument(byte[] doc) { document = doc; }


    /**
     * Sets the binary file to be stored in this document
     * @param file a File object containing a reference to a disk file to be read into
     *             this document
     * @throws IOException if there's a problem loading the file
     */
    public void setDocument(File file) throws IOException {
        setDocument(new FileInputStream(file));
    }


    /**
     * Sets the binary file to be stored in this document
     * @param fileName a fully qualified path and file name of a disk file to be read into
     *                 this document
     * @throws IOException if there's a problem loading the file
     */
    public void setDocument(String fileName) throws IOException {
        setDocument(new File(fileName));
    }


    /**
     * Sets the binary file to be stored in this document
     * @param in a stream from which the contents of the binary file may be read
     * @throws IOException if there's a problem loading the file
     */
    public void setDocument(InputStream in) throws IOException {
        if (in == null) return;
        final int BUF_SIZE = 32768;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream(BUF_SIZE);
        byte[] buffer = new byte[BUF_SIZE];

        // read chunks from the input stream and write them out
        int bytesRead;
        while ((bytesRead = in.read(buffer, 0, BUF_SIZE)) > 0) {
            outStream.write(buffer, 0, bytesRead);
        }
        setDocument(outStream.toByteArray());

        outStream.close();
        in.close();
    }


    /**
     * Writes the binary file stored in this document to a disk file
     * @param fileName a fully qualified path and file name of a disk file to write
     * @throws IOException if there's a problem writing the file
     */
    public void writeToFile(String fileName) throws IOException {
        writeToFile(new File(fileName));
    }
    
    
    /**
     * Writes the binary file stored in this document to a disk file
     * @param file a File object containing a reference to a disk file to be written to
     * @throws IOException if there's a problem writing the file
     */
    public void writeToFile(File file) throws IOException {
        FileOutputStream os = new FileOutputStream(file);
        os.write(document);
        os.flush();
        os.close();
    }

}
