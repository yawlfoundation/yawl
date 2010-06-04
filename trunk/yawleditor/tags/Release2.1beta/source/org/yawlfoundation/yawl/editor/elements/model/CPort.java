/**
 * 
 * Created by Jingxin XU on 13/01/2010
 * 
 */

package org.yawlfoundation.yawl.editor.elements.model;

import java.util.HashSet;
import java.util.Set;

public class CPort implements Cloneable {
	
	public final static String ACTIVATED = "activated";
	public final static String BLOCKED = "blocked";
	public final static String HIDDEN = "hidden";
	public final static int INPUTPORT = 0;
	public final static int OUTPUTPORT = 1;
	
	private int type;
	private String defaultValue;
	
	private Set<YAWLFlowRelation> flows;
	
	private String configurationSetting;
	
	private int ID;
	
	private YAWLTask task;
	
	public CPort(YAWLTask task, int type) {
		this.flows = new HashSet<YAWLFlowRelation>();
		this.configurationSetting = this.ACTIVATED;
		ID = -1;
		this.defaultValue = null;
		this.task = task;
		this.type = type;
	}

    public Object clone() {
        try {
            CPort cloned = (CPort) super.clone();
            cloned.task = this.task;
            cloned.type = this.type;
            cloned.setConfigurationSetting(this.configurationSetting);
            cloned.setID(this.ID);
            cloned.setDefaultValue(this.defaultValue);
            cloned.setFlows(this.flows);
            return cloned;
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

	public YAWLTask getTask(){
		return this.task;
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getConfigurationSetting() {
		return configurationSetting;
	}
	public void setConfigurationSetting(String configurationSetting) {
		this.configurationSetting = configurationSetting;
		//SetFlowsUnavailable();
	}
	
//	private void SetFlowsUnavailable(){
//		if(this.configurationSetting == this.BLOCKED){
//			
//		}
//	}

	public Set<YAWLFlowRelation> getFlows() {
		return flows;
	}


	public void setFlows(Set<YAWLFlowRelation> flows) {
		this.flows = flows;
	}


	public String getDefaultValue() {
		return defaultValue;
	}


	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}	
	
	public void AvailableFlows(){
		YAWLFlowRelation[] flowArray = new YAWLFlowRelation[this.flows.size()];
		flowArray = (YAWLFlowRelation[]) this.flows.toArray(flowArray);
		for(YAWLFlowRelation flow : flowArray){
			flow.setAvailable(true);
		}
	}
	
	public void UnavailableFlows(){
		for (YAWLFlowRelation flow : flows) {
		    flow.setAvailable(false);
		}
	}
	
	public String getSourceTasksLabels(){
		String sum = "";
		YAWLFlowRelation[] flowArray = new YAWLFlowRelation[this.flows.size()];
		flowArray = (YAWLFlowRelation[]) this.flows.toArray(flowArray);
		if(this.type == INPUTPORT){
			for(YAWLFlowRelation flow : flows){
				if(flow.getSourceVertex() instanceof InputCondition){
					if(flow == flowArray[flowArray.length-1]){
						sum = sum + "Start";
					}else {
						sum = sum + "Start"+", ";
					}
				}else if(flow != flowArray[flowArray.length-1]){
					if(flow.getSourceVertex().getLabel() != null){
						sum = sum + flow.getSourceVertex().getLabel()+", ";
					}else{
						sum = sum + flow.getSourceVertex().getEngineId()+", ";
					}
				}else{
					if(flow.getSourceVertex().getLabel() != null){
						sum = sum + flow.getSourceVertex().getLabel();
					}else{
						sum = sum + flow.getSourceVertex().getEngineId();
					}
				}
			}
		}
		return sum;
	}
	
	public String getTagetTasksLabels(){
		String sum = "";
		
		YAWLFlowRelation[] flowArray = new YAWLFlowRelation[this.flows.size()];
		flowArray = (YAWLFlowRelation[]) this.flows.toArray(flowArray);
		if(this.type == this.OUTPUTPORT){
			for(YAWLFlowRelation flow : flowArray){
				if(flow.getTargetVertex() instanceof OutputCondition){
					if(flow == flowArray[flowArray.length-1]){
						sum = sum + "End";
					}else {
						sum = sum + "End"+", ";
					}
				}else if(flow != flowArray[flowArray.length-1]){
					if( flow.getTargetVertex().getLabel() != null){
						sum = sum + flow.getTargetVertex().getLabel()+", ";
					}else{
						sum = sum + flow.getTargetVertex().getEngineId()+", ";
					}
				}else{
					if(flow.getTargetVertex().getLabel() != null){
						sum = sum + flow.getTargetVertex().getLabel();
					}else{
						sum = sum + flow.getTargetVertex().getEngineId();
					}
				}
			}
		}
		
		return sum;
	}
	
}
