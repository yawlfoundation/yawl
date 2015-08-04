package org.yawlfoundation.yawl.elements.predicate;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.cost.interfce.CostGatewayClient;
import org.yawlfoundation.yawl.elements.YDecomposition;
import org.yawlfoundation.yawl.elements.state.YIdentifier;

import java.io.IOException;

/**
 * @author Michael Adams
 * @date 5/12/12
 */
public class CostPredicateEvaluator implements PredicateEvaluator {

    private CostGatewayClient _client;
    private String _handle;


    public CostPredicateEvaluator() {
        CostGatewayClient _client = new CostGatewayClient();
    }


    public boolean accept(String predicate) {
        return predicate != null && predicate.startsWith("cost(");
    }


    public boolean evaluate(YDecomposition decomposition, String predicate,
                            YIdentifier token) {
        try {
            connect();
            return _client.evaluate(
                    decomposition.getSpecification().getSpecificationID(),
                    token.getId(), predicate, _handle);
        } catch (IOException ioe) {
            Logger.getLogger(this.getClass()).error(ioe.getMessage());
            return false;
        }
    }


    private void connect() throws IOException {
        if (!checkHandle()) {
            _handle = _client.connect("admin", "YAWL");
        }
    }


    private boolean checkHandle() throws IOException {
        return _handle != null && successful(_client.checkConnection(_handle));
    }


    private boolean successful(String msg) {
        return !(msg == null || msg.length() == 0 || msg.contains("<failure>"));
    }

}
