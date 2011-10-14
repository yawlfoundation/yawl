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

package org.yawlfoundation.yawl.scheduling.resource;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.yawlfoundation.yawl.exceptions.YAWLException;
import org.yawlfoundation.yawl.resourcing.resource.AbstractResource;
import org.yawlfoundation.yawl.resourcing.resource.Capability;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayException;
import org.yawlfoundation.yawl.scheduling.ConfigManager;
import org.yawlfoundation.yawl.scheduling.Constants;
import org.yawlfoundation.yawl.scheduling.util.*;
import org.yawlfoundation.yawl.scheduling.util.XMLUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * mock implementation of wrapping interface to RS interface methods
 * 
 * @author tbe
 * @version $Id$
 *
 */
public class ResourceServiceInterfaceMock implements Constants {
	private static Logger logger = Logger.getLogger(ResourceServiceInterfaceMock.class);
	
	private static ResourceServiceInterfaceMock instance = null;
	private ConfigManager config;
	
	private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyy HH:mm");

	private static List<Reservation> reservations = new ArrayList<Reservation>();
//	private static List<Period> periods = new ArrayList<Period>();

//	private static List<AbstractResource> resources = new ArrayList<AbstractResource>();
//	private static List<Role> roles = new ArrayList<Role>();
//	private static List<Capability> capabilities = new ArrayList<Capability>();
//	private static Map<String, String[]> types = new HashMap<String, String[]>();
	
	private ResourceServiceInterfaceMock() throws Exception {		
		logger.info("ResourceServiceInterfaceMock starting...");		
		config = ConfigManager.getInstance();

//		periods.add(new Period(sdf.parse("11.05.2010 11:00"), sdf.parse("11.05.2010 12:00")));
//		periods.add(new Period(sdf.parse("19.05.2010 08:00"), sdf.parse("19.05.2010 14:00")));
//		periods.add(new Period(sdf.parse("19.05.2010 15:00"), sdf.parse("19.05.2010 19:00")));
//		
//		Set<Position> posSet = new HashSet<Position>();
//		posSet.add(new Position("Member"));
//		
//		Role surgeon = new Role("Surgeon");
//		surgeon.setID("Surgeon");
//		roles.add(surgeon);
//		
//		Role anesthetican = new Role("Anesthetican");
//		anesthetican.setID("Anesthetican");
//		roles.add(anesthetican);
//		
//		Role circNurse = new Role("CirculatingNurse");
//		circNurse.setID("CirculatingNurse");
//		roles.add(circNurse);
//
//		Role nurse = new Role("Nurse");
//		nurse.setID("Nurse");
//		roles.add(nurse);
//		
//		Role caretaker = new Role("Caretaker");
//		caretaker.setID("Caretaker");
//		roles.add(caretaker);
//		
//		Set<Role> roleSetSurgeon = new HashSet<Role>();
//		roleSetSurgeon.add(surgeon);
//		
//		Set<Role> roleSetAnes = new HashSet<Role>();
//		roleSetAnes.add(anesthetican);
//		
//		Set<Role> roleSetCircNurse = new HashSet<Role>();
//		roleSetCircNurse.add(circNurse);
//		
//		Set<Role> roleSetNurse = new HashSet<Role>();
//		roleSetNurse.add(nurse);
//		roleSetNurse.add(circNurse);
//		
//		Set<Role> roleSetCare = new HashSet<Role>();
//		roleSetCare.add(caretaker);
//		
//		Capability heart = new Capability("Heart", "Heart");
//		capabilities.add(heart);
//		
//		Set<Capability> capaSet = new HashSet<Capability>();		
//		capaSet.add(heart);
//		
//		resources.add(new Participant("Tom", "Admin", "1", true, posSet, null, null));
//		resources.add(new Participant("House", "Gregory", "2", false, posSet, roleSetSurgeon, capaSet));
//		resources.add(new Participant("Geiger", "Jeffrey", "3", false, posSet, roleSetSurgeon, capaSet));
//		resources.add(new Participant("Shutt", "Aaron", "4", false, posSet, roleSetSurgeon, null));
//		resources.add(new Participant("Ross", "Doug", "5", false, posSet, roleSetAnes, capaSet));
//		resources.add(new Participant("Grey", "Meredith", "6", false, posSet, roleSetAnes, null));
//		resources.add(new Participant("Isolde", "Schwester", "7", false, posSet, roleSetCircNurse, capaSet));
//		resources.add(new Participant("Hilde", "Schwester", "8", false, posSet, roleSetNurse, null));
//		resources.add(new Participant("Erna", "Schwester", "9", false, posSet, roleSetNurse, null));
//		resources.add(new Participant("Johnson", "Dwayne", "10", false, posSet, roleSetCare, null));
//		resources.add(new NonHumanResource("11", "device1", "1.1"));
//		resources.add(new NonHumanResource("12", "device2", "2.2"));
//		resources.add(new NonHumanResource("13", "device3", "3.3"));
//		resources.add(new NonHumanResource("14", "device3", "3.4"));
//		for (AbstractResource r : resources) {
//			if (r instanceof Participant) {
//				r.setDescription(((Participant)r).getFirstName()+" "+((Participant)r).getLastName());
//				r.setID(((Participant)r).getUserID());
//			}
//		}
//		
//		//Reservation(caseId, utilizationStartTaskId, resource, count, period, planningStatus, workload)
//		reservations.add(new Reservation("123", "mach wat", resources.get(1), periods.get(0), XMLUtils.PLANNING_STATUS_RESERVED, 100));
//		reservations.add(new Reservation("123", "do something", resources.get(2), periods.get(0), XMLUtils.PLANNING_STATUS_RESERVED, 100));
//		reservations.add(new Reservation("123", "dawai", resources.get(3), periods.get(0), XMLUtils.PLANNING_STATUS_RESERVED, 100));
//		reservations.add(new Reservation("123", "hurtig", resources.get(11), periods.get(0), XMLUtils.PLANNING_STATUS_RESERVED, 100));
//
//		types.put("device1", new String[] {"1.1","1.2","1.3","1.4"});
//		types.put("device2", new String[] {"2.1","2.2","2.3","2.4"});
//		types.put("device3", new String[] {"3.1","3.2","3.3","3.4"});
	}
	
