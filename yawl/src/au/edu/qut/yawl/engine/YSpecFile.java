/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import au.edu.qut.yawl.persistence.PersistableObject;

public class YSpecFile {
    String xml = "";

    String id = null;

    public YSpecFile() {
    }

    public YSpecFile(String filename) {
        try {
            SAXBuilder builder = new SAXBuilder();

            Document document = builder.build(filename);
            Element specificationSetEl = document.getRootElement();

            List specifications = new Vector();
            List specificationElemList = specificationSetEl.getChildren();
            for (int i = 0; i < specificationElemList.size(); i++) {

                Element specificationElem = (Element) specificationElemList.get(i);
                String uriString = specificationElem.getAttributeValue("uri");
                this.id = uriString;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String temp = null;
        try {
            BufferedReader buf = new BufferedReader(new FileReader(new File(filename)));
            while ((temp = buf.readLine()) != null) {
                xml = xml + temp;
            }
            buf.close();
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

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

}