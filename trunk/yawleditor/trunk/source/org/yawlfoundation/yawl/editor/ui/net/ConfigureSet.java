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

/**
 * Created by Jingxin XU on 09/01/2010
 */

package org.yawlfoundation.yawl.editor.ui.net;

import org.yawlfoundation.yawl.editor.ui.configuration.CPort;
import org.yawlfoundation.yawl.editor.ui.elements.model.*;

import java.io.Serializable;
import java.util.*;

/**
 * This class calculates the elements which are needed to canceled.
 * @author n6986668
 *
 */
public class ConfigureSet implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  public static final int NO_Join = 0;
  public static final int NO_Split = 1;
  public static final int AND_Split = 2;
  public static final int XOR_Split = 3;
  
  
  private HashSet<YAWLCell> removeMembers; //This set is used to store all elements that need to be removed
  private HashSet removalTasks;	//This set is used to store the tasks that need to be removed
  private HashSet removalFlows; //This set is used to store the flows that need to be removed
  private HashSet removalConditions; 
  private HashSet<VertexContainer> removeVertexContainer;
  private NetGraphModel model;
  
  private HashSet conditions = new HashSet();
  private HashSet flows = new HashSet();
  private HashSet tasks = new HashSet();
  private HashSet<VertexContainer> vertexContainers = new HashSet<VertexContainer>();
  private HashMap<YAWLTask, Integer> changeTaskDecoration = new HashMap<YAWLTask, Integer>();
  
  
  
  public ConfigureSet(NetGraphModel model) {
  	
  	this.removeMembers = new HashSet<YAWLCell>();
  	this.removalFlows = new HashSet();
  	this.removalTasks = new HashSet();
  	this.removalConditions = new HashSet();
  	this.removeVertexContainer = new HashSet<VertexContainer>();
  	
  	this.model = model;
  	
  	this.initilizeData();
  	setUnavailableFlows();
  	configurationRemoval(this.model);
  	
  	this.changeDecoratorGeneration();
  	
  }
 
  
  private void initilizeData(){
	  Object[] cells = NetGraphModel.getRoots(model); 
	 	 for (int i=0; i < cells.length; i++) {
	 		 
	 		 if (cells[i] instanceof VertexContainer) {
	 			 this.vertexContainers.add((VertexContainer) cells[i]);
		         cells[i] = ((VertexContainer) cells[i]).getVertex();        
		      } 
	 		 if (cells[i] instanceof YAWLFlowRelation) {
	 	        flows.add(cells[i]);        
	 	      }
	 		 if (cells[i] instanceof YAWLTask) {
	 	        tasks.add(cells[i]);        
	 	      }
	 		 if((cells[i] instanceof Condition) && !(cells[i] instanceof InputCondition ) && !(cells[i] instanceof OutputCondition) ){
	 			 this.conditions.add(cells[i]);
	 		 }
	 	 }
	 	 
	 	YAWLFlowRelation[] flowArray = new YAWLFlowRelation[this.flows.size()]; 
		flowArray = (YAWLFlowRelation[]) this.flows.toArray(flowArray);
		for(YAWLFlowRelation flow : flowArray){
			flow.setAvailable(true);
		}
  }
  
  
  private void setUnavailableFlows(){
	  YAWLTask[] taskArray = new YAWLTask[this.tasks.size()]; 
	  taskArray = (YAWLTask[]) this.tasks.toArray(taskArray);
	  for(YAWLTask task : taskArray){
		  if(task.isConfigurable()){
			  List<CPort> outputPorts = task.getOutputCPorts();
			  for(CPort port : outputPorts){
				  if(port.getConfigurationSetting().equals(CPort.BLOCKED)){
					  port.UnavailableFlows();
				  }
			  }
			  for(CPort port : outputPorts){
				  if(port.getConfigurationSetting().equals(CPort.ACTIVATED)){
					  port.AvailableFlows();
				  }
			  }	  
		  }
	  }
	  for(YAWLTask task : taskArray){
		  if(task.isConfigurable()){
			  List<CPort> inputPorts = task.getInputCPorts();
			  for(CPort port : inputPorts){
				  if(port.getConfigurationSetting().equals(CPort.BLOCKED)){
					  port.UnavailableFlows();
				  }
			  }
		  }
		  }
  }
  /**
   * 
   * This method find the elements that need to removed
   * @param model  the NetGraphModel of this net
   */
  private void configurationRemoval(NetGraphModel model){
	  
		YAWLFlowRelation[] flowArray = new YAWLFlowRelation[this.flows.size()]; 
		flowArray = (YAWLFlowRelation[]) this.flows.toArray(flowArray);
		YAWLTask[] taskArray = new YAWLTask[this.tasks.size()]; 
		taskArray = (YAWLTask[]) this.tasks.toArray(taskArray);
		Condition[] conditionArray = new Condition[this.conditions.size()];
		conditionArray = (Condition[]) this.conditions.toArray(conditionArray);
		 
		
		
		//At first add the flows which target port or source port is blocked
		for(YAWLFlowRelation flow : flowArray){
			if(!flow.isAvailable()){
				removeMembers.add(flow);
				this.removalFlows.add(flow);			
			}
		}
		
		int count = 0;
		int oldValue;
		do{
			oldValue = count;
			count = deleteElementsNotOnPathsFromStart(count);
			count = deleteElementsNotOnPathstoEnd(count);
			
			for(YAWLTask task : taskArray){
				count = addRemovalTasks(count, task);
			}
			
			for(Condition condition : conditionArray){
				count = addRemovalCondition(count, condition);
			}
			
			// if the task is removed, all flows of it should be removed
			for(YAWLFlowRelation flow : flowArray){
				count = addRemovalFlows(count, flow);
			}
		
		}while(oldValue != count);
		
		
		
  }
  

