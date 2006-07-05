/*
 * Created on 23/10/2003
 * YAWLEditor v1.0 
 *
 * @author Lindsay Bradford
 * 
 * Copyright (C) 2003 Queensland University of Technology
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

import java.awt.Point;
import au.edu.qut.yawl.editor.data.DataVariable;
import au.edu.qut.yawl.editor.data.WebServiceDecomposition;
import au.edu.qut.yawl.editor.foundations.XMLUtilities;

public class MultipleAtomicTask extends YAWLTask implements YAWLMultipleInstanceTask, YAWLAtomicTask {

  private long minimumInstances = 1;
  private long maximumInstances = 1;
  private long continuationThreshold = 1;
  
  private int  instanceCreationType = STATIC_INSTANCE_CREATION;
  
  private DataVariable multipleInstanceVariable;
  private DataVariable resultNetVariable;
  
  private String splitterQuery;
  private String aggregateQuery;

  public MultipleAtomicTask() {
    super();
    initialise();
  }
  
  public MultipleAtomicTask(Point startPoint) {
    super(startPoint);
    initialise();
  }
  
  private void initialise() {
    multipleInstanceVariable = null;
    splitterQuery = "true()";
    aggregateQuery = "true()";
  }
  
  public long getMinimumInstances() {
    return minimumInstances;
  }

  public void setMinimumInstances(long instanceBound) {
    minimumInstances = instanceBound;
  }
  
  public long getMaximumInstances() {
    return maximumInstances;
  }

  public void setMaximumInstances(long instanceBound) {
    maximumInstances = instanceBound;
  }
  
  public long getContinuationThreshold() {
    return continuationThreshold;
  }

  public void setContinuationThreshold(long continuationThreshold) {
    this.continuationThreshold = continuationThreshold;
  }
  
  public int  getInstanceCreationType() {
    return instanceCreationType;
  }

  public void setInstanceCreationType(int instanceCreationType) {
    assert instanceCreationType == STATIC_INSTANCE_CREATION || 
           instanceCreationType == DYNAMIC_INSTANCE_CREATION : "invalid type passed";
           
    this.instanceCreationType = instanceCreationType;
  }
  
  public DataVariable getMultipleInstanceVariable() {
    return this.multipleInstanceVariable;
  }
  
  public void setMultipleInstanceVariable(DataVariable variable) {
    this.multipleInstanceVariable = variable;
  }
  
  public String getAccessorQuery() {
    return getParameterLists().getInputParameters().getQueryFor(
      multipleInstanceVariable
    );
  }

  public String getEngineReadyAccessorQuery() {
    return XMLUtilities.quoteSpecialCharacters(
        getAccessorQuery()
    );
  }

  public void setAccessorQuery(String query) {
    if (multipleInstanceVariable != null) {
      getParameterLists().getInputParameters().setQueryFor(
          multipleInstanceVariable, query
      );
    }
  }

  public String getSplitterQuery() {
    return this.splitterQuery;
  }

  public String getEngineReadySplitterQuery() {
    return XMLUtilities.quoteSpecialCharacters(
        getSplitterQuery()
    );
  }
  
  public void setSplitterQuery(String query) {
    this.splitterQuery = query;
  }

  public String getInstanceQuery() {
    return getParameterLists().getOutputParameters().getQueryFor(
      resultNetVariable
    );
  }

  public String getEngineReadyInstanceQuery() {
    return XMLUtilities.quoteSpecialCharacters(
        getInstanceQuery()
    );
  }
  
  public void setInstanceQuery(String query) {
    if (resultNetVariable != null) {
      getParameterLists().getOutputParameters().setQueryFor(
          resultNetVariable, query
      );
    }
  }
  
  public String getAggregateQuery() {
    return this.aggregateQuery;
  }

  public String getEngineReadyAggregateQuery() {
    return XMLUtilities.quoteSpecialCharacters(
        getAggregateQuery()
    );
  }
  
  public void setAggregateQuery(String query) {
    this.aggregateQuery = query;
  }
  
  public DataVariable getResultNetVariable() {
    return this.resultNetVariable;
  }
  
  public void setResultNetVariable(DataVariable variable) {
    this.resultNetVariable = variable;
  }
 
  public void setWSDecomposition(WebServiceDecomposition decomposition) {
    super.setDecomposition(decomposition);
  }
  
  public WebServiceDecomposition getWSDecomposition() {
    return (WebServiceDecomposition) super.getDecomposition();
  }
  
  public String getType() {
    return "Multiple Atomic Task";
  }
}