	public static ResourceServiceInterfaceMock getInstance() {
		if (instance==null) {
			try {
				instance = new ResourceServiceInterfaceMock();
			} catch (Exception e) {
				logger.error("cannot instantiate", e);
			}
		}
		return instance;
	}
	
	/**
	 * scheduling service get list of periods between given period, in which ALL resources, matching given resources,
	 * are available
	 * @param resource
	 * @param from
     * @param to
	 * @return list of reservation element 
	 * @throws YAWLException
	 */
	public List<Element> getAvailabilities(String resource, Date from, Date to) throws YAWLException, IOException {
		return new ArrayList<Element>();
	}

	public Map<String, String> getDropdownContent(String objectName, String prevFieldValue)
	throws ResourceGatewayException, IOException {
		return new TreeMap<String, String>();
	}

	/**
	 * return matching resources
	 * @param resource
	 * @return
	 * @throws IOException
	 * @throws ResourceGatewayException
	 */
	private List<AbstractResource> getMatchingResources(Element resource) throws IOException, ResourceGatewayException {
		String resourceId = resource.getChildText(XML_ID);
		String roleId = resource.getChildText(XML_ROLE);
		String capabilityId = resource.getChildText(XML_CAPABILITY);
		String category = resource.getChildText(XML_CATEGORY);
		String subcategory = resource.getChildText(XML_SUBCATEGORY);
//		logger.debug("resourceId="+resourceId+", roleId="+roleId+", capabilityId="+capabilityId+
//				", category="+category+", subcategory="+subcategory);

        ResourceServiceInterface rs = ResourceServiceInterface.getInstance();
		List<AbstractResource> allResources = new ArrayList<AbstractResource>();
		allResources.addAll(rs.getParticipants());
		allResources.addAll(rs.getNonHumanResources());
		
		List<AbstractResource> resourcesAvailable = new ArrayList<AbstractResource>();
		boolean idMatch = false;
		for (AbstractResource resourceAvailable : allResources) {
			boolean roleMatch = false, capabilityMatch = false, categoryMatch = false, subcategoryMatch = false;
			if (resourceAvailable instanceof Participant) {
				Participant par = (Participant)resourceAvailable;
				List<Role> roles = rs.getParticipantRoles(par.getID());
				List<Capability> capabilities = rs.getParticipantCapabilities(par.getID());
//				logger.debug("available Participant: "+par.getID()+", roles="+Utils.toString(roles)+", capabilities="+Utils.toString(capabilities));
				roleMatch = containsRole(roles, roleId);
				capabilityMatch = containsCapability(capabilities, capabilityId);
			} else if (resourceAvailable instanceof NonHumanResource) {
				NonHumanResource non = (NonHumanResource)resourceAvailable;
//				logger.debug("available NonHumanResource: "+non.getID()+", cat="+non.getCategory()+", subcat="+non.getSubCategory());
				categoryMatch = category.equals(non.getCategory());
				subcategoryMatch = 	subcategory.equals(non.getSubCategory());
			} else {
				logger.error("unknown AbstractResource: "+resourceAvailable.getID());
			}
//			logger.debug("match: "+roleMatch+", "+capabilityMatch+", "+typeMatch+", "+subTypeMatch);
			
			if ((resourceId.isEmpty() || resourceId.equals(resourceAvailable.getID()))
					&&
					(roleId.isEmpty() || roleMatch)
					&&
					(capabilityId.isEmpty() || capabilityMatch)
					&&
					(category.isEmpty() || categoryMatch)
					&&
					(subcategory.isEmpty() || subcategoryMatch)
					)
			{
				if (resourceId.isEmpty() && roleId.isEmpty() && capabilityId.isEmpty()
						&& category.isEmpty() && subcategory.isEmpty()) {
					XMLUtils.addWarningValue(resource.getParentElement(), "msgDummyResource", null);
				}
				resourcesAvailable.add(resourceAvailable);
				continue;
			}
			
			if (!resourceId.isEmpty() && resourceId.equals(resourceAvailable.getID())) {
				if (!roleId.isEmpty() && !roleMatch) {
					XMLUtils.addErrorValue(resource.getChild(XML_ROLE), true, "msgHasNoRole", null);
				}
				if (!capabilityId.isEmpty() && !capabilityMatch) {
					XMLUtils.addErrorValue(resource.getChild(XML_CAPABILITY), true, "msgHasNoCapability", null);
				}					
				if (!category.isEmpty() && !categoryMatch) {
					XMLUtils.addErrorValue(resource.getChild(XML_CATEGORY), true, "msgHasNoType", null);
				}
				if (!subcategory.isEmpty() && !subcategoryMatch) {
					XMLUtils.addErrorValue(resource.getChild(XML_SUBCATEGORY), true, "msgHasNoSubType", null);
				}
				idMatch = true;
				break;
			}
		}
			
		if (resourcesAvailable.isEmpty()) {
			if (!resourceId.isEmpty() && !idMatch) {
				XMLUtils.addErrorValue(resource.getChild(XML_ID), true, "msgResourceIdNotExist", null);
			} else {
				XMLUtils.addErrorValue(resource.getParentElement(), true, "msgResourceNotExist", null);
			}
		}

		return resourcesAvailable;
	}
	
