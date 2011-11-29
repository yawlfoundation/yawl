/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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
 * @author Michael Adams
 * @date 18/11/11
 */
public class YDocument {
    long id;
    String caseId;
    byte[] document;

    public YDocument() {
        id = -1;
        caseId = "";
    }

    
    public YDocument(String caseID, long docID, byte[] docAsBytes) {
        caseId = caseID;
        id = docID;
        document = docAsBytes;
    }
    
    public YDocument(String caseID, long docID, InputStream docStream) throws IOException {
        caseId = caseID;
        id = docID;
        setDocument(docStream);
    }


    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public boolean hasValidId() { return id > -1; }


    public String getCaseId() { return caseId != null ? caseId : ""; }

    public void setCaseId(String id) { caseId = id; }


    public byte[] getDocument() { return document; }

    public void setDocument(byte[] doc) { document = doc; }
    
    public void setDocument(File file) throws IOException {
        setDocument(new FileInputStream(file));
    }

    public void setDocument(String fileName) throws IOException {
        setDocument(new File(fileName));
    }

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
    
    
    public void writeToFile(String fileName) throws IOException {
        writeToFile(new File(fileName));
    }
    
    
    public void writeToFile(File file) throws IOException {
        FileOutputStream os = new FileOutputStream(file);
        os.write(document);
        os.flush();
        os.close();
    }

    
    public int getDocumentSize() { return (document != null) ? document.length : 0; }

}
