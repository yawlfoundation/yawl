/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.scheduling.persistence;

import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.scheduling.Case;
import org.yawlfoundation.yawl.scheduling.Mapping;
import org.yawlfoundation.yawl.scheduling.util.Utils;
import org.yawlfoundation.yawl.util.HibernateEngine;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.text.ParseException;
import java.util.*;


/**
 * This class encapsulates the database. It exclusively uses stored procedures to
 * access the persistence layer, thereby maintaining the concept of logical data
 * independence. As long as the signature of the stored procedure and their
 * semantics do not change, data source and application code may be developed
 * independently from each other.
 * 
 * Specifically, DataMapper facilitates the persistence of resource utilisation
 * plans (RUPs) and of mappings.
 * 
 * A mapping is a bijective function that assigns to every YAWL workitem Id a
 * unique request key, where the latter is used by the custom service. In
 * effect, the mapping table binds the operations of the Scheduling Service to
 * the YAWL engine in a non-ambiguous way.
 * 
 * @author tbe, jku
 * @version $Id$
 * 
 */
public class DataMapper {
    private HibernateEngine _db;

	public DataMapper()	{

        // setup database connection
        Set<Class> persistedClasses = new HashSet<Class>();
        persistedClasses.add(Mapping.class);
        persistedClasses.add(Case.class);
        _db = HibernateEngine.getInstance(true, persistedClasses);
    }

	/**
	 * Saves or updates a mapping to the database for recovery of failed YAWL
	 * requests
	 * 
	 * @param mapping
	 *           object
	 *
	 */
	public void saveMapping(Mapping mapping) {
        Mapping exists = (Mapping) _db.get(Mapping.class, mapping.getWorkItemId());
        if (exists != null) {
            exists.setRequestKey(mapping.getRequestKey());
            exists.setWorkItemStatus(mapping.getWorkItemStatus());
            exists.setLocked(mapping.isLocked());
            _db.exec(exists, HibernateEngine.DB_UPDATE, true);
        }
        else {
            _db.exec(mapping, HibernateEngine.DB_INSERT, true);
        }
	}


	/**
	 * removes a mapping from database
	 * 
	 * @param mapping
	 *           Object
	 */
	public void removeMapping(Mapping mapping) {
        _db.exec(mapping, HibernateEngine.DB_DELETE, true);
	}


	/**
	 * Removes all mappings with specified "workItemId"
	 *
	 * @author tbe, jku
	 * @param workItemId - YAWL's unique work item identifier
	 */
	public void removeMapping(String workItemId) {
        Mapping mapping = (Mapping) _db.get(Mapping.class, workItemId);
        if (mapping != null) removeMapping(mapping);
	}


	/**
	 * Get all mappings from the database
	 * 
	 * @return ArrayList<Mapping>
	 */
	public List<Mapping> getMappings() {
        List<Mapping> mappingList = new ArrayList<Mapping>();
        for (Object o : _db.getObjectsForClass("Mapping")) {
            mappingList.add((Mapping) o);
        }
        return mappingList;
	}

	
    public void saveRup(Case theCase) {
        Case exists = (Case) _db.get(Case.class, theCase.getCaseId());
        if (exists != null) {
            exists.setCaseName(theCase.getCaseName());
            exists.setDescription(theCase.getDescription());
            exists.setTimestamp(theCase.getTimestamp());
            exists.setSavedBy(theCase.getSavedBy());
            exists.setRupAsString(theCase.getRupAsString());
            _db.exec(exists, HibernateEngine.DB_UPDATE, true);
        }
        else {
            _db.exec(theCase, HibernateEngine.DB_INSERT, true);
        }
    }
    

	/**
	 * Get all RUPs from the database that start after "from" and end before
	 * "to". Allow for specifying a set of Yawl case Ids that are to be excluded
	 * from the result. Also, it is possible to select only RUPs that are active.
	 * 
	 * @author jku, tbe
	 * @param from
	 * @param to
	 * @param yCaseIdsToExclude
	 * @param activeOnly
	 * @return List<Case> all cases with RUPs that meet the selection criteria
	 */
	public List<Case> getRupsByInterval(Date from, Date to,
                                   List<String> yCaseIdsToExclude, boolean activeOnly) {

        if (yCaseIdsToExclude == null) yCaseIdsToExclude = new ArrayList<String>();
        List<Case> caseList = new ArrayList<Case>();
        for (Case c : getAllRups()) {
            if ((activeOnly && (! c.isActive())) || yCaseIdsToExclude.contains(c.getCaseId())) {
                continue;
            }
            Document rup = c.getRUP();
            long fromTime = getTime(rup, "//Activity/From");
            long toTime = getTime(rup, "//Activity/To");
            if ((fromTime > -1) && (toTime > -1) && (fromTime >= from.getTime()) && 
                    (toTime <= to.getTime())) {
                caseList.add(c);
            }
        }
        return caseList;
	}


