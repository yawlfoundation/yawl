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

package org.yawlfoundation.yawl.logging;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.engine.YEngine;
import org.yawlfoundation.yawl.engine.YPersistenceManager;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.instance.InstanceCache;
import org.yawlfoundation.yawl.engine.instance.WorkItemInstance;
import org.yawlfoundation.yawl.exceptions.YPersistenceException;
import org.yawlfoundation.yawl.logging.table.*;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

import java.util.Iterator;
import java.util.List;

/**
 * The server side of interface E. An API to retrieve data from the process event logs
 * and pass it back as XML.
 *
 * Create Date: 29/10/2007. Last Date: 12/12/2007
 * Completely revised for the new logging schema in v2.1 11/09 - 6/10
 *
 *  @author Michael Adams 
 *  @version 2.0
 */

public class YLogServer {

    private static YLogServer _me ;
    private YPersistenceManager _pmgr ;
    private static final Logger _log = Logger.getLogger(YLogServer.class);

    // some error messages
    private final String _exErrStr = "<failure>Unable to retrieve data.</failure>";
    private final String _pmErrStr = "<failure>Database connection is disabled.</failure>";
    private final String _noRowsStr = "<failure>No rows returned.</failure>";


    // CONSTRUCTOR - called from getInstance() //

    private YLogServer() {
        if (YEngine.isPersisting()) {
            _pmgr = YEngine.getPersistenceManager();
        }
    }

    public static YLogServer getInstance() {
        if (_me == null) _me = new YLogServer();
        return _me ;
    }


    public boolean startTransaction() {
        try {
            return (connected()) && _pmgr.startTransaction();
        }
        catch (YPersistenceException ype) {
            _log.error("Could not initialise connection to log tables.", ype) ;
            return false;
        }
    }


    public void commitTransaction() {
        try {
            if (connected()) _pmgr.commit();
        }
        catch (YPersistenceException ype) {
             _log.error("Could not commit after log table read.", ype) ;
        }
    }

    public YPersistenceManager getPersistenceManager() {
        return _pmgr;
    }


    private boolean connected() {
        return (_pmgr != null) && _pmgr.isEnabled();
    }


    /*****************************************************************************/

