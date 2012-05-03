/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.scheduling;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayException;
import org.yawlfoundation.yawl.scheduling.persistence.DataMapper;
import org.yawlfoundation.yawl.scheduling.resource.ResourceServiceInterface;
import org.yawlfoundation.yawl.scheduling.timer.JobTimer;
import org.yawlfoundation.yawl.scheduling.util.PropertyReader;
import org.yawlfoundation.yawl.scheduling.util.Utils;
import org.yawlfoundation.yawl.scheduling.util.XMLUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;


/**
 * Bridge between Scheduling Service and the YAWL engine; has to be registered
 * as YAWL service; checks out resource utilisation tasks, i.e. SOU and EOU
 *
 * @author tbe
 * @version $Id: SchedulingService.java 23027 2010-10-22 14:02:53Z tbe $
 */
public class SchedulingService extends Service {

    private enum HistoricalMode { most, least, average, median }

    private ResourceServiceInterface _rs;
    private Scheduler _scheduler;
    private PlanningGraphCreator _pgc;
    private Map<String, List<String>> _allActivityTypes = new HashMap<String, List<String>>();
    private Map<String, Map<String, Long>> _errorRUPs = new HashMap<String, Map<String, Long>>();
    private long _lastSaveInDB = 0;
    private String _lastSaveInDBMsg = "";

    private static final Logger _log = Logger.getLogger(SchedulingService.class);
    private static SchedulingService INSTANCE;


    private SchedulingService() {
        super();
        _log.info("SchedulingService starting...");
        _dataMapper = new DataMapper();
        _rs = ResourceServiceInterface.getInstance();
        _pgc = PlanningGraphCreator.getInstance();
        _scheduler = new Scheduler();
    }


    public static SchedulingService getInstance() {
        if (INSTANCE == null) INSTANCE = new SchedulingService();
        return INSTANCE;
    }


    public long getLastSaveTime() { return _lastSaveInDB; }

    public String getLastSaveMsg() { return _lastSaveInDBMsg; }


    /**
     * process work item depending on task type check back into the engine remove
     * the mapping for successful processed work items updates the mapping
     * continuously
     *
     * @param mapping : contained work item is child
     */
    protected void processMappingChild(Mapping mapping) throws Exception {

        // process work item
        if (mapping.getWorkItemStatus().equals(Mapping.WORKITEM_STATUS_CACHED)) {
            Element dataList = getDataListFromWorkItem(mapping);
            Element elem = (Element) dataList.getChildren().get(0);
            _log.debug("elem:\r\n" + Utils.element2String(elem, true));

            String taskType = elem.getName();
            if (taskType.equals(XML_UTILISATION) || taskType.equals(XML_RESCHEDULING)) {
                handleUtilisationTask(mapping, elem, false);
            }
            else {
                throw new SchedulingException("Handling of data type '" +
                        taskType + "' is not implemented");
            }
        }
    }


    private void updateRup(String caseID) {
        _dataMapper.updateRup(caseID, false);
    }


    @Override
    public void handleCancelledCaseEvent(String caseID) {
        super.handleCancelledCaseEvent(caseID);
        updateRup(caseID);
    }


    @Override
    public void handleCompleteCaseEvent(String caseID, String casedata) {
        super.handleCompleteCaseEvent(caseID, casedata);
        updateRup(caseID);
    }


    /**
     * check utilisation relations of rup
     *
     * @param rup
     * @throws JDOMException
     */
    public void checkRelations(Document rup) throws JDOMException {
        _log.debug("checkRelations, rup: " + Utils.element2String(rup.getRootElement(), true));
        String xpath = XMLUtils.getXPATH_Activities();
        List<Element> activities = XMLUtils.getXMLObjects(rup, xpath);
        for (Element activity : activities) {
            Date from = XMLUtils.getDateValue(activity.getChild(XML_FROM), true);
            Date to = XMLUtils.getDateValue(activity.getChild(XML_TO), true);
            if (from != null && to != null && from.after(to)) {
                XMLUtils.addErrorValue(activity.getChild(XML_TO), true, "To msgBefore From");
            }

            // validate utilisations against scheduling service
            List<Element> utilisations = activity.getChildren(XML_UTILISATIONREL);
            for (Element utilisation : utilisations) {
                checkRelation(from, to, utilisation, rup);
            }
        }
    }


    private long getTime(Date d) { return d == null ? 0 : d.getTime(); }

