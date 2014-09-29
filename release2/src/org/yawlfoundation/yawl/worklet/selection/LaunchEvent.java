package org.yawlfoundation.yawl.worklet.selection;

import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;

/**
 * @author Michael Adams
 * @date 26/09/2014
 */
public class LaunchEvent {

    private long id;                // hibernate PKey

    private YSpecificationID specId;
    private String taskId;
    private String itemId;
    private RuleType ruleType;
    private String parentCaseId;
    private String launchedCaseId;
    private String data;

    public LaunchEvent() { }

    public LaunchEvent(YSpecificationID specId, String taskId, String itemId,
                       RuleType ruleType, String parentCaseID,
                       String launchedCaseId, String data) {
        this.specId = specId;
        this.taskId = taskId;
        this.itemId = itemId;
        this.ruleType = ruleType;
        this.parentCaseId = parentCaseID;
        this.launchedCaseId = launchedCaseId;
        this.data = data;
    }

    public LaunchEvent(WorkItemRecord wir, RuleType ruleType,
                       String launchedCaseId, String data) {
        this(new YSpecificationID(wir), wir.getTaskID(), wir.getID(), ruleType,
                wir.getRootCaseID(), launchedCaseId, data);
    }


    public long getId() { return id; }

    public void setId(long id) { this.id = id; }


    public YSpecificationID getSpecId() { return specId; }

    public void setSpecId(YSpecificationID specId) { this.specId = specId; }


    public String getTaskId() { return taskId; }

    public void setTaskId(String taskId) { this.taskId = taskId; }


    public String getItemId() { return itemId; }

    public void setItemId(String itemId) { this.itemId = itemId; }


    public RuleType getRuleType() { return ruleType; }

    public void setRuleType(RuleType ruleType) { this.ruleType = ruleType; }


    public String getParentCaseId() { return parentCaseId; }

    public void setParentCaseId(String caseId) { this.parentCaseId = caseId; }


    public String getLaunchedCaseId() { return launchedCaseId; }

    public void setLaunchedCaseId(String caseId) { this.launchedCaseId = caseId; }


    public String getData() { return data; }

    public void setData(String data) { this.data = data; }

}
