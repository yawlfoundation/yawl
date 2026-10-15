package org.yawlfoundation.yawl.elements.pdoj;

import org.yawlfoundation.yawl.elements.*;

import java.util.*;

/**
 * Builds a matrix of post-dominance between pairs of all elements in a YNet
 * @author Michael Adams
 * @date 09/10/2026
 */
public class PostDominanceMatrix {

    private final List<YExternalNetElement> _allElements;
    private final YOutputCondition _outputCondition;
    private final Map<YExternalNetElement, Set<YExternalNetElement>> _pdMap;
    private final Map<YExternalNetElement, YExternalNetElement> _immediatePdMap;

    
    public PostDominanceMatrix(YNet net) {
        _outputCondition = net.getOutputCondition();
        _allElements = new ArrayList<>(net.getNetElements().values());
        _pdMap = new HashMap<>();
        _immediatePdMap = new HashMap<>();
        constructPostDominatorMaps();
    }


    /**
     * @return true if e1 post-dominates 32
     */
    public boolean postDominates(YExternalNetElement e1, YExternalNetElement e2) {
        if (e1.equals(e2)) return true;

        Set<YExternalNetElement> pds = _pdMap.get(e2);
        return pds != null && pds.contains(e1);
    }


    /**
     * Validates if the net is well-ordered according to Definition 12:
     *  For any two connected OR-joins (n, m), either n = m,
     *  n post-dominates m, or m post-dominates n.
     */
    public boolean isWellOrdered() {
        List<YExternalNetElement> orJoins = extractOrJoins();
        if (orJoins.size() < 2) {
            return true;
        }

        // check every pair
        for (int i = 0; i < orJoins.size(); i++) {
            YExternalNetElement n = orJoins.get(i);

            for (int j = i + 1; j < orJoins.size(); j++) {
                YExternalNetElement m = orJoins.get(j);

                // check they share a common path (connected in either direction)
                if (hasPath(n, m) || hasPath(m, n)) {

                    // one must post-dominate the other
                    boolean nPostDominatesM = postDominates(n, m);
                    boolean mPostDominatesN = postDominates(m, n);

                    if (! nPostDominatesM && ! mPostDominatesN) {
                        // Violation found: They are on a common path but neither post-dominates the other
                        return false;
                    }
                }
            }
        }
        return true; // No violations found
    }


    /**
     * Gets the immediate post-dominator for a specific net element.
     * @return The immediate post-dominator, or null if it has none (like the output condition).
     */
    public YExternalNetElement getImmediatePostDominator(YExternalNetElement element) {
        return _immediatePdMap.get(element);
    }

    
    /**
     * Detects if a directed path exists from 'source' to 'target'
     * using a standard Depth-First Search
     */
    private boolean hasPath(YExternalNetElement source, YExternalNetElement target) {
        Queue<YExternalNetElement> queue = new LinkedList<>();
        Set<YExternalNetElement> visited = new HashSet<>();

        queue.add(source);
        visited.add(source);

        while (!queue.isEmpty()) {
            YExternalNetElement current = queue.poll();
            if (current.equals(target)) {                     // path found
                return true;
            }

            // move on to next element(s)
            for (YExternalNetElement successor : current.getPostsetElements()) {
                if (! visited.contains(successor)) {
                    visited.add(successor);
                    queue.add(successor);
                }
            }
        }
        return false;                                       // no path found
    }


