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

package org.yawlfoundation.yawl.scheduling.lanes;

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayException;
import org.yawlfoundation.yawl.scheduling.*;
import org.yawlfoundation.yawl.scheduling.util.XMLUtils;
import org.yawlfoundation.yawl.scheduling.persistence.DataMapper;
import org.yawlfoundation.yawl.scheduling.resource.ResourceServiceInterface;
import org.yawlfoundation.yawl.scheduling.util.*;

import java.util.*;


public class LaneProducer implements Constants {
	private static Logger logger = Logger.getLogger(LaneProducer.class);

	private DataMapper dataMapper;
	private ConfigManager config;
    private Scheduler scheduler;
	private String categoryId;
	private String roleId;
	private List<Map<String, Object>> lanes = new ArrayList<Map<String, Object>>();
	private final int titleShortLength = 3;
	private final boolean debug = true; // show debug output in process units
	
	public LaneProducer(String categoryOrRoleId, ConfigManager config) throws Exception {
		this.config = config;
        ResourceServiceInterface rs = ResourceServiceInterface.getInstance();
		scheduler = new Scheduler();
		dataMapper = new DataMapper();
		
		NonHumanCategory category = null;
		Role role = null;
		try {
			category = rs.getNonHumanCategory(categoryOrRoleId);
		} catch (ResourceGatewayException e) {
			// NonHumanCategory not found
		}
		if (category == null) {
			try {
				role = rs.getRole(categoryOrRoleId);
			} catch (ResourceGatewayException e) {
				// Role not found
			}
		}

		if (category != null) {
			categoryId = category.getID();
			for (NonHumanResource nhr : rs.getNonHumanResources()) {
				if (nhr.getCategory()!=null && categoryId.equals(nhr.getCategory().getID())) {
					Map<String, Object> lane = new HashMap<String, Object>();
					put(lane, "id", nhr.getID());
					put(lane, "title", nhr.getName() + (debug ? " ("+nhr.getID()+")" : ""));
					int idx = nhr.getName().lastIndexOf("#")+1;
					put(lane, "titleShort", idx<nhr.getName().length() ?
                            nhr.getName().substring(idx) : nhr.getName());
					lanes.add(lane);
				}
			}
		}
        else if (role != null) {
			roleId = role.getID();
			for (Participant participant : rs.getParticipants()) {
				Collection<Role> roles = rs.getParticipantRoles(participant.getID());
				if (roles != null) participant.setRoles(new HashSet<Role>(roles));
				if (participant.hasRole(role)) {
					Map<String, Object> lane = new HashMap<String, Object>();
					put(lane, "id", participant.getID());
					put(lane, "title", participant.getFullName());
					put(lane, "titleShort", getInitials(participant));
					lanes.add(lane);
				}
			}
		} else {
			throw new SchedulingException("lane group id not found: " + categoryOrRoleId);
		}
		
		// sort lanes by title, then add virtualLane at end
		Collections.sort(lanes, new Comparator<Map<String, Object>>() {
	        public int compare(Map<String, Object> m1, Map<String, Object> m2) {
	        	return ((String)m1.get("title")).compareTo((String)m2.get("title"));
	        }
		});
				
		Map<String, Object> virtualLane = new HashMap<String, Object>();
		put(virtualLane, "id", "virtual");
		put(virtualLane, "title", config.getLocalizedString("virtual"));
		put(virtualLane, "titleShort", config.getLocalizedString("virtual").substring(0,
                titleShortLength));
		lanes.add(virtualLane);
		logger.debug("categoryId="+categoryId+", roleId="+roleId+", lanes "+
                Utils.toString(lanes));
	}
	