private int addRemovalFlows(int count, YAWLFlowRelation flow) {
	if(!this.removalFlows.contains(flow)){
	if(this.removalTasks.contains(flow.getSourceVertex())||this.removalTasks.contains(flow.getTargetVertex())){
		count++;
		removeMembers.add(flow);
		this.removalFlows.add(flow);
	}
	
	if(this.removalConditions.contains(flow.getSourceVertex())||this.removalConditions.contains(flow.getTargetVertex())){
		count++;
		removeMembers.add(flow);
		this.removalFlows.add(flow);
	}
	}
	return count;
}

private int addRemovalCondition(int count, Condition condition) {
	if(!this.removalConditions.contains(condition)){
		int allIncoming = 0;
		int allOutgoing = 0;
		YAWLFlowRelation[] incomingFlows = new YAWLFlowRelation[condition.getIncomingFlows().size()]; 
		incomingFlows = (YAWLFlowRelation[]) condition.getIncomingFlows().toArray(incomingFlows);
		YAWLFlowRelation[] outgoingFlows = new YAWLFlowRelation[condition.getOutgoingFlows().size()]; 
		outgoingFlows = (YAWLFlowRelation[]) condition.getOutgoingFlows().toArray(outgoingFlows);
		for(YAWLFlowRelation flow : incomingFlows){
			if(this.removalFlows.contains(flow)){
				allIncoming++;
			}
		}
		for(YAWLFlowRelation flow : outgoingFlows){
			if(this.removalFlows.contains(flow)){
				allOutgoing++;
			}
		}
		if((allIncoming == condition.getIncomingFlows().size())||(allOutgoing == condition.getOutgoingFlows().size())){
			this.removalConditions.add(condition);
			this.removeMembers.add(condition);
			count++;
			//System.out.println("delete a condition");
		}
	}
	return count;
}