    /**
     * Check related utilisation elements
     *
     * @param from
     * @param to
     * @param utilisation
     * @param rup
     * @throws JDOMException
     */
    private void checkRelation(Date from, Date to, Element utilisation, Document rup)
            throws JDOMException {
        Date relatedFrom = null;
        Date relatedTo = null;
        long thisTime = 0;
        long relatedTime = 0;

        String thisUtilisationType = utilisation.getChild(XML_THISUTILISATIONTYPE).getText();
        if (thisUtilisationType.equals(UTILISATION_TYPE_BEGIN)) {
            thisTime = getTime(from);
        }
        else if (thisUtilisationType.equals(UTILISATION_TYPE_END)) {
            thisTime = getTime(to);
        }

        String relatedUtilisationType = utilisation.getChild(XML_OTHERUTILISATIONTYPE).getText();

        Element relatedActivityElem = utilisation.getChild(XML_OTHERACTIVITYNAME);
        String relatedActivityName = relatedActivityElem.getText();
        List<Element> relatedActivities = XMLUtils.getXMLObjects(rup,
                XMLUtils.getXPATH_Activities(relatedActivityName));
        if (relatedActivities.size() != 1) {
            XMLUtils.addErrorValue(relatedActivityElem, true, "msgUnknownValue");
        }
        else {
            relatedFrom = XMLUtils.getDateValue(relatedActivities.get(0).getChild(XML_FROM), true);
            relatedTo = XMLUtils.getDateValue(relatedActivities.get(0).getChild(XML_TO), true);
        }

        if (relatedUtilisationType.equals(UTILISATION_TYPE_BEGIN)) {
            relatedTime = getTime(relatedFrom);
        }
        else if (relatedUtilisationType.equals(UTILISATION_TYPE_END)) {
            relatedTime = getTime(relatedTo);
        }

        // no time given -> no check possible
        if (thisTime == 0 || relatedTime == 0) {
            return;
        }

        // check in the past, but show warning only
        String msgType = XML_ERROR;
        Date now = new Date();
        if ((thisTime < now.getTime() && relatedTime < now.getTime())) {
            msgType = XML_WARNING;
        }

        Integer min = XMLUtils.getDurationValueInMinutes(utilisation.getChild(XML_MIN), true);
        Integer max = XMLUtils.getDurationValueInMinutes(utilisation.getChild(XML_MAX), true);

        long diffMinutes = (relatedTime - thisTime) / 1000 / 60;
        if (min != null && diffMinutes < min) {
            XMLUtils.addAttributeValue(utilisation, msgType, "msgDistanceToActivityShorterAsDefined",
                    Long.toString(diffMinutes), relatedActivityElem.getText(), min.toString());
        }
        else if (max != null && diffMinutes > max) {
            XMLUtils.addAttributeValue(utilisation, msgType, "msgDistanceToActivityLongerAsDefined",
                    Long.toString(diffMinutes), relatedActivityElem.getText(), max.toString());
        }
    }


    /**
     * @param caseId
     * @param savedBy
     * @param rup
     * @throws JDOMException
     * @throws SQLException
     * @throws IOException
     */
    public void saveRupToDatabase(String caseId, String savedBy, Document rup, String msg)
            throws JDOMException, SQLException, IOException {

        _lastSaveInDB = System.currentTimeMillis();
        _lastSaveInDBMsg = msg + " by " + savedBy;            // for showing in schedule

        Case case_ = new Case(caseId, null, null, rup);
        case_.setSavedBy(savedBy);
        case_.setTimestamp(_lastSaveInDB);
        _dataMapper.saveRup(case_);
        removeActivityTypes(rup);
    }


    public Case loadCase(String caseId)  throws SQLException {
        Case caseToLoad;

        List<Case> cases = _dataMapper.getRupByCaseId(caseId);
        if (cases.isEmpty()) {
            Element rupElement = new Element(XML_RUP);
            Document doc = new Document(rupElement);

            // add caseId to the RUP
            Element caseIdElem = XMLUtils.getElement(doc, XML_RUP + "/" + XML_CASEID);
            if (caseIdElem == null) {
                caseIdElem = new Element(XML_CASEID);
                doc.getRootElement().addContent(caseIdElem);
            }
            caseIdElem.setText(caseId);

            caseToLoad = new Case(caseId, null, null, doc);
            _log.info("Created empty RUP for case Id " + caseId + ", " +
                    caseToLoad.getRupAsString());
        }
        else {
            caseToLoad = cases.get(0);
            _log.info("Loaded RUP for case Id " + caseId + " from database: "
                    + caseToLoad.getRupAsString());
        }

        return caseToLoad;
    }


    /**
     * adds new activities from YAWL model specification to document in right
     * order, e.g. if another worklet with new activities was started update
     * times of all activities beginning at last activity which has a time
     * TODO@tbe: returns false also, if order of activities was changed
     *
     * @param doc
     * @return true, if new activity was added
     */
    public void extendRUPFromYAWLModel(Document doc, Set<String> addedActivityNames)
            throws Exception {
        String caseId = XMLUtils.getCaseId(doc);

        Element rupElement = new Element(XML_RUP);
        rupElement.addContent(_pgc.getActivityElements(caseId));

        boolean changed = XMLUtils.mergeElements(rupElement, doc.getRootElement());
        addedActivityNames.addAll(getDiffActivityNames(doc.getRootElement(), rupElement));
        doc.setRootElement(rupElement);

        if (changed) {
            _log.info("Extract RUP of case Id " + caseId + " from YAWL specification: "
                    + Utils.document2String(doc, false));
        }
    }

    /**
     * get activity names of e2 which are not in e1
     *
     * @param e1
     * @param e2
     * @return
     */
    public Set<String> getDiffActivityNames(Element e1, Element e2) {
        Set<String> activityNames1 = XMLUtils.getActivityNames(e1);
        Set<String> activityNames2 = XMLUtils.getActivityNames(e2);
        Set<String> diffActivityNames = new HashSet<String>();

        for (String activityName : activityNames2) {
            if (!activityNames1.contains(activityName)) {
                diffActivityNames.add(activityName);
            }
        }
        return diffActivityNames;
    }