	public List<List<Map<String, Object>>> getLanes(String... dates) throws Exception {
		Date from, to;
		int count = 0;
		
		// show intraoperative activities only
		String possibleActivitiesSorted = PropertyReader.getInstance()
                .getSchedulingProperty("possibleActivitiesSorted");
		String[] possibleActivities = Utils.parseCSV(possibleActivitiesSorted).toArray(new String[0]);

		// return dates in same order as given
		List<List<Map<String, Object>>> allLanes4AllDates = new ArrayList<List<Map<String, Object>>>();
		for (String date : dates) {
			List<Map<String, Object>> allLanes4Date = (List<Map<String, Object>>)Utils.deepCopy(lanes);
			//logger.debug("date="+date+", allLanes4Date "+Utils.toString(allLanes4Date));
			
			// lane goes from 00:00:00.001 bis 23:59:59.999
			long tmp = Utils.string2Date(date, Utils.DATE_PATTERN_XML).getTime();
			from = new Date(tmp-1);
			to = new Date(tmp+24*60*60*1000);
			List<Case> cases = dataMapper.getRupsByInterval(from, to, null, false);
			logger.debug("found "+cases.size()+" cases for date "+date);
			
			for (Case cas : cases) {
				try {
					String xpath = XMLUtils.getXPATH_Activities(possibleActivities);
					List<Element> activities = XMLUtils.getXMLObjects(cas.getRUP(), xpath);
					if (activities.isEmpty()) {
						continue;
					}
					
					// get original FROM and TO date
					Date originalEarlFrom = XMLUtils.getDateValue(activities.get(0).getChild(XML_FROM), false);
					Date originalLatestTo = XMLUtils.getDateValue(activities.get(activities.size()-1).getChild(XML_TO), false);
					
					// set times for showable in OP plan, if no duration given, set 5 min as default
					Element earlFrom = XMLUtils.getEarliestBeginElement(cas.getRUP(), possibleActivities);
					Element latestTo = XMLUtils.getLatestEndElement(cas.getRUP(), possibleActivities);
					scheduler.setTimes(cas.getRUP(), earlFrom==null ? latestTo.getParentElement() : earlFrom.getParentElement(), false, false, Utils.stringXML2Duration("PT5M"));
					earlFrom = XMLUtils.getEarliestBeginElement(cas.getRUP(), possibleActivities);
					latestTo = XMLUtils.getLatestEndElement(cas.getRUP(), possibleActivities);
									
					Map<String, Object> processUnit = new HashMap<String, Object>();
					put(processUnit, "caseId", cas.getCaseId());
					put(processUnit, "title", cas.getCaseName());
					put(processUnit, "start", Utils.date2String(XMLUtils.getDateValue(earlFrom, false), "yyyy-MM-dd HH:mm:ss"));
					put(processUnit, "end", Utils.date2String(XMLUtils.getDateValue(latestTo, false), "yyyy-MM-dd HH:mm:ss"));
					put(processUnit, "hasConflicts", false); // TODO@tbe: has rup conflicts ?
					put(processUnit, "hasErrors", XMLUtils.hasErrors(cas.getRUP().getRootElement()));
					put(processUnit, "hasWarnings", XMLUtils.hasWarnings(cas.getRUP().getRootElement()));
					//put(processUnit, "canBeEdited", true); // TODO@tbe: false wenn schon gestartet oder kein YAWL-Case mehr vorhanden, noch im OP-Plan erweitern
					
					Map<String, Object> data = new HashMap<String, Object>();
					//put(data, "description", cas.getId() + ": " + cas.getPatientFullName() + (cas.getPatientSex()==null ? "" : " (" + cas.getPatientSex() + ")"));
					put(data, "description", "" + ": " + cas.getCaseDescription());
					put(data, "note0", "xxx");
					if (debug) put(data, "note1", originalEarlFrom==null ? "???" : Utils.date2String(originalEarlFrom, "HH:mm dd.MM.yyyy") + " - ");
					if (debug) put(data, "note2", originalLatestTo==null ? "???" : Utils.date2String(originalLatestTo, "HH:mm dd.MM.yyyy"));
					put(data, "note3", cas.getCaseId()); // YAWL-CaseId
					put(data, "note4", ""); // reserved for title
					put(data, "note5", ""); // reserved for activityNames
					put(data, "note6", ""); // reserved for activityNames
					put(data, "note7", ""); // reserved for activityNames
					put(data, "note8", ""); // reserved for activityNames
					put(data, "note9", ""); // reserved for activityNames
					
					put(processUnit, "data", data);
					
					List<Map<String, Object>> stages = new ArrayList<Map<String, Object>>();
					Long firstActFrom = null;
					int note=0;
					for (Element activity : activities) {
						String activityName = activity.getChildText(XML_ACTIVITYNAME);
						Date actFrom = XMLUtils.getDateValue(activity.getChild(XML_FROM), true);
						if (firstActFrom == null) {
							firstActFrom = actFrom.getTime();
						}
						
						if (debug) {
							put(data, "note"+(note+5), activityName);
							note++;
						}
						
						Map<String, Object> stage = new HashMap<String, Object>();
						put(stage, "id", "stage-" + activityName);
						put(stage, "title", config.getLocalizedString(activityName));
						put(stage, "start", Utils.date2String(actFrom, Utils.DATETIME_PATTERN));
						put(stage, "offset", (actFrom.getTime() - firstActFrom)/1000/60);
						put(stage, "running", UTILISATION_TYPE_BEGIN.equals(activity.getChildText(XML_REQUESTTYPE)));
						stages.add(stage);
					}
					
					put(processUnit, "stages", stages);
	
					// find matching lanes
					xpath = XMLUtils.getXPATH_ActivitiesElement(possibleActivities, XML_RESERVATION, null);
					List<Element> reservations = XMLUtils.getXMLObjects(cas.getRUP(), xpath);
					List<Map<String, Object>> lanes4Date = getMatchingLanes(reservations, allLanes4Date);
					logger.debug(lanes4Date.size() + " lanes found for caseId: " + cas.getCaseId());
									
					for (Map<String, Object> lane4Date : lanes4Date) {
						List<Map<String, Object>> processUnits = (List)lane4Date.get("processUnits");
						if (processUnits==null) {
							processUnits = new ArrayList<Map<String, Object>>();
						}
						if (debug) put(data, "note4", lane4Date.get("title"));
						Map<String, Object> processUnitCopy = (Map<String, Object>) Utils.deepCopy(processUnit);
						put(processUnitCopy, "id", "id" + processUnitCopy.get("caseId") + "-" + count++);
						processUnits.add(processUnitCopy);
						put(lane4Date, "processUnits", processUnits);
					}
				} catch (Throwable e) {
					logger.error("cannot show case: " + cas.getCaseId(), e);
				}
			}
			allLanes4AllDates.add(allLanes4Date);
		}
		return allLanes4AllDates;
	}
	