    public String getNetInstancesOfSpecification(YSpecificationID specID) {
        String result ;
        if (connected()) {
            try {
                YLogSpecification spec = getSpecification(specID);
                if (spec != null) {
                    result = getNetInstancesOfSpecification(spec.getRowKey());
                }
                else result = "<failure>No records for specification '" +
                              specID.toString() + "'.</failure>" ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result;
    }


    /**
     * @param specKey the specification PK to get the case instances for
     * @return the set of all case ids for the specID passed
     */
    public String getNetInstancesOfSpecification(long specKey) {
        String result ;
        if (connected()) {
            try {
                Iterator itr = _pmgr.createQuery(
                        "select ni from YLogNet as n, YLogNetInstance as ni " +
                        "where ni.netID=n.netID and n.specKey=:specKey")
                        .setLong("specKey", specKey).iterate();
                if (itr.hasNext()) {
                    StringBuilder xml = new StringBuilder() ;
                    xml.append(String.format("<netInstances specID=\"%d\">", specKey));
                    while (itr.hasNext()) {
                        YLogNetInstance instance = (YLogNetInstance) itr.next() ;
                        xml.append(instance.toXML()) ;
                    }
                    xml.append("</netInstances>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result ;
    }

    /*****************************************************************************/

    public String getCaseEvents(long rootNetInstanceKey) {
        String result ;
        if (connected()) {
            try {
               Iterator itr = _pmgr.createQuery("from YLogEvent as e where " +
                       "e.rootNetInstanceID=:key")
                       .setLong("key", rootNetInstanceKey)
                       .iterate();
                if (itr.hasNext()) {
                    StringBuilder xml = new StringBuilder();
                    xml.append(String.format("<events rootNetInstanceKey=\"%d\">",
                            rootNetInstanceKey));
                    while (itr.hasNext()) {
                        YLogEvent event = (YLogEvent) itr.next();
                        xml.append(event.toXML());
                    }
                    xml.append("</events>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result;
    }


    public String getCaseEvents(String caseID) {
        String result ;
        if (connected()) {
            try {
                Iterator itr = _pmgr.createQuery(
                        "from YLogNetInstance as n where n.engineInstanceID=:caseID")
                        .setString("caseID", caseID).iterate();
                if (itr.hasNext()) {
                    YLogNetInstance instance = (YLogNetInstance) itr.next();
                    result = getCaseEvents(instance.getNetInstanceID());
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result;
    }


    public String getTaskInstancesForTask(String caseID, String taskName) {
        String result ;
        if (connected()) {
            try {
                Iterator itr = _pmgr.createQuery(
                        "from YLogTaskInstance as ti, YLogTask as t " +
                        "where (ti.engineInstanceID=:caseID " +
                        "or ti.engineInstanceID like :caseIDlike) " +
                        "and ti.taskID=t.taskID " +
                        "and t.name=:taskName")
                        .setString("caseID", caseID)
                        .setString("caseIDlike", caseID + ".%")
                        .setString("taskName", taskName).iterate();
                if (itr.hasNext()) {
                    StringBuilder xml = new StringBuilder();
                    xml.append(String.format("<taskinstances caseID=\"%s\" taskname=\"%s\">",
                            caseID, taskName));
                    while (itr.hasNext()) {
                        Object[] next = (Object[]) itr.next();
                        YLogTaskInstance inst = (YLogTaskInstance) next[0];
                        xml.append(getFullyPopulatedTaskInstance(inst));
                    }
                    xml.append("</taskinstances>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result;
    }


    public String getInstanceEvents(long instanceKey) {
        String result ;
        if (connected()) {
            try {
               Iterator itr = _pmgr.createQuery("from YLogEvent as e where " +
                       "e.instanceID=:key")
                       .setLong("key", instanceKey)
                       .iterate();
                if (itr.hasNext()) {
                    StringBuilder xml = new StringBuilder();
                    xml.append(String.format("<events instanceKey=\"%d\">",
                            instanceKey));
                    while (itr.hasNext()) {
                        YLogEvent event = (YLogEvent) itr.next();
                        xml.append(event.toXML());
                    }
                    xml.append("</events>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result;
    }


    public String getDataForEvent(long eventKey) {
        String result ;
        if (connected()) {
            try {
               Iterator itr = _pmgr.createQuery("from YLogDataItemInstance as di where " +
                       "di.eventID=:key")
                       .setLong("key", eventKey)
                       .iterate();
                if (itr.hasNext()) {
                    StringBuilder xml = new StringBuilder();
                    xml.append(String.format("<dataitems eventKey=\"%d\">", eventKey));
                    while (itr.hasNext()) {
                        YLogDataItemInstance dataItem = (YLogDataItemInstance) itr.next();
                        xml.append(dataItem.toXML());
                    }
                    xml.append("</dataitems>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result;
    }


    public String getDataTypeForDataItem(long dataTypeKey) {
        String result ;
        if (connected()) {
            try {
               Iterator itr = _pmgr.createQuery("from YLogDataType as dt where " +
                       "dt.dataTypeID=:key")
                       .setLong("key", dataTypeKey)
                       .iterate();
                if (itr.hasNext()) {
                    StringBuilder xml = new StringBuilder();
                    YLogDataType dataType = (YLogDataType) itr.next();
                    xml.append(dataType.toXML());
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result;
    }


    public String getTaskInstancesForCase(String caseID) {
        String result ;
        if (connected()) {
            try {
               Iterator itr = _pmgr.createQuery("from YLogTaskInstance as ti where " +
                       "ti.engineInstanceID like :key")
                       .setString("key", caseID + ".%")
                       .iterate();
                if (itr.hasNext()) {
                    StringBuilder xml = new StringBuilder();
                    xml.append(String.format("<taskinstances caseID=\"%s\">", caseID));
                    while (itr.hasNext()) {
                        YLogTaskInstance instance = (YLogTaskInstance) itr.next();
                        xml.append(instance.toXML());
                    }
                    xml.append("</taskinstances>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result;
    }


    public String getTaskInstancesForTask(long taskKey) {
        String result ;
        if (connected()) {
            try {
               Iterator itr = _pmgr.createQuery("from YLogTaskInstance as ti where " +
                       "ti.taskID=:key")
                       .setLong("key", taskKey)
                       .iterate();
                if (itr.hasNext()) {
                    StringBuilder xml = new StringBuilder();
                    xml.append(String.format("<taskinstances key=\"%d\">", taskKey));
                    while (itr.hasNext()) {
                        YLogTaskInstance instance = (YLogTaskInstance) itr.next();
                        xml.append(instance.toXML());
                    }
                    xml.append("</taskinstances>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result;
    }



    public String getAllSpecifications() {
        String result ;
        if (connected()) {
            try {
                Iterator itr = _pmgr.execQuery("from YLogSpecification").iterator();
                if (itr.hasNext()) {
                    StringBuilder xml = new StringBuilder("<specifications>");
                    while (itr.hasNext()) {
                        YLogSpecification spec = (YLogSpecification) itr.next() ;
                        xml.append(spec.toXML());
                    }
                    xml.append("</specifications>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
                result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result ;
    }

    /***************************************************************************/


    public String getCaseEvent(String caseID, String eventType) {
        String result ;
        if (connected()) {
            try {
                Iterator itr = _pmgr.createQuery(
                        "select e from YLogEvent as e, YLogNetInstance as ni where " +
                        "ni.engineInstanceID=:caseID and e.instanceID=ni.netInstanceID " +
                        "and e.descriptor=:eventType")
                        .setString("caseID", caseID)
                        .setString("eventType", eventType)
                        .iterate();
                 if (itr.hasNext()) {
                     StringBuilder xml = new StringBuilder();
                     xml.append(String.format("<caseEvent caseID=\"%s\">", caseID));
                     while (itr.hasNext()) {
                         YLogEvent event = (YLogEvent) itr.next();
                         xml.append(event.toXML());
                     }
                     xml.append("</caseEvent>");
                     result = xml.toString();
                 }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result ;
    }


    public String getAllCaseEventsByService(String serviceName, String eventType) {
        String result ;
         if (connected()) {
             try {
                 Iterator itr = _pmgr.createQuery(
                         "select e from YLogEvent as e, YLogService as s where " +
                         "s.serviceID=e.serviceID and " +
                         "s.name=:serviceName and e.descriptor=:event")
                         .setString("serviceName", serviceName)
                         .setString("event", eventType)
                         .iterate();
                  if (itr.hasNext()) {
                      StringBuilder xml = new StringBuilder();
                      xml.append(String.format("<caseEvent serviceName=\"%s\">", serviceName));
                      while (itr.hasNext()) {
                          YLogEvent event = (YLogEvent) itr.next();
                          xml.append(event.toXML());
                      }
                      xml.append("</caseEvent>");
                      result = xml.toString();
                  }
                 else result = _noRowsStr ;
             }
             catch (YPersistenceException ype) {
                result = _exErrStr ;
             }
         }
         else result = _pmErrStr ;

         return result ;
    }


    public String getAllCasesStartedByService(String serviceName) {
        return getAllCaseEventsByService(serviceName, "CaseStart");
    }

    public String getAllCasesCancelledByService(String serviceName) {
        return getAllCaseEventsByService(serviceName, "CaseCancel");
    }


    public String getCompleteCaseLogsForSpecification(YSpecificationID specID) {
        String result ;
        if (connected()) {
            try {
                YLogSpecification spec = getSpecification(specID);
                if (spec != null) {
                    result = getCompleteCaseLogsForSpecification(spec.getRowKey());
                }
                else result = "<failure>No records for specification '" +
                              specID.toString() + "'.</failure>" ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result;
    }


    public String getSpecificationStatistics(YSpecificationID specID) {
        return getSpecificationStatistics(specID, -1, Long.MAX_VALUE);
    }

    public String getSpecificationStatistics(YSpecificationID specID, long from, long to) {
        String result ;
        if (connected()) {
            try {
                YLogSpecification spec = getSpecification(specID);
                if (spec != null) {
                    result = getSpecificationStatistics(spec.getRowKey(), from, to);
                }
                else result = "<failure>No records for specification '" +
                              specID.toString() + "'.</failure>" ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result;
    }


    public String getSpecificationStatistics(long specKey) {
        return getSpecificationStatistics(specKey, -1, Long.MAX_VALUE);
    }

    public String getSpecificationStatistics(long specKey, long from, long to) {
        String result ;
         if (connected()) {
             try {
                 YLogSpecification spec = getSpecification(specKey);
                 int casesStarted = 0;
                 int casesCompleted = 0;
                 int casesCancelled = 0;
                 long maxCompletedTime = 0;
                 long minCompletedTime = Long.MAX_VALUE;
                 double totalCompletedTime = 0;
                 long maxCancelledTime = 0;
                 long minCancelledTime = Long.MAX_VALUE;
                 double totalCancelledTime = 0;
                 if (to == -1) to = Long.MAX_VALUE;
                 List instances = getNetInstanceObjects(spec.getRootNetID());
                 for (Object o : instances) {
                     YLogNetInstance instance = (YLogNetInstance) o;
                     List events = getInstanceEventObjects(instance.getNetInstanceID());
                     long startTime = 0;
                     long completeTime = 0;
                     long cancelTime = 0;
                     for (Object obj : events) {
                         YLogEvent event = (YLogEvent) obj;
                         if ((event.getTimestamp() >= from) && (event.getTimestamp() <= to)) {
                             String eventLabel = event.getDescriptor();
                             if (eventLabel.equals("CaseStart")) {
                                 casesStarted++;
                                 startTime = event.getTimestamp();
                             }
                             else if (eventLabel.equals("CaseComplete")) {
                                 casesCompleted++;
                                 completeTime = event.getTimestamp();
                             }
                             else if (eventLabel.equals("CaseCancel")) {
                                 casesCancelled++;
                                 cancelTime = event.getTimestamp();
                             }
                         }
                     }
                     if (completeTime > 0) {
                         long expiredTime = completeTime - startTime;
                         maxCompletedTime = Math.max(maxCompletedTime, expiredTime);
                         minCompletedTime = Math.min(minCompletedTime, expiredTime);
                         totalCompletedTime += expiredTime;
                     }
                     else if (cancelTime > 0) {
                         long expiredTime = cancelTime - startTime;
                         maxCancelledTime = Math.max(maxCancelledTime, expiredTime);
                         minCancelledTime = Math.min(minCancelledTime, expiredTime);
                         totalCancelledTime += expiredTime;
                     }
                 }
                 XNode node = new XNode("specification");
                 node.addAttribute("id", spec.getUri() + " - " + spec.getVersion());
                 node.addAttribute("key", specKey);
                 node.addChild("started", casesStarted);
                 node.addChild("completed", casesCompleted);
                 node.addChild("cancelled", casesCancelled);
                 node.addChild("completionMaxtime", StringUtil.formatTime(maxCompletedTime));
                 node.addChild("completionMintime", StringUtil.formatTime(minCompletedTime));
                 node.addChild("completionAvgtime",
                         StringUtil.formatTime((long) totalCompletedTime / casesCompleted));
                 node.addChild("cancelledMaxtime", StringUtil.formatTime(maxCancelledTime));
                 node.addChild("cancelledMintime", StringUtil.formatTime(minCancelledTime));
                 node.addChild("cancelledAvgtime",
                         StringUtil.formatTime((long) totalCancelledTime / casesCancelled));
                 result = node.toString();
             }
             catch (YPersistenceException ype) {
                result = _exErrStr ;
             }
         }
         else result = _pmErrStr ;

         return result ;
     }


    public XNode getXESLog(YSpecificationID specID, boolean withData) {
        if (connected()) {
            try {
                YLogSpecification spec = getSpecification(specID);
                if (spec != null) {
                    XNode cases = new XNode("cases");
                    for (Object o : getNetInstanceObjects(spec.getRootNetID())) {
                        YLogNetInstance logNetInstance = (YLogNetInstance) o;
                        XNode caseNode = cases.addChild("case");
                        caseNode.addAttribute("id", logNetInstance.getEngineInstanceID());
                        addNetInstance(caseNode, logNetInstance, withData);
                    }
                    return cases;
                }    
            }
            catch (YPersistenceException ype) {
               _log.error(ype.getMessage(), ype);
            }
        }
        return null;
    }


    private void addNetInstance(XNode node, YLogNetInstance logNetInstance, boolean withData)
            throws YPersistenceException {

        XNode netInstance = node.addChild("netinstance");

        // for each task instance in the net
        for (Object o : getTaskInstanceObjects(logNetInstance.getNetInstanceID())) {
            YLogTaskInstance logTaskInstance = (YLogTaskInstance) o;
            YLogTask task = getTask(logTaskInstance.getTaskID());
            XNode taskInstance = netInstance.addChild("taskinstance");
            taskInstance.addChild("taskname", task.getName());

            // for each event for the task
            for (Object o1 : getInstanceEventObjects(logTaskInstance.getTaskInstanceID())) {
                YLogEvent logEvent = (YLogEvent) o1;
                String descriptor = logEvent.getDescriptor();

                // don't include data change events if withData is false
                if (withData || (! descriptor.equals("DataValueChange"))) {
                    XNode event = new XNode("event");
                    event.addChild("descriptor", descriptor);
                    event.addChild("timestamp", logEvent.getTimestampString());
                    if (descriptor.equals("DataValueChange")) {
                       event.addChild(addDataInstances(logEvent.getEventID())); 
                    }
                    taskInstance.addChild(event);
                }
            }

            // now do any sub-nets
            YLogNetInstance logSubNetInstance =
                     getSubNetInstance(logTaskInstance.getTaskInstanceID());
            if (logSubNetInstance != null) {
                addNetInstance(node, logSubNetInstance, withData);     // recurse
            }
        }
    }


    private XNode addDataInstances(long eventKey) throws YPersistenceException {
        XNode dataInstances = new XNode("dataItems");

        for (Object o : getDataItemInstanceObjects(eventKey)) {
            YLogDataItemInstance dataItemInstance = (YLogDataItemInstance) o;
            YLogDataType dataType = getDataType(dataItemInstance.getDataTypeID());
            XNode dataItem = dataInstances.addChild("dataItem");
            dataItem.addChild("descriptor", dataItemInstance.getDescriptor());
            dataItem.addChild("name", dataItemInstance.getName());
            dataItem.addChild("value", dataItemInstance.getValue());
            dataItem.addChild("typeName", dataType.getName());
            dataItem.addChild("typeDefinition", dataType.getDefinition());
        }
        return dataInstances;
    }


    public String getCompleteCaseLogsForSpecification(long specKey) {
        String result ;
         if (connected()) {
             try {
                 YLogSpecification spec = getSpecification(specKey);
                 List instances = getNetInstanceObjects(spec.getRootNetID());
                 StringBuilder xml = new StringBuilder();
                 xml.append(String.format("<cases specKey=\"%d\">", specKey));
                 for (Object o : instances) {
                     YLogNetInstance instance = (YLogNetInstance) o;
                     xml.append(getCompleteCaseLog(instance.getEngineInstanceID()));
                 }
                 xml.append("</cases>");
                 result = xml.toString();
             }
             catch (YPersistenceException ype) {
                result = _exErrStr ;
             }
         }
         else result = _pmErrStr ;

         return result ;
     }
        

    public String getCompleteCaseLog(String caseID) {
        String result ;
         if (connected()) {
             try {
                 YLogNetInstance rootNet = getNetInstance(caseID);
                 if (rootNet != null) {
                     YLogNet net = getNet(rootNet.getNetID());
                     YLogSpecification spec = getSpecification(net.getSpecKey());
                     StringBuilder xml = new StringBuilder();
                     xml.append(String.format("<case id=\"%s\">", caseID));
                     xml.append(spec.toXML());
                     xml.append(getFullyPopulatedNetInstance(rootNet, net, true));
                     xml.append("</case>");
                     result = xml.toString();
                 }
                 else result = String.format(
                         "<failure>No record of case '%s'.</failure>", caseID) ;
            }
            catch (YPersistenceException ype) {
               result = _exErrStr ;
            }
        }
        else result = _pmErrStr ;

        return result ;
    }


    public String getServiceName(long key) {
        String result;
        if (connected()) {
            try {
                YLogService service = getService(key);
                if (service != null) {
                    return StringUtil.wrap(service.getName(), "service");
                }
                else result = String.format(
                        "<failure>No record of service with key '%d'.</failure>", key) ;
           }
           catch (YPersistenceException ype) {
              result = _exErrStr ;
           }
       }
       else result = _pmErrStr ;

       return result ;

    }


    public String getEventsForTaskInstance(String itemID) {
        String result ;
        if (itemID != null) {
            try {
                YLogTaskInstance itemInstance = getTaskInstance(itemID);
                if (itemInstance != null) {
                    StringBuilder xml = new StringBuilder();
                    xml.append(String.format("<taskevents id=\"%s\">", itemID));
                    xml.append(getEventListAsXML(itemInstance.getTaskInstanceID()));

                    long parentInstanceID = itemInstance.getParentTaskInstanceID();
                    if (parentInstanceID > -1) {
                        xml.append(getEventListAsXML(parentInstanceID));
                    }
                    xml.append("</taskevents>");
                    result = xml.toString();
                }
                else result = _noRowsStr ;
            }
            catch (YPersistenceException ype) {
                result = _exErrStr ;
            }
        }
        else result = "<failure>Null item id</failure>" ;

        return result;
    }


    public String getSpecificationXESLog(YSpecificationID specid, boolean withData) {
        XNode cases = getXESLog(specid, withData);
        if (cases != null) {
            return new YXESBuilder().buildLog(specid, cases);
        }
        return "";
    }

    /**********************************************************************/

    private String getFullyPopulatedNetInstance(YLogNetInstance instance, YLogNet net,
                                         boolean root) throws YPersistenceException {
        StringBuilder xml = new StringBuilder();
        xml.append(String.format("<netinstance key=\"%d\" root=\"%b\">",
                instance.getNetInstanceID(), root));
        xml.append(String.format("<net name=\"%s\"  key=\"%d\"/>",
                net.getName(), net.getNetID()));
        for (Object o : getInstanceEventObjects(instance.getNetInstanceID())) {
            xml.append(getFullyPopulatedEvent((YLogEvent) o));
        }
        for (Object o : getTaskInstanceObjects(instance.getNetInstanceID())) {
            xml.append(getFullyPopulatedTaskInstance((YLogTaskInstance) o));
        }
        xml.append("</netinstance>");
        return xml.toString();
    }

    private String getFullyPopulatedTaskInstance(YLogTaskInstance instance)
            throws YPersistenceException {
        StringBuilder xml = new StringBuilder();
        xml.append(String.format("<taskinstance key=\"%d\">", instance.getTaskInstanceID()));
        YLogTask task = getTask(instance.getTaskID());
        String caseID = instance.getEngineInstanceID();
        xml.append(task.toXML());
        xml.append(StringUtil.wrap(caseID, "caseid"));
        for (Object o : getInstanceEventObjects(instance.getTaskInstanceID())) {
            xml.append(getFullyPopulatedEvent((YLogEvent) o));
        }

        // recurse for sub-nets
        if (task.getChildNetID() > -1) {
            YLogNet childNet = getNet(task.getChildNetID());
            YLogNetInstance childInstance = getNetInstance(caseID);
            xml.append(getFullyPopulatedNetInstance(childInstance, childNet, false));
        }
        xml.append("</taskinstance>");
        return xml.toString();
    }

    private String getFullyPopulatedEvent(YLogEvent event) throws YPersistenceException {
        StringBuilder xml = new StringBuilder();
        xml.append(String.format("<event key=\"%d\">", event.getEventID()));
        xml.append(StringUtil.wrap(event.getDescriptor(), "descriptor"));
        xml.append(StringUtil.wrap(String.valueOf(event.getTimestamp()), "timestamp"));
        if (event.getServiceID() > -1) {
            YLogService service = getService(event.getServiceID());
            xml.append(service.toXML());
        }
        List dataItemInstances = getDataItemInstanceObjects(event.getEventID());
        for (Object o : dataItemInstances) {
            xml.append(getFullyPopulatedDataItem((YLogDataItemInstance) o));
        }
        xml.append("</event>");
        return xml.toString();
    }


    private String getFullyPopulatedDataItem(YLogDataItemInstance instance)
            throws YPersistenceException {
        StringBuilder xml = new StringBuilder(150);
        xml.append(String.format("<dataitem key=\"%d\">", instance.getDataItemID()));
        xml.append(instance.getDataItem().toXMLShort());
        YLogDataType dataType = getDataType(instance.getDataTypeID());
        xml.append(dataType.toXML());
        xml.append("</dataitem>");
        return xml.toString();
    }


    private String getEventListAsXML(long instanceID) throws YPersistenceException {
        StringBuilder xml = new StringBuilder();
        List itemEvents = getInstanceEventObjects(instanceID);
        for (Object o : itemEvents) {
            YLogEvent event = (YLogEvent) o;
            xml.append(event.toXML());
        }
        return xml.toString();
    }


    private YLogSpecification getSpecification(long key) throws YPersistenceException {
        if (connected()) {
            return (YLogSpecification) _pmgr.selectScalar("YLogSpecification", "rowKey", key);
        }
        else return null;
    }

    private YLogSpecification getSpecification(YSpecificationID specID)
            throws YPersistenceException {
        if (connected()) {
            String identifier = specID.getIdentifier();
            Iterator itr ;
            if (identifier != null) {
                itr = _pmgr.createQuery("from YLogSpecification as s where " +
                      "s.identifier=:id and s.version=:version and s.uri=:uri")
                      .setString("id", identifier)
                      .setString("version", specID.getVersionAsString())
                      .setString("uri", specID.getUri())
                      .iterate();
            }
            else {
                itr = _pmgr.createQuery("from YLogSpecification as s where " +
                      "s.version=:version and s.uri=:uri")
                      .setString("version", specID.getVersionAsString())
                      .setString("uri", specID.getUri())
                      .iterate();
            }
            if (itr.hasNext()) {
                return (YLogSpecification) itr.next();
            }
        }
        return null;
    }


    private YLogNet getNet(long key) throws YPersistenceException {
        if (connected()) {
            return (YLogNet) _pmgr.selectScalar("YLogNet", "netID", key);
        }
        else return null;
    }


    private List getNets(long specKey) throws YPersistenceException {
        if (connected()) {
            return _pmgr.createQuery("from YLogNet as n where n.specKey=:specKey")
                    .setLong("specKey", specKey)
                    .list();
        }
        else return null;
    }


    private YLogNetInstance getNetInstance(String caseID) throws YPersistenceException {
        if (connected()) {
            String quotedCaseID = String.format("'%s'", caseID);
            return (YLogNetInstance) _pmgr.selectScalar(
                    "YLogNetInstance", "engineInstanceID", quotedCaseID);
        }
        else return null;
    }

    private YLogNetInstance getNetInstance(long key) throws YPersistenceException {
        if (connected()) {
            return (YLogNetInstance) _pmgr.selectScalar(
                    "YLogNetInstance", "netID", key);
        }
        else return null;
    }

    private YLogNetInstance getSubNetInstance(long key) throws YPersistenceException {
        if (connected()) {
            return (YLogNetInstance) _pmgr.selectScalar(
                    "YLogNetInstance", "parentTaskInstanceID", key);
        }
        else return null;
    }


    private List getNetInstances(String caseID) throws YPersistenceException {
        if (connected()) {
            return _pmgr.createQuery("from YLogNetInstance as ni where " +
                "ni.engineInstanceID=:caseid or ni.engineInstanceID like :likeid")
                .setString("caseid", caseID)
                .setString("likeid", caseID + ".%")
                .list();
        }
        else return null;
    }




    private YLogTaskInstance getTaskInstance(String itemID) throws YPersistenceException {
        if ((itemID != null) && (connected())) {
            String caseID = itemID.split(":")[0];
            String taskName = getTaskNameForWorkItem(caseID, itemID);
            if (taskName != null) {
                List result = _pmgr.createQuery(
                        "from YLogTaskInstance as ti, YLogTask as t where ti.engineInstanceID=:caseID" +
                        " and t.name=:taskName and t.taskID=ti.taskID")
                           .setString("caseID", caseID)
                           .setString("taskName", taskName)
                           .list();

                if (! result.isEmpty()) {
                    Object[] rows = (Object[]) result.get(0);
                    return (YLogTaskInstance) rows[0];
                }    
            }
        }
        return null;
    }

    private YLogTaskInstance getTaskInstance(long key) throws YPersistenceException {
        if (connected()) {
             return (YLogTaskInstance) _pmgr.selectScalar(
                    "YLogTaskInstance", "taskInstanceID", key);
        }
        else return null;
    }

    private YLogTask getTask(long key) throws YPersistenceException {
        if (connected()) {
            return (YLogTask) _pmgr.selectScalar("YLogTask", "taskID", key);
        }
        else return null;
    }

    private YLogService getService(long key) throws YPersistenceException {
        if (connected()) {
            return (YLogService) _pmgr.selectScalar("YLogService", "serviceID", key);
        }
        else return null;
    }

    private YLogDataType getDataType(long key) throws YPersistenceException {
        if (connected()) {
            return (YLogDataType) _pmgr.selectScalar("YLogDataType", "dataTypeID", key);
        }
        else return null;
    }

    private List getInstanceEventObjects(long key) throws YPersistenceException {
        if (connected()) {
            return _pmgr.createQuery("from YLogEvent as e where e.instanceID=:key")
                       .setLong("key", key)
                       .list();
        }
        else return null;
    }

    private List getTaskInstanceObjects(long key) throws YPersistenceException {
        if (connected()) {
            return _pmgr.createQuery(
                    "from YLogTaskInstance as ti where ti.parentNetInstanceID=:key")
                       .setLong("key", key)
                       .list();
        }
        else return null;
    }

    private List getNetInstanceObjects(long key) throws YPersistenceException {
        if (connected()) {
            return _pmgr.createQuery("from YLogNetInstance as ni where ni.netID=:key")
                       .setLong("key", key)
                       .list();
        }
        else return null;
    }

    private List getDataItemInstanceObjects(long key) throws YPersistenceException {
        if (connected()) {
            return _pmgr.createQuery("from YLogDataItemInstance as di where di.eventID=:key")
                       .setLong("key", key)
                       .list();
        }
        else return null;
    }


    private String getTaskNameForWorkItem(String caseID, String itemID) {
        String rootCaseID = caseID.contains(".") ? caseID.substring(0, caseID.indexOf(".")) : caseID;
        InstanceCache instanceCache = YEngine.getInstance().getInstanceCache();
        WorkItemInstance itemInstance = instanceCache.getWorkItemInstance(rootCaseID, itemID);
        return (itemInstance != null) ? itemInstance.getTaskName() : null;
    }


}
