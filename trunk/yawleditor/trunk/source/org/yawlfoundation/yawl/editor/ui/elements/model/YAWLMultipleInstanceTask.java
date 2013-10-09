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

package org.yawlfoundation.yawl.editor.ui.elements.model;

import org.yawlfoundation.yawl.elements.data.YVariable;

public interface YAWLMultipleInstanceTask {

  public static final long INFINITY = Integer.MAX_VALUE;

  public static final int STATIC_INSTANCE_CREATION  = -2;
  public static final int DYNAMIC_INSTANCE_CREATION = -3;
  
  abstract public MultipleInstanceTaskConfigSet getConfigurationInfor();

  abstract public long getMinimumInstances();
  abstract public void setMinimumInstances(long instanceBound);
  
  abstract public long getMaximumInstances();
  abstract public void setMaximumInstances(long instanceBound);
  
  abstract public long getContinuationThreshold();
  abstract public void setContinuationThreshold(long continuationThreshold);
  
  abstract public int  getInstanceCreationType();
  abstract public void setInstanceCreationType(int instanceCreationType);
  
  abstract public YVariable getMultipleInstanceVariable();
  abstract public void setMultipleInstanceVariable(YVariable parameter);

  abstract public String getAccessorQuery();
  abstract public void setAccessorQuery(String query);

  abstract public String getSplitterQuery();
  abstract public void setSplitterQuery(String query);

  abstract public String getInstanceQuery();
  abstract public void setInstanceQuery(String query);
  
  abstract public String getAggregateQuery();
  abstract public void setAggregateQuery(String query);
  
  abstract public YVariable getResultNetVariable();
  abstract public void setResultNetVariable(YVariable variable);
}
