package org.yawlfoundation.yawl.simulation;

/**
 * @author Michael Adams
 * @date 15/10/12
 */
class ResourceLimit {

    private int limit;
    private int cumulative;

    ResourceLimit(int limit) {
        this.limit = limit > -1 ? limit : Integer.MAX_VALUE;
        cumulative = 0;
    }

    void increment(int time) { cumulative += time; }

    boolean hasBeenExceeded() { return cumulative >= limit; }


}
