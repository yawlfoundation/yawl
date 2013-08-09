package org.yawlfoundation.yawl.editor.core.identity;

/**
 * Stores the used and available suffixes for a particular name. Uses boolean[]
 * rather than BitSet since size will be typically small (i.e. in all but the
 * most complex specifications).
 */
class SuffixStore {
    int size = 4;                                 // initial size
    boolean[] used = new boolean[size];

    int getNext() {
        int i = 0;
        while (used[i] && ++i < size);            // find a free suffix
        if (i == size) growArray(size * 2);       // all spots taken, double size
        used[i] = true;                           // mark as used
        return i;
    }

    void use(int suffix) {
        if (suffix > size - 1) growArray(suffix + 1);
        used[suffix] = true;                       // mark as used
    }

    void free(int suffix) {
        if (suffix > -1 && suffix < size) used[suffix] = false;   // mark as free
    }

    boolean isUsed(int suffix) {
        return suffix < size && used[suffix];
    }

    // returns true if there is a gap of unused suffixes between any used ones,
    // denoting that this store can be rationalised
    boolean isDirty() {
        boolean hasUnused = false;                        // assume no unused to begin
        for (boolean isUsed : used) {
            if (! isUsed) hasUnused = true;               // found one unused
            else if (hasUnused) return true;              // used, but with prior unused
        }
        return false;
    }

    private void growArray(int newSize) {
        boolean[] temp = new boolean[newSize];
        System.arraycopy(used, 0, temp, 0, size);
        used = temp;
        size = newSize;
    }
}