    /**
     * Builds both the complete post-dominator sets (_pdMap) and the single
     * immediate post-dominator assignments (_immediatePdMap) for all net elements.
     *
     * Post-dominator sets are repeatedly refined until a fixed point is reached
     * (i.e., no more changes occur).
     */
    private void constructPostDominatorMaps() {
        initialiseSets();

        boolean changed = true;
        while (changed) {
            changed = false;

            for (YExternalNetElement element : _allElements) {
                if (! element.equals(_outputCondition)) {          // output has no PDs
                    Set<YExternalNetElement> immediateSuccessors =
                            new HashSet<>(element.getPostsetElements());
                    Set<YExternalNetElement> successorsIntersect = new HashSet<>();

                    // Compute the intersection of post-dominators across all successor paths.
                    // For a element n to be strictly post-dominated by element m, m must
                    // be found on every single path leading out of n.
                    if (! immediateSuccessors.isEmpty()) {
                        Iterator<YExternalNetElement> itr = immediateSuccessors.iterator();

                        // Start with the post-dominator set of the first successor
                        successorsIntersect.addAll(_pdMap.get(itr.next()));

                        // Keep only elements that are also present in all subsequent
                        // successors' sets
                        while (itr.hasNext()) {
                            successorsIntersect.retainAll(_pdMap.get(itr.next()));
                        }
                    }

                    // Combine the element itself with the shared post-dominators of all
                    // its successors.
                    Set<YExternalNetElement> postDominators = new HashSet<>();
                    postDominators.add(element);
                    postDominators.addAll(successorsIntersect);

                    // If the newly calculated set is smaller than the previous
                    // iteration, update the map and do another pass.
                    if (! postDominators.equals(_pdMap.get(element))) {
                        _pdMap.put(element, postDominators);
                        changed = true;
                    }
                }
            }
        }

        // Now the global post-dominator sets are stable, extract the single
        // unique 'closest' post-dominator for every element.
        computeImmediateDominators();
    }


    /**
     * Initialise the maps to begin with full element sets (to be pruned later)
     */
    private void initialiseSets() {

        // setup set for output condition -> no successors
        Set<YExternalNetElement> endSet = new HashSet<>();
        endSet.add(_outputCondition);
        _pdMap.put(_outputCondition, endSet);

        // init all other elements with full element set
        for (YExternalNetElement netElement : _allElements) {
            if (netElement != _outputCondition) {
                _pdMap.put(netElement, new HashSet<>(_allElements));
            }
        }
    }


    /**
     * Maps each element with their immediate post-dominator (i.e. the element where all
     * paths from a particular element converge)
     */
    private void computeImmediateDominators() {
        _immediatePdMap.put(_outputCondition, null);           // output has no PD

        for (YExternalNetElement element : _allElements) {
            if (! element.equals(_outputCondition)) {

                // Get the complete set of all post-dominators for this element
                Set<YExternalNetElement> possibles = new HashMap<>(_pdMap).get(element);
                if (possibles == null) continue;

                // Find the closest strict post-dominator
                Set<YExternalNetElement> strictPDs = new HashSet<>(possibles);
                strictPDs.remove(element);

                YExternalNetElement ipd = null;
                for (YExternalNetElement p : strictPDs) {
                    boolean dominatesOtherStrictPD = false;
                    for (YExternalNetElement other : strictPDs) {
                        if (p.equals(other)) continue;

                        // If the other candidate's post-dominance set contains p,
                        // it means 'other' happens first, and p post-dominates other
                        if (_pdMap.get(other).contains(p)) {
                            dominatesOtherStrictPD = true;
                            break;
                        }
                    }

                    // If p does not post-dominate any other element in the strict set,
                    // it is structurally the closest, unique convergence point.
                    if (! dominatesOtherStrictPD) {
                        ipd = p;
                        break;
                    }
                }
                _immediatePdMap.put(element, ipd);
            }
        }
    }


    /**
     * @return The list of all OR-joins in the net
     */
    protected List<YExternalNetElement> extractOrJoins() {
        List<YExternalNetElement> orJoins = new ArrayList<>();
        for (YExternalNetElement element : _allElements) {
            if (element instanceof YTask task) {
                if (task.getJoinType() == YTask._OR) {
                    orJoins.add(task);
                }
            }
        }
        return orJoins;
    }


    /**
     * @return The list of net elements that are currently marked (i.e. contain at
     * least one token)
     */
    protected List<YExternalNetElement> getMarkedElements() {
        List<YExternalNetElement> marked = new ArrayList<>();
        for (YExternalNetElement element : _allElements) {
            if (element instanceof YCondition condition) {
                if (condition.containsIdentifier()) {
                    marked.add(condition);
                }
            }
            else if (element instanceof YTask task) {
                if (task.getMIActive().containsIdentifier()) {
                    marked.add(task);
                }
            }
        }
        return marked;
    }
    
}
