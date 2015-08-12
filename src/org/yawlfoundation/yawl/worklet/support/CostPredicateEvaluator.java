package org.yawlfoundation.yawl.worklet.support;

import org.jdom2.Element;
import org.yawlfoundation.yawl.cost.interfce.CostGatewayClient;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.logging.table.YLogDataType;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.worklet.WorkletService;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 14/12/2013
 */
public class CostPredicateEvaluator {

    private static final char CHEAPEST = '<';
    private static final char DEAREST = '>';


    public String evaluate(String predicate, Element data) throws RdrConditionException {
        WorkItemRecord wir = transformData(data);
        if (predicate.startsWith("cheapestVariant(")) {
            return getCheapestVariant(predicate, wir);
        }
        else if (predicate.startsWith("dearestVariant(")) {
            return getDearestVariant(predicate, wir);
        }
        else if (predicate.startsWith("cost(")) {
            YSpecificationID specID = new YSpecificationID(wir);
            String caseID = wir.getRootCaseID();
            return String.valueOf(calculate(predicate, specID, caseID));
        }
        throw new RdrConditionException("Invalid cost expression");
    }


    private String getCheapestVariant(String predicate, WorkItemRecord wir)
            throws RdrConditionException {
        return getVariantMeetingCriterion(predicate, wir, CHEAPEST);
    }


    private String getDearestVariant(String predicate, WorkItemRecord wir)
            throws RdrConditionException {
        return getVariantMeetingCriterion(predicate, wir, DEAREST);
    }


    private String getVariantMeetingCriterion(String predicate, WorkItemRecord wir,
                                              char criterion)
            throws RdrConditionException {

        Set<YSpecificationID> variantIDs = getVariants(wir);
        if (variantIDs.size() == 1) {
            return variantIDs.iterator().next().getUri();    // short circuit
        }

        String costPredicate = extractCostPredicate(predicate);
        String variantURI = "";
        double criterionCost = (criterion == CHEAPEST) ? Double.MAX_VALUE : -1;
        for (YSpecificationID specID : variantIDs) {
            double cost = calculate(costPredicate, specID, "0");
            if ((criterion == CHEAPEST && cost < criterionCost) ||
                    (criterion == DEAREST && cost > criterionCost)) {
                criterionCost = cost;
                variantURI = specID.getUri();
            }
        }
        return variantURI;
    }


    private double calculate(String predicate, YSpecificationID specID, String caseID)
            throws RdrConditionException {
        CostGatewayClient client = new CostGatewayClient();
        String handle = getHandle(client);
        try {
            double result = client.calculate(specID, caseID, predicate, handle);
            client.disconnect(handle);
            if (result > -1) return result;
            throw new RdrConditionException("Invalid cost expression");
        }
        catch (IOException ioe) {
            throw new RdrConditionException(ioe.getMessage());
        }
    }


    private WorkItemRecord transformData(Element data) throws RdrConditionException {
        try {
            Element eInfo = data.getChild("process_info")
                    .getChild("workItemRecord").detach();
            return Marshaller.unmarshalWorkItem(eInfo);
        }
        catch (Exception e) {
            throw new RdrConditionException("Malformed RDR search data element");
        }
    }


    private String getHandle(CostGatewayClient client) throws RdrConditionException {
        try {
            String handle = WorkletService.getInstance().getExternalServiceHandle(client);
            if (handle == null) {
                throw new RdrConditionException(
                        "Null session handle returned from serivce call");
            }
            if (handle.contains("<fail")) {
                throw new RdrConditionException(StringUtil.unwrap(handle));
            }
            return handle;
        }
        catch (IOException ioe) {
            String msg = ioe.getMessage();
            if (msg.startsWith("http")) {
                msg = "Cost Service is missing or unavailable";
            }
            throw new RdrConditionException(ioe.getMessage());
        }
    }


    private String extractCostPredicate(String predicate) throws RdrConditionException {
        predicate = predicate.trim();
        int start = predicate.indexOf('(') + 1;
        int end = predicate.lastIndexOf(')');
        if (start < 0 || end < 0 || end < start) {
            throw new RdrConditionException("Invalid function format");
        }
        return predicate.substring(start, end);
    }


    private Set<YSpecificationID> getVariants(WorkItemRecord wir) {
        Set<YSpecificationID> variants = new HashSet<YSpecificationID>();
        String parentQuery = "from WorkletEvent w where w._specId.uri=:uri and " +
                "w._taskId=:taskid and w._event='CheckOutWorkItem'";
        String workletQuery = "from WorkletEvent w where w._parentCaseId=:caseid and " +
                "w._event='WorkletLaunched'";

        List parentList = Persister.getInstance().createQuery(parentQuery)
                        .setString("uri", wir.getSpecURI())
                        .setString("taskid", wir.getTaskID()).list();
        for (Object o : parentList) {
            WorkletEvent parentEvent = (WorkletEvent) o;
            List workletList = Persister.getInstance().createQuery(workletQuery)
                            .setString("caseid", parentEvent.get_caseId()).list();
            for (Object ow : workletList) {
                variants.add(((WorkletEvent) ow).get_specId());
            }
        }
        return variants;
    }

}
