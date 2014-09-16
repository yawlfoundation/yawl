/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.configuration;

import org.jdom2.Element;
import org.yawlfoundation.yawl.configuration.element.TaskConfiguration;
import org.yawlfoundation.yawl.configuration.menu.action.ConfigurableTaskAction;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLFlowRelation;
import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLMultipleInstanceTask;
import org.yawlfoundation.yawl.editor.ui.net.NetGraph;

import java.awt.event.ActionEvent;
import java.util.*;

public class ConfigurationImporter {
	
	public static final Map<TaskConfiguration, Element> map =
            new HashMap<TaskConfiguration, Element>();
	public static final LinkedList<TaskConfiguration> CTaskList =
            new LinkedList<TaskConfiguration>();

	public ConfigurationImporter(){
		
	}

	public static void ApplyConfiguration(){

		while(!CTaskList.isEmpty()){
            TaskConfiguration taskConfiguration = CTaskList.removeFirst();
			NetGraph net = taskConfiguration.getGraphModel().getGraph();
			Element root = map.get(taskConfiguration);
			Element configuration = root.getChild("configuration", root.getNamespace());
			Element defaultConfiguration = root.getChild("defaultConfiguration", root.getNamespace());
			if(configuration != null){
				 ConfigurableTaskAction simulateAction =
                         new ConfigurableTaskAction(taskConfiguration.getTask(),net);
	    		 ActionEvent event = new ActionEvent(net,1001, "Configurable Task");
	    		 simulateAction.actionPerformed(event);
	    		 
	    		 ApplyOutputCPorts(taskConfiguration, configuration);
	    		 ApplyInputCPorts(taskConfiguration, configuration);
	    		 
	    		 ApplyCancellationSetConfiguration(taskConfiguration, configuration);
	    		 MultipleInstanceConfiguration(taskConfiguration, configuration);
				if(defaultConfiguration != null){
					ApplyDefaultInputCPorts(taskConfiguration,defaultConfiguration);
					ApplyDefaultOutputCPorts(taskConfiguration,defaultConfiguration);
				}
			}
        ProcessConfigurationModel.getInstance().refresh();  
		}
		
	}

	private static void MultipleInstanceConfiguration(TaskConfiguration taskConfig,
			Element configuration) {
		Element MultipleTask = configuration.getChild("nofi",configuration.getNamespace());
		 if(MultipleTask != null){
			 MultipleInstanceTaskConfigSet configSet =
                     new MultipleInstanceTaskConfigSet((YAWLMultipleInstanceTask) taskConfig.getTask());
			 String value1 = MultipleTask.getChildText("minIncrease");
			 int min = 0;
			 if(value1 != null){
				 min = new Integer(value1);
			 }
			 
			 String value2 = MultipleTask.getChildText("maxDecrease");
			 int max = 0;
			 if(value2 != null){
				 max = new Integer(value2);
			 }
			 
			 String value3 = MultipleTask.getChildText("thresIncrease");
			 int threshold = 0;
			 if(value3 != null){
				 threshold = new Integer(value3);
			 }
			 
			 String createMode = MultipleTask.getChildText("creationMode",MultipleTask.getNamespace());
			 
			 System.out.println(createMode);
			 
			 if(createMode.equals("keep")){
			 configSet.setForbidDynamic(false);
			 } else {
				 configSet.setForbidDynamic(true);
			 }
			 
			 configSet.setIncreaseMin(min);
			 configSet.setReduceMax(max);
			 configSet.setIncreaseThreshold(threshold);
		 }
	}

	private static void ApplyCancellationSetConfiguration(TaskConfiguration taskConfig,
			Element configuration) {
		Element cancellation = configuration.getChild("rem",configuration.getNamespace());
		 if(cancellation != null){
			 if(cancellation.getAttributeValue("value").equals(CPort.ACTIVATED)){
                 taskConfig.setCancellationSetEnable(true);
			 }else{
                 taskConfig.setCancellationSetEnable(false);
			 }
		 }
	}

