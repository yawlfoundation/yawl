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
 * 
 * Created by Jingxin XU on 13/01/2010
 * 
 */

package org.yawlfoundation.yawl.editor.ui.configuration;

import org.yawlfoundation.yawl.editor.ui.elements.model.*;

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


    public void addFlow(YAWLFlowRelation flow) {
        flows.add(flow);
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
        flowArray = flows.toArray(flowArray);
        if(this.type == INPUTPORT){
            for(YAWLFlowRelation flow : flows){
                YAWLVertex vertex = flow.getSourceVertex();
                if (vertex != null) {
                    if(vertex instanceof InputCondition){
                        if(flow == flowArray[flowArray.length-1]){
                            sum = sum + "Start";
                        }else {
                            sum = sum + "Start"+", ";
                        }
                    }else if(flow != flowArray[flowArray.length-1]){
                        if(vertex.getLabel() != null){
                            sum = sum + flow.getSourceVertex().getLabel()+", ";
                        }else{
                            sum = sum + flow.getSourceVertex().getID()+", ";
                        }
                    }else{
                        if(vertex.getLabel() != null){
                            sum = sum + flow.getSourceVertex().getLabel();
                        }else{
                            sum = sum + flow.getSourceVertex().getID();
                        }
                    }
                }
            }
        }
        return sum;
    }

    public String getTargetTasksLabels(){
        String sum = "";

        YAWLFlowRelation[] flowArray = new YAWLFlowRelation[this.flows.size()];
        flowArray = flows.toArray(flowArray);
        if(this.type == OUTPUTPORT){
            for(YAWLFlowRelation flow : flowArray){
                YAWLVertex vertex = flow.getTargetVertex();
                if(vertex != null) {
                    if (vertex instanceof OutputCondition) {

                        if(flow == flowArray[flowArray.length-1]){
                            sum = sum + "End";
                        }else {
                            sum = sum + "End"+", ";
                        }
                    }else if(flow != flowArray[flowArray.length-1]){
                        if(vertex.getLabel() != null){
                            sum = sum + flow.getTargetVertex().getLabel()+", ";
                        }else{
                            sum = sum + flow.getTargetVertex().getID()+", ";
                        }
                    }else{
                        if(vertex.getLabel() != null){
                            sum = sum + flow.getTargetVertex().getLabel();
                        }else{
                            sum = sum + flow.getTargetVertex().getID();
                        }
                    }
                }
            }
        }

        return sum;
    }

}
