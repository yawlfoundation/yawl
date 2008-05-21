/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Vector;
import java.nio.CharBuffer;

public class YSpecFile{
    String xml = "";
    P_YSpecFileID specid = new P_YSpecFileID();
    private static final Logger logger = Logger.getLogger(YSpecFile.class);


    public YSpecFile() { }

    public YSpecFile(String filename) {
        this();
        specid = new P_YSpecFileID();
        try {
            logger.debug("--> YSpecFile: " + filename);
            SAXBuilder builder = new SAXBuilder();

            Document document = builder.build(filename);
            Element specificationSetEl = document.getRootElement();

            List specifications = new Vector();
            List specificationElemList = specificationSetEl.getChildren();
            for (int i = 0; i < specificationElemList.size(); i++) {

                Element specificationElem = (Element) specificationElemList.get(i);
                String uriString = specificationElem.getAttributeValue("uri");
                specid.setId(uriString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String temp = null;
        try {
            /**
             * Following code is good example of how not to load a file into a String instance!!!.
             * Load time for a 41K XML spec gets reduced from 10mins to 1 sec on a P4 1.8GHz PC.
             */
//            BufferedReader buf = new BufferedReader(new FileReader(new File(filename)));
//            while ((temp = buf.readLine()) != null) {
//                xml = xml + temp;
//                System.out.println(xml.length());
//            }
//            buf.close();

            //mlf:BEGIN
            File file = new File(filename);
            long length = file.length();
            logger.info("Allocating input buffer at size " + length + " bytes");
            CharBuffer charBuffer = CharBuffer.allocate(Math.abs((int) length));

            BufferedReader buf = new BufferedReader(new FileReader(file));

            int read;
            do
            {
                read = buf.read(charBuffer);
            }
            while (read != -1 && buf.ready());

            charBuffer.position(0);
            xml = charBuffer.toString();
            logger.info("Specification buffer loaded OK");
            //mlf:END

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getXML() {
        return xml;
    }

    public void setXML(String xml) {
        this.xml = xml;
    }

    public P_YSpecFileID getSpecid()
    {
        return specid;
    }

    public void setSpecid(P_YSpecFileID specid)
    {
        this.specid = specid;
    }

}