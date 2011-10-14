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
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.jdom.JDOMXPath;
import org.jdom.*;
import org.yawlfoundation.yawl.scheduling.util.PropertyReader;
import org.yawlfoundation.yawl.scheduling.util.Utils;

import java.io.IOException;
import java.util.*;


/**
 * get list of activities from process model actually loaded in engine
 * 
 * @author tbe
 * @version $Id: PlanningGraphCreator.java 19933 2010-06-22 14:05:02Z tbe $
 */
public class PlanningGraphCreator implements Constants
{
	private static Logger logger = Logger.getLogger(PlanningGraphCreator.class);

	private static PlanningGraphCreator instance = null;
	private ConfigManager config;
	private static int lenActName = XML_ACTIVITYNAME.length()+2;
	
	private PlanningGraphCreator() {
		logger.info("PlanningGraphCreator starting...");

		try {
			config = ConfigManager.getInstance();
		} catch (Exception e) {
			logger.error("cannot load properties", e);
		}
	}
	
	public static PlanningGraphCreator getInstance() {
		if (instance==null) {
			instance = new PlanningGraphCreator();
		}
		return instance;
	}


	public Element getSpecification(String caseId) throws IOException, JDOMException {
		return SchedulingService.getInstance().getSpecificationForCase(caseId);
	}

