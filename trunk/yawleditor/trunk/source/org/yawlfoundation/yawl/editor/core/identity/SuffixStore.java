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
