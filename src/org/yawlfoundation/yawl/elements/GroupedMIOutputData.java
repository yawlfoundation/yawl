package org.yawlfoundation.yawl.elements;

import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.engine.YWorkItem;
import org.yawlfoundation.yawl.engine.YWorkItemID;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private Document _completedWorkitems;

    public GroupedMIOutputData() { }       // for hibernate and importer instantiation

    protected GroupedMIOutputData(YIdentifier caseID, String taskID, String rootName) {
        _uniqueID = caseID.get_idString() + ":" + taskID;
        _dataDoc = new Document(new Element(rootName));
        _dynamicDataDoc = new Document(new Element(rootName));
        _completedWorkitems = new Document(new Element("items"));
    }


    protected void addStaticContent(Element content) {
        addContent(_dataDoc, content);
    }


    protected void addDynamicContent(Element content) {
        addContent(_dynamicDataDoc, content);
    }


    public void addCompletedWorkItem(YWorkItem item) {
        Element eItem = JDOMUtil.stringToElement(item.toXML());
        _completedWorkitems.getRootElement().addContent(eItem.clone());
    }

    
    private void addContent(Document doc, Element content) {
        doc.getRootElement().addContent(content.clone());
    }


    public String getUniqueIdentifier() { return _uniqueID; }

    public String getCaseID() { return _uniqueID.split(":")[0]; }

    public List<YWorkItem> getCompletedWorkItems() {
        List<YWorkItem> items = new ArrayList<>();
        if (_completedWorkitems != null) {
            for (Content eItem : _completedWorkitems.getRootElement().getContent()) {
                items.add(makeWorkItem((Element) eItem));
            }
        }
        return items;
    }
    

    protected Document getDataDoc() { return _dataDoc; }

    protected Document getDynamicDataDoc() { return _dynamicDataDoc; }


    private YWorkItem makeWorkItem(Element nItem) {
        YWorkItem item = new YWorkItem();
        String caseID = nItem.getChildText("caseid");
        String taskID = nItem.getChildText("taskid");
        String uniqueID = nItem.getChildText("uniqueid");
        YWorkItemID id = new YWorkItemID(new YIdentifier(caseID), taskID, uniqueID);
        item.setWorkItemID(id);
        item.set_thisID(id.toString() + "!" + id.getUniqueID());

        item.set_specIdentifier(nItem.getChildText("specidentifier"));
        item.set_specUri(nItem.getChildText("specuri"));
        item.set_specVersion(nItem.getChildText("specversion"));
        setTimestamps(item, nItem);
        setData(item, nItem);
        item.set_status(nItem.getChildText("status"));
        item.set_prevStatus(nItem.getChildText("prevstatus"));
        item.set_externalClient(nItem.getChildText("client"));
        item.set_allowsDynamicCreation(getBoolean(nItem.getChildText("allowsdynamiccreation")));
        item.setRequiresManualResourcing(getBoolean(nItem.getChildText("requiresmanualresourcing")));
        item.setTimerStarted(getBoolean(nItem.getChildText("timerstarted")));
        item.setTimerExpiry(getLong(nItem.getChildText("timerexpiry")));
        item.setCodelet(nItem.getChildText("codelet"));
        item.set_deferredChoiceGroupID(nItem.getChildText("deferredgroupid"));
        return item;
    }

    private void setTimestamps(YWorkItem item, Element nItem) {
        Date timestamp = makeDate(nItem.getChildText("enablementTimeMs"));
        if (timestamp != null) item.set_enablementTime(timestamp);
        timestamp = makeDate(nItem.getChildText("firingTimeMs"));
        if (timestamp != null) item.set_firingTime(timestamp);
        timestamp = makeDate(nItem.getChildText("startTimeMs"));
        if (timestamp != null) item.set_startTime(timestamp);
    }


    private void setData(YWorkItem item, Element nItem) {
        Element nData = nItem.getChild("data");
        if (nData != null && nData.getContentSize() > 0) {
            item.set_dataString(JDOMUtil.elementToString(nData));
        }
    }


    private Date makeDate(String timeStr) {
        return ! (timeStr == null || "0".equals(timeStr)) ?
                new Date(StringUtil.strToLong(timeStr, 0)) : null;
    }


    private boolean getBoolean(String bValue) {
        return "true".equalsIgnoreCase(bValue);
    }


    private long getLong(String lvalue) {
        return StringUtil.strToLong(lvalue,0);
    }

    // for hibernate
    public String getDataDocString() {
        return JDOMUtil.documentToString(_dataDoc);
    }

    // for hibernate
    public void setDataDocString(String xml) {
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
    protected String getCompletedItemsString() {
        return JDOMUtil.documentToString(_completedWorkitems);
    }

    // for hibernate
    protected void setCompletedItemsString(String xml) {
        _completedWorkitems = JDOMUtil.stringToDocument(xml);
    }
    
    // for hibernate
    public void setUniqueIdentifier(String id) {
        _uniqueID = id;
    }

}
