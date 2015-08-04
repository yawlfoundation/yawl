/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */

package org.yawlfoundation.yawl.resourcing.datastore.eventlog;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.datastore.persistence.Persister;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.util.ArrayList;
import java.util.List;

/**
 * An API to retrieve data from the resource service's event logs
 * and pass it back as XML.
 *
 * Create Date: 16/12/2008
 *
 *  @author Michael Adams
 *  @version 2.0
 */

public class LogMiner {

    private static LogMiner _me ;
    private Persister _reader ;
    private static final Logger _log = Logger.getLogger(LogMiner.class);

    // some error messages
    private final String _exErrStr = "<failure>Unable to retrieve data.</failure>";
    private final String _pmErrStr = "<failure>Error connecting to database.</failure>";
    private final String _noRowsStr = "<failure>No rows returned.</failure>";


    // CONSTRUCTOR - called from getInstance() //

    private LogMiner() {
        _reader = Persister.getInstance();
    }

    public static LogMiner getInstance() {
        if (_me == null) _me = new LogMiner();
        return _me ;
    }


    /*****************************************************************************/

    /**
     * @param specID the specification id to get the case eventids for
     * @return the set of all case ids for the specID passed
     */
    public String getWorkItemDurationsForParticipant(YSpecificationID specID,
                                                     String taskName, String participantID) {
        String result ;
        List rows ;
        if (_reader != null) {
            long specKey = getSpecificationKey(specID);
            StringBuilder template = new StringBuilder("FROM ResourceEvent AS re ");
            template.append("WHERE re._taskID='%s' ")
                    .append("AND re._specKey=%d ")
                    .append("AND re._participantID='%s' ")
                    .append("ORDER BY re._itemID, re._timeStamp");

            String query = String.format(template.toString(), taskName, specKey, participantID);

            rows = _reader.execQuery(query) ;
            if (rows != null) {
                StringBuilder xml = new StringBuilder() ;
                String currentItemID = "";
                xml.append(String.format(
                        "<workitems specID=\"%s\" taskName=\"%s\" participantID=\"%s\">",
                        specID.toString(), taskName, participantID));
                for (Object o : rows) {
                    ResourceEvent event = (ResourceEvent) o ;
                    if (! event.get_itemID().equals(currentItemID)) {
                        if (! "".equals(currentItemID)) {
                            xml.append("</workitem>");
                        }
                        currentItemID = event.get_itemID();
                        xml.append(String.format("<workitem ID=\"%s\">", currentItemID));
                    }
                    xml.append("<event>");
                    xml.append(StringUtil.wrap(event.get_event(), "type")) ;
                    xml.append(StringUtil.wrap(String.valueOf(event.get_timeStamp()), "time")) ;
                    xml.append("</event>");
                }
                xml.append("</workitem></workitems>");
                result = xml.toString();
            }
            else result = _noRowsStr ;

        }
        else result = _pmErrStr ;

        return result ;
    }

    /*****************************************************************************/

    /**
     * @param caseID the case id to get the event for
     * @param launch true for launch, false for cancel
     * @return the case event
     */
    public String getCaseEvent(String caseID, boolean launch) {
        String result ;
        List rows ;
        if (_reader != null) {
            String template = "FROM ResourceEvent AS re WHERE re._caseID='%s' AND re._event='%s'";
            String query = String.format(template, caseID, launch ? "launch_case" : "cancel_case");

            rows = _reader.execQuery(query) ;
            result = (rows != null) ? eventListToXML(rows) : _noRowsStr;
        }
        else result = _pmErrStr ;

        return result ;
    }


    public String getWorkItemEvents(String itemID, boolean fullName) {
        List events = getWorkItemEventsList(itemID);
        if (events != null) {
            if (containsStartEvent(events)) {
                String parentID = deriveParentID(itemID);
                List parentEvents = getWorkItemEventsList(parentID);
                if (parentEvents != null) {
                    events.addAll(parentEvents);
                }
            }
            if (fullName) events = replaceParticipantIDsWithNames(events);
        }
        return (events != null) ? eventListToXML(events) : _noRowsStr;
    }


    public String getCaseStartedBy(String caseID) {
        String caseEvent = getCaseEvent(caseID, true);
        if (successful(caseEvent)) {
            return getParticipantName(getFieldValue(caseEvent, "participantid"));
        }
        else return "Unavailable"; 
    }