	@SuppressWarnings("unchecked")
	/**
	 * TODO@tbe: determine order of activities, store them as utilisation relations and sort
	 * activities according to utilisation relations
	 */
	public Collection<Element> getActivityElements(String caseId) throws Exception {
		Element element = getSpecification(caseId);
		Document doc = new Document(element);
		
	  HashMap map = new HashMap();
	  map.put("bla", element.getNamespace().getURI());
	  for (Object o : element.getAdditionalNamespaces()) {
		  map.put(((Namespace)o).getPrefix(), ((Namespace)o).getURI());
	  }
	  
	  org.jaxen.XPath xp = new JDOMXPath("//bla:expression/@query");
	  xp.setNamespaceContext(new SimpleNamespaceContext(map));
	  List<Attribute> list = xp.selectNodes(doc);
		//logger.debug("Attributelist: " + Utils.toString(list));
	  
	  List<String> activityNames = new ArrayList<String>();
		for (Attribute e : list) {
			String value = e.getValue();
			int idx1 = value.indexOf("<"+XML_ACTIVITYNAME+">"), idx2 = value.indexOf("</"+XML_ACTIVITYNAME+">");
			while (idx1>=0 && idx2>idx1) {
				if (!value.startsWith("<"+XML_EVENT_RECEIVE+">")) {
					String activityName = value.substring(idx1+lenActName, idx2).trim();
					if (!activityName.contains("/") && !activityNames.contains(activityName)) { // ignore XPath expressions
						activityNames.add(activityName);
						//logger.debug("add activity: " + activityName);
					}
				}
				value = value.substring(idx2+lenActName);
				idx1 = value.indexOf("<"+XML_ACTIVITYNAME+">");
				idx2 = value.indexOf("</"+XML_ACTIVITYNAME+">");
			}
		}
		
		//TODO@tbe: sort activities, only until order of activities can be determined from process model
		final List<String> possibleActivities = 
			Utils.parseCSV(PropertyReader.getInstance().getSchedulingProperty(
                    "possibleActivitiesSorted"));
		Collections.sort(activityNames, new Comparator<String>() {
	    public int compare(String a1, String a2) {
	    	if (possibleActivities.indexOf(a1)<0) {
	    		return -1; // set missing activities at beginning
	    	} else if (possibleActivities.indexOf(a2)<0) {
	    		return 1; // set missing activities at beginning
	    	} else {
		    	return possibleActivities.indexOf(a1) - possibleActivities.indexOf(a2);
	    	}
	    }    
		});		
		logger.debug("activityNames: " + Utils.toString(activityNames));
		
		Collection<Element> activities = new ArrayList<Element>();
		Element preRelation = null; // relation of previous activity
		for (int i=0; i<activityNames.size(); i++) {
			String activityName = activityNames.get(i);
			if (i>0) {
				preRelation.getChild(XML_OTHERACTIVITYNAME).setText(activityName);
			}
			
			Element activity = FormGenerator.getTemplate(XML_ACTIVITY);
			activities.add(activity);
			activity.getChild(XML_ACTIVITYNAME).setText(activityName);
			
			if (i<activityNames.size()-1) {
				preRelation = FormGenerator.getTemplate(XML_UTILISATIONREL);
				/*preRelation.getChild(XML_THISUTILISATIONTYPE).setText(UTILISATION_TYPE_END);
				preRelation.getChild(XML_OTHERUTILISATIONTYPE).setText(UTILISATION_TYPE_BEGIN);
				preRelation.getChild(XML_MIN).setText(XMLUtils.getDefaultFromSchema(XML_MIN));
				preRelation.getChild(XML_MAX).setText(XMLUtils.getDefaultFromSchema(XML_MAX));*/
				activity.addContent(preRelation);
			}
		}
		//logger.debug("activities: "+Utils.toString(activities));
		return activities;
	}
	/*public Collection<Element> getActivityElements(String caseId) throws Exception {
		Element element = getSpecification(caseId);
		Namespace ns = element.getNamespace();
		Document doc = new Document(element);
		
	  HashMap map = new HashMap();
	  map.put("bla", ns.getURI());
	  for (Object o : element.getAdditionalNamespaces()) {
		  map.put(((Namespace)o).getPrefix(), ((Namespace)o).getURI());
	  }
	  
	  //org.jaxen.XPath xp = new JDOMXPath("//bla:task[bla:startingMappings/bla:mapping/bla:expression/@query!='&lt;"+XML_UTILISATION_TYPE+"&gt;"+UTILISATION_TYPE_END+"&lt;/"+XML_UTILISATION_TYPE+"&gt;']");
	  org.jaxen.XPath xp = new JDOMXPath("//bla:task[bla:startingMappings/bla:mapping/bla:expression]");
	  //org.jaxen.XPath xp = new JDOMXPath("//bla:task[@id='SurgicalProcedure_Begin_69']");
	  //org.jaxen.XPath xp = new JDOMXPath("//bla:task[bla:name=\"SurgicalProcedure Begin\"]");
	  //org.jaxen.XPath xp = new JDOMXPath("//bla:task[bla:name/text()='SurgicalProcedure Begin']");
	  //org.jaxen.XPath xp = new JDOMXPath("//bla:task[bla:startingMappings/bla:mapping/bla:expression/@query]");
	  xp.setNamespaceContext(new SimpleNamespaceContext(map));
	  List<Element> list = xp.selectNodes(doc);
	  HashMap<String, Element> activities = new HashMap<String, Element>();
		for (Element task : list) {
			String activityId = task.getAttributeValue("id");
			//logger.debug("activityId: "+activityId);
			String activityName = null;
			boolean activityEnd = false;
			//logger.debug("startingMappings: "+Utils.element2String(task.getChild("startingMappings", ns), true));
			Element startingMappings = task.getChild("startingMappings", ns);
			for (Element mapping : (List<Element>)startingMappings.getChildren("mapping", ns)) {
				//logger.debug("mapsTo: "+mapping.getChildText("mapsTo", ns));
				// get activity name
				if (mapping.getChildText("mapsTo", ns).equals("+XML_ACTIVITYNAME+")) {
					String activityNameTmp = mapping.getChild("expression", ns).getAttributeValue("query");
					int idx = activityNameTmp.indexOf("</"+"+XML_ACTIVITYNAME+"+">");
					activityNameTmp = activityNameTmp.substring(("<"+"+XML_ACTIVITYNAME+"+">").length(), idx);
					if (activityNameTmp.contains("/")) {
						break; // ignore tasks in subnet
					} else {
						activityName = activityNameTmp;
						//logger.debug("set name: "+activityName);
					}
				}
				
				// get utilisation start task only
				if (mapping.getChildText("mapsTo", ns).equals(XML_UTILISATION_TYPE)) {
					//String uEnd = "&lt;"+XML_UTILISATION_TYPE+"&gt;"+UTILISATION_TYPE_END+"&lt;/"+XML_UTILISATION_TYPE+"&gt;";
					String uEnd = "<"+XML_UTILISATION_TYPE+">"+UTILISATION_TYPE_END+"</"+XML_UTILISATION_TYPE+">";
					String activityStartTmp = mapping.getChild("expression", ns).getAttributeValue("query");
					if (activityStartTmp.equals(uEnd)) {
						activityEnd = true;
						//logger.debug("set end: "+activityEnd);
						break; // ignore utilisation end tasks
					}
				}				
			}
			
			if (activityName != null && !activityEnd) {
				Element act = FormGenerator.getTemplate(XML_ACTIVITY);
				act.getChild(XML_STARTTASKID).setText(activityId);
				act.getChild(XML_NAME).setText(activityName);
				activities.put(activityName, act);				
			}
		}
		//logger.debug("activities: "+Utils.toString(activities));
		return activities.values();
	}*/
	
}
