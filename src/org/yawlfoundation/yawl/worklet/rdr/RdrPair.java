/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.rdr;

/**
 * This class holds the last evaluated node, and the last node that evaluated to true,
 * as the result of a tree search
 *
 * @author Michael Adams
 * @date 19/09/2014
 */
public class RdrPair {

    private RdrNode lastTrue;
    private RdrNode lastEvaluated;

    public RdrPair() { }

    public RdrPair(RdrNode lTrue, RdrNode lEvaluated) {
        lastTrue = lTrue;
        lastEvaluated = lEvaluated;
    }


    public boolean isPairEqual() {
        return lastTrue != null && lastEvaluated != null && lastTrue == lastEvaluated;
    }


    public RdrNode getParentForNewNode() {
        return isPairEqual() ? getLastTrueNode() : getLastEvaluatedNode();
    }


    public RdrNode getLastTrueNode() { return lastTrue; }


    public RdrNode getLastEvaluatedNode() { return lastEvaluated; }


    public RdrConclusion getConclusion() {
        return lastTrue != null ? lastTrue.getConclusion() : null;
    }


    public boolean hasNullConclusion() {
        RdrConclusion conclusion = getConclusion();
        return conclusion == null || conclusion.isNullConclusion();
    }


    public String toString() {
        String trueString = lastTrue != null ? lastTrue.toXML() : "";
        String evalString = lastEvaluated != null ? lastEvaluated.toXML() : "";
        return trueString + ":::" + evalString;
    }
}
