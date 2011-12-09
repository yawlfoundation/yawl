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

package org.yawlfoundation.yawl.scheduling;

import org.apache.log4j.Logger;
import org.jaxen.JaxenException;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.*;
import org.yawlfoundation.yawl.scheduling.util.PropertyReader;
import org.yawlfoundation.yawl.scheduling.util.Utils;

import java.io.IOException;
import java.util.*;


/**
 * get list of activities from a process model actually loaded in engine
 * 
 * @author tbe
 */
public class PlanningGraphCreator implements Constants {

    private static PlanningGraphCreator INSTANCE = null;
    private static int lenActName = XML_ACTIVITYNAME.length() + 2;
    private static Logger logger = Logger.getLogger(PlanningGraphCreator.class);


	private PlanningGraphCreator() {
		logger.info("PlanningGraphCreator starting...");
	}
	
	public static PlanningGraphCreator getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new PlanningGraphCreator();
		}
		return INSTANCE;
	}


	@SuppressWarnings("unchecked")
	/**
	 * TODO@tbe: determine order of activities, store them as utilisation relations and sort
	 * activities according to utilisation relations
	 */
	public Collection<Element> getActivityElements(String caseId)
            throws IOException, JDOMException, JaxenException {
		Element element = SchedulingService.getInstance().getSpecificationForCase(caseId);
		Document doc = new Document(element);
		
	  Map<String, String> map = new HashMap<String, String>();
	  map.put("bla", element.getNamespace().getURI());
	  for (Object o : element.getAdditionalNamespaces()) {
          Namespace ns = (Namespace) o;
		  map.put(ns.getPrefix(), ns.getURI());
	  }
	  
	  org.jaxen.XPath xp = new JDOMXPath("//bla:expression/@query");
	  xp.setNamespaceContext(new SimpleNamespaceContext(map));
	  List<Attribute> list = xp.selectNodes(doc);
	  List<String> activityNames = new ArrayList<String>();
		for (Attribute e : list) {
			String value = e.getValue();
			int startIndex = value.indexOf("<"+XML_ACTIVITYNAME+">");
            int endIndex = value.indexOf("</"+XML_ACTIVITYNAME+">");
			while (startIndex >= 0 && endIndex > startIndex) {
				if (! value.startsWith("<"+XML_EVENT_RECEIVE+">")) {
					String activityName = value.substring(startIndex+lenActName, endIndex).trim();

                    // ignore XPath expressions
					if (!activityName.contains("/") && !activityNames.contains(activityName)) {
						activityNames.add(activityName);
					}
				}
				value = value.substring(endIndex+lenActName);
				startIndex = value.indexOf("<"+XML_ACTIVITYNAME+">");
				endIndex = value.indexOf("</"+XML_ACTIVITYNAME+">");
			}
		}
		
		//TODO@tbe: sort activities, only until order of activities can be determined from process model
		final List<String> possibleActivities = 
			Utils.parseCSV(PropertyReader.getInstance().getSchedulingProperty(
                    "possibleActivitiesSorted"));
        Collections.sort(activityNames, new Comparator<String>() {
            public int compare(String a1, String a2) {
                if (possibleActivities.indexOf(a1) < 0) {
                    return -1; // set missing activities at beginning
                }
                else if (possibleActivities.indexOf(a2) < 0) {
                    return 1; // set missing activities at beginning
                }
                else {
                    return possibleActivities.indexOf(a1) - possibleActivities.indexOf(a2);
                }
            }
        });
        logger.debug("activityNames: " + Utils.toString(activityNames));
		
		Collection<Element> activities = new ArrayList<Element>();
		Element preRelation = null; // relation of previous activity
		for (int i=0; i<activityNames.size(); i++) {
			String activityName = activityNames.get(i);
			if (i > 0) {
				preRelation.getChild(XML_OTHERACTIVITYNAME).setText(activityName);
			}
			
			Element activity = FormGenerator.getTemplate(XML_ACTIVITY);
			activities.add(activity);
			activity.getChild(XML_ACTIVITYNAME).setText(activityName);
			
			if (i < activityNames.size() - 1) {
				preRelation = FormGenerator.getTemplate(XML_UTILISATIONREL);
				activity.addContent(preRelation);
			}
		}
		return activities;
	}

	
}