    /**
     * completes a rup with historical data of rups with same ActivityType, but
     * only for new added activities and if data fields are empty yet
     *
     * @param rup
     * @param addedActivitiyNames
     * @return
     */
    public void completeRupFromHistory(Document rup, Set<String> addedActivitiyNames) {
        String xpath = XMLUtils.getXPATH_Activities();
        List<Element> activities = XMLUtils.getElements(rup, xpath);
        List<List<Element>> nodes;
        boolean changed = false;

        for (Element activity : activities) {
            String activityName = activity.getChildText(XML_ACTIVITYNAME);
            if (!addedActivitiyNames.contains(activityName)) {
                continue;
            }

            String activityType = activity.getChildText(XML_ACTIVITYTYPE);

            Integer dur = XMLUtils.getDurationValueInMinutes(activity.getChild(XML_DURATION), false);
            if (dur == null || dur == 0) {
                nodes = getRupNodes(activityName, activityType, XML_DURATION, HistoricalMode.median);
                activity.getChild(XML_DURATION).setText(nodes.isEmpty() ? "" : nodes.get(0).get(0).getText());
                changed = true;
            }

            xpath = XMLUtils.getXPATH_ActivityElement(activityName, XML_RESERVATION, null);
            List<Element> reservations = XMLUtils.getElements(rup, xpath);
            if (reservations.isEmpty()) {
                nodes = getRupNodes(activityName, activityType, XML_RESERVATION, HistoricalMode.most);
                activity.addContent(nodes.isEmpty() ? new ArrayList<Element>() : nodes.get(0));
                changed = true;
            }
        }

        if (changed) {
            _log.info("update rup for case " + XMLUtils.getCaseId(rup) + " from history: "
                    + Utils.document2String(rup, false));
        }
    }

    /**
     * gets the most or least used value or the average value of field
     *
     * @param activityName
     * @param fieldName
     * @return
     */
    private List<List<Element>> getRupNodes(String activityName, String activityType, String fieldName,
                                            HistoricalMode mode) {
        Map<Object, List<Element>> elementMap = new HashMap<Object, List<Element>>();
        Map<Object, Comparable> sortMap = getRUPNodes(elementMap, activityName, activityType, fieldName);
        List<List<Element>> nodes = new ArrayList<List<Element>>();
        _log.debug("elementMap: " + Utils.toString(elementMap));

        if (!elementMap.isEmpty()) {
            if (mode.equals(HistoricalMode.average)) {
                boolean isDurationValue = false;
                long count = 0, sum = 0;
                List<Element> nodeList = null;
                for (Iterator<List<Element>> i = elementMap.values().iterator(); i.hasNext(); ) {
                    nodeList = i.next();
                    for (Element node : nodeList) {
                        try {
                            sum += Long.parseLong(node.getText());
                            count++;
                        }
                        catch (Exception e) {
                            try {
                                sum += Utils.duration2Minutes(XMLUtils.getDurationValue(node, true));
                                isDurationValue = true;
                                count++;
                            }
                            catch (Exception e1) {
                                 _log.debug(Utils.toString(node) + " cannot be parsed", e1);
                            }
                        }
                    }
                }

                if (count > 0) {
                    Element e = nodeList.get(0);
                    e.setText(Long.toString(sum / count));
                    if (isDurationValue) {
                        try {
                            e.setText(Utils.stringMinutes2stringXMLDuration(e.getText()));
                        }
                        catch (DatatypeConfigurationException e1) {
                        }
                    }
                    List<Element> list = new ArrayList<Element>();
                    list.add(e);
                    nodes.add(list);
                }
            }
            else if (mode.equals(HistoricalMode.median)) {
                boolean isDurationValue = false;
                List<Long> values = new ArrayList<Long>();
                List<Element> nodeList = null;
                for (Iterator<List<Element>> i = elementMap.values().iterator(); i.hasNext(); ) {
                    nodeList = i.next();
                    for (Element node : nodeList) {
                        try {
                            values.add(Long.parseLong(node.getText()));
                        } catch (Exception e) {
                            try {
                                values.add(new Long(Utils.duration2Minutes(XMLUtils.getDurationValue(node, true))));
                                isDurationValue = true;
                            } catch (Exception e1) {
                                // _log.debug(Utils.toString(node) +
                                // " cannot be parsed", e1);
                            }
                        }
                    }
                }

                if (!values.isEmpty()) {
                    Element e = nodeList.get(0);
                    e.setText(Long.toString(Utils.getMedian(values)));
                    if (isDurationValue) {
                        try {
                            e.setText(Utils.stringMinutes2stringXMLDuration(e.getText()));
                        } catch (DatatypeConfigurationException e1) {
                        }
                    }
                    List<Element> list = new ArrayList<Element>();
                    list.add(e);
                    nodes.add(list);
                }
            }
            else { // least, most
                boolean descOrder = mode.equals(HistoricalMode.most);
                SortedMap<Object, Comparable> sortedNodes = new TreeMap(new Utils.ValueComparer(sortMap, descOrder));
                sortedNodes.putAll(sortMap);
                _log.debug("sortedNodes: " + Utils.toString(sortedNodes));
                for (Iterator i = sortedNodes.keySet().iterator(); i.hasNext(); ) {
                    Object o = i.next();
                    if (!((String) o).isEmpty()) {
                        nodes.add(elementMap.get(o));
                        break;
                    }
                }
            }
        }
        _log.debug("return: " + Utils.toString(nodes));
        return nodes;
    }