	private static void ApplyInputCPorts(TaskConfiguration taskConfig, Element configuration) {
		 YAWLFlowRelation[] inFlows = new YAWLFlowRelation[
                 taskConfig.getTask().getIncomingFlowCount()];
		 inFlows = taskConfig.getTask().getIncomingFlows().toArray(inFlows);
		 Map<String, YAWLFlowRelation> flowMap = new HashMap<String, YAWLFlowRelation>();
		 for(YAWLFlowRelation flow : inFlows){
			 flowMap.put(flow.getSourceVertex().getID(), flow);
		 }
		 Element inputConfig = configuration.getChild("join",configuration.getNamespace());
		 if(inputConfig != null){
		 List InputPortsElement = inputConfig.getChildren("port",inputConfig.getNamespace());
		 List<CPort> InputPorts = new ArrayList<CPort>();
		 int i= 0;
		 for(Object o:InputPortsElement){
			 Element portElement = (Element) o;
			 CPort port = new CPort(taskConfig, CPort.INPUTPORT);
			 port.setID(i);
			 port.setConfigurationSetting(portElement.getAttributeValue("value"));
			 HashSet flowSet = new HashSet();
			 List flows = portElement.getChildren("flowSource",portElement.getNamespace());
			 for(Object ob : flows ){
				 Element flow = (Element) ob;
				 flowSet.add(flowMap.get(flow.getAttributeValue("id")));
			 }
			 port.setFlows(flowSet);
			 InputPorts.add(port);
			 i++;
		 }
             taskConfig.setInputCPorts(InputPorts);
		 }
	}

	
	private static void ApplyDefaultInputCPorts(TaskConfiguration taskConfig, Element defaultConfiguration){
		 YAWLFlowRelation[] inFlows = new YAWLFlowRelation[taskConfig.getTask().getIncomingFlowCount()];
		 inFlows = taskConfig.getTask().getIncomingFlows().toArray(inFlows);
		 HashMap flowMap = new HashMap();
		 for(YAWLFlowRelation flow : inFlows){
			 flowMap.put(flow.getSourceVertex().getID(), flow);
		 }
		 Element inputConfig = defaultConfiguration.getChild("join",defaultConfiguration.getNamespace());
		 if(inputConfig != null){
		 List InputPortsElement = inputConfig.getChildren("port",inputConfig.getNamespace());
		 for(Object o:InputPortsElement){
			 Element portElement = (Element) o;
			 HashSet flowSet = new HashSet();
			 List flows = portElement.getChildren("flowSource",portElement.getNamespace());
			 for(Object ob : flows ){
				 Element flow = (Element) ob;
				 flowSet.add(flowMap.get(flow.getAttributeValue("id")));
			 }
			 CPort myPort = null;
			 for(CPort port: taskConfig.getInputCPorts()){
				 if(port.getFlows().equals(flowSet)){
					 myPort = port;
					 break;
				 }
			 }
			 if(myPort != null){
				 myPort.setDefaultValue(portElement.getAttributeValue("value"));
			 }
		 	}
		 }
	}
	
	private static void ApplyDefaultOutputCPorts(TaskConfiguration taskConfig, Element defaultConfiguration) {
		 YAWLFlowRelation[] outFlows = new YAWLFlowRelation[taskConfig.getTask().getOutgoingFlowCount()];
		 outFlows = taskConfig.getTask().getOutgoingFlows().toArray(outFlows);
		 HashMap flowMap = new HashMap();
		 for(YAWLFlowRelation flow : outFlows){
			 flowMap.put(flow.getTargetVertex().getID(), flow);
		 }
		 Element outputConfig = defaultConfiguration.getChild("split",defaultConfiguration.getNamespace());
		 if(outputConfig != null){
		 List OutputPortsElement = outputConfig.getChildren("port",outputConfig.getNamespace());
		 for(Object o:OutputPortsElement){
			 Element portElement = (Element) o;
			 HashSet flowSet = new HashSet();
			 List flows = portElement.getChildren("flowDestination",portElement.getNamespace());
			 for(Object ob : flows ){
				 Element flow = (Element) ob;
				 flowSet.add(flowMap.get(flow.getAttributeValue("id")));
			 }
			 CPort myPort = null;
			 for(CPort port: taskConfig.getOutputCPorts()){
				 if(port.getFlows().equals(flowSet)){
					 myPort = port;
					 break;
				 }
			 }
			 if(myPort != null){
				 myPort.setDefaultValue(portElement.getAttributeValue("value"));
				 //System.out.println("set deafault output");
			 }
		 	}
		 }
	}
	
	private static void ApplyOutputCPorts(TaskConfiguration taskConfig, Element configuration) {
		 YAWLFlowRelation[] outFlows = new YAWLFlowRelation[
                 taskConfig.getTask().getOutgoingFlowCount()];
		 outFlows = taskConfig.getTask().getOutgoingFlows().toArray(outFlows);
		 HashMap flowMap = new HashMap();
		 for(YAWLFlowRelation flow : outFlows){
			 flowMap.put(flow.getTargetVertex().getID(), flow);
		 }
		 Element outputConfig = configuration.getChild("split",configuration.getNamespace());
		 if(outputConfig != null){
		 List outputPortsElement = outputConfig.getChildren("port",outputConfig.getNamespace());
		 ArrayList<CPort> outputPorts = new ArrayList<CPort>();
		 int i= 0;
		 for(Object o:outputPortsElement){
			 Element portElement = (Element) o;
			 CPort port = new CPort(taskConfig, CPort.OUTPUTPORT);
			 port.setID(i);
			 port.setConfigurationSetting(portElement.getAttributeValue("value"));
			 HashSet flowSet = new HashSet();
			 List flows = portElement.getChildren("flowDestination",portElement.getNamespace());
			 for(Object ob : flows ){
				 Element flow = (Element) ob;
				 flowSet.add(flowMap.get(flow.getAttributeValue("id")));
			 }
			 port.setFlows(flowSet);
			 outputPorts.add(port);
			 i++;
		 }
             taskConfig.setOutputCPorts(outputPorts);
	}
	}

	
	
}