private int addRemovalTasks(int count, YAWLTask task) {
	boolean shouldRemove = false;
	if(!this.removalTasks.contains(task)){
	int allIncoming = 0;
	int allOutgoing = 0;
	YAWLFlowRelation[] incomingFlows = new YAWLFlowRelation[task.getIncomingFlowCount()]; 
	incomingFlows = (YAWLFlowRelation[]) task.getIncomingFlows().toArray(incomingFlows);
	YAWLFlowRelation[] outgoingFlows = new YAWLFlowRelation[task.getOutgoingFlowCount()]; 
	outgoingFlows = (YAWLFlowRelation[]) task.getOutgoingFlows().toArray(outgoingFlows);
	for(YAWLFlowRelation flow : incomingFlows){
		if(removalFlows.contains(flow)){
			if(task.hasJoinDecorator()&&task.getJoinDecorator().getType()== Decorator.AND_TYPE){
				shouldRemove = true;
				break;
			}
			allIncoming ++;
		}	
	}
	if( !shouldRemove ){
		for(YAWLFlowRelation flow : outgoingFlows){
			if(removalFlows.contains(flow)){
				if(task.hasSplitDecorator()&&task.getSplitDecorator().getType()== Decorator.AND_TYPE){
					shouldRemove = true;
					break;
				}
				allOutgoing ++;
			}	
		}
	}
	// if all incoming flows or outgoing flows are in the removal set, the task should be removed 
	if(!shouldRemove){
		if(allIncoming == task.getIncomingFlowCount()|| allOutgoing== task.getOutgoingFlowCount()){
			shouldRemove = true;
			}
		}	
	}	
	if(shouldRemove){
		count++;
		removeMembers.add(task);
		this.removalTasks.add(task);
	}
	return count;
}
  
  
  
  /**
   * This method use breath-first approach to go through the graph and delete all elements groups that do not link to the start or end conditions
 * @param count 
   */
  private int deleteElementsNotOnPathsFromStart(int count){
	  HashSet validVertex = new HashSet();
	  LinkedList<YAWLVertex> vertexQueue  = new LinkedList<YAWLVertex>(); 
	  YAWLFlowRelation[] flowArray = new YAWLFlowRelation[this.flows.size()]; 
	  flowArray = (YAWLFlowRelation[]) this.flows.toArray(flowArray);
	  for(YAWLFlowRelation flow : flowArray){
		  if((flow.getSourceVertex() instanceof InputCondition)
				  && (flow.getTargetVertex()!=null)
				  &&(!this.removalFlows.contains(flow))){
			  YAWLVertex vertex = flow.getTargetVertex();
			  if((!this.removeMembers.contains(vertex))&& !vertexQueue.contains(vertex)){
				  vertexQueue.add(vertex);
			  }
		  }
	  }
	  
	  while(!vertexQueue.isEmpty()){
		  YAWLVertex vertex = vertexQueue.poll();
		  validVertex.add(vertex);
		  YAWLFlowRelation[] flows = new YAWLFlowRelation[vertex.getOutgoingFlows().size()]; 
		  flows = (YAWLFlowRelation[]) vertex.getOutgoingFlows().toArray(flows);
		  for(YAWLFlowRelation flow : flows){
			  if((flow.getTargetVertex()!=null)&&(!this.removeMembers.contains(flow.getTargetVertex()))
					  && (!vertexQueue.contains(flow.getTargetVertex()))&& (!validVertex.contains(flow.getTargetVertex()))){
				  vertexQueue.add(flow.getTargetVertex());
			  } 
		  }
	  }
	  count = removeTasksWithNoPathsFromStartOrToEnd(validVertex,count);
	  
	  count = removeConditionsWithNoPathFromStartOrToEnd(validVertex,count);
	 
	  return count;
  }

  
  private int deleteElementsNotOnPathstoEnd(int count){
	  HashSet validVertex = new HashSet();
	  LinkedList<YAWLVertex> vertexQueue  = new LinkedList<YAWLVertex>(); 
	  YAWLFlowRelation[] flowArray = new YAWLFlowRelation[this.flows.size()]; 
	  flowArray = (YAWLFlowRelation[]) this.flows.toArray(flowArray);
	  for(YAWLFlowRelation flow : flowArray){
		  if((flow.getTargetVertex() instanceof OutputCondition)
				  && (flow.getSourceVertex()!=null)
				  &&(!this.removalFlows.contains(flow))){
			  YAWLVertex vertex = flow.getSourceVertex();
			  if((!this.removeMembers.contains(vertex))&& !vertexQueue.contains(vertex)){
				  vertexQueue.add(vertex);
			  }
		  }
	  }
	  
	  while(!vertexQueue.isEmpty()){
		  YAWLVertex vertex = vertexQueue.poll();
		  validVertex.add(vertex);
		  YAWLFlowRelation[] flows = new YAWLFlowRelation[vertex.getIncomingFlows().size()]; 
		  flows = (YAWLFlowRelation[]) vertex.getIncomingFlows().toArray(flows);
		  for(YAWLFlowRelation flow : flows){
			  if((flow.getSourceVertex()!=null)&&(!this.removeMembers.contains(flow.getSourceVertex()))
					  && (!vertexQueue.contains(flow.getSourceVertex()))&& (!validVertex.contains(flow.getSourceVertex()))){
				  vertexQueue.add(flow.getSourceVertex());
			  }		  
		  }
	  }
	  count = removeTasksWithNoPathsFromStartOrToEnd(validVertex, count);
	  
	  count = removeConditionsWithNoPathFromStartOrToEnd(validVertex, count);
	  
	  return count;
	  
  }
  
  