    /**
     * gets field data from case in db
     *
     * @param activity
     * @param nodeName
     * @return
     */
    private Map<Object, Comparable> getRUPNodes(Map<Object, List<Element>> elementMap, String activity, String type,
                                                String nodeName) {
        Map<Object, Comparable> sortMap = new HashMap<Object, Comparable>();
        List<List<Element>> nodeLists = null;
        try {

            nodeLists = _dataMapper.getRupNodes(activity, type, nodeName);
            for (List<Element> nodeList : nodeLists) {
                String key;
                List<Element> newNodeList = new ArrayList<Element>();

                if (nodeList.isEmpty()) {
                    key = "";
                } else {
                    List<String> keyParts = new ArrayList<String>();

                    for (Element node : nodeList) {
                        // _log.debug("node: " + Utils.toString(node));
                        if (node.getName().equals(XML_RESERVATION)) {
                            Element reservation = FormGenerator.getTemplate(XML_RESERVATION);
                            Element resource = reservation.getChild(XML_RESOURCE);

                            Element resourceNode = node.getChild(XML_RESOURCE);

                            String keyPart = resourceNode.getChildText(XML_CAPABILITY);
                            resource.getChild(XML_CAPABILITY).setText(resourceNode.getChildText(XML_CAPABILITY));

                            keyPart = resourceNode.getChildText(XML_ROLE) + keyPart;
                            resource.getChild(XML_ROLE).setText(resourceNode.getChildText(XML_ROLE));

                            keyPart = resourceNode.getChildText(XML_SUBCATEGORY) + keyPart;
                            resource.getChild(XML_SUBCATEGORY).setText(resourceNode.getChildText(XML_SUBCATEGORY));

                            keyPart = resourceNode.getChildText(XML_CATEGORY) + keyPart;
                            resource.getChild(XML_CATEGORY).setText(resourceNode.getChildText(XML_CATEGORY));

                            if (keyPart.isEmpty()) { // use Id, because it was set in custom form or Schedule
                                keyPart = resourceNode.getChildText(XML_ID);
                                resource.getChild(XML_ID).setText(resourceNode.getChildText(XML_ID));
                            } else {
                                resource.getChild(XML_ID).setText(""); // remove Id,
                                // because it was
                                // set in RS
                            }

                            keyPart = keyPart + node.getChildText(XML_WORKLOAD);
                            reservation.getChild(XML_WORKLOAD).setText(node.getChildText(XML_WORKLOAD));

                            // _log.debug("keyPart: " + keyPart);
                            keyParts.add(keyPart);

                            newNodeList.add(reservation);
                        } else {
                            Element newNode = new Element(node.getName());
                            keyParts.add(node.getText());
                            newNode.setText(node.getText());

                            newNodeList.add(newNode);
                        }
                    }

                    Collections.sort(keyParts);
                    key = Utils.toString(keyParts);
                }

                // _log.debug("key='"+key+"' for nodeList: " +
                // Utils.toString(newNodeList));
                if (key.isEmpty()) {
                    continue;
                }

                if (sortMap.containsKey(key)) {
                    sortMap.put(key, (Integer) sortMap.get(key) + 1);
                } else {
                    sortMap.put(key, 1);
                    elementMap.put(key, newNodeList);
                }
            }
        } catch (Exception e) {
            _log.error("cannot get historical data for " + activity + "." + nodeName, e);
        }
        // _log.debug("sortMap: " + Utils.toString(sortMap) + ", elementMap: " +
        // Utils.toString(elementMap));
        return sortMap;
    }

    /**
     * generates key for list of elements and cleans irrelevant nodes, e.g.
     * errors, warnings, Ids and status set by RS ...
     *
     * @param nodeList
     * @return
     */
    private String getKeyForNodeList(List<Element> nodeList) {
        if (nodeList.isEmpty()) {
            return "";
        } else {
            List<String> keyParts = new ArrayList<String>();
            List<Element> newNodeList = new ArrayList<Element>();

            for (Element node : nodeList) {
                // _log.debug("node: " + Utils.toString(node));
                if (node.getName().equals(XML_RESERVATION)) {
                    Element reservation = FormGenerator.getTemplate(XML_RESERVATION);
                    Element resource = reservation.getChild(XML_RESOURCE);

                    Element resourceNode = node.getChild(XML_RESOURCE);

                    String keyPart = resourceNode.getChildText(XML_CAPABILITY);
                    resource.getChild(XML_CAPABILITY).setText(resourceNode.getChildText(XML_CAPABILITY));

                    keyPart = resourceNode.getChildText(XML_ROLE) + keyPart;
                    resource.getChild(XML_ROLE).setText(resourceNode.getChildText(XML_ROLE));

                    keyPart = resourceNode.getChildText(XML_SUBCATEGORY) + keyPart;
                    resource.getChild(XML_SUBCATEGORY).setText(resourceNode.getChildText(XML_SUBCATEGORY));

                    keyPart = resourceNode.getChildText(XML_CATEGORY) + keyPart;
                    resource.getChild(XML_CATEGORY).setText(resourceNode.getChildText(XML_CATEGORY));

                    if (keyPart.isEmpty()) { // use Id, because it was set in custom form or Schedule
                        keyPart = resourceNode.getChildText(XML_ID);
                        resource.getChild(XML_ID).setText(resourceNode.getChildText(XML_ID));
                    } else {
                        resource.getChild(XML_ID).setText(""); // remove Id, because
                        // it was set in RS
                    }

                    keyPart = keyPart + node.getChildText(XML_WORKLOAD);
                    reservation.getChild(XML_WORKLOAD).setText(node.getChildText(XML_WORKLOAD));

                    // _log.debug("keyPart: " + keyPart);
                    keyParts.add(keyPart);

                    newNodeList.add(reservation);
                } else {
                    Element newNode = new Element(node.getName());
                    keyParts.add(node.getText());
                    newNode.setText(node.getText());

                    newNodeList.add(newNode);
                }
            }

            Collections.sort(keyParts);
            nodeList = newNodeList;
            return Utils.toString(keyParts);
        }
    }

