package org.yawlfoundation.yawl.worklet.support;

import org.jdom2.Element;
import org.yawlfoundation.yawl.cost.interfce.CostGatewayClient;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Marshaller;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;
import org.yawlfoundation.yawl.worklet.WorkletService;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 14/12/2013
 */
public class CostPredicateEvaluator {

    public String evaluate(String predicate, Element data) throws RdrConditionException {
        WorkItemRecord wir = transformData(data);
        YSpecificationID specID = new YSpecificationID(wir);
        String caseID = wir.getRootCaseID();
        CostGatewayClient client = new CostGatewayClient();
        String handle = getHandle(client);
        try {
            boolean result = client.evaluate(specID, caseID, predicate, handle);
            client.disconnect(handle);
            return String.valueOf(result);
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
}
