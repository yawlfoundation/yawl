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

package org.yawlfoundation.yawl.schema;

import org.w3c.dom.ls.LSInput;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * @author Michael Adams
 * @date 8/12/14
 */
public class Input implements LSInput {

    private String publicId;
    private String systemId;
    private BufferedInputStream inputStream;


    public Input(String publicId, String sysId, InputStream input) {
        this.publicId = publicId;
        this.systemId = sysId;
        this.inputStream = new BufferedInputStream(input);
    }


    public String getPublicId() {
        return publicId;
    }

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    public String getBaseURI() {
        return null;
    }

    public InputStream getByteStream() {
        return null;
    }

    public boolean getCertifiedText() {
        return false;
    }

    public Reader getCharacterStream() {
        return null;
    }

    public String getEncoding() {
        return null;
    }

    public String getStringData() {
        synchronized (inputStream) {
            try {
                byte[] input = new byte[inputStream.available()];
                inputStream.read(input);
                String contents = new String(input);
                return contents;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Exception " + e);
                return null;
            }
        }
    }

    public void setBaseURI(String baseURI) {
    }

    public void setByteStream(InputStream byteStream) {
    }

    public void setCertifiedText(boolean certifiedText) {
    }

    public void setCharacterStream(Reader characterStream) {
    }

    public void setEncoding(String encoding) {
    }

    public void setStringData(String stringData) {
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public BufferedInputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(BufferedInputStream inputStream) {
        this.inputStream = inputStream;
    }

}