    /**
     * Get all RUPs that have at least one started activity. (SOU) und endzeit <
     * jetzt
     *
     * @return
     * @throws SQLException
     * @author jku, tbe
     */
    private List<Document> getActiveRups(Date now) throws SQLException {
        String timestamp = Utils.date2String(now, Utils.DATETIME_PATTERN_XML);
        List<Case> list = _dataMapper.getActiveRups(timestamp);
        return getRupList(list);
    }

    /**
     * Get all RUP's of the specified activity from the database.
     *
     * @return
     * @throws SQLException
     */
    private List<Document> getRupsByActivity(String activityName) throws SQLException {
        return getRupList(_dataMapper.getRupsByActivity(activityName));
    }

    public List<Document> getRupList(List<Case> cases) {
        List<Document> docs = new ArrayList<Document>();
        for (Case cas : cases) {
            if (cas.getRUP() != null) {
                docs.add(cas.getRUP());
            }
        }
        return docs;
    }

    /**
     * resource service retrieves reservations for given case and task to utilise
     * them
     *
     * @param caseId
     * @param activityName
     * @return List<Element>
     * @throws SchedulingException
     */
    public List<Element> loadReservations(String caseId, String activityName) throws SchedulingException {
        try {
            Document rup = loadCase(caseId).getRUP();
            String xpath = XMLUtils.getXPATH_ActivityElement(activityName, XML_RESERVATION, null);
            List<Element> reservations = XMLUtils.getXMLObjects(rup, xpath);
            return reservations;
        } catch (Exception e) {
            throw new SchedulingException("error during retrieve reservations", e);
        }
    }

    /**
     * call from rescheduling task in yawl engine if rescheduling of an activity
     * was made
     */
    public void activityStatusChange(String caseId, String activityName, String from, String to)
            throws SchedulingException {
        _log.debug("caseId: " + caseId + ", activityName: " + activityName + ", from: " + from + ", to: " + to);
        utilisationPlanChange(null, caseId, activityName, UTILISATION_TYPE_PLAN, from, to, "activityStatusChange");
    }

    /**
     * call from utilisation task in yawl engine if (de)utilisation for all
     * resources of an activity have to made
     *
     * @param timeStampXML yyyy-MM-ddTHH:mm:ss.SSS
     */
    public void resourceUtilisationChange(String taskID, String caseId, String activityName, String utilisationType,
                                          String timeStampXML) throws SchedulingException {
        _log.debug("taskID: " + taskID + ", caseId: " + caseId + ", activityName: " + activityName
                + ", utilisationType: " + utilisationType + ", timeStampXML: " + timeStampXML);
        if (UTILISATION_TYPE_BEGIN.equals(utilisationType)) {
            utilisationPlanChange(taskID, caseId, activityName, utilisationType, timeStampXML, null,
                    "resourceUtilisationChange");
        } else if (UTILISATION_TYPE_END.equals(utilisationType)) {
            utilisationPlanChange(taskID, caseId, activityName, utilisationType, null, timeStampXML,
                    "resourceUtilisationChange");
        } else {
            throw new SchedulingException("unknown utilisationType: " + utilisationType);
        }
    }

    /**
     * call from scheduling service if (de)utilisation for all resources of an
     * activity have to made
     *
     * @param taskID
     * @param caseId
     * @param activityName
     * @param utilisationType
     * @param fromXML
     * @param toXML
     * @param savedBy
     * @throws SchedulingException
     */
    private void utilisationPlanChange(String taskID, String caseId, String activityName, String utilisationType,
                                       String fromXML, String toXML, String savedBy) throws SchedulingException {
        try {
            // search RUP by caseId from DB
            Document rup = loadCase(caseId).getRUP();
            if (rup == null) {
                throw new SchedulingException("resourceUtilisationPlan not found");
            }

            String xpath = XMLUtils.getXPATH_Activities(activityName);
            Element activity = XMLUtils.getElement(rup, xpath);

            Element from = activity.getChild(XML_FROM);
            Element to = activity.getChild(XML_TO);
            Element duration = activity.getChild(XML_DURATION);

            String errorMsg = null;

            if (fromXML != null) {
                errorMsg = XML_FROM;
                XMLUtils.setStringValue(from, fromXML);

                try {
                    Date toDate = XMLUtils.getDateValue(from, true);
                    Duration dur = XMLUtils.getDurationValue(duration, true);
                    dur.addTo(toDate);
                    XMLUtils.setStringValue(to, Utils.date2String(toDate, Utils.DATETIME_PATTERN_XML));
                    _log.debug(activityName + ", duration: " + duration.getText() + " -> set from: " + from.getText()
                            + ", to: " + to.getText());
                } catch (Exception e) {
                    _log.error("cannot update " + XML_TO, e);
                }
            }

            if (toXML != null) {
                errorMsg = XML_TO;
                XMLUtils.setStringValue(to, toXML);

                try {
                    Date toDate = XMLUtils.getDateValue(to, true);
                    Date fromDate = XMLUtils.getDateValue(from, true);
                    // long minutes = (toDate.getTime()-fromDate.getTime())/1000/60;
                    // String dur =
                    // Utils.stringMinutes2stringXMLDuration(String.valueOf(minutes));
                    // XMLUtils.setStringValue(duration, dur);
                    XMLUtils.setDurationValue(duration, toDate.getTime() - fromDate.getTime());
                    _log.debug(activityName + ", from: " + from.getText() + " -> set to: " + to.getText() + ", duration: "
                            + duration.getText());
                } catch (Exception e) {
                    _log.error("cannot update " + XML_DURATION, e);
                }
            }

            if (UTILISATION_TYPE_BEGIN.equals(utilisationType)) {
                XMLUtils.setChildText(activity, XML_STARTTASKID, taskID);
                XMLUtils.setChildText(activity, XML_REQUESTTYPE, UTILISATION_TYPE_BEGIN);
            } else if (UTILISATION_TYPE_END.equals(utilisationType)) {
                XMLUtils.setChildText(activity, XML_ENDTASKID, taskID);
                XMLUtils.setChildText(activity, XML_REQUESTTYPE, UTILISATION_TYPE_END);
            } else { // if (UTILISATION_TYPE_PlAN.equals(utilisationType)) {
                // do nothing
            }

            if (errorMsg != null) {
                errorMsg += " of Activity " + activityName;
            } else {
                _log.error("timestamps from and to was null");
            }

            _scheduler.setTimes(rup, activity, true, true, null);
            // checkRelations(rup);

            optimizeAndSaveRup(rup, savedBy, errorMsg, false);
        } catch (Exception e) {
            _log.error("cannot handle caseId: " + caseId, e);
        }
    }

