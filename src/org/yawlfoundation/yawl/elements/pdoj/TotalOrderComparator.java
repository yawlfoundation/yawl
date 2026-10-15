package org.yawlfoundation.yawl.elements.pdoj;

import org.yawlfoundation.yawl.elements.YExternalNetElement;

import java.util.Comparator;


/**
 * Compares two OR-joins using Total Order Semantics
 * @author Michael Adams
 * @date 10/10/2026
 */
public class TotalOrderComparator implements Comparator<YExternalNetElement> {

    private final PostDominanceMatrix _postDominanceMatrix;

    public TotalOrderComparator(PostDominanceMatrix postDominanceMatrix) {
        this._postDominanceMatrix = postDominanceMatrix;
    }

    @Override
    public int compare(YExternalNetElement n, YExternalNetElement m) {
        
        // Handle reflexivity (n ⪯OR n is always true)
        if (n.equals(m)) {
            return 0;
        }

        // m post-dominates n (n ⪯OR m)
        if (_postDominanceMatrix.postDominates(m, n)) {
            return -1; // n has lower priority
        }

        // n post-dominates m (m ⪯OR n)
        if (_postDominanceMatrix.postDominates(n, m)) {
            return 1; // n has higher priority
        }

        // Calculate a pseudo total order for parallel or independent OR-joins,
        // using their id strings to calculate an ordering.
        String idN = n.getID();
        String idM = m.getID();

        if (idN == null) return -1;
        if (idM == null) return 1;

        int idComparison = idN.compareTo(idM);

        // safety check (should never happen): if IDs are identical but objects are not
        return idComparison != 0 ? idComparison :
                Integer.compare(n.hashCode(), m.hashCode());
    }
    
}

