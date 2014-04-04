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

/**
 * Created by Jingxin XU on 25/01/2010
 */

package org.yawlfoundation.yawl.configuration;

import org.yawlfoundation.yawl.editor.ui.elements.model.YAWLMultipleInstanceTask;

public class MultipleInstanceTaskConfigSet {
	
	private final YAWLMultipleInstanceTask task;
	
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