    /**
     * call from resource service about unavailabilities of resources (if Admin
     * change availability of resource and PLANNING_STATUS was changed to
     * PLANNING_STATUS_NOTAVAILABLE)
     */
    public void reservationStatusChange(String caseId, String activityName,
                                        Long reservationId, String statusNew) {
        try {
            _log.debug("caseId: " + caseId + ", reservationId: " + reservationId);
            Document rup = loadCase(caseId).getRUP();

            // passende reservation im rup finden und status auf statusNew setzen
            String xpath = XMLUtils.getXPATH_ActivityElement(activityName, XML_RESERVATION, null);
            List<Element> reservations = XMLUtils.getXMLObjects(rup, xpath);
            List<Element> reservationsMatched = updateMatchingReservations(reservationId, reservations, statusNew);
            optimizeAndSaveRup(rup, "reservationStatusChange", "Reservation " + statusNew, true);
        } catch (Exception e) {
            _log.error("cannot handle caseId: " + caseId, e);
        }
    }

    /**
     * call from JobRUPCheck, sets TO time of running activities to actual time
     * and calculate new DURATION value
     */
    public void updateRunningRups(String savedBy) throws Exception {
        Date now = new Date();
        List<Document> rups = getActiveRups(now);

        // update rups startzeit und/oder endzeit
        for (Document rup : rups) {
            for (Element activity : (List<Element>) rup.getRootElement().getChildren(XML_ACTIVITY)) {
                String activityName = activity.getChildText(XML_ACTIVITYNAME);
                Element from = activity.getChild(XML_FROM);
                Date fromDate = XMLUtils.getDateValue(from, true);
                Element to = activity.getChild(XML_TO);
                Date toDate = XMLUtils.getDateValue(to, true);
                Element duration = activity.getChild(XML_DURATION);
                String requestType = activity.getChildText(XML_REQUESTTYPE);

                // _log.debug("caseId: "+caseId+", requestType: "+requestType+", from: "+from.getText()+", to: "+to.getText());
                if (requestType.equals(UTILISATION_TYPE_BEGIN) &&
                         toDate.before(now)) {
                    XMLUtils.setDateValue(to, now);
                    XMLUtils.setDurationValue(duration, toDate.getTime() - fromDate.getTime());

                    _log.info("update caseId: " + XMLUtils.getCaseId(rup) + ", set " + activityName + ".TO: "
                            + Utils.date2String(now, Utils.DATETIME_PATTERN));

                    _scheduler.setTimes(rup, activity, true, true, null);
                }
            }
            optimizeAndSaveRup(rup, savedBy, null, false);
        }
    }

    /**
     * TODO@tbe: was wenn RUP gerade angezeigt/konfiguriert wird?
     *
     * @param rup
     * @param errorMsg
     * @throws Exception
     */
    public Set<String> optimizeAndSaveRup(Document rup, String savedBy, String errorMsg, boolean resourceChange)
            throws Exception {
        String caseId = XMLUtils.getCaseId(rup);
        rup = _rs.saveReservations(rup, false, resourceChange);

        Set<String> errors = XMLUtils.getErrors(rup.getRootElement());
        if (!errors.isEmpty()) {
            String msg = _config.getLocalizedString("msgRUPInvalid", errorMsg == null ? "" : errorMsg);
            String address = _props.getSchedulingProperty("ReschedulingError.Address");
            String addressType = _props.getSchedulingProperty("ReschedulingError.AddressType");

            // send message if same message was not send since x minutes
            synchronized (_errorRUPs) {
                boolean alreadySent = false;
                long minutes = _props.getLongProperty(
                        PropertyReader.SCHEDULING, "ReschedulingMessageInterval");
                Map<String, Long> errorRUP = _errorRUPs.get(caseId);
                if (errorRUP != null) {
                    for (String msgSent : errorRUP.keySet()) {
                        long minutesSent = (System.currentTimeMillis() - errorRUP.get(msgSent)) / 1000 / 60;
                        if (msgSent.equals(msg) && minutesSent <= minutes) {
                            alreadySent = true;
                            break;
                        }
                    }
                } else {
                    errorRUP = new HashMap<String, Long>();
                    _errorRUPs.put(caseId, errorRUP);
                }

                if (!alreadySent) {
                    sendPushMessage(address, addressType, msg, caseId);
                    errorRUP.put(msg, System.currentTimeMillis());
                }
            }
        }
        // _log.debug("CHECKPOINT 110");
        saveRupToDatabase(caseId, savedBy, rup, "rescheduled");
        // _log.debug("CHECKPOINT 111");

        return errors;
    }



