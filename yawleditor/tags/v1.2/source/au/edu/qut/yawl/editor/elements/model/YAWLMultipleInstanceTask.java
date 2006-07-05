/*
 * Created on 06/02/2004, 10:12:00
 * YAWLEditor v1.0 
 * 
 * Copyright (C) 2004 Lindsay Bradford
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package au.edu.qut.yawl.editor.elements.model;

import au.edu.qut.yawl.editor.data.DataVariable;

public interface YAWLMultipleInstanceTask {
  public static final long INFINITY = -1;

  public static final int STATIC_INSTANCE_CREATION  = -2;
  public static final int DYNAMIC_INSTANCE_CREATION = -3;
  
  abstract public long getMinimumInstances();
  abstract public void setMinimumInstances(long instanceBound);
  
  abstract public long getMaximumInstances();
  abstract public void setMaximumInstances(long instanceBound);
  
  abstract public long getContinuationThreshold();
  abstract public void setContinuationThreshold(long continuationThreshold);
  
  abstract public int  getInstanceCreationType();
  abstract public void setInstanceCreationType(int instanceCreationType);
  
  abstract public DataVariable getMultipleInstanceVariable();
  abstract public void setMultipleInstanceVariable(DataVariable parameter);

  abstract public String getAccessorQuery();
  abstract public String getEngineReadyAccessorQuery();
  abstract public void setAccessorQuery(String query);

  abstract public String getSplitterQuery();
  abstract public String getEngineReadySplitterQuery();
  abstract public void setSplitterQuery(String query);

  abstract public String getInstanceQuery();
  abstract public String getEngineReadyInstanceQuery();
  abstract public void setInstanceQuery(String query);
  
  abstract public String getAggregateQuery();
  abstract public String getEngineReadyAggregateQuery();
  abstract public void setAggregateQuery(String query);
  
  abstract public DataVariable getResultNetVariable();
  abstract public void setResultNetVariable(DataVariable variable);
}