	/**
	 * lanes finden durch pr�fen von rolle bzw. category 
	 */
	private List<Map<String, Object>> getMatchingLanes(List<Element> reservations, List<Map<String, Object>> allLanes4Date) {
		List<Map<String, Object>> lanes4Date = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> lane4Date : allLanes4Date) {
			for (Element reservation : reservations) {
				try {
					String statusToBe = reservation.getChildText(XML_STATUSTOBE);
					if (!statusToBe.equals(RESOURCE_STATUS_REQUESTED) && !statusToBe.equals(RESOURCE_STATUS_RESERVED)) {
						continue;
					}
					
					int workload = Integer.parseInt(reservation.getChildText(XML_WORKLOAD));
					if (workload == 0) {
						continue;
					}
					
					Element resource = reservation.getChild(XML_RESOURCE);
					String resourceId = resource.getChildText(XML_ID);
					String categoryId = resource.getChildText(XML_CATEGORY);
					String roleId = resource.getChildText(XML_ROLE);
					Object laneId = lane4Date.get("id");
					//logger.debug("resourceId="+resourceId+", categoryId="+categoryId+", roleId="+roleId+", laneId="+laneId);
					if (!"virtual".equals(laneId) && resourceId.equals(laneId)) { // match by Id
						lanes4Date.add(lane4Date);
						//logger.debug("match by Id, resourceId="+resourceId+", laneId="+lane4Date.get("id"));
						break;
					}	else if (resourceId.isEmpty() && "virtual".equals(laneId)) {
						if (categoryId.equals(this.categoryId)) { // match by category
							lanes4Date.add(lane4Date);
							//logger.debug("match by category, categoryId="+categoryId);
							break;
						} else if (roleId.equals(this.roleId)) { // match by role
							lanes4Date.add(lane4Date);
							//logger.debug("match by role, roleId="+roleId);
							break;
						}
					}
				} catch (Exception e) {
					logger.error("cannot search for matching lane", e);
				}
			}
		}
		
		// if no lane found (means that no matching resource was defined in rup) then add to virtualLane 
		if (lanes4Date.isEmpty()) {
			Map<String, Object> virtualLane4Date = allLanes4Date.get(allLanes4Date.size()-1);
			lanes4Date.add(virtualLane4Date);
		}
				
		return lanes4Date;
	}
	
	/**
	 * replace null values by ""
	 */
	private void put(Map<String, Object> map, String key, Object value) {
		map.put(key, value==null ? "" : value);
	}
	
	private String getInitials(Participant participant) {
		return (participant.getFirstName().substring(0, 1) +
                participant.getLastName()).substring(0, titleShortLength);
	}
}
