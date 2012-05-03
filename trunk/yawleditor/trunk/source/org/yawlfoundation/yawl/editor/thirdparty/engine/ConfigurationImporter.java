package org.yawlfoundation.yawl.editor.thirdparty.engine;

import org.jdom2.Element;
import org.yawlfoundation.yawl.editor.actions.net.ConfigurableTaskAction;
import org.yawlfoundation.yawl.editor.elements.model.*;
import org.yawlfoundation.yawl.editor.net.NetGraph;
import org.yawlfoundation.yawl.editor.net.NetGraphModel;
import org.yawlfoundation.yawl.editor.specification.ProcessConfigurationModel;

import java.awt.event.ActionEvent;
import java.util.*;

public class ConfigurationImporter {
	
	public static HashMap map = new HashMap();
	public static LinkedList<YAWLTask> CTaskList = new LinkedList();
	public static HashMap NetTaskMap = new HashMap();
	public ConfigurationImporter(){
		
	}

	public static void ApplyConfiguration(){

		while(!CTaskList.isEmpty()){
			YAWLTask task = CTaskList.removeFirst();
			NetGraphModel netModel = (NetGraphModel) NetTaskMap.get(task);
			NetGraph net = netModel.getGraph();
			Element root = (Element) map.get(task);
			Element configuration = root.getChild("configuration", root.getNamespace());
			Element defaultConfiguration = root.getChild("defaultConfiguration", root.getNamespace());
			if(configuration != null){
				 ConfigurableTaskAction simulateAction = new ConfigurableTaskAction(task,net);
	    		 ActionEvent event = new ActionEvent(net,1001, "Configurable Task");
	    		 simulateAction.actionPerformed(event);
	    		 
	    		 ApplyOutputCPorts(task, configuration);
	    		 ApplyInputCPorts(task, configuration);
	    		 
	    		 ApplyCancellationSetConfiguration(task, configuration);
	    		 MultipleInstanceConfiguration(task, configuration);
				if(defaultConfiguration != null){
					ApplyDefaultInputCPorts(task,defaultConfiguration);
					ApplyDefaultOutputCPorts(task,defaultConfiguration);
				}
			}
        ProcessConfigurationModel.getInstance().refresh();  
		}
		
	}

	private static void MultipleInstanceConfiguration(YAWLTask task,
			Element configuration) {
		Element MultipleTask = configuration.getChild("nofi",configuration.getNamespace());
		 if(MultipleTask != null){
			 MultipleInstanceTaskConfigSet configSet = new MultipleInstanceTaskConfigSet((YAWLMultipleInstanceTask) task);
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

	private static void ApplyCancellationSetConfiguration(YAWLTask task,
			Element configuration) {
		Element cancellation = configuration.getChild("rem",configuration.getNamespace());
		 if(cancellation != null){
			 if(cancellation.getAttributeValue("value").equals(CPort.ACTIVATED)){
				 task.setCancellationSetEnable(true);
			 }else{
				 task.setCancellationSetEnable(false);
			 }
		 }
	}

	private static void ApplyInputCPorts(YAWLTask task, Element configuration) {
		
		
		 YAWLFlowRelation[] inFlows = new YAWLFlowRelation[task.getIncomingFlowCount()];
		 inFlows = task.getIncomingFlows().toArray(inFlows);
		 HashMap flowMap = new HashMap();
		 for(YAWLFlowRelation flow : inFlows){
			 flowMap.put(flow.getSourceVertex().getEngineId(), flow);
		 }
		 Element inputConfig = configuration.getChild("join",configuration.getNamespace());
		 if(inputConfig != null){
		 List InputPortsElement = inputConfig.getChildren("port",inputConfig.getNamespace());
		 ArrayList<CPort> InputPorts = new ArrayList<CPort>();
		 int i= 0;
		 for(Object o:InputPortsElement){
			 Element portElement = (Element) o;
			 CPort port = new CPort(task, CPort.INPUTPORT);
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
		 task.setInputCPorts(InputPorts);
		 }
	}

	
	private static void ApplyDefaultInputCPorts(YAWLTask task, Element defaultConfiguration){
		 YAWLFlowRelation[] inFlows = new YAWLFlowRelation[task.getIncomingFlowCount()];
		 inFlows = task.getIncomingFlows().toArray(inFlows);
		 HashMap flowMap = new HashMap();
		 for(YAWLFlowRelation flow : inFlows){
			 flowMap.put(flow.getSourceVertex().getEngineId(), flow);
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
			 for(CPort port: task.getInputCPorts()){
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
	
	private static void ApplyDefaultOutputCPorts(YAWLTask task, Element defaultConfiguration) {
		 YAWLFlowRelation[] outFlows = new YAWLFlowRelation[task.getOutgoingFlowCount()];
		 outFlows = task.getOutgoingFlows().toArray(outFlows);
		 HashMap flowMap = new HashMap();
		 for(YAWLFlowRelation flow : outFlows){
			 flowMap.put(flow.getTargetVertex().getEngineId(), flow);
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
			 for(CPort port: task.getOutputCPorts()){
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
	
	private static void ApplyOutputCPorts(YAWLTask task, Element configuration) {
		 YAWLFlowRelation[] outFlows = new YAWLFlowRelation[task.getOutgoingFlowCount()];
		 outFlows = task.getOutgoingFlows().toArray(outFlows);
		 HashMap flowMap = new HashMap();
		 for(YAWLFlowRelation flow : outFlows){
			 flowMap.put(flow.getTargetVertex().getEngineId(), flow);
		 }
		 Element outputConfig = configuration.getChild("split",configuration.getNamespace());
		 if(outputConfig != null){
		 List outputPortsElement = outputConfig.getChildren("port",outputConfig.getNamespace());
		 ArrayList<CPort> outputPorts = new ArrayList<CPort>();
		 int i= 0;
		 for(Object o:outputPortsElement){
			 Element portElement = (Element) o;
			 CPort port = new CPort(task, CPort.OUTPUTPORT);
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
		 task.setOutputCPorts(outputPorts);
	}
	}

	
	
}
