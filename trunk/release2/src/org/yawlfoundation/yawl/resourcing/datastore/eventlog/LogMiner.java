/*
 * Copyright (c) 2004-2010 The YAWL Foundation. All rights reserved.
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
import java.util.Set;
import java.util.TreeSet;

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
    private static final String _exErrStr = "<failure>Unable to retrieve data.</failure>";
    private static final String _pmErrStr = "<failure>Error connecting to database.</failure>";
    private static final String _noRowsStr = "<failure>No rows returned.</failure>";
    private static final String _badKeyStr = "<failure>Invalid specification key.</failure>";

    private static final String _baseQuery = "FROM ResourceEvent AS re";


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


    public String getCaseEvents(String caseID) {
        List events = getCaseEventsList(caseID);
        return (! events.isEmpty()) ? eventListToXML(events) : _noRowsStr;
    }


    public String getCaseEvents(String caseID, long from, long to) {
        if ((from < 0) && (to < 0)) return getCaseEvents(caseID);
        List events = getCaseEventsList(caseID, from, to);
        return (! events.isEmpty()) ? eventListToXML(events) : _noRowsStr;
    }


    public String getCaseEvent(String caseID, boolean launch) {
        EventLogger.event eventType = launch ? EventLogger.event.launch_case :
                EventLogger.event.cancel_case;
        ResourceEvent event = getCaseEvent(caseID, eventType);
        return (event != null) ? event.toXML() : _noRowsStr;
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


    public String getWorkItemEvents(String itemID, boolean fullName, long from, long to) {
        if ((from < 0) && (to < 0)) return getWorkItemEvents(itemID, fullName);
        List events = getWorkItemEventsList(itemID, from, to);
        if (events != null) {
            if (containsStartEvent(events)) {
                String parentID = deriveParentID(itemID);
                List parentEvents = getWorkItemEventsList(parentID, from, to);
                if (parentEvents != null) {
                    events.addAll(parentEvents);
                }
            }
            if (fullName) events = replaceParticipantIDsWithNames(events);
        }
        return (events != null) ? eventListToXML(events) : _noRowsStr;
    }


    public String getCaseStartedBy(String caseID) {
        ResourceEvent event = getCaseEvent(caseID, EventLogger.event.launch_case);
        if (event != null) {
            return getParticipantName(getFieldValue(event.toXML(), "participantid"));
        }
        else return "Unavailable"; 
    }


    public String getParticipantHistory(String pid) {
        return getResourceHistory(pid);
    }


    public String getParticipantHistory(String pid, long from, long to) {
        return ((from < 0) && (to < 0)) ? getResourceHistory(pid) :
                getResourceHistory(pid, from, to);
    }


    public String getParticipantHistoryForEvent(String pid, EventLogger.event eventType) {
        return getResourceHistoryForEvent(pid, eventType);
    }


    public String getParticipantHistoryForEvent(String pid, EventLogger.event eventType,
                                                long from, long to) {
         return getResourceHistoryForEvent(pid, eventType, from, to);
     }


    public String getResourceHistory(String id) {
        List events = getResourceEventsList(id);
        return (events != null) ? eventListToXML(events) : _noRowsStr;
    }


    public String getResourceHistory(String id, long from, long to) {
        if ((from < 0) && (to < 0)) return getResourceHistory(id);
        List events = getResourceEventsList(id, from, to);
        return (events != null) ? eventListToXML(events) : _noRowsStr;
    }


    public String getResourceHistoryForEvent(String id, EventLogger.event eventType) {
        List events = getResourceEventsList(id);
        return getExtractedEvents(events, eventType);
    }


    public String getResourceHistoryForEvent(String id, EventLogger.event eventType,
                                             long from, long to) {
        List events = getResourceEventsList(id, from, to);
        return getExtractedEvents(events, eventType);
    }


    public String getWorkItemOffered(String itemID) {
        List events = getWorkItemEventsList(itemID);
        if (events != null) {
            List<ResourceEvent> offered = extractEvents(events, EventLogger.event.offer);
            return (offered != null) ? eventListToXML(offered) : _noRowsStr;
        }
        return _noRowsStr;
    }


    public String getWorkItemAllocated(String itemID) {
        ResourceEvent event = getWorkItemEvent(itemID, EventLogger.event.allocate);
        return (event != null) ? event.toXML() : _noRowsStr;
    }

    
    public String getWorkItemStarted(String itemID) {
        ResourceEvent event = getWorkItemEvent(itemID, EventLogger.event.start);
        return (event != null) ? event.toXML() : _noRowsStr;
    }


    public String getCaseHistoryInvolvingParticipant(String pid) {
        return getCaseHistoryForParticipantEvents(getResourceEventsList(pid));
    }


    public String getCaseHistoryInvolvingParticipant(String pid, long from, long to) {
        return ((from < 0) && (to < 0)) ? getCaseHistoryInvolvingParticipant(pid) :
                getCaseHistoryForParticipantEvents(getResourceEventsList(pid, from, to));
    }


    public String getSpecificationEvents(Set<YSpecificationID> specIDs) {
        StringBuilder s = new StringBuilder("<SpecificationEvents>");
        for (YSpecificationID specID : specIDs) {
            List events = getSpecificationEvents(getSpecificationKey(specID));
            if (events != null) s.append(formatSpecificationEvents(specID, events));
        }
        s.append("</SpecificationEvents>");
        return s.toString();
    }


    public String getSpecificationEvents(Set<YSpecificationID> specIDs, long from, long to) {
        if ((from < 0) && (to < 0)) return getSpecificationEvents(specIDs);
        StringBuilder s = new StringBuilder("<SpecificationEvents>");
        for (YSpecificationID specID : specIDs) {
            List events = getSpecificationEvents(getSpecificationKey(specID), from, to);
            if (events != null) s.append(formatSpecificationEvents(specID, events));
        }
        s.append("</SpecificationEvents>");
        return s.toString();
    }


    public String getSpecificationEvents(YSpecificationID specID) {
        List events = getSpecificationEvents(getSpecificationKey(specID));
        return (events != null) ? eventListToXML(events) : _noRowsStr;        
    }


    public String getSpecificationEvents(YSpecificationID specID, long from, long to) {
        if ((from < 0) && (to < 0)) return getSpecificationEvents(specID);
        List events = getSpecificationEvents(getSpecificationKey(specID), from, to);
        return (events != null) ? eventListToXML(events) : _noRowsStr;
    }


    public String getSpecificationIdentifiers(String keyStr) {
        try {
            long key = new Long(keyStr);
            SpecLog spec = getSpecLogRecord(key);
            return (spec != null) ? spec.getSpecID().toXML() : _noRowsStr;
        }
        catch (NumberFormatException nfe) {
            return _badKeyStr; 
        }
    }


    public String getSpecificationXESLog(YSpecificationID specid) {
        XNode cases = getXESLog(specid);
        if (cases != null) {
            return new ResourceXESLog().buildLog(specid, cases);
        }
        return "";
    }


    public String getMergedXESLog(YSpecificationID specid) {
        return getMergedXESLog(specid, false);
    }


    public String getMergedXESLog(YSpecificationID specid, boolean withData) {
        XNode rsCases = getXESLog(specid);
        String engCases = ResourceManager.getInstance().getEngineXESLog(specid, withData);
        if ((rsCases != null) && (engCases != null)) {
            return new ResourceXESLog().mergeLogs(rsCases, engCases);
        }
        return "";
    }

    
    public String getSpecificationStatistics(YSpecificationID specID) {
        return ResourceManager.getInstance().getEngineSpecificationStatistics(specID);
    }

    /*****************************************************************************/

    private List execQuery(String query) {
        List rows = null;
        if (_reader != null) {
            rows = _reader.execQuery(query) ;
        }
        return rows;

    }


    private String getWhereQuery(String field, String value) {
        return String.format("%s WHERE re.%s='%s'", _baseQuery, field, value);
    }


    private String getWhereQuery(String field, long value) {
        return String.format("%s WHERE re.%s=%d", _baseQuery, field, value);
    }


    private List getWorkItemEventsList(String itemID) {
        return execQuery(getWhereQuery("_itemID", itemID));
    }


    private List getWorkItemEventsList(String itemID, long from, long to) {
        String query = getWhereQuery("_itemID", itemID) + getTimeRangeSubclause(from, to);
        return execQuery(query) ;
    }


    private List getSpecificationEvents(long specKey) {
        return (specKey == -1) ? null :
                execQuery(getWhereQuery("_specKey", specKey));
    }
    

    private List getSpecificationEvents(long specKey, long from, long to) {
        return (specKey == -1) ? null :
                execQuery(getWhereQuery("_specKey", specKey) +
                          getTimeRangeSubclause(from, to));
    }


    private List getCaseEventsList(String caseID) {
        String query = String.format(
                "%s WHERE re._caseID='%s' OR re._caseID LIKE '%s%s'",
                _baseQuery, caseID, caseID, ".%");
        return execQuery(query) ;
    }


    private List getCaseEventsList(String caseID, long from, long to) {
        String query = String.format(
                "%s WHERE re._caseID='%s' OR re._caseID LIKE '%s%s'",
                _baseQuery, caseID, caseID, ".%") + getTimeRangeSubclause(from, to);
        return execQuery(query) ;
    }

    
    private List getResourceEventsList(String resourceID) {
        return execQuery(getWhereQuery("_participantID", resourceID)) ;
    }


    private List getResourceEventsList(String resourceID, long from, long to) {
        String query = getWhereQuery("_participantID", resourceID) +
                getTimeRangeSubclause(from, to);
        return execQuery(query) ;
    }


    private SpecLog getSpecLogRecord(long key) {
        if (_reader != null) {
            String query = String.format("FROM SpecLog AS sl WHERE sl.logID=%d", key);
            List rows = _reader.execQuery(query) ;
            if ((rows != null) && (! rows.isEmpty())) {
                return (SpecLog) rows.get(0);
            }
        }
        return null;
    }


    private String getTimeRangeSubclause(long from, long to) {
        String subclause = "";
        if (from > 0) {
            subclause = " AND re._timeStamp >= " + from;
        }
        if (to > 0) {
            subclause = " AND re._timeStamp <= " + to;
        }
        return subclause;
    }


    private boolean containsStartEvent(List events) {
        return containsEvent(events, EventLogger.event.start);
    }


    private boolean containsEvent(List events, EventLogger.event eventType) {
        for (Object o : events) {
            ResourceEvent event = (ResourceEvent) o;
            if (event.get_event().equals(eventType.name())) {
               return true;
            }
        }
        return false;
    }


    private List<ResourceEvent> extractEvents(List events, EventLogger.event eventType) {
        List<ResourceEvent> extracted = new ArrayList<ResourceEvent>();
        for (Object o : events) {
            ResourceEvent event = (ResourceEvent) o;
            if (event.get_event().equals(eventType.name())) {
               extracted.add(event);
            }
        }
        return extracted;
    }


    private String getCaseHistoryForParticipantEvents(List participantEvents) {
        List allEvents = new ArrayList();
        if (participantEvents != null) {

            // get set of cases involving this participant
            Set<String> caseIDs = new TreeSet<String>();
            for (Object o : participantEvents) {
                ResourceEvent event = (ResourceEvent) o;
                caseIDs.add(getRootCaseID(event.get_caseID()));
            }

            for (String caseID : caseIDs) {
                 allEvents.addAll(getCaseEventsList(caseID));
            }
        }
        return (! allEvents.isEmpty()) ? eventListToXML(allEvents) : _noRowsStr;
    }


    private String formatSpecificationEvents(YSpecificationID specID, List events) {
        StringBuilder s = new StringBuilder("<specification id=\"");
        s.append(specID.toString()).append("\">");
        if (events != null) s.append(eventListToXML(events));
        s.append("</specification>");
        return s.toString();
    }





    private String getExtractedEvents(List events, EventLogger.event eventType) {
        if (events != null) {
            List<ResourceEvent> extracted = extractEvents(events, eventType);
            return (extracted != null) ? eventListToXML(extracted) : _noRowsStr;
        }
        return _noRowsStr;

    }

    /**
     * @param caseID the case id to get the event for
     * @param eventType which event to get
     * @return the case event
     */
    private ResourceEvent getCaseEvent(String caseID, EventLogger.event eventType) {
        String query = String.format(
                "%s WHERE re._caseID='%s' AND re._event='%s'",
                _baseQuery, caseID, eventType.name());
        return execScalarQuery(query);
    }


    private ResourceEvent getWorkItemEvent(String itemID, EventLogger.event eventType) {
        String query = String.format(
                "%s WHERE re._itemID='%s' AND re._event='%s'",
                _baseQuery, itemID, eventType.name());
        return execScalarQuery(query);
    }


    private ResourceEvent execScalarQuery(String query) {
        if (_reader != null) {
            List rows = _reader.execQuery(query) ;
            if ((rows != null) && (! rows.isEmpty())) {
                return (ResourceEvent) rows.get(0);
            }
        }
        return null;
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
            String query = String.format(
                  "%s WHERE re._specKey=%d ORDER BY re._caseID, re._taskID, re._timeStamp",
                    _baseQuery, specKey);

            List rows = _reader.execQuery(query) ;
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


    private String getRootCaseID(String caseID) {
        return caseID.contains(".") ? caseID.substring(0, caseID.indexOf('.')) : caseID;
    }
    

    private boolean successful(String s) {
        return (s != null) && (! s.startsWith("<fail"));
    }

}