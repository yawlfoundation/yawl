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
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.List;

public class YSpecFile{
    String xml = "";
    P_YSpecFileID specid = new P_YSpecFileID();


    public YSpecFile() { }


    public YSpecFile(Reader ioReader) {
        this();
        init(ioReader);
    }


    public YSpecFile(String filename) {
        this();
        try {
            Reader ioReader = new FileReader(filename);
            init(ioReader);
        }
        catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
    }


    private void init(Reader ioReader) {
        specid = new P_YSpecFileID();
        SAXBuilder builder = new SAXBuilder();
        Document doc;
        try {
            doc = builder.build(ioReader);
            initSpecID(doc);
            xml = JDOMUtil.documentToString(doc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initSpecID(Document doc) {
        Element specificationSetEl = doc.getRootElement();
        Namespace ns = specificationSetEl.getNamespace();
        List specificationElemList = specificationSetEl.getChildren("specification", ns);
        for (int i = 0; i < specificationElemList.size(); i++) {
            Element specificationElem = (Element) specificationElemList.get(i);
            String uriString = specificationElem.getAttributeValue("uri");
            specid.setId(uriString);
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
