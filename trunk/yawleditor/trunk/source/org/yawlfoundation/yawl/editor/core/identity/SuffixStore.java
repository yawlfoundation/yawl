/*
 * Copyright (c) 2004-2013 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.editor.core.identity;

import java.util.HashSet;
import java.util.Set;

/**
 * Stores the used and available suffixes for a particular name. Uses boolean[]
 * rather than BitSet since size will be typically small (i.e. in all but the
 * most complex specifications).
 */
class SuffixStore {
    private int size = 4;                                 // initial size
    private boolean[] used = new boolean[size];

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
            if (! isUsed) {
                hasUnused = true;                         // found one unused
            }
            else {
                if (hasUnused) return true;               // used, but with prior unused
            }
        }
        return false;
    }


    Set<SuffixPair> rationalise() {
        Set<SuffixPair> changes = new HashSet<SuffixPair>();
        int firstFreeSlot = firstFree(0);
        int lastUsedSlot = lastUsed(size);
        while (firstFreeSlot > -1 && firstFreeSlot < lastUsedSlot) {
            changes.add(new SuffixPair(lastUsedSlot, firstFreeSlot));
            use(firstFreeSlot);
            free(lastUsedSlot);
            firstFreeSlot = firstFree(firstFreeSlot);
            lastUsedSlot = lastUsed(lastUsedSlot);
        }
        return changes;
    }


    private void growArray(int newSize) {
        boolean[] temp = new boolean[newSize];
        System.arraycopy(used, 0, temp, 0, size);
        used = temp;
        size = newSize;
    }


    private int firstFree(int start) {
        for (int i = start; i < size; i++) {
            if (! used[i]) return i;
        }
        return -1;  // no free slots
    }


    private int lastUsed(int start) {
        for (int i = start - 1; i >= 0; i--) {
            if (used[i]) return i;
        }
        return -1;  // no used slots
    }
}
