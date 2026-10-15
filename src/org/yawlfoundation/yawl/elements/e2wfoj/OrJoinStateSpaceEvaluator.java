package org.yawlfoundation.yawl.elements.e2wfoj;

import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.YNetElement;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.pdoj.OrJoinEvaluator;
import org.yawlfoundation.yawl.elements.state.YIdentifier;
import org.yawlfoundation.yawl.elements.state.YMarking;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

/**
 * Legacy state-space OR-join enablement evaluator (moved from YNet)
 * @author Michael Adams
 * @date 09/10/2026
 */
public class OrJoinStateSpaceEvaluator implements OrJoinEvaluator {

    private final YNet _net;


    public OrJoinStateSpaceEvaluator(YNet net) {
        _net = net;
    }


    /**
     * Legacy method extracted from YNet
     * @param orJoinTask the or-join to check for enablement
     * @param caseID the current case id
     * @return true if the or-join is enabled via the legacy state-space strategy
     */
    @Override
    public boolean isOrJoinEnabled(YExternalNetElement orJoinTask, YIdentifier caseID) {
        YMarking actualMarking = new YMarking(caseID);
        List<YNetElement> locations = new Vector<YNetElement>(actualMarking.getLocations());
        Set<YExternalNetElement> preSet = orJoinTask.getPresetElements();
        if (new HashSet<>(locations).containsAll(preSet)) {
            return true;
        }

        for (YNetElement element : locations) {
            if (element instanceof YExternalNetElement netElement) {
                if (preSet.contains(netElement)) {
                    try {
                        E2WFOJNet e2Net = new E2WFOJNet(_net, (YTask) orJoinTask);
                        e2Net.restrictNet(actualMarking);
                        e2Net.restrictNet((YTask) orJoinTask);
                        return e2Net.orJoinEnabled(actualMarking, (YTask) orJoinTask);
                    }
                    catch (Exception e) {
                        throw new RuntimeException("Exception in OR-join call:" + e);
                    }
                }
            }
        }
        // or join task has no tokens in preset
        return false;
    }
}
