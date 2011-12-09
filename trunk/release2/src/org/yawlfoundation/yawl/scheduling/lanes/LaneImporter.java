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

package org.yawlfoundation.yawl.scheduling.lanes;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayException;
import org.yawlfoundation.yawl.scheduling.ConfigManager;
import org.yawlfoundation.yawl.scheduling.Constants;
import org.yawlfoundation.yawl.scheduling.Scheduler;
import org.yawlfoundation.yawl.scheduling.SchedulingService;
import org.yawlfoundation.yawl.scheduling.resource.ResourceServiceInterface;
import org.yawlfoundation.yawl.scheduling.util.PropertyReader;
import org.yawlfoundation.yawl.scheduling.util.Utils;
import org.yawlfoundation.yawl.scheduling.util.XMLUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;


public class LaneImporter implements Constants {

    private static Logger logger = Logger.getLogger(LaneImporter.class);

	private ConfigManager config;
	private Scheduler scheduler;
	private String sessionHandle;	
	private Document rup;
	private String caseId;
	private Element earliestFrom;
	public boolean hasErrors;
	public String errors;
	
	public boolean reload = true;
	
	public LaneImporter(String caseId, ConfigManager config, String sessionHandle)
            throws IOException, SQLException {
		this.config = config;
		this.sessionHandle = sessionHandle;
		this.caseId = caseId;
		scheduler = new Scheduler();
		
		rup = SchedulingService.getInstance().loadCase(caseId).getRUP();
		String possibleActivitiesSorted = PropertyReader.getInstance()
                .getSchedulingProperty("possibleActivitiesSorted");
		String[] possibleActivities = Utils.parseCSV(possibleActivitiesSorted).toArray(new String[0]);
		earliestFrom = XMLUtils.getEarliestBeginElement(rup, possibleActivities);
		logger.debug("rup="+rup+", earliestFrom="+earliestFrom);
	}


	public void updateRUP(String start, String categoryOrRoleId, String resourceId)
            throws Exception {
		logger.debug("categoryOrRoleId="+categoryOrRoleId+", resourceId="+resourceId);
        ResourceServiceInterface rs = ResourceServiceInterface.getInstance();

		if (logger.isInfoEnabled()) { // get category/role and resource names for logging only
			String categoryOrRoleName = null;
			String resourceName = "virtual";
			try {
				NonHumanCategory category = rs.getNonHumanCategory(categoryOrRoleId);
				categoryOrRoleName = category.getName();
				resourceName = rs.getNonHumanResource(resourceId).getName();
			}
            catch (ResourceGatewayException e) {   // NonHumanCategory not found
				try {
					Role role = rs.getRole(categoryOrRoleId);
					categoryOrRoleName = role.getName();
					resourceName = rs.getParticipant(resourceId).getFullName();
				}
                catch (ResourceGatewayException e1) {
					// Role not found
				}
			}
			logger.info("------------------------caseId: "+caseId+", set new start time="+
                    start+" and "+categoryOrRoleName+" to "+resourceName);
		}
		
		// set new resourceId
		String xpath = XMLUtils.getXPATH_ActivityElement(null, XML_RESERVATION, null);
		List reservations = XMLUtils.getXMLObjects(rup, xpath);
		boolean reservationFound = false;
		for (Object o : reservations) {
			Element resource = ((Element) o).getChild(XML_RESOURCE);
			String resId = resource.getChildText(XML_ID);
			String categoryId = resource.getChildText(XML_CATEGORY);
			String roleId = resource.getChildText(XML_ROLE);

			if (categoryOrRoleId.equals(categoryId)) {
				update(resource, XML_CATEGORY, resourceId, categoryOrRoleId);
				reservationFound = true;
				continue;
			}
					
			if (categoryOrRoleId.equals(roleId)) {
				update(resource, XML_ROLE, resourceId, categoryOrRoleId);
				reservationFound = true;
				continue;
			}
					
			NonHumanResource nhr = null;
			try {
				nhr = rs.getNonHumanResource(resId);
			} catch (ResourceGatewayException e) {
				// NonHumanResource not found
			}
			
			if (nhr != null) {
				categoryId = nhr.getCategory()==null ? null : nhr.getCategory().getID();
				if (categoryOrRoleId.equals(categoryId)) {
					update(resource, XML_CATEGORY, resourceId, categoryOrRoleId);
					reservationFound = true;
					continue;
				}
			}
					
			Participant par = null;
			try {
				par = rs.getParticipant(resId);
			} catch (ResourceGatewayException e) {
				// Participant not found
			}
			
			if (par != null) {
				List<Role> roles = rs.getParticipantRoles(resId);
				if (roles != null) {
					for (Role r : roles) {
						if (categoryOrRoleId.equals(r.getID())) {
							update(resource, XML_ROLE, resourceId, categoryOrRoleId);
							reservationFound = true;
						}
					}
				}
			}
		}
		
		if (!reservationFound) {
			logger.error("resourceId of rup "+caseId+
                    " was not updated, because no matching resource found");
		}
				
		// set new start time
		XMLUtils.setDateValue(earliestFrom, Utils.string2Date(start, "yyyy-MM-dd HH:mm:ss"));
		reload = scheduler.setTimes(rup, earliestFrom.getParentElement(), true, true, null) || reload;
		
		errors = "";
		Set<String> errorSet = SchedulingService.getInstance().optimizeAndSaveRup(
                rup, rs.getUserName(sessionHandle), null, reservationFound);
		logger.debug("save caseId: "+caseId + ", errors: " +Utils.toString(errorSet));

        for (String error : errorSet) {
			errors += config.getLocalizedJSONString(error) + ";";
		}
		if (errors.isEmpty()) {
			logger.debug("no errors in rup "+caseId+" found");
		}
        else {
			logger.debug("errors in rup "+caseId+" found: "+ errors);
			errors = caseId + ": " + errors;
		}
		
		hasErrors = ! (reservationFound && errors.isEmpty());
	}


	private void update(Element resource, String resType, String resourceId,
                        String categoryOrRoleId) {
		if (resourceId.equals(resource.getChildText(XML_ID))) {
			return; // no resource update necessary
		}
		
		if (resourceId.equals("virtual")) {
			resource.getChild(XML_ID).setText("");
			resource.getChild(resType).setText(categoryOrRoleId);
		}
        else {
			resource.getChild(XML_ID).setText(resourceId);
		}
		
		// remove reservationId to force cancellation and new creation of reservation
		// in RS, because resource changes cannot be updated in RS
		Element reservation = resource.getParentElement();
		if (reservation.getChild(XML_RESERVATIONID) != null) {
			reservation.getChild(XML_RESERVATIONID).setText("");
		}
        else {
			resource.addContent(new Element(XML_RESERVATIONID));
		}		
	}
}