    public String getSpecificationXESLog(YSpecificationID specid) {
        XNode cases = getXESLog(specid);
        if (cases != null) {
            return new ResourceXESLog().buildLog(specid, cases);
        }
        return "";
    }

    
    public String getMergedXESLog(YSpecificationID specid) {
        XNode rsCases = getXESLog(specid);
        String engCases = ResourceManager.getInstance().getEngineXESLog(specid);
        if ((rsCases != null) && (engCases != null)) {
            return new ResourceXESLog().mergeLogs(rsCases, engCases);
        }
        return "";
    }


    /*****************************************************************************/

    private List getWorkItemEventsList(String itemID) {
        List rows = null;
        if (_reader != null) {
            String query = String.format("FROM ResourceEvent AS re WHERE re._itemID='%s'",
                    itemID);
            rows = _reader.execQuery(query) ;
        }
        return rows;
    }


    private boolean containsStartEvent(List events) {
        for (Object o : events) {
            ResourceEvent event = (ResourceEvent) o;
            if (event.get_event().equals(EventLogger.event.start.name())) {
               return true;
            }
        }
        return false;
    }


    private String deriveParentID(String itemID) {
        String[] parts = itemID.split(":");
        String caseID = parts[0].substring(0, parts[0].lastIndexOf("."));
        return caseID + ":" + parts[1];
    }


    private List<BaseEvent> replaceParticipantIDsWithNames(List events) {
        List<BaseEvent> cloneList = new ArrayList<BaseEvent>();
        for (Object o : events) {
            ResourceEvent event = ((ResourceEvent) o).clone();
            event.set_participantID(getParticipantName(event.get_participantID()));
            cloneList.add(event) ;
        }
        return cloneList;
    }


    private String getParticipantName(String pid) {
        String name = "Unavailable" ;
        if (pid != null) {
            if (! pid.equals("admin")) {
                Participant p = ResourceManager.getInstance().getOrgDataSet().getParticipant(pid);
                if (p != null) name = p.getFullName();
            }
            else name = "admin" ;
        }
        return name;
    }


    private String eventListToXML(List rows) {
        StringBuilder xml = new StringBuilder("<events>") ;
        for (Object o : rows) {
            BaseEvent event = (BaseEvent) o ;
            xml.append(event.toXML());
        }
        xml.append("</events>");
        return xml.toString();
    }


    private String getFieldValue(String caseEventXML, String fieldname) {
        Element eventElem = JDOMUtil.stringToElement(caseEventXML);
        if (eventElem != null) {
            Element firstEvent = eventElem.getChild("event");
            if (firstEvent != null) {
                return firstEvent.getChildText(fieldname);
            }
        }
        return null;
    }


    private long getSpecificationKey(YSpecificationID specID) {
        return EventLogger.getSpecificationKey(specID);
    }


    private XNode getXESLog(YSpecificationID specID) {
        XNode cases = new XNode("cases");
        XNode caseNode = null;
        long specKey = getSpecificationKey(specID);
        if (specKey > -1) {
            StringBuilder query = new StringBuilder(100);
            query.append("FROM ResourceEvent AS re WHERE re._specKey=")
                    .append(specKey)
                    .append(" ORDER BY re._caseID, re._taskID, re._timeStamp");

            List rows = _reader.execQuery(query.toString()) ;
            String caseID = "-1";
            for (Object row : rows) {
                ResourceEvent event = (ResourceEvent) row;
                if (! sameCase(event.get_caseID(), caseID)) {
                    caseID = event.get_caseID();
                    caseNode = cases.addChild("case");
                    caseNode.addAttribute("id", caseID);
                }
                if (event.get_taskID() != null) {            // only want task events

                    XNode eventNode = caseNode.addChild("event");
                    eventNode.addChild("taskname", event.get_taskID());
                    eventNode.addChild("descriptor", event.get_event());
                    eventNode.addChild("timestamp", event.getTimeStampString());
                    eventNode.addChild("resource", event.get_participantID());
                }    
            }
        }
        return cases;
    }

    private boolean sameCase(String eventCaseID, String currentCaseID) {
        return eventCaseID.equals(currentCaseID) ||
               eventCaseID.startsWith(currentCaseID + ".");
    }

    

    private boolean successful(String s) {
        return (s != null) && (! s.startsWith("<fail"));
    }

}