    private List<Element> updateMatchingReservations(Long reservationIdToMatch, List<Element> reservations,
                                                     String statusNew) {
        List<Element> reservationsMatched = new ArrayList<Element>();
        for (Element reservation : reservations) {
            Long reservationId = XMLUtils.getLongValue(reservation.getChild(XML_RESERVATIONID), true);
            if (reservationIdToMatch != null && reservationId != null
                    && reservationIdToMatch.longValue() == reservationId.longValue()) {
                Element status = reservation.getChild(XML_STATUS);
                if (status.getText().equals(RESOURCE_STATUS_REQUESTED) || status.getText().equals(RESOURCE_STATUS_RESERVED)) {
                    XMLUtils.addErrorValue(reservation, true, "msgUnavailable");
                    reservationsMatched.add(reservation);
                }
                status.setText(statusNew);
                _log.debug("set reservation " + reservationId + " to " + statusNew);
            }
        }
        return reservationsMatched;
    }

    public void sendPushMessage(String address, String addressType, String msg, String caseId) {
        try {
            Element message = new Element(XML_MESSAGEPUSH_SEND);
            // Element timestamp = new Element(XML_TIMESTAMP).setText(time);
            Element text = new Element(XML_TEXT).setText("CaseId: " + caseId + ", " + msg);
            message.addContent(new Element(XML_PAYLOAD).addContent(text));
            message.addContent(new Element(XML_ADDRESSTYPE).setText(addressType));
            message.addContent(new Element(XML_ADDRESS).setText(address));
            String msgStr = Utils.element2String(message, false);

            Map<String, Object[]> parameters = new HashMap<String, Object[]>();
            parameters.put("sessionHandle", new String[]{getHandle()});
            parameters.put("message", new String[]{msgStr});
            // _log.debug("parameters: " + Utils.toString(parameters));

            String url = _props.getYAWLProperty("JCouplingMessageReceiver.backEndURI");
            _log.debug("send message with caseId " + caseId + " to " + url);
            String ret = Utils.sendRequest(url, parameters);
            // _log.debug("ret: "+ret);
            if (ret.startsWith("<failure>")) {
                _log.error("cannot send message: " + ret);
            }
        } catch (Exception e) {
            _log.error("cannot send message", e);
        }
    }

    public void handleEngineInitialisationCompletedEvent() {
        super.handleEngineInitialisationCompletedEvent();
        registerMessageReceiveServlet();
        processCachedMappingsTask();
        JobTimer.initialize();
    }