//private void removeAllElements() {
//	this.removalTasks = this.tasks;
//	  this.removalFlows = this.flows;
//	  this.removeMembers.clear();
//	  Object[] cells = NetGraphModel.getRoots(model);
//	 	 for (int i=0; i < cells.length; i++) {
//
//	 		 if (cells[i] instanceof VertexContainer) {
//	 			 this.vertexContainers.add(cells[i]);
//		         cells[i] = ((VertexContainer) cells[i]).getVertex();
//		      }
//	 		 if (cells[i] instanceof YAWLFlowRelation) {
//	 	        this.removeMembers.add(cells[i]);
//	 	      }
//	 		 if (cells[i] instanceof YAWLTask) {
//	 			this.removeMembers.add(cells[i]);
//	 	      }
//	 		 if(cells[i] instanceof YAWLCondition){
//	 			 this.removeMembers.add(cells[i]);
//	 		 }
//	 	 }
//}

private int removeConditionsWithNoPathFromStartOrToEnd(HashSet validVertex, int count) {
	Condition[] conditionArray = new Condition[this.conditions.size()];
	  conditionArray = (Condition[]) this.conditions.toArray(conditionArray);
	  for(Condition condition : conditionArray){
		  if((!validVertex.contains(condition))&& (!this.removalConditions.contains(condition))){
			  count++;
			  this.removalConditions.add(condition);
			  this.removeMembers.add(condition);
			  YAWLFlowRelation[] flows = new YAWLFlowRelation[condition.getOutgoingFlows().size()]; 
			  flows = (YAWLFlowRelation[]) condition.getOutgoingFlows().toArray(flows);
			  for(YAWLFlowRelation flow : flows){
				  if(!this.removalFlows.contains(flow)){
					  this.removalFlows.add(flow);
					  this.removeMembers.add(flow);
				  }
			  }
			  
			  YAWLFlowRelation[] inflows = new YAWLFlowRelation[condition.getIncomingFlows().size()]; 
			  inflows = (YAWLFlowRelation[]) condition.getIncomingFlows().toArray(inflows);
			  for(YAWLFlowRelation flow : inflows){
				  if(!this.removalFlows.contains(flow)){
					  this.removalFlows.add(flow);
					  this.removeMembers.add(flow);
				  }	
			  }
		  }
	  }
	  return count;
}

