package org.yawlfoundation.yawl.elements.pdoj;

import org.yawlfoundation.yawl.elements.YExternalNetElement;
import org.yawlfoundation.yawl.elements.YNet;
import org.yawlfoundation.yawl.elements.state.YIdentifier;

import java.util.*;

/**
 * Determines if an OR-join is enabled using Total Order Semantics (or simple
 * Post-Dominance semantics if that strategy is set on startup)
 * @author Michael Adams
 * @date 09/10/2026
 */
public class OrJoinTotalOrderEvaluator implements OrJoinEvaluator {

    private final List<YExternalNetElement> _allOrJoins;
    private final Comparator<YExternalNetElement> _totalOrderComparator;
    private final PostDominanceMatrix _matrix;

    // determine the strategy for checking (post-dominance with/out total order)
    private final OrJoinEnablementStrategy _enablementStrategy =
            OrJoinEnablementStrategy.getSelectedStrategy();

    
    public OrJoinTotalOrderEvaluator(YNet net) {
        _matrix = new PostDominanceMatrix(net);
        _allOrJoins = _matrix.extractOrJoins();
        _totalOrderComparator = new TotalOrderComparator(_matrix);
    }

    
    /**
     * Determines if a target OR-join gateway is enabled under Total Order Semantics.
     * Implements Definition 11.
     *
     * @param orJoin The OR-join gateway (n) we are checking.
     * @param caseID (unused)
     */
    public boolean isOrJoinEnabled(YExternalNetElement orJoin, YIdentifier caseID) {
        List<YExternalNetElement> marked = _matrix.getMarkedElements();
        List<YExternalNetElement> unmarkedPresetElements = new ArrayList<>();
        
        // Condition 1: Check for an incoming token in the preset (•n)
        boolean markedInput = false;
        for (YExternalNetElement presetElement : orJoin.getPresetElements()) {
            if (marked.contains(presetElement)) {
                markedInput = true;
            }
            else {

                // Path segment is empty (S(nk-1, nk) == 0).
                // We must look upstream from here to see if a token is coming.
                unmarkedPresetElements.add(presetElement);
            }
        }
        if (! markedInput) {
            return false;    // No token on any preset -> Not enabled
        }

        // Condition 2: Backward traversal to look for unblocked token paths
        Queue<YExternalNetElement> queue = new LinkedList<>();
        Set<YExternalNetElement> visited = new HashSet<>();

        // Initialize queue with the immediate unmarked preset elements of the OR-join
        for (YExternalNetElement presetElement : unmarkedPresetElements) {
            queue.add(presetElement);
            visited.add(presetElement);
        }

        while (! queue.isEmpty()) {
            YExternalNetElement current = queue.poll();

            // If there is an active token on this flow
            if (marked.contains(current)) {
                
                // We have found a path back to a token that was NOT blocked by a
                // higher-priority OR-join. According to Definition 11, this means
                // {nj | n ⪯OR nj} is empty on this path segment, so this OR-join is
                // not enabled.
                return false;
            }

            // Move upstream
            for (YExternalNetElement upstream : current.getPresetElements()) {
                if (! visited.contains(upstream)) {

                    // If this upstream element is an OR-join
                    if (_allOrJoins.contains(upstream)) {

                        // if we want to evaluate on post-dominance + priority
                        if (usingTotalOrderStrategy()) {

                            // Does it satisfy the n ⪯OR nj total order rule?
                            if (_totalOrderComparator.compare(orJoin, upstream) <= 0) {

                                // path is blocked by a higher or equal priority OR-join.
                                // traverse no further on this path (disregard it).
                                continue;
                            }
                        }

                        // else we want post-dominance check only
                        else {
                            if (_matrix.postDominates(upstream, orJoin)) {
                                
                                // path is safely structural-blocked;
                                // traverse no further on this path (disregard it).
                                continue;
                            }
                        }
                    }

                    visited.add(upstream);
                    queue.add(upstream);
                }
            }
        }

        // No tokens found on any other backward path besides the immediate preset.
        // OR-join is considered enabled
        return true;
    }


    private boolean usingTotalOrderStrategy() {
        return _enablementStrategy == OrJoinEnablementStrategy.TOTAL_ORDER;
    }

}