    /**
     * Register the MessageReceiveServlet with the Resource Service TODO@tbe:
     * better to use our own IP and figure out the port number
     */
    public void registerMessageReceiveServlet() {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                String address;
                try {
                    address = _props.getYAWLProperty("SchedulingMessageReceiver.backEndURI");
                    String result = ResourceServiceInterface.getInstance()
                              .registerCalendarStatusChangeListener(address);
                    if (result.startsWith("<failure>")) {
                        throw new SchedulingException(result);
                    }
                    _log.info("successfully registered " + address + " as YAWL calendar StatusChangeListener");
                }
                catch (Throwable e) {
                    _log.error("cannot register MessageReceiveServlet", e);
                }
            }
        });
        thread.start();
    }

    /**
     * retrieve a list of all activity types for showing in configuration
     * dropdown boxes of custom form
     *
     * @throws YAWLException
     */
    public synchronized List<String> getActivityTypes(String activityName, String newValue)
            throws ResourceGatewayException, IOException {
        Set<String> activityTypeSet;
        List<String> activityTypes = new ArrayList<String>();

        try {
            activityTypes = _allActivityTypes.get(activityName);

            if (activityTypes == null) {
                activityTypeSet = new HashSet<String>();
                List<String> types = _dataMapper.getRupActivityTypes(activityName);
                for (String type : types) {
                    if (!type.trim().isEmpty()) {
                        activityTypeSet.add(type);
                    }
                }
            } else {
                activityTypeSet = new HashSet<String>(activityTypes);
            }

            if (newValue != null && !"".equals(newValue)) {
                activityTypeSet.add(newValue);
            }

            activityTypes = new ArrayList<String>(activityTypeSet);
            Collections.sort(activityTypes);

            _allActivityTypes.put(activityName, activityTypes);
        } catch (Exception e) {
            _log.error("cannot load activityTypes", e);
        }

        return activityTypes;
    }

    /**
     * cleans activityTypes for these activities, which a new activityType not
     * stored in DB yet forces a reload of activityTypes of related activityNames
     *
     * @param rup
     */
    private void removeActivityTypes(Document rup) {
        String xpath = XMLUtils.getXPATH_ActivityElement(null, XML_ACTIVITYTYPE, null);
        List<Element> activityTypeElems = XMLUtils.getXMLObjects(rup, xpath);
        for (Element activityTypeElem : activityTypeElems) {
            _log.debug("activityTypeElem.getText(): " + activityTypeElem.getText());
            if (activityTypeElem.getText().trim().isEmpty()) {
                continue;
            }

            String activityName = activityTypeElem.getParentElement().getChildText(XML_ACTIVITYNAME);
            List<String> activityTypes = _allActivityTypes.get(activityName);
            if (activityTypes != null && !activityTypes.contains(activityTypeElem.getText())) {
                _allActivityTypes.put(activityName, null);
                _log.debug("remove activityTypes for " + activityName);
            }
        }
    }

    /**
     * get overall time of activities
     */
    private long getOverallTimeInMin(Document rup) {
        long begin = Long.MAX_VALUE, end = 0;
        List<Element> activities = XMLUtils.getXMLObjects(rup, XMLUtils.getXPATH_Activities());
        for (Element activity2 : activities) {
            String activityName2 = activity2.getChildText(XML_ACTIVITYNAME);
            try {
                Element from = activity2.getChild(XML_FROM);
                Date fromDate = XMLUtils.getDateValue(from, true);
                begin = Math.min(begin, fromDate.getTime());

                Element to = activity2.getChild(XML_TO);
                Date toDate = XMLUtils.getDateValue(to, true);
                end = Math.max(end, toDate.getTime());
            } catch (Exception e) {
                _log.error("cannot get times of activity: " + activityName2, e);
            }
        }
        long time = (end - begin) / 1000 / 60;
        _log.debug("overall time = " + time + " min");
        return time;
    }

    /**
     * extract message transfers from rup and find/create job group for this rup
     * and update/create job for each message transfer item
     *
     * @param rup
     */
    public void startMessageTransfers(String caseId, Document rup) {
        try {
            JobTimer.startJobMsgTransfer(caseId, rup);
        } catch (Exception e) {
            XMLUtils.addErrorValue(rup.getRootElement(), true, "msgMsgTransferError", e.getMessage());
        }
    }

    /**
     * **************************************************************************************
     * task methods and inner classes
     * ***************************************************************************************
     */

    public void processCachedMappingsTask() {
        CachedMappingsHandler cwih = new CachedMappingsHandler();
        cwih.start(); // handle initial process in own thread
    }

    /**
     * count of processes which already waiting for method
     * <p/>
     * see processCachedWorkItems(String channelName)
     */
    private static int countProcessCachedWorkItems = 0;

    /**
     * @author tbe
     */
    private class CachedMappingsHandler extends Thread {
        public CachedMappingsHandler() {
        }

        public void run() {
            countProcessCachedWorkItems++;
            _log.debug(countProcessCachedWorkItems + " process(es) are processing/waiting/requesting...");

            try {
                process();
            } finally {
                countProcessCachedWorkItems--;
            }
        }

        /**
         * only serial access to table MAPPING, otherwise: ORA-06550: PLS-00201:
         * identifier 'PKG_MAPPING.GET_MAPPINGS' must be declared
         */
        private synchronized void process() {
            // noch zu bearbeitende Mappings aus DB laden
            List<Mapping> mappings = null;
            try {
                getHandle();

                // _log.debug("load mappings for channel: "+channelName+"...");
                mappings = _dataMapper.getMappings();
                _log.debug("found " + mappings.size() + " mappings");

                sort(mappings);

                for (Mapping mapping : mappings) {
                    try {
                        if (isCancelledWorkitem(mapping.getWorkItemId())) {
                            _dataMapper.removeMapping(mapping);
                        } else {
                            processMapping(mapping);
                        }
                    } catch (Throwable e) {
                        _log.error("cannot execute cached mapping, work item: " + mapping.getWorkItemId(), e);
                    }
                }
            } catch (Throwable e) {
                _log.error("cannot process mappings", e);
            }
        }
    }

    private void handleUtilisationTask(Mapping mapping, Element msg, boolean asThread) {
        UtilisationHandler uHandler = new UtilisationHandler(mapping, msg);
        if (asThread) {
            uHandler.start(); // handle utilisation tasks in own thread
        } else {
            uHandler.run(); // handle utilisation tasks serial
        }
    }

    /**
     * Handle message-task work items
     *
     * @author tbe
     */
    private class UtilisationHandler extends Thread {
        private Mapping mapping = null;
        private Element msg = null;

        public UtilisationHandler(Mapping mapping, Element msg) {
            this.mapping = mapping;
            this.msg = msg;
        }

        public void run() {
            String name = null;
            try {
                name = msg.getName();
                _log.info("handle " + name + " work item " + mapping.getWorkItemId());

                String caseId = mapping.getWorkItemId();
                caseId = caseId.substring(0, caseId.indexOf("."));
                String activityName = msg.getChildText(XML_ACTIVITYNAME);

                if (msg.getName().equals(XML_UTILISATION)) {
                    String timeStampXML = msg.getChild(XML_PAYLOAD).getChildText(XML_TIMESTAMP);
                    String utilisationType = msg.getChildText(XML_UTILISATIONTYPE);
                    WorkItemRecord wir = getWorkItemFromCache(mapping);
                    resourceUtilisationChange(wir.getTaskID(), caseId, activityName, utilisationType, timeStampXML);
                } else if (msg.getName().equals(XML_RESCHEDULING)) {
                    String from = msg.getChild(XML_PAYLOAD).getChildText(XML_FROM);
                    String to = msg.getChild(XML_PAYLOAD).getChildText(XML_TO);
                    activityStatusChange(caseId, activityName, from, to);
                } else {
                    throw new SchedulingException("unknown taskType: " + msg.getName());
                }

                _log.info("++++++++++++++++++++ successfully processed " + name + " work item " + mapping.getWorkItemId());

                checkInWorkItem(mapping, null);

                try {
                    _dataMapper.removeMapping(mapping); // work item is completed,
                    // dont't throw an exeption
                    // anymore
                } catch (Throwable e) {
                    _log.error("cannot cleanup " + name + " work item", e);
                }
            } catch (Throwable e) {
                _log.error("cannot process " + name + " work item: " + mapping.getWorkItemId(), e);
            }
        }
    }

}