	private boolean containsRole(List<Role> list, String roleId) {
		if (list == null) {
			return false;
		}
		
		for (Role r : list) {
			if (r.getID().equals(roleId)) {
				return true;
			}
		}
		return false;
	}

	private boolean containsCapability(List<Capability> list, String capabilityId) {
		if (list == null) {
			return false;
		}
		
		for (Capability c : list) {
			if (c.getID().equals(capabilityId)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * save all reservation xml elements of resourceUtilisationPlan, depending on reservation.planningStatus
	 * (see commons-YAWL\src\XML-Schema.xsd to inform about xml structure) and remove or update older reservations for case
	 * and each activity. Then sets planning status and error or warning values on xml elements (e.g. if
	 * resource cannot be reserved or technical error occurs) and returns the resourceUtilisationPlan.
	 * @param rupStr
	 */
	public String saveReservations(String rupStr, boolean checkOnly) throws ResourceGatewayException, JDOMException, IOException {
		Document rup = new Document(Utils.string2Element(rupStr));
		List<Reservation> reservationsTemp = new ArrayList<Reservation>();
		Date now = new Date();
		String caseId = XMLUtils.getElement(rup, XML_RUP + "/" + XML_CASEID).getText();
		List<Element> reservationElements = XMLUtils.getXMLObjects(rup, XMLUtils.getXPATH_ActivityElement(null, XML_RESERVATION, null));		
//		logger.debug("reservations to check: " + reservationElements.size());
		for (int i=0; i<reservationElements.size(); i++) {
			checkOnly = saveReservation(reservationElements.get(i), checkOnly, now, caseId, reservationsTemp);
		}
		
//		logger.debug("checkOnly: " + checkOnly + ", reservationElements.size(): " + reservationElements.size());
		if (checkOnly) {
//			logger.debug("dont save in RS, checkOnly="+checkOnly);
		} else {
			reservations.addAll(reservationsTemp);
			//logger.debug("add "+reservationsTemp.size()+" to reservations");
			for (int i=0; i<reservationElements.size(); i++) {
				Element reservation = reservationElements.get(i);
				Element status = reservation.getChild(XML_STATUS);
				String statusToBe = reservation.getChild(XML_STATUSTOBE).getText();
				status.setText(statusToBe);
//				logger.debug("resourceId: "+reservation.getChild(XML_RESOURCE).getChildText(XML_ID)+", set status="+statusToBe);	
			}
		}
		
//		logger.debug("return rup: " + Utils.element2String(rup.getRootElement(), true));
		return Utils.element2String(rup.getRootElement(), false);
	}
	
	private boolean saveReservation(Element reservation, boolean checkOnly, Date now, String caseId, List<Reservation> reservationsTemp) {
		try {
//		logger.debug("check reservation: " + Utils.element2String(reservation, true));
			Element activity = reservation.getParentElement();
			String activityName = activity.getChildText(XML_ACTIVITYNAME);
			Date from = XMLUtils.getDateValue(activity.getChild(XML_FROM), true);
			Date to = XMLUtils.getDateValue(activity.getChild(XML_TO), true);			
			Element resource = reservation.getChild(XML_RESOURCE);			
			Element workload = reservation.getChild(XML_WORKLOAD);
			Integer workloadValue = XMLUtils.getIntegerValue(workload, true);
			
			List<AbstractResource> abstractResources = getMatchingResources(resource);
			//logger.debug("abstractResource="+abstractResource+",from="+from+",to="+to+",workload="+workload+",workloadValue="+workloadValue);			
			if (abstractResources.isEmpty() || from==null || to == null || XMLUtils.hasErrors(workload)) {
				return true;
			}
			
			if (workloadValue == 0) {
				XMLUtils.addWarningValue(workload, "msgDummyValue", null);
			}

			//String error = checkReservation(reservationsTemp, caseId, activityName, resourceIdStr, from, to, workloadValue);
			//private String checkReservation(List<Reservation> reservationsTemp, String caseId, String activityName, String resourceId, Date from, Date to, int workload)
			String error = null;
			String[] errorValues = null;
			List<Reservation> reservationsCheck = new ArrayList<Reservation>();
			reservationsCheck.addAll(reservations);
			reservationsCheck.addAll(reservationsTemp);		
			AbstractResource abstractResource = null;
			for (int i=0; i<abstractResources.size(); i++) {
				abstractResource = abstractResources.get(i);
				error = null;
				errorValues = null;
				
				// Workload wird nicht korrekt gepr�ft, muss pro Ressource aufsummiert werden, f�rn Mock reichts aber
				for (Reservation r : reservationsCheck) {
					//logger.debug("r.resource.getID()="+r.resource.getID()+",resourceId="+resourceId+",r.workload="+r.workload+",workload="+workload);
					//logger.debug("r.period.from="+r.period.from+",r.period.to="+r.period.to+",from="+from+",to="+to);
					if (r.resource.getID()!=null
							&&
							r.resource.getID().equals(abstractResource.getID())
							&&
							(r.workload + workloadValue) > 100
							&&
							(r.period.to.getTime() > now.getTime() &&  to.getTime() > now.getTime()) // Vergangenheit nicht pr�fen
							&&
							(
								(r.period.from.getTime() >= from.getTime() && r.period.from.getTime() <= to.getTime())
							  ||
							  (r.period.to.getTime() >= from.getTime() && r.period.to.getTime() <= to.getTime())
							)) {
						error = "msgCaseCollision";
						errorValues = new String[]{r.caseId, r.activityName,
								sdf.format(new Date(Math.max(r.period.from.getTime(), from.getTime()))),
								sdf.format(new Date(Math.min(r.period.to.getTime(), to.getTime())))};
						break;
					}
				}
				
				if (error == null) {
					break; // passende Ressource gefunden
				}
			}
						
			Element status = reservation.getChild(XML_STATUS);
			String statusToBe = reservation.getChild(XML_STATUSTOBE).getText();
			if (error != null) {
				XMLUtils.addErrorValue(reservation, true, error, errorValues);
				status.setText(RESOURCE_STATUS_NOTAVAILABLE);
			} else {
				reservationsTemp.add(new Reservation(caseId, activityName, abstractResource,
					new Period(from, to), statusToBe, workloadValue));
				status.setText(RESOURCE_STATUS_AVAILABLE);
				reservation.getChild(XML_RESERVATIONID).setText("123");
			}
			//logger.debug("reservation checked: " + Utils.element2String(reservation, true));
			
			return checkOnly || XMLUtils.hasErrors(reservation);			
		} catch (Exception e) {
			logger.error("Exception", e);
			XMLUtils.addErrorValue(reservation, true, "msgRUPSaveRSError", new String[]{e.getMessage()});
			return true;
		}
	}
}