	public List<Case> getRupByCaseId(String caseId)	{
        List<Case> caseList = new ArrayList<Case>();
        if (caseId != null) {
            for (Case c : getAllRups()) {
                if (caseId.equals(c.getCaseId())) {
                    caseList.add(c);
                }
            }
        }
        return caseList;
	}

	/**
	 * @author jku, tbe
	 * @param
	 * @return
	 */
	public List<Case> getAllRups() {
        List list = _db.getObjectsForClass("Case");
        List<Case> caseList = new ArrayList<Case>();
        if (list != null) {
            for (Object o : list) {
                caseList.add((Case) o);
            }
        }
        return caseList;
    }


	/**
	 * @author jku, tbe
	 * @param timestamp
	 * @return
	 */
	public List<Case> getActiveRups(String timestamp) {
        List<Case> caseList = new ArrayList<Case>();
        for (Case c : getAllRups()) {
            if (c.isActive()) {
                Document rup = c.getRUP();
                Element phaseElem = JDOMUtil.selectElement(rup, "//Activity/RequestType");
                if (phaseElem != null && "SOU".equals(phaseElem.getText())) {
                    long toTime = getTime(rup, "//Activity/To");
                    if (toTime > -1 && toTime < getTime(timestamp)) {
                        caseList.add(c);
                    }
                }
            }
        }
        return caseList;
    }

	/**
	 * @author jku, tbe
	 * @param activityName
	 * @param activityType
	 * @param nodeName
	 * @return
	 */
	public List<List<Element>> getRupNodes(String activityName, String activityType,
                                           String nodeName)	{
        List<List<Element>> nodesList = new ArrayList<List<Element>>();
        for (Case c : getRupsByActivity(activityName)) {
            Document rup = c.getRUP();
            Element elem = JDOMUtil.selectElement(rup, "//Activity/ActivityType");
            if (elem != null) {
                if (activityType.equals(elem.getText())) {
                    Element node = JDOMUtil.selectElement(rup, "//Activity/" + nodeName);
                    if (node != null) {
                        nodesList.add(Utils.string2Elements(JDOMUtil.elementToString(node)));
                    }
                }
            }
        }
        return nodesList;
	}


	/**
	 * @author jku, tbe
	 * @param activityName
	 * @return
	 */
	public List<Case> getRupsByActivity(String activityName) {
        List<Case> caseList = new ArrayList<Case>();
        for (Case c : getAllRups()) {
            Document rup = c.getRUP();
            Element elem = JDOMUtil.selectElement(rup, "//Activity/ActivityName");
            if (elem != null) {
                if (activityName.equals(elem.getText())) {
                    caseList.add(c);
                }
            }
        }
        return caseList;
	}

	/**
	 * @param activityName
	 * @return
	 */
	public List<String> getRupActivityTypes(String activityName) {
        List<String> activityTypes = new ArrayList<String>();
        for (Case c : getRupsByActivity(activityName)) {
            Document rup = c.getRUP();
            Element elem = JDOMUtil.selectElement(rup, "//Activity/ActivityType");
            if (elem != null) {
                if (elem.getText() != null) {
                    activityTypes.add(elem.getText());
                }
            }
        }
        return activityTypes;
	}

	/**
	 * Update RUP, set "active status"
	 * 
	 * @param caseId
	 * @param active
	 * @return
	 */
	public void updateRup(String caseId, boolean active) {
        Case caseWithRup = (Case) _db.getObjectsForClassWhere("Case",
                "caseId='" + caseId + "'");
        if (caseWithRup != null) {
            caseWithRup.setActive(active);
            _db.exec(caseWithRup, HibernateEngine.DB_UPDATE, true);
        }
	}


    private long getTime(Document rup, String xpath) {
        Element elem = JDOMUtil.selectElement(rup, xpath);
        return elem != null ? getTime(elem.getText()) : -1;
    }


    private long getTime(String dateText) {
        if (dateText != null) {
            try {
                Date date = Utils.string2Date(dateText, Utils.DATETIME_PATTERN_XML);
                return date.getTime();
            }
            catch (ParseException pe) {
                // fall through to default return
            }
        }
        return -1;
    }

}