private int removeTasksWithNoPathsFromStartOrToEnd(HashSet validVertex, int count) {
	YAWLTask[] taskArray = new YAWLTask[this.tasks.size()]; 
	  taskArray = (YAWLTask[]) this.tasks.toArray(taskArray);
	  for(YAWLTask task : taskArray){
		  if((!validVertex.contains(task))&& (!this.removalTasks.contains(task))){
			  count++;
			  this.removalTasks.add(task);
			  this.removeMembers.add(task);
			  YAWLFlowRelation[] flows = new YAWLFlowRelation[task.getOutgoingFlowCount()]; 
			  flows = (YAWLFlowRelation[]) task.getOutgoingFlows().toArray(flows);
			  for(YAWLFlowRelation flow : flows){
				  if(!this.removalFlows.contains(flow)){
					  this.removalFlows.add(flow);
					  this.removeMembers.add(flow);
				  }
			  }
			  
			  YAWLFlowRelation[] inflows = new YAWLFlowRelation[task.getIncomingFlowCount()]; 
			  inflows = (YAWLFlowRelation[]) task.getIncomingFlows().toArray(inflows);
			  for(YAWLFlowRelation flow : inflows){
				  if(!this.removalFlows.contains(flow)){
					  this.removalFlows.add(flow);
					  this.removeMembers.add(flow);
				  }
			  }
		  }
	  }
	  return count;
}
  private void changeDecoratorGeneration(){
	  YAWLTask[] taskArray = new YAWLTask[this.tasks.size()]; 
	  taskArray = (YAWLTask[]) this.tasks.toArray(taskArray);
	  for(YAWLTask task : taskArray){
		  if(!this.removalTasks.contains(task)){
			  
			  if((task.hasJoinDecorator())&&(task.getIncomingFlowCount()>1)){
				  //When only one incoming flow, the join decorator should remove
				  int number = task.getIncomingFlowCount();
				  YAWLFlowRelation[] incomingFlows = new YAWLFlowRelation[task.getIncomingFlowCount()]; 
				  incomingFlows = (YAWLFlowRelation[]) task.getIncomingFlows().toArray(incomingFlows);
				  for(YAWLFlowRelation flow : incomingFlows){
						if(this.removalFlows.contains(flow)){
							number--;						}	
					}
				  if(number == 1){
					  this.changeTaskDecoration.put(task, NO_Join);
				  }
			  }
			  
			  if((task.hasSplitDecorator())&&(task.getOutgoingFlowCount()>1)){
				  //when only one outgoing flow, the split decorator should remove
				  int number = task.getOutgoingFlowCount();
				  YAWLFlowRelation[] outgoingFlows = new YAWLFlowRelation[task.getOutgoingFlowCount()]; 
				  outgoingFlows = (YAWLFlowRelation[]) task.getOutgoingFlows().toArray(outgoingFlows);
				  for(YAWLFlowRelation flow : outgoingFlows){
						if(this.removalFlows.contains(flow)){
							number--;
									}	
					}
				  if(number == 1){
					  this.changeTaskDecoration.put(task, NO_Split);
				  }
			  }
			  
			  if(task.isConfigurable() && task.hasSplitDecorator() 
					  && task.getSplitDecorator().getType() == Decorator.OR_TYPE){
				  
				  boolean oneFlow = false;
				  boolean moreFlows = false;
				 // boolean allFlows = false;
				  int count = 0;
				  List<CPort> ports = task.getOutputCPorts();
				  for(CPort port : ports){
					 if(port.getFlows().size() == 1 && port.getConfigurationSetting().equals("activated")) {
						 oneFlow = true;						 
					 }
					 if(port.getFlows().size() >1 && port.getConfigurationSetting().equals("activated")){
						 moreFlows =true; 
						 count++;
//						 if((port.getFlows().size() == task.getOutgoingFlowCount())){
//							 allFlows = true;
//						 }
					 }
				  }
				  if(oneFlow&&(!moreFlows)&&(!this.changeTaskDecoration.containsKey(task))){
					  this.changeTaskDecoration.put(task, XOR_Split);
				  }else if((!oneFlow) && (count==1)&&(!this.changeTaskDecoration.containsKey(task))){
					  this.changeTaskDecoration.put(task, AND_Split);
				  }
			  }
		  }
	  }
  }
  
  public HashSet<VertexContainer> getRemoveVertexContainers(){
	  VertexContainer[] containers = new VertexContainer[this.vertexContainers.size()]; 
	  containers = (VertexContainer[]) this.vertexContainers.toArray(containers);
	  for(VertexContainer container : containers){
		  if(this.removalTasks.contains(container.getVertex())){
			  this.removeVertexContainer.add(container);
		  }
		  
		  if(this.removalConditions.contains(container.getVertex())){
			  this.removeVertexContainer.add(container);
		  }
	  }
	  return this.removeVertexContainer;
  }
  
  public HashSet<YAWLCell> getRemoveSetMembers() {
    return removeMembers;
  }
  
  public void setRemoveSetMembers(HashSet<YAWLCell> setMembers) {
  	this.removeMembers = setMembers;
  }

  public HashSet getRemoveFlows(){
	  return this.removalFlows;
  }
  
  public HashSet getRemoveTasks(){
	  return this.removalTasks;
  }
  
  public boolean addRemoveMember(YAWLCell newSetMember) {
    if (newSetMember instanceof YAWLTask || newSetMember instanceof Condition || 
        newSetMember instanceof YAWLFlowRelation) {
      return removeMembers.add(newSetMember);
    }
    return false;
  }
  
  public boolean removeRemoveMember(YAWLCell oldSetMember) {
    return removeMembers.remove(oldSetMember);
  }
  
  public boolean containsRemoveMembers(YAWLCell cell) {
    return removeMembers.contains(cell);
  }
  
  public int sizeOfRemoveMembers() {
    return removeMembers.size();
  }

  public boolean hasRemoveMembers() {
      return removeMembers.size() > 0;
  }
  
  
  public Map<YAWLTask, Integer> getChangedDecorators(){
	return this.changeTaskDecoration;
	  
  }


}
