package org.yawlfoundation.yawl.elements;

import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.util.JDOMUtil;

/**
 * Provides for the persistence of in-progress multiple instance task output data -
 * i.e. stores the output data of completed child work items of an MI task, until
 * the entire task completes
 *
 * @author Michael Adams
 * @date 7/10/2024
 */
public class GroupedMIOutputData {

    private String _uniqueID;
    private Document _dataDoc;
    private Document _dynamicDataDoc;

    protected GroupedMIOutputData() { }       // for hibernate instantiation

    protected GroupedMIOutputData(YIdentifier caseID, String taskID, String rootName) {
        _uniqueID = caseID.get_idString() + ":" + taskID;
        _dataDoc = new Document(new Element(rootName));
        _dynamicDataDoc = new Document(new Element(rootName));
    }


    protected void addStaticContent(Element content) {
        addContent(_dataDoc, content);
    }

    protected void addDynamicContent(Element content) {
        addContent(_dynamicDataDoc, content);
    }
    
    private void addContent(Document doc, Element content) {
        doc.getRootElement().addContent(content.clone());
    }


    public String getUniqueIdentifier() { return _uniqueID; }

    public String getCaseID() { return _uniqueID.split(":")[0]; }
    

    protected Document getDataDoc() { return _dataDoc; }

    protected Document getDynamicDataDoc() { return _dynamicDataDoc; }

    
    // for hibernate
    protected String getDataDocString() {
        return JDOMUtil.documentToString(_dataDoc);
    }

    // for hibernate
    protected void setDataDocString(String xml) {
        _dataDoc = JDOMUtil.stringToDocument(xml);
    }

    // for hibernate
    protected String getDynamicDataDocString() {
        return JDOMUtil.documentToString(_dynamicDataDoc);
    }

    // for hibernate
    protected void setDynamicDataDocString(String xml) {
        _dynamicDataDoc = JDOMUtil.stringToDocument(xml);
    }
    
    // for hibernate
    protected void setUniqueIdentifier(String id) {
        _uniqueID = id;
    }

}
