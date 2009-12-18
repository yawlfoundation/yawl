package org.yawlfoundation.yawl.monitor.sort;

import org.yawlfoundation.yawl.engine.instance.CaseInstance;
import org.yawlfoundation.yawl.engine.instance.WorkItemInstance;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Author: Michael Adams
 * Creation Date: 9/12/2009
 */
public class TableSorter {

    public static enum CaseColumn { Case, SpecName, Version, StartTime, Undefined }

    public static enum ItemColumn { ItemID, TaskID, Status, Service, EnabledTime,
                                    StartTime, CompletionTime, TimerStatus,
                                    TimerExpiry, Undefined }

    private CaseOrder caseOrder;
    private ItemOrder itemOrder;

    public TableSorter() {
        caseOrder = new CaseOrder(CaseColumn.Undefined);
        itemOrder = new ItemOrder(ItemColumn.Undefined);
    }

    public List<CaseInstance> sort(List<CaseInstance> caseList, CaseColumn column) {
        caseOrder.setOrder(column);
        return applyCaseOrder(caseList);
    }

    public List<CaseInstance> applyCaseOrder(List<CaseInstance> caseList) {
        Collections.sort(caseList, getCurrentCaseComparator());
        return caseList;
    }

    public CaseOrder getCaseOrder() { return caseOrder; }

    private Comparator<CaseInstance> getCurrentCaseComparator() {
        return CaseInstanceComparator.getComparator(caseOrder);
    }


    public List<WorkItemInstance> sort(List<WorkItemInstance> itemList, ItemColumn column) {
        itemOrder.setOrder(column);
        return applyItemOrder(itemList);
    }

    public List<WorkItemInstance> applyItemOrder(List<WorkItemInstance> itemList) {
        Collections.sort(itemList, getCurrentItemComparator());
        return itemList;
    }

    public ItemOrder getItemOrder() { return itemOrder; }

    private Comparator<WorkItemInstance> getCurrentItemComparator() {
        return ItemInstanceComparator.getComparator(itemOrder);
    }




}
