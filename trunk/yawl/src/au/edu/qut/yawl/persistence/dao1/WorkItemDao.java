/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.persistence.dao1;

import java.util.Set;

import org.apache.log4j.Logger;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.engine.domain.YWorkItem;


public interface WorkItemDao {
	// OLD YWorkItemRepository Interface definitions:  Hopefully we can find ways to remove these?
    public void cancelNet(YIdentifier caseIDForNet);
    public Set getChildrenOf(String workItemID);
    public Set getEnabledWorkItems();
    public Set getFiredWorkItems();
    public Set getExecutingWorkItems();
    public Set getCompletedWorkItems();
    public void addNewWorkItem(YWorkItem workItem);
    public YWorkItem getWorkItem(String workItemID);
	public YWorkItem getWorkItem(String caseIDStr, String taskID);
    public Set getWorkItems();
    public boolean removeWorkItemFamily(YWorkItem workItem);
    public void removeWorkItemsForCase(YIdentifier caseID);
    public void dump(Logger logger);
    public void setNetRunnerToCaseIDBinding(YNetRunner netRunner, YIdentifier caseID);
    public Set getParentWorkItems();
}
