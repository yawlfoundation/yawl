/**
 * Created by Jingxin XU on 25/01/2010
 */

package org.yawlfoundation.yawl.editor.ui.elements.model;

public class MultipleInstanceTaskConfigSet {
	
	private YAWLMultipleInstanceTask task;
	
	private long reduceMax;
	private long increaseMin;
	private long increaseThreshold;
	private boolean forbidDynamic;
	
	public MultipleInstanceTaskConfigSet(YAWLMultipleInstanceTask task){
		this.task = task;
		reduceMax = task.getMaximumInstances();
		this.increaseMin = task.getMinimumInstances();
		this.increaseThreshold = task.getContinuationThreshold();
		if(task.getInstanceCreationType() == YAWLMultipleInstanceTask.DYNAMIC_INSTANCE_CREATION){
			this.forbidDynamic = false;
		} else if(task.getInstanceCreationType() == YAWLMultipleInstanceTask.STATIC_INSTANCE_CREATION){
			this.forbidDynamic = true;
		}
	}

	public long getReduceMax() {
		return reduceMax;
	}

	public void setReduceMax(long reduceMax) {	
		this.reduceMax = reduceMax;
	}

	public long getIncreaseMin() {
		return increaseMin;
	}

	public void setIncreaseMin(long increaseMin) {
		this.increaseMin = increaseMin;
	}

	public long getIncreaseThreshold() {
		return increaseThreshold;
	}

	public void setIncreaseThreshold(long increaseThreshold) {
		this.increaseThreshold = increaseThreshold;
	}

	public boolean isForbidDynamic() {
		return forbidDynamic;
	}

	public void setForbidDynamic(boolean forbidDynamic) {
		this.forbidDynamic = forbidDynamic;
	}
}
